<?php
/*
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
namespace LMFClient\Clients;

require_once 'vendor/.composer/autoload.php';
require_once 'model/content/Content.php';
require_once 'exceptions/LMFClientException.php';
require_once 'exceptions/NotFoundException.php';
require_once 'exceptions/ContentFormatException.php';

use \LMFClient\Model\Content\Content;
use \LMFClient\ClientConfiguration;

use \LMFClient\Exceptions\LMFClientException;
use \LMFClient\Exceptions\NotFoundException;
use \LMFClient\Exceptions\ContentFormatException;

use LMFClient\Model\RDF\Literal;
use LMFClient\Model\RDF\URI;
use LMFClient\Model\RDF\BNode;


use Guzzle\Http\Client;
use Guzzle\Http\Message\BadResponseException;


/**
 * A client allowing to retrieve and configure the SOLR cores that are registered in the LMF server.
 * User: sschaffe
 * Date: 27.01.12
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */
class CoresClient
{
    private static $URL_CORES_SERVICE  = "/solr/cores";

    protected $config;

    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;
    }


    /**
     * Retrieve a list of all core names registered and activated in the LMF server.
     *
     * @return array of core names
     * @throws IOException
     * @throws LMFClientException
     */
    public function listCores() {
        $client = new Client();
        $request = $client->get($this->getServiceUrl(),array(
            "User-Agent"   => "LMF Client Library (PHP)",
            "Accept" => "application/json"
        ));
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();

        return json_decode($response->getBody(true),true);
    }

    /**
     * Return the LDPath program configured for the core with the name passed as argument.
     *
     * Note that this library provides no further functionality for evaluating LDPath programs. You may use the
     * separate LDPath libraries at http://code.google.com/p/ldpath/.
     *
     * @param coreName name of the core for which to retrieve the LDPath program
     * @return
     * @throws IOException
     * @throws LMFClientException
     */
    public function getCoreConfiguration($coreName) {
        $client = new Client();
        $request = $client->get($this->getServiceUrl($coreName),array(
            "User-Agent"   => "LMF Client Library (PHP)",
            "Accept" => "text/plain"
        ));
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();

        return $response->getBody(true);
    }

    /**
     * Create the core configuration for the given core using the LDPath program passed  as argument.
     *
     * Note that this library provides no further functionality for evaluating LDPath programs. You may use the
     * separate LDPath libraries at http://code.google.com/p/ldpath/.
     *
     * @param coreName     the name of the core to update
     * @param coreProgram  the LDPath program to use as core configuration
     * @throws IOException
     * @throws LMFClientException
     */
    public function createCoreConfiguration($coreName, $coreProgram) {
        $client = new Client();
        $request = $client->post($this->getServiceUrl($coreName),array(
            "User-Agent"   => "LMF Client Library (PHP)",
            "Content-Type" => "text/plain"
        ),$coreProgram);
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();
    }


    /**
     * Update the core configuration for the given core using the LDPath program passed  as argument.
     *
     * Note that this library provides no further functionality for evaluating LDPath programs. You may use the
     * separate LDPath libraries at http://code.google.com/p/ldpath/.
     *
     * @param coreName     the name of the core to update
     * @param coreProgram  the LDPath program to use as core configuration
     * @throws IOException
     * @throws LMFClientException
     */
    public function updateCoreConfiguration($coreName, $coreProgram) {
        $client = new Client();
        $request = $client->put($this->getServiceUrl($coreName),array(
            "User-Agent"   => "LMF Client Library (PHP)",
            "Content-Type" => "text/plain"
        ),$coreProgram);
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();
    }

    /**
     * Remove the core with the name passed as argument.
     *
     * @param coreName   name of the core to delete
     * @throws IOException
     * @throws NotFoundException  in case the core with this name does not exist
     * @throws LMFClientException
     */
    public function deleteCore($coreName) {
        $client = new Client();
        $request = $client->delete($this->getServiceUrl($coreName),array(
            "User-Agent"   => "LMF Client Library (PHP)"
        ));
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();
    }


    private function getServiceUrl($suffix = 0) {
        $serviceUrl = $this->config->getBaseUrl() . CoresClient::$URL_CORES_SERVICE .
            ($suffix ? "/" . urlencode($suffix) : "");
        return $serviceUrl;
    }

}
