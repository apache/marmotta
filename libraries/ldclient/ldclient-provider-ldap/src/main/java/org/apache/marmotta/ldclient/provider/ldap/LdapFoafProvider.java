/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ldclient.provider.ldap;

import com.unboundid.ldap.sdk.*;

import org.apache.marmotta.commons.constants.Namespace;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.provider.ldap.mapping.LiteralPredicateFactory;
import org.apache.marmotta.ldclient.provider.ldap.mapping.PredicateObjectFactory;
import org.apache.marmotta.ldclient.provider.ldap.mapping.UriPredicateFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LdapFoafProvider mapps LDAP accounts to foaf:Person
 * 
 * @author Daniel Trabe <daniel.trabe@salzburgresearch.at>
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 * 
 */
public class LdapFoafProvider implements DataProvider {

    private static Logger log = LoggerFactory.getLogger(LdapFoafProvider.class);

    /**
     * Mapping for the Attributes in the LDAP-Directory
     */
    private static final Map<String, PredicateObjectFactory> MAPPING;
    static {
        Map<String, PredicateObjectFactory> m = new HashMap<String, PredicateObjectFactory>();
        m.put("distinguishedName", new LiteralPredicateFactory(Namespace.DCTERMS.identifier));
        m.put("name", new LiteralPredicateFactory(Namespace.FOAF.name));
        m.put("givenName", new LiteralPredicateFactory(Namespace.FOAF.firstName));
        m.put("sn", new LiteralPredicateFactory(Namespace.FOAF.surname));
        m.put("mail", new UriPredicateFactory(Namespace.FOAF.mbox) {
            @Override
            public Set<Value> createObjects(String value, ValueFactory valueFactory) {
                return super.createObjects("mailto:" + value, valueFactory);
            }
        });
        m.put("objectClass", new UriPredicateFactory(Namespace.RDF.type) {
            @Override
            public Set<Value> createObjects(String value, ValueFactory valueFactory) {
                if (value.equalsIgnoreCase("person"))
                    return super.createObjects(Namespace.FOAF.Person, valueFactory);
                else
                    return Collections.emptySet();
            }
        });

        MAPPING = Collections.unmodifiableMap(m);
    }

    @Override
    public String getName() {
        return "LdapFoafProvider";
    }

    @Override
    public String[] listMimeTypes() {
        return new String[] {};
    }

    /**
     * Opens a connection tho the LDAP-server. in most cases an Account + Password is needed to
     * connect
     * to the Server. This need to be configured at the endpoint
     * 
     * @param endpoint
     * @return
     * @throws LDAPException if connecting failed.
     * @throws DataRetrievalException
     */
    private LDAPConnection openLdapConnection(Endpoint endpoint) throws LDAPException, DataRetrievalException {
        try {
            String loginDN, loginPW;

            loginDN = endpoint.getProperty("loginDN");
            loginPW = endpoint.getProperty("loginPW");

            java.net.URI u = new java.net.URI(endpoint.getEndpointUrl());
            LDAPConnection ldapCTX = new LDAPConnection(u.getHost(), u.getPort() > 0 ? u.getPort() : 389, loginDN, loginPW);
            return ldapCTX;
        } catch (URISyntaxException e) {
            throw new DataRetrievalException("Invalid LDAP-URL in config for endpoint '" + endpoint.getName() + "'!");
        }

    }

    @Override
    public ClientResponse retrieveResource(String resource, LDClientService client, Endpoint endpoint) throws DataRetrievalException {
        String account = java.net.URI.create(resource.replaceAll(" ", "%20")).getPath().substring(1);
        String prefix = getEndpointSuffix(endpoint);
        try {
            final LDAPConnection ldapCTX = openLdapConnection(endpoint);

            Repository rep = new SailRepository(new MemoryStore());
            rep.initialize();

            RepositoryConnection conn = rep.getConnection();
            try {
                ValueFactory vf = conn.getValueFactory();
                String userDN = buildDN(prefix, account, ldapCTX);

                Map<String, java.util.List<String>> accountData = getAccountData(userDN, ldapCTX);

                final URI subject = vf.createURI(resource);
                for (String attr : MAPPING.keySet()) {
                    if (!accountData.containsKey(attr)) {
                        continue;
                    }

                    final PredicateObjectFactory factory = MAPPING.get(attr);
                    final URI predicate = factory.createPredicate(vf);

                    for (String val : accountData.get(attr)) {
                        for (Value object : factory.createObjects(val, vf)) {
                            conn.add(vf.createStatement(subject, predicate, object));
                        }
                    }

                }

                final ClientResponse resp = new ClientResponse(rep);
                resp.setExpires(new Date());
                return resp;

            } finally {
                conn.close();
            }
        } catch (RepositoryException e1) {
            log.warn("Could not create SailRep: {}", e1.getMessage());
            throw new DataRetrievalException(e1);
        } catch (LDAPException e) {
            log.warn("Could not connect to LDAP Server: {}", e.getMessage());
            throw new DataRetrievalException("Connection to LADP failed", e);
        }
    }

    /**
     * Returns the suffix (e.g. "dc=salzburgresearch,dc=at" of the Endpoint. This suffix is needed
     * to perform
     * miscellaneous LDAP-Operations
     * 
     * @param endpoint
     * @return
     * @throws DataRetrievalException when Config of LDAP-Suffix for endpoint is invalid
     */
    private String getEndpointSuffix(Endpoint endpoint) throws DataRetrievalException {
        try {
            java.net.URI u;
            u = new java.net.URI(endpoint.getEndpointUrl());
            return u.getPath() != null ? u.getPath().substring(1) : "";
        } catch (URISyntaxException e) {
            throw new DataRetrievalException("Invalid LDAP-Suffix config for endpoint '" + endpoint.getName() + "'!");
        }
    }

    /**
     * Builds an distinguished name which is needed for many LDAP operations based
     * on the URL we get from the resource.
     * (e.g "/SRFG/USERS/Daniel%20Trabe" ==>
     * "cn=Daniel  Trabe,ou=USERS,ou=SRFG,dc=salzburgresearch,dc=at")
     * 
     * @param suffix the LDAP-Suffix configured at the Endpoint
     * @param path part of the URL (e.g. /SRFG/USERS/DanielTrabe)
     * @param con an initialized LDAP-Context
     * @return the distinguished name of an Entry in the LDAP
     * @throws LDAPException if there is a problem with the LDAP connection
     * @throws DataRetrievalException when the Entry cannot be found
     */
    private String buildDN(String suffix, String path, LDAPConnection con) throws LDAPException, DataRetrievalException {
        if (path.length() == 0) return suffix;

        String current = path.split("/")[0];

        final List<String> data = getChildList(suffix, con);
        String next = null;
        for (String dn : data) {
            if (dn.toLowerCase().endsWith((current + "," + suffix).toLowerCase())) {
                next = dn;
                break;
            }
        }

        if (next == null) throw new DataRetrievalException("The Object '" + current + "' cannot be found");

        return buildDN(next, path.replaceFirst("^[^/]*(/|$)", ""), con);
    }

    /**
     * 
     * @param entryDN The distinguished name of an Entry in the LDAP
     * @param con An initialized LDAP-Context
     * @return All child's of an Entry
     * @throws LDAPSearchException
     */
    private List<String> getChildList(String entryDN, LDAPConnection con) throws LDAPSearchException {
        java.util.List<String> res = new ArrayList<String>();

        Filter filter = Filter.createPresenceFilter("distinguishedName");
        SearchRequest req = new SearchRequest(entryDN, SearchScope.ONE, filter);
        SearchResult result = con.search(req);

        for (Entry entry : result.getSearchEntries()) {
            res.add(entry.getDN());
        }
        return res;
    }

    /**
     * 
     * @param accountDN The distinguished Name of the Account (e.g
     *            "cn=Daniel  Trabe,ou=USERS,ou=SRFG,dc=salzburgresearch,dc=at")
     * @param con An initialized LDAP-Context
     * @return a Map of Attributes and Values of an Account
     * @throws DataRetrievalException
     * @throws LDAPException
     */
    private Map<String, java.util.List<String>> getAccountData(String accountDN, LDAPConnection con) throws DataRetrievalException, LDAPException {
        Map<String, java.util.List<String>> res = new HashMap<String, java.util.List<String>>();

        SearchResultEntry entry = con.getEntry(accountDN);
        for (Attribute attr : entry.getAttributes()) {
            ArrayList<String> vals = new ArrayList<String>();
            for (String val : entry.getAttributeValues(attr.getName())) {
                vals.add(val);
            }
            res.put(attr.getBaseName(), vals);
        }
        return res;
    }

}
