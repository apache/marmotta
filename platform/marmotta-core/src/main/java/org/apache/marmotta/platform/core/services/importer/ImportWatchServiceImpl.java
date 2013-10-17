/*
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.nio.watch.SimpleTreeWatcher;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.importer.ImportService;
import org.apache.marmotta.platform.core.api.importer.ImportWatchService;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.ContextService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Implementation for watching import directory.
 * This service watches the import directory (see {@link #getImportRoot()}) for (new) files and imports them. 
 * 
 * @author Sergio Fernández
 * @author Jakob Frank <jakob@apache.org>
 * 
 */
@ApplicationScoped
public class ImportWatchServiceImpl implements ImportWatchService {

	private static final String CONFIG_PREFIX = "file-import.";
    private static final String CONFIG_KEY_LOCK_FILE = CONFIG_PREFIX + "lockFile";
    private static final String CONFIG_KEY_CONF_FILE = CONFIG_PREFIX + "dirConfigFile";
    private static final String CONFIG_KEY_IMPORT_DELAY = CONFIG_PREFIX + "importDelay";
    private static final String CONFIG_KEY_DELETE_AFTER_IMPORT = CONFIG_PREFIX + "deleteAfterImport";
    private static final String CONFIG_KEY_SERVICE_ENABLED = CONFIG_PREFIX + "enabled";

    private static final String TASK_GROUP = "Import Watch";

	private static final String TASK_DETAIL_PATH = "path";
	private static final String TASK_DETAIL_QUEUE = "import queue";
	
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
	
    private ImportWatcher importWatcher = null;

    /**
     * Initialize and start the watcher service.
     */
	@Override
	public void startup() {
	    if (importWatcher == null && configurationService.getBooleanConfiguration(CONFIG_KEY_SERVICE_ENABLED, true)) {
    	    importWatcher = new ImportWatcher(getImportRoot());
    	    importWatcher.setDeleteAfterImport(configurationService.getBooleanConfiguration(CONFIG_KEY_DELETE_AFTER_IMPORT, true));
            importWatcher.setImportDelay(configurationService.getIntConfiguration(CONFIG_KEY_IMPORT_DELAY, 2500));
    	    importWatcher.setDirConfigFileName(configurationService.getStringConfiguration(CONFIG_KEY_CONF_FILE, "config"));
    	    importWatcher.setLockFile(configurationService.getStringConfiguration(CONFIG_KEY_LOCK_FILE, "lock"));
    	    new Thread(importWatcher).start();
	    }
	}

	/**
	 * The import root. all files put into this directory (and any subdir) will be imported.
	 * Directories containing a file called "lock" (configurable, see {@link #CONFIG_KEY_LOCK_FILE}) are ignored.
	 */
	@Override
    public Path getImportRoot() {
        return Paths.get(configurationService.getHome(), ConfigurationService.DIR_IMPORT).toAbsolutePath();
    }
	
	/**
	 * Shutdown the directory.
	 */
    @Override
    public void shutdown() {
        if (importWatcher != null) {
            try {
                importWatcher.shutdown();
            } catch (IOException e) {
                log.error("Exception while shutting down import watcher: {}\n{}", e.getMessage(), e);
            }
            importWatcher = null;
        }
    }
    
    protected void onConfigurationChangedEvent(@Observes ConfigurationChangedEvent event) {
        if (event.containsChangedKeyWithPrefix(CONFIG_PREFIX)) {
            if (event.containsChangedKey(CONFIG_KEY_SERVICE_ENABLED)) {
                shutdown();
                startup();
            } else if (importWatcher != null) {
                importWatcher.setDeleteAfterImport(configurationService.getBooleanConfiguration(CONFIG_KEY_DELETE_AFTER_IMPORT, true));
                importWatcher.setImportDelay(configurationService.getIntConfiguration(CONFIG_KEY_IMPORT_DELAY, 2500));
                importWatcher.setDirConfigFileName(configurationService.getStringConfiguration(CONFIG_KEY_CONF_FILE, "config"));
                importWatcher.setLockFile(configurationService.getStringConfiguration(CONFIG_KEY_LOCK_FILE, "lock"));
            }
        }
    }

	protected void onSystemStartupEvent(@Observes SystemStartupEvent event) {
	    shutdown();
	    startup();
	}

	/**
	 * Import the given file.
	 * @see #importFile(Path)
	 */
	@Override
	public boolean importFile(File file) throws MarmottaImportException {
	    return importFile(file.toPath());
	}
	
	/**
	 * Import the given file.
	 * TODO
	 */
	@Override
	public boolean importFile(Path file) throws MarmottaImportException {
	    try {
	        URI context;
            try {
                context = getTargetContext(file);
            } catch (URISyntaxException e) {
                log.warn("Could not build context for file {}: {}", file, e.getMessage());
                context = null;
            }
	        String format = detectFormat(file);
	        FileInputStream is = new FileInputStream(file.toFile());
	        URI user = userService.getAdminUser();
	        importService.importData(is, format, user, context);
	        return true;
	    } catch (IOException e) {
	        throw new MarmottaImportException("Could not read input file " + file.toFile().getAbsolutePath(), e);
	    }
	}

	/**
	 * Detect the import format of the given file (mime-type)
	 * @param file the file to check
	 * @return the mime-type
	 * @throws MarmottaImportException
	 */
	private String detectFormat(Path file) throws MarmottaImportException {
		String format = null;
		final String fileName = file.toFile().getName();
		
		//mimetype detection
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(fileName);
		if (rdfFormat != null && importService.getAcceptTypes().contains(rdfFormat.getDefaultMIMEType())) {
			format = rdfFormat.getDefaultMIMEType();
		} else {
			throw new MarmottaImportException("Suitable RDF parser not found");
		}

	    //encoding detection
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file.toFile()))) {
			CharsetDetector cd = new CharsetDetector();
			cd.setText(bis);
			CharsetMatch cm = cd.detect();
			if (cm != null) {
				format += "; charset=" + cm.getName();
			}
		} catch (IOException e) {
			log.error("Error detecting charset for '{}': {}", fileName, e.getMessage());
		}

		return format;
	}
	
	/**
	 * Get the target context. 
	 * The algorithm is as follows:
	 * <ol>
	 * <li>check for a file "conf" (configurable, see {@link #CONFIG_KEY_CONF_FILE}) which specifies 
	 * the target content using {@link Properties} syntax (key {@code context}), then use is; or
	 * <li>check if the sub-directory is a url-encoded URI, then use it; or
	 * <li>construct the context by using {@link ConfigurationService#getBaseContext()} and the relative sub-dirs and use it; or
	 * <li>use the default context as a general fallback.
	 * </ol>
	 * 
	 * @param file the file
	 * @return the context URI
	 * @throws URISyntaxException 
	 */
	private URI getTargetContext(Path file) throws URISyntaxException {
	    // Check for a configFile
	    final Path config = file.getParent().resolve(configurationService.getStringConfiguration(CONFIG_KEY_CONF_FILE, "config"));
	    if (Files.isReadable(config)) {
	        try {
	            Properties prop = new Properties();
	            prop.load(new FileInputStream(config.toFile()));
	            final String _c = prop.getProperty("context");
	            if (_c != null) {
	                try {
	                    URI context = contextService.createContext(_c);
	                    log.debug("using context {} from config file {}", context, config);
	                    return context;
	                } catch (URISyntaxException e) {
	                    log.warn("invalid context {} in config file {}, ignoring", _c, config);
	                }
	            } else {
	                log.trace("no context defined in config file {}", config);
	            }
	        } catch (IOException e) {
	            log.warn("could not read dirConfigFile {}: {}", config, e.getMessage());
	        }
	    }

	    // Check for url-encoded directory
	    Path subDir = file.getParent().relativize(getImportRoot());
	    if (StringUtils.isBlank(subDir.toString())) {
	        log.trace("using default context for file {}", file);
	        return contextService.getDefaultContext();
	    } else if (StringUtils.startsWith(subDir.toString(), "http%3A%2F%2F")){
	        log.debug("using url-encoded context {} for import of {}", subDir, file);
	        try {
	            return contextService.createContext(URLDecoder.decode(subDir.toString(), "UTF-8"));
	        } catch (UnsupportedEncodingException e) {
	            log.error("Error url-decoding context name '{}', so using the default one: {}", subDir, e.getMessage());
	            return contextService.getDefaultContext();
	        }
	    } else {
	        final String _c = String.format("%s/%s", configurationService.getBaseContext().replaceFirst("/$", ""), subDir);
	        final URI context = contextService.createContext(_c);
	        log.debug("using context {} based on relative subdir {} for file {}", context, subDir, file);
            return context;
	    }
	}
	
	private class ImportWatcher extends SimpleTreeWatcher {

        private String dirConfigFileName = null;
        private boolean deleteAfterImport = false;
        private int importDelay = 2500;
        private String lockFile = null;
        
        private final ScheduledThreadPoolExecutor executor;
        private final Map<Path, ScheduledFuture<?>> fileSchedules;
        
        private final Task task;

        public ImportWatcher(Path target) {
            super(target, true);
            
            executor = new ScheduledThreadPoolExecutor(1);
            executor.setMaximumPoolSize(1);

            fileSchedules = new HashMap<>();
            
            task = taskManagerService.createTask("Import Watch", TASK_GROUP);
            task.updateMessage("off");
            task.updateDetailMessage(TASK_DETAIL_PATH, target.toAbsolutePath().toString());
        }
        
        public void setLockFile(String lockFile) {
            this.lockFile = lockFile;
        }

        public void setDirConfigFileName(String configFileName) {
            this.dirConfigFileName = configFileName;
        }

        public void setDeleteAfterImport(boolean deleteAfterImport) {
            this.deleteAfterImport = deleteAfterImport;
        }
        
        /**
         * Wait for some time before actually starting the import.
         * @param importDelay the delay in milliseconds.
         */
        public void setImportDelay(int importDelay) {
            this.importDelay = importDelay;
        }

        @Override
        public void run() {
            task.updateMessage("waiting for new files");
            scheduleDirectoryRecursive(root);
            super.run();
        }
        
        @Override
        public void shutdown() throws IOException {
            try {
                task.updateMessage("shutting down");
                super.shutdown();
                executor.shutdownNow();
            } finally {
                task.endTask();
            }
        }
        
        @Override
        public void onChildDeleted(final Path parent, Path child) {
            // if the lockfile is deleted, import the full directory
            if (lockFile != null && child.endsWith(lockFile)) {
                scheduleDirectory(parent);
            } else {
                // otherwise remove a potential scheduled import
                final ScheduledFuture<?> scheduled = fileSchedules.remove(child);
                if (scheduled != null) {
                    scheduled.cancel(true);
                    updateQueueSizeMonitor();
                }
            }
        }
        
        private void scheduleDirectory(Path dir) {
            if (!isLocked(dir)) {
                try {
                    Files.walkFileTree(dir, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file,
                                BasicFileAttributes attrs) throws IOException {
                            scheduleFile(file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    log.warn("Could not schedule directory {} for import: {}", dir, e.getMessage());
                }
            }
        }
        
        private boolean isLocked(Path dir) {
            if (lockFile == null) {
                return false;
            } else {
                return Files.exists(dir.resolve(lockFile));
            }
        }

        private void scheduleFile(final Path file) {
            // if the dir is locked, do not schedule
            if (isLocked(file.getParent())) return;
            
            // do not schedule a config file
            if (dirConfigFileName != null && file.endsWith(dirConfigFileName)) return;

            // schedule the import
            final ScheduledFuture<?> prevSchedule = fileSchedules.put(file, executor.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.updateMessage("importing " + file);
                        final File f = file.toFile();
                        if (importFile(f)) {
                            fileSchedules.remove(file);
                            updateQueueSizeMonitor();
                            if (deleteAfterImport) {
                                Files.delete(file);
                            }
                        }
                    } catch (IOException e) {
                        log.warn("Could not delete file {} after successful import: {}", file, e.getMessage());
                    } catch (MarmottaImportException e) {
                        log.warn("importing {} failed: {}", file, e.getMessage());
                    } catch (final Throwable t) {
                        log.error("{} during file-import: {}", t.getClass().getSimpleName(), t.getMessage());
                        throw t;
                    } finally {
                        task.updateMessage("waiting for new files");
                    }
                }
            }, importDelay, TimeUnit.MILLISECONDS));
            
            // cancel any previously scheduled import for this file.
            if (prevSchedule != null) {
                prevSchedule.cancel(true);
            }

            updateQueueSizeMonitor();
        }

        private void updateQueueSizeMonitor() {
            task.updateDetailMessage(TASK_DETAIL_QUEUE, executor.getQueue().size() + " files");
        }

        @Override
        public void onFileCreated(Path createdFile) {
            scheduleFile(createdFile);
        }
        
        @Override
        public void onFileModified(Path modifiedFile) {
            scheduleFile(modifiedFile);
        }
        
        @Override
        public void onDirectoryCreated(Path createdDir) {
            scheduleDirectoryRecursive(createdDir);
        }

        private void scheduleDirectoryRecursive(Path directory) {
            try {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path> () {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir,
                            BasicFileAttributes attrs) throws IOException {
                        scheduleDirectory(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                log.warn("Could not schedule directory {} for import: {}", directory, e.getMessage());
            }
        }
	    
	}

}
