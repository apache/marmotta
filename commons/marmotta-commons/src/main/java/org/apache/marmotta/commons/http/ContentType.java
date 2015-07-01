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
package org.apache.marmotta.commons.http;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A LMF internal representation of MIME media types that captures the various 
 * aspects of interest in a structured way.
 * 
 * @author Sebastian Schaffert
 * @author Sergio Fernández
 */
public class ContentType implements Comparable<ContentType> {

    private String type, subtype;

    private Charset charset = Charset.defaultCharset();

    private Map<String,String> parameters = new HashMap<String, String>();

    public ContentType(String type, String subtype) {
        this.type = type;
        this.subtype = subtype;
        setParameter("q","1.0");
    }

    public ContentType(String type, String subtype, double q) {
        this.type = type;
        this.subtype = subtype;
        setParameter("q",Double.toString(q));
    }


    public ContentType(String type, String subtype, Charset charset) {
        this(type,subtype);
        this.charset = charset;
    }

    /**
     * Return true if the ContentType matches with another content type, not taking into account wildcards and possible "+"
     * extensions.
     *
     * @param contentType
     * @return
     */
    public boolean matches(ContentType contentType) {
        if(! (contentType.getType().equalsIgnoreCase(getType()))) {
            return false;
        }
        if(! (contentType.getSubtype().equalsIgnoreCase(getSubtype()))) {
            return false;
        }
        return getParameter("rel") == null || getParameter("rel").equalsIgnoreCase(contentType.getParameter("rel"));

    }

    /**
     * Return true if the ContentType matches with another content type, taking into account possible "+"
     * extensions, as well as subtype qualifiers attached to the subtype with + (e.g. application/rdf+json)
     *
     * TODO: match types of the form type/qualifier+subtype with type/subtype
     *
     * @param contentType
     * @return
     */
    public boolean matchesSubtype(ContentType contentType) {
        if(! ("*".equals(getType()) || contentType.getType().equalsIgnoreCase(getType()))) {
            return false;
        }
        String[] componentsMine  = getSubtype().split("\\+");
        String[] componentsOther = contentType.getSubtype().split("\\+");

        if(componentsOther.length > 1 && !(componentsOther[1].equalsIgnoreCase(getSubtype()))) {
            return false;
        }

        if((componentsMine.length == 2 || componentsOther.length == 1) && ! (contentType.getSubtype().equalsIgnoreCase(getSubtype()))) {
            return false;
        }
        return getParameter("rel") == null || getParameter("rel").equalsIgnoreCase(contentType.getParameter("rel"));

    }

    /**
     * Return true if the ContentType matches with another content type, taking into account wildcards
     *
     * TODO: match types of the form type/qualifier+subtype with type/subtype
     *
     * @param contentType
     * @return
     */
    public boolean matchesWildcard(ContentType contentType) {
        if(! ("*".equals(getType()) || contentType.getType().equalsIgnoreCase(getType()))) {
            return false;
        }
        if(! ("*".equals(getSubtype()) || contentType.getSubtype().equalsIgnoreCase(getSubtype()))) {
            return false;
        }
        return getParameter("rel") == null || getParameter("rel").equalsIgnoreCase(contentType.getParameter("rel"));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }


    public String getMime() {
        return type + "/" + subtype;
    }


    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
        setParameter("charset",charset.name().toLowerCase());
    }


    public String getParameter(String key) {
        return parameters.get(key);
    }

    public void setParameter(String key, String value) {
        if(value.startsWith("\"")) {
            parameters.put(key,value.substring(1,value.length()-1));
        } else {
            parameters.put(key,value);
        }
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder base = new StringBuilder();
        base.append(type);
        base.append("/");
        base.append(subtype);

        for(String key : parameters.keySet()) {
            base.append("; ");
            base.append(key);
            base.append("=");
            base.append(parameters.get(key));
        }

        return base.toString();
    }

    /**
     * Create a string representation of this content type using only the type/subtype scheme and ignoring charset
     * and other parameters.
     * @return
     */
    public String toStringNoParameters() {
        return toString(new String[]{});
    }


    public String toString(String... paramNames) {
        StringBuilder base = new StringBuilder();
        base.append(type);
        base.append("/");
        base.append(subtype);

        for(String key : paramNames) {
            if(parameters.get(key) != null) {
                base.append("; ");
                base.append(key);
                base.append("=");
                base.append(parameters.get(key));
            }
        }

        return base.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentType that = (ContentType) o;

        if (!charset.equals(that.charset)) return false;
        if (!parameters.equals(that.parameters)) return false;
        if (!subtype.equals(that.subtype)) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + subtype.hashCode();
        result = 31 * result + charset.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }

    @Override
    public int compareTo(ContentType o) {

        return 8 * compareType(type,o.getType()) +
               4 * compareType(subtype,o.getSubtype()) +
               2 * compareParameters(parameters, o.parameters) +
               1 * compareQ(parameters.get("q"),o.parameters.get("q"));
    }

    private int compareType(String t1, String t2) {
        if("*".equals(t1) && !"*".equals(t2)) {
            return 1;
        } else if(!"*".equals(t1) && "*".equals(t2)) {
            return -1;
        } else {
            return 0;
        }
    }

    private int compareParameters(Map<String,String> p1, Map<String,String> p2) {
        if(p1.size() > p2.size()) {
            // p1 is more specific
            return -1;
        } else if(p1.size() < p2.size()) {
            return 1;
        } else {
            return 0;
        }
    }

    private int compareQ(String s1, String s2) {
        double q1 = 0.0, q2 = 0.0;
        if(s1 != null) {
            try {
                q1 = Double.parseDouble(s1);
            } catch(NumberFormatException ex) {}
        }
        if(s2 != null) {
            try {
                q2 = Double.parseDouble(s2);
            } catch(NumberFormatException ex) {}
        }
        // if q1 is bigger than q2, we sort it first ...
        return (int)Math.signum(q2-q1);
    }

}
