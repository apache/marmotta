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
 * Time: 10:38
 * To change this template use File | Settings | File Templates.
 */


require_once 'autoload.php';

use MarmottaClient\ClientConfiguration;
use MarmottaClient\Clients\ResourceClient;

$config = new ClientConfiguration("http://localhost:8080/mtta");

$client = new ResourceClient($config);

/*
echo $client->getResourceContent("http://localhost:8080/mtta/resource/Chess","application/xhtml+xml")->getData();
*/

/*
echo "should be false: " . $client->existsResource("http://brzlbrnft.com/123") . "\n";
echo "should be true: " . $client->existsResource("http://localhost:8080/mtta/resource/Chess") . "\n";
*/


$client->createResource("http://localhost:8080/mtta/resource/1234");

$metadata = array(
    "http://xmlns.com/foaf/0.1/name" => array(new \MarmottaClient\Model\RDF\Literal("Hans Mustermann"))
);

$client->updateResourceMetadata("http://localhost:8080/mtta/resource/1234",$metadata);

$client->deleteResource("http://localhost:8080/mtta/resource/1234");


/*
$metadata = $client->getResourceMetadata("http://localhost:8080/mtta/resource/hans_meier");

var_dump($metadata);

echo encode_metadata("http://localhost:8080/mtta/resource/hans_meier",$metadata);
*/
?>