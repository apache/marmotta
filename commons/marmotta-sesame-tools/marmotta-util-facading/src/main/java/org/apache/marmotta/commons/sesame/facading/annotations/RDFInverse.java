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
 * This annotation indicates that a certain KiWi facade field should be mapped
 * inversely to a property in the triple store. It is the inverse of the
 * <code>@RDF</code> annotation, e.g. when using
 * <code>@RDFInverse("rdfs:subClassOf")</code>, the annotated method returns the
 * subclasses, while the annotation <code>@RDF("rdfs:subClassOf")</code> would
 * return the superclasses. Note that <code>@RDFInverse</code> only works on
 * ObjectProperties; for all other properties it behaves exactly like
 * <code>@RDF</code>
 * <p>
 * The KiWiEntityManager and TripleStore check for the presence of this
 * annotation on methods and dynamically maps them to queries on the triple
 * store, using the resource of the annotated interface or class (which must
 * implement KiWiEntity to provide a getResource() method) as object.
 * <p>
 * This is a runtime annotation and it is applicable on getter methods.<br>
 * <p>
 * TODO: currently, only KiWiFacades are supported; also, it is currently not
 * possible to provide {@link @RDF} and {@link @RDFInverse} on the same method
 * at the same time.
 * 
 * @author Sebastian Schaffert
 */

@Retention(RetentionPolicy.RUNTIME)
@Target( {ElementType.METHOD})
public @interface RDFInverse {

	/**
	 * Return the URI of the RDF predicate to use for the field
	 * or method.
	 *
	 * @returns URI of the RDF predicate to use for the field or
	 *          method.
	 */
	String[] value();
}
