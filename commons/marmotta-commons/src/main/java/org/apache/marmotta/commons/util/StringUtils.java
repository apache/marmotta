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
package org.apache.marmotta.commons.util;

/**
 * Some util string functions
 *
 * @author Sergio Fernández
 */
public class StringUtils {

    private StringUtils() {

    }

    public static String fixLatin1(String str) {
        //TODO: find a way to re-code properly the literal
        //http://www.ic.unicamp.br/~stolfi/EXPORT/www/ISO-8859-1-Encoding.html
        str = str.replaceAll("\\\\xe1", "á");
        str = str.replaceAll("\\\\xe2", "â");
        str = str.replaceAll("\\\\xe3", "ã");
        str = str.replaceAll("\\\\xe4", "ä");
        str = str.replaceAll("\\\\xe7", "ç");
        str = str.replaceAll("\\\\xe8", "è");
        str = str.replaceAll("\\\\xe9", "é");
        str = str.replaceAll("\\\\xea", "ê");
        str = str.replaceAll("\\\\xeb", "ë");
        str = str.replaceAll("\\\\xed", "í");
        str = str.replaceAll("\\\\xee", "î");
        str = str.replaceAll("\\\\xef", "ï");
        str = str.replaceAll("\\\\xf3", "ó");
        str = str.replaceAll("\\\\xf4", "ô");
        str = str.replaceAll("\\\\xf6", "ö");
        str = str.replaceAll("\\\\xf9", "ù");
        str = str.replaceAll("\\\\xfb", "û");
        str = str.replaceAll("\\\\xfc", "ü");
        str = str.replaceAll("\\\\xfa", "ú");
        str = str.replaceAll("\\\\x", ""); //FIXME: wrong, wrong, wrong!
        return str;
    }

}
