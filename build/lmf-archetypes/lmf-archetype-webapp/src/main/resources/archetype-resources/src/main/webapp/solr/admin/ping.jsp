#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@ page contentType="text/xml; charset=utf-8" pageEncoding="UTF-8" language="java" %>

<%--
  ~ Copyright (c) 2012 Salzburg Research.
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
<%@ page import="org.apache.solr.request.SolrQueryRequest,
                 org.apache.solr.request.SolrQueryResponse"%>

<%@include file="_info.jsp" %>
<?xml-stylesheet type="text/xsl" href="ping.xsl"?>

<solr>
  <core><%=core.getName()%></core>
  <ping>
<%
  SolrQueryRequest req = core.getPingQueryRequest();
  SolrQueryResponse resp = new SolrQueryResponse();
  try {
    core.execute(req,resp);
    if (resp.getException() == null) {
// No need for explicit status in the body, when the standard HTTP
// response codes already transmit success/failure message
      out.println("<status>200</status>");
    }
    else if (resp.getException() != null) {
     throw resp.getException();
    }
  } catch (Throwable t) {
     // throw t;
  } finally {
      req.close();
  }
%>
  </ping>
</solr>
