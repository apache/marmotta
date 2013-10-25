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
package org.apache.marmotta.commons.nio.watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractTreeWatcher is a convenience wrapper around the Java7 {@link WatchService}.
 * In most cases you will use the {@link SimpleTreeWatcher}
 * 
 * @see SimpleTreeWatcher
 * 
 * @author Jakob Frank <jakob@apache.org>
 * 
 */
public abstract class AbstractTreeWatcher implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final HashMap<WatchKey, Path> pathTable;
    protected final Path root;
    protected final boolean recursive;

    private WatchService watchService;

    /**
     * Create a new {@link AbstractTreeWatcher} watching on the target path.
     * @param target the {@link Path} to watch, must be a directory.
     * @param recursive will also watch subdirectories if <code>true</code>.
     */
    public AbstractTreeWatcher(Path target, boolean recursive) {
        this.root = target;
        this.recursive = recursive;
        this.pathTable = new HashMap<WatchKey, Path>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        log.debug("file-system watcher on {} ({}) starting up...", root, recursive?"recursive":"non-recursive");
        try {
            synchronized (this) {
                if (watchService == null) {
                    watchService = FileSystems.getDefault().newWatchService();
                }
            }
            pathTable.clear();
            if (recursive) {
                registerAll(watchService, root);
            } else {
                register(watchService, root);
            }
            log.debug("watching...");
            while (true) {
                try {
                    final WatchKey key = watchService.take();

                    final Path parent = pathTable.get(key);
                    if (parent == null) {
                        log.warn("WatchKey not recognized: {}, ignoring event", key);
                        continue;
                    }
                    try {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            final WatchEvent.Kind<?> kind = event.kind();

                            if (kind == OVERFLOW) {
                                log.trace("overflow event for {}", parent);
                                continue;
                            }

                            @SuppressWarnings("unchecked")
                            final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;

                            final Path localPath = pathEvent.context();
                            if (localPath == null) {
                                log.warn("Could not get context for %s in %s", kind, parent);
                                continue;
                            }
                            final Path target = parent.resolve(localPath);
                            if (kind == ENTRY_CREATE) {
                                if (Files.isDirectory(target)) {
                                    log.trace("created dir: {}", target);
                                    onDirectoryCreated(target);
                                    if (recursive) {
                                        registerAll(watchService, target);
                                    }
                                } else {
                                    log.trace("created file: {}", target);
                                    onFileCreated(target);
                                }
                                log.trace("new child in {}: {}", parent, localPath);
                                onChildCreated(parent, target);
                            } else if (kind == ENTRY_MODIFY) {
                                log.trace("modified file: {}", target);
                                onFileModified(target);
                            } else if (kind == ENTRY_DELETE) {
                                log.trace("deleted child in {}: {}", parent, localPath);
                                onChildDeleted(parent, target);
                            } else {
                                log.error("Unexpected event type: {}", kind);
                                continue;
                            }
                        }
                    } finally {
                        if (!key.reset()) {
                            pathTable.remove(key);
                        }
                    }
                } catch (InterruptedException | ClosedWatchServiceException e) {
                    log.trace("shutting down...");
                    break;
                }
            }
            watchService.close();
            watchService = null;
            log.info("file-system watcher on {} ({}) stopped.", root, recursive?"recursive":"non-recursive");
        } catch (IOException e) {
            log.error("file-system watcher on {} ({}) died: {}", root, recursive?"recursive":"non-recursive", e.getMessage());
        } finally {
            
        }
    }

    /**
     * Register the start dir and all subdirs for changes.
     * @param watcher the {@link WatchService} to register to
     * @param start the start directory
     * @throws IOException
     */
    private void registerAll(final WatchService watcher, final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(watcher, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Register the provided dir for changes.
     * @param watcher the {@link WatchService} to register to
     * @param dir the dir to register
     * @return the registered {@link WatchKey}
     * @throws IOException
     */
    private WatchKey register(WatchService watcher, Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        Path prev = pathTable.get(key);
        if (prev == null) {
            log.trace("new watch on {}", dir);
        } else {
            if (!dir.equals(prev)) {
                log.trace("updated watch on {} -> {}", prev, dir);
            }
        }
        pathTable.put(key, dir);
        return key;
    }

    /**
     * Shutdown the treewatcher.
     * @throws IOException
     */
    public void shutdown() throws IOException {
        if (watchService != null) {
            watchService.close();
        }
    }

    /**
     * Notification hook for created directories
     * @param createdDir the path of the created directory.
     * @see #onChildCreated(Path, Path)
     */
    public abstract void onDirectoryCreated(Path createdDir);

    /**
     * Notification hook for created files.
     * <strong>Note:</strong> after creation also a {@link #onFileModified(Path)} is invoked.
     * @param createdFile the path of the created file
     * @see #onFileModified(Path)
     * @see #onChildCreated(Path, Path)
     */
    public abstract void onFileCreated(Path createdFile);

    /**
     * Notificaton hook for modified files.
     * <strong>Note:</strong> this hook is also invoked after file creation.
     * @param modifiedFile the path of the modified file
     * @see #onFileCreated(Path)
     */
    public abstract void onFileModified(Path modifiedFile);

    /**
     * Notification hook for a created child in a directory.
     * @param parent the container of the newly created child
     * @param child the created child
     * @see #onFileCreated(Path)
     * @see #onDirectoryCreated(Path)
     */
    public abstract void onChildCreated(Path parent, Path child);

    /**
     * Notification hook for a created child in a directory
     * @param parent the container from which the child was deleted
     * @param child the local path of the deleted child
     */
    public abstract void onChildDeleted(Path parent, Path child);

}
