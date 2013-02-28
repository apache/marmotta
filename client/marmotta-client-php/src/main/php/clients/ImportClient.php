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
 * Date: 27.01.12
 * Time: 14:01
 * To change this template use File | Settings | File Templates.
 */
class ImportClient
{

    private static $URL_TYPES_SERVICE  = "/import/types";
    private static $URL_UPLOAD_SERVICE = "/import/upload";

    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;
    }


    /**
     * Return a set of mime types representing the types that are accepted by the Marmotta server.
     *
     * @return
     * @throws IOException
     * @throws MarmottaClientException
     */
    public function getSupportedTypes() {
        $serviceUrl = $this->config->getBaseUrl() . ImportClient::$URL_TYPES_SERVICE;

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

            return json_decode($response->getBody(true),true);


        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("error listing supported types; ".$ex->getResponse()->getReasonPhrase());
        }
    }

    /**
     * Upload/Import a dataset in the Marmotta Server. The dataset is given as a string that contains data of the
     * mime type passed as argument. The mime type must be one of the acceptable types of the server.
     *
     * @param data        string to read the dataset from; will be consumed by this method
     * @param mimeType  mime type of the input data
     * @throws IOException
     * @throws MarmottaClientException
     */
    public function uploadDataset($data, $mimeType) {
        $serviceUrl = $this->config->getBaseUrl() . ImportClient::$URL_UPLOAD_SERVICE;

        try {
            $client = new Client();
            $request = $client->post($serviceUrl,array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Content-Type" => $mimeType
            ),$data);
            // set authentication if given in configuration
            if(!is_null($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

        } catch(BadResponseException $ex) {
            throw new MarmottaClientException("error uploading dataset; ".$ex->getResponse()->getReasonPhrase());
        }
    }

}
