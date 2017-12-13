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
package org.apache.marmotta.platform.versioning.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.versioning.utils.MementoUtils;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import freemarker.template.TemplateException;

/**
 * Serializes an ordered list of versions in text/html into an output stream
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@ApplicationScoped
public class HtmlVersionSerializer implements VersionSerializer {

    private static final String TEMPLATE = "memento_timemap.ftl";

    private static final SimpleDateFormat TSTAMP;
    static {
        TSTAMP = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TSTAMP.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Inject
    private ConfigurationService configurationService;
    
    @Inject
    private TemplatingService templatingService;


    //a static list that contains the contentTypes
    private static final List<ContentType> contentTypes = Arrays.asList(new ContentType("text","html"));

    /**
     * return the content type that will be produced
     * @return
     */
    public ContentType getContentType() {
        return new ContentType("text","html");
    }

    /**
     * returns a list of supported content types (text/html)
     * @return a list of types
     */
    @Override
    public List<ContentType> getContentTypes() {
        return contentTypes;
    }

    /**
     * writes serialized version list (text/html) to output stream
     * @param original the original (current) resource
     * @param versions a list of versions in ascending order
     * @param out an output stream
     */
    @Override
    public void write(Resource original, RepositoryResult<Version> versions, OutputStream out) throws IOException {

        try {

            //write data to map
            Map<String, Object> data = new HashMap<String, Object>();

            data.put("original",original.toString());

            List<Map<String,String>> vs = new ArrayList<Map<String,String>>();

            while (versions.hasNext()) {
                Version v = versions.next();
                Map<String,String> m = new HashMap<String,String>();
                m.put("date",v.getCommitTime().toString());
                m.put("uri",MementoUtils.resourceURI(original.toString(), v.getCommitTime(), configurationService.getBaseUri()).toString());
                m.put("tstamp", TSTAMP.format(v.getCommitTime()));
                vs.add(m);
            }

            data.put("versions",vs);

            //put generic data
            String project = configurationService.getStringConfiguration("kiwi.pages.project","lmf");
            data.put("LOGO",configurationService.getStringConfiguration("kiwi.pages.project."+project+".logo","logo.png"));
            data.put("FOOTER",configurationService.getStringConfiguration("kiwi.pages.project."+project+".footer","a footer"));
            data.put("SERVER_URL",configurationService.getServerUri());
            data.put("baseUri", configurationService.getServerUri());

            //create writer
            OutputStreamWriter writer = new OutputStreamWriter(out);

            //process
            templatingService.process(this.getClass(), TEMPLATE, data, writer);

            //flush and close writer
            writer.flush();
            writer.close();
        } catch (RepositoryException e) {
            throw new IOException("cannot serialize versions in text/html format");
        } catch (TemplateException e) {
            throw new IOException("cannot finish templating");
        }
    }
}
