/**
 * Creates a LMF Client object which implements all methods below.
 *
 * Author: Thomas Kurz
 * @param url The basic URL of the Linked Media Framework.
 * @param opts an options object (OPTIONAL)
 */
function LMFClient(url,opts) {

    if( url==undefined) throw "url must be defined"; //test if url is defined
    if( url.lastIndexOf("/")==url.length-1) url=url.substring(0,url.length-1); //clean url

    //default options
    var options = {
        configuration : {
            path : "/config" //path to config webservices
        },
        resource : {
            path : "/resource"
        },
        import : {
            path : "/import"
        },
        sparql : {
            path : "/sparql"
        },
        cores : {
            path : "/solr/cores"
        },
        search : {
            path : "/solr"
        },
        ldpath : {
            path : "/ldpath"
        },
        classifier : {
        	path : "/classifier"	
        }
    }
    if ( opts ) {
        $.extend( options, opts );
    }

    //create http stub
    var HTTP = new HTTP_Client(url);

    //init client and return
    var client = new Client(options);
    return client;

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
             * 	Requests for a list of all configuration keys in the system.
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
             * 	Sets a configuration property.
             * @param key The name of the property to set. If the property already exists it is overwritten.
             * @param value The value (a javascript Object, Array or Primitive).
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            setConfiguration : function(key,value,onsuccess,onfailure) {
                configurationClient.setConfiguration(key,value,onsuccess,onfailure);
            },
            /**
             * 	Delete a configuration property. If property does not exists a ServerError 500 is passed to the onfailure function.
             * @param key The name of the property to delete.
             * @param onsuccess Function is executed on success.(OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            deleteConfiguration : function(key,onsuccess,onfailure) {
                configurationClient.deleteConfiguration(key,onsuccess,onfailure);
            }
        }

        var resourceClient = new ResourceClient(options.resource);
        /**
         * This client manages the CRUD operations on resources (content as well as metadata)
         */
        this.resourceClient = {
            /**
             * Create a resource in the remote LMF installation
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
        }

        var importClient = new ImportClient(options.import);
        /**
         * TODO TEST
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
        }

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
        }

        var coresClient = new CoresClient(options.cores);
        /**
         *  A client for core management
         */
        this.coresClient = {
            /**
             * List all cores available
             * @param onsuccess Function is executed on success with an array list of corenames as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            listCores : function(onsuccess,onfailure) {
                coresClient.listCores(onsuccess,onfailure);
            },
            /**
             *
             * @param name The name of the requested core
             * @param onsuccess Function is executed on success with core program (string) as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            getCoreConfiguration : function(name,onsuccess,onfailure) {
                coresClient.getCoreConfiguration(name,onsuccess,onfailure);
            },
            /**
             * Creates a new solr core. If core already exist, an Error is thrown (or an ServerError is retrieved).
             * @param name The name of the core to create
             * @param program The program string
             * @param onsuccess Function is executed on success.(OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            createCoreConfiguration : function(name,program,onsuccess,onfailure) {
                coresClient.createCoreConfiguration(name,program,onsuccess,onfailure)
            },
            /**
             * Updates an existing core.
             * @param name The name of the core to create
             * @param program The program string
             * @param onsuccess Function is executed on success.(OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            updateCoreConfiguration : function(name,program,onsuccess,onfailure) {
                coresClient.updateCoreConfiguration(name,program,onsuccess,onfailure)
            },
            /**
             * Deletes an existing core
             * @param name The core name to delete
             * @param onsuccess Function is executed on success.(OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            deleteCore : function(name,onsuccess,onfailure) {
                coresClient.deleteCore(name,onsuccess,onfailure);
            }
        }


        var searchClient = new SearchClient(options.search);
        /**
         * A client for solr search (just basics, it's better to use specialized solr client libraries)
         */
        this.searchClient = {
            /**
             * Execute a solr query on a given core
             * @param corename The name of the core
             * @param query A query object (e.g. {q:'*:*'})
             * @param onsuccess Function is executed on success with solr result object as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            search : function(corename, query, onsuccess, onfailure) {
                searchClient.search(corename, query, onsuccess, onfailure);
            },
            /**
             * Queries for recommended resources for a basic resource
             * @param corename The name of the core
             * @param uri The basic resource
             * @param onsuccess Function is executed on success with solr result object as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            recommendation : function(corename, uri, onsuccess, onfailure) {
                searchClient.recommendation(corename, uri, onsuccess, onfailure);
            }
        }

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
             * @param path the path
             * @param onsuccess Function is executed on success with ldpath result object (Map) as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            evaluateProgram : function(uri, program, onsuccess, onfailure) {
                ldPathClient.evaluateProgram(uri, program, onsuccess, onfailure);
            }
        }

        var classificationClient = new ClassificationClient(options.classifier);

        this.classificationClient = {
            /**
             * Create a new classifier with the given name. The service will take care of creating the appropriate
             * configuration entries and work files in the LMF work directory.
             * @param name a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            createClassifier : function(name, onsuccess, onfailure) {
                classificationClient.createClassifier(name, onsuccess, onfailure);
            },
            /**
             * Remove the classifier with the given name from the system configuration.
             *
             * @param name a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
             * @param removeData also remove all training and model data of this classifier from the file system
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            removeClassifier : function(name, removeData, onsuccess, onfailure) {
                classificationClient.removeClassifier(name, removeData, onsuccess, onfailure);
            },
            /**
             * List all classifiers registered in the classification service.
             * @param onsuccess Function is executed on success with list of classifiers as parameter.
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            listClassifiers : function(onsuccess, onfailure) {
                classificationClient.listClassifiers(onsuccess, onfailure);
            },
            /**
             * Add training data to the classifier identified by the given name and for the concept passed as argument. Note
             * that training data is not immediately taken into account by the classifier. Retraining of the classifier will
             * take place when a certain threshold of training datasets has been added or when a certain (configurable) time has
             * passed.
             * @param name a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
             * @param uri the URI of the concept which to train with the sample text
             * @param data the sample text for the concept
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            trainClassifier : function(name, uri, data, onsuccess, onfailure) {
                classificationClient.trainClassifier(name, uri, data, onsuccess, onfailure);
            },
            /**
             * Retrain the classifier with the given name immediately. Will read in the training data and create a new
             * classification model.
             * @param name
             * @param onsuccess Function is executed on success. (OPTIONAL)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            retrainClassifier : function(name, onsuccess, onfailure) {
                classificationClient.retrainClassifier(name, onsuccess, onfailure);
            },
            /**
             * Get classifications from the given classifier for the given text. The classifications will be ordered by
             * descending probability, so that classifications with higher probability will be first. A classification object
             * consists of a KiWiUriResource identifying the classified concept and a probability indicating how likely it is
             * that the text matches the given concept.
             *
             * @param name a string identifying the classifier; should only consist of alphanumeric characters (no white spaces)
             * @param data the text to classify
             * @param threshold the minimum probability of a classification to be considered in the result (may be null).
             * @param onsuccess Function is executed on success with a list of classifications ordered by descending probability as parameter (all having higher probability than threshold)
             * @param onfailure Function is executed on failure. It takes a ServerError object.(OPTIONAL)
             */
            listClassifications : function(name, data, threshold, onsuccess, onfailure) {
                classificationClient.listClassifications(name, data, threshold, onsuccess, onfailure);
            }
        }
    }
    
    function ClassificationClient(options) {
    	function checkClassificationName(name) {
    		return !/[^A-Za-z0-9]/.test(name);
    	}
    	this.createClassifier = function(name,onsuccess,onfailure) {
    		if(!checkClassificationName(name)) throw new Error("name for classifier is not valid");
    		HTTP.post(options.path+"/"+name,null,null,null,{
    			200:function(){if(onsuccess)onsuccess();console.debug("created classifier "+name)},
    			403:function(){if(onfailure)onfailure(new ServerError("classifier "+name+" already exists",403));else throw new Error("classifier "+name+" already exists")},
    			"default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
    		});	
    	}
    	this.removeClassifier = function(name,removeData,onsuccess,onfailure) {
    		if(!checkClassificationName(name)) throw new Error("name for classifier is not valid");
    		var query=removeData?{removeData:true}:null;
    		HTTP.delete(options.path+"/"+name,query,null,null,{
    			200:function(){if(onsuccess)onsuccess();console.debug("deleted classifier "+name)},
    			404:function(){if(onfailure)onfailure(new ServerError("classifier "+name+" does not exist",404));else throw new Error("classifier "+name+" does not exist")},
    			"default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
    		});		
    	}
    	this.listClassifiers = function(onsuccess,onfailure) {
    		HTTP.get(options.path+"/list",null,null,null,{
    			200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("classifiers listed successfully");},
    			"default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
    		});	
    	}	
    	this.trainClassifier = function(name,uri,data,onsuccess,onfailure) {
    		var query = {concept:encodeURIComponent(uri)};
    		HTTP.post(options.path+"/"+name+"/train",query,data,"text/plain",{
    			200:function(){if(onsuccess)onsuccess();console.debug("classifier "+name+" updated successfully");},
    			"default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
    		});
    	}
    	this.retrainClassifier = function(name,onsuccess,onfailure) {
    		HTTP.post(options.path+"/"+name+"/retrain",null,null,null,{
    			200:function(){if(onsuccess)onsuccess();console.debug("classifier "+name+" retrained successfully");},
    			"default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
    		});
    	}
    	this.listClassifications = function(name,data,threshold,onsuccess,onfailure) {
            var query = threshold?{threshold:encodeURIComponent(threshold)}:null;
    		HTTP.post(options.path+"/"+name+"/classify",query,data,"text/plain",{
    			200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("classification executed successfully");},
    			"default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
    		});
    	}
    }

    function LDPathClient(options) {
        this.evaluatePath = function(uri,path,onsuccess,onfailure) {
            HTTP.get(options.path+"/path",{uri:encodeURIComponent(uri),path:encodeURIComponent(path)},null,null,{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("path evaluated successfully");},
                400:function(){if(onfailure)onfailure(new ServerError("the server did not accept the uri or path arguments",400));else throw new Error("the server did not accept the uri or path arguments")},
                404:function(){if(onfailure)onfailure(new ServerError("the resource with URI does not exist on the server",404));else throw new Error("the resource with URI does not exist on the server")},
                "default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
            })
        }
        this.evaluateProgram = function(uri,program,onsuccess,onfailure) {
            HTTP.get(options.path+"/program",{uri:encodeURIComponent(uri),program:encodeURIComponent(program)},null,null,{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("path evaluated successfully");},
                400:function(){if(onfailure)onfailure(new ServerError("the server did not accept the uri or program arguments",400));else throw new Error("the server did not accept the uri or program arguments")},
                404:function(){if(onfailure)onfailure(new ServerError("the resource with URI does not exist on the server",404));else throw new Error("the resource with URI does not exist on the server")},
                "default":function(err,request){if(onfailure)onfailure(new ServerError("unexpected error",request.status));else throw new Error("unexpected error")}
            })
        }
    }

    /**
     * Internal sorl search implementation (it is better to use solr client library like ajax/solr)
     * @param options
     */
    function SearchClient(options) {
        this.search = function(corename,query,onsuccess,onfailure) {
            query.wt="json";
            HTTP.get(options.path+"/"+corename+"/select",query,null,null,{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("query successful");},
                "default":function(err,request){if(onfailure)onfailure(new ServerError(err,request.status));else throw new Error(err)}
            });
        }
        this.recommendation = function(corename,uri,onsuccess,onfailure) {
            //TODO not implemented yet
            alert("Recommendation function is not yet implemented");
        }
    }

    /**
     * Internal Cores client implementation
     * @param options
     */
    function CoresClient(options) {
        this.listCores = function(onsuccess,onfailure) {
            HTTP.get(options.path,null,null,null,{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("listed cores");},
                "default":function(){if(onfailure)onfailure(new ServerError("Cannot list cores"));else throw new Error("Cannot list cores")}
            });
        }
        this.getCoreConfiguration = function(name,onsuccess,onfailure) {
            HTTP.get(options.path+"/"+encodeURIComponent(name),null,null,"text/plain",{
                200:function(data){if(onsuccess)onsuccess(data);console.debug("return core "+name);},
                404:function(){if(onfailure)onfailure(new ServerError("Core '"+name+"' does not exist",404));else throw new Error("Core '"+name+"' does not exist")},
                "default":function(){if(onfailure)onfailure(new ServerError("Cannot return core '"+name+"'"));else throw new Error("Cannot return core '"+name+"'")}
            });
        }
        this.createCoreConfiguration = function(name,program,onsuccess,onfailure) {
            HTTP.post(options.path+"/"+encodeURIComponent(name),null,program,"text/plain",{
                200:function(){if(onsuccess)onsuccess("created '"+name+"' successful");console.debug("created '"+name+"' successful")},
                403:function(err){if(onfailure)onfailure(new ServerError(err,403));else throw new Error(err)},
                400:function(err){if(onfailure)onfailure(new ServerError(err,400));else throw new Error(err)},
                500:function(err){if(onfailure)onfailure(new ServerError(err,500));else throw new Error(err)},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        }
        this.updateCoreConfiguration = function(name,program,onsuccess,onfailure) {
            HTTP.put(options.path+"/"+encodeURIComponent(name),null,program,"text/plain",{
                200:function(msg){if(onsuccess)onsuccess("updated '"+name+"' successful");console.debug("updated '"+name+"' successful")},
                400:function(err){if(onfailure)onfailure(new ServerError(err,400));else throw new Error(err)},
                404:function(err){if(onfailure)onfailure(new ServerError(err,404));else throw new Error(err)},
                500:function(err){if(onfailure)onfailure(new ServerError(err,500));else throw new Error(err)},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        }
        this.deleteCore = function(name,onsuccess,onfailure) {
            HTTP.delete(options.path+"/"+encodeURIComponent(name),null,null,null,{
                200:function(msg){if(onsuccess)onsuccess(msg);console.debug(msg)},
                404:function(err){if(onfailure)onfailure(new ServerError(err,404));else throw new Error(err)},
                "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
            });
        }
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
        }
        this.ask = function(query,onsuccess,onfailure) {
            HTTP.get(options.path+"/select",{query:encodeURIComponent(query)},null,"application/sparql-results+json",{
                200:function(data){if(onsuccess)onsuccess(JSON.parse(data).boolean);console.debug("ask-query returned successful");},
                "default":function(err,response){if(onfailure)onfailure(new ServerError(err,response.status));else throw new Error(err)}
            });
        }
        this.update = function(query,onsuccess,onfailure) {
            HTTP.get(options.path+"/update",{query:encodeURIComponent(query)},null,null,{
                200:function(){if(onsuccess)onsuccess();console.debug("sparql update was successful");},
                "default":function(err,response){if(onfailure)onfailure(new ServerError(err,response.status));else throw new Error(err)}
            });
        }
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
        }
        this.uploadFromUrl = function(url,mimetype,context,onsuccess,onfailure) {
            var params = context ? {url:encodeURIComponent(url),context:context} : {url:encodeURIComponent(url)};
             HTTP.post(options.path+"/external",params,null,mimetype,{
                 200:function(){if(onsuccess)onsuccess();console.debug("import successful");},
                 412:function(){if(onfailure)onfailure(new ServerError("mime type "+mimetype+" not acceptable by import service",412));else throw new Error("mime type "+mimetype+" not acceptable by import service")},
                 "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
             });
        }
        this.upload = function(data,mimetype,context,onsuccess,onfailure) {
            var params = context ? {context:context} : null;
            HTTP.post(options.path+"/upload",params,data,mimetype,{
                 200:function(){if(onsuccess)onsuccess();console.debug("import successful");},
                 412:function(){if(onfailure)onfailure(new ServerError("mime type "+mimetype+" not acceptable by import service",412));else throw new Error("mime type "+mimetype+" not acceptable by import service")},
                 "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
             });
        }
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
            }
            this.existsResource = function(uri,onsuccess,onfailure) {
                //TODO implement
                alert('Resource existence test is not implemented yet!');
            }
            this.getResourceMetadata = function(uri,onsuccess,onfailure) {
                HTTP.get(options.path,{uri:encodeURIComponent(uri)},null,"application/json; rel=meta",{
                    200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("metadata for resource "+uri+" retrieved");},
                    406:function(){if(onfailure)onfailure(new ServerError("server does not support metadata type application/json",406));else throw new Error("server does not support metadata type application/json")},
                    404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist",404));else throw new Error("resource "+uri+" does not exist")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                })
            }
            this.updateResourceMetadata = function(uri,data,onsuccess,onfailure) {
                 HTTP.put(options.path,{uri:encodeURIComponent(uri)},JSON.stringify(data),"application/json; rel=meta",{
                     200:function(){if(onsuccess)onsuccess();console.debug("metadata for resource "+uri+" updated")},
                     415:function(){if(onfailure)onfailure(new ServerError("server does not support metadata type application/json",415));else throw new Error("server does not support metadata type application/json")},
                     404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist, cannot update",404));else throw new Error("resource "+uri+" does not exist, cannot update")},
                     "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                 });
            }
            this.getResourceContent = function(uri,mimetype,onsuccess,onfailure) {
                HTTP.get(options.path,{uri:encodeURIComponent(uri)},null,mimetype+"; rel=content",{
                    200:function(data){if(onsuccess)onsuccess(data);console.debug("content for resource "+uri+" retrieved");},
                    406:function(){if(onfailure)onfailure(new ServerError("server does not support content type "+mimetype,406));else throw new Error("server does not support content type "+mimetype)},
                    404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist",404));else throw new Error("resource "+uri+" does not exist")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                });
            }
            this.updateResourceContent = function(uri,data,mimetype,onsuccess,onfailure) {
                 HTTP.put(options.path,{uri:encodeURIComponent(uri)},data,mimetype+"; rel=content",{
                     200:function(){if(onsuccess)onsuccess();console.debug("content for resource "+uri+" updated")},
                     415:function(){if(onfailure)onfailure(new ServerError("server does not support content type "+mimetype,415));else throw new Error("server does not support content type "+mimetype)},
                     404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist, cannot update",404));else throw new Error("resource "+uri+" does not exist, cannot update")},
                     "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                 });
            }
            this.deleteResource = function(uri,onsuccess,onfailure) {
                 HTTP.delete(options.path,{uri:encodeURIComponent(uri)},null,null,{
                     200:function(){if(onsuccess)onsuccess();console.debug("resource "+uri+" deleted")},
                     400:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" invalid, cannot delete",400));else throw new Error("resource "+uri+" invalid, cannot delete")},
                     404:function(){if(onfailure)onfailure(new ServerError("resource "+uri+" does not exist, cannot delete",404));else throw new Error("resource "+uri+" does not exist, cannot delete")},
                     "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                 });
            }
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
                            for(prop in data) { //filter keys
                                keys.push(prop)
                            }
                            if(onsuccess)onsuccess(keys);console.debug("listed configuration keys successfully")
                        },
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                    }
            );
        }
        this.listConfigurations = function(prefix,onsuccess,onfailure) {
            var queryParams = prefix==null ? null : {prefix:prefix};
            HTTP.get(options.path+"/list",queryParams,null,null,
                    {200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("listed configurations"+(prefix==null?"":" width prefix "+prefix)+" successfully")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                    }
            );
        }
        this.getConfiguration = function(key,onsuccess,onfailure) {
            HTTP.get(options.path+"/data/"+encodeURIComponent(key),null,null,null,
                    {200:function(data){if(onsuccess)onsuccess(JSON.parse(data));console.debug("get configuration '"+key+"' successfully")},
                     404:function(){if(onfailure)onfailure(new ServerError("configuration "+key+" is not set",404));else throw new Error("configuration "+key+" is not set")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                    }
            );
        }
        this.setConfiguration = function(key,value,onsuccess,onfailure) {
            HTTP.post(options.path+"/data/"+encodeURIComponent(key),null,JSON.stringify(value),null,
                    {200:function(){if(onsuccess)onsuccess();console.debug("set '"+key+"' to '"+value+"' successfully")},
                     400:function(){if(onfailure)onfailure(new ServerError("cannot parse input into json",400));else throw new Error("cannot parse input into json")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                    }
            );
        }
        this.deleteConfiguration = function(key,onsuccess,onfailure) {
            HTTP.delete(options.path+"/data/"+encodeURIComponent(key),null,null,null,
                    {200:function(){if(onsuccess)onsuccess();console.debug("deleted configuration '"+key+"' successfully")},
                     404:function(){if(onfailure)onfailure(new ServerError("configuration "+key+" does not exist",404));else throw new Error("configuration "+key+" is not set")},
                     500:function(){if(onfailure)onfailure(new ServerError("cannot delete configuration",500));else throw new Error("cannot delete configuration")},
                    "default":function(){if(onfailure)onfailure(new ServerError("unknown error"));else throw new Error("unknown error")}
                    }
            );
        }
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
            var s="?"
            for(prop in params) {
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
        }

        this.put = function(path,queryParams,data,mimetype,callbacks) {
             doRequest("PUT",path,queryParams,data,mimetype,callbacks);
        }

        this.post = function(path,queryParams,data,mimetype,callbacks) {
             doRequest("POST",path,queryParams,data,mimetype,callbacks);
        }

        this.delete = function(path,queryParams,data,mimetype,callbacks) {
             doRequest("DELETE",path,queryParams,data,mimetype,callbacks);
        }
    }
}