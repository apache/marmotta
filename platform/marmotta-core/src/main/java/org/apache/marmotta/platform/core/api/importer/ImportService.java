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
package org.apache.marmotta.platform.core.api.importer;

import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Set;

/**
 * A service for importing and exporting different kinds of data into the KiWi system.
 * 
 * Should at least support:
 * - a native KiWi format
 * - RDF in XML format
 * 
 * @author Sebastian Schaffert
 *
 */
public interface ImportService {

	/**
	 * Get a collection of all mime types accepted by this importer. Used for automatically
	 * selecting the appropriate importer in ImportService.
	 *
	 * @return a set of strings representing the mime types accepted by this importer
	 */
	public Set<String> getAcceptTypes();

	/**
	 * Import data from the input stream provided as argument into the KiWi database.
	 *
	 *
     *
     * @param url the url from which to read the data
     * @param format the mime type of the import format
     * @param user the user to use as author of all imported data
	 * @param context the context of the import data; if null, default context is used
     *
     * @return the number of Content Items imported
	 * @throws org.apache.marmotta.platform.core.exception.io.MarmottaImportException in case the import fails
	 */
	public int importData(URL url, String format, Resource user, URI context) throws MarmottaImportException;


	/**
	 * Import data from the input stream provided as argument into the KiWi database.
	 *
	 *
     *
     * @param is the input stream from which to read the data
     * @param format the mime type of the import format
     * @param user the user to use as author of all imported data
	 * @param context the context of the import data; if null, default context is used
     *
     * @return the number of Content Items imported
	 * @throws org.apache.marmotta.platform.core.exception.io.MarmottaImportException in case the import cannot execute
	 */
	public int importData(InputStream is, String format, Resource user, URI context) throws MarmottaImportException;

	/**
	 * Import data from the reader provided as argument into the KiWi database.
	 *
	 *
     *
     * @param reader the reader from which to read the data
     * @param format the mime type of the import format
     * @param user the user to use as author of all imported data
	 * @param context the context of the import data; if null, default context is used
     *
     * @return the number of Content Items imported
	 * @throws org.apache.marmotta.platform.core.exception.io.MarmottaImportException in case the import fails
	 */
	public int importData(Reader reader, String format, Resource user, URI context) throws MarmottaImportException;

	
	/**
	 * Schedule an import for regular execution. When the ImportService is running, it
	 * checks at regular intervals whether the task is due and runs it if necessary.
	 * 
	 * @param description a human-readable description of the task
	 * @param interval a Date representing the interval in which to run the task
	 * @param url the URL to retrieve
	 * @param format the format of the content to retrieve
	 * @param types the types to associate with each imported content item
	 * @param tags the tags to associate with each imported content item
	 * @param user the user to set as author for each imported content item
	 */
	//public void scheduleImport(String description, Date interval, URL url, String format, Set<KiWiUriResource> types, Set<KiWiResource> tags, KiWiUser user);
	//TODO implement scheduled importer
	
}
