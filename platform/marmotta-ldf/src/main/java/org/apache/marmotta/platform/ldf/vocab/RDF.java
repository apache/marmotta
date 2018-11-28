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
package org.apache.marmotta.platform.ldf.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * The RDF Concepts Vocabulary (RDF).
 * <p>
 * This is the RDF Schema for the RDF vocabulary terms in the RDF
 * Namespace, defined in RDF 1.1 Concepts..
 * <p>
 * Namespace 22-rdf-syntax-ns.
 * Prefix: {@code <http://www.w3.org/1999/02/22-rdf-syntax-ns#>}
 */
public class RDF {

    /** {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#} **/
    public static final String NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** {@code 22-rdf-syntax-ns} **/
    public static final String PREFIX = "22-rdf-syntax-ns";

    /**
     * Alt
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt}.
     * <p>
     * The class of containers of alternatives.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt">Alt</a>
     */
    public static final URI Alt;

    /**
     * Bag
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag}.
     * <p>
     * The class of unordered containers.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag">Bag</a>
     */
    public static final URI Bag;

    /**
     * first
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#first}.
     * <p>
     * The first item in the subject RDF list.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#first">first</a>
     */
    public static final URI first;

    /**
     * HTML
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML}.
     * <p>
     * The datatype of RDF literals storing fragments of HTML content
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML">HTML</a>
     */
    public static final URI HTML;

    /**
     * langString
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#langString}.
     * <p>
     * The datatype of language-tagged string values
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#langString">langString</a>
     */
    public static final URI langString;

    /**
     * List
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#List}.
     * <p>
     * The class of RDF Lists.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#List">List</a>
     */
    public static final URI List;

    /**
     * nil
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#nil}.
     * <p>
     * The empty list, with no items in it. If the rest of a list is nil then
	 * the list has no more items in it.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#nil">nil</a>
     */
    public static final URI nil;

    /**
     * object
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#object}.
     * <p>
     * The object of the subject RDF statement.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#object">object</a>
     */
    public static final URI object;

    /**
     * PlainLiteral
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral}.
     * <p>
     * The class of plain (i.e. untyped) literal values, as used in RIF and
	 * OWL 2
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral">PlainLiteral</a>
     */
    public static final URI PlainLiteral;

    /**
     * predicate
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate}.
     * <p>
     * The predicate of the subject RDF statement.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate">predicate</a>
     */
    public static final URI predicate;

    /**
     * Property
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Property}.
     * <p>
     * The class of RDF properties.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#Property">Property</a>
     */
    public static final URI Property;

    /**
     * rest
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#rest}.
     * <p>
     * The rest of the subject RDF list after the first item.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#rest">rest</a>
     */
    public static final URI rest;

    /**
     * Seq
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq}.
     * <p>
     * The class of ordered containers.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq">Seq</a>
     */
    public static final URI Seq;

    /**
     * Statement
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement}.
     * <p>
     * The class of RDF statements.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement">Statement</a>
     */
    public static final URI Statement;

    /**
     * subject
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#subject}.
     * <p>
     * The subject of the subject RDF statement.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#subject">subject</a>
     */
    public static final URI subject;

    /**
     * type
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#type}.
     * <p>
     * The subject is an instance of a class.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#type">type</a>
     */
    public static final URI type;

    /**
     * value
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#value}.
     * <p>
     * Idiomatic property used for structured values.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#value">value</a>
     */
    public static final URI value;

    /**
     * XMLLiteral
     * <p>
     * {@code http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral}.
     * <p>
     * The datatype of XML literal values.
     *
     * @see <a href="http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral">XMLLiteral</a>
     */
    public static final URI XMLLiteral;

    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();

        Alt = factory.createURI(RDF.NAMESPACE, "Alt");
        Bag = factory.createURI(RDF.NAMESPACE, "Bag");
        first = factory.createURI(RDF.NAMESPACE, "first");
        HTML = factory.createURI(RDF.NAMESPACE, "HTML");
        langString = factory.createURI(RDF.NAMESPACE, "langString");
        List = factory.createURI(RDF.NAMESPACE, "List");
        nil = factory.createURI(RDF.NAMESPACE, "nil");
        object = factory.createURI(RDF.NAMESPACE, "object");
        PlainLiteral = factory.createURI(RDF.NAMESPACE, "PlainLiteral");
        predicate = factory.createURI(RDF.NAMESPACE, "predicate");
        Property = factory.createURI(RDF.NAMESPACE, "Property");
        rest = factory.createURI(RDF.NAMESPACE, "rest");
        Seq = factory.createURI(RDF.NAMESPACE, "Seq");
        Statement = factory.createURI(RDF.NAMESPACE, "Statement");
        subject = factory.createURI(RDF.NAMESPACE, "subject");
        type = factory.createURI(RDF.NAMESPACE, "type");
        value = factory.createURI(RDF.NAMESPACE, "value");
        XMLLiteral = factory.createURI(RDF.NAMESPACE, "XMLLiteral");
    }

    private RDF() {
        //static access only
    }

}
