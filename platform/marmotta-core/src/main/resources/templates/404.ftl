<#--

    Copyright (C) 2013 The Apache Software Foundation.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

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
  <script src="${baseUri}core/public/js/lib/jquery-1.7.2.js" type="text/javascript" ></script>
  <link href="${baseUri}core/public/style/style.css" title="screen" rel="stylesheet" type="text/css" />
  <link href="${baseUri}core/public/style/scheme/blue.css" title="screen" rel="stylesheet" type="text/css" />  
  <link href="${baseUri}core/public/style/scheme/dark.css" title="screen" rel="stylesheet" type="text/css" />  
  <link href="${baseUri}core/public/style/rdfhtml.css" title="screen" rel="stylesheet" type="text/css" />  
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
        <a id="logo" href="${baseUri}">
            <img src="${baseUri}core/public/img/logo/lmf-logo.png" alt="LMF" />
        </a>    
        <h1>LMF Linked Data Explorer</h1>
    </div>

    <div id="center">
        <div id="content">

          <h2>404 Not Found</h2>
        
          <p>
            <strong><a href="${baseUri}resource?uri=${encoded_uri}">${uri}</a></strong><a href="${uri}"><img src="${baseUri}core/public/img/icon/link.png" alt="${uri}" title="go to ${uri} directly" /></a>
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
                <abbr title="Linked Media Framework">LMF</abbr> is a project of <a href="http://www.newmedialab.at/">SNML-TNG</a></span>
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
