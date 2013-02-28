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
package org.apache.marmotta.client.model.content;

import java.io.InputStream;

/**
 * Representation of media content returned by the Apache Marmotta. Provides an InputStream for
 * reading the media data. Optionally, supports reading all content into a byte array.
 *
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class Content {
    
    private String mimeType;

    private long size;


    public Content(String mimeType, long size) {
        this.mimeType = mimeType;
        this.size = size;
    }

    /**
     * Return the content of this object as stream. Note that the stream is only guaranteed to be consumable once.
     * @return
     */
    public abstract InputStream getStream();


    /**
     * Return the content of this object as byte array. Note that when calling this method it is not safe to
     * call the getStream method afterwards.
     * @return
     */
    public abstract byte[] getBytes();


    public String getMimeType() {
        return mimeType;
    }

    public long getSize() {
        return size;
    }
}
