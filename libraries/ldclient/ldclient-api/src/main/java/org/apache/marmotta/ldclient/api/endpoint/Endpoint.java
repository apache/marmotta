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
package org.apache.marmotta.ldclient.api.endpoint;

import org.apache.marmotta.commons.http.ContentType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.apache.marmotta.commons.http.MarmottaHttpUtils.parseAcceptHeader;

/**
 * Definition of a Linked Data Endpoint. Contains information how to query the
 * endpoint, what format to expect, and when to expire data from this endpoint.
 * <p/>
 * <ul>
 * <li> LinkedDataProvider endpoints will be accessed by directly
 * retrieving the URI with appropriate Accept headers following the Linked Data
 * recommendations
 * <li> CacheProvider endpoints will be accessed by passing the URI
 * as request parameter to the cache and parsing the response according to the
 * content type defined for this endpoint (Content-Type header is too
 * unspecific)
 * <li> SPARQLProvider endpoints will be accessed by issuing a query
 * of <code>SELECT ?p ?o WHERE { {url} ?p ?o }</code> to retrieve all triples
 * for the requested resource
 * <li> NONE act as blacklist. Resources matching handled by this endpoint are not fetched.
 * </ul>
 * <p/>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * <p/>
 * User: sschaffe
 */
public class Endpoint implements Comparable<Endpoint> {

    /**
     * constant indicating high priority of this endpoint definition
     */
    public final static int PRIORITY_HIGH = 3;

    /**
     * constant indicating medium priority of this endpoint definition
     */
    public final static int PRIORITY_MEDIUM = 2;

    /**
     * constant indicating low priority of this endpoint definition
     */
    public final static int PRIORITY_LOW = 1;

    /**
     * A human-readable name for this endpoint.
     */
    private String name;

    /**
     * The type of the endpoint. Either the name of a data provider, or the special name "NONE" to indicate that
     * the data for these URIs should not be retrieved.
     */
    private String type;


    public static final String REGEX_INDICATOR = "~";

    /**
     * A regular expression describing for which URIs the endpoint applies. The endpoint will be applied to all
     * resource requests matching with this pattern.
     * 
     * @see Pattern
     */
    private String uriPattern;

    private Pattern uriPatternCompiled = null;

    /**
     * Flag to temporarily enable/disable the endpoint configuration at runtime.
     */
    private boolean            active;

    /**
     * The priority of this endpoint configuration; endpoints with higher priority take precedence over
     * endpoints with lower priority in case both would be applicable.
     */
    private int priority = PRIORITY_MEDIUM;

    /**
     * The HTTP URL to access the endpoint. Occurrences of the string {uri} will
     * be replaced by the resource URI of the queried resource for CACHE
     * endpoints. Occurrences of the string {query} will be replaced by the
     * SPARQL query for the requested resource for SPARQL endpoints.
     * 
     * Requried for SPARQL and CACHE endpoints, ignored for LINKEDDATA endpoints
     * 
     * <pre>
     * Examples:
     * - Sindice: http://api.sindice.com/v2/live?url={uri}
     *        or  http://api.sindice.com/v2/cache?url={uri}
     *        or  http://sparql.sindice.com/sparql?default-graph-uri=&query={query}&format=text%2Fhtml&debug=on
     * - Stanbol: http://dev.iks-project.eu:8080/entityhub/lookup/?id={uri}&create=false
     * </pre>
     */
    private String endpointUrl;


    /**
     * The content type (MIME) returned by this endpoint. Used to determine how to parse the result.
     */
    private Set<ContentType> contentTypes;

    /**
     * The default expiry time in seconds to use for this endpoint if the HTTP request does not explicitly return an
     * expiry time.
     */
    private Long defaultExpiry;


    /**
     * Additional configuration options used by this endpoint. Can be accessed in the different providers.
     */
    private Map<String,String> properties;

    public Endpoint() {
        this.active = true;
        this.contentTypes = new HashSet<ContentType>();
        this.properties = new HashMap<String, String>();    	
    }

    public Endpoint(String name, String type, String uriPattern, String endpointUrl, Long defaultExpiry) {
    	this();
        this.name = name;
        this.type = type;
        this.uriPattern = uriPattern;
        this.endpointUrl = endpointUrl;
        this.defaultExpiry = defaultExpiry;
    }

    public Endpoint(String name, String type, String uriPattern, String endpointUrl, String contentType, Long defaultExpiry) {
        this(name,type,uriPattern,endpointUrl,defaultExpiry);
        this.contentTypes = new HashSet<ContentType>(parseAcceptHeader(contentType));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
        this.uriPatternCompiled = null;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public Set<ContentType> getContentTypes() {
        return contentTypes;
    }

    public void addContentType(ContentType type) {
        contentTypes.add(type);
    }

    public void setContentTypes(Set<ContentType> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public Long getDefaultExpiry() {
        return defaultExpiry;
    }

    public void setDefaultExpiry(Long defaultExpiry) {
        this.defaultExpiry = defaultExpiry;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key,value);
    }

    public Pattern getUriPatternCompiled() {
        if (uriPatternCompiled == null) {
            try {
                if (uriPattern.startsWith(REGEX_INDICATOR)) {
                    // backwards compatibility
                    uriPatternCompiled = Pattern.compile(uriPattern.substring(REGEX_INDICATOR.length()));
                } else {
                    uriPatternCompiled = Pattern.compile(uriPattern);
                }
            } catch (PatternSyntaxException pse) {
            }
        }
        return uriPatternCompiled;
    }

    /**
     * Check if this {@link Endpoint} handles (is responsible) for this URI.
     * 
     * @param uri the URI to check
     * @return <code>true</code> if the uri matches the endpoint's {@link #uriPattern}
     * 
     * @see #uriPattern
     */
    public boolean handles(String uri) {
        if (isActive() && getUriPatternCompiled() != null) return getUriPatternCompiled().matcher(uri).find();
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Endpoint endpoint = (Endpoint) o;

        if (name != null ? !name.equals(endpoint.name) : endpoint.name != null) return false;
        if (type != null ? !type.equals(endpoint.type) : endpoint.type != null) return false;
        if (uriPattern != null ? !uriPattern.equals(endpoint.uriPattern) : endpoint.uriPattern != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (uriPattern != null ? uriPattern.hashCode() : 0);
        return result;
    }


    /**
     * Compares this object with the specified object for order according to priority.
     * Returns a negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @see #getPriority()
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Endpoint o) {
        if(getPriority() > o.getPriority()) {
            return -1;
        } else if(getPriority() < o.getPriority()) {
            return 1;
        } else {
            return 0;
        }
    }
}
