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
/**
 * Created by IntelliJ IDEA.
 * User: sschaffe
 * Date: 25.01.12
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */

require_once 'model/rdf/Literal.php';
require_once 'model/rdf/URI.php';
require_once 'model/rdf/BNode.php';

use MarmottaClient\Model\RDF\Literal;
use MarmottaClient\Model\RDF\URI;
use MarmottaClient\Model\RDF\BNode;

/**
 * Convert an RDF/JSON string into the MarmottaClient metadata representation. Returns an array of the form
 *
 * array(
 *    "http://xmlns.com/foaf/0.1/name" => array(new Literal("Sepp Huber"))
 * )
 *
 * @param $rdfjson_string
 */
function decode_metadata($uri,$rdfjson_string) {
    $result = array();

    $json_array = json_decode($rdfjson_string,true);
    foreach($json_array as $subject => $properties) {
        if($uri == $subject) {
            foreach($properties as $property => $objects) {
                $result[$property] = array();
                foreach($objects as $object) {
                    $result[$property][] = decode_node($object);
                }
            }
        }
    }
    return $result;
}


function decode_node($object) {
    if($object["type"] == "literal") {
        return new Literal(
            $object["value"],
            isset($object["lang"]) ? $object["lang"] : null,
            isset($object["datatype"]) ? $object["datatype"] : null
        );
    } else if($object["type"] == "uri") {
        return new URI($object["value"]);
    } else if($object["type"] == "bnode") {
        return new BNode($object["value"]);
    } else {
        return null;
    }
}

/**
 * Transform a metadata array into an RDF/JSON representation suitable for sending to the Marmotta Server.
 *
 * @param $uri
 * @param $metadata_array
 */
function encode_metadata($uri, $metadata_array) {
    $result = array();
    $result[$uri] = array();

    foreach($metadata_array as $property => $values) {
        $result[$uri][$property] = array();
        foreach($values as $value) {
            if($value instanceof \MarmottaClient\Model\RDF\Literal) {
                $object = array (
                    "type"  => "literal",
                    "value" => $value->getContent()
                );
                if(!is_null($value->getLanguage())) {
                    $object["lang"] = $value->getLanguage();
                }
                if(!is_null($value->getDatatype())) {
                    $object["datatype"] = $value->getDatatype();
                }
                $result[$uri][$property][] = $object;
            } else if($value instanceof \MarmottaClient\Model\RDF\URI) {
                $result[$uri][$property][] = array(
                    "type"  => "uri",
                    "value" => $value->getUri()
                );
            } else if($value instanceof \MarmottaClient\Model\RDF\BNode) {
                $result[$uri][$property][] = array(
                    "type"  => "bnode",
                    "value" => $value->getAnonId()
                );
            } else {
                // try figuring out whether it is literal or uri based on the string value
                if(preg_match('|^http(s)?://[a-z0-9-]+(.[a-z0-9-]+)*(:[0-9]+)?(/.*)?$|i', $value)) {
                    $result[$uri][$property][] = array(
                        "type"  => "uri",
                        "value" => $value
                    );
                } else {
                    $object = array (
                        "type"  => "literal",
                        "value" => $value
                    );
                }
            }
        }
    }
    return json_encode($result);
}



?>