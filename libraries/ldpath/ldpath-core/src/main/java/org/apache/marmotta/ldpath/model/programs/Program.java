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
package org.apache.marmotta.ldpath.model.programs;


import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.marmotta.ldpath.api.LDPathConstruct;
import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.tests.NodeTest;
import org.apache.marmotta.ldpath.model.Constants;
import org.apache.marmotta.ldpath.model.fields.FieldMapping;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert <sebastian.schaffert@salzburgresearch.at>
 */
public class Program<Node> implements LDPathConstruct<Node> {

    public static final Map<String, String> DEFAULT_NAMESPACES;
    static {
        HashMap<String, String> defNS = new HashMap<String, String>();
        defNS.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        defNS.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        defNS.put("owl", "http://www.w3.org/2002/07/owl#");
        defNS.put("skos", "http://www.w3.org/2004/02/skos/core#");
        defNS.put("foaf", "http://xmlns.com/foaf/0.1/");
        defNS.put("dc", "http://purl.org/dc/elements/1.1/");
        defNS.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        defNS.put("lmf", "http://www.newmedialab.at/lmf/types/1.0/");
        defNS.put("fn", Constants.NS_LMF_FUNCS);
        DEFAULT_NAMESPACES = Collections.unmodifiableMap(defNS);
    }

    public static final String DOCUMENT_BOOST_TYPE = "http://www.w3.org/2001/XMLSchema#float";

    /**
     * A map mapping from namespace prefix to namespace URI
     */
    private Map<String, String> namespaces;
    
    /**
     * Restrict evaluation of the program to the graphs/contexts
     */
    private Set<Node> graphs;

    /**
     * An (optional) filter to use for checking which resources should be
     * indexed.
     */
    private NodeTest<Node> filter;

    /**
     * An (optional) selector to resolve a document boost factor.
     */
    private FieldMapping<Float,Node> booster;
    
    /**
     * The field mappings contained in this program.
     */
    private Set<FieldMapping<?,Node>> fields;

    public Program() {
        namespaces = new LinkedHashMap<String, String>();
        fields = new LinkedHashSet<FieldMapping<?,Node>>();
        graphs = new HashSet<Node>();
    }

    public void addNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }

    public void addMapping(FieldMapping<?,Node> mapping) {
        fields.add(mapping);
    }

    public Set<FieldMapping<?,Node>> getFields() {
        return fields;
    }
    
    public FieldMapping<?,Node> getField(String name) {
        for(FieldMapping<?,Node> m : fields) {
            if(name.equals(m.getFieldName())) {
                return m;
            }
        }
        return null;
    }

    public void setFields(Set<FieldMapping<?,Node>> fields) {
        this.fields = fields;
    }

    public NodeTest<Node> getFilter() {
        return filter;
    }

    public void setFilter(NodeTest<Node> filter) {
        this.filter = filter;
    }

    public FieldMapping<Float,Node> getBooster() {
        return booster;
    }

    public void setBooster(FieldMapping<Float,Node> boost) {
        this.booster = boost;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = new LinkedHashMap<String, String>(namespaces);
    }
    
    public Set<Node> getGraphs() {
        return this.graphs;
    }
    
    @SuppressWarnings("unchecked")
    public Node[] getGraphArr() {
        return (Node[]) this.graphs.toArray(new Object[this.graphs.size()]);
    }
    
    public void setGraphs(Collection<Node> graphs) {
        this.graphs.clear();
        this.graphs.addAll(graphs);
    }
    
    /**
     * Executes this Program on the parsed {@link RDFBackend backend}. 
     * @param context The context of the execution
     * @return The result
     */
    public Map<String,Collection<?>> execute(RDFBackend<Node> backend, Node context) {
        Map<String,Collection<?>> result = new HashMap<String, Collection<?>>();

        for(FieldMapping<?,Node> mapping : getFields()) {
            result.put(mapping.getFieldName(),mapping.getValues(backend,context));
        }
        return result;
    }


    /**
     * Executes this Program on the parsed {@link RDFBackend backend}. 
     * @param context The context of the execution
     * @return The result
     */
    public Map<String,Collection<?>> execute(RDFBackend<Node> backend, Node context, Map<Node, List<Node>> paths) {
        Map<String,Collection<?>> result = new HashMap<String, Collection<?>>();

        for(FieldMapping<?,Node> mapping : getFields()) {
            result.put(mapping.getFieldName(),mapping.getValues(backend,context, paths));
        }
        return result;
    }

    
    
    public String getPathExpression(NodeBackend<Node> backend) {
        StringBuilder sb = new StringBuilder();
        // Filter (?)
        if (filter != null) {
            sb.append(String.format("@filter %s ;%n", filter.getPathExpression(backend)));
        }

        // Booster (?)
        if (booster != null) {
            sb.append(String.format("@boost %s ;%n", booster.getSelector().getPathExpression(backend)));
        }

        // Field-Definitions
		for (FieldMapping<?,Node> field : fields) {
            sb.append(String.format("  %s%n", field.getPathExpression(backend)));
        }
        String progWithoutNamespace = sb.toString();

        // Definded Namespaces (reverse sorted, to give longer prefixes precedence over shorter)
        final TreeSet<Entry<String, String>> sortedNamespaces = new TreeSet<Entry<String,String>>(new Comparator<Entry<String, String>>() {
            @Override
            public int compare(Entry<String, String> e1, Entry<String, String> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });
        sortedNamespaces.addAll(namespaces.entrySet());
        for (Entry<String, String> ns : sortedNamespaces) {
            progWithoutNamespace = progWithoutNamespace.replaceAll("<" + Pattern.quote(ns.getValue()) + "([^>]*)>", Matcher.quoteReplacement(ns.getKey())
                    + ":$1");
        }
        

        // Also resolve default namespaces...
        for (Entry<String, String> ns : DEFAULT_NAMESPACES.entrySet()) {
            if (!namespaces.containsKey(ns.getKey())) {
                progWithoutNamespace = progWithoutNamespace.replaceAll("<" + Pattern.quote(ns.getValue()) + "([^>]*)>",
                        Matcher.quoteReplacement(ns.getKey()) + ":$1");
            }
        }
        final StringBuilder prefixes = new StringBuilder();
        for (Entry<String, String> ns : namespaces.entrySet()) {
        	prefixes.append(String.format("@prefix %s : <%s> ;%n", ns.getKey(), ns.getValue()));
		}

        return prefixes.append(progWithoutNamespace).toString();
    }

}
