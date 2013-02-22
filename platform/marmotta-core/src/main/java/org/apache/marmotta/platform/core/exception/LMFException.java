/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.exception;

/**
 *
 * @author Sebastian Schaffert
 */
public class LMFException extends Exception {

    private static final long serialVersionUID = 6193753017649674995L;


    /**
     * Creates a new instance of <code>KiWiException</code> without detail message.
     */
    public LMFException() {
    }


    /**
     * Constructs an instance of <code>KiWiException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LMFException(String msg) {
        super(msg);
    }

    public LMFException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public LMFException(Throwable c) {
        super(c);
    }
}
