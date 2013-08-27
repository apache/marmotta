/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.services.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.importer.ImportWatchService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.openrdf.model.URI;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

/**
 * Implementation for watching import directory
 * 
 * @author Sergio Fernández
 * 
 */
@ApplicationScoped
public class ImportWatchServiceImpl implements ImportWatchService {

	private static final String TASK_GROUP = "ImportWatch";

	@Inject
	private Logger log;

	@Inject
	private TaskManagerService taskManagerService;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private ImportService importService;

	@Inject
	private ContextService contextService;

	@Inject
	private UserService userService;
	
	private Map<WatchKey,Path> keys;

	private String path;

	private int count;

	public ImportWatchServiceImpl() {
		this.keys = new HashMap<WatchKey,Path>();
		count = 0;
	}

	@Override
	public void initialize(@Observes SystemStartupEvent event) {
		this.path = configurationService.getHome() + File.separator + ConfigurationService.DIR_IMPORT;

		Runnable r = new Runnable() {

			@Override
			public void run() {
				final Task task = taskManagerService.createTask("Directory import watch", TASK_GROUP);
				task.updateMessage("watching...");
				task.updateDetailMessage("path", path);

				try {
					Path root = Paths.get(path);
					WatchService watcher = root.getFileSystem().newWatchService();
					register(root, watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
					while (true) {
						final WatchKey key = watcher.take();
						for (WatchEvent<?> event : key.pollEvents()) {
							
							@SuppressWarnings("unchecked")
							Path item = ((WatchEvent<Path>) event).context();
							Path dir = keys.get(key);
							File file = new File(dir.toString(), item.toString()).getAbsoluteFile();
							
							if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
								if (file.isDirectory()) {
									//recursive registration of sub-directories
									register(Paths.get(path, item.toString()), watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
									task.updateProgress(++count);
								} else {
									log.debug("Importing '{}'...", file.getAbsolutePath());
									task.updateMessage("importing...");
									task.updateDetailMessage("path", file.getAbsolutePath());
									URI context = getTargetContext(file);
									if (execImport(file, context)) {
										log.info("Sucessfully imported file '{}' into {}", file.getAbsolutePath(), context.stringValue());
										try {
											//delete the imported file
											log.debug("Deleting {}...", file.getAbsolutePath());
											file.delete();
										} catch (Exception ex) {
											log.error("Error deleing {}: {}", file.getAbsolutePath(), ex.getMessage());
										}
									}
									task.updateProgress(++count);
									task.updateMessage("watching...");
									task.updateDetailMessage("path", path);
								}
							} else if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind()) && Files.isDirectory(item)) {
								//TODO: unregister deleted directories?
								task.updateProgress(++count);
							}
							
						}
						if (!key.reset()) {
							// exit loop if the key is not valid
							// e.g. if the directory was deleted
							break;
						}
					}
				} catch (IOException e) {
					log.error("Error registering the import watch service over '{}': {}", path, e.getMessage());
				} catch (InterruptedException e) {
					log.error("Import watch service has been interrupted");
				}
			}

		};

		Thread t = new Thread(r);
		t.setName(TASK_GROUP + "(start:" + new Date() + ",path:" + this.path + ")");
		t.setDaemon(true);
		t.start();

	}

	@Override
	public boolean execImport(File file, URI context) {
		try {
			importService.importData(new FileInputStream(file),
					Rio.getParserFormatForFileName(file.getName()).getDefaultMIMEType(), 
					userService.getAdminUser(), context);
			return true;
		} catch (MarmottaImportException e) {
			log.error("Error importing file {} from the local directory: {}", file.getAbsolutePath(), e.getMessage());
			return false;
		} catch (IOException e) {
			log.error("Error retrieving file {} from the local directory: {}", file.getAbsolutePath(), e.getMessage());
			return false;
		}
	}
	
	/**
	 * Get the target context, according the path relative to the base import directory
	 * 
	 * @param file
	 * @return
	 */
	private URI getTargetContext(File file) {
		String subdir = StringUtils.removeStart(file.getParentFile().getAbsolutePath(), this.path);
		if (StringUtils.isBlank(subdir)) {
			return contextService.getDefaultContext();
		} else {
			return contextService.createContext(configurationService.getBaseContext() + subdir.substring(1));
		}
	}
	
	/**
	 * Registers a new path in the watcher, keeping the path mapping for future uses
	 * 
	 * @param path
	 * @param watcher
	 * @param events
	 * @throws IOException
	 */
	private void register(Path path, WatchService watcher, Kind<?>... events) throws IOException {
		keys.put(path.register(watcher, events), path);
	}

}
