<%--

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

--%>
<html>

  <head>
        <title>Linked Media Framework</title>
        
        <script type="text/javascript" src="/LMF/webjars/jquery/1.8.2/jquery.min.js"></script>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta http-equiv="Default-Style" content="blue">
        <link href="/LMF/core/public/style/javadoc.css" rel="stylesheet" type="text/css" />
        <link href="/LMF/core/public/style/style.css" rel="stylesheet" type="text/css" />
        <link href="/LMF/core/public/style/scheme/blue.css" title="blue" rel="stylesheet" type="text/css" />
        <link href="/LMF/core/public/style/scheme/dark.css" title="dark" rel="alternate stylesheet" type="text/css" />
        <link href="/LMF/core/public/img/icon/lmf.ico" rel="SHORTCUT ICON" />

        <style type="text/css">
            .hidden {
                display: none;
            }
        </style>
  </head>

  <body>

    <div id="wrapper">

        <div id="header">
          <a id="logo" href="http://localhost:8080/">
            <img src="/LMF/core/public/img/logo/lmf-logo.png">
          </a>
        </div>

        <div id="center" class="clear" style="margin: 2em 5em 2em 5em; width: auto;">

          <div id="content" style="min-height: 600px;">

            <h1>Welcome to the Linked Media Framework</h1>

            <p>
                The Linked Media Framework (LMF) is an all-in-one solution for publishing your legacy data as Linked Data,
                connecting it with the Web of Data and build fascinating applications on top. The LMF can bundle several
                applications in a single installation to make your life easier. Currently, the following applications are
                installed:
            </p>

            <h2>Administration</h2>

            <ul id="service_list">
                <li id="service_lmf" class="hidden">
                    <a href="/LMF">Linked Media Framework</a> - the administration and debugging interface of the main
                    LMF server. Go here to configure LMF settings and access the data you stored.
                </li>
                <li id="service_stanbol" class="hidden">
                    <a href="/LMF/stanbol/config/">Apache Stanbol</a> - administration and debugging interface of the information
                    extraction tool Apache Stanbol. Go here if you need to customize Stanbol settings or want to
                    experiment with Stanbol.
                </li>
                <li><a href="http://code.google.com/p/lmf/wiki/TableOfContents">Documentation</a> - the complete documentation for
                    the Linked Media Framework, including module descriptions, guides, and background information.</li>
            </ul>
            
            
            <h2>Applications</h2>
            
            <ul id="application_list">
                <li id="service_refine" class="hidden">
                    <a href="/refine">Open Refine</a> - customized version of the data management tool Open Refine,
                    including RDF and LMF extensions, preconfigured for the LMF. Go here if you want to publish legacy
                    data as Linked Data.
                </li>
                <li id="application_skosjs" class="hidden">
                    <a href="/LMF/skos/index.html">SKOSjs</a> - a SKOS thesaurus manager building on top of the
                    Linked Media Framework. Go here to create and manage your own thesauruses or import existing
                    thesauruses.
                </li>
            </ul>

            <h2>Demos</h2>

            <ul id="demo_list">
                <li id="demo_books" class="hidden">
                    <a href="/LMF/demo-books/about.html">LMF Books Demo</a> - start from a legacy CSV data file and
                    convert it into a Semantic Search including enriched information from the Linked Data Cloud.
                    Demonstrates Linked Data Caching, Thesaurus Management, Reasoning, and Semantic Search.
                </li>
            </ul>

          </div>

        </div>

        <div class="clear"></div>

        <div id="footer">
            <div id="footer_line">
                <span>
                    <a href="http://lmf.googlecode.com">LMF</a> is a project of <a href="http://www.newmedialab.at/">Salzburg NewMediaLab</a>. <br/>
                    Copyright &copy; 2013 <a href="http://www.salzburgresearch.at">Salzburg Research</a>. <br/>
                    Licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License, Version 2.0.</a>
            </span>
            </div>
        </div>

    </div>

    <script type="text/javascript">
        var checkService = function(uri, callback) {
            $.ajax({
                url: uri,
                type: "HEAD",
                success: callback,
                error: function(jqXHR, textStatus, errorThrown) {

                }
            });
        };

        $(document).ready(function() {

            checkService("/LMF",function(data) {
                $('#service_lmf').removeClass("hidden");
            });

            checkService("/refine",function(data) {
                $('#service_refine').removeClass("hidden");
            });

            checkService("/LMF/stanbol/config/entityhub",function(data) {
                $('#service_stanbol').removeClass("hidden");
            });

            checkService("/LMF/skos/index.html",function(data) {
                $('#application_skosjs').removeClass("hidden");
            });

            checkService("/LMF/demo-books/about.html",function(data) {
                $('#demo_books').removeClass("hidden");
            });

            checkService("/LMF/demo-social/index.gsp",function(data) {
                $('#demo_social').removeClass("hidden");
            });

        })
    </script>

  </body>

</html>
