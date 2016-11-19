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

import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.exception.WritingNotSupportedException;
import org.openrdf.model.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: Thomas Kurz
 * Date: 07.02.11
 * Time: 12:30
 */
public interface ContentService {

    /**
     * Initialise the content service, setting up all content readers and writers.
     */
    void initialise();


    /**
     * Store the content of the specified mime type for the specified resource. Accepts a byte array containing
     * the byte data of the content that is then written to the destination configured for this writer.
     * <p/>
     * @param resource  the resource for which to store the content
     * @param mimeType  the mime type of the content
     * @param data a byte array containing the content of the resource
     */
    void setContentData(Resource resource, byte[] data, String mimeType) throws WritingNotSupportedException;


    /**
     * Store the content of the specified mime type for the specified resource. Accepts an input stream containing
     * the byte data of the content that is read and written to the destination configured for this writer.
     * <p/>
     * This method is preferrable for resources with large amounts of data.
     *
     * @param resource  the resource for which to return the content
     * @param mimeType  the mime type to retrieve of the content
     * @param in a InputStream containing the content of the resource
     */
    void setContentStream(Resource resource, InputStream in, String mimeType) throws WritingNotSupportedException;


    /**
     * Retrieve the content of the specified mime type for the specified resource. Returns a byte array containing
     * the byte data of the content, or null, indicating that a content of the specified mime type does not exist
     * for the resource.
     * <p/>
     * Specialised content readers could even transform the resource content from its original form to the new
     * mimetype, e.g. converting an image from JPEG to PNG.
     *
     * @param resource  the resource for which to return the content
     * @param mimeType  the mime type to retrieve of the content
     * @return a byte array containing the content of the resource, or null if no content exists
     */
    byte[] getContentData(Resource resource, String mimeType);


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
     * @param resource  the resource for which to return the content
     * @param mimetype  the mime type to retrieve of the content
     * @return a InputStream containing the content of the resource, or null if no content exists
     */
    InputStream getContentStream(Resource resource, String mimetype) throws IOException;


    /**
     * Check whether the specified resource has content of the specified mimetype for this reader. Returns true
     * in this case, false otherwise.
     *
     * @param resource the resource to check
     * @param mimetype the mimetype to look for
     * @return true if content of this mimetype is associated with the resource, false otherwise
     */
    boolean hasContent(Resource resource, String mimetype);

	String getContentType(Resource resource);

    /**
      * Return the number of bytes the content of this resource contains.
      *
      * @param resource resource for which to return the content length
      * @return byte count for the resource content
      */

    long getContentLength(Resource resource, String mimetype);
    /**
     * Delete the content of the specified resource.
     *
     * @param resource the resource for which to delete the content
     */
    boolean deleteContent(Resource resource) throws MarmottaException;

}
