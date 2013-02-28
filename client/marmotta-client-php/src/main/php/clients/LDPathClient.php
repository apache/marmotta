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
require_once 'util/rdfjson.php';

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
 * Run LDPath queries agains the Marmotta LDPath Webservice. Allows evaluation of simple path queries as well as of
 * complex path programs.
 *
 * User: sschaffe
 * Date: 27.01.12
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
class LDPathClient
{
    protected $config;

    private static $URL_PATH_SERVICE  = "/ldpath/path?path=";
    private static $URL_PROGRAM_SERVICE = "/ldpath/program?program=";

    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;
    }


    /**
     * Evaluate the path query passed as second argument, starting at the resource with the uri given as first argument.
     * Returns an array of RDFNode objects.
     *
     * @param $uri
     * @param $path
     */
    public function evaluatePath($uri, $path) {
        $serviceUrl = $this->config->getBaseUrl() . LDPathClient::$URL_PATH_SERVICE . urlencode($path) . "&uri=" . urlencode($uri);


        try {
            $client = new Client();
            $request = $client->get($serviceUrl,array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Accept" => "application/json"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

            $query_result = json_decode($response->getBody(true),true);

            $result = array();

            foreach($query_result as $node) {
                $result[] = decode_node($node);
            }
            return $result;
        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("error evaluating LDPath Query $path; ".$ex->getResponse()->getReasonPhrase());
        }
    }


    /**
     * Evaluate the LDPath program passed as second argument starting at the resource identified by the uri given as first argument.
     * Returns a map from field names to lists of RDFNode objects, representing the results of the evaluation.
     *
     * @param $uri
     * @param $program
     */
    public function evaluateProgram($uri, $program) {
        $serviceUrl = $this->config->getBaseUrl() . LDPathClient::$URL_PROGRAM_SERVICE . urlencode($program) . "&uri=" . urlencode($uri);


        try {
            $client = new Client();
            $request = $client->get($serviceUrl,array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Accept" => "application/json"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

            $query_result = json_decode($response->getBody(true),true);

            $result = array();

            foreach($query_result as $field => $value) {
                $result[$field] = array();
                foreach($value as $node) {
                    $result[$field][] = decode_node($node);
                }
            }
            return $result;
        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("error evaluating LDPath Query $path; ".$ex->getResponse()->getReasonPhrase());
        }
    }

}
