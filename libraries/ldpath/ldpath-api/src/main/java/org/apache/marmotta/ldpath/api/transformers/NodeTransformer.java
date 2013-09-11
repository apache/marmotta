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
package org.apache.marmotta.ldpath.api.transformers;

import java.util.Map;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;

/**
 * Implementations of this interface allow to transform KiWiNode objects into the type T. This is
 * currently required by the indexer to map KiWiNodes to the Java types corresponding to the
 * respective XML Schema datatypes.
 *
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public interface NodeTransformer<T,Node> {

    /**
     * Transform the KiWiNode node into the datatype T. In case the node cannot be transformed to
     * the respective datatype, throws an IllegalArgumentException that needs to be caught by the class
     * carrying out the transformation.
     *
     *
     *
     * @param backend
     * @param node
     * @param configuration the field configuration used when defining the LDPath rule
     * @return
     */
    public T transform(RDFBackend<Node> backend, Node node, Map<String, String> configuration) throws IllegalArgumentException;

}
