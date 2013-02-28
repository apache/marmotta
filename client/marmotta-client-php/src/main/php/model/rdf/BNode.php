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
namespace MarmottaClient\Model\RDF;

require_once 'RDFNode.php';
use MarmottaClient\Model\RDF\RDFNode;

/**
 * Representation of an RDF blank node / anonymous node in PHP
 *
 * User: sschaffe
 * Date: 25.01.12
 * Time: 10:20
 * To change this template use File | Settings | File Templates.
 */
class BNode extends RDFNode
{
    /** @var the ID of the anonymous node (depending on server implementation) */
    private $anonId;

    function __construct($anonId)
    {
        $this->anonId = $anonId;
    }



    function __toString()
    {
        return "_:"+$this->anonId;
    }

    /**
     * @return \the
     */
    public function getAnonId()
    {
        return $this->anonId;
    }


}
