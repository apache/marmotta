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
package org.apache.marmotta.platform.core.exception;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class WritingNotSupportedException extends MarmottaException {

    /**
     * 
     */
    private static final long serialVersionUID = 7591775138145634253L;

    /**
     * Creates a new instance of <code>KiWiException</code> without detail message.
     */
    public WritingNotSupportedException() {
    }

    /**
     * Constructs an instance of <code>KiWiException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public WritingNotSupportedException(String msg) {
        super(msg);
    }

    public WritingNotSupportedException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
