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
 * A client for running SPARQL 1.1 Queries and Updates on the Marmotta Server.
 * User: sschaffe
 * Date: 27.01.12
 * Time: 10:18
 * To change this template use File | Settings | File Templates.
 */
class SPARQLClient
{
    protected $config;

    private static $URL_QUERY_SERVICE  = "/sparql/select?query=";
    private static $URL_UPDATE_SERVICE = "/sparql/update?query=";

    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;
    }


    /**
     * Run a SPARQL Select query against the Marmotta Server and return the results as an array of rows, each consisting of a
     * map mapping query variable names to result values (instances of RDFNode). Results will be transfered and parsed
     * using the SPARQL JSON format.
     * @param query a SPARQL Select query to run on the database
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public function select($query) {
        $serviceUrl = $this->config->getBaseUrl() . SPARQLClient::$URL_QUERY_SERVICE . urlencode($query);


        try {
            $client = new Client();
            $request = $client->get($serviceUrl,array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Accept" => "application/sparql-results+json"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

            $sparql_result = json_decode($response->getBody(true),true);

            $variables = $sparql_result["head"]["vars"];
            $bindings  = $sparql_result["results"]["bindings"];

            $result = array();
            foreach($bindings as $binding) {
                $row = array();
                foreach($binding as $var => $value) {

                    if($value["type"] == "uri") {
                        $object = new URI($value["value"]);
                    } else if($value["type"] == "literal" || $value["type"] == "typed-literal") {
                        $object = new Literal(
                            $value["value"],
                            isset($value["language"])?$value["language"]:null,
                            isset($value["datatype"])?$value["datatype"]:null);

                    } else if($value["type"] == "bnode") {
                        $object = new BNode($value["value"]);
                    }
                    $row[$var] = $object;
                }
                $result[] = $row;
            }
            return $result;

        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("error evaluating SPARQL Select Query $query; ".$ex->getResponse()->getReasonPhrase());
        }

    }


    /**
     * Carry out a SPARQL ASK Query and return either true or false, depending on the query result.
     *
     * @param askQuery
     * @return boolean
     * @throws IOException
     * @throws MarmottaClientException
     */
    public function ask($askQuery) {
        $serviceUrl = $this->config->getBaseUrl() . SPARQLClient::$URL_QUERY_SERVICE . urlencode($askQuery);


        try {
            $client = new Client();
            $request = $client->get($serviceUrl,array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Accept" => "application/sparql-results+json"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

            $body = str_replace("boolean:","\"boolean\":",str_replace("head:","\"head\":",(string)$response->getBody(true)));
            $sparql_result = json_decode($body,true);

            if(count($sparql_result) == 0) {
                return False;
            } else {
                $result  = $sparql_result["boolean"];
                if($result == "true") {
                    return True;
                } else {
                    return False;
                }
            }



        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("error evaluating SPARQL Ask Query $askQuery; ".$ex->getResponse()->getReasonPhrase());
        }
    }


    /**
     * Execute a SPARQL Update query according to the SPARQL 1.1 standard. The query will only be passed to the server,
     * which will react either with ok (in this the method simply returns) or with error (in this case, the method
     * throws an MarmottaClientException).
     *
     * @param updateQuery         the SPARQL Update 1.1 query string
     * @throws IOException        in case a connection problem occurs
     * @throws MarmottaClientException in case the server returned and error and did not execute the update
     */
    public function update($updateQuery) {
        $serviceUrl = $this->config->getBaseUrl() . SPARQLClient::$URL_UPDATE_SERVICE . urlencode($updateQuery);


        try {
            $client = new Client();
            $request = $client->get($serviceUrl,array(
                "User-Agent"   => "Marmotta Client Library (PHP)"
            ));
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();


        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("error evaluating SPARQL Update Query $updateQuery; ".$ex->getResponse()->getReasonPhrase());
        }
    }
}
