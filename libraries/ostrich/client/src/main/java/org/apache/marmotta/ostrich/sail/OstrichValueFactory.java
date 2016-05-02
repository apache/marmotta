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

package org.apache.marmotta.ostrich.sail;

import org.apache.marmotta.commons.sesame.model.LiteralCommons;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.ostrich.model.*;
import org.openrdf.model.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.Random;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class OstrichValueFactory implements ValueFactory {

    private Random anonIdGenerator;

    public OstrichValueFactory() {
        this.anonIdGenerator = new Random();
    }

    /**
     * Creates a new bNode.
     *
     * @return An object representing the bNode.
     */
    @Override
    public BNode createBNode() {
        return new ProtoBNode(Long.toHexString(System.currentTimeMillis())+Integer.toHexString(anonIdGenerator.nextInt(1000)));
    }

    /**
     * Creates a new URI from the supplied string-representation.
     *
     * @param uri A string-representation of a URI.
     * @return An object representing the URI.
     */
    @Override
    public URI createURI(String uri) {
        return new ProtoURI(uri);
    }

    /**
     * Creates a new URI from the supplied namespace and local name. Calling this
     * method is funtionally equivalent to calling {@link #createURI(String)
     * createURI(namespace+localName)}, but allows the ValueFactory to reuse
     * supplied namespace and local name strings whenever possible. Note that the
     * values returned by {@link URI#getNamespace()} and
     * {@link URI#getLocalName()} are not necessarily the same as the values that
     * are supplied to this method.
     *
     * @param namespace The URI's namespace.
     * @param localName The URI's local name.
     * @throws IllegalArgumentException If the supplied namespace and localname do not resolve to a legal
     *                                  (absolute) URI.
     */
    @Override
    public URI createURI(String namespace, String localName) {
        return new ProtoURI(namespace+localName);
    }

    /**
     * Creates a new blank node with the given node identifier.
     *
     * @param nodeID The blank node identifier.
     * @return An object representing the blank node.
     */
    @Override
    public BNode createBNode(String nodeID) {
        return new ProtoBNode(nodeID);
    }

    /**
     * Creates a new literal with the supplied label.
     *
     * @param label The literal's label.
     */
    @Override
    public Literal createLiteral(String label) {
        return new ProtoStringLiteral(label);
    }

    /**
     * Creates a new literal with the supplied label and language attribute.
     *
     * @param label    The literal's label.
     * @param language The literal's language attribute, or <tt>null</tt> if the literal
     */
    @Override
    public Literal createLiteral(String label, String language) {
        return new ProtoStringLiteral(label, language);
    }

    /**
     * Creates a new literal with the supplied label and datatype.
     *
     * @param label    The literal's label.
     * @param datatype The literal's datatype, or <tt>null</tt> if the literal doesn't
     */
    @Override
    public Literal createLiteral(String label, URI datatype) {
        return new ProtoDatatypeLiteral(label, datatype);
    }

    /**
     * Creates a new <tt>xsd:boolean</tt>-typed literal representing the
     * specified value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:boolean</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(boolean value) {
        return new ProtoDatatypeLiteral(Boolean.toString(value), LiteralCommons.getXSDType(Boolean.class));
    }

    /**
     * Creates a new <tt>xsd:byte</tt>-typed literal representing the specified
     * value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:byte</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(byte value) {
        return new ProtoDatatypeLiteral(Byte.toString(value), LiteralCommons.getXSDType(Byte.class));
    }

    /**
     * Creates a new <tt>xsd:short</tt>-typed literal representing the specified
     * value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:short</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(short value) {
        return new ProtoDatatypeLiteral(Short.toString(value), LiteralCommons.getXSDType(Short.class));
    }

    /**
     * Creates a new <tt>xsd:int</tt>-typed literal representing the specified
     * value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:int</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(int value) {
        return new ProtoDatatypeLiteral(Integer.toString(value), LiteralCommons.getXSDType(Integer.class));
    }

    /**
     * Creates a new <tt>xsd:long</tt>-typed literal representing the specified
     * value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:long</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(long value) {
        return new ProtoDatatypeLiteral(Long.toString(value), LiteralCommons.getXSDType(Long.class));
    }

    /**
     * Creates a new <tt>xsd:float</tt>-typed literal representing the specified
     * value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:float</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(float value) {
        return new ProtoDatatypeLiteral(Float.toString(value), LiteralCommons.getXSDType(Float.class));
    }

    /**
     * Creates a new <tt>xsd:double</tt>-typed literal representing the specified
     * value.
     *
     * @param value The value for the literal.
     * @return An <tt>xsd:double</tt>-typed literal for the specified value.
     */
    @Override
    public Literal createLiteral(double value) {
        return new ProtoDatatypeLiteral(Double.toString(value), LiteralCommons.getXSDType(Double.class));
    }

    /**
     * Creates a new literal representing the specified calendar that is typed
     * using the appropriate XML Schema date/time datatype.
     *
     * @param calendar The value for the literal.
     * @return An typed literal for the specified calendar.
     */
    @Override
    public Literal createLiteral(XMLGregorianCalendar calendar) {
        return new ProtoDatatypeLiteral(calendar.toXMLFormat(), LiteralCommons.getXSDType(Date.class));
    }

    /**
     * Creates a new literal representing the specified date that is typed using
     * the appropriate XML Schema date/time datatype.
     *
     * @param date
     * @since 2.7.0
     */
    @Override
    public Literal createLiteral(Date date) {
        return new ProtoDatatypeLiteral(DateUtils.getXMLCalendar(date).toXMLFormat(), LiteralCommons.getXSDType(Date.class));
    }

    /**
     * Creates a new statement with the supplied subject, predicate and object.
     *
     * @param subject   The statement's subject.
     * @param predicate The statement's predicate.
     * @param object    The statement's object.
     * @return The created statement.
     */
    @Override
    public Statement createStatement(Resource subject, URI predicate, Value object) {
        return new ProtoStatement(subject, predicate, object, null);
    }

    /**
     * Creates a new statement with the supplied subject, predicate and object
     * and associated context.
     *
     * @param subject   The statement's subject.
     * @param predicate The statement's predicate.
     * @param object    The statement's object.
     * @param context   The statement's context.
     * @return The created statement.
     */
    @Override
    public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
        return new ProtoStatement(subject, predicate, object, context);
    }
}
