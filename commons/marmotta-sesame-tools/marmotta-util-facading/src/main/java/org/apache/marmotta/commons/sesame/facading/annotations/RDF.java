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
package org.apache.marmotta.commons.sesame.facading.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation indicates that a certain field should be
 * persisted to the KiWi triple store using the property URI
 * passed as annotation parameter.<br>
 * The TripleStore class checks for <b>RDF</b> annotations
 * during persist and load. <br>
 * Classes using this annotation must currently implement the
 * {@link org.apache.marmotta.platform.core.model.rdf.KiWiEntity} interface, so that the
 * KnowledgeSpace has access to the resource associated with the
 * entity.<br>
 * This is a runtime annotation and it is applicable on fields
 * and on getter methods.<br>
 *
 * @author Sebastian Schaffert
 * @see org.apache.marmotta.platform.core.model.rdf.KiWiEntity
 */

@Retention(RetentionPolicy.RUNTIME)
@Target( {ElementType.FIELD, ElementType.METHOD})
public @interface RDF {

    /**
     * Return the URI of the RDF predicate to use for the field
     * or method.
     *
     * @returns URI of the RDF predicate to use for the field or
     *          method.
     */
	String[] value();
}
