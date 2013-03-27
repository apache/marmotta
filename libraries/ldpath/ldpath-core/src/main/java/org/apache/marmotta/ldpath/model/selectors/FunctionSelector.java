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
package org.apache.marmotta.ldpath.model.selectors;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.NodeFunction;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;

/**
 * Apply a function to the collection of nodes passed as argument of the selector.
 *
 * @param <Node> the node type used by the backend
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public class FunctionSelector<Node> implements NodeSelector<Node> {

    private List<NodeSelector<Node>> selectors;
    private NodeFunction<Collection<Node>,Node> function;


    public FunctionSelector(NodeFunction<Collection<Node>,Node> function, List<NodeSelector<Node>> selectors) {
        this.function  = function;
        this.selectors = selectors;
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
    public Collection<Node> select(RDFBackend<Node> nodeRDFBackend, Node context, List<Node> path, Map<Node, List<Node>> resultPaths) {
        ArrayList<Collection<Node>> args = new ArrayList<Collection<Node>>();

        // for a function, we include in the result path all paths to all arguments, so we create a new map to collect the paths
        Map<Node, List<Node>> myResultPaths = null;
        if(resultPaths != null && path != null) {
            myResultPaths = new HashMap<Node, List<Node>>();
        }

        for(NodeSelector<Node> selector : selectors) {
            Collection<Node> param = selector.select(nodeRDFBackend, context, path, myResultPaths);
            args.add(param);
        }
        @SuppressWarnings("unchecked")
        Collection<Node> result = function.apply(nodeRDFBackend, context, args.toArray(new Collection[selectors.size()]));
        if(myResultPaths != null && path != null) {
            // for a function, we include in the result path all paths to all arguments ...
            List<Node> functionPath = new ArrayList<Node>();
            for(List<Node> subpath : myResultPaths.values()) {
                for(Node n : subpath) {
                    if(!functionPath.contains(n)) {
                        functionPath.add(n);
                    }
                }
            }

            for(Node n : result) {
                resultPaths.put(n,functionPath);
            }
        }
        return result;
    }



    /**
     * Return the name of the NodeSelector for registration in the selector registry
     *
     * @return
     * @param backend
     */
    @Override
    public String getPathExpression(NodeBackend<Node> backend) {
        final StringBuilder format = new StringBuilder();
        format.append(String.format("fn:%s(", function.getPathExpression(backend)));
        boolean first = true;
        for (NodeSelector<Node> ns : selectors) {
            if (!first) {
                format.append(", ");
            }
            format.append(ns.getPathExpression(backend));
            first = false;
        }
        return format.append(")").toString();
    }

    /**
     * Return a name for this selector to be used as the name for the whole path if not explicitly
     * specified. In complex selector expressions, this is typically delegated to the first
     * occurrence of an atomic selector.
     */
    @Override
    public String getName(NodeBackend<Node> nodeRDFBackend) {
        throw new UnsupportedOperationException("cannot use functions in unnamed field definitions because the name is ambiguous");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        FunctionSelector<Node> that = (FunctionSelector<Node>) o;

        if (function != null ? !function.equals(that.function) : that.function != null) {
            return false;
        }
        if (selectors != null ? !selectors.equals(that.selectors) : that.selectors != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = selectors != null ? selectors.hashCode() : 0;
        result = 31 * result + (function != null ? function.hashCode() : 0);
        return result;
    }
}
