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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.importer.ImportWatchService;
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
	
	@Inject
    private Logger log;
	
	@Inject
    private ConfigurationService configurationService;
	
	@Inject
    private ImportService importService;
	
	@Inject
	private ContextService contextService;
	
	@Inject
	private UserService userService;
	
	private String path;
	
	public ImportWatchServiceImpl() {
		
	}
	
	@Override
    public void initialize(@Observes SystemStartupEvent event) {
		this.path = configurationService.getHome() + File.separator + ConfigurationService.DIR_IMPORT;
        try {
        	Path path = Paths.get(this.path);
        	WatchService watchService = path.getFileSystem().newWatchService();
			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
			while (true) {
				final WatchKey key = watchService.take();
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					if (StandardWatchEventKinds.ENTRY_CREATE.equals(watchEvent.kind())) { //TODO: is it necessary?
						@SuppressWarnings("unchecked") final Path item = ((WatchEvent<Path>) watchEvent).context();
						if (execImport(item)) {
							log.info("Sucessfully imported file '{}'!", item.toString());
							Files.delete(item);
						}
					}
				}
				if (!key.reset()) {
					// exit loop if the key is not valid
					// e.g. if the directory was deleted
					break;
				}
			}
		} catch (IOException e) {
			log.error("Error registering the import watch service over '{}': {}", this.path, e.getMessage());
		} catch (InterruptedException e) {
			log.error("Import watch service has been interrupted");
		}
    }
	
	@Override
	public boolean execImport(Path item) {
		try {
			importService.importData(Files.newInputStream(item), 
					Rio.getParserFormatForFileName(item.toString()).getDefaultMIMEType(), 
					userService.getAdminUser(), 
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
