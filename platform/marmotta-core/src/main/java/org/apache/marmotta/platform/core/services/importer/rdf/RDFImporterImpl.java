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
package org.apache.marmotta.platform.core.services.importer.rdf;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.importer.Importer;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.exception.io.MarmottaImportException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserRegistry;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An io for importing RDF sources in RDF/XML or other RDF formats. Currently uses
 * the Sesame parser for parsing the RDF content.
 * <p>
 * For each triple found in the imported data, an appropriate KiWi triple is added. For each
 * resource in the imported data, the io creates a ContentItem using rdfs:label or
 * dc:title as title and rdfs:comment or dc:description as content.
 *
 * @author Sebastian Schaffert
 *
 */
@ApplicationScoped
public class RDFImporterImpl implements Importer {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private TaskManagerService   taskManagerService;

    @Inject
    private SesameService sesameService;

    private static long taskCounter = 0;

    private List<String> acceptTypes;

    /**
     * Get a collection of all mime types accepted by this io. Used for automatically
     * selecting the appropriate io in ImportService.
     *
     * @return a set of strings representing the mime types accepted by this io
     */
    @Override
    public Set<String> getAcceptTypes() {
        return new HashSet<String>(acceptTypes);
    }

    /**
     * Get a description of this io for presentation to the user.
     *
     * @return a string describing this io for the user
     */
    @Override
    public String getDescription() {
        return "Importer for various RDF formats (RDF/XML, N3, TURTLE); also supports OWL and RDFS files";
    }

    /**
     * Get the name of this io. Used for presentation to the user and for internal
     * identification.
     *
     * @return a string uniquely identifying this io
     */
    @Override
    public String getName() {
        return "RDF";
    }

    /**
     * Import data from the input stream provided as argument into the KiWi database.
     *
     * @param url the url from which to read the data
     * @param user the user to use as author of all imported data
     */
    @Override
    //@RaiseEvent("ontologyChanged")
    public int importData(URL url, String format, Resource user, URI context) throws MarmottaImportException {
        try {
            return importData(url.openStream(), format, user,context, url.toString());
        } catch (IOException ex) {
            log.error("I/O error while importing data from URL {}: {}", url, ex.getMessage());
            return 0;
        }
    }


    /**
     * Import data from the input stream provided as argument into the KiWi database.
     * <p>
     * Import function for formats supported by Sesame; imports the data first into a separate memory
     * repository, and then iterates over all statements, adding them to the current knowledge space.
     * This method also checks for resources that have a rdfs:label, dc:title, or skos:prefLabel and uses
     * it as the title for newly created ContentItems.
     *
     * @param is the input stream from which to read the data
     * @param user the user to use as author of all imported data
     */
    @Override
    //@RaiseEvent("ontologyChanged")
    public int importData(InputStream is, String format, Resource user, URI context) throws MarmottaImportException {
        String baseUri = configurationService.getBaseUri() + "resource/";
        return importData(is,format,user,context,baseUri);
    }

    /**
     * Import data from the input stream provided as argument into the KiWi database.
     * <p>
     * Import function for formats supported by Sesame; imports the data first into a separate memory
     * repository, and then iterates over all statements, adding them to the current knowledge space.
     * This method also checks for resources that have a rdfs:label, dc:title, or skos:prefLabel and uses
     * it as the title for newly created ContentItems.
     *
     * @param is the input stream from which to read the data
     * @param user the user to use as author of all imported data
     */
    //@RaiseEvent("ontologyChanged")
    private int importData(InputStream is, String format, Resource user, URI context, String baseUri) throws MarmottaImportException {

        // TODO: need to figure out format automatically!
        RDFFormat f = getFormat(format);

        final String taskName = String.format("RDF Importer Task %d (%s)", ++taskCounter, f.getName());
        Task task = taskManagerService.createSubTask(taskName,"Importer");
        task.updateMessage("importing data into Apache Marmotta repository");
        task.updateDetailMessage("format", f.getDefaultMIMEType());
        task.updateDetailMessage("baseUri", baseUri);

        int count = 0;

        try {
            if (is != null) {

                long timer = System.currentTimeMillis();

                RepositoryConnection c_import = sesameService.getConnection();
                try {
                    c_import.add(is, baseUri, f, context );
                } catch (RepositoryException ex) {
                    log.error("error while importing Sesame data:", ex);
                    c_import.rollback();
                    throw ex;
                } catch (RDFParseException ex) {
                    log.error("parse error while importing Sesame data:", ex);
                    c_import.rollback();
                    throw ex;
                } catch (IOException ex) {
                    log.error("I/O error while importing Sesame data:", ex);
                    c_import.rollback();
                    throw ex;
                } finally {
                    c_import.commit();
                    c_import.close();
                }
                log.debug("imported data into Apache Marmotta repository ({} ms)", System.currentTimeMillis() - timer);

            } else {
                log.error("could not load ontology; InputStream was null");
            }
        } catch (Exception ex) {
            log.error("error while importing Sesame data:", ex);
            throw new MarmottaImportException(ex);
        } finally {
            taskManagerService.endTask(task);
        }
        return count;

    }

    /**
     * Import data from the reader provided as argument into the KiWi database.
     * <p>
     * Import function for formats supported by Sesame; imports the data first into a separate memory
     * repository, and then iterates over all statements, adding them to the current knowledge space.
     * This method also checks for resources that have a rdfs:label, dc:title, or skos:prefLabel and uses
     * it as the title for newly created ContentItems.
     *
     * @param reader the reader from which to read the data
     * @param user the user to use as author of all imported data
     */
    @Override
    public int importData(Reader reader, String format, Resource user, URI context) throws MarmottaImportException {

        // TODO: need to figure out format automatically!
        RDFFormat f = getFormat(format);

        String baseUri = configurationService.getBaseUri() + "resource/";
        final String taskName = String.format("RDF Importer Task %d (%s)", ++taskCounter, f.getName());
        Task task = taskManagerService.createSubTask(taskName, "Importer");
        task.updateMessage("importing data into Apache Marmotta repository");
        task.updateDetailMessage("format", f.getDefaultMIMEType());
        task.updateDetailMessage("baseURI", baseUri);

        int count = 0;

        try {
            if (reader != null) {

                long timer = System.currentTimeMillis();

                RepositoryConnection c_import = sesameService.getConnection();
                try {
                    c_import.add(reader, baseUri, f, context);
                } catch (RepositoryException ex) {
                    log.error("error while importing Sesame data:", ex);
                    c_import.rollback();
                    throw ex;
                } catch (RDFParseException ex) {
                    log.error("parse error while importing Sesame data:", ex);
                    c_import.rollback();
                    throw ex;
                } catch (IOException ex) {
                    log.error("I/O error while importing Sesame data:", ex);
                    c_import.rollback();
                    throw ex;
                } finally {
                    c_import.commit();
                    c_import.close();
                }

                log.info("imported data into Apache Marmotta repository ({} ms)", System.currentTimeMillis() - timer);

            } else {
                log.error("could not load ontology; InputStream was null");
            }
        } catch (Exception ex) {
            log.error("error while importing Sesame data:", ex);
            throw new MarmottaImportException(ex);
        } finally {
            taskManagerService.endTask(task);
        }
        return count;
    }


    @PostConstruct
    public void initialise() {
        log.info("registering RDF importer ...");


        RDFParserRegistry parserRegistry = RDFParserRegistry.getInstance();

        acceptTypes = new ArrayList<String>();
        for(RDFFormat format : parserRegistry.getKeys()) {
            acceptTypes.addAll(format.getMIMETypes());
        }
        log.info(" - available parsers: {}", Arrays.toString(acceptTypes.toArray()));

    }


    private static RDFFormat getFormat(String format) {
        return RDFParserRegistry.getInstance().getFileFormatForMIMEType(format);
    }
}
