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
package org.apache.marmotta.platform.ldp.exceptions;

import org.apache.commons.lang3.StringUtils;

/**
 * Exception to be thrown if a RDFS-Resource is available where a Non-RDF-Resource is expected, and vice versa.
 */
public class IncompatibleResourceTypeException extends LDPException {

    public IncompatibleResourceTypeException(String expected, String available) {
        super(createMessage(null, expected, available));
    }

    public IncompatibleResourceTypeException(String message, String expected, String available) {
        super(createMessage(message, expected, available));
    }

    private static String createMessage(String message, String expected, String available) {
        if (StringUtils.isBlank(message)) {
            return String.format("Expected %s but %s was provided", expected, available);
        } else {
            return String.format("%s: expected %s but %s was provided", message, expected, available);
        }
    }
}
