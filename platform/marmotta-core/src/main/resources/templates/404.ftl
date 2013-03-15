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
  <title>404 Not Found - Marmotta Linked Data Explorer</title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" /> 
  <meta http-equiv="Default-Style" content="${DEFAULT_STYLE}"> 
  <script src="${SERVER_URL}core/public/js/lib/jquery-1.7.2.js" type="text/javascript" ></script>
  <link href="${SERVER_URL}core/public/style/style.css" title="screen" rel="stylesheet" type="text/css" />
  <link href="${SERVER_URL}core/public/style/scheme/blue.css" title="screen" rel="stylesheet" type="text/css" />  
  <link href="${SERVER_URL}core/public/style/scheme/dark.css" title="screen" rel="alternate stylesheet" type="text/css" />  
  <link href="${SERVER_URL}core/public/style/rdfhtml.css" title="screen" rel="stylesheet" type="text/css" />  
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

<div id="wrapper">
    <div id="header">
        <a id="logo" href="${SERVER_URL}" title="${PROJECT}">
            <img src="${SERVER_URL}${LOGO}" alt="${PROJECT} logo" />
        </a>
        <h1>Marmotta Linked Data Explorer</h1>
        <div class="clean"></div>
    </div>
    <div class="clear"></div>
    <div id="center">
        <div id="content">

          <h2>404 Not Found</h2>
        
          <p>
            <strong><a href="${SERVER_URL}resource?uri=${encoded_uri}">${uri}</a></strong><a href="${uri}"><img src="${SERVER_URL}core/public/img/icon/link.png" alt="${uri}" title="go to ${uri} directly" /></a>
          </p>
          
          <p>
            Sorry, but ${message}.
          </p>  
        
        </div>
        
    </div>
    
    <div class="clear"></div>
    <div id="footer">
        <div id="footer_line">
            <span>
                ${FOOTER}
            </span>
        </div>
    </div> 

</div>

<script type="text/javascript"> 

  $(document).ready(function() {

  });

</script> 

</body>

</html>
