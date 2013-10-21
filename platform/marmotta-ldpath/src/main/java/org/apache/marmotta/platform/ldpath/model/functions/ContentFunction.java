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
package org.apache.marmotta.platform.ldpath.model.functions;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.platform.core.api.content.ContentService;
import org.apache.marmotta.platform.ldpath.api.AutoRegisteredLDPathFunction;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
@ApplicationScoped
public class ContentFunction extends AutoRegisteredLDPathFunction {

    @Inject
    private Logger log;

    @Inject
    private ContentService contentService;

    private String[] allowedTypes = new String[] {
            "text/.*", "application/([a-z]+\\+)?xml", "application/([a-z]+\\+)?json"
    };

    public ContentFunction() {
    }


    @PostConstruct
    public void initialise() {
        log.info("initialising LMF LDPath fn:content(...) function ...");
    }


    /**
     * Apply the function to the list of nodes passed as arguments and return the result as type T.
     * Throws IllegalArgumentException if the function cannot be applied to the nodes passed as argument
     * or the number of arguments is not correct.
     *
     * @param context the context of the execution. Same as using the
     *                {@link org.apache.marmotta.ldpath.api.selectors.NodeSelector} '.' as parameter.
     * @param args    a nested list of KiWiNodes
     * @return
     */
    @Override
    public Collection<Value> apply(RDFBackend<Value> kiWiNodeRDFBackend, Value context, Collection<Value>... args) throws IllegalArgumentException {
        List<Value> result = new LinkedList<Value>();

        for(Collection<? extends Value> nodes : args) {
            for(Value n : nodes) {
                if(n instanceof Resource) {
                    Resource r = (Resource)n;

                    String type = contentService.getContentType(r);

                    if(type != null) {
                        for(String allowedType : allowedTypes) {
                            if(type.matches(allowedType)) {
                                byte[] data = contentService.getContentData(r,type);
                                String content = new String(data);
                                result.add(new LiteralImpl(content));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return the representation of the NodeFunction or NodeSelector in the RDF Path Language
     *
     * @return
     */
    @Override
    public String getLocalName() {
        return "content";
    }

    /**
     * A string describing the signature of this node function, e.g. "fn:content(uris : Nodes) : Nodes". The
     * syntax for representing the signature can be chosen by the implementer. This method is for informational
     * purposes only.
     *
     * @return
     */
    @Override
    public String getSignature() {
        return "fn:content(nodes : URIResourceList) : LiteralList";
    }

    /**
     * A short human-readable description of what the node function does.
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Resolve the URIs passed as argument and retrieve their content using the content reader applicable for the resource.";
    }
}
