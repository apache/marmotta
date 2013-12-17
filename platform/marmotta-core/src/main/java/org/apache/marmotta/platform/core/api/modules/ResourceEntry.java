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
package org.apache.marmotta.platform.core.api.modules;

import java.io.Serializable;
import java.net.URL;

/**
 * Internal representation of a resource contained in one of the modules.
 * <p/>
 * User: sschaffe
 */
public class ResourceEntry implements Serializable {

    /**
     * The path relative to the web application root under which this resource is accessed.
     */
    private String relativePath;


    /**
     * The file system location of the resource entry (in case it needs to be refreshed)
     */
    private URL location;

    /**
     * The byte data of the resource, this is the actual content
     */
    private byte[] data;

    /**
     * The content length of the resource (number of bytes)
     */
    private int length;

    /**
     * The content type (MIME) of the resource
     */
    private String contentType;

    public ResourceEntry(URL location, byte[] data, int length, String contentType) {
        super();
        this.location = location;
        this.data = data;
        this.length = length;
        this.contentType = contentType;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public URL getLocation() {
        return location;
    }

    public void setLocation(URL location) {
        this.location = location;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
}
