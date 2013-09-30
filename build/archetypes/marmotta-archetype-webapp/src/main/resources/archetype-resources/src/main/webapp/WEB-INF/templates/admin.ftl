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
<html>
    <head>
	    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta http-equiv="Default-Style" content="${DEFAULT_STYLE}">
        <link href="${SERVER_URL}core/public/style/javadoc.css" rel="stylesheet" type="text/css" />
	    <link href="${SERVER_URL}core/public/style/style1.css" title="screen" rel="stylesheet" type="text/css" />
        <link href="${SERVER_URL}core/public/style/style2.css" title="beamer" rel="alternate stylesheet" type="text/css" />
        <link href="${SERVER_URL}core/public/img/icon-small.ico" rel="SHORTCUT ICON">
        <script type="text/javascript">
            var _BASIC_URL = "${BASIC_URL}";
            //use _SERVER_URL for webservice calls
            var _SERVER_URL = "${SERVER_URL}";
        </script>
        <script type="text/javascript" src="${SERVER_URL}core/public/js/lib/jquery-1.7.2.js"></script>
        <script type="text/javascript" src="${SERVER_URL}core/public/js/widgets/current-user.js"></script>
        <script type="text/javascript">
            $(function() {
        	  new LMF.currentUserWidget(_SERVER_URL, document.getElementById("login_logout")).init();
        	});
        </script>
        ${HEAD}
        <title>LMF - The Linked Media Server</title>
        <style type="text/css">
        	#login_logout {
				float: right;
				margin: 5px;
        	}
        </style>
    </head>
    <body>
        <a id="top-link" href="${SERVER_URL}">TOPLINK</a>
        <div id="wrapper">
            <div id="header">
                <div id="logo">
                    <a href="${SERVER_URL}"><img src="${SERVER_URL}core/public/img/lmf-white.png" /></a>
                </div>
                <div id="header_text">
                	<h1>${CURRENT_TITLE}</h1>
                	<div id="topnav">
    	            	<div id="login_logout"></div>
    	            </div>
                </div>
            </div>
            <div class="clear"></div>
            <div id="left">
                ${MODULE_MENU}
            </div>
            <div id="center">
                ${CONTENT}
            </div>
            <div class="clear"></div>
            <div id="footer">
                <span>
                    <a href="http://lmf.googlecode.com">LMF</a> 
                    is a project of 
                    <a href="http://www.newmedialab.at/">SNML-TNG</a>
                </span>
            </div>
        </div>
    </body>
</html>
