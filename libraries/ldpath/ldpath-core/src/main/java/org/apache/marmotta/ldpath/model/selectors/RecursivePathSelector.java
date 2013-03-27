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

import com.google.common.collect.ImmutableList;

import java.util.*;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.selectors.NodeSelector;

public class RecursivePathSelector<Node> implements NodeSelector<Node> {

	private final NodeSelector<Node> delegate;
	private final int minRecursions, maxRecursions;

	public RecursivePathSelector(NodeSelector<Node> delegate, int min, int max) {
		this.delegate = delegate;
		minRecursions = min;
		maxRecursions = max;
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
    public Collection<Node> select(RDFBackend<Node> rdfBackend, Node context, List<Node> path, Map<Node, List<Node>> resultPaths) {
		Set<Node> result = new HashSet<Node>();

		if (minRecursions <= 0) {
			result.add(context);
		}
		subSelect(context, 0, rdfBackend, result,path,resultPaths);

		return result;
	}

	private void subSelect(Node currentContext, int depth, RDFBackend<Node> rdfBackend, Set<Node> resultSet, List<Node> path, Map<Node, List<Node>> resultPaths) {
		Collection<Node> nextNodes = delegate.select(rdfBackend, currentContext,path,resultPaths);
		depth++;
		for (Node n : nextNodes) {
			if (!resultSet.contains(n)) {
				if (depth >= minRecursions){
					resultSet.add(n);
				}
				if (depth < maxRecursions) {
                    if(path != null && resultPaths != null) {
					    subSelect(n, depth, rdfBackend, resultSet, new ImmutableList.Builder<Node>().addAll(path).add(currentContext).build(),resultPaths);
                    } else {
                        subSelect(n, depth, rdfBackend, resultSet, null,resultPaths);
                    }
				}
			}
		}
	}

    /**
     * Return the name of the NodeSelector for registration in the selector registry
     *
     * @param rdfBackend
     * @return
     */
    @Override
    public String getPathExpression(NodeBackend<Node> rdfBackend) {
    	if (maxRecursions != Integer.MAX_VALUE) {
    		if (minRecursions <= 0) {
    	    	return String.format("(%s){,%d}", delegate.getPathExpression(rdfBackend), maxRecursions);
    		} else {
    	    	return String.format("(%s){%d,%d}", delegate.getPathExpression(rdfBackend), minRecursions, maxRecursions);
    		}
    	} else {
    		if (minRecursions <= 0) {
    	    	return String.format("(%s)*", delegate.getPathExpression(rdfBackend));
    		} else if (minRecursions == 1) {
    	    	return String.format("(%s)+", delegate.getPathExpression(rdfBackend));
    		} else {
    	    	return String.format("(%s){%d,}", delegate.getPathExpression(rdfBackend), minRecursions);
    		}
    	}
	}

    /**
     * Return a name for this selector to be used as the name for the whole path if not explicitly
     * specified. In complex selector expressions, this is typically delegated to the first
     * occurrence of an atomic selector.
     */
    @Override
    public String getName(NodeBackend<Node> nodeRDFBackend) {
        return delegate.getName(nodeRDFBackend);
    }

    /**
     * <code>(delegate)*</code>
     * @param delegate the delegate
     */
    public static <N> RecursivePathSelector<N> getPathSelectorStared(NodeSelector<N> delegate) {
    	return new RecursivePathSelector<N>(delegate, 0, Integer.MAX_VALUE);
    }

    /**
     * <code>(delegate)+</code>
     * @param delegate the delegate
     */
    public static <N> RecursivePathSelector<N> getPathSelectorPlused(NodeSelector<N> delegate) {
    	return new RecursivePathSelector<N>(delegate, 1, Integer.MAX_VALUE);
    }
    
    /**
     * <code>(delegate){m,}</code>
     * @param delegate the delegate
     * @param minBound <code>m</code>
     */
    public static <N> RecursivePathSelector<N> getPathSelectorMinBound(NodeSelector<N> delegate, int minBound) {
    	return new RecursivePathSelector<N>(delegate, minBound, Integer.MAX_VALUE);
    }

    /**
     * <code>(delegate){,n}</code>
     * @param delegate the delegate
     * @param maxBound <code>n</code>
     */
    public static <N> RecursivePathSelector<N> getPathSelectorMaxBound(NodeSelector<N> delegate, int maxBound) {
    	return new RecursivePathSelector<N>(delegate, 0, maxBound);
    }

    /**
     * <code>(delegate){m,n}</code>
     * @param delegate the delegate
     * @param minBound <code>m</code>
     * @param maxBound <code>n</code>
     */
    public static <N> RecursivePathSelector<N> getPathSelectorMinMaxBound(NodeSelector<N> delegate, int minBound, int maxBound) {
    	return new RecursivePathSelector<N>(delegate, minBound, maxBound);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
		RecursivePathSelector<Node> that = (RecursivePathSelector<Node>) o;

        if (delegate != null ? !delegate.equals(that.delegate) : that.delegate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return delegate != null ? delegate.hashCode() : 0;
    }
}
