<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Apache Marmotta</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="${SERVER_URL}core/public/img/icon/lmf.ico" rel="SHORTCUT ICON">
    <script src="${SERVER_URL}core/public/js/lib/jquery-1.7.2.js"></script>
    <!-- JS -->
    <script type="text/javascript">
        var _BASIC_URL = "${BASIC_URL}";
        //use _SERVER_URL for webservice calls
        var _SERVER_URL = "${SERVER_URL}";

        var _CURRENT_STYLE = "${DEFAULT_STYLE}";
    </script>

    <!-- TODO could be nicer-->
    <link href="${SERVER_URL}${DEFAULT_STYLE}javadoc.css" rel="stylesheet" type="text/css" />

    <!-- Bootstrap -->
    <link href="${SERVER_URL}ext/bootstrap/css/bootstrap.css" rel="stylesheet" media="screen">
    <link href="${SERVER_URL}core/public/style/screen.css" rel="stylesheet">

    <link href='http://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'>
    ${HEAD}
</head>

<body>

<div class="navbar navbar-inverse navbar-fixed-top menu-level-1">
    <div class="navbar-inner">
        <div class="container">
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="brand" href="#"></a>

            <div class="nav-collapse collapse">
                <p class="navbar-text pull-right" id="login_logout">
                    <!--Logged in as <a href="#" class="navbar-link">Username</a>-->
                </p>
                <#if USER_MODULE_IS_ACTIVE>
                    <script type="text/javascript" src="${SERVER_URL}user/admin/widgets/user.js"></script>
                    <script type="text/javascript">
                        window.onload = function () {
                            LoginLogout.draw(_SERVER_URL,"login_logout");
                        }
                    </script>
                </#if>
                <!-- TODO: which links should be displayed -->
                <ul class="nav">
                    <li class="active"><a href="${SERVER_URL}">Home</a></li>
                    <li><a href="#about">About</a></li>
                    <li><a href="#contact">Contact</a></li>
                </ul>
            </div><!--/.nav-collapse -->

        </div>
    </div>
</div>

<div class="well sidebar-nav square-corners menu-level-2 span3 full-height">
    <ul class="nav nav-list">
        <#list MENU["items"] as menu>
        <li class="nav-header">${menu.label}</li>
        <#list menu["items"] as submenu>
            <li
                <#if submenu["isActive"]> class="active" </#if>
                    >
                <#if submenu["items"]?has_content>
                        <a href="${SERVER_URL}${submenu["items"][0]["path"]?substring(1)}">
                <#else>
                        <a href="${SERVER_URL}doc/rest/index.html">
                </#if>
                    <i class="${submenu["icon"]}"></i>  <!-- TODO icon -->
                    <span>${submenu["label"]}</span>
                </a>
            </li>
        </#list>
        </#list>
    </ul>
</div><!--/.well -->

<div class="menu-level-3 row">
    <div class="offset3 span9">

        <ul class="nav nav-tabs square-corners">
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
            <!--<li class="active">
                <a href="#">Configuration</a>
            </li>
            <li><a href="#">Task</a></li>
            <li><a href="#">Import</a></li>
            <li><a href="#">Export</a></li>
            <li><a href="#">Dataview</a></li>
            <li><a href="#">Context</a></li>
            <li><a href="#">Prefixes</a></li>
            <li class="dropdown">
                <a class="dropdown-toggle"
                   data-toggle="dropdown"
                   href="#">
                    More
                    <b class="caret"></b>   ???
                </a>
                <ul class="dropdown-menu">
                    <li><a href="#">System</a></li>
                    <li><a href="#">Databases</a></li>
                    <li><a href="#">Web Services</a></li>
                </ul>
            </li> -->
        </ul>
    </div>
</div>

<div id="content" class="row content">
    <div class="offset3 span9">
        <div class="row">
            ${CONTENT}
        </div>
    </div>
    <!--


            <div class="span9">
                <div class="hero-unit">
                    <h1>Export Data</h1>
                    <p>The following page allows you to export and download RDF data contained in the Linked Media Framework. You can choose from different serialization formats and either export all RDF data or only the RDF data contained in a certain context (named graph). If you want to download the data instead of displaying in the browser, right-click the download link and choose "save as".</p>
                </div>
            </div>

            <div class="span4">
                <div class="hero-unit">
                    <form>
                    <fieldset>
                            <legend>Legend</legend>
                            <div class="field-box">
                                <label>Format</label>
                                <select>
                                    <option>Application ld+json</option>
                                    <option>Application ld+json</option>
                                    <option>Application ld+json</option>
                                    <option>Application ld+json</option>
                                    <option>Application ld+json</option>
                                </select>
                            </div>
                            <div class="field-box">
                                <label>Context</label>
                                <select>
                                    <option>Application ld+json</option>
                                    <option>Application ld+json</option>
                                    <option>Application ld+json</option>
                                    <option>Application ld+json</option>
                                    <option>Application ld+json</option>
                                </select>
                            </div>
                            <div class="field-box">
                                <label></label>
                                <button type="submit" class="btn btn-primary btn-large">Download</button>
                            </div>
                        </fieldset>
                    </form>
                </div>
            </div>

        </div>  -->
    </div>

        <!-- ?FOOTER? -->
</body>

<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="${SERVER_URL}ext/bootstrap/js/bootstrap.js"></script>

</html>
