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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * W3C Linked Data Platform (LDP).
 * <p>
 * This ontology provides an informal representation of the concepts and
 * terms as defined in the LDP specification. Consult the LDP
 * specification for normative reference..
 * <p>
 * Namespace Ldp.
 * Prefix: {@code <http://www.w3.org/ns/ldp#>}
 *
 * @see <a href="http://www.w3.org/2012/ldp">http://www.w3.org/2012/ldp</a>
 * @see <a href="http://www.w3.org/TR/ldp-ucr/">http://www.w3.org/TR/ldp-ucr/</a>
 * @see <a href="http://www.w3.org/TR/ldp/">http://www.w3.org/TR/ldp/</a>
 * @see <a href="http://www.w3.org/2011/09/LinkedData/">http://www.w3.org/2011/09/LinkedData/</a>
 */
public class LDP {

    /** {@code http://www.w3.org/ns/ldp#} **/
    public static final String NAMESPACE = "http://www.w3.org/ns/ldp#";

    /** {@code ldp} **/
    public static final String PREFIX = "ldp";

    /**
     * BasicContainer
     * <p>
     * {@code http://www.w3.org/ns/ldp#BasicContainer}.
     * <p>
     * An LDPC that uses a predefined predicate to simply link to its
     * contained resources.
     *
     * @see <a href="http://www.w3.org/ns/ldp#BasicContainer">BasicContainer</a>
     */
    public static final IRI BasicContainer;

    /**
     * Container
     * <p>
     * {@code http://www.w3.org/ns/ldp#Container}.
     * <p>
     * A Linked Data Platform RDF Source (LDP-RS) that also conforms to
     * additional patterns and conventions for managing membership. Readers
     * should refer to the specification defining this ontology for the list
     * of behaviors associated with it.
     *
     * @see <a href="http://www.w3.org/ns/ldp#Container">Container</a>
     */
    public static final IRI Container;

    /**
     * contains
     * <p>
     * {@code http://www.w3.org/ns/ldp#contains}.
     * <p>
     * Links a container with resources created through the container.
     *
     * @see <a href="http://www.w3.org/ns/ldp#contains">contains</a>
     */
    public static final IRI contains;

    /**
     * DirectContainer
     * <p>
     * {@code http://www.w3.org/ns/ldp#DirectContainer}.
     * <p>
     * An LDPC that is similar to a LDP-DC but it allows an indirection with
     * the ability to list as member a resource, such as a IRI representing a
     * real-world object, that is different from the resource that is created
     *
     * @see <a href="http://www.w3.org/ns/ldp#DirectContainer">DirectContainer</a>
     */
    public static final IRI DirectContainer;

    /**
     * hasMemberRelation
     * <p>
     * {@code http://www.w3.org/ns/ldp#hasMemberRelation}.
     * <p>
     * Indicates which predicate is used in membership triples, and that the
     * membership triple pattern is < membership-constant-IRI ,
     * object-of-hasMemberRelation, member-IRI >.
     *
     * @see <a href="http://www.w3.org/ns/ldp#hasMemberRelation">hasMemberRelation</a>
     */
    public static final IRI hasMemberRelation;

    /**
     * IndirectContainer
     * <p>
     * {@code http://www.w3.org/ns/ldp#IndirectContainer}.
     * <p>
     * An LDPC that has the flexibility of choosing what form the membership
     * triples take.
     *
     * @see <a href="http://www.w3.org/ns/ldp#IndirectContainer">IndirectContainer</a>
     */
    public static final IRI IndirectContainer;

    /**
     * insertedContentRelation
     * <p>
     * {@code http://www.w3.org/ns/ldp#insertedContentRelation}.
     * <p>
     * Indicates which triple in a creation request should be used as the
     * member-IRI value in the membership triple added when the creation
     * request is successful.
     *
     * @see <a href="http://www.w3.org/ns/ldp#insertedContentRelation">insertedContentRelation</a>
     */
    public static final IRI insertedContentRelation;

    /**
     * isMemmberOfRelation
     * <p>
     * {@code http://www.w3.org/ns/ldp#isMemberOfRelation}.
     * <p>
     * Indicates which predicate is used in membership triples, and that the
     * membership triple pattern is < member-IRI ,
     * object-of-isMemberOfRelation, membership-constant-IRI >.
     *
     * @see <a href="http://www.w3.org/ns/ldp#isMemberOfRelation">isMemberOfRelation</a>
     */
    public static final IRI isMemberOfRelation;

    /**
     * member
     * <p>
     * {@code http://www.w3.org/ns/ldp#member}.
     * <p>
     * LDP servers should use this predicate as the membership predicate if
     * there is no obvious predicate from an application vocabulary to use.
     *
     * @see <a href="http://www.w3.org/ns/ldp#member">member</a>
     */
    public static final IRI member;

    /**
     * membershipResource
     * <p>
     * {@code http://www.w3.org/ns/ldp#membershipResource}.
     * <p>
     * Indicates the membership-constant-IRI in a membership triple.
     * Depending upon the membership triple pattern a container uses, as
     * indicated by the presence of ldp:hasMemberRelation or
     * ldp:isMemberOfRelation, the membership-constant-IRI might occupy
     * either the subject or object position in membership triples.
     *
     * @see <a href="http://www.w3.org/ns/ldp#membershipResource">membershipResource</a>
     */
    public static final IRI membershipResource;

    /**
     * MemberSubject
     * <p>
     * {@code http://www.w3.org/ns/ldp#MemberSubject}.
     * <p>
     * Used to indicate default and typical behavior for
     * ldp:insertedContentRelation, where the member-IRI value in the
     * membership triple added when a creation request is successful is the
     * IRI assigned to the newly created resource.
     *
     * @see <a href="http://www.w3.org/ns/ldp#MemberSubject">MemberSubject</a>
     */
    public static final IRI MemberSubject;

    /**
     * NonRDFSource
     * <p>
     * {@code http://www.w3.org/ns/ldp#NonRDFSource}.
     * <p>
     * A Linked Data Platform Resource (LDPR) whose state is NOT represented
     * as RDF.
     *
     * @see <a href="http://www.w3.org/ns/ldp#NonRDFSource">NonRDFSource</a>
     */
    public static final IRI NonRDFSource;

    /**
     * PreferContainment
     * <p>
     * {@code http://www.w3.org/ns/ldp#PreferContainment}.
     * <p>
     * IRI identifying a LDPC's containment triples, for example to allow
     * clients to express interest in receiving them.
     *
     * @see <a href="http://www.w3.org/ns/ldp#PreferContainment">PreferContainment</a>
     */
    public static final IRI PreferContainment;

    /**
     * PreferEmptyContainer
     * <p>
     * {@code http://www.w3.org/ns/ldp#PreferEmptyContainer}.
     * <p>
     * IRI identifying the subset of a LDPC's triples present in an empty
     * LDPC, for example to allow clients to express interest in receiving
     * them. Currently this excludes containment and membership triples, but
     * in the future other exclusions might be added. This definition is
     * written to automatically exclude those new classes of triples.
     *
     * @see <a href="http://www.w3.org/ns/ldp#PreferEmptyContainer">PreferEmptyContainer</a>
     * @deprecated use {@link #PreferMinimalContainer} instead
     */
    @Deprecated
    public static final IRI PreferEmptyContainer;

    /**
     * PreferMinimalContainer
     * <p>
     * {@code http://www.w3.org/ns/ldp#PreferMinimalContainer}.
     * <p>
     * IRI identifying the subset of a LDPC's triples present in an empty
     * LDPC, for example to allow clients to express interest in receiving
     * them. Currently this excludes containment and membership triples, but
     * in the future other exclusions might be added. This definition is
     * written to automatically exclude those new classes of triples.
     *
     * @see <a href="http://www.w3.org/ns/ldp#PreferMinimalContainer">PreferMinimalContainer</a>
    */
    public static final IRI PreferMinimalContainer;

    /**
     * PreferMembership
     * <p>
     * {@code http://www.w3.org/ns/ldp#PreferMembership}.
     * <p>
     * IRI identifying a LDPC's membership triples, for example to allow
     * clients to express interest in receiving them.
     *
     * @see <a href="http://www.w3.org/ns/ldp#PreferMembership">PreferMembership</a>
     */
    public static final IRI PreferMembership;

    /**
     * RDFSource
     * <p>
     * {@code http://www.w3.org/ns/ldp#RDFSource}.
     * <p>
     * A Linked Data Platform Resource (LDPR) whose state is represented as
     * RDF.
     *
     * @see <a href="http://www.w3.org/ns/ldp#RDFSource">RDFSource</a>
     */
    public static final IRI RDFSource;

    /**
     * Resource
     * <p>
     * {@code http://www.w3.org/ns/ldp#Resource}.
     * <p>
     * A HTTP-addressable resource whose lifecycle is managed by a LDP
     * server.
     *
     * @see <a href="http://www.w3.org/ns/ldp#Resource">Resource</a>
     */
    public static final IRI Resource;

    static {
        ValueFactory factory = SimpleValueFactory.getInstance();

        BasicContainer = factory.createIRI(LDP.NAMESPACE, "BasicContainer");
        Container = factory.createIRI(LDP.NAMESPACE, "Container");
        contains = factory.createIRI(LDP.NAMESPACE, "contains");
        DirectContainer = factory.createIRI(LDP.NAMESPACE, "DirectContainer");
        hasMemberRelation = factory.createIRI(LDP.NAMESPACE, "hasMemberRelation");
        IndirectContainer = factory.createIRI(LDP.NAMESPACE, "IndirectContainer");
        insertedContentRelation = factory.createIRI(LDP.NAMESPACE, "insertedContentRelation");
        isMemberOfRelation = factory.createIRI(LDP.NAMESPACE, "isMemberOfRelation");
        member = factory.createIRI(LDP.NAMESPACE, "member");
        membershipResource = factory.createIRI(LDP.NAMESPACE, "membershipResource");
        MemberSubject = factory.createIRI(LDP.NAMESPACE, "MemberSubject");
        NonRDFSource = factory.createIRI(LDP.NAMESPACE, "NonRDFSource");
        PreferContainment = factory.createIRI(LDP.NAMESPACE, "PreferContainment");
        PreferEmptyContainer = factory.createIRI(LDP.NAMESPACE, "PreferEmptyContainer");
        PreferMembership = factory.createIRI(LDP.NAMESPACE, "PreferMembership");
        PreferMinimalContainer = factory.createIRI(LDP.NAMESPACE, "PreferMinimalContainer");
        RDFSource = factory.createIRI(LDP.NAMESPACE, "RDFSource");
        Resource = factory.createIRI(LDP.NAMESPACE, "Resource");
    }

    private LDP() {
        //static access only
    }

}
