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
public class LMFConfigurationException extends LMFException {

	private static final long serialVersionUID = 5573137610964184957L;

	/**
     * Creates a new instance of <code>KiWiConfigurationException</code> without detail message.
     */
    public LMFConfigurationException() {
    }


    /**
     * Constructs an instance of <code>KiWiConfigurationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LMFConfigurationException(String msg) {
        super(msg);
    }
}
