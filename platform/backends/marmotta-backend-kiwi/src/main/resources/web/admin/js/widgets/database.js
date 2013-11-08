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

function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

(function( $ ){
    $.fn.lmf_database = function(options) {
        var settings = {
	        host: 'http://localhost:8080/LMF/',
            anker_name: 'database',
            title: 'Database Configuration',
            loading_img: '../public/img/loader/ajax-loader_small.gif',
            toplink:true
        }

        //jquery elements
        var table;

        //basically there are 2 functions
        //1. getValues from server and write it to inputFields
        //2. storeValues after testing Configuration
        var functions = {
            input_fields:undefined,
            getValues : function(callback) {

                var writeValues = function(databases, values) {
                    table.html("");
                    this.input_fields={};

                    //DB Type
                    var tr1 = $("<tr/>");
                    var td11 = $("<td/>").css({"font-weight":"bold"}).text("Database: ");
                    var td12 = $("<td/>");
                    this.input_fields.db_select = $("<select/>").css({'width':'200px'});
                    for (value in databases) {
                        this.input_fields.db_select.append("<option>" + databases[value] + "</option>");
                    }
                    this.input_fields.db_select.val(values["database.type"]);
                    td12.append(this.input_fields.db_select);
                    this.input_fields.l_div = $("<div style='float:right;margin-top: -55px;'></div>");
                    table.parent().append(this.input_fields.l_div);
                    td11.appendTo(tr1);
                    td12.appendTo(tr1);
                    tr1.appendTo(table);

                    //DBHost
                    var tr2 = $("<tr/>");
                    var td21 = $("<td/>").css({"font-weight":"bold"}).text("Host: ");
                    var td22 = $("<td/>");
                    this.input_fields.db_host = $("<input type='text' style='width:100%'/>");
                    this.input_fields.db_host.val(values["database.url"]);
                    td22.append(this.input_fields.db_host);
                    td21.appendTo(tr2);
                    td22.appendTo(tr2);
                    tr2.appendTo(table);

                    //DBUser
                    var tr3 = $("<tr/>");
                    var td31 = $("<td/>").css({"font-weight":"bold"}).text("User: ");
                    var td32 = $("<td/>");
                    this.input_fields.db_user = $("<input type='text' style='width:100%'/>");
                    this.input_fields.db_user.val(values["database.user"]);
                    td32.append(this.input_fields.db_user);
                    td31.appendTo(tr3);
                    td32.appendTo(tr3);
                    tr3.appendTo(table);

                    //DB password
                    var tr4 = $("<tr/>");
                    var td41 = $("<td/>").css({"font-weight":"bold"}).text("Password: ");
                    var td42 = $("<td/>");
                    this.input_fields.db_pass = $("<input type='password' style='width:100%'/>");
                    this.input_fields.db_pass.val(values["database.password"]);
                    td42.append(this.input_fields.db_pass);
                    td41.appendTo(tr4);
                    td42.appendTo(tr4);
                    tr4.appendTo(table);

                    this.input_fields.db_select.change(function() {
                        getUrlValue();
                    });

                    if (callback) callback();
                };

                var getDatabases = function() {
                    //get prefixes
                    var url = settings.host+"config/list?prefix=database";
                    var that = this;
                    $.getJSON(url,function(data) {
                        var values = [];
                         var databases = new Array();
                         for(var property in data) {
                             if(endsWith(property,"url")) {
                                 property = property.substring(property.indexOf('.')+1);
                                 property = property.substring(0,property.indexOf('.'));
                                 if(property!="") {
                                     if($.inArray(property,databases)==-1) {
                                         databases.push(property);
                                     }
                                 }
                             }
                        }
                        getDBType(databases,new Array());
                    });
                };

                var getDBType = function(databases,values) {
                    var cid = "database.type";
                    var url = settings.host+"config/data/"+cid;
                    $.getJSON(url,function(data) {
                        values[cid] = data[cid];
                        getDBHost(databases,values);
                    });
                };

                var getDBHost = function(databases,values) {
                    var cid = "database.url";
                    var url = settings.host+"config/data/"+cid;
                    $.getJSON(url,function(data) {
                        values[cid] = data[cid];
                        getDBUser(databases,values);
                    });
                };

                var getDBUser = function(databases,values) {
                    var cid = "database.user";
                    var url = settings.host+"config/data/"+cid;
                    $.getJSON(url,function(data) {
                        values[cid] = data[cid];
                        getDBPass(databases,values);
                    });
                };

                var getDBPass = function(databases,values) {
                    var cid = "database.password";
                    var url = settings.host+"config/data/"+cid;
                    $.getJSON(url,function(data) {
                        values[cid] = data[cid];
                        writeValues(databases,values);
                    });

                };
                getDatabases();

                //helper
                function getUrlValue() {
                    var that = this;
                    $.getJSON(settings.host+"config/data/"+"database."+this.input_fields.db_select.val()+".url",function(data){
                        that.input_fields.db_host.val(data["database."+that.input_fields.db_select.val()+".url"]);
                    })
                }
            },

            ping:function() {
                var that = this;
                var dburl = input_fields.db_host.val();
                if (input_fields.db_host.val().indexOf(";") > 0) {
                    dburl = input_fields.db_host.val().substring(0, input_fields.db_host.val().indexOf(";"));
                }
                var url = settings.host + "storage-kiwi/ping?type=" + input_fields.db_select.val() + "&url=" + dburl + "&user=" + input_fields.db_user.val() + "&pwd=" + input_fields.db_pass.val();
                $.ajax({
                    type:"POST",
                    url: url,
                    success: function() {
                        if(confirm("Configuration Test successfully! Save values?")){
                           that.saveValues();
                        }
                    },
                    error: function(jqXHR, statusText, errorThrown) {
                        alert(errorThrown + "(" + jqXHR.status + "):\n"+jqXHR.responseText)
                    }
                });
            },

            saveValues: function(callback) {

                var saveDatabase = function() {
                    $.ajax({
                        type:"POST",
                        contentType: "application/json",
                        url: settings.host + "config/data/database.type",
                        data: '["' + input_fields.db_select.val() + '"]',
                        success: function() {
                            saveUrl();
                        },
                        error: function(jqXHR, statusText, errorThrown) {
                            that.input_fields.l_div.html("");
                            alert("Error: " + errorThrown + "(" + jqXHR.status + ")" + jqXHR.responseText);
                        }
                    });
                }

                function saveUrl() {
                    $.ajax({
                        type:"POST",
                        contentType: "application/json",
                        url: settings.host + "config/data/database.url",
                        data: '["' + input_fields.db_host.val() + '"]',
                        success: function() {
                            saveUser();
                        },
                        error: function(jqXHR, statusText, errorThrown) {
                            that.input_fields.l_div.html("");
                            alert("Error: " + errorThrown + "(" + jqXHR.status + ")" + jqXHR.responseText);
                        }
                    });
                }

                function saveUser() {
                    $.ajax({
                        type:"POST",
                        contentType: "application/json",
                        url: settings.host + "config/data/database.user",
                        data: '["' + input_fields.db_user.val() + '"]',
                        success: function() {
                            savePwd();
                        },
                        error: function(jqXHR, statusText, errorThrown) {
                            that.input_fields.l_div.html("");
                            alert("Error: " + errorThrown + "(" + jqXHR.status + ")" + jqXHR.responseText);
                        }
                    });
                }

                function savePwd() {
                    $.ajax({
                        type:"POST",
                        contentType: "application/json",
                        url: settings.host + "config/data/database.password",
                        data: '["' + input_fields.db_pass.val() + '"]',
                        success: function() {
                            reinit();
                        },
                        error: function(jqXHR, statusText, errorThrown) {
                            that.input_fields.l_div.html("");
                            alert("Error: " + errorThrown + "(" + jqXHR.status + ")" + jqXHR.responseText);
                        }
                    });
                }

                function reinit() {
                    var that = this;
                    input_fields.l_div.html("<span>Reinitialize Database </span><img src='" + settings.loading_img + "'/>");
                    $.ajax({
                        type:"POST",
                        url: settings.host + "system/database/reinit",
                        success: function() {
                            that.input_fields.l_div.html("");
                            alert("Successfully reloaded!")
                        },
                        error: function(jqXHR, statusText, errorThrown) {
                            that.input_fields.l_div.html("");
                            alert("Error: " + errorThrown + "(" + jqXHR.status + ")" + jqXHR.responseText);
                        }
                    });
                }
                saveDatabase();
            }
        }

        /**
         * main method
         */
        return this.each(function() {
            // merge options
            if ( options ) {
                $.extend( settings, options );
            }
            //build skeleton

            //get Values
            functions.getValues();
            var top="";
            if(settings.toplink)top='<a href="#" target="_top" style="position:absolute;right:5px;font-size:12px;top:7px;text-decoration:none;">top</a>';
            //build basic view elements
            var title = $("<h2 style='position:relative;margin-bottom:10px'><a style='text-decoration:none' name='"+settings.anker_name+"'>"+settings.title+"</a><span style='margin-left:10px;display:none;' class='tiny_text,lmf_configurator_loader'><img src='"+settings.loading_img+"' alt='loading...'></span>"+top+"</h2>");
            table = $("<table style='margin:0px auto;background-color:#eeeeee;padding:20px;border:1px solid gray;-webkit-border-radius: 3px;border-radius: 3px;'></table>");
            var buttons = $("<div style='width:100%;text-align:center;padding-top:10px;margin-bottom:30px;'></div>");
            var b1 = $("<button>Reload</button>").click(function(){if(confirm("Discard all changes?"))functions.getValues()});
            var b3 = $("<button style='margin-left:10px;'>Test Configuration</button>").click(function(){functions.ping()});
            buttons.append(b1);
            buttons.append(b3);

            //append elements
            $(this).html("");
            $(this).append(title);
            $(this).append(table);
            $(this).append(buttons);
        });
    };
})( jQuery );

