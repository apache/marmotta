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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr">

  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    <title>404 Not Found - Marmotta Linked Data Explorer</title>
    <script src="${SERVER_URL}webjars/jquery/1.8.2/jquery.min.js" type="text/javascript" ></script>
    <link href="${SERVER_URL}${DEFAULT_STYLE}style.css" rel="stylesheet" type="text/css" />
    <link href="${SERVER_URL}${DEFAULT_STYLE}rdfhtml.css" rel="stylesheet" type="text/css" />
    <link href="${SERVER_URL}${DEFAULT_STYLE}404.css" rel="stylesheet" type="text/css" />
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

