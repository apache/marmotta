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

package org.apache.marmotta.platform.ldcache.model.filter;

import org.apache.marmotta.commons.sesame.filter.resource.ResourceFilter;

/**
 * This filter can be used to define as CDI services additional filters that allow to ignore resources when
 * caching. The advantage of this approach over "blacklisting" is that filters defined in this way do not even
 * create a cache entry, they ignore the resource early on.
 *
 * The filter should return true in case the resource should be ignored.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface LDCacheIgnoreFilter extends ResourceFilter {
}
