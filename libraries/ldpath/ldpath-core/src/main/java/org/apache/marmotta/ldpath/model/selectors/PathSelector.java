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


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;

/**
 * Traverse a path by following several edges in the RDF graph. Each step is separated by a "/".
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public class PathSelector<Node> implements NodeSelector<Node> {

    private NodeSelector<Node> left;
    private NodeSelector<Node> right;

    public PathSelector(NodeSelector<Node> left, NodeSelector<Node> right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Apply the selector to the context node passed as argument and return the collection
     * of selected nodes in appropriate order.
     *
     * @param context     the node where to start the selection
     * @param path        the path leading to and including the context node in the current evaluation of LDPath; may be null,
     *                    in which case path tracking is disabled
     * @param resultPaths a map where each of the result nodes maps to a path leading to the result node in the LDPath evaluation;
     *                    if null, path tracking is disabled and the path argument is ignored
     * @return the collection of selected nodes
     */
    @Override
    public Collection<Node> select(RDFBackend<Node> rdfBackend, Node context, List<Node> path, Map<Node, List<Node>> resultPaths) {
        // a new map for storing the result path for the left selector
        Map<Node,List<Node>> myResultPaths = null;
        if(resultPaths != null && path != null) {
            myResultPaths = new HashMap<Node, List<Node>>();
        }
        
        Collection<Node> nodesLeft = left.select(rdfBackend,context,path,myResultPaths);
        final Set<Node> result = new HashSet<Node>();

        
        
        for(Node n : nodesLeft) {
            // new path is the path resulting from selecting the context node in the left selector
            if(myResultPaths != null && myResultPaths.get(n) != null) {
                result.addAll(right.select(rdfBackend,n,myResultPaths.get(n),resultPaths));
            } else {
                result.addAll(right.select(rdfBackend,n,null,null));
            }
        }
        return result;
    }


    @Override
    public String getPathExpression(NodeBackend<Node> backend) {
        return String.format("%s / %s", left.getPathExpression(backend), right.getPathExpression(backend));
    }

    /**
     * Return a name for this selector to be used as the name for the whole path if not explicitly
     * specified. In complex selector expressions, this is typically delegated to the first
     * occurrence of an atomic selector.
     */
    @Override
    public String getName(NodeBackend<Node> nodeRDFBackend) {
        return left.getName(nodeRDFBackend);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("rawtypes")
		PathSelector that = (PathSelector) o;

        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
