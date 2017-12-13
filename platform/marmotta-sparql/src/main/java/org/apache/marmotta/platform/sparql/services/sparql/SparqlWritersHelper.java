/*
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
package org.apache.marmotta.platform.sparql.services.sparql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper for working with SPARQL Writers
 * 
 * @author Sergio Fernández
 *
 */
public class SparqlWritersHelper {
    
    public static Pattern subTypePattern = Pattern.compile("[a-z]+/([a-z0-9-._]+\\+)?([a-z0-9-._]+)(;.*)?");
    
    public static String parseSubType(String mimeType) {
        Matcher matcher = subTypePattern.matcher(mimeType);
        if (matcher.matches())
            return matcher.group(2);
        else
            return mimeType;
    }
    
}
