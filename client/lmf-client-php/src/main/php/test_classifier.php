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
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
require_once 'autoload.php';

use LMFClient\ClientConfiguration;
use LMFClient\Clients\ClassificationClient;
use LMFClient\Clients\ResourceClient;

$config = new ClientConfiguration("http://localhost:8080/LMF");

$client = new ClassificationClient($config);
$rclient = new ResourceClient($config);

// create classifier
$client->removeClassifier("phptest",true);
$client->createClassifier("phptest");

// add some training data
$rclient->createResource("http://www.example.com/Concept1");
$rclient->createResource("http://www.example.com/Concept2");

$data1 = <<<DATA1
Major acquisitions that have a lower gross margin than the existing network also
had a negative impact on the overall gross margin, but it should improve following
the implementation of its integration strategies .
DATA1;

$data2 = <<<DATA2
The upward movement of gross margin resulted from amounts pursuant to adjustments
to obligations towards dealers .
DATA2;


$client->trainClassifier("phptest","http://www.example.com/Concept1", $data1);
$client->trainClassifier("phptest","http://www.example.com/Concept2", $data2);

$client->retrainClassifier("phptest");

foreach($client->getAllClassifications("phptest","Major acquisitions that have a lower gross margin than the existing network") as $classification) {
    echo "Concept: " . $classification["concept"] . ", probability " . $classification["probability"] . "\n";
}
$client->removeClassifier("phptest",true);

?>
