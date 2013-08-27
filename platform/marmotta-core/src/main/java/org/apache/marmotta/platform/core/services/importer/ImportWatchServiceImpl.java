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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.importer.ImportWatchService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
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

	private String path;

	private int count;

	public ImportWatchServiceImpl() {
		count = 0;
	}

	@Override
	public void initialize(@Observes SystemStartupEvent event) {
		final String import_watch_path = configurationService.getHome() + File.separator + ConfigurationService.DIR_IMPORT;
		this.path = import_watch_path;

		Runnable r = new Runnable() {

			@Override
			public void run() {
				final Task task = taskManagerService.createTask("Directory import watch", TASK_GROUP);
				task.updateMessage("watching...");
				task.updateDetailMessage("path", import_watch_path);

				try {
					Path dir = Paths.get(path);
					WatchService watchService = dir.getFileSystem().newWatchService();
					dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
					while (true) {
						final WatchKey key = watchService.take();
						for (WatchEvent<?> event : key.pollEvents()) {
							if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) { // TODO: is it necessary?
								@SuppressWarnings("unchecked")
								Path item = dir.resolve(((WatchEvent<Path>) event).context());
								log.debug("Importing '{}'...", item.toString());
								task.updateMessage("importing...");
								task.updateDetailMessage("path", item.toString());
								if (execImport(item)) {
									log.info("Sucessfully imported file '{}'!", item.toString());
									Files.delete(item);
								}
								task.updateProgress(++count);
								task.updateMessage("watching...");
								task.updateDetailMessage("path", import_watch_path);
							}
						}
						if (!key.reset()) {
							// exit loop if the key is not valid
							// e.g. if the directory was deleted
							break;
						}
					}
				} catch (IOException e) {
					log.error(
							"Error registering the import watch service over '{}': {}",
							import_watch_path, e.getMessage());
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
	public boolean execImport(Path item) {
		try {
			importService.importData(Files.newInputStream(item), Rio
					.getParserFormatForFileName(item.toString())
					.getDefaultMIMEType(), userService.getAdminUser(),
					contextService.getDefaultContext());
			return true;
		} catch (MarmottaImportException e) {
			log.error("Error importing file {} from the local directory: {}", item.toString(), e.getMessage());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error retrieving file {} from the local directory: {}", item.toString(), e.getMessage());
			return false;
		}
	}

}
