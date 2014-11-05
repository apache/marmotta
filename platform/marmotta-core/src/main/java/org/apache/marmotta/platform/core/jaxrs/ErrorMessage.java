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
package org.apache.marmotta.platform.core.jaxrs;

import org.apache.marmotta.platform.core.exception.HttpErrorException;

/**
 * Error Message for serialization purposes
 *
 * @author Sergio Fernández
 */
public class ErrorMessage {

    public String uri;

    public int status;

    public String reason;

    public String message;

    public ErrorMessage(String uri, int status, String reason, String message) {
        this.status = status;
        this.reason = reason;
        this.message = message;
    }

    public ErrorMessage(HttpErrorException exception) {
        this.uri = exception.getUri();
        this.status = exception.getStatus();
        this.reason = exception.getReason();
        this.message = exception.getMessage();
    }

}
