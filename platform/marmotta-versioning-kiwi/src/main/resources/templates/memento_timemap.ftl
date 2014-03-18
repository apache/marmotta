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
<#-- @ftlvariable name="FOOTER" type="java.lang.String" -->
<#-- @ftlvariable name="LOGO" type="java.lang.String" -->
<#-- @ftlvariable name="SERVER_URL" type="java.lang.String" -->
<#-- @ftlvariable name="DEFAULT_STYLE" type="java.lang.String" -->
<#-- @ftlvariable name="baseUri" type="java.lang.String" -->
<#-- @ftlvariable name="versions" type="java.util.List<java.util.Map<String, String>" -->
<#-- @ftlvariable name="original" type="java.lang.String" -->
<!DOCTYPE html>
<html lang="en">

<head>
    <title>Timemap in HTML</title>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <script type="text/javascript" src="${SERVER_URL}webjars/jquery/1.8.2/jquery.min.js"></script>
    <link href="${SERVER_URL}${DEFAULT_STYLE}style.css" rel="stylesheet" type="text/css" />
    <link href="${SERVER_URL}${DEFAULT_STYLE}rdfhtml.css" rel="stylesheet" type="text/css" />

    <script type="text/javascript" src="${SERVER_URL}webjars/d3js/3.4.1/d3.min.js"></script>
    <script type="text/javascript" src="${SERVER_URL}webjars/timeknots/0.1/timeknots-min.js"></script>
</head>

<body>

<div id="wrapper">
    <div id="header">
        <a id="logo" href="${SERVER_URL}">
            <img src="${SERVER_URL}${LOGO}">
        </a>
        <h1 style="left:200px">Memento Timemap</h1>
        <div class="clean"></div>
    </div>
    <div id="center" style="width: 100%">
        <div id="content">
            <div id="timeknots" style="width:100%"></div>
<script type="text/javascript">
    $(function() {
        var target = "#timeknots",
            v = [
<#list versions as version>
            {'name':"${version.date}", 'date':new Date("${version.tstamp}"), 'uri':"${version.uri}"},
</#list>
            {'name':"now", 'date':new Date(),'lineWidth':1, 'uri':"${SERVER_URL}resource?uri=${original?url}"}
        ];

        function redraw() {
            var t = $(target).empty();
            TimeKnots.draw(target, v, {
                height: "50",
                width: t.innerWidth(),
                dateFormat: "%Y-%m-%d %H:%M:%S",
                color: "#0B61A4",
                showLabels: false,
                labelFormat: "%Y-%m-%d %H:%M:%S"
            });
            d3.select(target+" svg").selectAll('circle')
               .on("click", function(d) {
                    window.location.href = d.uri;
            });

        }
        $(window).resize(function() { redraw(); });

        redraw();
    });
</script>
            <table class="simple_table">
                <tr>
                    <th>Verions</th>
                </tr>
                <#list versions as version>
                <tr>
                    <td><a target="_blank" href="${version.uri}" class="ldcache">${version.date}</a></td>
                </tr>
            </#list>
            </table>
        </div>
    </div>

    <div class="clear"></div>
    <div id="footer">
        <div id="footer_line">
            <span>
            ${FOOTER}<br>
                The version access is following the <a href="http://www.mementoweb.org/">Memento</a> principles.
            </span>
        </div>
    </div>
</div>
</body>

</html>
