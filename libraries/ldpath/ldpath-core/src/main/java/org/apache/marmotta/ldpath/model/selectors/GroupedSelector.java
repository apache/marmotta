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
package org.apache.marmotta.ldpath.model.selectors;


import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Group is a complex selector in brackets.
 *
 * @param <Node> the node type used by the backend
 * @author Jakob Frank <jakob@apache.org>
 */
public class GroupedSelector<Node> implements NodeSelector<Node> {

    private final NodeSelector<Node> content;

    public GroupedSelector(NodeSelector<Node> content) {
        this.content = content;
    }


    /**
     * Apply the selector to the context node passed as argument and return the collection
     * of selected nodes in appropriate order.
     *
     * @param context     the node where to start the selection
     * @param path        the path leading to but not including the context node in the current evaluation of LDPath; may be null,
     *                    in which case path tracking is disabled
     * @param resultPaths a map where each of the result nodes maps to a path leading to the result node in the LDPath evaluation;
     *                    if null, path tracking is disabled and the path argument is ignored
     * @return the collection of selected nodes
     */
    @Override
    public Collection<Node> select(final RDFBackend<Node> rdfBackend, final Node context, final List<Node> path, final Map<Node, List<Node>> resultPaths) {
        return content.select(rdfBackend, context, path, resultPaths);
    }

    /**
     * Return the name of the NodeSelector for registration in the selector registry
     *
     * @param rdfBackend
     * @return
     */
    @Override
    public String getPathExpression(NodeBackend<Node> rdfBackend) {
        return String.format("(%s)", content.getPathExpression(rdfBackend));
    }

    /**
     * Return a name for this selector to be used as the name for the whole path if not explicitly
     * specified. In complex selector expressions, this is typically delegated to the first
     * occurrence of an atomic selector.
     */
    @Override
    public String getName(NodeBackend<Node> nodeRDFBackend) {
        throw new UnsupportedOperationException("cannot use a group in unnamed field definitions because the name is ambiguous");
    }

    /**
     * Getter for child content NodeSelector
     * @return child content NodeSelector
     */
    public NodeSelector<Node> getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("rawtypes")
		GroupedSelector that = (GroupedSelector) o;

        return content != null ? content.equals(that.content) : that.content == null;

    }

    @Override
    public int hashCode() {
        int result = 31;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
