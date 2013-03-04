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
package at.newmedialab.ldclient.model;

import org.openrdf.model.URI;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Definition of a Linked Data Endpoint. Contains information how to query the
 * endpoint, what format to expect, and when to expire data from this endpoint.
 * <p/>
 * <ul>
 * <li> {@link EndpointType#LINKEDDATA} endpoints will be accessed by directly
 * retrieving the URI with appropriate Accept headers following the Linked Data
 * recommendations
 * <li> {@link EndpointType#CACHE} endpoints will be accessed by passing the URI
 * as request parameter to the cache and parsing the response according to the
 * content type defined for this endpoint (Content-Type header is too
 * unspecific)
 * <li> {@link EndpointType#SPARQL} endpoints will be accessed by issuing a query
 * of <code>SELECT ?p ?o WHERE { {url} ?p ?o }</code> to retrieve all triples
 * for the requested resource
 * <li> {@link EndpointType#NONE} act as blacklist. Resources matching handled by
 * this endpoint are not fetched.
 * </ul>
 * <p/>
 * User: sschaffe
 */

public class Endpoint {


	private Long id;


	/**
	 * A human-readable name for this endpoint.
	 */
	private String name;

	/**
	 * The type of the endpoint. One of LINKEDDATA, CACHE, or SPARQL
	 */
	private EndpointType type;


	public static final String REGEX_INDICATOR = "~";

	/**
	 * URI prefix managed by the endpoint. All resources matching the URI prefix
	 * are handled by this endpoint. {@link #uriPrefix} starting with
	 * <code>{@value #REGEX_INDICATOR}</code> are interpreted as <i>regular
	 * expression</i> (which may not be a prefix).
	 *
	 * @see java.util.regex.Pattern
	 */
	private String uriPrefix;

	private Pattern uriPattern = null;

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
	private String contentType;


	/**
	 * The default expiry time in seconds to use for this endpoint if the HTTP request does not explicitly return an
	 * expiry time.
	 */
	private Long defaultExpiry;



	public Endpoint() {
	}


	public Endpoint(String name, EndpointType type, String uriPrefix, String endpointUrl, String contentType, Long defaultExpiry) {
		this.name = name;
		this.type = type;
		this.uriPrefix = uriPrefix;
		this.endpointUrl = endpointUrl;
		this.contentType = contentType;
		this.defaultExpiry = defaultExpiry;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EndpointType getType() {
		return type;
	}

	public void setType(EndpointType type) {
		this.type = type;
	}

	public String getUriPrefix() {
		return uriPrefix;
	}

	public void setUriPrefix(String uriPrefix) {
		this.uriPrefix = uriPrefix;
	}

	public String getEndpointUrl() {
		return endpointUrl;
	}

	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getDefaultExpiry() {
		return defaultExpiry;
	}

	public void setDefaultExpiry(Long defaultExpiry) {
		this.defaultExpiry = defaultExpiry;
	}


	/**
	 * Check if this {@link Endpoint} handles (is responsible) for this {@link URI}.
	 *
	 * @param resource the Resource to check
	 * @return <code>true</code> if the {@link javax.annotation.Resource}'s uri matches the endpoint's {@link #uriPrefix}
	 *
	 * @see #uriPrefix
	 */
	public boolean handles(URI resource) {
		return handles(resource.stringValue());
	}


	/**
	 * Check if this {@link Endpoint} handles (is responsible) for this URI.
	 * 
	 * @param uri the URI to check
	 * @return <code>true</code> if the uri matches the endpoint's {@link #uriPrefix}
	 * 
	 * @see #uriPrefix
	 */
	public boolean handles(String uri) {
		if (uriPrefix.startsWith(REGEX_INDICATOR)) {
			try {
				if (uriPattern == null) {
					uriPattern = Pattern.compile(uriPrefix.substring(REGEX_INDICATOR.length()));
				}
				return uriPattern.matcher(uri).find();
			} catch (PatternSyntaxException pse) {
				return false;
			}
		} else {
			return uri.startsWith(uriPrefix);
		}
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Endpoint endpoint = (Endpoint) o;

		if (name != null ? !name.equals(endpoint.name) : endpoint.name != null) {
			return false;
		}
		if (type != endpoint.type) {
			return false;
		}
		if (uriPrefix != null ? !uriPrefix.equals(endpoint.uriPrefix) : endpoint.uriPrefix != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (uriPrefix != null ? uriPrefix.hashCode() : 0);
		return result;
	}

	public enum EndpointType {
		/**
		 * Endpoint is a SPARQL endpoint that needs to be queried
		 */
		SPARQL,

		/**
		 * Endpoint is direct access to a linked data source
		 */
		LINKEDDATA,

		/**
		 * Endpoint is a triple cache that can be accessed by passing the resource URI as request parameter
		 */
		CACHE,

		/**
		 * Endpoint does not retrieve external triples
		 */
		NONE
	}

}
