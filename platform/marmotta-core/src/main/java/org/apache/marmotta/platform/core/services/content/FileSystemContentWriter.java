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
package org.apache.marmotta.platform.core.services.content;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.apache.marmotta.commons.sesame.facading.FacadingFactory;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.content.ContentWriter;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.model.content.MediaContentItem;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import sun.net.www.MimeEntry;
import sun.net.www.MimeTable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.net.URI;
import java.util.UUID;

import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;

/**
 * A content writer that writes the content of a resource to the file system.
 * It uses the kiwi:hasContentPath property to determine the destination path of the content to write to.
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class FileSystemContentWriter implements ContentWriter {

    @Inject
    private Logger log;


    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    private String defaultDir;

    public FileSystemContentWriter() {
    }

    @PostConstruct
    public void initialise() {
        defaultDir = configurationService.getWorkDir()+File.separator+"resources";

        log.debug("FileSystem Content Writer started (default file location: {})",defaultDir);

        File dir = new File(defaultDir);
        if(!dir.exists() && !dir.mkdirs()) {
            log.warn("could not create default directory for file system storage of content (directory: {})",defaultDir);
        }
    }

    /**
     * Delete the content of the speficied mime type for the specified resource.
     *
     * @param resource the resource for which to delete the content
     * @param mimetype the mime type of the content to delete (optional)
     */
    @Override
    public void deleteContent(Resource resource, String mimetype) throws IOException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String path = mci.getContentPath();
                if(path != null) {
                    if(!path.startsWith(defaultDir)) {
                        if(!configurationService.getBooleanConfiguration("content.filesystem.secure")) {
                            log.warn("accessing file {}, which is outside the default directory; this is a potential security risk; " +
                                    "enable the option content.filesystem.secure in the configuration",path);
                        } else {
                            throw new FileNotFoundException("the file "+path+" is outside the Apache Marmotta default directory location; access denied");
                        }
                    }

                    File file = new File(path);
                    if(file.exists() && file.canWrite()) {
                        log.info("deleting file {} for resource {} ...", file.getPath(), resource);
                        file.delete();
                    } else {
                        throw new FileNotFoundException("could not delete file "+path+"; it does not exist or is not writable");
                    }
                } else {
                    throw new FileNotFoundException("could not delete file content for resource "+resource+"; no content path has been specified for the resource");
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
        }
    }

    /**
     * Return the name of the content reader. Used to identify and display the content reader to admin users.
     *
     * @return
     */
    @Override
    public String getName() {
        return "FileSystem Content Writer";
    }

    /**
     * Store the content of the specified mime type for the specified resource. Accepts a byte array containing
     * the byte data of the content that is then written to the destination configured for this writer.
     * <p/>
     *
     * @param resource the resource for which to store the content
     * @param mimetype the mime type of the content
     * @param data     a byte array containing the content of the resource
     */
    @Override
    public void setContentData(Resource resource, byte[] data, String mimetype) throws IOException {
        setContentStream(resource, new ByteArrayInputStream(data),mimetype);
    }

    /**
     * Store the content of the specified mime type for the specified resource. Accepts an input stream containing
     * the byte data of the content that is read and written to the destination configured for this writer.
     * <p/>
     * This method is preferrable for resources with large amounts of data.
     *
     * @param resource the resource for which to return the content
     * @param mimetype the mime type to retrieve of the content
     * @param in       a InputStream containing the content of the resource
     */
    @Override
    public void setContentStream(Resource resource, InputStream in, String mimetype) throws IOException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String path = mci.getContentPath();

                if(path == null) {
                    if(resource instanceof org.openrdf.model.URI && resource.stringValue().startsWith("file:")) {
                        try {
                            URI uri = new URI(resource.stringValue());
                            path = uri.getPath();
                        } catch(Exception ex) {}
                    } else {
                        // we store all other resources in the default directory; create a random file name and store it in the hasContentLocation
                        // property
                        String extension = null;
                        MimeEntry entry = MimeTable.getDefaultTable().find(mimetype);
                        if(entry != null && entry.getExtensions().length > 0) {
                            extension = entry.getExtensions()[0];
                        }

                        String fileName = UUID.randomUUID().toString();
                        path = defaultDir + File.separator +
                                fileName.substring(0,2) + File.separator +
                                fileName.substring(2,4) + File.separator +
                                fileName.substring(4,6) + File.separator +
                                fileName + (extension != null ? extension : "");
                        mci.setContentPath(path);
                    }
                }

                if(path != null) {
                    if(!path.startsWith(defaultDir)) {
                        if(!configurationService.getBooleanConfiguration("content.filesystem.secure")) {
                            log.warn("accessing file {}, which is outside the default directory; this is a potential security risk; " +
                                    "enable the option content.filesystem.secure in the configuration",path);
                        } else {
                            throw new FileNotFoundException("the file "+path+" is outside the Apache Marmotta default directory location; access denied");
                        }
                    }

                    File file = new File(path);
                    if(!file.exists()) {
                        try {
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                        } catch(IOException ex) {
                            throw new FileNotFoundException("could not create file "+path+"; it is not writable");
                        }
                    }
                    if(file.exists() && file.canWrite()) {
                        log.debug("writing file content to file {} for resource {} ...", file, resource);
                        ByteStreams.copy(in, Files.asByteSink(file).openBufferedStream());
                    } else {
                        throw new FileNotFoundException("could not write to file "+path+"; it does not exist or is not writable");
                    }



                } else {
                    throw new FileNotFoundException("could not write file content for resource "+resource+"; no content path has been specified for the resource");
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
        }
    }
}
