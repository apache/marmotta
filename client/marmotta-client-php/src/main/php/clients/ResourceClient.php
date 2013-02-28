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
require_once 'util/rdfjson.php';
require_once 'model/content/Content.php';
require_once 'exceptions/MarmottaClientException.php';
require_once 'exceptions/NotFoundException.php';
require_once 'exceptions/ContentFormatException.php';

use \MarmottaClient\Model\Content\Content;
use \MarmottaClient\ClientConfiguration;

use \MarmottaClient\Exceptions\MarmottaClientException;
use \MarmottaClient\Exceptions\NotFoundException;
use \MarmottaClient\Exceptions\ContentFormatException;

use Guzzle\Http\Client;
use Guzzle\Http\Message\BadResponseException;

/**
 * Created by IntelliJ IDEA.
 * User: sschaffe
 * Date: 23.01.12
 * Time: 23:13
 * To change this template use File | Settings | File Templates.
 */
class ResourceClient
{
    protected $config;

    private static $URL_RESOURCE_SERVICE = "/resource?uri=";

    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;
    }

    /**
     * Create a resource in the remote Marmotta installation
     * @param $uri
     * @throws Exception
     */
    public /* boolean */ function createResource($uri) {
        $client = new Client();
        $request = $client->post($this->getServiceUrl($uri),array(
            "User-Agent"   => "Marmotta Client Library (PHP)",
            "Content-Length" => "0",
            "Content-Type" => "application/json"
        ));
        // set authentication if given in configuration
        if(!_isset($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        //$request->getCurlOptions()->set(CURLOPT_PROXY, "localhost:8888");
        $request->send();
    }


    public /* boolean */ function existsResource($uri) {
        $client = new Client();
        $request = $client->options($this->getServiceUrl($uri),array(
            "User-Agent"   => "Marmotta Client Library (PHP)"
        ));
        // set authentication if given in configuration
        if(!_isset($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();

        if($response->hasHeader("Access-Control-Allow-Methods")) {
            if($response->getHeader("Access-Control-Allow-Methods") == "POST") {
                return False;
            } else if(strpos($response->getHeader("Access-Control-Allow-Methods"),"GET")) {
                return True;
            } else {
                return False;
            }
        } else {
            return False;
        }
    }

    /**
     * Return the metadata of the resource identified by the URI passed as argument. In PHP, the returned
     * object is an array mapping from property URIs to arrays of RDFNodes, representing the values of this
     * property.
     * <p/>
     * Example:
     *
     * array(
     *    "http://xmlns.com/foaf/0.1/name" => array(new Literal("Sepp Huber"))
     * )
     *
     * @param $uri
     * @return array
     */
    public /* Metadata */ function getResourceMetadata($uri) {
        try {
            $client = new Client();
            $request = $client->get($this->getServiceUrl($uri),array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Accept" => "application/json; rel=meta"
            ));
            // set authentication if given in configuration
            if(!_isset($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

            return decode_metadata($uri,$response->getBody(true));
        } catch(BadResponseException $ex) {
            if($ex->getResponse()->getStatusCode() == 404) {
                throw new NotFoundException("could not retrieve resource metadata for resource $uri; it does not exist");
            } else if($ex->getResponse()->getStatusCode() == 406) {
                throw new ContentFormatException("server does not offer metadata type application/json for resource $uri");
            } else {
                throw new MarmottaClientException("could not retrieve resource metadata for resource $uri; ".$ex->getResponse()->getReasonPhrase());
            }
        }
    }

    /**
     * Update the metadata of the resource identified by the URI passed as argument. The resource must exist before
     * this method can be called (e.g. using createResource()).
     *
     * The metadata must be an array corresponding to the MarmottaClient metadata format.
     *
     * <p/>
     * Example:
     *
     * array(
     *    "http://xmlns.com/foaf/0.1/name" => array(new Literal("Sepp Huber"))
     * )
     *
     * @param $uri
     * @param $metadata
     */
    public /* void */ function updateResourceMetadata($uri, /* Metadata */ $metadata) {
        $metadata_json = encode_metadata($uri, $metadata);

        $client = new Client();
        $request = $client->put($this->getServiceUrl($uri),array(
            "User-Agent"   => "Marmotta Client Library (PHP)",
            "Content-Type" => "application/json; rel=meta"
        ), $metadata_json);
        // set authentication if given in configuration
        if(!_isset($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();

        if($response->getStatusCode() >= 400) {
            throw new Exception("could not update resource $uri; ".$response->getReasonPhrase());
        }
    }

    /**
     * Retrieve the (human-readable) content of the given mimeType of the given resource. Will return a content
     * object that allows reading the input stream. In case no content of the given mime type exists for the resource,
     * will throw a Exception.
     *
     * @param $uri
     * @param $mimeType
     * @return Model\Content\Content
     * @throws Exception
     */
    public /* Content */ function getResourceContent($uri, $mimeType)  {
        try {
            $client = new Client();
            $request = $client->get($this->getServiceUrl($uri),array(
                "User-Agent"   => "Marmotta Client Library (PHP)",
                "Accept" => $mimeType . "; rel=content"
            ));
            // set authentication if given in configuration
            if(!_isset($this->config->getUsername())) {
                $request->setAuth($this->config->getUsername(),$this->config->getPassword());
            }
            $response = $request->send();

            if($response->getStatusCode() >= 400) {
                throw new Exception("could not retrieve resource metadata for resource $uri; ".$response->getReasonPhrase());
            }

            return new \MarmottaClient\Model\Content\Content($response->getBody(true),$response->getBody()->getSize(),$response->getHeader("Content-Type"));
        } catch(BadResponseException $ex) {
            if($ex->getResponse()->getStatusCode() == 404) {
                throw new NotFoundException("could not retrieve resource content for resource $uri; it does not exist");
            } else if($ex->getResponse()->getStatusCode() == 406) {
                throw new ContentFormatException("server does not offer content type $mimeType for resource $uri");
            } else {
                throw new MarmottaClientException("could not retrieve resource content for resource $uri; ".$ex->getResponse()->getReasonPhrase());
            }
        }
    }

    /**
     * Update the content of the resource identified by the URI given as argument. The resource has to exist before
     * content can be uploaded to it. Any existing content will be overridden. Throws Exception if the content type is
     * not supported or if the resource does not exist.
     *
     * @param $uri
     * @param Model\Content\Content $content
     * @throws Exception
     */
    public /* void */ function updateResourceContent($uri, Content $content) {
        $client = new Client();
        $request = $client->put($this->getServiceUrl($uri),array(
            "User-Agent"   => "Marmotta Client Library (PHP)",
            "Content-Type" => $content->getMimetype() . "; rel=content"
        ), $content->getData());
        // set authentication if given in configuration
        if(!_isset($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();

        if($response->getStatusCode() >= 400) {
            throw new Exception("could not update resource $uri; ".$response->getReasonPhrase());
        }
    }

    public /* void */ function deleteResource($uri) {
        $client = new Client();
        $request = $client->delete($this->getServiceUrl($uri),array(
            "User-Agent"   => "Marmotta Client Library (PHP)",
        ));
        // set authentication if given in configuration
        if(!_isset($this->config->getUsername())) {
            $request->setAuth($this->config->getUsername(),$this->config->getPassword());
        }
        $response = $request->send();

        if($response->getStatusCode() >= 400) {
            throw new Exception("could not delete resource $uri; ".$response->getReasonPhrase());
        }
    }

    public function getServiceUrl($uri) {
        return $this->config->getBaseUrl() . ResourceClient::$URL_RESOURCE_SERVICE . encodeURIComponent($uri);
    }

}
