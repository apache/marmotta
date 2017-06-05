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
package org.apache.marmotta.commons.sesame.model;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.joda.time.DateTime;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

/**
 * Utility methods for working with literals.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LiteralCommons {

    private static final int HASH_BITS=128;

    private static DatatypeFactory dtf;
    static {
        try {
            dtf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ignored) {
            //nop;
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
		return createCacheKey(content, language != null ? language.getLanguage() : null, type != null ? type.stringValue() : null);
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
        return createCacheKey(content, language != null ? language.getLanguage() : null, type);
    }

    /**
     * Create a cache key for the date literal with the given date. Converts the date
     * to a XMLGregorianCalendar with UTC timezone and then calls the method above.
     *
     * @param date date object of the date literal
     * @param type datatype URI of the literal
     * @return a 64bit hash key for the literal
     */
    public static String createCacheKey(DateTime date, String type) {
        GregorianCalendar cal = date.toGregorianCalendar();

        XMLGregorianCalendar xml_cal = dtf.newXMLGregorianCalendar(cal);

        return createCacheKey(xml_cal.toXMLFormat(), (String)null, type);
    }

    /**
     * Create a cache key for the literal passed as argument. Takes content, language
     * and datatype URI as parameter to create a 64bit hash.
     *
     * @param l the literal to create the hash for
     * @return a 64bit hash key for the literal
     */
    public static String createCacheKey(Literal l) {
        return createCacheKey(l.getLabel(), l.getLanguage(), l.getDatatype() != null ? l.getDatatype().stringValue() : null);
    }


    /**
     * Create a cache key for a literal with the given content, locale and type
     *
     * @param content  string content representing the literal (can be an MD5 sum for binary types)
     * @param language language of the literal (optional)
     * @param type     datatype URI of the literal (optional)
     * @return a 64bit hash key for the literal
     */
    public static String createCacheKey(String content, String language, String type) {
        Hasher hasher = Hashing.goodFastHash(HASH_BITS).newHasher();
        hasher.putString(content, Charset.defaultCharset());
        if(type != null) {
            hasher.putString(type, Charset.defaultCharset());
        } else {
            // Since Sesame 2.8 all literals are datatyped, so when a null type is received, it must be replaced with default values (xsd:string or rdf:langString).
            // Details: https://web.archive.org/web/20160412201829/http://rdf4j.org:80/sesame/2.8/docs/articles/upgrade-notes.docbook?view
            if (language==null) type=getXSDType(String.class);
            else type=getRDFLangStringType();
            hasher.putString(type, Charset.defaultCharset());
        }
        if(language != null) {
            hasher.putString(language.toLowerCase(), Charset.defaultCharset());
        } 
        return hasher.hash().toString();
    }

    /**
     * Return the appropriate XSD type for RDF literals for the provided Java class.
     * @param clazz the Class
     * @return the XSD type for RDF literals of the provided Class
     */
    public static String getXSDType(Class<?> clazz) {
        if(String.class.isAssignableFrom(clazz)) {
            return Namespaces.NS_XSD+"string";
        } else if(Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
            return Namespaces.NS_XSD+"integer";
        } else if(Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
            return Namespaces.NS_XSD+"long";
        } else if(Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
            return Namespaces.NS_XSD+"double";
        } else if(Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
            return Namespaces.NS_XSD+"float";
        } else if(Date.class.isAssignableFrom(clazz) || DateTime.class.isAssignableFrom(clazz)) {
            return Namespaces.NS_XSD+"dateTime";
        } else if(Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
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

    private LiteralCommons() {
        // static access only
    }

}
