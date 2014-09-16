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

package org.apache.marmotta.kiwi.sparql.function;

import org.apache.marmotta.kiwi.vocabulary.FN_MARMOTTA;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.FN;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class FunctionUtil {

    /**
     * Map function call (internal or proper URI) to the proper SPARQL function URI
     * @param uri
     * @return
     */
    public static URI getFunctionUri(String uri) {
        if(uri.equalsIgnoreCase("RAND")) {
            return FN_MARMOTTA.RAND;
        } else if(uri.equalsIgnoreCase("UUID")) {
            return FN_MARMOTTA.UUID;
        } else if(uri.equalsIgnoreCase("STRUUID")) {
            return FN_MARMOTTA.STRUUID;
        } else if(uri.equalsIgnoreCase("MD5")) {
            return FN_MARMOTTA.MD5;
        } else if(uri.equalsIgnoreCase("SHA1")) {
            return FN_MARMOTTA.SHA1;
        } else if(uri.equalsIgnoreCase("SHA256")) {
            return FN_MARMOTTA.SHA256;
        } else if(uri.equalsIgnoreCase("SHA384")) {
            return FN_MARMOTTA.SHA384;
        } else if(uri.equalsIgnoreCase("SHA512")) {
            return FN_MARMOTTA.SHA512;
        } else if(uri.equalsIgnoreCase("NOW")) {
            return FN_MARMOTTA.NOW;
        } else if(uri.equalsIgnoreCase("YEAR")) {
            return FN_MARMOTTA.YEAR;
        } else if(uri.equalsIgnoreCase("MONTH")) {
            return FN_MARMOTTA.MONTH;
        } else if(uri.equalsIgnoreCase("DAY")) {
            return FN_MARMOTTA.DAY;
        } else if(uri.equalsIgnoreCase("HOURS")) {
            return FN_MARMOTTA.HOURS;
        } else if(uri.equalsIgnoreCase("MINUTES")) {
            return FN_MARMOTTA.MINUTES;
        } else if(uri.equalsIgnoreCase("SECONDS")) {
            return FN_MARMOTTA.SECONDS;
        } else if(uri.equalsIgnoreCase("TIMEZONE")) {
            return FN_MARMOTTA.TIMEZONE;
        } else if(uri.equalsIgnoreCase("TZ")) {
            return FN_MARMOTTA.TZ;
        } else if(uri.equalsIgnoreCase("ABS")) {
            return FN.NUMERIC_ABS;
        } else if(uri.equalsIgnoreCase("CEIL")) {
            return FN.NUMERIC_CEIL;
        } else if(uri.equalsIgnoreCase("FLOOR")) {
            return FN.NUMERIC_FLOOR;
        } else if(uri.equalsIgnoreCase("ROUND")) {
            return FN.NUMERIC_ROUND;
        }

        return new URIImpl(uri);
    }

}
