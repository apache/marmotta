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
package org.apache.marmotta.ldpath.backend.file;

import org.apache.marmotta.ldpath.backend.sesame.SesameRepositoryBackend;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class FileBackend extends SesameRepositoryBackend {

    private static final Logger log = LoggerFactory.getLogger(FileBackend.class);


    public FileBackend(File file) {
        this(file,null);
    }

    public FileBackend(File file, String mimetype) {
        super();

        RDFFormat format = null;

        if(mimetype != null) {
            format = Rio.getParserFormatForMIMEType(mimetype);
        }

        try {
            Repository repository = new SailRepository(new MemoryStore());
            repository.initialize();
            setRepository(repository);


            RepositoryConnection connection = repository.getConnection();
            try {
                connection.add(file,null,format);
            } finally {
                connection.close();
            }


        } catch (RDFParseException e) {
            log.error("error parsing RDF input data from file {}",file,e);
        } catch (IOException e) {
            log.error("I/O error while reading input data from file {}", file, e);
        } catch (RepositoryException e) {
            log.error("error initialising connection to Sesame in-memory repository",e);
        }
    }


    /**
     * Initialise a new sesame backend. Repository needs to be set using setRepository.
     */
    public FileBackend(String fileName) {
        this(new File(fileName));
    }

    public FileBackend(String fileName, String mimetype) {
        this(new File(fileName), mimetype);
    }


    public FileBackend(URL url) {
        this(url,null);
    }

    public FileBackend(URL url, String mimetype) {
        super();

        RDFFormat format = null;

        if(mimetype != null) {
            format = Rio.getParserFormatForMIMEType(mimetype);
        }

        try {
            Repository repository = new SailRepository(new MemoryStore());
            repository.initialize();
            setRepository(repository);


            RepositoryConnection connection = repository.getConnection();
            try {
                connection.add(url,null,format);
            } finally {
                connection.close();
            }


        } catch (RDFParseException e) {
            log.error("error parsing RDF input data from url {}",url,e);
        } catch (IOException e) {
            log.error("I/O error while reading input data from url {}", url, e);
        } catch (RepositoryException e) {
            log.error("error initialising connection to Sesame in-memory repository",e);
        }
    }

}
