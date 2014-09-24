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
 * Creates a Marmotta Client object which implements all methods below.
 *
 * Author: Thomas Kurz
 * @param url The basic URL where Marmotta runs
 * @param opts an options object (OPTIONAL)
 */
function MarmottaClient(url,opts) {

    if(url === undefined) {
        //test if url is defined
        throw "url must be defined";
    }
    if(url.lastIndexOf("/") === url.length -1) {
        //clean url
        url = url.substring(0, url.length -1);
    }

    //default options
    var HTTP,
        options = {
            configuration : {
                path : "/config" //path to config webservices
            },
            resource : {
                path : "/resource"
            },
            'import' : {
                path : "/import"
            },
            sparql : {
                path : "/sparql"
            },
            ldpath : {
                path : "/ldpath"
            }
        };
    if ( opts ) {
        $.extend( options, opts );
    }

    //create http stub
    HTTP = new HTTP_Client(url);

    //init client and return
    return new Client(options);

    /*
     **************************************
     * The Client
     **************************************
     */
    function Client(options) {

        var configurationClient = new ConfigurationClient(options.configuration);
        /**
         * A client that supports accessing the configuration webservice of the Linked Media Framework. May be used for
         * retrieving as well as changing properties.
         */
        this.configurationClient = {
            /**
             * Requests for a list of all configuration keys in the system.
             * @param onsuccess Function is executed on success. It takes a parameter array with key strings (e.g. ['key1','key2']).
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            listConfigurationKeys : function(onsuccess,onfailure) {
                configurationClient.listConfigurationKeys(onsuccess,onfailure);
            },
            /**
             * Returns a set of configuration values in the system. The set can be limited by key-prefix.
             * @param prefix A string key-prefix that is used to restrict the result set. Every key in the result set starts with prefix. To list all values in the system prefix must be 'null'.
             * @param onsuccess Function is executed on success. It takes a Configuration object parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            listConfigurations : function(prefix,onsuccess,onfailure) {
                configurationClient.listConfigurations(prefix,onsuccess,onfailure);
            },
            /**
             * Get a configuration property. If property does not exists a ServerError 404 is passed to the onfailure function.
             * @param key The name of the property to set.
             * @param onsuccess Function is executed on success.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            getConfiguration : function(key,onsuccess,onfailure) {
                configurationClient.getConfiguration(key,onsuccess,onfailure);
            },
            /**
             * Sets a configuration property.
             * @param key The name of the property to set. If the property already exists it is overwritten.
             * @param value The value (a javascript Object, Array or Primitive).
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            setConfiguration : function(key,value,onsuccess,onfailure) {
                configurationClient.setConfiguration(key,value,onsuccess,onfailure);
            },
            /**
             * Delete a configuration property. If property does not exists a ServerError 500 is passed to the onfailure function.
             * @param key The name of the property to delete.
             * @param onsuccess Function is executed on success.(OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            deleteConfiguration : function(key,onsuccess,onfailure) {
                configurationClient.deleteConfiguration(key,onsuccess,onfailure);
            }
        };

        var resourceClient = new ResourceClient(options.resource);
        /**
         * This client manages the CRUD operations on resources (content as well as metadata)
         */
        this.resourceClient = {
            /**
             * Create a resource in the remote Marmotta installation
             * @param uri The uri of the new resource. If parameter is null, the system creates a random uri.
             * @param onsuccess Function is executed on success with parameter uri (identifier of new resource).(OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            createResource : function(uri,onsuccess,onfailure) {
                resourceClient.createResource(uri,onsuccess,onfailure);
            },
            /**
             * Test whether the resource with the provided URI exists.
             * Uses an OPTIONS call to the resource web service to determine whether the resource exists or not.
             * @param uri The resource uri.
             * @param onsuccess Function is executed on success with boolean parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            existsResource : function(uri,onsuccess,onfailure) {
                resourceClient.existsResource(uri,onsuccess,onfailure);
            },
            /**
             * Return the resource metadata for the resource with the given URI, if it exists. Returns null if the
             * resource exists but there is no metadata.
             * @param uri The resource uri.
             * @param onsuccess Function is executed on success with result object (rdf/json)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            getResourceMetadata : function(uri,onsuccess,onfailure) {
                resourceClient.getResourceMetadata(uri,onsuccess,onfailure);
            },
            /**
             * Send data object to the server to update metadata.
             * @param uri The resource uri.
             * @param data Metadata object in rdf/json format
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            updateResourceMetadata : function(uri,data,onsuccess,onfailure) {
                resourceClient.updateResourceMetadata(uri,data,onsuccess,onfailure);
            },
            /**
             * Returns the resource content for given mimetype if available.
             * @param uri the resource uri
             * @param mimetype the requested mimetype
             * @param onsuccess Function is executed on success with result data as parameter
             * @param onfailure Function is executed on failure (e.g. if content does not exist). It takes a ServerError object.(OPTIONAL)
             */
            getResourceContent : function(uri,mimetype,onsuccess,onfailure) {
                resourceClient.getResourceContent(uri,mimetype,onsuccess,onfailure);
            },
            /**
             * Update the content of the resource. Content data should/must have given mimetype
             * @param uri The resource uri.
             * @param data The content data with given mimetype
             * @param mimetype The content mimetype
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            updateResourceContent : function(uri,data,mimetype,onsuccess,onfailure) {
                resourceClient.updateResourceContent(uri,data,mimetype,onsuccess,onfailure);
            },
            /**
             * Delete resource (content AND metadata)
             * @param uri The resource uri
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            deleteResource : function(uri,onsuccess,onfailure) {
                resourceClient.deleteResource(uri,onsuccess,onfailure);
            }
        };

        var importClient = new ImportClient(options['import']);
        /**
         * TODO: TEST
         * This client offers import functionalities
         */
        this.importClient = {
            /**
             * gets a list of supported mimetypes
             * @param onsuccess Function is executed on success. It takes an array of strings as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            getSupportedTypes : function(onsuccess,onfailure) {
                importClient.getSupportedTypes(onsuccess,onfailure);
            },
            /**
             *
             * @param url a url that identifies the datasource
             * @param mimetype the mimetype of the datasource
             * @param context the context to be used for import; if null, default content is used
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            uploadFromUrl : function(url,mimetype,context,onsuccess,onfailure) {
                importClient.uploadFromUrl(url,mimetype,context,onsuccess,onfailure);
            },
            /**
             *
             * @param data stringified data to be imported
             * @param mimetype the mimetype of the data
             * @param context the context to be used for import; if null, default content is used
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            upload : function(data,mimetype,context,onsuccess,onfailure) {
                importClient.upload(data,mimetype,context,onsuccess,onfailure);
            }
        };

        var sparqlClient = new SparqlClient(options.sparql);
        /**
         *  A client for SPARQL queries
         */
        this.sparqlClient = {
            /**
             * issue SPARQL select
             * @param query A SPARQL select query
             * @param onsuccess Function is executed on success with SPARQL/JSON result data as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            select : function(query,onsuccess,onfailure) {
                sparqlClient.select(query,onsuccess,onfailure);
            },
            /**
             * issues SPARQL ask
             * @param query A SPARQL ask query
             * @param onsuccess Function is executed on success with boolean value as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            ask : function(query,onsuccess,onfailure) {
                sparqlClient.ask(query,onsuccess,onfailure);
            },
            /**
             * issues SPARQL update
             * @param query A SPARQL update query
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            update : function(query,onsuccess,onfailure) {
                sparqlClient.update(query,onsuccess,onfailure);
            }
        };

        var ldPathClient = new LDPathClient(options.ldpath);
        /**
         * A client for ld path evaluation
         */
        this.ldPathClient = {
            /**
             * Evaluate a single LDPath
             * @param uri the starting point
             * @param path the path
             * @param onsuccess Function is executed on success with array of ldpath result objects as parameter (e.g. [{value:"x",type:"literal",datatype:"http://www.w3.org/2001/XMLSchema#string",lang:"en"}]).
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            evaluatePath : function(uri, path, onsuccess, onfailure) {
                ldPathClient.evaluatePath(uri, path, onsuccess, onfailure);
            },
            /**
             * Evaluate a LDPath program
             * @param uri the starting point
             * @param program the ldpath program
             * @param onsuccess Function is executed on success with ldpath result object (Map) as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            evaluateProgram : function(uri, program, onsuccess, onfailure) {
                ldPathClient.evaluateProgram(uri, program, onsuccess, onfailure);
            }
        };
    }


    function LDPathClient(options) {
        this.evaluatePath = function(uri,path,onsuccess,onfailure) {
            HTTP.get(options.path+"/path",{uri:encodeURIComponent(uri),path:encodeURIComponent(path)},null,null,{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("path evaluated successfully");},
                400:function(){if(onfailure)onfailure(new ServerError("the server did not accept the uri or path arguments",400));else throw new Error("the server did not accept the uri or path arguments")},
                404:function(){if(onfailure)onfailure(new ServerError("the resource with URI does not exist on the server",404));else throw new Error("the resource with URI does not exist on the server")},
                "default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
            })
        };
        this.evaluateProgram = function(uri,program,onsuccess,onfailure) {
            HTTP.get(options.path+"/program",{uri:encodeURIComponent(uri),program:encodeURIComponent(program)},null,null,{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("path evaluated successfully");},
                400:function(){if(onfailure)onfailure(new ServerError("the server did not accept the uri or program arguments",400));else throw new Error("the server did not accept the uri or program arguments")},
                404:function(){if(onfailure)onfailure(new ServerError("the resource with URI does not exist on the server",404));else throw new Error("the resource with URI does not exist on the server")},
                "default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
            })
        };
    }


    /**
     * Internal Spaqrl Client implementation
     * @param options
     */
    function SparqlClient(options) {
        this.select = function(query,onsuccess,onfailure) {
            HTTP.get(options.path+"/select",{query:encodeURIComponent(query)},null,"application/sparql-results+json",{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data).results.bindings);console.debug("query returned successful");},
                "default":function(err,response){if(onfailure)onfailure(new ServerError(err,response.status));else throw new Error(err)}
            });
        };
        this.ask = function(query,onsuccess,onfailure) {
            HTTP.get(options.path+"/select",{query:encodeURIComponent(query)},null,"application/sparql-results+json",{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data).boolean);console.debug("ask-query returned successful");},
                "default":function(err,response){if(onfailure)onfailure(new ServerError(err,response.status));else throw new Error(err)}
            });
        };
        this.update = function(query,onsuccess,onfailure) {
            HTTP.get(options.path+"/update",{query:encodeURIComponent(query)},null,null,{
                200:function(){if(onsuccess)onsuccess();console.debug("sparql update was successful");},
                "default":function(err,response){if(onfailure)onfailure(new ServerError(err,response.status));else throw new Error(err)}
            });
        };
    }

    /**
     * Interal Import Client implementation
     * @param options
     */
    function ImportClient(options) {
        this.getSupportedTypes = function(onsuccess,onfailure) {
            HTTP.get(options.path+"/types",null,null,null,{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("loaded types successfully");},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        };
        this.uploadFromUrl = function(url,mimetype,context,onsuccess,onfailure) {
            var params = context ? {url:url,context:context} : {url:url};
            HTTP.post(options.path+"/external",params,null,mimetype,{
                200:function(){if(onsuccess)onsuccess();console.debug("import successful");},
                412:function(){if(onfailure)onfailure(new ServerError("mime type "+mimetype+" not acceptable by import service",412));else throw new Error("mime type "+mimetype+" not acceptable by import service")},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        };
        this.upload = function(data,mimetype,context,onsuccess,onfailure) {
            var params = context ? {context:context} : null;
            HTTP.post(options.path+"/upload",params,data,mimetype,{
                200:function(){if(onsuccess)onsuccess();console.debug("import successful");},
                412:function(){if(onfailure)onfailure(new ServerError("mime type "+mimetype+" not acceptable by import service",412));else throw new Error("mime type "+mimetype+" not acceptable by import service")},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        };
    }

    /**
     * Internal Resource Client implementation
     * @param options
     */
    function ResourceClient(options) {
        this.createResource = function(uri,onsuccess,onfailure) {
            var queryParam = uri ? {uri:encodeURIComponent(uri)} : null;
            HTTP.post(options.path,queryParam,null,null,{
                200:function(location){if(onsuccess)onsuccess(location);console.debug("resource "+location+" already existed, not creating new")},
                201:function(location){if(onsuccess)onsuccess(location);console.debug("resource "+location+" created")},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        };
        this.existsResource = function(uri,onsuccess,onfailure) {
            //TODO implement
            alert('Resource existence test is not implemented yet!');
        };
        this.getResourceMetadata = function(uri,onsuccess,onfailure) {
            HTTP.get(options.path,{uri:encodeURIComponent(uri)},null,"application/json; rel=meta",{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("metadata for resource "+uri+" retrieved");},
                406:function(){if(onfailure)onfailure(new ServerError("server does not support metadata type application/json",406));else throw new Error("server does not support metadata type application/json")},
                404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist",404));else throw new Error("resource "+uri+" does not exist")},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            })
        };
        this.updateResourceMetadata = function(uri,data,onsuccess,onfailure) {
            HTTP.put(options.path,{uri:encodeURIComponent(uri)},JSON.stringify(data),"application/json; rel=meta",{
                200:function(){if(onsuccess)onsuccess();console.debug("metadata for resource "+uri+" updated")},
                415:function(){if(onfailure)onfailure(new ServerError("server does not support metadata type application/json",415));else throw new Error("server does not support metadata type application/json")},
                404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist, cannot update",404));else throw new Error("resource "+uri+" does not exist, cannot update")},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        };
        this.getResourceContent = function(uri,mimetype,onsuccess,onfailure) {
            HTTP.get(options.path,{uri:encodeURIComponent(uri)},null,mimetype+"; rel=content",{
                200:function(data){if(onsuccess)onsuccess(data);console.debug("content for resource "+uri+" retrieved");},
                406:function(){if(onfailure)onfailure(new ServerError("server does not support content type "+mimetype,406));else throw new Error("server does not support content type "+mimetype)},
                404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist",404));else throw new Error("resource "+uri+" does not exist")},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        };
        this.updateResourceContent = function(uri,data,mimetype,onsuccess,onfailure) {
            HTTP.put(options.path,{uri:encodeURIComponent(uri)},data,mimetype+"; rel=content",{
                200:function(){if(onsuccess)onsuccess();console.debug("content for resource "+uri+" updated")},
                415:function(){if(onfailure)onfailure(new ServerError("server does not support content type "+mimetype,415));else throw new Error("server does not support content type "+mimetype)},
                404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist, cannot update",404));else throw new Error("resource "+uri+" does not exist, cannot update")},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        };
        this.deleteResource = function(uri,onsuccess,onfailure) {
            HTTP.delete(options.path,{uri:encodeURIComponent(uri)},null,null,{
                200:function(){if(onsuccess)onsuccess();console.debug("resource "+uri+" deleted")},
                400:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" invalid, cannot delete",400));else throw new Error("resource "+uri+" invalid, cannot delete")},
                404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist, cannot delete",404));else throw new Error("resource "+uri+" does not exist, cannot delete")},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        };
    }

    /**
     * Internal Configuration Client implementation
     * @param options
     */
    function ConfigurationClient(options) {

        this.listConfigurationKeys = function(onsuccess,onfailure) {
            HTTP.get(options.path+"/list",null,null,null,
                {200:function(data){
                    data = JSON.parse(data);
                    var keys = [];
                    for(var prop in data) { //filter keys
                        keys.push(prop)
                    }
                    if(onsuccess)onsuccess(keys);console.debug("listed configuration keys successfully")
                },
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                }
            );
        };
        this.listConfigurations = function(prefix,onsuccess,onfailure) {
            var queryParams = prefix==null ? null : {prefix:prefix};
            HTTP.get(options.path+"/list",queryParams,null,null,
                {200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("listed configurations"+(prefix==null?"":" width prefix "+prefix)+" successfully")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                }
            );
        };
        this.getConfiguration = function(key,onsuccess,onfailure) {
            HTTP.get(options.path+"/data/"+encodeURIComponent(key),null,null,null,
                {200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("get configuration '"+key+"' successfully")},
                    404:function(){if(onfailure)onfailure(new ServerError("configuration "+key+" is not set",404));else throw new Error("configuration "+key+" is not set")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                }
            );
        };
        this.setConfiguration = function(key,value,onsuccess,onfailure) {
            HTTP.post(options.path+"/data/"+encodeURIComponent(key),null,JSON.stringify(value),null,
                {200:function(){if(onsuccess)onsuccess();console.debug("set '"+key+"' to '"+value+"' successfully")},
                    400:function(){if(onfailure)onfailure(new ServerError("cannot parse input into json",400));else throw new Error("cannot parse input into json")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                }
            );
        };
        this.deleteConfiguration = function(key,onsuccess,onfailure) {
            HTTP.delete(options.path+"/data/"+encodeURIComponent(key),null,null,null,
                {200:function(){if(onsuccess)onsuccess();console.debug("deleted configuration '"+key+"' successfully")},
                    404:function(){if(onfailure)onfailure(new ServerError("configuration "+key+" does not exist",404));else throw new Error("configuration "+key+" is not set")},
                    500:function(){if(onfailure)onfailure(new ServerError("cannot delete configuration",500));else throw new Error("cannot delete configuration")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                }
            );
        };
    }

    /**
     * A ServerError Object
     * @param message
     * @param status
     */
    function ServerError(message,status) {
        function getStatus(){
            switch(status) {
                case 203: return "Non-Authoritative Information";
                case 204: return "No Content";
                case 400: return "Bad Request";
                case 401: return "Unauthorized";
                case 403: return "Forbidden";
                case 404: return "Not Found";
                case 406: return "Not Acceptable";
                case 412: return "Invalid Content-Type";
                case 415: return "Unsupported Media Type";
                case 500: return "Internal Server Error";
                default: return "Unknown or not implmented";
            }
        }
        this.status = status;
        this.message = message;
        this.name = getStatus();
    }

    /**
     * HTTP Client based on XMLHTTPRequest Object, allows RESTful interaction (GET;PUT;POST;DELETE)
     * @param url
     */
    function HTTP_Client(url) {

        function createRequest() {
            var request = null;
            if (window.XMLHttpRequest) {
                request = new XMLHttpRequest();
            } else if (window.ActiveXObject) {
                request = new ActiveXObject("Microsoft.XMLHTTP");
            } else {
                throw "request object can not be created"
            }
            return request;
        }

        //build a query param string
        function buildQueryParms(params) {
            if(params==null||params.length==0) return "";
            var s="?";
            for(var prop in params) {
                s+=prop+"="+params[prop]+"&";
            } return s.substring(0,s.length-1);
        }

        //fire request, the method takes a callback object which can contain several callback functions for different HTTP Response codes
        function doRequest(method,path,queryParams,data,mimetype,callbacks) {
            mimetype = mimetype ||  "application/json";
            var _url = url+path+buildQueryParms(queryParams);
            var request = createRequest();
            request.onreadystatechange = function() {
                if (request.readyState==4) {
                    if(callbacks.hasOwnProperty(request.status)) {
                        callbacks[request.status](request.responseText,request);
                    } else if (callbacks.hasOwnProperty("default")) {
                        callbacks["default"](request.responseText,request);
                    } else {
                        throw "Status:"+request.status+",Text:"+request.responseText;
                    }
                }
            };
            request.open(method, _url, true);
            if(method=="PUT"||method=="POST")request.setRequestHeader("Content-Type",mimetype);
            if(method=="GET")request.setRequestHeader("Accept",mimetype);
            request.send( data );
        }

        this.get = function(path,queryParams,data,mimetype,callbacks) {
            doRequest("GET",path,queryParams,data,mimetype,callbacks);
        };

        this.put = function(path,queryParams,data,mimetype,callbacks) {
            doRequest("PUT",path,queryParams,data,mimetype,callbacks);
        };

        this.post = function(path,queryParams,data,mimetype,callbacks) {
            doRequest("POST",path,queryParams,data,mimetype,callbacks);
        };

        this.delete = function(path,queryParams,data,mimetype,callbacks) {
            doRequest("DELETE",path,queryParams,data,mimetype,callbacks);
        };
    }
}

