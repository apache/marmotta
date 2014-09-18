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
import org.apache.marmotta.platform.core.api.content.ContentReader;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.model.content.MediaContentItem;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.net.URI;

import static org.apache.marmotta.commons.sesame.repository.ExceptionUtils.handleRepositoryException;

/**
 * A content reader that reads the content of a resource from the file system.
 * It uses the kiwi:hasContentPath property to determine the path of the content to access.
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class FileSystemContentReader implements ContentReader {

    @Inject
    private Logger log;


    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    /** TIKA mime type detector */
    private DefaultDetector detector;


    private String defaultDir;

    public FileSystemContentReader() {
    }

    @PostConstruct
    public void initialise() {
        detector = new DefaultDetector();
        defaultDir = configurationService.getHome()+File.separator+"resources";

        log.debug("FileSystem Content Reader started (default directory: {})",defaultDir);

    }

    /**
     * Retrieve the content of the specified mime type for the specified resource. Returns a byte array containing
     * the byte data of the content, or null, indicating that a content of the specified mime type does not exist
     * for the resource.
     * <p/>
     * Specialised content readers could even transform the resource content from its original form to the new
     * mimetype, e.g. converting an image from JPEG to PNG.
     *
     * @param resource the resource for which to return the content
     * @param mimetype the mime type to retrieve of the content
     * @return a byte array containing the content of the resource, or null if no content exists
     */
    @Override
    public byte[] getContentData(Resource resource, String mimetype) throws IOException {
        InputStream in = getContentStream(resource,mimetype);
        try {
            return ByteStreams.toByteArray(in);
        } finally {
            in.close();
        }
    }

    /**
     * Return the name of the content reader. Used to identify and display the content reader to admin users.
     *
     * @return
     */
    @Override
    public String getName() {
        return "FileSystem Content Reader";
    }

    /**
     * Retrieve the content of the specified mime type for the specified resource. Returns a input stream containing
     * the byte data of the content, or null, indicating that a content of the specified mime type does not exist
     * for the resource.
     * <p/>
     * Specialised content readers could even transform the resource content from its original form to the new
     * mimetype, e.g. converting an image from JPEG to PNG.
     * <p/>
     * This method is preferrable for resources with large amounts of data.
     *
     * @param resource the resource for which to return the content
     * @param mimetype the mime type to retrieve of the content
     * @return a InputStream containing the content of the resource, or null if no content exists
     */
    @Override
    public InputStream getContentStream(Resource resource, String mimetype) throws IOException {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String path = mci.getContentPath();
                if(path == null && resource instanceof org.openrdf.model.URI && resource.stringValue().startsWith("file:")) {
                    try {
                        URI uri = new URI(resource.stringValue());
                        path = uri.getPath();
                    } catch(Exception ex) {}
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
                    if(file.exists() && file.canRead()) {
                        log.debug("reading file content from file {} for resource {} ...", file, resource);
                        return Files.asByteSource(file).openBufferedStream();
                    } else {
                        throw new FileNotFoundException("the file "+path+" does not exist or is not readable");
                    }
                } else {
                    return null;
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
            return null;
        }
    }

    /**
     * Check whether the specified resource has content of the specified mimetype for this reader. Returns true
     * in this case, false otherwise.
     *
     * @param resource the resource to check
     * @param mimetype the mimetype to look for
     * @return true if content of this mimetype is associated with the resource, false otherwise
     */
    @Override
    public boolean hasContent(Resource resource, String mimetype) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String path = mci.getContentPath();
                if(path == null && resource instanceof org.openrdf.model.URI && resource.stringValue().startsWith("file:")) {
                    try {
                        URI uri = new URI(resource.stringValue());
                        path = uri.getPath();
                    } catch(Exception ex) {}
                }

                if(path != null) {
                    File file = new File(path);
                    if(file.exists() && file.canRead()) {
                        log.debug("found file content from file {} for resource {} ...", file, resource);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
            return false;
        }
    }

    /**
     * Return the MIME content type of the resource passed as argument.
     *
     * @param resource resource for which to return the content type
     * @return the MIME content type of the resource
     */
    @Override
    public String getContentType(Resource resource) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String path = mci.getContentPath();
                if(path == null && resource instanceof org.openrdf.model.URI && resource.stringValue().startsWith("file:")) {
                    try {
                        URI uri = new URI(resource.stringValue());
                        path = uri.getPath();
                    } catch(Exception ex) {}
                }

                if(path != null) {
                    File file = new File(path);
                    if(file.exists() && file.canRead()) {

                        String mimeType = null;

                        Metadata metadata = new Metadata();
                        metadata.set(Metadata.RESOURCE_NAME_KEY, file.getAbsolutePath());
                        try {
                            InputStream in = new BufferedInputStream(Files.asByteSource(file).openBufferedStream());
                            mimeType = detector.detect(in,metadata).toString();
                            in.close();
                        } catch (IOException e) {
                            log.error("I/O error while detecting file type for file {}",file,e);
                        }
                        log.debug("detected mime type {} of file {} for resource {} ...", mimeType, file, resource);

                        return mimeType;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
            return null;
        }
    }


    /**
     * Return the number of bytes the content of this resource contains.
     *
     * @param resource resource for which to return the content length
     * @return byte count for the resource content
     */
    @Override
    public long getContentLength(Resource resource, String mimetype) {
        try {
            RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                MediaContentItem mci = FacadingFactory.createFacading(conn).createFacade(resource, MediaContentItem.class);

                String path = mci.getContentPath();
                if(path == null && resource instanceof org.openrdf.model.URI && resource.stringValue().startsWith("file:")) {
                    try {
                        URI uri = new URI(resource.stringValue());
                        path = uri.getPath();
                    } catch(Exception ex) {}
                }

                if(path != null) {
                    File file = new File(path);
                    if(file.exists() && file.canRead()) {
                        return file.length();
                    }
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (RepositoryException ex) {
            handleRepositoryException(ex,FileSystemContentReader.class);
        }
        return 0;
    }
}
