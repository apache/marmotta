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

package org.apache.marmotta.ostrich.model;

import org.apache.marmotta.commons.util.DateUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.datatypes.XMLDatatypeUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Base functionality for both types of literals (type conversions, equals, etc).
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class ProtoLiteralBase implements Literal {


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
     * Returns the {@link XMLGregorianCalendar} value of this literal. A calendar
     * representation can be given for literals whose label conforms to the
     * syntax of the following <a href="http://www.w3.org/TR/xmlschema-2/">XML
     * Schema datatypes</a>: <tt>dateTime</tt>, <tt>time</tt>,
     * <tt>date</tt>, <tt>gYearMonth</tt>, <tt>gMonthDay</tt>,
     * <tt>gYear</tt>, <tt>gMonth</tt> or <tt>gDay</tt>.
     *
     * @return The calendar value of the literal.
     * @throws IllegalArgumentException If the literal cannot be represented by a
     *                                  {@link XMLGregorianCalendar}.
     */
    @Override
    public XMLGregorianCalendar calendarValue() {
        try {
            return XMLDatatypeUtil.parseCalendar(getLabel());
        } catch(IllegalArgumentException ex) {
            // try harder to parse the label, sometimes they have stupid formats ...
            Date cv = DateUtils.parseDate(getLabel());
            return DateUtils.getXMLCalendar(cv);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if(o instanceof Literal) {
            Literal that = (Literal)o;

            if(!this.getLabel().equals(that.getLabel())) return false;

            if(this.getLanguage() != null && !(this.getLanguage().equals(that.getLanguage()))) return false;

            if(this.getDatatype()==null && that.getDatatype()!=null) return false;

            if(this.getDatatype() != null && !this.getDatatype().equals(that.getDatatype())) return false;

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getLabel().hashCode();
    }

}
