/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.ldp.exceptions;


/**
 * Invalid or unknown LDP Client Interaction Model Exception
 *
 * @see <a href="http://www.w3.org/TR/ldp/#h5_ldpc-post-createrdf">LDP Spec</a>
 *
 * @author Jakob Frank (jakob@apache.org)
 */
public class InvalidInteractionModelException extends LDPException {

    private final String href;

    public InvalidInteractionModelException(String href) {
        super(String.format("Invalid LDP Client Interaction Model: <%s>", href));
        this.href = href;
    }

    public InvalidInteractionModelException(java.net.URI uri) {
        this(uri.toASCIIString());
    }

    public InvalidInteractionModelException(org.openrdf.model.URI uri) {
        this(uri.stringValue());
    }

    public String getHref() {
        return href;
    }

}
