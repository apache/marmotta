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
<html lang="en" prefix="${prefixMappings}">

<head>
  <title>Resource/s in HTML</title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
  <script type="text/javascript" src="${baseUri}core/public/js/lib/jquery-1.7.2.js"></script>
  <script type="text/javascript" src="${baseUri}core/public/js/lib/jquery-ui-1.8.21.js"></script>
  <link href="${baseUri}core/public/style/style1.css" title="screen" rel="stylesheet" type="text/css" />
  <link href="${baseUri}core/public/style/rdfhtml.css" title="screen" rel="stylesheet" type="text/css" />
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

<#function zebra index>
  <#if (index % 2) == 0>
    <#return "even" />
  <#else>
    <#return "odd" />
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

<div id="tabs">

    <ul>
    
        <li><a href="#tab-raw-triples">raw triples</a></li>
        
        <#if resources?size = 1>
        <li><a href="#tab-inspection">inspection</a></li>
        </#if>  
        
    </ul>
    
    <div id="tab-raw-triples">
   
        <#if resources?has_content>
          <#list resources as resource>
            <div class="subheader">
              <h3>Local description of <a href="${resource.uri}" class="ldcache">${resource.uri}</a>:</h3>
            </div>
            <table>
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
                <td><a href="${triple.context.curie}">${triple.context.curie}</a></td>
                <td>${triple.info}</td>
              </tr>
              </#list>
            </table>
            <p id="rawrdf">
              Get this resource in raw RDF: 
              <a href="${baseUri}resource?uri=${resource.encoded_uri}&amp;format=application/rdf%2Bxml">RDF/XML</a>, 
              <a href="${baseUri}resource?uri=${resource.encoded_uri}&amp;format=text/rdf%2Bn3">N3</a>, 
              <a href="${baseUri}resource?uri=${resource.encoded_uri}&amp;format=text/turtle">Turtle</a>, 
              <a href="${baseUri}resource?uri=${resource.encoded_uri}&amp;format=application/rdf%2Bjson">RDF/JSON</a>, 
              <a href="${baseUri}resource?uri=${resource.encoded_uri}&amp;format=application/json">JSON-LD</a>
            </p>
          </#list>
        <#else> 
          <div class='subheader'>
            <h3>No local triples to display!</h3>
          </div>
        </#if>   
        
    </div>
    
    <#if resources?size = 1>
    <div id="tab-inspection">
        <div class="subheader">
            <h3>Inspection of <a href="${resources[0].uri}" class="ldcache">${resources[0].uri}</a>:</h3>
        </div>
        <div class="introspectionDetails">
            <h4><a href="${resources[0].uri}" class="ldcache">${resources[0].uri}</a> as Subject</h4>
            <button id="s0">|&lt;</button>
            <button id="s1">&lt;</button>
        	<button id="s2">&gt;</button>
        	<button id="s3">+</button>
        	<button id="s4">-</button>
        	<table id="inspect_subject">
        	  <tr class="trClassHeader">
        	    <th>Subject</th>
        	    <th>Property</th>
        	    <th>Object</th>
        	    <th>Context<th>
        	  </tr>
        	</table>
        </div>
        <div class="introspectionDetails">
            <h4><a href="${resources[0].uri}" class="ldcache">${resources[0].uri}</a> as Property</h4>
        	<table id="inspect_property">
              <tr class="trClassHeader">
                <th>Subject</th>
                <th>Property</th>
                <th>Object</th>
                <th>Context<th>
              </tr>
        	</table>
        </div>
        <div class="introspectionDetails">
            <h4><a href="${resources[0].uri}" class="ldcache">${resources[0].uri}</a> as Object</h4>
        	<table id="inspect_object">
              <tr class="trClassHeader">
                <th>Subject</th>
                <th>Property</th>
                <th>Object</th>
                <th>Context<th>
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

<div id="footer" class="clear">
    <span><abbr title="Linked Media Framework">LMF</abbr> is a project of <a href="http://www.newmedialab.at/">SNML-TNG</a></span>
</div>

<script type="text/javascript"> 

  $(document).ready(function() {

    $("div#tabs").tabs();
    
    $("a.ldcache").each(function(index) { 
      $(this).click(function() { 
        window.location.href = "${baseUri}resource?uri=" + encodeURIComponent($(this).attr("href")); 
        return false; 
      }); 
    });    
    
    function loader(uri, type, target) {
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
            target: $(target),
            offset: 0,
            limit: 10,
            fetch: function() {
                var self = this;
                $.getJSON("${baseUri}inspect/" + type, {uri: self.resource, start: self.offset, limit: self.limit}, function(data) {
                    //self.target.empty();
                    for( var i in data) {
                        var t = data[i];
                        self.target.append(createRow(t, zebra(i)));                                     
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
    var subj = $("table#inspect_subject tbody");
    var subjLoader = new loader("${resources[0].uri}", "subject", subj);
    subjLoader.fetch();
    $("#s0").click(function() {subjLoader.first();});
    $("#s1").click(function() {subjLoader.prev();});
    $("#s2").click(function() {subjLoader.next();});
    $("#s3").click(function() {subjLoader.more();});
    $("#s4").click(function() {subjLoader.less();});

    var prop = $("table#inspect_property tbody");
    var propLoader = new loader("${resources[0].uri}", "predicate", prop);
    propLoader.fetch();

    var obj = $("table#inspect_object tbody");
    var objLoader = new loader("${resources[0].uri}", "object", obj);
    objLoader.fetch();    
    </#if> 

  });

</script> 

</body>

</html>
