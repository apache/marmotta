/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.commons.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Namespace LDP
 */
public class LDP {

    public static final String NAMESPACE = "http://www.w3.org/ns/ldp#";

    public static final String PREFIX = "ldp";

    /**
     * A Linked Data Platform Container (LDPC) that also conforms to
     * additional patterns and conventions for managing members.  It is distinguished from
     * CompositeContainer by the following behaviors:
     * <ol>
     * <li>Clients cannot assume that an AggregateContainer, when deleted, deletes its members.
     * </ol>
     * <p/>
     * While every attempt is made to be complete in this list, readers should also refer
     * to the specification defining this ontology.
     */
    public static final URI AggregateContainer;

    /**
     * FIXME: Not yet part of the official vocab, but used in the Spec. (2014-02-18)
     */
    public static final URI BasicContainer;

    /**
     * A Linked Data Platform Container (LDPC) that also conforms to
     * additional patterns and conventions for managing members.  It is distinguished from
     * AggregateContainer by the following behaviors:
     * <ol>
     * <li>An CompositeContainer, when deleted, must delete all its members.
     * </ol>
     * <p/>
     * While every attempt is made to be complete in this list, readers should also
     * refer to the specification defining this ontology.
     */
    public static final URI CompositeContainer;

    /**
     * A Linked Data Platform Resource (LDPR) that also conforms to
     * additional patterns and conventions for managing membership.
     * Readers should refer to the specification defining this ontology for the list of
     * behaviors associated with it.
     */
    public static final URI Container;

    /**
     * A resource that represents a limited set of members of a LDP Container.
     */
    public static final URI Page;

    /**
     * A HTTP-addressable resource with a linked data representation.
     */
    public static final URI Resource;

    /**
     * A HTTP-addressable resource with a RDF Source representation.
     */
    public static final URI RdfResource;

    /**
     * A HTTP-addressable resource with a Non-RDF Source representation.
     */
    public static final URI NonRdfResource;

    /**
     * FIXME: Not yet part of the vocab, but used in the spec. (2014-02-24)
     */
    public static final URI contains;

    /**
     * List of predicates that indicate the ascending order of the members in a page.
     */
    public static final URI containerSortPredicates;

    /**
     * FIXME: Not yet part of the vocab, but used in the spec. (2014-02-18)
     */
    public static final URI member;

    /**
     * Indicates which predicate of the container should be used to determine the membership.
     */
    public static final URI membershipPredicate;

    /**
     * Indicates which resource is the subject for the members of the container.
     */
    public static final URI membershipSubject;

    /**
     * From a known page, how to indicate the next or last page as rdf:nil.
     */
    public static final URI nextPage;

    /**
     * Associated a page with its container.
     */
    public static final URI pageOf;


    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        AggregateContainer = factory.createURI(LDP.NAMESPACE, "AggregateContainer");
        BasicContainer = factory.createURI(LDP.NAMESPACE, "BasicContainer");
        CompositeContainer = factory.createURI(LDP.NAMESPACE, "CompositeContainer");
        Container = factory.createURI(LDP.NAMESPACE, "Container");
        Page = factory.createURI(LDP.NAMESPACE, "Page");
        Resource = factory.createURI(LDP.NAMESPACE, "Resource");
        RdfResource = factory.createURI(LDP.NAMESPACE, "RdfResource"); //TODO: missing term in the vocab
        NonRdfResource = factory.createURI(LDP.NAMESPACE, "NonRdfResource"); //TODO: missing term in the vocab
        contains = factory.createURI(LDP.NAMESPACE, "contains");
        containerSortPredicates = factory.createURI(LDP.NAMESPACE, "containerSortPredicates");
        member = factory.createURI(LDP.NAMESPACE, "member");
        membershipPredicate = factory.createURI(LDP.NAMESPACE, "membershipPredicate");
        membershipSubject = factory.createURI(LDP.NAMESPACE, "membershipSubject");
        nextPage = factory.createURI(LDP.NAMESPACE, "nextPage");
        pageOf = factory.createURI(LDP.NAMESPACE, "pageOf");
    }

}
