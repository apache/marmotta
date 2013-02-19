<?php
/*
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
namespace LMFClient\Clients;

require_once 'vendor/.composer/autoload.php';

use \LMFClient\ClientConfiguration;
use \LMFClient\Exceptions\LMFClientException;

use Guzzle\Http\Client;
use Guzzle\Http\Message\BadResponseException;


/**
 * Created by IntelliJ IDEA.
 * User: sschaffe
 * Date: 25.01.12
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
class ClassificationClient
{
    protected $config;

    private static $URL_CLASSIFICATION_SERVICE = "/classifier";

    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;
    }



    /**
     * Create a new classifier with the given name. The service will take care of creating the appropriate
     * configuration entries and work files in the LMF work directory.
     *
     * @param name string a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     */
    public function createClassifier($name) {
        try {
            $client = new Client();
            $request = $client->post($this->getServiceUrl("/" . urlencode($name)),array(
                "User-Agent"   => "LMF Client Library (PHP)",
                "Content-Length" => "0",
                "Content-Type" => "application/json"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $request->send();

        } catch(BadResponseException $ex) {
            throw new LMFClientException("could not create classifier with name $name; ".$ex->getResponse()->getReasonPhrase());
        }
    }


    /**
     * Remove the classifier with the given name from the system configuration.
     *
     * @param name       string a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     * @param removeData bool also remove all training and model data of this classifier from the file system
     */
    public function removeClassifier($name, $removeData = false)  {
        try {
            $client = new Client();
            $request = $client->delete($this->getServiceUrl("/" . urlencode($name)),array(
                "User-Agent"   => "LMF Client Library (PHP)",
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

        } catch(BadResponseException $ex) {
            throw new LMFClientException("could not delete classifier $name; ".$ex->getResponse()->getReasonPhrase());
        }
    }


    /**
     * List all classifiers registered in the classification service.
     *
     * @return a collection of Classifier instances representing all registered classifiers
     */
    public function listClassifiers() {
        try {
            $client = new Client();
            $request = $client->get($this->getServiceUrl("/list") ,array(
                "User-Agent"   => "LMF Client Library (PHP)",
                "Accept"       => "application/json"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

            $result = json_decode($response->getBody(true),true);

            return $result;
        } catch(BadResponseException $ex) {
            throw new LMFClientException("could not list classifiers; ".$ex->getResponse()->getReasonPhrase());
        }
    }


    /**
     * Add training data to the classifier identified by the given name and for the concept passed as argument. Note
     * that training data is not immediately taken into account by the classifier. Retraining of the classifier will
     * take place when a certain threshold of training datasets has been added or when a certain (configurable) time has
     * passed.
     *
     * @param name        string a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     * @param concept_uri string the URI of the concept which to train with the sample text
     * @param sampleText  string the sample text for the concept
     */
    public function trainClassifier($name, $concept_uri, $sampleText) {
        try {
            $client = new Client();
            $request = $client->post($this->getServiceUrl("/" . urlencode($name) . "/train?concept=" . urlencode($concept_uri)),array(
                "User-Agent"   => "LMF Client Library (PHP)",
                "Content-Type" => "text/plain"
            ), $sampleText);
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

        } catch(BadResponseException $ex) {
            throw new LMFClientException("could not train classifier $name; ".$ex->getResponse()->getReasonPhrase());
        }

    }



    /**
     * Retrain the classifier with the given name immediately. Will read in the training data and create a new
     * classification model.
     *
     * @param name
     *
     * @throws LMFClientException
     */
    public function retrainClassifier($name) {
        try {
            $client = new Client();
            $request = $client->post($this->getServiceUrl("/" . urlencode($name) . "/retrain"),array(
                "User-Agent"   => "LMF Client Library (PHP)",
                "Content-Length" => "0",
                "Content-Type" => "application/json"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

        } catch(BadResponseException $ex) {
            throw new LMFClientException("could not retrain classifier $name; ".$ex->getResponse()->getReasonPhrase());
        }
    }


    /**
     * Get classifications from the given classifier for the given text. The classifications will be ordered by
     * descending probability, so that classifications with higher probability will be first. A classification object
     * consists of a KiWiUriResource identifying the classified concept and a probability indicating how likely it is
     * that the text matches the given concept.
     *
     * @param classifier string a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
     * @param text       string the text to classify
     * @return a list of classifications ordered by descending probability
     */
    public function getAllClassifications($classifier, $text, $threshold = 0.0) {
        try {
            $client = new Client();
            $request = $client->post($this->getServiceUrl("/" . urlencode($classifier) . "/classify?threshold=" . $threshold),array(
                "User-Agent"   => "LMF Client Library (PHP)",
                "Content-Type" => "text/plain"
            ), $text);
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

            return json_decode($response->getBody(true),true);

        } catch(BadResponseException $ex) {
            throw new LMFClientException("could not execute classifier $classifier; ".$ex->getResponse()->getReasonPhrase());
        }
    }


    private function getServiceUrl($suffix) {
        $serviceUrl = $this->config->getBaseUrl() . ClassificationClient::$URL_CLASSIFICATION_SERVICE . $suffix;
        return $serviceUrl;
    }

}
