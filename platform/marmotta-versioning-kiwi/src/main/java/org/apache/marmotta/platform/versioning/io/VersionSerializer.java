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

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryResult;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Serializes an ordered list of versions into an output stream
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public interface VersionSerializer {

    /**
     * returns a list of supported content types
     * @return
     */
    public List<ContentType> getContentTypes();

    /**
     * return the content type that will be produced
     * @return
     */
    public ContentType getContentType();

    /**
     * writes serialized version list to output stream
     * @param original the original (current) resource
     * @param versions a list of versions in ascending order
     * @param out an output stream
     */
    public void write(Resource original, RepositoryResult<Version> versions, OutputStream out) throws IOException;

}
