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
package org.apache.marmotta.platform.core.exception.webservice;

import org.apache.marmotta.platform.core.exception.LMFException;

/**
 * User: Thomas Kurz
 * Date: 21.02.11
 * Time: 13:09
 */
public class NotFoundException extends LMFException {
    private static final long serialVersionUID = 4705776110923618486L;

    public NotFoundException() {
        super();
    }
    public NotFoundException(String s) {
        super(s);
    }

    public NotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NotFoundException(Throwable c) {
        super(c);
    }

}
