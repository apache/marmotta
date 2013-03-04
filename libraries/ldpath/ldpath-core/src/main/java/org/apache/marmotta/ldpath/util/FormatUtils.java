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
package org.apache.marmotta.ldpath.util;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class FormatUtils {

    public static final SimpleDateFormat ISO8601FORMAT  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static final SimpleDateFormat ISO8601FORMAT_TIME  = new SimpleDateFormat("HH:mm:ss.SSSZ");
    public static final SimpleDateFormat ISO8601FORMAT_DATE  = new SimpleDateFormat("yyyy-MM-dd");


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
         createDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "UTF"), // UTC/Zulu
         createDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", null),    // With timezone
         createDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", null),     // Without timezone
         createDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", "UTF"),     // UTC/Zulu
         createDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", null),        // With timezone
         createDateFormat("yyyy-MM-dd'T'HH:mm:ss", null),         // Without timezone
         // yyyy-mm-dd hh...
         createDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS'Z'", "UTF"), // UTC/Zulu
         createDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ", null),    // With timezone
         createDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS", null),     // Without timezone
         createDateFormat("yyyy-MM-dd' 'HH:mm:ss'Z'", "UTF"),     // UTC/Zulu
         createDateFormat("yyyy-MM-dd' 'HH:mm:ssZ", null),        // With timezone
         createDateFormat("yyyy-MM-dd' 'HH:mm:ss", null),         // Without timezone
         createDateFormat("EEE MMM dd HH:mm:ss z yyyy", null),    // Word documents/java Date.toString()
         createDateFormat("EEE MMM d HH:mm:ss z yyyy", null),     // Word documents/java Date.toString()
         createDateFormat("dd.MM.yyyy' 'HH:mm:ss", null),         // German with seconds
         createDateFormat("dd.MM.yyyy' 'HH:mm", null),            // German without seconds
     };

     private static DateFormat createDateFormat(String format, String timezone) {
         SimpleDateFormat sdf =
             new SimpleDateFormat(format, new DateFormatSymbols(Locale.US));
         if (timezone != null) {
             sdf.setTimeZone(TimeZone.getTimeZone(timezone));
         }
         return sdf;
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
        if (date.charAt(n - 3) == ':'
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



}
