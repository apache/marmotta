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
package org.apache.marmotta.platform.ldp.util;

import javax.ws.rs.core.EntityTag;

/**
 *
 * @deprecated this is a workaround for <a href="https://issues.jboss.org/browse/RESTEASY-1019">RESTEASY-1019</a>
 */
@Deprecated
public class EntityTagUtils {

    /**
     * This is a workaround for <a href="https://issues.jboss.org/browse/RESTEASY-1019">RESTEASY-1019</a>
     *
     * @see javax.ws.rs.core.EntityTag#equals(Object)
     * @deprecated use {@link javax.ws.rs.core.EntityTag#equals(Object)} or {@link javax.ws.rs.core.Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)} when RESTEASY-1019 is fixed.
     */
    @Deprecated
    public static boolean equals(EntityTag tag1, EntityTag tag2) {
        if (tag1 == null || tag2 == null) return false;

        // If it works, it's fine
        if (tag1.equals(tag2)) {
            return true;
        } else {
            // Weak-Tag comparison requires extra check because of RESTEASY-1019
            if (tag1.isWeak() && tag2.isWeak()) {
                return tag1.getValue().replaceAll("^\"", "").equals(
                        tag2.getValue().replaceAll("^\"", "")
                );
            }
            return false;
        }
    }

    /**
     * This is a workaround for <a href="https://issues.jboss.org/browse/RESTEASY-1019">RESTEASY-1019</a>
     *
     * @see javax.ws.rs.core.EntityTag#equals(Object)
     * @deprecated use {@link javax.ws.rs.core.EntityTag#valueOf(String)} when RESTEASY-1019 is fixed.
     */
    @Deprecated
    public static EntityTag parseEntityTag(String headerValue) {
        boolean weak = false;
        if (headerValue.startsWith("W/")) {
            weak = true;
            headerValue = headerValue.substring(2);
        }
        if (headerValue.startsWith("\"") && headerValue.endsWith("\"")) {
            headerValue = headerValue.substring(1, headerValue.length() -1);
        }
        return new EntityTag(headerValue,weak);
    }

}
