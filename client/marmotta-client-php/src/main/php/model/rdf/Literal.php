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
 * Represents an RDF Literal in PHP, optionally with language specification and datatype.
 *
 * User: sschaffe
 * Date: 25.01.12
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
class Literal extends RDFNode
{
    /** @var content of the literal */
    private $content;

    /** @var language of the literal (2-letter ISO code, optional) */
    private $language;

    /** @var datatype of the literal (URI, optional) */
    private $datatype;

    function __construct($content, $language = null, $datatype = null)
    {
        $this->content = $content;
        $this->language = $language;
        $this->datatype = $datatype;
    }

    public function getContent()
    {
        return $this->content;
    }

    public function getDatatype()
    {
        return $this->datatype;
    }

    public function getLanguage()
    {
        return $this->language;
    }

    function __toString()
    {
        return $this->content;
    }


}
