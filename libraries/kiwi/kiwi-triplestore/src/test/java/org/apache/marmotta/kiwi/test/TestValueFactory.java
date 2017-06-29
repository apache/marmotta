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

package org.apache.marmotta.kiwi.test;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.Locale;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.marmotta.commons.sesame.model.LiteralCommons;
import org.apache.marmotta.kiwi.generator.SnowflakeIDGenerator;
import org.apache.marmotta.kiwi.model.rdf.KiWiAnonResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiBooleanLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDateLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDoubleLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiIntLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.joda.time.DateTime;
import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.AbstractValueFactory;

/**
 * A value factory creating KiWi values without requiring a real repository
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
//public class TestValueFactory implements ValueFactory {
public class TestValueFactory extends AbstractValueFactory {

    private SnowflakeIDGenerator idGenerator;

    public TestValueFactory() {
        idGenerator = new SnowflakeIDGenerator(1);
    }

    /**
     * Creates a new IRI from the supplied string-representation.
     *
     * @param iri A string-representation of a IRI.
     * @return An object representing the IRI.
     * @throws IllegalArgumentException If the supplied string does not resolve to a legal (absolute) IRI.
     */
    @Override
    public IRI createIRI(String iri) {
        KiWiUriResource r = new KiWiUriResource(iri, new Date());
        r.setId(idGenerator.getId());

        return r;
    }

    /**
     * Creates a new URI from the supplied namespace and local name. Calling this
     * method is funtionally equivalent to calling {@link #createURI(String)
     * createURI(namespace+localName)}, but allows the ValueFactory to reuse
     * supplied namespace and local name strings whenever possible. Note that the
     * values returned by {@link org.openrdf.model.URI#getNamespace()} and
     * {@link org.openrdf.model.URI#getLocalName()} are not necessarily the same as the values that
     * are supplied to this method.
     *
     * @param namespace The IRI's namespace.
     * @param localName The IRI's local name.
     * @throws IllegalArgumentException If the supplied namespace and localname do not resolve to a legal
     *                                  (absolute) IRI.
     */
    @Override
    public IRI createIRI(String namespace, String localName) {
        return createIRI(namespace+localName);
    }

    /**
     * Creates a new bNode.
     *
     * @return An object representing the bNode.
     */
    @Override
    public BNode createBNode() {
        return createBNode(Long.toHexString(idGenerator.getId()));
    }

    /**
     * Creates a new blank node with the given node identifier.
     *
     * @param nodeID The blank node identifier.
     * @return An object representing the blank node.
     */
    @Override
    public BNode createBNode(String nodeID) {
        KiWiAnonResource r = new KiWiAnonResource(nodeID, new Date());
        r.setId(idGenerator.getId());

        return r;
    }

    /**
     * Creates a new literal with the supplied label.
     *
     * @param label The literal's label.
     */
    @Override
    public Literal createLiteral(String label) {
        KiWiLiteral l = new KiWiStringLiteral(label, new Date());
        l.setId(idGenerator.getId());

        return l;
    }

    /**
     * Creates a new literal with the supplied label and language attribute.
     *
     * @param label    The literal's label.
     * @param language The literal's language attribute, or <tt>null</tt> if the literal
     */
    @Override
    public Literal createLiteral(String label, String language) {
        KiWiLiteral l = new KiWiStringLiteral(label, Locale.forLanguageTag(language), null, new Date());
        l.setId(idGenerator.getId());

        return l;
    }

    /**
     * Creates a new literal with the supplied label and datatype.
     *
     * @param label    The literal's label.
     * @param datatype The literal's datatype, or <tt>null</tt> if the literal doesn't
     */
    @Override
    public Literal createLiteral(String label, IRI datatype) {
        KiWiUriResource t;
        if(datatype instanceof KiWiUriResource) {
            t = (KiWiUriResource) datatype;
        } else {
            t = (KiWiUriResource) createURI(datatype.stringValue());
        }

        KiWiLiteral l = new KiWiStringLiteral(label, null, t, new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiBooleanLiteral(value, (KiWiUriResource) createURI(LiteralCommons.getXSDType(Boolean.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiIntLiteral(Long.valueOf(value), (KiWiUriResource) createURI(LiteralCommons.getXSDType(Byte.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiIntLiteral(Long.valueOf(value), (KiWiUriResource) createURI(LiteralCommons.getXSDType(Short.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiIntLiteral(Long.valueOf(value), (KiWiUriResource) createURI(LiteralCommons.getXSDType(Integer.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiIntLiteral(Long.valueOf(value), (KiWiUriResource) createURI(LiteralCommons.getXSDType(Long.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiDoubleLiteral(Double.valueOf(value), (KiWiUriResource) createURI(LiteralCommons.getXSDType(Float.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiDoubleLiteral(Double.valueOf(value), (KiWiUriResource) createURI(LiteralCommons.getXSDType(Double.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiDateLiteral(new DateTime(calendar.toGregorianCalendar()), (KiWiUriResource) createURI(LiteralCommons.getXSDType(Date.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
        KiWiLiteral l = new KiWiDateLiteral(new DateTime(date), (KiWiUriResource) createURI(LiteralCommons.getXSDType(Date.class)), new Date());
        l.setId(idGenerator.getId());

        return l;
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
    public Statement createStatement(Resource subject, IRI predicate, Value object) {
        return createStatement(subject, predicate, object, null);
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
    public Statement createStatement(Resource subject, IRI predicate, Value object, Resource context) {
        Preconditions.checkArgument(subject instanceof KiWiNode);
        Preconditions.checkArgument(predicate instanceof KiWiNode);
        Preconditions.checkArgument(object instanceof KiWiNode);
        Preconditions.checkArgument(context == null || context instanceof KiWiNode);

        KiWiTriple t = new KiWiTriple((KiWiResource)subject,(KiWiUriResource)predicate,(KiWiNode)object,(KiWiResource)context, new Date());
        t.setId(idGenerator.getId());

        return t;
    }
}
