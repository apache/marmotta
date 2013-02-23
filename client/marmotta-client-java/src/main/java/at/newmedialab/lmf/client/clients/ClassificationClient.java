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
package at.newmedialab.lmf.client.clients;

import at.newmedialab.lmf.client.ClientConfiguration;
import at.newmedialab.lmf.client.exception.LMFClientException;
import at.newmedialab.lmf.client.model.classification.Classification;
import at.newmedialab.lmf.client.model.rdf.URI;
import at.newmedialab.lmf.client.util.HTTPUtil;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A client supporting access to the LMF Classification Services. Allows creating and removing classifiers as
 * well as training classifiers with sample data and the classification of textual documents.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class ClassificationClient {

    private static Logger log = LoggerFactory.getLogger(ClassificationClient.class);

    private static final String URL_CLASSIFICATION_SERVICE = "/classifier";


    private ClientConfiguration config;

    public ClassificationClient(ClientConfiguration config) {
        this.config = config;
    }


    /**
     * Create a new classifier with the given name. The service will take care of creating the appropriate
     * configuration entries and work files in the LMF work directory.
     *
     * @param name a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     */
    public boolean createClassifier(String name) throws LMFClientException, IOException {
        Preconditions.checkArgument(name.matches("^\\p{Alnum}+$"));

        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpPost post = new HttpPost(config.getLmfUri() + URL_CLASSIFICATION_SERVICE + "/" + name);
        
        try {

            HttpResponse response = httpClient.execute(post);

            switch(response.getStatusLine().getStatusCode()) {
                case 403:
                    log.debug("classifier {} already existed, not creating new",name);
                    return true;
                case 200:
                    log.debug("classifier {} created",name);
                    return true;
                default:
                    log.error("error creating classifier {}: {} {}",new Object[] {name,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    return true;
            }

        } catch (UnsupportedEncodingException e) {
            log.error("could not encode URI parameter",e);
            return false;
        } finally {
            post.releaseConnection();
        }
    }


    /**
     * Remove the classifier with the given name from the system configuration.
     *
     * @param name       a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     * @param removeData also remove all training and model data of this classifier from the file system
     */
    public void removeClassifier(String name, boolean removeData) throws LMFClientException, IOException {
        Preconditions.checkArgument(name.matches("^\\p{Alnum}+$"));

        HttpClient httpClient = HTTPUtil.createClient(config);

        HttpDelete delete = new HttpDelete(config.getLmfUri() + URL_CLASSIFICATION_SERVICE + "/" + name + (removeData?"?removeData=true":""));
            
            try {
            
            HttpResponse response = httpClient.execute(delete);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("classifier {} deleted", name);
                    break;
                case 404:
                    log.error("classifier {} does not exist, cannot delete", name);
                    break;
                default:
                    log.error("error deleting classifier {}: {} {}",new Object[] {name,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
            }

        } catch (UnsupportedEncodingException e) {
            delete.abort();
            log.error("could not encode URI parameter",e);
        } finally {
            delete.releaseConnection();
        }
    }


    /**
     * List all classifiers registered in the classification service.
     *
     * @return a collection of Classifier instances representing all registered classifiers
     */
    public Collection<String> listClassifiers() throws IOException, LMFClientException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CLASSIFICATION_SERVICE + "/list";

        HttpGet get = new HttpGet(serviceUrl);
        get.setHeader("Accept", "application/json");

        try {
            
            HttpResponse response = httpClient.execute(get);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("classifiers listed successfully");
                    ObjectMapper mapper = new ObjectMapper();
                    List<String> result = mapper.readValue(response.getEntity().getContent(),new TypeReference<List<String>>(){});
                    
                    return result;
                default:
                    log.error("error retrieving list of classifiers: {} {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error retrieving list of classifiers: "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            get.releaseConnection();
        }
    }


    /**
     * Add training data to the classifier identified by the given name and for the concept passed as argument. Note
     * that training data is not immediately taken into account by the classifier. Retraining of the classifier will
     * take place when a certain threshold of training datasets has been added or when a certain (configurable) time has
     * passed.
     *
     * @param name        a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     * @param concept_uri the URI of the concept which to train with the sample text
     * @param sampleText  the sample text for the concept
     */
    public void trainClassifier(String name, String concept_uri, final String sampleText) throws LMFClientException, IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CLASSIFICATION_SERVICE + "/" + URLEncoder.encode(name,"utf-8") + "/train?concept=" + URLEncoder.encode(concept_uri,"utf-8");

        HttpPost post = new HttpPost(serviceUrl);
        post.setHeader("Content-Type", "text/plain");
        
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                ByteStreams.copy(new ByteArrayInputStream(sampleText.getBytes("utf-8")), outstream);
            }
        };
        
        post.setEntity(new EntityTemplate(cp));
        
        try {
                
            HttpResponse response = httpClient.execute(post);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("classifier {} updated successfully",name);
                    break;
                default:
                    log.error("error updating classifier {}: {} {}",new Object[] {name,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error updating classifier "+name+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            post.releaseConnection();
        }
    }


    /**
     * Retrain the classifier with the given name immediately. Will read in the training data and create a new
     * classification model.
     *
     * @param name
     * @throws LMFClientException
     */
    public void retrainClassifier(String name) throws LMFClientException, IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CLASSIFICATION_SERVICE + "/" + URLEncoder.encode(name,"utf-8") + "/retrain";

        HttpPost post = new HttpPost(serviceUrl);
        
        try {
            
            HttpResponse response = httpClient.execute(post);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("classifier {} retrained successfully",name);
                    break;
                default:
                    log.error("error retraining classifier {}: {} {}",new Object[] {name,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error updating classifier "+name+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            post.releaseConnection();
        }
    }


    /**
     * Get classifications from the given classifier for the given text. The classifications will be ordered by
     * descending probability, so that classifications with higher probability will be first. A classification object
     * consists of a KiWiUriResource identifying the classified concept and a probability indicating how likely it is
     * that the text matches the given concept.
     *
     * @param classifier a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     * @param text       the text to classify
     * @return a list of classifications ordered by descending probability
     */
    public List<Classification> getAllClassifications(String classifier, final String text) throws LMFClientException, IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CLASSIFICATION_SERVICE + "/" + classifier+"/classify";

        HttpPost post = new HttpPost(serviceUrl);
        post.setHeader("Content-Type", "text/plain");
        
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                ByteStreams.copy(new ByteArrayInputStream(text.getBytes("utf-8")), outstream);
            }
        };
        
        post.setEntity(new EntityTemplate(cp));
        
        try {
                
            HttpResponse response = httpClient.execute(post);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("classification {} executed successfully",classifier);
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String,String>> jsonResult =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<List<Map<String,String>>>(){});

                    List<Classification> result = new LinkedList<Classification>();
                    for(Map<String,String> entry : jsonResult) {
                        result.add(new Classification(new URI(entry.get("concept")), Double.parseDouble(entry.get("probability"))));
                    }
                    return result;
                default:
                    log.error("error executing classifier {}: {} {}",new Object[] {classifier,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error executing classifier "+classifier+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            post.releaseConnection();
        }
    }


    /**
     * Get classifications from the given classifier for the given text. The classifications will be ordered by
     * descending probability, so that classifications with higher probability will be first. Only classifications with
     * a probability higher than the threshold will be considered. A classification object
     * consists of a KiWiUriResource identifying the classified concept and a probability indicating how likely it is
     * that the text matches the given concept.
     *
     * @param classifier a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     * @param text       the text to classify
     * @param threshold  the minimum probability of a classification to be considered in the result
     * @return a list of classifications ordered by descending probability, all having higher probability than threshold
     */
    public List<Classification> getAllClassifications(String classifier, final String text, double threshold) throws LMFClientException, IOException {
        HttpClient httpClient = HTTPUtil.createClient(config);

        String serviceUrl = config.getLmfUri() + URL_CLASSIFICATION_SERVICE + "/" + classifier+"/classify?threshold="+threshold;

        HttpPost post = new HttpPost(serviceUrl);
        post.setHeader("Content-Type", "text/plain");
        ContentProducer cp = new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                ByteStreams.copy(new ByteArrayInputStream(text.getBytes("utf-8")), outstream);
            }
        };
        post.setEntity(new EntityTemplate(cp));
        
        try {
            HttpResponse response = httpClient.execute(post);

            switch(response.getStatusLine().getStatusCode()) {
                case 200:
                    log.debug("classification {} executed successfully",classifier);
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String,String>> jsonResult =
                            mapper.readValue(response.getEntity().getContent(),new TypeReference<List<Map<String,String>>>(){});

                    List<Classification> result = new LinkedList<Classification>();
                    for(Map<String,String> entry : jsonResult) {
                        result.add(new Classification(new URI(entry.get("concept")), Double.parseDouble(entry.get("probability"))));
                    }
                    return result;
                default:
                    log.error("error executing classifier {}: {} {}",new Object[] {classifier,response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase()});
                    throw new LMFClientException("error executing classifier "+classifier+": "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            post.releaseConnection();
        }
    }

}
