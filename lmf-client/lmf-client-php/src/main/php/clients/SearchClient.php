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
require_once 'model/content/Content.php';
require_once 'exceptions/LMFClientException.php';
require_once 'exceptions/NotFoundException.php';
require_once 'exceptions/ContentFormatException.php';

use \LMFClient\Model\Content\Content;
use \LMFClient\ClientConfiguration;

use \LMFClient\Exceptions\LMFClientException;
use \LMFClient\Exceptions\NotFoundException;
use \LMFClient\Exceptions\ContentFormatException;

use \Solarium_Query_Select;

use LMFClient\Model\RDF\Literal;
use LMFClient\Model\RDF\URI;
use LMFClient\Model\RDF\BNode;


use Guzzle\Http\Client;
use Guzzle\Http\Message\BadResponseException;

use Solarium_Client;

/**
 * Evaluate SOLR searches over the LMF search cores. The PHP client library of the LMF makes use of Solarium;
 * you can find the documentation at http://wiki.solarium-project.org/index.php/V2:Manual_for_version_2.x
 *
 * User: sschaffe
 * Date: 27.01.12
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
class SearchClient
{

    private static $URL_SOLR_SERVICE  = "/solr";

    private $config;

    private $adapter;

    private $clients = array();


    function __construct(ClientConfiguration $config)
    {
        $this->config = $config;

        $this->adapter = $this->config->getBaseUrlComponents();
        if($this->config->getUsername() || $this->config->getPassword()) {
            throw new LMFClientException("HTTP Authentication is not yet supported by the Solarium client");
        }
    }


    /**
     * Create and return a Solarium query object querying against the LMF core passed as argument. The query object
     * can then be used with the full Solarium functionality for further querying. When configured, it needs to
     * be passed to the search() method for evaluation.
     *
     * @param $core
     * @return \Solarium_Query_Select
     */
    public function createQuery($core) {

        if(!isset($clients[$core])) {
            $solarium_config = array(
                "adapteroptions" => array(
                    "host" => $this->adapter["host"],
                    "port" => $this->adapter["port"],
                    "path" => $this->adapter["path"] . SearchClient::$URL_SOLR_SERVICE,
                    "core" => $core
                )
            );
            $clients[$core] = new Solarium_Client($solarium_config);
        }
        return $clients[$core]->createSelect();
    }

    /**
     * Evaluate a SOLR query against the LMF core passed as argument. Returns an array of documents.
     *
     * @param $query
     */
    public function search($core,$query) {
        if(!isset($clients[$core])) {
            $solarium_config = array(
                "adapteroptions" => array(
                    "host" => $this->adapter["host"],
                    "port" => $this->adapter["port"],
                    "path" => $this->adapter["path"] . SearchClient::$URL_SOLR_SERVICE,
                    "core" => $core
                )
            );
            $clients[$core] = new Solarium_Client($solarium_config);
        }
        return $clients[$core]->select($query);
    }


    /**
     * Run a SOLR search against the selected core and return the result as array of documents. This is a convenience
     * wrapper around createQuery and Solarium for simpler cases.
     *
     * @param coreName name of the core to query
     * @param query    the SolrQuery to run on the core
     * @param options  array of configuration options
     *                 (fields: array of field names, facets: array of fields to facet over, sort: array of sort fields,
     *                  offset: integer position to start with, limit: maximum number of documents to return)
     * @return
     * @throws IOException
     * @throws LMFClientException
     */
    public function simpleSearch($core, $query, $options = array()) {

        $solr_query = $this->createQuery($core);
        $solr_query->setQuery($query);

        if(isset($options["fields"])) {
            foreach($options["fields"] as $field) {
                $solr_query->addField($field);
            }
        }

        if(isset($options["sort"])) {
            foreach($options["sort"] as $field) {
                $solr_query->addSort($field, Solarium_Query_Select::SORT_DESC);
            }
        } else {
            $solr_query->addSort("score", Solarium_Query_Select::SORT_DESC);
        }

        if(isset($options["facets"])) {
            $facets = $solr_query->getFacetSet();
            foreach($options["facets"] as $field) {
                $facets->createFacetField($field)->setField($field);
            }
        }

        if(isset($options["offset"])) {
            $solr_query->setStart($options["offset"]);
        }
        if(isset($options["limit"])) {
            $solr_query->setRows($options["limit"]);
        }

        return $this->search($core,$solr_query);
    }


    /**
     * Run a SOLR MoreLikeThis query to get recommendations for the resource whose URI is passed as the first argument.
     * The field options are a map from field names to floats representing the weight of the field for the recommendation.
     *
     * @param $uri
     * @param array $field_options
     */
    public function recommendations($core, $uri, array $field_options) {
        if(!isset($clients[$core])) {
            $solarium_config = array(
                "adapteroptions" => array(
                    "host" => $this->adapter["host"],
                    "port" => $this->adapter["port"],
                    "path" => $this->adapter["path"] . SearchClient::$URL_SOLR_SERVICE,
                    "core" => $core
                )
            );
            $clients[$core] = new Solarium_Client($solarium_config);
        }
        $query = $clients[$core]->createMoreLikeThis();
        $query->setQuery('lmf.uri:"'.$uri.'"');
        $query->setInterestingTerms("details");
        $query->setMinimumDocumentFrequency(1);
        $query->setMinimumTermFrequency(1);
        $query->setMatchInclude(true);

        $fields = "";
        $weights = "";
        foreach($field_options as $field => $weight) {
            $fields = $fields . $field . ",";
            $weights = $weights . $field . "^" . $weight . " ";
        }
        if(count($field_options) > 0) {
            $fields = substr($fields,0,-1);
            $weights = substr($weights,0,-1);
        }
        $query->setMltFields($fields);
        $query->setQueryFields($weights);

        return $clients[$core]->select($query);
    }

}
