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
package org.apache.marmotta.platform.core.api.content;

import org.openrdf.model.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * API for plugins that allow writing of content to some destination.
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface ContentWriter {


    /**
     * Return the name of the content reader. Used to identify and display the content reader to admin users.
     * @return
     */
    String getName();


    /**
     * Store the content of the specified mime type for the specified resource. Accepts a byte array containing
     * the byte data of the content that is then written to the destination configured for this writer.
     * <p/>
     * @param resource  the resource for which to store the content
     * @param mimetype  the mime type of the content
     * @param data a byte array containing the content of the resource
     */
    void setContentData(Resource resource, byte[] data, String mimetype) throws IOException;


    /**
     * Store the content of the specified mime type for the specified resource. Accepts an input stream containing
     * the byte data of the content that is read and written to the destination configured for this writer.
     * <p/>
     * This method is preferrable for resources with large amounts of data.
     *
     * @param resource  the resource for which to return the content
     * @param mimetype  the mime type to retrieve of the content
     * @param in a InputStream containing the content of the resource
     */
    void setContentStream(Resource resource, InputStream in, String mimetype) throws IOException;


    /**
     * Delete the content of the speficied mime type for the specified resource.
     *
     * @param resource the resource for which to delete the content
     * @param mimetype the mime type of the content to delete (optional)
     */
    void deleteContent(Resource resource, String mimetype) throws IOException;

}
