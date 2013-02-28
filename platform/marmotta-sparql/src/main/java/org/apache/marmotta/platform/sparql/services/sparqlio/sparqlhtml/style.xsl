<?xml version="1.0"?>
<!--

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
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:res="http://www.w3.org/2005/sparql-results#"
		exclude-result-prefixes="res xsl">

  <!--
    <xsl:output
    method="html"
    media-type="text/html"
    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
    indent="yes"
    encoding="UTF-8"/>
  -->

  <!-- or this? -->

  <xsl:output
   method="xml" 
   indent="yes"
   encoding="UTF-8" 
   doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
   doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
   omit-xml-declaration="no" />

  <xsl:param name="serverurl" select="'http://localhost:8080/LMF/'"/>

  <xsl:template name="header">
    <div>
      <h2>Header</h2>
      <xsl:for-each select="res:head/res:link"> 
	<p>Link to <xsl:value-of select="@href"/></p>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template name="boolean-result">
    <div>
      <!--      
	<h2>Boolean Result</h2>
      -->      
      <p>ASK => <xsl:value-of select="res:boolean"/></p>
    </div>
  </xsl:template>


  <xsl:template name="vb-result">
    <div>
      <!--
	<h2>Variable Bindings Result</h2>
	<p>Ordered: <xsl:value-of select="res:results/@ordered"/></p>
	<p>Distinct: <xsl:value-of select="res:results/@distinct"/></p>
      -->

      <table>
	<xsl:text>
	</xsl:text>
	<tr style="background-color: #006D8F;color: white;">
	  <xsl:for-each select="res:head/res:variable">
	    <th><xsl:value-of select="@name"/></th>
	  </xsl:for-each>
	</tr>
	<xsl:text>
	</xsl:text>
	<xsl:for-each select="res:results/res:result">
        <xsl:choose>
		<xsl:when test="(position() mod 2) = 0">
	    <tr style="background-color:lightblue;">
	        <xsl:apply-templates select="."/>
	    </tr>
        </xsl:when>
        <xsl:otherwise>
        <tr style="background-color:#DFF7FF;">
	        <xsl:apply-templates select="."/>
	    </tr>
        </xsl:otherwise>
        </xsl:choose>
	</xsl:for-each>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="res:result">
    <xsl:variable name="current" select="."/>
    <xsl:for-each select="//res:head/res:variable">
      <xsl:variable name="name" select="@name"/>
      <td>
	<xsl:choose>
	  <xsl:when test="$current/res:binding[@name=$name]">
	    <!-- apply template for the correct value type (bnode, uri, literal) -->
	    <xsl:apply-templates select="$current/res:binding[@name=$name]"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <!-- no binding available for this variable in this solution -->
	  </xsl:otherwise>
	</xsl:choose>
      </td>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="res:bnode">
    <xsl:text>_:</xsl:text>
    <xsl:value-of select="text()"/>
  </xsl:template>

  <xsl:template match="res:uri">
    <xsl:variable name="uri" select="text()"/>
    <a href="{$serverurl}resource?uri={$uri}">
        <xsl:text>&lt;</xsl:text>
            <xsl:value-of select="$uri"/>
        <xsl:text>&gt;</xsl:text>
    </a>
  </xsl:template>

  <xsl:template match="res:literal">
    <xsl:text>"</xsl:text>
    <xsl:value-of select="text()"/>
    <xsl:text>"</xsl:text>

    <xsl:choose>
      <xsl:when test="@datatype">
	<!-- datatyped literal value -->
	^^&lt;<xsl:value-of select="@datatype"/>&gt;
      </xsl:when>
      <xsl:when test="@xml:lang">
	<!-- lang-string -->
	@<xsl:value-of select="@xml:lang"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="res:sparql">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
	<title>SPARQL Query Results</title>
	<style>
	  <![CDATA[
	  h1 { font-size: 150% ; }
	  h2 { font-size: 125% ; }
	  table { border-collapse: collapse ; border: 2px solid white ; }
	  td, th
 	  { border: 2px solid white ;
	    padding-left:0.5em; padding-right: 0.5em; 
	    padding-top:0.2ex ; padding-bottom:0.2ex 
	  }

	  ]]>
	</style>
      </head>
      <body>


	<h1>SPARQL Query Results</h1>

	<xsl:if test="res:head/res:link">
	  <xsl:call-template name="header"/>
	</xsl:if>

	<xsl:choose>
	  <xsl:when test="res:boolean">
	    <xsl:call-template name="boolean-result" />
	  </xsl:when>

	  <xsl:when test="res:results">
	    <xsl:call-template name="vb-result" />
	  </xsl:when>

	</xsl:choose>


      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
