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

package org.apache.marmotta.ldpath.backend.sesame;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameValueBackend implements NodeBackend<Value>{

    private static final Logger log = LoggerFactory.getLogger(SesameValueBackend.class);

    public SesameValueBackend() {
        super();
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

}