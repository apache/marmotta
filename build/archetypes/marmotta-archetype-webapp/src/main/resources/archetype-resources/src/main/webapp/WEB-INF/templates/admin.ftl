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

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link href="${SERVER_URL}${DEFAULT_STYLE}javadoc.css" rel="stylesheet" type="text/css" />
    <link href="${SERVER_URL}${DEFAULT_STYLE}style.css" rel="stylesheet" type="text/css" />
    <link href="${SERVER_URL}core/public/img/icon/marmotta.ico" rel="SHORTCUT ICON">
    <script type="text/javascript">
        var _BASIC_URL = "${BASIC_URL}";
        //use _SERVER_URL for webservice calls
        var _SERVER_URL = "${SERVER_URL}";

        var _CURRENT_STYLE = "${DEFAULT_STYLE}";

    </script>
    <#if USER_MODULE_IS_ACTIVE>
        <link href="${SERVER_URL}user/admin/style/style.css" rel="stylesheet" type="text/css">
        <script type="text/javascript" src="${SERVER_URL}user/admin/widgets/user.js"></script>
        <script type="text/javascript">
            window.onload = function () {
                    LoginLogout.draw(_SERVER_URL,"login_logout");
            }
        </script>
    </#if>

    ${HEAD}

</head>

<body>

<div id="wrapper">
    <div id="header">
        <a id="logo" href="${SERVER_URL}" title="${PROJECT}">
            <img src="${SERVER_URL}${LOGO}" alt="${PROJECT} logo" />
        </a>
        <h1>${CURRENT_MODULE} - ${CURRENT_TITLE}</h1>
        <#if USER_MODULE_IS_ACTIVE>
            <div id="login_logout"></div>
        </#if>
    </div>
    <div class="clear"></div>
    <div id="left">
        <ul id="menu">
            <#list MODULE_MENU as menu>
            <li
                <#if menu.properties["active"]>
                        class="menu_item active"
                <#else>
                        class="menu_item"
                </#if>
            >
                <div class="menu_heading"><a href="${menu.submenu[0].properties["path"]}">${menu.properties["title"]}</a></div>
                <ul class="submenu">
                <#list menu.submenu as submenu>
                    <li
                        <#if submenu.properties["active"]> class="active" </#if>
                    >
                    <a href="${submenu.properties["path"]}">${submenu.properties["title"]}</a>
                    </li>
                </#list>
                </ul>
            </li>
            </#list>
        </ul>
    </div>
    <div id="center">
        <div id="content">
        ${CONTENT}
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

