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
package org.apache.marmotta.commons.sesame.model;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility methods for working with literals.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LiteralCommons {
    private static DatatypeFactory dtf;
    static {
        try {
            dtf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
        }
    }

    
    /**
	 * Create a cache key for a literal with the given content, locale and type
	 *
	 * @param content  string content representing the literal (can be an MD5 sum for binary types)
	 * @param language language of the literal (optional)
	 * @param type     datatype URI of the literal (optional)
	 * @return a 64bit hash key for the literal
	 */
    public static String createCacheKey(String content, Locale language, URI type) {
		return createCacheKey(content, language, type != null ? type.stringValue() : null);
	}

	/**
     * Create a cache key for a literal with the given content, locale and type
     *
     * @param content  string content representing the literal (can be an MD5 sum for binary types)
     * @param language language of the literal (optional)
     * @param type     datatype URI of the literal (optional)
     * @return a 64bit hash key for the literal
     */
    public static String createCacheKey(String content, Locale language, String type) {
        Hasher hasher = Hashing.goodFastHash(64).newHasher();
        hasher.putString(content);
        if(type != null) {
            hasher.putString(type);
        }
        if(language != null) {
            hasher.putString(language.getLanguage().toLowerCase());
        }
        return hasher.hash().toString();
    }

    /**
     * Create a cache key for the date literal with the given date. Converts the date
     * to a XMLGregorianCalendar with UTC timezone and then calls the method above.
     *
     * @param date date object of the date literal
     * @param type datatype URI of the literal
     * @return a 64bit hash key for the literal
     */
    public static String createCacheKey(Date date, String type) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);

        XMLGregorianCalendar xml_cal = dtf.newXMLGregorianCalendar(cal).normalize();
        xml_cal.setTimezone(0);

        return createCacheKey(xml_cal.toXMLFormat(), null, type);
    }

    /**
     * Create a cache key for the literal passed as argument. Takes content, language
     * and datatype URI as parameter to create a 64bit hash.
     *
     * @param l the literal to create the hash for
     * @return a 64bit hash key for the literal
     */
    public static String createCacheKey(Literal l) {
        Hasher hasher = Hashing.goodFastHash(64).newHasher();
        hasher.putString(l.getLabel());
        if(l.getDatatype() != null) {
            hasher.putString(l.getDatatype().stringValue());
        }
        if(l.getLanguage() != null) {
            hasher.putString(l.getLanguage().toLowerCase());
        }
        return hasher.hash().toString();
    }

    /**
     * Get an appropriate RDF type for the mime type passed as argument.
     * @param mime_type
     * @return
     */
    public static String getRDFType(String mime_type) {
        String iw_type = "MultimediaObject";
        if (mime_type.startsWith("image")) {
            iw_type = "Image";
        } else if (mime_type.startsWith("video/flash")) {
            iw_type = "FlashVideo";
        } else if (mime_type.startsWith("video")) {
            iw_type = "Video";
        } else if (mime_type.startsWith("application/pdf")) {
            iw_type = "PDFDocument";
        } else if (mime_type.startsWith("application/msword")) {
            iw_type = "MSWordDocument";
        } else if (mime_type
                .startsWith("application/vnd.oasis.opendocument")
                || mime_type.startsWith("application/postscript")
                || mime_type.startsWith("application/vnd.ms-")) {
            iw_type = "Document";
        } else if (mime_type.startsWith("audio/mpeg")
                || mime_type.startsWith("audio/mp3")) {
            iw_type = "MP3Audio";
        } else if (mime_type.startsWith("audio")) {
            iw_type = "Audio";
        } else if (mime_type.startsWith("text/html")) {
            iw_type = "HTML";
        } else if (mime_type.startsWith("text")) {
            iw_type = "TEXT";
        }
        return Namespaces.NS_KIWI_CORE + iw_type;
    }

    /**
     * Return the appropriate XSD type for RDF literals for the provided Java class.
     * @param javaClass
     * @return
     */
    public static String getXSDType(Class<?> javaClass) {
        if(String.class.isAssignableFrom(javaClass)) {
            return Namespaces.NS_XSD+"string";
        } else if(Integer.class.isAssignableFrom(javaClass) || int.class.isAssignableFrom(javaClass)) {
            return Namespaces.NS_XSD+"integer";
        } else if(Long.class.isAssignableFrom(javaClass) || long.class.isAssignableFrom(javaClass)) {
            return Namespaces.NS_XSD+"long";
        } else if(Double.class.isAssignableFrom(javaClass) || double.class.isAssignableFrom(javaClass)) {
            return Namespaces.NS_XSD+"double";
        } else if(Float.class.isAssignableFrom(javaClass) || float.class.isAssignableFrom(javaClass)) {
            return Namespaces.NS_XSD+"float";
        } else if(Date.class.isAssignableFrom(javaClass)) {
            return Namespaces.NS_XSD+"dateTime";
        } else if(Boolean.class.isAssignableFrom(javaClass) || boolean.class.isAssignableFrom(javaClass)) {
            return Namespaces.NS_XSD+"boolean";
        } else {
            // FIXME: MARMOTTA-39 (no default datatype before RDF-1.1)
            return null; //Namespaces.NS_XSD+"string";
        }
    }
    
    /**
     * The RDF 1.1 datatype for language literals.
     * @see <a href="http://www.w3.org/TR/2013/WD-rdf11-concepts-20130115/#section-Graph-Literal">http://www.w3.org/TR/2013/WD-rdf11-concepts-20130115/#section-Graph-Literal</a>
     */
    public static String getRDFLangStringType() {
    	return Namespaces.NS_RDF + "langString";
    }
}
