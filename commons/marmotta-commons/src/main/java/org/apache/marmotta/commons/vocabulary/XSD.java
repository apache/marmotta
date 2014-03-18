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
 * XSD Datatypes
 */
public class XSD {
    public static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema#";
    public static final String PREFIX = "xsd";

    /**
     * {@code http://www.w3.org/2001/XMLSchema#anyURI}.
     * <br>
     * anyURI represents an Internationalized Resource Identifier Reference (IRI). An anyURI value can be absolute or relative and may have an optional fragment identifier (i.e. it may be an IRI Reference). This type should be used when the value fulfills the role of an IRI as defined in RFC 3987 or its successor(s) in the IETF Standards Track.
     */
    public static final URI AnyURI;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#base64Binary}.
     * <br>
     * base64Binary represents arbitrary Base64-encoded binary data. For base64Binary data the entire binary stream is encoded using the Base64 Encoding defined in RFC 3548 which is derived from the encoding described in RFC 2045.
     */
    public static final URI Base64Binary;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#boolean}.
     * <br>
     * boolean represents the values of two-valued logic.
     */
    public static final URI Boolean;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#byte}.
     * <br>
     * byte is derived from short by setting the value of maxInclusive to be 127 and minInclusive to be -128. The base type of byte is short.
     */
    public static final URI Byte;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#date}.
     * <br>
     * date represents top-open intervals of exactly one day in length on the timelines of dateTime beginning on the beginning moment of each day up to but not including the beginning moment of the next day). For non-timezoned values the top-open intervals disjointly cover the non-timezoned timeline one per day. For timezoned values the intervals begin at every minute and therefore overlap.
     */
    public static final URI Date;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#dateTime}.
     * <br>
     * dateTime represents instants of time optionally marked with a particular time zone offset. Values representing the same instant but having different time zone offsets are equal but not identical.
     */
    public static final URI DateTime;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#dateTimeStamp}.
     * <br>
     * The dateTimeStamp datatype is derived from dateTime by giving the value required to its explicitTimezone facet.
     */
    public static final URI DateTimeStamp;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#dayTimeDuration}.
     * <br>
     * dayTimeDuration is a datatype derived from duration by restricting its lexical representations to instances of dayTimeDurationLexicalRep.
     */
    public static final URI DayTimeDuration;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#decimal}.
     * <br>
     * decimal represents a subset of the real numbers which can be represented by decimal numerals. The value space of decimal is the set of numbers that can be obtained by dividing an integer by a non-negative power of ten i.e. expressible as i10n where i and n are integers and n0. Precision is not reflected in this value space; the number 2.0 is not distinct from the number 2.00. The order relation on decimal is the order relation on real numbers restricted to this subset.
     */
    public static final URI Decimal;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#double}.
     * <br>
     * The double datatype is patterned after the IEEE double-precision 64-bit floating point datatype IEEE 754-2008.
     */
    public static final URI Double;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#duration}.
     * <br>
     * duration is a datatype that represents durations of time.
     */
    public static final URI Duration;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#float}.
     * <br>
     * The float datatype is patterned after the IEEE single-precision 32-bit floating point datatype IEEE 754-2008.
     */
    public static final URI Float;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#gDay}.
     * <br>
     * gDay represents whole days within an arbitrary monthdays that recur at the same point in each (Gregorian) month.
     */
    public static final URI GDay;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#gMonth}.
     * <br>
     * gMonth represents whole (Gregorian) months within an arbitrary yearmonths that recur at the same point in each year. It might be used for example to say what month annual Thanksgiving celebrations fall in different countries (--11 in the United States --10 in Canada and possibly other months in other countries).
     */
    public static final URI GMonth;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#gMonthDay}.
     * <br>
     * gMonthDay represents whole calendar days that recur at the same point in each calendar year or that occur in some arbitrary calendar year. (Obviously days beyond 28 cannot occur in all Februaries; 29 is nonetheless permitted.)
     */
    public static final URI GMonthDay;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#gYear}.
     * <br>
     * gYear represents Gregorian calendar years.
     */
    public static final URI GYear;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#gYearMonth}.
     * <br>
     * gYearMonth represents specific whole Gregorian months in specific Gregorian years.
     */
    public static final URI GYearMonth;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#hexBinary}.
     * <br>
     * hexBinary represents arbitrary hex-encoded binary data.
     */
    public static final URI HexBinary;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#int}.
     * <br>
     * int is derived from long by setting the value of maxInclusive to be 2147483647 and minInclusive to be -2147483648. The base type of int is long.
     */
    public static final URI Int;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#integer}.
     * <br>
     * integer is derived from decimal by fixing the value of fractionDigits to be 0 and disallowing the trailing decimal point. This results in the standard mathematical concept of the integer numbers. The value space of integer is the infinite set ...-2-1012.... The base type of integer is decimal.
     */
    public static final URI Integer;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#language}.
     * <br>
     * language represents formal natural language identifiers as defined by BCP 47 (currently represented by RFC 4646 and RFC 4647) or its successor(s).
     */
    public static final URI Language;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#long}.
     * <br>
     * long is derived from integer by setting the value of maxInclusive to be 9223372036854775807 and minInclusive to be -9223372036854775808. The base type of long is integer.
     */
    public static final URI Long;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#negativeInteger}.
     * <br>
     * negativeInteger is derived from nonPositiveInteger by setting the value of maxInclusive to be -1. This results in the standard mathematical concept of the negative integers. The value space of negativeInteger is the infinite set ...-2-1. The base type of negativeInteger is nonPositiveInteger.
     */
    public static final URI NegativeInteger;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#nonNegativeInteger}.
     * <br>
     * nonNegativeInteger is derived from integer by setting the value of minInclusive to be 0. This results in the standard mathematical concept of the non-negative integers. The value space of nonNegativeInteger is the infinite set 012.... The base type of nonNegativeInteger is integer.
     */
    public static final URI NonNegativeInteger;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#nonPositiveInteger}.
     * <br>
     * nonPositiveInteger is derived from integer by setting the value of maxInclusive to be 0. This results in the standard mathematical concept of the non-positive integers. The value space of nonPositiveInteger is the infinite set ...-2-10. The base type of nonPositiveInteger is integer.
     */
    public static final URI NonPositiveInteger;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#normalizedString}.
     * <br>
     * normalizedString represents white space normalized strings. The value space of normalizedString is the set of strings that do not contain the carriage return (#xD) line feed (#xA) nor tab (#x9) characters. The lexical space of normalizedString is the set of strings that do not contain the carriage return (#xD) line feed (#xA) nor tab (#x9) characters. The base type of normalizedString is string.
     */
    public static final URI NormalizedString;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#positiveInteger}.
     * <br>
     * positiveInteger is derived from nonNegativeInteger by setting the value of minInclusive to be 1. This results in the standard mathematical concept of the positive integer numbers. The value space of positiveInteger is the infinite set 12.... The base type of positiveInteger is nonNegativeInteger.
     */
    public static final URI PositiveInteger;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#short}.
     * <br>
     * short is derived from int by setting the value of maxInclusive to be 32767 and minInclusive to be -32768. The base type of short is int.
     */
    public static final URI Short;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#string}.
     * <br>
     * The string datatype represents character strings in XML.
     */
    public static final URI String;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#time}.
     * <br>
     * time represents instants of time that recur at the same point in each calendar day or that occur in some arbitrary calendar day.
     */
    public static final URI Time;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#token}.
     * <br>
     * token represents tokenized strings. The value space of token is the set of strings that do not contain the carriage return (#xD) line feed (#xA) nor tab (#x9) characters that have no leading or trailing spaces (#x20) and that have no internal sequences of two or more spaces. The lexical space of token is the set of strings that do not contain the carriage return (#xD) line feed (#xA) nor tab (#x9) characters that have no leading or trailing spaces (#x20) and that have no internal sequences of two or more spaces. The base type of token is normalizedString.
     */
    public static final URI Token;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#unsignedByte}.
     * <br>
     * unsignedByte is derived from unsignedShort by setting the value of maxInclusive to be 255. The base type of unsignedByte is unsignedShort.
     */
    public static final URI UnsignedByte;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#unsignedInt}.
     * <br>
     * unsignedInt is derived from unsignedLong by setting the value of maxInclusive to be 4294967295. The base type of unsignedInt is unsignedLong.
     */
    public static final URI UnsignedInt;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#unsignedLong}.
     * <br>
     * unsignedLong is derived from nonNegativeInteger by setting the value of maxInclusive to be 18446744073709551615. The base type of unsignedLong is nonNegativeInteger.
     */
    public static final URI UnsignedLong;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#unsignedShort}.
     * <br>
     * unsignedShort is derived from unsignedInt by setting the value of maxInclusive to be 65535. The base type of unsignedShort is unsignedInt.
     */
    public static final URI UnsignedShort;

    /**
     * {@code http://www.w3.org/2001/XMLSchema#yearMonthDuration}.
     * <br>
     * yearMonthDuration is a datatype derived from duration by restricting its lexical representations to instances of yearMonthDurationLexicalRep.
     */
    public static final URI YearMonthDuration;


    static {
        final ValueFactory vf = ValueFactoryImpl.getInstance();

        AnyURI = vf.createURI(NAMESPACE, "anyURI");
        Base64Binary = vf.createURI(NAMESPACE, "base64Binary");
        Boolean = vf.createURI(NAMESPACE, "boolean");
        Byte = vf.createURI(NAMESPACE, "byte");
        Date = vf.createURI(NAMESPACE, "date");
        DateTime = vf.createURI(NAMESPACE, "dateTime");
        DateTimeStamp = vf.createURI(NAMESPACE, "dateTimeStamp");
        DayTimeDuration = vf.createURI(NAMESPACE, "dayTimeDuration");
        Decimal = vf.createURI(NAMESPACE, "decimal");
        Double = vf.createURI(NAMESPACE, "double");
        Duration = vf.createURI(NAMESPACE, "duration");
        Float = vf.createURI(NAMESPACE, "float");
        GDay = vf.createURI(NAMESPACE, "gDay");
        GMonth = vf.createURI(NAMESPACE, "gMonth");
        GMonthDay = vf.createURI(NAMESPACE, "gMonthDay");
        GYear = vf.createURI(NAMESPACE, "gYear");
        GYearMonth = vf.createURI(NAMESPACE, "gYearMonth");
        HexBinary = vf.createURI(NAMESPACE, "hexBinary");
        Int = vf.createURI(NAMESPACE, "int");
        Integer = vf.createURI(NAMESPACE, "integer");
        Language = vf.createURI(NAMESPACE, "language");
        Long = vf.createURI(NAMESPACE, "long");
        NegativeInteger = vf.createURI(NAMESPACE, "negativeInteger");
        NonNegativeInteger = vf.createURI(NAMESPACE, "nonNegativeInteger");
        NonPositiveInteger = vf.createURI(NAMESPACE, "nonPositiveInteger");
        NormalizedString = vf.createURI(NAMESPACE, "normalizedString");
        PositiveInteger = vf.createURI(NAMESPACE, "positiveInteger");
        Short = vf.createURI(NAMESPACE, "short");
        String = vf.createURI(NAMESPACE, "string");
        Time = vf.createURI(NAMESPACE, "time");
        Token = vf.createURI(NAMESPACE, "token");
        UnsignedByte = vf.createURI(NAMESPACE, "unsignedByte");
        UnsignedInt = vf.createURI(NAMESPACE, "unsignedInt");
        UnsignedLong = vf.createURI(NAMESPACE, "unsignedLong");
        UnsignedShort = vf.createURI(NAMESPACE, "unsignedShort");
        YearMonthDuration = vf.createURI(NAMESPACE, "yearMonthDuration");
    }

}
