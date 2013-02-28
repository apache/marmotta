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
package org.apache.marmotta.platform.core.api.triplestore;

import org.openrdf.sail.Sail;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * An interface implemented by all modules that provide sail wrappers for the sesame sail stack.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public interface StandardSailProvider extends SailProvider {

    /**
     * Create the sail wrapper provided by this SailProvider
     *
     * @param parent the parent sail to wrap by the provider
     * @return the wrapped sail
     */
    public SailWrapper createSail(Sail parent);

}
