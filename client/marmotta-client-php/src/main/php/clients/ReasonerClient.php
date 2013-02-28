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
require_once 'model/content/Content.php';
require_once 'exceptions/MarmottaClientException.php';
require_once 'exceptions/NotFoundException.php';
require_once 'exceptions/ContentFormatException.php';

use \MarmottaClient\Model\Content\Content;
use \MarmottaClient\ClientConfiguration;

use \MarmottaClient\Exceptions\MarmottaClientException;
use \MarmottaClient\Exceptions\NotFoundException;
use \MarmottaClient\Exceptions\ContentFormatException;

use MarmottaClient\Model\RDF\Literal;
use MarmottaClient\Model\RDF\URI;
use MarmottaClient\Model\RDF\BNode;


use Guzzle\Http\Client;
use Guzzle\Http\Message\BadResponseException;

/**
 * Created by IntelliJ IDEA.
 * User: sschaffe
 * Date: 01.02.12
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */
class ReasonerClient
{

    private static $URL_PROGRAM_SERVICE = "/reasoner/program";


    protected $config;

    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;
    }


    /**
     * List all reasoner programs installed in the Marmotta knowledge base. Returns an array of maps of the form
     * array("name" => program name, "rules" => program string in KWRL syntax)
     *
     */
    public function listPrograms() {
        $client = new Client();
        $request = $client->get($this->getServiceUrl("list"),array(
            "User-Agent"   => "Marmotta Client Library (PHP)",
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
     * Upload a reasoner program to the Marmotta knowledge base and register it with the name given as argument.
     * The Marmotta will automatically trigger a full reasoning when the program has been uploaded successfully.
     * The reasoner program is a string in the KWRL Syntax described at http://code.google.com/p/kiwi/wiki/Reasoning
     *
     * @param $name
     * @param $program
     */
    public function uploadProgram($name, $program) {
        $client = new Client();
        $request = $client->post($this->getServiceUrl($name),array(
            "User-Agent"   => "Marmotta Client Library (PHP)",
            "Content-Type" => "text/plain"
        ),$program);
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();
    }

    /**
     * Remove the reasoner program with the given name from the Marmotta knowledge base.
     * @param $name
     */
    public function deleteProgram($name) {
        $client = new Client();
        $request = $client->delete($this->getServiceUrl($name),array(
            "User-Agent"   => "Marmotta Client Library (PHP)"
        ));
        // set authentication if given in configuration
        if(!is_null($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();
    }


    private function getServiceUrl($suffix = 0) {
        $serviceUrl = $this->config->getBaseUrl() . ReasonerClient::$URL_PROGRAM_SERVICE .
            ($suffix ? "/" . urlencode($suffix) : "");
        return $serviceUrl;
    }

}
?>
