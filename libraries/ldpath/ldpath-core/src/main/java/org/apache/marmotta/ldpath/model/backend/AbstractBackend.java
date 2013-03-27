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
package org.apache.marmotta.ldpath.model.backend;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.util.FormatUtils;



/**
 * This provides an generic implementation of all <code>**Value(Node node)</code> 
 * methods other than {@link #stringValue(Object)}.<p>
 * This allows to implement {@link RDFBackend}s without dealing with the actual
 * conversions needed for types literals not directly supported by a backend.<p>
 * Backends however should implement - override default implementations provided
 * by this class - <code>**Value(Node node)</code> methods with natively supported
 * types to avoid unnecessary type conversions.
 * <p>
 * An Example for a RDFRepository&lt;Object&gt; that directly uses the the
 * Java types for typed literals as nodes.
 * <code><pre>
 *     public Double doubleValue(Object node) {
 *         //assume that this is typically only called on Double values
 *         try { 
 *             return (Double)node;
 *         } catch (ClassCastException e){
 *             //not a Double - call super to trigger parsing from
 *             //the lexical form.
 *             return super.doubleValue(node);
 *         }
 * </pre></code>
 * Here an other possible implementation that does not assume that the node is
 * of the requested type.
 * <code><pre>
 *     public Double doubleValue(Object node) {
 *         if(node instanceof Double){
 *             return (Double)node;
 *         } else {
 *             //not a Double - call super to trigger parsing from
 *             //the lexical form.
 *             return super.doubleValue(node);
 *         }
 * </pre></code>
 * It will depend on the use cases what of the two implementations performs 
 * better.
 * @see org.apache.marmotta.ldpath.api.backend.NodeBackend#stringValue(java.lang.Object)
 */
public abstract class AbstractBackend<Node> implements NodeBackend<Node> {

    /**
     * A clone of the DateFormat provided by {@link FormatUtils} to parse xsd:date 
     * values. This is necessary because {@link SimpleDateFormat} is not thread 
     * save and therefore we do not directly use a public static member.
     */
    private SimpleDateFormat dateFormat = (SimpleDateFormat)FormatUtils.ISO8601FORMAT_DATE.clone();
    
    /**
     * A clone of the DateFormat provided by {@link FormatUtils} to parse xsd:time 
     * values. This is necessary because {@link SimpleDateFormat} is not thread 
     * save and therefore we do not directly use a public static member.
     */
    private SimpleDateFormat timeFormat = (SimpleDateFormat)FormatUtils.ISO8601FORMAT_TIME.clone();
    /**
     * Parses the Double value of the parsed node based on its lexical form as
     * returned by {@link #stringValue(Object)}.
     * @return the double value
     * @throws NumberFormatException if the lexical form can not be converted
     * to a Double.
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#doubleValue(java.lang.Object)
     */
    @Override
    public Double doubleValue(Node node) {
        return new Double(trimPlusSign(stringValue(node)));
    }
    /**
     * Parses the Float value of the parsed node based on its lexical form as
     * returned by {@link #stringValue(Object)}.
     * @return the float value
     * @throws NumberFormatException if the lexical form can not be converted
     * to a Double.
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#floatValue(java.lang.Object)
     */
    public Float floatValue(Node node) {
        return new Float(trimPlusSign(stringValue(node)));
    };

    /**
     * Parses the {@link BigDecimal#longValueExact() Long value} of the parsed 
     * node by using {@link #decimalValue(Object)}. This has the advantage, that
     * decimal values that are also valid long values - such as '1.0' are also
     * correctly converted to long.<p>
     * @return the long value
     * @throws NumberFormatException if the lexical form can not be converted
     * to an Long.
     * @throws ArithmeticException if the lexical form can not be converted
     * to a Long.
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#longValue(java.lang.Object)
     */
    @Override
    public Long longValue(Node node) {
        return decimalValue(node).longValueExact();
    }

    /**
     * Parses the {@link BigDecimal#intValueExact() Integer value} of the parsed 
     * node by using {@link #decimalValue(Object)}. This has the advantage, that
     * decimal values that are also valid integer values - such as '1.0' are also
     * correctly converted to {@link Integer}.<p>
     * @return the int value
     * @throws NumberFormatException if the lexical form can not be converted
     * to an Long.
     * @throws ArithmeticException if the lexical form can not be converted
     * to a Long.
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#intValue(java.lang.Object)
     */
    @Override
    public Integer intValue(Node node) {
        return decimalValue(node).intValueExact();
    };
    /**
     * Parses the {@link BigDecimal} value from the lexical form of the parsed
     * node as returned by {@link RDFBackend#stringValue(Object)}. This
     * trims loading '+' sings.
     * @return the int value
     * @throws NumberFormatException if the lexical form can not be converted
     * to an Long.
     * @throws ArithmeticException if the lexical form can not be converted
     * to a Long.
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#decimalValue(java.lang.Object)
     */
    public BigDecimal decimalValue(Node node) {
        return new BigDecimal(trimPlusSign(stringValue(node)));
    };
    /**
     * Parses the {@link BigDecimal#toBigIntegerExact() BugIneger value} of the parsed 
     * node by using {@link #decimalValue(Object)}. This has the advantage, that
     * decimal values that are also valid integer values - such as '1.0' are also
     * correctly converted to {@link BigInteger}.<p>
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#integerValue(java.lang.Object)
     */
    public java.math.BigInteger integerValue(Node node) {
        return decimalValue(node).toBigIntegerExact();
    };
    /**
     * Parses the boolean value from the {@link #stringValue(Object) lexical form}.
     * Supports both '1' and {@link Boolean#parseBoolean(String)}.
     * @return the boolean value
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#booleanValue(java.lang.Object)
     */
    public Boolean booleanValue(Node node) {
        String lexicalForm = stringValue(node);
        if(lexicalForm.length() == 1){ //support '1' as true
            return lexicalForm.charAt(0) == '1'; 
        } else {
            return Boolean.parseBoolean(lexicalForm);
        }
    };
    /**
     * Parses date vales based on the ISO8601 specification by using the
     * {@link #stringValue(Object) lexical form} of the parsed node.
     * @return the {@link Date} representing the parsed date.
     * @throws IllegalArgumentException on any {@link ParseException} while 
     * parsing the {@link #stringValue(Object) lexical form} of the parsed
     * node.
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#dateValue(java.lang.Object)
     */
    public Date dateValue(Node node) {
        String lexicalForm = stringValue(node);
        try {
            return parseDate(lexicalForm);
        } catch (ParseException e) {
            throw new IllegalArgumentException("could not parse ISO8601 date from '"+
                lexicalForm+"'!",e);
        }
    };
    
    /**
     * Parses time value based on the ISO8601 specification by using the
     * {@link #stringValue(Object) lexical form} of the parsed node.
     * @return the {@link Date} representing the parsed date.
     * @throws IllegalArgumentException on any {@link ParseException} while 
     * parsing the {@link #stringValue(Object) lexical form} of the parsed
     * node.
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#timeValue(java.lang.Object)
     */
    public Date timeValue(Node node) {
        String lexicalForm = stringValue(node);
        try {
            return parseTime(lexicalForm);
        } catch (ParseException e) {
            throw new IllegalArgumentException("could not parse ISO8601 time from '"+
                lexicalForm+"'!",e);
        }
    };
    
    /**
     * Parses dateTime value based on the {@link #stringValue(Object) lexical form} 
     * of the parsed node. For details about parsing see
     * {@link FormatUtils#parseDate(String)}.
     * @return the {@link Date} representing the parsed date.
     * @throws IllegalArgumentException if the parsed node can not be converted
     * to an {@link Date}. 
     * @see org.apache.marmotta.ldpath.api.backend.RDFBackend#dateTimeValue(java.lang.Object)
     */
    public Date dateTimeValue(Node node) {
        String lexicalForm = stringValue(node);
        Date date = FormatUtils.parseDate(lexicalForm);
        if(date == null){
            throw new IllegalArgumentException("could not parse xsd:dateTime from '"+
                lexicalForm+"'!");
        } else {
            return date;
        }
    };
    
    @Override
    public abstract String stringValue(Node node);
    
    /**
     * Removes the first character from the supplied string if this is a plus
     * sign ('+'). Number strings with leading plus signs cannot be parsed by
     * methods such as {@link Integer#parseInt(String)}.<p>
     * Taken from the Sesame XMLDatatypeUtil.
     */
    private static String trimPlusSign(String s) {
        return (s.length() > 0 && s.charAt(0) == '+') ? s.substring(1) : s;
    }
    /**
     * Utility to parse xsd:date strings
     * @param dateString
     * @return
     * @throws ParseException
     */
    protected Date parseDate(String dateString) throws ParseException {
        synchronized (dateFormat) {
            return dateFormat.parse(dateString);
        }
    }
    /**
     * Utility to parse xsd:time strings
     * @param timeString
     * @return
     * @throws ParseException
     */
    protected Date parseTime(String timeString) throws ParseException {
        synchronized (timeFormat) {
            return timeFormat.parse(timeString);
        }
    }
    
}
