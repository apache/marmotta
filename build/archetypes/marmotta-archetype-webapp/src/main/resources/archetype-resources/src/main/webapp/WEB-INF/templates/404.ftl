<#--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements. See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership. The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<!DOCTYPE html>
<html lang="en">

<head>
  <title>404 Not Found - LMF Linked Data Explorer</title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
  <script type="text/javascript" src="${baseUri}core/public/js/lib/jquery-1.7.2.js"></script>
  <script type="text/javascript" src="${baseUri}core/public/js/lib/jquery-ui-1.8.21.js"></script>
  <link href="${baseUri}core/public/style/style1.css" title="screen" rel="stylesheet" type="text/css" />
  <link href="${baseUri}core/public/style/center.css" title="screen" rel="stylesheet" type="text/css" />  
  <style type="text/css">
    div#center {
      float: none; 
      width: auto; 
      vertical-align: middle; 
      min-height: 400px; 
      margin: 0; 
      padding: 2em 30% 5em 30%;
    }
    div#center > * {
      margin-top: 2em;
      font-size: 1.6em;
    }
    div#center > p > a > img {
      vertical-align: text-top;
      margin-left: 0.15em;
    }
  </style>  
</head>

<body>

<div id="header">
  <div id="logo">
    <a href="${baseUri}">
      <img src="${baseUri}core/public/img/lmf-white.png" alt="LMF" />
    </a>
  </div>
  <h1>LMF Linked Data Explorer</h1>
</div>

<div id="center">

  <h2>404 Not Found</h2>

  <p>
    <strong><a href="${baseUri}resource?uri=${encoded_uri}">${uri}</a></strong><a href="${uri}"><img src="${baseUri}core/public/img/link.png" alt="${uri}" title="go to ${uri} directly" /></a>
  </p>
  
  <p>
    Sorry, but the requested resource could not be found in LMF right now,
    but may be available again in the future.
  </p>  

</div>

<div id="footer" class="clear">
    <span><abbr title="Linked Media Framework">LMF</abbr> is a project of <a href="http://www.newmedialab.at/">SNML-TNG</a></span>
</div>

<script type="text/javascript"> 

  $(document).ready(function() {

  });

</script> 

</body>

</html>
