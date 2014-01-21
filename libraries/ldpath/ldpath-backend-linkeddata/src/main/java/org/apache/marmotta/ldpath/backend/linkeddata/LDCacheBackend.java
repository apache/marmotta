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
package org.apache.marmotta.ldpath.backend.linkeddata;

import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.backend.infinispan.LDCachingInfinispanBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A Linked Data backend with persistent caching of the retrieved data. All data is read and stored in the directory
 * passed as constructor argument.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDCacheBackend implements RDFBackend<Value> {
    private static final Logger log = LoggerFactory.getLogger(LDCacheBackend.class);


    private LDCache ldcache;


    public LDCacheBackend() {
        LDCachingBackend backend = new LDCachingInfinispanBackend();
        backend.initialize();

        this.ldcache = new LDCache(new CacheConfiguration(), backend);
    }

    public LDCacheBackend(LDCache ldcache) {
        this.ldcache = ldcache;
    }

    /**
     * Test whether the node passed as argument is a literal
     *
     * @param n the node to check
     * @return true if the node is a literal
     */
    @Override
    public boolean isLiteral(Value n) {
        return n instanceof Literal;
    }

    /**
     * Test whether the node passed as argument is a URI
     *
     * @param n the node to check
     * @return true if the node is a URI
     */
    @Override
    public boolean isURI(Value n) {
        return n instanceof org.openrdf.model.URI;
    }

    /**
     * Test whether the node passed as argument is a blank node
     *
     * @param n the node to check
     * @return true if the node is a blank node
     */
    @Override
    public boolean isBlank(Value n) {
        return n instanceof BNode;
    }

    /**
     * Return the language of the literal node passed as argument.
     *
     * @param n the literal node for which to return the language
     * @return a Locale representing the language of the literal, or null if the literal node has no language
     * @throws IllegalArgumentException in case the node is no literal
     */
    @Override
    public Locale getLiteralLanguage(Value n) {
        try {
            if(((Literal)n).getLanguage() != null) {
                return new Locale( ((Literal)n).getLanguage() );
            } else {
                return null;
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+n.stringValue()+" is not a literal" +
                    "but of type "+debugType(n));
        }
    }

    /**
     * Return the URI of the type of the literal node passed as argument.
     *
     * @param n the literal node for which to return the typer
     * @return a URI representing the type of the literal content, or null if the literal is untyped
     * @throws IllegalArgumentException in case the node is no literal
     */
    @Override
    public URI getLiteralType(Value n) {
        try {
            if(((Literal)n).getDatatype() != null) {
                try {
                    return new URI(((Literal)n).getDatatype().stringValue());
                } catch (URISyntaxException e) {
                    log.error("literal datatype was not a valid URI: {}",((Literal) n).getDatatype());
                    return null;
                }
            } else {
                return null;
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+n.stringValue()+" is not a literal" +
                    "but of type "+debugType(n));
        }
    }

    /**
     * Return the string value of a node. For a literal, this will be the content, for a URI node it will be the
     * URI itself, and for a blank node it will be the identifier of the node.
     *
     * @param value
     * @return
     */
    @Override
    public String stringValue(Value value) {
        return value.stringValue();
    }

    @Override
    public BigDecimal decimalValue(Value node) {
        try {
            return ((Literal)node).decimalValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }

    @Override
    public BigInteger integerValue(Value node) {
        try {
            return ((Literal)node).integerValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }

    @Override
    public Boolean booleanValue(Value node) {
        try {
            return ((Literal)node).booleanValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }

    @Override
    public Date dateTimeValue(Value node) {
        try {
            XMLGregorianCalendar cal = ((Literal)node).calendarValue();
            //TODO: check if we need to deal with timezone and Local here
            return cal.toGregorianCalendar().getTime();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }

    @Override
    public Date dateValue(Value node) {
        try {
            XMLGregorianCalendar cal = ((Literal)node).calendarValue();
            return new GregorianCalendar(cal.getYear(), cal.getMonth(), cal.getDay()).getTime();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }

    @Override
    public Date timeValue(Value node) {
        //TODO: Unless someone knows how to create a Date that only has the time
        //      from a XMLGregorianCalendar
        return dateTimeValue(node);
    }

    @Override
    public Long longValue(Value node) {
        try {
            return ((Literal)node).longValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }

    @Override
    public Double doubleValue(Value node) {
        try {
            return ((Literal)node).doubleValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }

    @Override
    public Float floatValue(Value node) {
        try {
            return ((Literal)node).floatValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }

    @Override
    public Integer intValue(Value node) {
        try {
            return ((Literal)node).intValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value "+node.stringValue()+" is not a literal" +
                    "but of type "+debugType(node));
        }
    }


    /**
     * Prints the type (URI,bNode,literal) by inspecting the parsed {@link Value}
     * to improve error messages and other loggings. In case of literals
     * also the {@link #getLiteralType(Value) literal type} is printed
     * @param value the value or <code>null</code>
     * @return the type as string.
     */
    protected String debugType(Value value) {
        return value == null ? "null":isURI(value)?"URI":isBlank(value)?"bNode":
                "literal ("+getLiteralType(value)+")";
    }

    @Override
    public Literal createLiteral(String content) {
        return new LiteralImpl(content);
    }

    @Override
    public Literal createLiteral(String content, Locale language, URI type) {
        log.debug("creating literal with content \"{}\", language {}, datatype {}",new Object[]{content,language,type});
        if(language == null && type == null) {
            return createLiteral(content);
        } else if(type == null) {
            return new LiteralImpl(content,language.getLanguage());
        } else  {
            return new LiteralImpl(content, createURI(type.toString()));
        }
    }

    @Override
    public org.openrdf.model.URI createURI(String uri) {
        return new URIImpl(uri);
    }


    /**
     * @deprecated subject to be removed in the next release
     */
    @Override
    public boolean supportsThreading() {
        return false;
    }

    /**
     * @deprecated subject to be removed in the next release
     */
    @Override
    public ThreadPoolExecutor getThreadPool() {
        return null;
    }

    /**
     * List the objects of triples in the triple store underlying this backend that have the subject and
     * property given as argument.
     *
     * @param subject  the subject of the triples to look for
     * @param property the property of the triples to look for, <code>null</code> is interpreted as wildcard
     * @return all objects of triples with matching subject and property
     */
    @Override
    public Collection<Value> listObjects(Value subject, Value property) {
        log.info("retrieving resource {}", subject);
        if(subject instanceof org.openrdf.model.URI && subject instanceof org.openrdf.model.URI) {
            org.openrdf.model.URI s = (org.openrdf.model.URI) subject;
            org.openrdf.model.URI p = (org.openrdf.model.URI) property;
            return ldcache.get(s).filter(s, p, null).objects();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * List the subjects of triples in the triple store underlying this backend that have the object and
     * property given as argument.
     *
     * @param property the property of the triples to look for, <code>null</code> is interpreted as wildcard
     * @param object   the object of the triples to look for
     * @return all subjects of triples with matching object and property
     * @throws UnsupportedOperationException in case reverse selection is not supported (e.g. when querying Linked Data)
     */
    @Override
    public Collection<Value> listSubjects(Value property, Value object) {
        throw new UnsupportedOperationException("reverse traversal not supported for Linked Data backend");
    }
}
