/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.lmf.client.clients;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.exception.LMFClientException;
import at.newmedialab.lmf.client.util.KiWiCollections;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provide Semantic Search functionality to the LMF search cores based on SOLRJ queries and results. Any SOLRJ query
 * can be used and a SOLRJ result is returned, so the client provides maximum flexibility.
 * <p/>
 * SOLR updating is not supported through this client, since this is not an intended use of the LMF search cores.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SearchClient {

    private static Logger log = LoggerFactory.getLogger(CoresClient.class);

    private static final String URL_SOLR_SERVICE  = "/solr";

    private ClientConfiguration config;

    Set<String> cores;
    
    public SearchClient(ClientConfiguration config) {
        this.config = config;
        
        CoresClient coresClient = new CoresClient(config);
        cores = new HashSet<String>();
        try {
            cores.addAll(coresClient.listCores());
        } catch (IOException e) {
            log.error("could not initialise list of cores; search functionality will not work",e);
        } catch (LMFClientException e) {
            log.error("could not initialise list of cores; search functionality will not work", e);
        }
    }

    /**
     * Run a SOLR search against the selected core and return the result as SolrDocumentList.
     *
     * @param coreName name of the core to query
     * @param query    the SolrQuery to run on the core
     * @return
     * @throws IOException
     * @throws LMFClientException
     */
    public SolrDocumentList search(String coreName, SolrQuery query) throws IOException, LMFClientException {
        Preconditions.checkArgument(cores.contains(coreName),"core {} does not exist",coreName);

        SolrServer server = new HttpSolrServer(config.getLmfUri()+URL_SOLR_SERVICE+"/"+ URLEncoder.encode(coreName,"utf-8"));

        try {
            QueryResponse response = server.query(query);
            return response.getResults();
        } catch (SolrServerException e) {
            log.error("error while evaluating SOLR query",e);
            throw new LMFClientException("error while evaluating SOLR query",e);
        }

    }


    /**
     * Perform a simple string search on the given core using default parameters for the SolrQuery.
     */
    public SolrDocumentList simpleSearch(String coreName, String queryString, Map<String,String> options) throws IOException, LMFClientException {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryString);
        if(options != null && options.containsKey("fields")) {
            query.setFields(options.get("fields"));
        } else {
            query.setFields("*,score");
        }

        if(options != null && options.containsKey("sort")) {
            query.addSortField(options.get("sort"), SolrQuery.ORDER.desc);
        } else {
            query.addSortField("score", SolrQuery.ORDER.desc);
        }

        if(options != null && options.containsKey("facets")) {
            for(String facet : options.get("facets").split(",")) {
                query.addFacetField(facet);
            }
        }
        if(options != null && options.containsKey("offset")) {
            query.setStart(Integer.parseInt(options.get("offset")));
        }
        if(options != null && options.containsKey("limit")) {
            query.setRows(Integer.parseInt(options.get("limit")));
        }
        return search(coreName,query);

    }


    /**
     * Retrieve recommendations for the given URI using the SOLR moreLikeThis handler for the core passed as first argument.
     * The fieldWeights map field names to weights, where 1 is the standard weight. Can be used to improve the significance of
     * a field in the calculation of recommendations.
     *
     * @param coreName
     * @param uri
     * @param fieldWeights
     * @return
     * @throws IOException
     * @throws LMFClientException
     */
    public SolrDocumentList recommendations(String coreName, String uri, Map<String,Double> fieldWeights) throws IOException, LMFClientException {
        SolrQuery query = new SolrQuery();
        query.setQuery("uri:\""+URLEncoder.encode(uri,"utf-8")+"\"");
        query.setFields("*,score");
        query.addSortField("score", SolrQuery.ORDER.desc);
        query.setQueryType("/" + MoreLikeThisParams.MLT);
        query.set(MoreLikeThisParams.MATCH_INCLUDE, false);
        query.set(MoreLikeThisParams.MIN_DOC_FREQ, 1);
        query.set(MoreLikeThisParams.MIN_TERM_FREQ, 1);
        query.set(MoreLikeThisParams.SIMILARITY_FIELDS, KiWiCollections.fold(fieldWeights.keySet(),","));
        query.set(MoreLikeThisParams.QF, KiWiCollections.fold(Collections2.transform(fieldWeights.entrySet(), new Function<Map.Entry<String, Double>, Object>() {
            @Override
            public Object apply(Map.Entry<String, Double> input) {
                return input.getKey()+"^"+input.getValue();
            }
        })," "));
        return search(coreName,query);
    }
}
