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
package org.apache.marmotta.ldpath.model.transformers;

import java.math.BigInteger;
import java.util.Map;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.transformers.NodeTransformer;


/**
 * A node transformer transforming the node to its big integer value, if possible.
 *
 * @param <Node> the node type used by the backend
 */
public class BigIntegerTransformer<Node> implements NodeTransformer<BigInteger,Node> {

    @Override
    public BigInteger transform(RDFBackend<Node> backend, Node node, Map<String, String> configuration) throws IllegalArgumentException {
        if(backend.isLiteral(node)) {
            return backend.integerValue(node);
        } else {
            throw new IllegalArgumentException("cannot transform node of type "+node.getClass().getCanonicalName()+" to BigInteger");
        }
    }
}
