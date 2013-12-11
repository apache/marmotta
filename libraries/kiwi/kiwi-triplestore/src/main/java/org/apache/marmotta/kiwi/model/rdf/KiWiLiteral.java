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

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;

/**
 * KiWiLiterals store literal information from the knowledge base. They directly
 * correspond to an RDF literal stored in Sesame. KiWiLiterals are
 * parametrized with the Java type corresponding to the literal content they store.
 * The method getType() returns the KiWiResource representing the RDF type of this
 * literal. This information can e.g. be used to provide appropriate user interfaces.
 *
 * A factory class should be provided that maps between RDF types and Java classes.
 *
 * @author Sebastian Schaffert
 */
public abstract class KiWiLiteral extends KiWiNode implements Literal {

    /**
     *
     */
    private static final long serialVersionUID = 1772323725671607249L;


    private Locale locale;

    private KiWiUriResource type;

    public KiWiLiteral() {
        super();
    }

    protected KiWiLiteral(Date created) {
        super(created);
    }


    protected KiWiLiteral(Locale locale, KiWiUriResource type) {
        this();
        this.locale = locale;
        this.type = type;
    }

    protected KiWiLiteral(Locale locale, KiWiUriResource type, Date created) {
        super(created);
        this.locale = locale;
        this.type = type;
    }


    /**
     * Return the content of the literal, using the parametrized Java type
     * @return
     */
    public abstract String getContent();


    /**
     * Set the content of the literal to the content provided as parameter.
     * @param content
     */
    public abstract void setContent(String content);

    /**
     * Get the locale representing the language this literal is in; returns null
     * if no language is associated with the literal
     * @return
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Set the language of this literal to the given locale.
     * @param language
     */
    public void setLocale(Locale language) {
        this.locale = language;
    }

    /**
     * Return the RDF/XSD type of this literal.
     * @return
     */
    public KiWiUriResource getType() {
        return type;
    }

    /**
     * Set the RDF/XSD type of this literal.
     * @param type
     */
    public void setType(KiWiUriResource type) {
        this.type = type;
    }




    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\"").append(getContent()).append("\"");
        if(locale != null) {
            result.append("@").append(locale.getLanguage());
        }
        if (type != null) {
            result.append("^^");
            if (type.stringValue().startsWith(Namespaces.NS_XSD)) {
                result.append(getType().stringValue().replace(Namespaces.NS_XSD, "xsd:"));
            } else if (type.stringValue().startsWith(Namespaces.NS_RDF)) {
                result.append(getType().stringValue().replace(Namespaces.NS_RDF, "rdf:"));
            } else {
                result.append(getType());
            }
        }
        return result.toString();
    }

    /**
     * A separate equalsContent method for checking whether literals are equal in their content; we cannot override
     * the .equals function with this because it would break the system in cases where the same content is used in
     * different literals.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if(o instanceof Literal) {
            Literal that = (Literal)o;

            if(!this.getLabel().equals(that.getLabel())) return false;

            if(this.getLanguage() != null && !(this.getLanguage().equals(that.getLanguage()))) return false;

            // getDatatype should never be null, this is only for legacy support
            if(this.getDatatype()==null && that.getDatatype()!=null) return false;

            if(this.getDatatype() != null && !this.getDatatype().equals(that.getDatatype())) return false;

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        // not compatible with Sesame:
        /*
        int result =  this.getClass().hashCode();
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + this.getLabel().hashCode();
        return result;
        */
        return getLabel().hashCode();
    }


    @Override
    public boolean isAnonymousResource() {
        return false;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }

    @Override
    public boolean isUriResource() {
        return false;
    }

    /**
     * Returns the <tt>boolean</tt> value of this literal.
     *
     * @return The <tt>long</tt> value of the literal.
     * @throws IllegalArgumentException If the literal's label cannot be represented by a <tt>boolean</tt>.
     */
    @Override
    public boolean booleanValue() {
        return Boolean.parseBoolean(getLabel());
    }

    /**
     * Gets the label of this literal.
     *
     * @return The literal's label.
     */
    @Override
    public String getLabel() {
        return getContent();
    }

    /**
     * Gets the language tag for this literal, normalized to lower case.
     *
     * @return The language tag for this literal, or <tt>null</tt> if it
     *         doesn't have one.
     */
    @Override
    public String getLanguage() {
        if(getLocale() != null) {
            return getLocale().getLanguage().toLowerCase();
        } else {
            return null;
        }
    }

    /**
     * Gets the datatype for this literal.
     *
     * @return The datatype for this literal, or <tt>null</tt> if it doesn't
     *         have one.
     */
    @Override
    public URI getDatatype() {
        return type;
    }

    /**
     * Returns the <tt>byte</tt> value of this literal.
     *
     * @return The <tt>byte value of the literal.
     * @throws NumberFormatException If the literal cannot be represented by a <tt>byte</tt>.
     */
    @Override
    public byte byteValue() {
        return Byte.parseByte(getLabel());
    }

    /**
     * Returns the <tt>short</tt> value of this literal.
     *
     * @return The <tt>short</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by a <tt>short</tt>.
     */
    @Override
    public short shortValue() {
        return Short.parseShort(getLabel());
    }

    /**
     * Returns the <tt>int</tt> value of this literal.
     *
     * @return The <tt>int</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by a <tt>int</tt>.
     */
    @Override
    public int intValue() {
        return Integer.parseInt(getLabel());
    }

    /**
     * Returns the <tt>long</tt> value of this literal.
     *
     * @return The <tt>long</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by to a <tt>long</tt>.
     */
    @Override
    public long longValue() {
        return Long.parseLong(getLabel());
    }

    /**
     * Returns the integer value of this literal.
     *
     * @return The integer value of the literal.
     * @throws NumberFormatException If the literal's label is not a valid integer.
     */
    @Override
    public BigInteger integerValue() {
        return new BigInteger(getLabel());
    }

    /**
     * Returns the decimal value of this literal.
     *
     * @return The decimal value of the literal.
     * @throws NumberFormatException If the literal's label is not a valid decimal.
     */
    @Override
    public BigDecimal decimalValue() {
        return new BigDecimal(getLabel());
    }

    /**
     * Returns the <tt>float</tt> value of this literal.
     *
     * @return The <tt>float</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by a <tt>float</tt>.
     */
    @Override
    public float floatValue() {
        return Float.parseFloat(getLabel());
    }

    /**
     * Returns the <tt>double</tt> value of this literal.
     *
     * @return The <tt>double</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by a <tt>double</tt>.
     */
    @Override
    public double doubleValue() {
        return Double.parseDouble(getLabel());
    }

    /**
     * Returns the {@link javax.xml.datatype.XMLGregorianCalendar} value of this literal. A calendar
     * representation can be given for literals whose label conforms to the
     * syntax of the following <a href="http://www.w3.org/TR/xmlschema-2/">XML
     * Schema datatypes</a>: <tt>dateTime</tt>, <tt>time</tt>,
     * <tt>date</tt>, <tt>gYearMonth</tt>, <tt>gMonthDay</tt>,
     * <tt>gYear</tt>, <tt>gMonth</tt> or <tt>gDay</tt>.
     *
     * @return The calendar value of the literal.
     * @throws IllegalArgumentException If the literal cannot be represented by a
     *                                  {@link javax.xml.datatype.XMLGregorianCalendar}.
     */
    @Override
    public XMLGregorianCalendar calendarValue() {
        return XMLDatatypeUtil.parseCalendar(getLabel());
    }

    /**
     * Returns the String-value of a <tt>Value</tt> object. This returns either
     * a {@link org.openrdf.model.Literal}'s label, a {@link org.openrdf.model.URI}'s URI or a {@link org.openrdf.model.BNode}'s ID.
     */
    @Override
    public String stringValue() {
        return getLabel();
    }
}
