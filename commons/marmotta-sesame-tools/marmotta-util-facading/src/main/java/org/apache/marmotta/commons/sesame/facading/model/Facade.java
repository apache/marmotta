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
package org.apache.marmotta.commons.sesame.facading.model;

import org.openrdf.model.Resource;

/**
 * Interface that must be the base interface of all KiWi facades. It defines no methods but has an underlying KiWiResource
 * that is used by the KiWi client proxy to resolve the associated data in the triple store.
 * <p/>
 * An interface that inherits from this interface indicates that it represents a facade that can delegate getters and
 * setters to properties in the triple store. All getter methods need to be annotated with appropriate @RDF annotations
 * that map to a property in the triple store.

 * <p/>
 * User: Sebastian Schaffert
 */
public interface Facade {

    /**
     * Return the resource that is facaded by this KiWiFacade.
     *
     * @return
     */
    Resource getDelegate();
}
