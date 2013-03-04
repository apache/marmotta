<?php
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
namespace MarmottaClient\Clients;

require_once 'vendor/.composer/autoload.php';

use \MarmottaClient\ClientConfiguration;
use \MarmottaClient\Exceptions\MarmottaClientException;

use Guzzle\Http\Client;
use Guzzle\Http\Message\BadResponseException;


/**
 * Created by IntelliJ IDEA.
 * User: sschaffe
 * Date: 25.01.12
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
class ConfigurationClient
{
    protected $config;

    private static $URL_CONFIG_SERVICE = "/config";

    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;
    }


    /**
     * Return a list of all configuration keys that are currently set in the Marmotta configuration.
     * @return array an array containing all configuration keys
     */
    public function listConfigurationKeys()  {
        $client = new Client();
        $request = $client->get($this->getServiceUrl("/list"),array(
            "User-Agent"   => "Marmotta Client Library (PHP)",
            "Accept" => "application/json"
        ));
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();

        $result = array();
        // parse JSON response
        foreach(json_decode($response->getBody(true)) as $key => $value) {
            $result[] = $key;
        }
        return $result;
    }

    /**
     * Return a list of all configurations (keys and values) that are currently set in the Marmotta configuration.
     * @return array a map mapping all configuration keys to their values (string or array of strings)
     */
    public function listConfigurations($prefix = 0) {
        $client = new Client();
        $request = $client->get($this->getServiceUrl("/list" . ($prefix?"?prefix=".urlencode($prefix):"")),array(
            "User-Agent"   => "Marmotta Client Library (PHP)",
            "Accept" => "application/json"
        ));
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();

        $result = array();
        // parse JSON response
        foreach(json_decode($response->getBody(true),true) as $key => $value) {
            $result[$key] = $value["value"];
        }
        return $result;
    }

    /**
     * Return the configuration with the given key, or null if it does not exist
     * @param key
     * @return
     */
    public function getConfiguration($key) {
        try {
            $client = new Client();
            $request = $client->get($this->getServiceUrl("/data/" . urlencode($key)),array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Accept" => "application/json"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }

            $response = $request->send();

            $result = json_decode($response->getBody(true),true);

            return $result[$key];
        } catch(BadResponseException $ex) {
            if($ex->getResponse()->getStatusCode() == 404) {
                return null;
            } else {
                throw new MarmottaClientException("could not retrieve configuration with key $key; ".$ex->getResponse()->getReasonPhrase());
            }
        }
    }


    /**
     * Update the configuration "key" with the given value. Value can be either a list of values or one of the
     * primitive types String, Boolean, Integer, Double
     * @param key
     * @param value
     */
    public function setConfiguration($key, $value) {
        // send values always as a JSON list
        $json_data = json_encode(is_array($value)?$value:array($value));

        try {
            $client = new Client();
            $request = $client->post($this->getServiceUrl("/data/" . urlencode($key)),array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Content-Type" => "application/json"
            ),$json_data);
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $request->send();

        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("could not update configuration with key $key; ".$ex->getResponse()->getReasonPhrase());
        }
    }

    /**
     * Remove the configuration with the given key.
     *
     * @param key
     * @throws IOException
     * @throws MarmottaClientException
     */
    public function deleteConfiguration($key) {
        try {
            $client = new Client();
            $request = $client->delete($this->getServiceUrl("/data/" . urlencode($key)),array(
                "User-Agent"   => "Marmotta Client Library (PHP)"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();
        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("could not delete configuration with key $key; ".$ex->getResponse()->getReasonPhrase());
        }
    }


    private function getServiceUrl($suffix) {
        $serviceUrl = $this->config->getBaseUrl() . ConfigurationClient::$URL_CONFIG_SERVICE . $suffix;
        return $serviceUrl;
    }

}
