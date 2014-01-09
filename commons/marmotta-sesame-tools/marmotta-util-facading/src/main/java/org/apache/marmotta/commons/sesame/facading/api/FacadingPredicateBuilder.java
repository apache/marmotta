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
package org.apache.marmotta.commons.sesame.facading.api;


import java.lang.reflect.Method;

import org.apache.marmotta.commons.sesame.facading.impl.FacadingPredicate;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

/**
 * Dynamically create the RDF-property uri for facading.
 * <p>
 * <strong>NOTE: All implementations MUST provide either a public no-arg Constructor or a public
 * static no-arg <code>getInstance()</code>-method!</strong>
 * <p>
 * 
 */
public interface FacadingPredicateBuilder {

    public FacadingPredicate getFacadingPredicate(String fieldName, Class<? extends Facade> facade, Method method);

}
