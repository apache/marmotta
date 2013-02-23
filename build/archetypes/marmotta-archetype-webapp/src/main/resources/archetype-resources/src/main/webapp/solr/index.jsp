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
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%--
  ~ Copyright (c) 2008-2012 Salzburg Research.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<html>
<head>
<link rel="stylesheet" type="text/css" href="solr-admin.css">
<link rel="icon" href="favicon.ico" type="image/ico"></link>
<link rel="shortcut icon" href="favicon.ico" type="image/ico"></link>
<title>Welcome to Solr</title>
</head>

<body>
<h1>Welcome to Solr!</h1>
<a href="."><img border="0" align="right" height="78" width="142" src="admin/solr_small.png" alt="Solr"/></a>

<% 
  org.apache.solr.core.CoreContainer cores = (org.apache.solr.core.CoreContainer)request.getAttribute("org.apache.solr.CoreContainer");
  if( cores != null
   && cores.getCores().size() > 0 // HACK! check that we have valid names...
   && cores.getCores().iterator().next().getName().length() != 0 ) { 
    for( org.apache.solr.core.SolrCore core : cores.getCores() ) {%>
<a href="<%= core.getName() %>/admin/">Admin <%= core.getName() %></a><br/>
<% }} else { %>
<a href="admin">Solr Admin</a>
<% } %>
<!--<a href="admin/shortcuts/search.gsp">SOLR shortcuts</a>--><br/>

</body>
</html>
