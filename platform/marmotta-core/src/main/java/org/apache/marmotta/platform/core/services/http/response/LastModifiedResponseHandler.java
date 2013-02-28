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
package org.apache.marmotta.platform.core.services.http.response;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LastModifiedResponseHandler implements ResponseHandler<Date> {
    // Sun, 06 Nov 1994 08:49:37 GMT ; RFC 822, updated by RFC 1123
    private final SimpleDateFormat RFC_1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    // Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
    private final SimpleDateFormat RFC_1036 = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss z");
    // Sun Nov 6 08:49:37 1994 ; ANSI C's asctime() format
    private final SimpleDateFormat ANSI_C   = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");

    @Override
    public Date handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        try {
            Header lastModH = response.getFirstHeader("Last-Modified");
            return lastModH != null ? parseDate(lastModH.getValue()) : null;
        } finally {
            EntityUtils.consume(response.getEntity());
        }
    }

    private Date parseDate(String value) {
        try {
            return RFC_1123.parse(value);
        } catch (ParseException e) {
            try {
                return RFC_1036.parse(value);
            } catch (ParseException e1) {
                try {
                    return ANSI_C.parse(value);
                } catch (ParseException e2) {
                    return null;
                }
            }
        }
    }

}