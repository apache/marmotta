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
package org.apache.marmotta.ldpath.backend.jena;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.util.FormatUtils;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class GenericJenaBackend implements RDFBackend<RDFNode> {


    private Model model;

    public GenericJenaBackend(Model model) {
        this.model = model;
    }


    /**
     * Return true if the underlying backend supports the parallel execution of queries.
     *
     * @return
     */
    @Override
    public boolean supportsThreading() {
        return false;
    }


    /**
     * In case the backend supports threading, this method should return the ExecutorService representing the
     * thread pool. LDPath lets the backend manage the thread pool to avoid excessive threading.
     *
     * @return
     */
    @Override
    public ThreadPoolExecutor getThreadPool() {
        return null;
    }

    /**
     * Test whether the node passed as argument is a literal
     *
     * @param n the node to check
     * @return true if the node is a literal
     */
    @Override
    public boolean isLiteral(RDFNode n) {
        return n.isLiteral();
    }

    /**
     * Test whether the node passed as argument is a URI
     *
     * @param n the node to check
     * @return true if the node is a URI
     */
    @Override
    public boolean isURI(RDFNode n) {
        return n.isURIResource();
    }

    /**
     * Test whether the node passed as argument is a blank node
     *
     * @param n the node to check
     * @return true if the node is a blank node
     */
    @Override
    public boolean isBlank(RDFNode n) {
        return n.isAnon();
    }

    /**
     * Return the language of the literal node passed as argument.
     *
     * @param n the literal node for which to return the language
     * @return a Locale representing the language of the literal, or null if the literal node has no language
     * @throws IllegalArgumentException in case the node is no literal
     */
    @Override
    public Locale getLiteralLanguage(RDFNode n) {
        if(n.isLiteral()) {
            if (((Literal)n).getLanguage() != null) {
                return new Locale(((Literal)n).getLanguage());
            } else {
                return null;
            }
        } else {
            throw new IllegalArgumentException("the node "+n+" is not a literal, cannot return language");
        }
    }

    /**
     * Return the URI of the type of the literal node passed as argument.
     *
     * @param n the literal node for which to return the typer
     * @return a URI representing the type of the literal content, or null if the literal is untyped
     * @throws IllegalArgumentException in case the node is no literal
     */
    @Override
    public URI getLiteralType(RDFNode n) {
        if(n.isLiteral()) {
            if (((Literal)n).getLanguage() != null) {
                try {
                    return new URI(((Literal)n).getDatatypeURI());
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("the type of node "+n+" was not a valid URI");
                }
            } else {
                return null;
            }
        } else {
            throw new IllegalArgumentException("the node "+n+" is not a literal, cannot return literal type");
        }
    }

    /**
     * Create a literal node with the content passed as argument
     *
     * @param content string content to represent inside the literal
     * @return a literal node in using the model used by this backend
     */
    @Override
    public RDFNode createLiteral(String content) {
        return model.createLiteral(content);
    }

    /**
     * Create a literal node with the content passed as argument
     *
     * @param content string content to represent inside the literal
     * @return a literal node in using the model used by this backend
     */
    @Override
    public RDFNode createLiteral(String content, Locale language, URI type) {
        if(language != null && type == null) {
            return model.createLiteral(content,language.getLanguage());
        } else if(language == null && type != null) {
            return model.createTypedLiteral(content, TypeMapper.getInstance().getSafeTypeByName(type.toString()));
        } else {
            return model.createLiteral(content);
        }
    }

    /**
     * Create a URI mode with the URI passed as argument
     *
     * @param uri URI of the resource to create
     * @return a URI node using the model used by this backend
     */
    @Override
    public RDFNode createURI(String uri) {
        return model.createProperty(uri);
    }

    /**
     * Return the lexial representation of a node. For a literal, this will be the content, for a URI node it will be the
     * URI itself, and for a blank node it will be the identifier of the node.
     *
     * @param rdfNode
     * @return
     */
    @Override
    public String stringValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return ((Literal)rdfNode).getString();
        } else if(isURI(rdfNode)) {
            return ((Resource)rdfNode).getURI();
        } else if(isBlank(rdfNode)) {
            return ((Resource)rdfNode).getId().getLabelString();
        } else {
            return rdfNode.toString();
        }
    }

    /**
     * Return the double value of a literal node. Depending on the backend implementing this method,
     * the value can be retrieved directly or must be parsed from the string representation. The method can throw
     * a NumberFormatException or ArithmeticException indicating that the value cannot be represented as double, and an
     * IllegalArgumentException, indicating that the passed node is not a literal
     *
     * @param rdfNode the literal node for which to return the double value
     * @return double value of the literal node
     * @throws NumberFormatException    in case the literal cannot be represented as double value
     * @throws ArithmeticException      in case the literal cannot be represented as double value
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public Double doubleValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return ((Literal)rdfNode).getDouble();
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * Return the long value of a literal node. Depending on the backend implementing this method,
     * the value can be retrieved directly or must be parsed from the string representation. The method can throw
     * a NumberFormatException or ArithmeticException indicating that the value cannot be represented as long, and an
     * IllegalArgumentException, indicating that the passed node is not a literal
     *
     * @param rdfNode the literal node for which to return the long value
     * @return long value of the literal node
     * @throws NumberFormatException    in case the literal cannot be represented as long value
     * @throws ArithmeticException      in case the literal cannot be represented as long value
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public Long longValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return ((Literal)rdfNode).getLong();
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * Return the boolean value of a literal node. Depending on the backend implementing this method,
     * the value can be retrieved directly or must be parsed from the string representation.
     * TODO: Define:<ul>
     * <li> Do we also support '0' '1', 'yes', 'no'; whats about case insensitive
     * such as TRUE, False
     * <li> should we throw an RuntimeException of not an boolean value or return
     * false as {@link Boolean#parseBoolean(String)}
     * </ul>
     *
     * @param rdfNode the literal node for which to return the boolean value
     * @return long value of the literal node
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public Boolean booleanValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return ((Literal)rdfNode).getBoolean();
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * TODO
     *
     * @param rdfNode the literal node for which to return the dateTime value
     * @return long value of the literal node
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public Date dateTimeValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return FormatUtils.parseDate(((Literal)rdfNode).getString());
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * TODO
     *
     * @param rdfNode the literal node for which to return the date value
     * @return long value of the literal node
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public Date dateValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return FormatUtils.parseDate(((Literal)rdfNode).getString());
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * TODO
     *
     * @param rdfNode the literal node for which to return the time value
     * @return long value of the literal node
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public Date timeValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return FormatUtils.parseDate(((Literal)rdfNode).getString());
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * Return the float value of a literal node. Depending on the backend implementing this method,
     * the value can be retrieved directly or must be parsed from the string representation. The method can throw
     * a NumberFormatException or ArithmeticException indicating that the value cannot be represented as float, and an
     * IllegalArgumentException, indicating that the passed node is not a literal
     *
     * @param rdfNode the literal node for which to return the float value
     * @return long value of the literal node
     * @throws NumberFormatException    in case the literal cannot be represented as float value
     * @throws ArithmeticException      in case the literal cannot be represented as float value
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public Float floatValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return ((Literal)rdfNode).getFloat();
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * Return the 32bit integer value of a literal node. Depending on the backend implementing this method,
     * the value can be retrieved directly or must be parsed from the string representation. The method can throw
     * a NumberFormatException or ArithmeticException indicating that the value cannot be represented as integer, and an
     * IllegalArgumentException, indicating that the passed node is not a literal.
     * <p/>
     * Note that this is restricted to 32bit singed integer values as defined by
     * xsd:int and {@link Integer}. For bigger nuber one might want to use
     * xsd:integer represented by {@link java.math.BigInteger}.
     *
     * @param rdfNode the literal node for which to return the Integer (xsd:int) value
     * @return long value of the literal node
     * @throws NumberFormatException    in case the literal cannot be represented as 32 bit integer value
     * @throws ArithmeticException      in case the literal cannot be represented as 32 bit integer value
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public Integer intValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return ((Literal)rdfNode).getInt();
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * Return the arbitrary length integer value of a literal node. Depending on the backend implementing this method,
     * the value can be retrieved directly or must be parsed from the string representation. The method can throw
     * a NumberFormatException or ArithmeticException indicating that the value cannot be represented as integer, and an
     * IllegalArgumentException, indicating that the passed node is not a literal.
     *
     * @param rdfNode the literal node for which to return the {@link java.math.BigInteger xsd:integer} value
     * @return long value of the literal node
     * @throws NumberFormatException    in case the literal cannot be represented as integer value
     * @throws ArithmeticException      in case the literal cannot be represented as long value
     * @throws IllegalArgumentException in case the node passed as argument is integer a literal
     */
    @Override
    public BigInteger integerValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return new BigInteger(((Literal)rdfNode).getString());
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * Return the decimal number of a literal node. Depending on the backend implementing this method,
     * the value can be retrieved directly or must be parsed from the string representation. The method can throw
     * a NumberFormatException or ArithmeticException indicating that the value cannot be represented as decimal, and an
     * IllegalArgumentException, indicating that the passed node is not a literal.
     *
     * @param rdfNode the literal node for which to return the xsd:decimal value
     * @return long value of the literal node
     * @throws NumberFormatException    in case the literal cannot be represented as decimal value
     * @throws ArithmeticException      in case the literal cannot be represented as decimal value
     * @throws IllegalArgumentException in case the node passed as argument is not a literal
     */
    @Override
    public BigDecimal decimalValue(RDFNode rdfNode) {
        if(isLiteral(rdfNode)) {
            return new BigDecimal(((Literal)rdfNode).getString());
        } else {
            throw new IllegalArgumentException("the node "+rdfNode+" is not a literal value");
        }
    }

    /**
     * List the objects of triples in the triple store underlying this backend that have the subject and
     * property given as argument.
     *
     * @param subject  the subject of the triples to look for
     * @param property the property of the triples to look for, <code>null</code> is interpreted as wildcard
     * @return all objects of triples with matching subject and property
     */
    @Override
    public Collection<RDFNode> listObjects(RDFNode subject, RDFNode property) {
        try {
            return ImmutableSet.copyOf(
                    Iterators.transform(
                            model.listStatements((Resource)subject,(Property)property,(RDFNode)null),
                            new Function<Statement, RDFNode>() {
                                @Override
                                public RDFNode apply(Statement input) {
                                    return input.getObject();
                                }
                            })
            );

        } catch(ClassCastException ex) {
            throw new IllegalArgumentException("subject or property where no valid resources in the Jena model",ex);
        }

    }

    /**
     * List the subjects of triples in the triple store underlying this backend that have the object and
     * property given as argument.
     *
     * @param object   the object of the triples to look for
     * @param property the property of the triples to look for, <code>null</code> is interpreted as wildcard
     * @return all subjects of triples with matching object and property
     * @throws UnsupportedOperationException in case reverse selection is not supported (e.g. when querying Linked Data)
     */
    @Override
    public Collection<RDFNode> listSubjects(RDFNode property, RDFNode object) {
        try {
            return ImmutableSet.copyOf(
                    Iterators.transform(
                            model.listStatements((Resource)null,(Property)property,object),
                            new Function<Statement, RDFNode>() {
                                @Override
                                public RDFNode apply(Statement input) {
                                    return input.getSubject();
                                }
                            })
            );
            } catch(ClassCastException ex) {
            throw new IllegalArgumentException("property was no valid resource in the Jena model",ex);
        }
    }
}
