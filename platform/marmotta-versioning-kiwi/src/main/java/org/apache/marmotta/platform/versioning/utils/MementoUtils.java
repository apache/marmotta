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
package org.apache.marmotta.platform.versioning.utils;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class MementoUtils {

    public static final String MEMENTO_WEBSERVICE = "memento";
    public static final String MEMENTO_TIMEGATE = "timegate";
    public static final String MEMENTO_TIMEMAP = "timemap";
    public static final String MEMENTO_RESOURCE = "resource";

    /**
     * is used for date format used in memento resource uris
     */
    public static final DateFormat MEMENTO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * builds a memento permalink
     * @param date the date of the version that should be represented by the permalink
     * @param resource  the resource that should be represented by the permalink
     * @return a permalink
     */
    public static URI resourceURI(String resource, Date date, String baseURI) {
        return URI.create(
                baseURI +
                        MEMENTO_WEBSERVICE + "/" +
                        MEMENTO_RESOURCE + "/" +
                        MEMENTO_DATE_FORMAT.format(date) + "/" +
                        resource);
    }

    /**
     * builds a memento timemap uri
     * @param resource  the resource that should be represented
     * @return a timemap uri
     */
    public static URI timemapURI(String resource, String baseURI) {
        return URI.create(
                baseURI +
                        MEMENTO_WEBSERVICE + "/" +
                        MEMENTO_TIMEMAP + "/" +
                        resource);
    }

    /**
     * builds a memento timemap uri
     * @param resource  the resource that should be represented
     * @return a timemap uri
     */
    public static URI timegateURI(String resource, String baseURI) {
        return URI.create(
                baseURI +
                        MEMENTO_WEBSERVICE + "/" +
                        MEMENTO_TIMEGATE + "/" +
                        resource);
    }

}
