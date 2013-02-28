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
namespace MarmottaClient;

/**
 * Created by IntelliJ IDEA.
 * User: sschaffe
 * Date: 24.01.12
 * Time: 22:04
 * To change this template use File | Settings | File Templates.
 */
class ClientConfiguration
{

    protected $baseUrl;

    protected $username;

    protected $password;

    function __construct($baseUrl, $username = null, $password = null)
    {
        $this->baseUrl  = $baseUrl;
        $this->username = $username;
        $this->password = $password;
    }

    public function setBaseUrl($baseUrl)
    {
        $this->baseUrl = $baseUrl;
    }

    public function getBaseUrl()
    {
        return $this->baseUrl;
    }

    public function setPassword($password)
    {
        $this->password = $password;
    }

    public function getPassword()
    {
        return $this->password;
    }

    public function setUsername($username)
    {
        $this->username = $username;
    }

    public function getUsername()
    {
        return $this->username;
    }

    public function getBaseUrlComponents($path = "") {
        $components = parse_url($this->getBaseUrl() . $path);
        if (!in_array("path", $components)) {
            $components["path"] = "";
        }
        return $components;
    }

}
