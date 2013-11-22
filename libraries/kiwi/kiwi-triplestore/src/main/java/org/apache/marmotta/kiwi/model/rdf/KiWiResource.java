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
package org.apache.marmotta.kiwi.model.rdf;

import org.openrdf.model.Resource;

import java.util.Date;

/**
 * KiWiResources correspond to RDF resources. A KiWi resource is either an anonymous 
 * resource or a URIResource. A KiWiResource represents an RDF resource. However, there
 * is no 1:1 correspondance, as there may be several KiWiResources in different
 * KnowledgeSpaces that represent the same RDF resource.
 * <p>
 * Each KiWiResource has a 1:1 correspondance to a content item. Each KiWiResource is 
 * also directly related with a Sesame 2 resource in the knowledge base backend. 
 * The kiwi.sesame package contains appropriate adaptors for transforming Sesame 2 
 * resources into KiWiResources.
 * <p>
 * 
 * Each KiWiResoure furthermore participates in exactly one knowledge space, where the RDF 
 * resource may have different identifiers for the users. This knowledge space defines the 
 * getTripleStore().of the node. For this reason, a Sesame 2 Value may correspond to several
 * KiWiNodes in different knowledge spaces.
 * <p>
 * TODO: we could further simplify the data model by treating TextContent and MediaContent
 * as literals that are in a certain relation with the resource ("kiwi:hasTextContent",
 * "kiwi:hasMediaContent") and introducing the following new literal types:
 * KiWiTextContentLiteral
 * KiWiMediaContentLiteral
 * KiWiStringContentLiteral
 *
 * The class hierarchy should be changed so that from KiWiNode -> KiWiLiteral and from
 * KiWiNode -> KiWiResource there is joined subclass, and from there on single table
 *
 * currently existing functionality should be moved to ContentItemService
 *
 * @author Sebastian Schaffert
 */

public abstract class KiWiResource extends KiWiNode implements Resource {
    
	private static final long serialVersionUID = 1L;


    protected KiWiResource() {
        super();
    }

    protected KiWiResource(Date created) {
        super(created);
    }


    /**
     * Return true if the node is a literal. Since all resources are not literals,
     * returns false.
     *
     * @return false (the node is always a resource)
     */
    @Override
    public boolean isLiteral() {
        return false;
    }


    /**
     * Return true if the resource is an anonymous resource / blank node
     * @return
     */
    @Override
    public abstract boolean isAnonymousResource();

    @Override
    public abstract boolean isUriResource();


}
