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
 * Interface specification for importer components that may be used for importing content
 * in various formats into the KiWi system.
 * <p>
 * Importers are typically implemented as stateless components that register themselves on
 * startup with the ImportService by calling registerImporter(); they usually make
 * use of the kiwiEntityManager for persisting entities and the tripleStore component for
 * adding/updating triples.
 * 
 * @author Sebastian Schaffert
 *
 */
public interface Importer {


	/**
	 * Get the name of this importer. Used for presentation to the user and for internal
	 * identification.
	 * 
	 * @return a string uniquely identifying this importer
	 */
	public String getName();

	/**
	 * Get a description of this importer for presentation to the user.
	 * 
	 * @return a string describing this importer for the user
	 */
	public String getDescription();


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
     *
     * @return the number of Content Items imported
	 * @throws org.apache.marmotta.platform.core.exception.io.MarmottaImportException in case the import fails
	 */
	public int importData(Reader reader, String format, Resource user, URI context) throws MarmottaImportException;

}
