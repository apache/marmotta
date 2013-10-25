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

import java.nio.file.Path;

/**
 * {@link SimpleTreeWatcher} with empty stub implementations of all abstract methods from {@link AbstractTreeWatcher}.
 * 
 * @author Jakob Frank <jakob@apache.org>
 *
 */
public class SimpleTreeWatcher extends AbstractTreeWatcher {

    /**
     * @param target
     * @param recursive
     */
    public SimpleTreeWatcher(Path target, boolean recursive) {
        super(target, recursive);
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.commons.nio.watch.AbstractTreeWatcher#onDirectoryCreated(java.nio.file.Path)
     */
    @Override
    public void onDirectoryCreated(Path createdDir) {
        // Empty placeholder hook, overwrite if required
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.commons.nio.watch.AbstractTreeWatcher#onFileCreated(java.nio.file.Path)
     */
    @Override
    public void onFileCreated(Path createdFile) {
        // Empty placeholder hook, overwrite if required    
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.commons.nio.watch.AbstractTreeWatcher#onFileModified(java.nio.file.Path)
     */
    @Override
    public void onFileModified(Path modifiedFile) {
        // Empty placeholder hook, overwrite if required
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.commons.nio.watch.AbstractTreeWatcher#onChildCreated(java.nio.file.Path, java.nio.file.Path)
     */
    @Override
    public void onChildCreated(Path parent, Path child) {
        // Empty placeholder hook, overwrite if required
    }

    /* (non-Javadoc)
     * @see org.apache.marmotta.commons.nio.watch.AbstractTreeWatcher#onChildDeleted(java.nio.file.Path, java.nio.file.Path)
     */
    @Override
    public void onChildDeleted(Path parent, Path child) {
        // Empty placeholder hook, overwrite if required
    }

}
