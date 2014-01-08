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
package org.apache.marmotta.platform.versioning.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.versioning.utils.MementoUtils;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * Serializes an ordered list of versions in application/link-format into an output stream
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@ApplicationScoped
public class LinkVersionSerializer implements VersionSerializer {

    @Inject
    ConfigurationService configurationService;

    //a static list thta contains the contentTypes
    private static final List<ContentType> contentTypes = Arrays.asList(new ContentType("application","link-format"));

    /**
     * return the content type that will be produced
     * @return
     */
    public ContentType getContentType() {
        return new ContentType("application","link-format");
    }

    /**
     * returns a list of supported content types (application/link-format)
     * @return a list of types
     */
    @Override
    public List<ContentType> getContentTypes() {
        return contentTypes;
    }

    /**
     * writes serialized version list (application/link-format) to output stream
     * @param original the original (current) resource
     * @param versions a list of versions in ascending order
     * @param out an output stream
     */
    @Override
    public void write(Resource original, RepositoryResult<Version> versions, OutputStream out) throws IOException {

        try {
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));

            //write original resource
            w.append("<");
            w.append(original.toString());
            w.append(">;rel=\"original\",");
            w.newLine();

            //to control the loop
            boolean first = true;

            //write versions
            while (versions.hasNext()) {

                Version v = versions.next();

                //append memento resource uri for versions v
                w.append("<");
                w.append(MementoUtils.resourceURI(original.toString(),v.getCommitTime(),configurationService.getBaseUri()).toString());
                w.append(">; rel=\"");

                //write first, last, memento
                if( first && versions.hasNext()) {
                    w.append("first memento");
                } else if(!versions.hasNext()) {
                    w.append("last memento");
                } else {
                    w.append("memento");
                }

                //add datetime
                w.append("\"; datetime=\"");
                w.append(v.getCommitTime().toString());
                w.append("\",");

                w.newLine();
                first = false;
            }

            w.flush();
            w.close();

        } catch (RepositoryException e) {
            throw new IOException("cannot serialize versions in application/link-format");
        }
    }
}
