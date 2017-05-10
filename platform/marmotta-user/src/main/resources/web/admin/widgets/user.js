/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Created with IntelliJ IDEA.
 * User: tkurz
 * Date: 24.01.13
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */

var LoginLogout = {
    
    draw : function(basic_url, container) {

        function getUser(url) {
            var xmlHttp = new XMLHttpRequest();
            xmlHttp.open( "GET", url, false );
            xmlHttp.send( null );
            return xmlHttp.responseText;
        }
        var user = eval('('+getUser(basic_url+"user/me")+')');

        console.log("current login: " + user.login);
        var link = document.createElement("a");
        if(user.login=="anonymous") {
            link.innerHTML = "login";
            link.setAttribute("href", basic_url+"user/login");
        } else {
            link.innerHTML = "logout";
            link.setAttribute("href", basic_url+"user/logout");
            document.getElementById(container).innerHTML =
                "<span><a href='"+basic_url+"user/me.html'>"+user.login+"</a></span>&nbsp;|&nbsp;";
        }
        document.getElementById(container).appendChild(link);

    }

}