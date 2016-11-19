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
package org.apache.marmotta.platform.core.api.modules;

import javax.servlet.Filter;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public interface MarmottaHttpFilter extends Filter {

    // authentication and access control have to come before everything else
    int PRIO_AUTH = Integer.MIN_VALUE;

    int PRIO_ACL  = Integer.MIN_VALUE + 10;

    // first in filter chain
    int PRIO_FIRST = 1;
   
    // somewhere inbetween
    int PRIO_MIDDLE = Integer.MAX_VALUE / 2;
    
    // last in filter chain
    int PRIO_LAST  = Integer.MAX_VALUE;
    
    /**
     * Return the pattern (regular expression) that a request URI (relative to the LMF base URI) has to match
     * before triggering this filter.
     *
     * @return
     */
    String getPattern();


    /**
     * Return the priority of the filter. Filters that need to be executed before anything else should return
     * PRIO_FIRST, filters that need to be executed last in the chain should return PRIO_LAST, all other filters
     * something inbetween (e.g. PRIO_MIDDLE).
     * @return
     */
    int getPriority();
}
