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
<html lang="en" prefix="${prefixMappings}" xmlns="http://www.w3.org/1999/html">

<head>
  <title>Resource/s in HTML</title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
  <meta http-equiv="Default-Style" content="${DEFAULT_STYLE}">
  <script type="text/javascript" src="${SERVER_URL}webjars/jquery/1.8.2/jquery.min.js"></script>
  <link href="${SERVER_URL}${DEFAULT_STYLE}style.css" rel="stylesheet" type="text/css" />
  <link href="${SERVER_URL}${DEFAULT_STYLE}rdfhtml.css" rel="stylesheet" type="text/css" />
  <#if resources?size = 1>
  <link rel="alternate" type="application/rdf+xml" href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=application/rdf%2Bxml" /> 
  <link rel="alternate" type="text/rdf+n3" href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=text/rdf%2Bn3" /> 
  <link rel="alternate" type="text/turtle" href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=text/turtle" /> 
  <link rel="alternate" type="application/rdf+json" href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=application/rdf%2Bjson" /> 
  <link rel="alternate" type="application/ld+json" href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=application/ld%2Bjson" /> 
  </#if>
      
  <script>
      $(document).ready(function(){
          $(".submenu li").click(function(event){
              event.preventDefault();
              if(!$(this).hasClass("active")) {
                  $(".submenu").children().removeClass("active");
                  $(this).addClass("active");
                  var tabid = $(this).children(":first").attr("href").substring(1);
                  $("#content").children().hide();
                  $("#"+tabid).show();
              }
          })

          $("a.ldcache").each(function(index) {
              $(this).click(function() {
                  window.location.href = "${SERVER_URL}resource?uri=" + encodeURIComponent($(this).attr("href"));
                  return false;
              });
          });

          function loader(uri, type, target) {

              var table = $("#"+target);

              function linkify(text) {
                  var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
                  return text.replace(exp,"<a href='$1' class='ldcache'>$1</a>");
              }
              function zebra(index) {
                  return ( index % 2 ? "even": "odd" );
              }
              function createRow(data, cssClass) {
                  return $("<tr>", {})
                          .append($("<td>", {html: linkify(data.s)}))
                          .append($("<td>", {html: linkify(data.p)}))
                          .append($("<td>", {html: linkify(data.o)}))
                          .append($("<td>", {html: linkify(data.c)}))
                          .addClass(cssClass);
              }
              return {
                  resource: uri,
                  target: table,
                  offset: 0,
                  limit: 10,
                  fetch: function() {
                      var self = this;
                      $.getJSON("${SERVER_URL}inspect/" + type, {uri: self.resource, start: self.offset, limit: self.limit}, function(data) {
                          if(data.length == 0) {
                              console.log(table);
                              table.replaceWith("<p class='no_triples'>No triples to display</p>");
                          }
                          for( var i in data) {
                              var t = data[i];
                              table.append(createRow(t, zebra(i)));
                          }
                      });
                  },
                  next: function(step) {
                      step = step || this.limit;
                      this.offset += step;
                      this.fetch();
                  },
                  prev: function(step) {
                      step = step || this.limit
                      this.offset = Math.max(this.offset - step, 0);
                      this.fetch();
                  },
                  more: function() {
                      this.limit += 5;
                      this.fetch();
                  },
                  less: function() {
                      this.limit = Math.max(this.limit - 5, 5);
                      this.fetch();
                  },
                  first: function() {
                      this.offset = 0;
                      this.fetch();
                  }
              };
          }

      <#if resources?size = 1>
          var subjLoader = new loader("${resources[0].uri}", "subject", "inspect_subject");
          subjLoader.fetch();
          $("#s0").click(function() {subjLoader.first();});
          $("#s1").click(function() {subjLoader.prev();});
          $("#s2").click(function() {subjLoader.next();});
          $("#s3").click(function() {subjLoader.more();});
          $("#s4").click(function() {subjLoader.less();});

          var propLoader = new loader("${resources[0].uri}", "predicate", "inspect_property");
          propLoader.fetch();

          var objLoader = new loader("${resources[0].uri}", "object", "inspect_object");
          objLoader.fetch();
      </#if>

      })
  </script>
</head>

<body>
<#function zebra index>
    <#if (index % 2) == 0>
        <#return "odd" />
    <#else>
        <#return "even" />
    </#if>
</#function>

<#function cacheClass object>
    <#if object.cache?has_content>
        <#return "ldcache" />
    <#else>
        <#return "" />
    </#if>
</#function>

<#function rdfaAttributes object>
    <#return "${rdfaDatatype(object)} ${rdfaLanguage(object)}" />
</#function>

<#function rdfaDatatype object>
    <#if object.datatype?has_content>
        <#return "datatype=\"${object.datatype}\"" />
    <#else>
        <#return "" />
    </#if>
</#function>

<#function rdfaLanguage object>
    <#if object.lang?has_content>
        <#return "lang=\"${object.lang}\"" />
    <#else>
        <#return "" />
    </#if>
</#function>

<div id="wrapper">
    <div id="header">
        <a id="logo" href="${SERVER_URL}" title="${PROJECT}">
            <img src="${SERVER_URL}${LOGO}" alt="${PROJECT} logo" />
        </a>
        <h1>RDF/HTML</h1>
        <#if resources?size = 1>
        <div id="top_serialisation_links">
            <a href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=application/rdf%2Bxml">RDF/XML</a>&nbsp;|&nbsp;
            <a href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=text/rdf%2Bn3">N3</a>&nbsp;|&nbsp;
            <a href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=text/turtle">Turtle</a>&nbsp;|&nbsp;
            <a href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=application/rdf%2Bjson">RDF/JSON</a>&nbsp;|&nbsp;
            <a href="${SERVER_URL}resource?uri=${resources[0].encoded_uri}&amp;format=application/ld%2Bjson">JSON-LD</a>
        </div>
        <div class="clean"></div>
        </#if>
    </div>
    <div class="clear"></div>
    <div id="left">
    <ul id="menu">
        <li class="menu_item">
        <div class="menu_heading">Views</div>
        <ul class="submenu">
                <li class="active"><a href="#tab-raw-triples">Triples</a></li>
                <#if resources?size = 1>
                <li><a href="#tab-inspection">Inspector</a></li>
                </#if>
            </ul>
        </li>
    </ul>
    </div>
    <div id="center">
        <div id="content">
            <div id="tab-raw-triples">

            <h1>Triples</h1>
            <#if resources?has_content>
                <#list resources as resource>
                    <h2><a href="${resource.uri}" class="ldcache">${resource.uri}</a>
                        <#if timemaplink??>
                            <a style="float:right" id="timemap_link" href="${SERVER_URL}${timemaplink}${resource.uri}">
                                <img style="width: 24px" title="browser versions" alt="memento" src="${SERVER_URL}core/public/img/icon/memento_logo_128.png">
                            </a>
                        </#if>
                    </h2>
                    <table class="simple_table">
                        <tr class="trClassHeader">
                            <th>property</th>
                            <th>has value</th>
                            <th>context</th>
                            <th id="info">info</th>
                        </tr>
                        <#list resource.triples as triple>
                            <tr class="${zebra(triple_index)}">
                                <td><a href="${triple.predicate.uri}" class="ldcache">${triple.predicate.curie}</a></td>
                                <td about="${resource.uri}">
                                    <#if triple.object.uri?has_content>
                                        <a rel="${triple.predicate.curie}" href="${triple.object.uri}" class="${cacheClass(triple.object)}">${triple.object.curie}</a>
                                    <#else>
                                        <span property="${triple.predicate.curie}" ${rdfaAttributes(triple.object)}>${triple.object.value}</span>
                                    </#if>
                                </td>
                                <td><a href="${triple.context.uri}">${triple.context.curie}</a></td>
                                <td>${triple.info}</td>
                            </tr>
                        </#list>
                    </table>
                    <#if resources?size != 1>
                    <p id="rawrdf">
                        Get this resource in raw RDF:
                        <a href="${SERVER_URL}resource?uri=${resource.encoded_uri}&amp;format=application/rdf%2Bxml">RDF/XML</a>,
                        <a href="${SERVER_URL}resource?uri=${resource.encoded_uri}&amp;format=text/rdf%2Bn3">N3</a>,
                        <a href="${SERVER_URL}resource?uri=${resource.encoded_uri}&amp;format=text/turtle">Turtle</a>,
                        <a href="${SERVER_URL}resource?uri=${resource.encoded_uri}&amp;format=application/rdf%2Bjson">RDF/JSON</a>,
                        <a href="${SERVER_URL}resource?uri=${resource.encoded_uri}&amp;format=application/ld%2Bjson">JSON-LD</a>
                    </p>
                    </#if>
                </#list>
            <#else>
                <p>
                    No local triples to display!
                </p>
            </#if>

            </div>

        <#if resources?size = 1>
            <div id="tab-inspection" style="display: none">
                <h1>Inspection of <a href="${resources[0].uri}" class="ldcache">${resources[0].uri}</a></h1>
                <div class="introspectionDetails">
                    <h2>Resource as Subject</h2>
                    <div id="table_buttons">
                    <button id="s0">|&lt;</button>
                    <button id="s1">&lt;</button>
                    <button id="s2">&gt;</button>
                    <button id="s3">+</button>
                    <button id="s4">-</button>
                    </div>
                    <table id="inspect_subject" class="simple_table">
                        <tr class="trClassHeader">
                            <th>Subject</th>
                            <th>Property</th>
                            <th>Object</th>
                            <th>Context</th>
                        </tr>
                    </table>
                </div>
                <div class="introspectionDetails">
                    <h2>Resource as Property</h2>
                    <table id="inspect_property" class="simple_table">
                        <tr class="trClassHeader">
                            <th>Subject</th>
                            <th>Property</th>
                            <th>Object</th>
                            <th>Context</th>
                        </tr>
                    </table>
                </div>
                <div class="introspectionDetails">
                    <h2>Resource as Object</h2>
                    <table id="inspect_object" class="simple_table">
                        <tr class="trClassHeader">
                            <th>Subject</th>
                            <th>Property</th>
                            <th>Object</th>
                            <th>Context</th>
                        </tr>
                    </table>
                </div>
                <!--
                <div class="introspectionDetails">
                    <h4><a href="${resources[0].uri}" class="ldcache">${resources[0].uri}</a> as Context</h4>
                	<table id="inspect_context">
                      <tr class="trClassHeader">
                        <th>Subject</th>
                        <th>Property</th>
                        <th>Object</th>
                        <th>Context<th>
                      </tr>
                	</table>
                </div>
                -->
            </div>
        </#if>
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
</body>

</html>

