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
package org.apache.marmotta.platform.core.model.content;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A non-entity class used to represent in-memory media content in KiWi. The actual content is stored in
 * KiWiMediaContentLiteral.
 *
 *
 * <p/>
 * User: sschaffe
 */
public class MediaContent implements Serializable  {

    private static final long serialVersionUID = 1L;

    private byte[] data;

    private String mimeType;

    public MediaContent(byte[] data, String mimeType) {
        this.data = data;
        this.mimeType = mimeType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaContent that = (MediaContent) o;

        if (!Arrays.equals(data, that.data)) return false;
        return mimeType.equals(that.mimeType);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(data);
        result = 31 * result + mimeType.hashCode();
        return result;
    }
}
