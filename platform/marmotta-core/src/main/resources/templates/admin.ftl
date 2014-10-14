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
        <#if USER_MODULE_IS_ACTIVE>
            <div id="login_logout"></div>
        </#if>
    </div>
    <div class="clear"></div>
    <div id="left">
        <ul id="menu">
        <#list MENU["items"] as menu>
            <li class="menu_item">
                <div class="menu_heading">${menu.label}</div>
                <ul class="submenu">
                    <#list menu["items"] as submenu>
                        <li
                            <#if submenu["isActive"]> class="menu_item active" </#if>
                                >
                            <#if submenu["items"]?has_content>
                            <a href="${SERVER_URL}${submenu["items"][0]["path"]?substring(1)}">
                            <#else>
                            <a href="${SERVER_URL}doc/rest/index.html">
                            </#if>
                            <!-- <i class="${submenu["icon"]}"></i>  <!-- TODO icon -->
                            <span>${submenu["label"]}</span>
                        </a>
                        </li>
                    </#list>
                </ul>
            </li>
        </#list>

        </ul>
    </div>
    <div id="center">
        <ul class="center_submenu">
        <#list MENU["items"] as menu>
            <#if menu["isActive"]>
                <#list menu["items"] as submenu>
                    <#if submenu["isActive"]>
                        <#list submenu["items"] as pages>
                            <li
                                <#if pages["isActive"]> class="active" </#if>
                                    >
                                <a href="${SERVER_URL}${pages["path"]?substring(1)}">${pages["label"]}</a>
                            </li>
                        </#list>
                    </#if>
                </#list>
            </#if>
        </#list>
        </ul>
        <div class="clear"></div>
        <div id="content" class="contentWrap">
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
