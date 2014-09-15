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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.content.ContentReader;
import org.apache.marmotta.platform.core.api.content.ContentService;
import org.apache.marmotta.platform.core.api.content.ContentWriter;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.events.SesameStartupEvent;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.exception.WritingNotSupportedException;
import org.openrdf.model.Resource;
import org.slf4j.Logger;

/**
 * Service that provides access to the content associated with a resource. It makes use of the ContentReader and
 * ContentWriter implementations registered in the system.
 *
 * User: Thomas Kurz, Sebastian Schaffert
 * Date: 07.02.11
 * Time: 12:37
 */
@ApplicationScoped
public class ContentServiceImpl implements ContentService {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private Instance<ContentReader> readers;

    @Inject
    private Instance<ContentWriter> writers;

    private Map<Pattern,ContentReader> readerMap;
    private Map<Pattern,ContentWriter> writerMap;

    @Override
    public void initialise() {
        log.info("Content Service starting up ...");
        initialiseReadersWriters();
    }
    
    protected void initialize(@Observes SesameStartupEvent event) {
    	initialise();
    }

    private void initialiseReadersWriters() {
        readerMap = new HashMap<Pattern, ContentReader>();
        writerMap = new HashMap<Pattern, ContentWriter>();

        // first read from the config file all content.* keys and store the name of the reader/writer in this set
        // will e.g. store "triplestore" and "filesystem" if it finds keys of the form "content.triplestore.reader"
        // and "content.filesystem.writer"
        Set<String> configNames = new HashSet<String>();
        for(String key : configurationService.listConfigurationKeys("content")) {
            String[] components = key.split("\\.");
            if(components.length > 1) {
                configNames.add(components[1]);
            }
        }

        // then read the configuration for each reader/writer specification in the config file and look whether
        // the appropriate reader and/or writer is available
        for(String configName : configNames) {
            String readerClass = configurationService.getStringConfiguration("content."+configName+".reader");
            String writerClass = configurationService.getStringConfiguration("content."+configName+".writer");
            String patternStr  = configurationService.getStringConfiguration("content."+configName+".pattern");
            String enabledStr  = configurationService.getStringConfiguration("content."+configName+".enabled");

            if(Boolean.parseBoolean(enabledStr)) {
                log.debug("Initializing content provider {}", configName);
                ContentReader reader = null;
                ContentWriter writer = null;
                Pattern pattern = null;
                if(readerClass != null) {
                    for(ContentReader r : readers) {
                        if(r.getClass().getCanonicalName().startsWith(readerClass)) {
                            reader = r;
                            break;
                        }
                    }
                }

                if(writerClass != null) {
                    for(ContentWriter w : writers) {
                        if(w.getClass().getCanonicalName().startsWith(writerClass)) {
                            writer = w;
                            break;
                        }
                    }
                }

                try {
                    pattern = Pattern.compile(patternStr);
                } catch(Exception ex) {
                    log.warn("pattern {} is not a valid regular expression; disabling reader/writer {} (message was {})", patternStr,configName,ex.getMessage());
                    continue;
                }

                if(pattern != null && reader != null) {
                    readerMap.put(pattern,reader);
                    log.info("enabled content reader '{}' for pattern {}",reader.getName(), pattern);
                }
                if(pattern != null && writer != null) {
                    writerMap.put(pattern,writer);
                    log.info("enabled content writer '{}' for pattern {}", writer.getName(), pattern);
                }


            } else {
                log.info("content reader/writer {} disabled",configName);
            }
        }
    }


    public void configurationChangedEvent(@Observes ConfigurationChangedEvent event) {
        for (String key : event.getKeys())
            if (key.startsWith("content.")) {
                log.info("Content Service reinitialising ...");
                initialiseReadersWriters();
                break;
            }
    }




    @Override
    public void setContentData(Resource resource, byte[] data, String mimetype) throws WritingNotSupportedException {
        // iterate over all possible writers; if the pattern matches, try to store the content and return
        for(Pattern p : writerMap.keySet()) {
            if(p.matcher(resource.toString()).matches()) {
                ContentWriter writer = writerMap.get(p);
                log.debug("setting content for resource {} using writer {}",resource,writer.getName());
                try {
                    writer.setContentData(resource, data, mimetype);
                    return;
                } catch(IOException ex) {
                    log.error("could not write content, writer threw an IO Exception",ex);
                    throw new WritingNotSupportedException(ex.getMessage(),ex);
                }
            }
        }
        throw new WritingNotSupportedException("no writer found for resource "+resource);
    }


    /**
     * Store the content of the specified mime type for the specified resource. Accepts an input stream containing
     * the byte data of the content that is read and written to the destination configured for this writer.
     * <p/>
     * This method is preferrable for resources with large amounts of data.
     *
     * @param resource the resource for which to return the content
     * @param mimeType the mime type to retrieve of the content
     * @param in       a InputStream containing the content of the resource
     */
    @Override
    public void setContentStream(Resource resource, InputStream in, String mimeType) throws WritingNotSupportedException {
        // iterate over all possible writers; if the pattern matches, try to store the content and return
        for(Pattern p : writerMap.keySet()) {
            if(p.matcher(resource.toString()).matches()) {
                ContentWriter writer = writerMap.get(p);
                log.debug("setting content for resource {} using writer {}",resource,writer.getName());
                try {
                    writer.setContentStream(resource,in,mimeType);
                    return;
                } catch(IOException ex) {
                    log.error("could not write content, writer threw an IO Exception",ex);
                    throw new WritingNotSupportedException(ex.getMessage(),ex);
                }
            }
        }
        throw new WritingNotSupportedException("no writer found for resource "+resource);
    }

    /**
     * Retrieve the content of the specified mime type for the specified resource. Returns a byte array containing
     * the byte data of the content, or null, indicating that a content of the specified mime type does not exist
     * for the resource.
     * <p/>
     * Specialised content readers could even transform the resource content from its original form to the new
     * mimetype, e.g. converting an image from JPEG to PNG.
     *
     * @param resource  the resource for which to return the content
     * @param mimetype  the mime type to retrieve of the content
     * @return a byte array containing the content of the resource, or null if no content exists
     */
    @Override
    public byte[] getContentData(Resource resource, String mimetype) {
        // iterate over all possible writers; if the pattern matches, try to store the content and return
        for(Pattern p : readerMap.keySet()) {
            if(p.matcher(resource.toString()).matches()) {
                ContentReader reader = readerMap.get(p);
                log.debug("reading content for resource {} using reader {}",resource,reader.getName());
                try {
                    return reader.getContentData(resource, mimetype);
                } catch(IOException ex) {
                    log.error("could not read content, reader threw an IO Exception",ex);
                    return null;
                }
            }
        }
        return null;
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
        // iterate over all possible writers; if the pattern matches, try to store the content and return
        for(Pattern p : readerMap.keySet()) {
            if(p.matcher(resource.toString()).matches()) {
                ContentReader reader = readerMap.get(p);
                log.debug("reading content for resource {} using reader {}",resource,reader.getName());
                try {
                    return reader.getContentStream(resource, mimetype);
                } catch(IOException ex) {
                    log.error("could not read content for resource {}, reader threw an IO Exception (message: {})",resource,ex.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasContent(Resource resource, String mimetype) {
        // iterate over all possible writers; if the pattern matches, try to store the content and return
        for(Pattern p : readerMap.keySet()) {
            if(p.matcher(resource.toString()).matches()) {
                ContentReader reader = readerMap.get(p);
                return reader.hasContent(resource, mimetype);
            }
        }
        return false;
    }

    @Override
    public String getContentType(Resource resource) {
        // iterate over all possible writers; if the pattern matches, try to store the content and return
        for(Pattern p : readerMap.keySet()) {
            if(p.matcher(resource.stringValue()).matches()) {
                ContentReader reader = readerMap.get(p);
                return reader.getContentType(resource);
            }
        }
        return null;
    }


    /**
     * Return the number of bytes the content of this resource contains.
     *
     * @param resource resource for which to return the content length
     * @return byte count for the resource content
     */
    @Override
    public long getContentLength(Resource resource, String mimetype) {
        // iterate over all possible writers; if the pattern matches, try to store the content and return
        for(Pattern p : readerMap.keySet()) {
            if(p.matcher(resource.toString()).matches()) {
                ContentReader reader = readerMap.get(p);
                return reader.getContentLength(resource,mimetype);
            }
        }
        return 0;
    }

    @Override
    public boolean deleteContent(Resource resource) throws MarmottaException {
        // iterate over all possible writers; if the pattern matches, try to store the content and return
        for(Pattern p : writerMap.keySet()) {
            if(p.matcher(resource.toString()).matches()) {
                ContentWriter writer = writerMap.get(p);
                try {
                    writer.deleteContent(resource,"");
                    return true;
                } catch(IOException ex) {
                    log.error("could not write content, writer threw an IO Exception",ex);
                    throw new WritingNotSupportedException(ex.getMessage(),ex);
                }
            }
        }
        return false;
    }
}
