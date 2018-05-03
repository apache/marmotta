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
package org.apache.marmotta.platform.core.api.importer;

import java.io.File;
import java.nio.file.Path;

import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;

/**
 * A service for watching import directory
 *  
 * @author Sergio Fernández
 *
 */
public interface ImportWatchService {

	/**
	 * Import an observed item 
	 * 
	 * @param file
	 * @return
	 * @throws MarmottaImportException 
	 */
    boolean importFile(File file) throws MarmottaImportException;

    boolean importFile(Path file) throws MarmottaImportException;

    void startup();

    void shutdown();

    Path getImportRoot();

}
