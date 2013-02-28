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
 * Date: 30.01.12
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
require_once 'autoload.php';

use MarmottaClient\ClientConfiguration;
use MarmottaClient\Clients\LDPathClient;

$config = new ClientConfiguration("http://localhost:8080/mtta");

$client = new LDPathClient($config);

// list friends of hans meier
foreach($client->evaluatePath("https://www.youtube.com/profile?user=wastl76", "(<http://gdata.youtube.com/schemas/2007#playlist> | <http://gdata.youtube.com/schemas/2007#favorite> ) / <http://gdata.youtube.com/schemas/2007#video> / <http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag> / <http://www.holygoat.co.uk/owl/redwood/0.1/tags/name>") as $field) {
    echo "Tag: $field\n";
}

?>