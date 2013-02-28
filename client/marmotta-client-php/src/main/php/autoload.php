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
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
require_once 'vendor/.composer/autoload.php';

require_once 'ClientConfiguration.php';
require_once 'clients/ResourceClient.php';
require_once 'clients/ConfigurationClient.php';
require_once 'clients/ClassificationClient.php';
require_once 'clients/SPARQLClient.php';
require_once 'clients/LDPathClient.php';
require_once 'clients/ImportClient.php';
require_once 'clients/SearchClient.php';
require_once 'clients/CoresClient.php';
require_once 'clients/ReasonerClient.php';


require_once 'exceptions/MarmottaClientException.php';
require_once 'exceptions/NotFoundException.php';
require_once 'exceptions/ContentFormatException.php';


require_once 'model/content/Content.php';
require_once 'model/rdf/RDFNode.php';
require_once 'model/rdf/BNode.php';
require_once 'model/rdf/Literal.php';
require_once 'model/rdf/URI.php';


?>