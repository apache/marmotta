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
package org.apache.marmotta.commons.util;

import org.joda.time.DateTime;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Sebastian Schaffert
 *
 */
public class DateUtils {


    public static final SimpleDateFormat ISO8601FORMAT = createDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "UTC");

    public static final SimpleDateFormat ISO8601FORMAT_TIME = createDateFormat("HH:mm:ss.SSS'Z'", "UTC");
    public static final SimpleDateFormat ISO8601FORMAT_DATE  = createDateFormat("yyyy-MM-dd", "UTC");

    public static final SimpleDateFormat FILENAME_FORMAT     = createDateFormat("yyyyMMdd-HHmmss", null);


    public static final SimpleDateFormat GMTFORMAT = createDateFormat("EEE, dd MMM yyyy HH:mm:ss z", "GMT");


    private static DatatypeFactory datatypeFactory;

    /**
     * Some parsers will have the date as a ISO-8601 string
     *  already, and will set that into the Metadata object.
     * So we can return Date objects for these, this is the
     *  list (in preference order) of the various ISO-8601
     *  variants that we try when processing a date based
     *  property.
     */
    private static final DateFormat[] iso8601InputFormats = new DateFormat[] {

            // yyyy-mm-ddThh...
            ISO8601FORMAT,
            createDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", null),
            createDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", "UTC"),
            createDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", null),    // With timezone
            createDateFormat("yyyy-MM-dd'T'HH:mm:ss", null),     // Without timezone
            // yyyy-mm-dd hh...
            createDateFormat("yyyy-MM-dd' 'HH:mm:ss'Z'", "UTC"), // UTC/Zulu
            createDateFormat("yyyy-MM-dd' 'HH:mm:ssZ", null),    // With timezone
            createDateFormat("yyyy-MM-dd' 'HH:mm:ss.SZ", null),    // With timezone
            createDateFormat("yyyy-MM-dd' 'HH:mm:ss", null),     // Without timezone


            // GMT
            GMTFORMAT,
            createDateFormat("EEE, dd MMM yyyy HH:mm:ss'Z'", "GMT"),

            // Some more date formats
            createDateFormat("EEE MMM dd HH:mm:ss z yyyy", null),     // Word documents
            createDateFormat("EEE MMM d HH:mm:ss z yyyy", null),     // Word documents
            createDateFormat("dd.MM.yyy' 'HH:mm:ss", null),     // German
            createDateFormat("dd.MM.yyy' 'HH:mm", null),     // German

            // SES-711 (see https://openrdf.atlassian.net/browse/SES-711)
            ISO8601FORMAT_DATE,
            createDateFormat("yyyy-MM-ddX", null), // ISO8601 short date with zimezone
    };

    private static SimpleDateFormat createDateFormat(String format, String timezone) {
        SimpleDateFormat sdf =
                new SimpleDateFormat(format, DateFormatSymbols.getInstance(Locale.US));
        if (timezone != null) {
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        return sdf;
    }


    private static DatatypeFactory getDatatypeFactory() throws DatatypeConfigurationException {
        if(datatypeFactory == null) {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        return datatypeFactory;
    }

    /**
     * Parses the given date string. This method is synchronized to prevent
     * concurrent access to the thread-unsafe date formats.
     *
     * Stolen from TIKA to workaround a bug there ...
     *
     * @see <a href="https://issues.apache.org/jira/browse/TIKA-495">TIKA-495</a>
     * @param date date string
     * @return parsed date, or <code>null</code> if the date can't be parsed
     */
    public static synchronized Date parseDate(String date) {
        if(date == null) {
            return null;
        }

        // Java doesn't like timezones in the form ss+hh:mm
        // It only likes the hhmm form, without the colon
        int n = date.length();
        if (n > 2 && date.charAt(n - 3) == ':'
                && (date.charAt(n - 6) == '+' || date.charAt(n - 6) == '-')) {
            date = date.substring(0, n - 3) + date.substring(n - 2);
        }

        // Try several different ISO-8601 variants
        for (DateFormat format : iso8601InputFormats) {
            try {
                return format.parse(date);
            } catch (ParseException ignore) {
            }
        }
        return null;
    }

    /**
     * Transform an XML calendar into a Java date. Useful for working with date literals.
     * @param calendar
     * @return
     */
    public static Date getDate(XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar().getTime();
    }


    /**
     * Transform a Java date into a XML calendar. Useful for working with date literals.
     * @param date
     * @return
     */
    public static XMLGregorianCalendar getXMLCalendar(Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTime(date);
        try {
            return getDatatypeFactory().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }


    /**
     * Transform a Java date into a XML calendar. Useful for working with date literals.
     * @param date
     * @return
     */
    public static XMLGregorianCalendar getXMLCalendar(DateTime date) {
        GregorianCalendar c = date.toGregorianCalendar();
        try {
            return getDatatypeFactory().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }
}
