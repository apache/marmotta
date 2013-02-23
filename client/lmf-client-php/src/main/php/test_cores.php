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
/**
 * Created by IntelliJ IDEA.
 * User: sschaffe
 * Date: 27.01.12
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */

require_once 'autoload.php';

use LMFClient\ClientConfiguration;
use LMFClient\Clients\CoresClient;

$config = new ClientConfiguration("http://localhost:8080/LMF");

$client = new CoresClient($config);

// list all available cores
foreach($client->listCores() as $core) {
    echo "Found core: $core\n";
}

// retrieve a core
echo "Core $core:\n";
echo $client->getCoreConfiguration($core);


// create a core
$program = "title = rdfs:label :: xsd:string ;";
$client->createCoreConfiguration("lmfclient",$program);

// give it some time
sleep(2);

// retrieve the core
echo "Core lmfclient:\n";
echo $client->getCoreConfiguration("lmfclient");

// delete the core
$client->deleteCore("lmfclient");


// try to get it again
//echo "Core lmfclient:\n";
//echo $client->getCoreConfiguration("lmfclient");

?>