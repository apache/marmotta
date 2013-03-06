package org.apache.marmotta.ldclient.provider.facebook;

import org.apache.http.client.utils.URIUtils;
import org.apache.marmotta.commons.http.UriUtil;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.FOAF;
import org.apache.marmotta.commons.vocabulary.SCHEMA;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.services.provider.AbstractHttpProvider;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A provider that accesses objects exposed by the Facebook Graph API (in JSON format). The provider will map the
 * properties of Facebook Objects to RDF and tries choosing the most appropriate RDF types and vocabularies. The base
 * vocabulary used for describing objects is dcterms; the rdf types of objects will be taken from schema.org.
 * <p/>
 * @see    <a href="http://developers.facebook.com/docs/reference/api/">Facebook Graph API</a>
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class FacebookGraphProvider extends AbstractHttpProvider {

    public static final String PROVIDER_NAME = "Facebook Graph API";


    private static final Pattern pattern = Pattern.compile("http://www\\.facebook\\.com/([^/]+/)*([^/]+)");

    private static Logger log = LoggerFactory.getLogger(FacebookGraphProvider.class);


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
     * Build the URL to use to call the webservice in order to retrieve the data for the resource passed as argument.
     * In many cases, this will just return the URI of the resource (e.g. Linked Data), but there might be data providers
     * that use different means for accessing the data for a resource, e.g. SPARQL or a Cache.
     *
     * @param resourceUri
     * @param endpoint    endpoint configuration for the data provider (optional)
     * @return
     */
    @Override
    protected List<String> buildRequestUrl(String resourceUri, Endpoint endpoint) throws DataRetrievalException {
        log.info("retrieving Facebook Graph object {}",resourceUri);

        // if a URI starts with http://www.facebook.com we redirect to the object in the Graph API at http://graph.facebook.com
        Matcher matcher = pattern.matcher(resourceUri);
        if(matcher.matches()) {
            String requestUri = "http://graph.facebook.com/"+matcher.group(2);

            log.info("... redirecting to {}",resourceUri);

            return Collections.singletonList(requestUri);
        } else {
            return Collections.singletonList(resourceUri);
        }

    }

    /**
     * Parse the HTTP response entity returned by the web service call and return its contents in a Sesame RDF
     * repository also passed as argument. The content type returned by the web service is passed as argument to help
     * the implementation decide how to parse the data. The implementation can return a list of additional pages to
     * retrieve for completing the data of the resource
     *
     * @param resourceUri
     * @param repository  an RDF repository for storing an RDF representation of the dataset located at the remote resource.
     * @param in          input stream as returned by the remote webservice
     * @param contentType content type as returned in the HTTP headers of the remote webservice
     * @return a possibly empty list of URLs of additional resources to retrieve to complete the content
     * @throws java.io.IOException in case an error occurs while reading the input stream
     */
    @Override
    protected List<String> parseResponse(String resourceUri, String requestUrl, Repository repository, InputStream in, String contentType) throws DataRetrievalException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String,Object> data = mapper.readValue(in, new TypeReference<Map<String,Object>>() { });

            RepositoryConnection con = repository.getConnection();
            try {
                con.begin();

                ValueFactory vf = repository.getValueFactory();

                URI subject = vf.createURI(resourceUri);

                // add the type based on the facebook category
                if(data.get("category") != null) {
                    con.add(subject, RDF.TYPE, getType(data.get("category").toString()));
                }

                con.add(subject,DCTERMS.identifier,vf.createLiteral(data.get("id").toString()));

                // schema:name is the facebook name
                con.add(subject, SCHEMA.name, vf.createLiteral(data.get("name").toString()));
                con.add(subject, DCTERMS.title, vf.createLiteral(data.get("name").toString()));

                // dct:description in case a description or about is present
                if(data.get("description") != null) {
                    con.add(subject,SCHEMA.description, vf.createLiteral(data.get("description").toString()));
                    con.add(subject,DCTERMS.description, vf.createLiteral(data.get("description").toString()));
                }
                if(data.get("about") != null) {
                    con.add(subject,SCHEMA.description, vf.createLiteral(data.get("about").toString()));
                    con.add(subject,DCTERMS.description, vf.createLiteral(data.get("about").toString()));
                }

                // if there is genre information, add it using schema:genre and dct:subject
                if(data.get("genre") != null) {
                    con.add(subject,SCHEMA.genre, vf.createLiteral(data.get("genre").toString()));
                    con.add(subject,DCTERMS.subject, vf.createLiteral(data.get("genre").toString()));
                }
                if(data.get("directed_by") != null) {
                    con.add(subject,SCHEMA.director, vf.createLiteral(data.get("directed_by").toString()));
                    con.add(subject,DCTERMS.creator, vf.createLiteral(data.get("directed_by").toString()));
                }
                if(data.get("studio") != null) {
                    con.add(subject,SCHEMA.publisher, vf.createLiteral(data.get("studio").toString()));
                    con.add(subject,DCTERMS.publisher, vf.createLiteral(data.get("studio").toString()));
                }
                if(data.get("plot_outline") != null) {
                    con.add(subject,SCHEMA.description, vf.createLiteral(data.get("plot_outline").toString()));
                    con.add(subject,DCTERMS.description, vf.createLiteral(data.get("plot_outline").toString()));
                }
                if(data.get("phone") != null) {
                    con.add(subject,SCHEMA.telephone, vf.createLiteral(data.get("phone").toString()));
                    con.add(subject,FOAF.phone, vf.createLiteral(data.get("phone").toString()));
                }
                if(data.get("username") != null) {
                    con.add(subject,FOAF.nick, vf.createLiteral(data.get("username").toString()));
                }

                if(data.get("cover") != null && data.get("cover") instanceof Map && ((Map)data.get("cover")).get("source") != null) {
                    con.add(subject,FOAF.thumbnail, vf.createURI(((Map) data.get("cover")).get("source").toString()));
                }



                // website
                if(data.get("website") != null && UriUtil.validate(data.get("website").toString())) {
                    con.add(subject, FOAF.homepage, vf.createURI(data.get("website").toString()));
                }
                if(data.get("link") != null) {
                    con.add(subject, FOAF.homepage, vf.createURI(data.get("link").toString()));
                }

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } finally {
                con.close();
            }
        } catch(RepositoryException ex) {
            log.error("error while storing retrieved triples in repository",ex);
        } catch (JsonMappingException e) {
            throw new DataRetrievalException("error while mapping JSON response",e);
        } catch (JsonParseException e) {
            throw new DataRetrievalException("error while parsing JSON response",e);
        } catch (IOException e) {
            throw new DataRetrievalException("error while accessing Facebook Graph API",e);
        }

        return Collections.EMPTY_LIST;
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
}
