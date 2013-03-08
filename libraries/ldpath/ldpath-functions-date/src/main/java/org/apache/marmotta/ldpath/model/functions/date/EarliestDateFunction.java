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
package org.apache.marmotta.ldpath.model.functions.date;


import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.model.transformers.DateTransformer;

public class EarliestDateFunction<Node> extends DateFunction<Node> {

    private final DateTransformer<Node> transformer = new DateTransformer<Node>();

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context,
            Collection<Node>... args) throws IllegalArgumentException {
        if (args.length != 1) {
            throw new IllegalArgumentException("earliest requires exactly one argument");
        }

        Node result = null;
        Date earliest = null;

        for (Node node : args[0]) {
            try {
                Date d = transformer.transform(backend, node, null);
                if (earliest == null || d.before(earliest)) {
                    earliest = d;
                    result = node;
                }
            } catch (IllegalArgumentException e) {
                // Non-Date Literals are just ignored
            }
        }

        return result!=null?Collections.singleton(result):Collections.<Node>emptyList();
    }

    @Override
    public String getSignature() {
        return "fn:earliest(DateLiteralList): DateLiteral";
    }

    @Override
    public String getDescription() {
        return "select the earliest date (min)";
    }

    @Override
    public String getLocalName() {
        return "earliest";
    }

}
