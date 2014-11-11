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

package org.apache.marmotta.ldclient.provider.facebook;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.util.EntityUtils;
import org.apache.marmotta.commons.http.UriUtil;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.commons.vocabulary.SCHEMA;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.ACCEPT_LANGUAGE;

/**
 * A provider that accesses objects exposed by the Facebook Graph API (in JSON format). The provider will map the
 * properties of Facebook Objects to RDF and tries choosing the most appropriate RDF types and vocabularies. The base
 * vocabulary used for describing objects is dcterms; the rdf types of objects will be taken from schema.org.
 * <p/>
 * @see    <a href="http://developers.facebook.com/docs/reference/api/">Facebook Graph API</a>
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class FacebookGraphProvider implements DataProvider {

    public static final String PROVIDER_NAME = "Facebook Graph API";


    private static final Pattern pattern = Pattern.compile("http://www\\.facebook\\.com/([^/]+/)*([^/]+)");
    public static final int RETRY_AFTER = 60;

    private static Logger log = LoggerFactory.getLogger(FacebookGraphProvider.class);

    private static String[] defaultLanguages = new String[] {"en", "de", "fr", "es", "it"};

    private static Map<String,URI> facebookCategories = new HashMap<String, URI>();
    static {
        // see http://www.marketinggum.com/types-of-facebook-pages-for-business/

        // Local Business or Place
        facebookCategories.put("attractions/things to do", SCHEMA.TouristAttraction);
        facebookCategories.put("bank", SCHEMA.BankOrCreditUnion);
        facebookCategories.put("bar", SCHEMA.BarOrPub);
        facebookCategories.put("book store", SCHEMA.BookStore);
        facebookCategories.put("concert venue", SCHEMA.MusicVenue);
        facebookCategories.put("food/grocery", SCHEMA.GroceryStore);
        facebookCategories.put("hotel", SCHEMA.Hotel);
        facebookCategories.put("local business", SCHEMA.LocalBusiness);
        facebookCategories.put("movie theatre", SCHEMA.MovieTheater);
        facebookCategories.put("museum/art gallery", SCHEMA.Museum);
        facebookCategories.put("outdoor gear/sporting goods", SCHEMA.SportingGoodsStore);
        facebookCategories.put("real estate", SCHEMA.RealEstateAgent);
        facebookCategories.put("restaurant/café", SCHEMA.CafeOrCoffeeShop);
        facebookCategories.put("arts/entertainment/nightlife", SCHEMA.NightClub);
        facebookCategories.put("school", SCHEMA.School);


        // Company Organization or Institution
        facebookCategories.put("automobiles and parts", SCHEMA.AutoPartsStore);
        facebookCategories.put("church", SCHEMA.Church);
        facebookCategories.put("company", SCHEMA.Corporation);
        facebookCategories.put("computers/technology", SCHEMA.ComputerStore);
        facebookCategories.put("consulting/business services", SCHEMA.Corporation);
        facebookCategories.put("insurance company", SCHEMA.InsuranceAgency);
        facebookCategories.put("internet/software", SCHEMA.SoftwareApplication);
        facebookCategories.put("legal/law", SCHEMA.Attorney);
        facebookCategories.put("retail and consumer merchandise", SCHEMA.ConvenienceStore);
        facebookCategories.put("media/news/publishing", SCHEMA.Corporation);
        facebookCategories.put("travel/leisure", SCHEMA.TravelAgency);


        // Brand or Product
        facebookCategories.put("app", SCHEMA.MobileApplication);
        facebookCategories.put("appliance", SCHEMA.Product);
        facebookCategories.put("baby goods/kids goods", SCHEMA.Product);
        facebookCategories.put("cars", SCHEMA.AutoPartsStore);
        facebookCategories.put("clothing", SCHEMA.ClothingStore);
        facebookCategories.put("electronics", SCHEMA.ElectronicsStore);
        facebookCategories.put("food/beverages", SCHEMA.FoodEstablishment);
        facebookCategories.put("furniture", SCHEMA.FurnitureStore);
        facebookCategories.put("games/toys", SCHEMA.ToyStore);
        facebookCategories.put("health/beauty", SCHEMA.BeautySalon);
        facebookCategories.put("jewelry/watches", SCHEMA.JewelryStore);
        facebookCategories.put("kitchen/cooking", SCHEMA.HomeGoodsStore);
        facebookCategories.put("pet supplies", SCHEMA.PetStore);
        facebookCategories.put("vitamins/minerals", SCHEMA.Product);



        // Artist, Band or Public Figure
        facebookCategories.put("actor/director", SCHEMA.Person);
        facebookCategories.put("artist", SCHEMA.Person);
        facebookCategories.put("athlete", SCHEMA.Person);
        facebookCategories.put("author", SCHEMA.Person);
        facebookCategories.put("business person", SCHEMA.Person);
        facebookCategories.put("chef", SCHEMA.Person);
        facebookCategories.put("coach", SCHEMA.Person);
        facebookCategories.put("doctor", SCHEMA.Person);
        facebookCategories.put("entertainer", SCHEMA.Person);
        facebookCategories.put("journalist", SCHEMA.Person);
        facebookCategories.put("lawyer", SCHEMA.Person);
        facebookCategories.put("musician/band", SCHEMA.MusicGroup);
        facebookCategories.put("politician", SCHEMA.Person);
        facebookCategories.put("teacher", SCHEMA.Person);
        facebookCategories.put("writer", SCHEMA.Person);


        // entertainment
        facebookCategories.put("movie",SCHEMA.Movie);
        facebookCategories.put("album",SCHEMA.MusicAlbum);
        facebookCategories.put("book", SCHEMA.Book);
        facebookCategories.put("concert tour", SCHEMA.MusicEvent);
        facebookCategories.put("library", SCHEMA.Library);
        facebookCategories.put("magazine", SCHEMA.CreativeWork);
        facebookCategories.put("radio station", SCHEMA.RadioStation);
        facebookCategories.put("record label", SCHEMA.Brand);
        facebookCategories.put("sports venue", SCHEMA.SportsActivityLocation);
        facebookCategories.put("tv channel", SCHEMA.TelevisionStation);
        facebookCategories.put("tv show", SCHEMA.TVSeries);

        // NGO
        facebookCategories.put("non-profit organization", SCHEMA.NGO);
        facebookCategories.put("cause", SCHEMA.NGO);
        facebookCategories.put("health/medical/pharmaceuticals", SCHEMA.MedicalOrganization);
        facebookCategories.put("health/medical/pharmacy", SCHEMA.MedicalOrganization);
        facebookCategories.put("community/government", SCHEMA.GovernmentOrganization);
        facebookCategories.put("church/religious organization", SCHEMA.Church);
        facebookCategories.put("education", SCHEMA.EducationalOrganization);
        facebookCategories.put("hospital/clinic", SCHEMA.Hospital);

        // interests
        facebookCategories.put("interest", SKOS.CONCEPT);
    }

    /**
     * Parse the HTTP response entity returned by the web service call and return its contents in a Sesame RDF
     * repository also passed as argument. The content type returned by the web service is passed as argument to help
     * the implementation decide how to parse the data. The implementation can return a list of additional pages to
     * retrieve for completing the data of the resource
     *
     * @param resourceUri
     * @param model       an RDF model for storing an RDF representation of the dataset located at the remote resource.
     * @param in          input stream as returned by the remote webservice
     * @param language    content language as returned in the HTTP headers of the remote webservice
     * @return a possibly empty list of URLs of additional resources to retrieve to complete the content
     * @throws java.io.IOException in case an error occurs while reading the input stream
     */
    protected List<String> parseResponse(String resourceUri, String requestUrl, Model model, InputStream in, String language) throws DataRetrievalException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String,Object> data = mapper.readValue(in, new TypeReference<Map<String,Object>>() { });

            ValueFactory vf = ValueFactoryImpl.getInstance();

            URI subject = vf.createURI(resourceUri);

            // add the type based on the facebook category
            if(data.get("category") != null) {
                model.add(subject, RDF.TYPE, getType(data.get("category").toString()));
            }

            model.add(subject,DCTERMS.identifier,vf.createLiteral(data.get("id").toString()));

            // schema:name is the facebook name (can have multiple languages)
            model.add(subject, SCHEMA.name, vf.createLiteral(data.get("name").toString(), language));
            model.add(subject, DCTERMS.title, vf.createLiteral(data.get("name").toString(), language));

            // dct:description in case a description or about is present (all content in English)
            if(data.get("description") != null) {
                model.add(subject,SCHEMA.description, vf.createLiteral(data.get("description").toString(), "en"));
                model.add(subject,DCTERMS.description, vf.createLiteral(data.get("description").toString(), "en"));
            }
            if(data.get("about") != null) {
                model.add(subject,SCHEMA.description, vf.createLiteral(data.get("about").toString(), "en"));
                model.add(subject,DCTERMS.description, vf.createLiteral(data.get("about").toString(), "en"));
            }

            // if there is genre information, add it using schema:genre and dct:subject
            if(data.get("genre") != null) {
                model.add(subject,SCHEMA.genre, vf.createLiteral(data.get("genre").toString()));
                model.add(subject,DCTERMS.subject, vf.createLiteral(data.get("genre").toString()));
            }
            if(data.get("directed_by") != null) {
                model.add(subject,SCHEMA.director, vf.createLiteral(data.get("directed_by").toString()));
                model.add(subject,DCTERMS.creator, vf.createLiteral(data.get("directed_by").toString()));
            }
            if(data.get("studio") != null) {
                model.add(subject,SCHEMA.publisher, vf.createLiteral(data.get("studio").toString()));
                model.add(subject,DCTERMS.publisher, vf.createLiteral(data.get("studio").toString()));
            }
            if(data.get("plot_outline") != null) {
                model.add(subject,SCHEMA.description, vf.createLiteral(data.get("plot_outline").toString()));
                model.add(subject,DCTERMS.description, vf.createLiteral(data.get("plot_outline").toString()));
            }
            if(data.get("phone") != null) {
                model.add(subject,SCHEMA.telephone, vf.createLiteral(data.get("phone").toString()));
                model.add(subject,FOAF.phone, vf.createLiteral(data.get("phone").toString()));
            }
            if(data.get("username") != null) {
                model.add(subject,FOAF.nick, vf.createLiteral(data.get("username").toString()));
            }

            if(data.get("cover") != null && data.get("cover") instanceof Map && ((Map<?,?>)data.get("cover")).get("source") != null) {
                model.add(subject,FOAF.thumbnail, vf.createURI(((Map<?,?>) data.get("cover")).get("source").toString()));
            }



            // website
            if(data.get("website") != null && UriUtil.validate(data.get("website").toString())) {
                model.add(subject, FOAF.homepage, vf.createURI(data.get("website").toString()));
            }
            if(data.get("link") != null) {
                model.add(subject, FOAF.homepage, vf.createURI(data.get("link").toString()));
            }

        } catch (JsonMappingException e) {
            throw new DataRetrievalException("error while mapping JSON response",e);
        } catch (JsonParseException e) {
            throw new DataRetrievalException("error while parsing JSON response",e);
        } catch (IOException e) {
            throw new DataRetrievalException("error while accessing Facebook Graph API",e);
        }

        return Collections.emptyList();
    }

    /**
     * Return an appropriate RDF type for the given facebook category. Since this is not really documented in facebook,
     * not all categories will be supported. The fallback type is "foaf:Document".
     *
     * @param facebookCategory
     * @return
     */

    private URI getType(String facebookCategory) {
        if(facebookCategories.get(facebookCategory.toLowerCase()) != null) {
            return facebookCategories.get(facebookCategory.toLowerCase());
        } else {
            return FOAF.Document;
        }

    }


    /**
     * Return the name of this data provider. To be used e.g. in the configuration and in log messages.
     *
     * @return
     */
    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    /**
     * Return the list of mime types accepted by this data provider.
     *
     * @return
     */
    @Override
    public String[] listMimeTypes() {
        return new String[] { "application/json"};
    }




    /**
     * Retrieve the data for a Facebook resource using the given http client and endpoint definition. Since Facebook
     * returns multiple language versions for an object depending on the Accept-Language header, we will issue several
     * requests to the same URL to get all the data. If the endpoint definition contains a property "languages"
     * whose value is an array of language strings, this property is used. Otherwise it reverts to defaultLanguages.
     *
     *
     *
     * @param resourceUri the resource to be retrieved
     * @param endpoint the endpoint definition
     * @return a completely specified client response, including expiry information and the set of triples
     */
    @Override
    public ClientResponse retrieveResource(String resourceUri, LDClientService client, Endpoint endpoint) throws DataRetrievalException {

        try {

            String contentType = "application/json";

            long defaultExpires = client.getClientConfiguration().getDefaultExpiry();
            if(endpoint != null && endpoint.getDefaultExpiry() != null) {
                defaultExpires = endpoint.getDefaultExpiry();
            }

            final ResponseHandler handler = new ResponseHandler(resourceUri, endpoint);


            String requestUri;
            log.info("retrieving Facebook Graph object {}",resourceUri);

            // if a URI starts with http://www.facebook.com we redirect to the object in the Graph API at http://graph.facebook.com
            Matcher matcher = pattern.matcher(resourceUri);
            if(matcher.matches()) {
                log.info("... redirecting to {}",resourceUri);
                requestUri = "http://graph.facebook.com/"+matcher.group(2);
            } else {
                requestUri = resourceUri;
            }

            String[] languages;
            if(endpoint.getProperty("languages") != null) {
                languages = endpoint.getProperty("languages").split(",");
            } else {
                languages = defaultLanguages;
            }

            for(String lang : languages) {
                HttpGet get = new HttpGet(requestUri);
                try {
                    get.setHeader(ACCEPT, contentType);
                    get.setHeader(ACCEPT_LANGUAGE, lang);

                    log.info("retrieving resource data for {} from '{}' endpoint, request URI is <{}>", new Object[]  {resourceUri, getName(), get.getURI().toASCIIString()});

                    handler.requestUrl = requestUri;
                    handler.language   = lang;
                    client.getClient().execute(get, handler);
                } finally {
                    get.releaseConnection();
                }
            }

            Date expiresDate = handler.expiresDate;
            if (expiresDate == null) {
                expiresDate = new Date(System.currentTimeMillis() + defaultExpires * 1000);
            }

            long min_expires = System.currentTimeMillis() + client.getClientConfiguration().getMinimumExpiry() * 1000;
            if (expiresDate.getTime() < min_expires) {
                log.info("expiry time returned by request lower than minimum expiration time; using minimum time instead");
                expiresDate = new Date(min_expires);
            }

            if(log.isInfoEnabled()) {
                log.info("retrieved {} triples for resource {}; expiry date: {}", new Object[]{handler.triples.size(), resourceUri, expiresDate});
            }

            ClientResponse result = new ClientResponse(200, handler.triples);
            result.setExpires(expiresDate);
            return result;
        } catch (RepositoryException e) {
            log.error("error while initialising Sesame repository; classpath problem?",e);
            throw new DataRetrievalException("error while initialising Sesame repository; classpath problem?",e);
        } catch (ClientProtocolException e) {
            log.error("HTTP client error while trying to retrieve resource {}: {}", resourceUri, e.getMessage());
            throw new DataRetrievalException("I/O error while trying to retrieve resource "+resourceUri,e);
        } catch (IOException e) {
            log.error("I/O error while trying to retrieve resource {}: {}", resourceUri, e.getMessage());
            throw new DataRetrievalException("I/O error while trying to retrieve resource "+resourceUri,e);
        } catch(RuntimeException ex) {
            log.error("Unknown error while trying to retrieve resource {}: {}", resourceUri, ex.getMessage());
            throw new DataRetrievalException("Unknown error while trying to retrieve resource "+resourceUri,ex);
        }

    }


    protected class ResponseHandler implements org.apache.http.client.ResponseHandler<List<String>> {

        private Date             expiresDate;

        private String                requestUrl;

        // the repository where the triples will be stored in case the data providers return them
        private final Model triples;

        private final Endpoint   endpoint;

        private final String resource;

        private int httpStatus;

        // language tag to use for literals
        public String language;

        public ResponseHandler(String resource, Endpoint endpoint) throws RepositoryException {
            this.resource = resource;
            this.endpoint = endpoint;

            triples = new TreeModel();
        }

        @Override
        public List<String> handleResponse(HttpResponse response) throws IOException {
            ArrayList<String> requestUrls = new ArrayList<String>();

            if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 400) {
                final HttpEntity entity = response.getEntity();
                if (entity == null)
                    throw new IOException("no content returned by Linked Data resource " + resource);

                this.httpStatus = response.getStatusLine().getStatusCode();

                if (entity != null) {
                    InputStream in = entity.getContent();
                    try {

                        List<String> urls = parseResponse(resource, requestUrl, triples, in, language);
                        requestUrls.addAll(urls);

                        if (expiresDate == null) {
                            Header expires = response.getFirstHeader("Expires");
                            if (expires != null) {
                                expiresDate = DateUtils.parseDate(expires.getValue());
                            }
                        }

                    } catch (DataRetrievalException e) {
                        // FIXME: get.abort();
                        throw new IOException(e);
                    } finally {
                        in.close();
                    }
                }
                EntityUtils.consume(entity);
            } else if(response.getStatusLine().getStatusCode() == 500 || response.getStatusLine().getStatusCode() == 503  || response.getStatusLine().getStatusCode() == 504) {
                this.httpStatus = response.getStatusLine().getStatusCode();

                Header retry = response.getFirstHeader("Retry-After");
                if(retry != null) {
                    try {
                        int duration = Integer.parseInt(retry.getValue());
                        expiresDate = new Date(System.currentTimeMillis() + duration*1000);
                    } catch(NumberFormatException ex) {
                        log.debug("error parsing Retry-After: header");
                    }
                } else {
                    expiresDate = new Date(System.currentTimeMillis() + RETRY_AFTER *1000);
                }

            } else {
                log.error("the HTTP request failed (status: {})", response.getStatusLine());
                throw new ClientProtocolException("the HTTP request failed (status: " + response.getStatusLine() + ")");
            }

            return requestUrls;
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        public int getHttpStatus() {
            return httpStatus;
        }

    }

}
