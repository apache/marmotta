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
package org.apache.marmotta.platform.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    
    public static final String XSD_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static Date parse(String xmlDateTime) throws ParseException  {
        if ( xmlDateTime.length() != 25 )  {
            throw new ParseException("Date not in expected xsd:datetime format", 0);
        }
        StringBuilder sb = new StringBuilder(xmlDateTime);
        sb.deleteCharAt(22);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(XSD_DATETIME);
        return simpleDateFormat.parse(sb.toString());
    }
    
    public static String serializeXsdDateTime(Date date) {
        return serializeXsdDateTime(date, TimeZone.getDefault());
    }
    
    public static String serializeXsdDateTime(Date date, TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(XSD_DATETIME);
        simpleDateFormat.setTimeZone(timezone);
        String s =  simpleDateFormat.format(date);
        StringBuilder sb = new StringBuilder(s);
        sb.insert(22, ':');
        return sb.toString();
    }

}
