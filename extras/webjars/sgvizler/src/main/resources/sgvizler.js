/*  Sgvizler JavaScript SPARQL result set visualizer, version 0.5.1
 *  (c) 2011--2012 Martin G. Skjæveland
 *
 *  Sgvizler is freely distributable under the terms of an MIT-style license.
 *  Sgvizler web site: https://code.google.com/p/sgvizler/
 *--------------------------------------------------------------------------*/
(function (global) {
    "use strict";

    /*global google, $, jQuery */
    /*jslint browser: true */

    var sgvizler = {

        go: function (callback) {
            google.load('visualization',
                        '1.0',
                        {'packages':
                         ['annotatedtimeline',
                          'corechart',
                          'gauge',
                          'geomap',
                          'geochart',
                          'imagesparkline',
                          'map',
                          'orgchart',
                          'table',
                          'motionchart',
                          'treemap'
                         ]
                        }
                       );

            google.setOnLoadCallback(function () {
                sgvizler.charts.loadCharts();
                sgvizler.drawFormQuery();
                sgvizler.drawContainerQueries();
                callback();
            });
        },

        drawFormQuery: function () {
            var query = new sgvizler.query(sgvizler.ui.id.chartCon),
                params = sgvizler.ui.getUrlParams();
            $.extend(query,
                     sgvizler.option.query,
                     { query: params.query, chart: params.chart });

            if (sgvizler.ui.isElement(query.container) && query.query) {
                $.extend(query.chartOptions,
                         { width: params.width, height: params.height });
                query.draw();
            }
            sgvizler.ui.displayUI(query);
        },

        drawContainerQueries: function () {
            $('[' + this.ui.attr.prefix + 'query]').each(function () {
                var query = new sgvizler.query();
                $.extend(query,
                         sgvizler.option.query,
                         sgvizler.ui.getQueryOptionAttr(this));
                $.extend(query.chartOptions,
                         sgvizler.ui.getChartOptionAttr(this));
                query.draw();
            });
        },

        // kept in separate files:
        option: {},   // settings, global variables.
        chart: {},    // the set of user-defined rendering functions.
        charts: {},   // functions for handling rendering functions.
        parser: {},   // SPARQL results XML/JSON parser.
        ui: {}       // html get/set functions.
    };

    jQuery.ajaxSetup({
        accepts: {
            xml:  "application/sparql-results+xml",
            json: "application/sparql-results+json"
        }
    });
    sgvizler.option = {

        home: (window.location.href).replace(window.location.search, ""),
        homefolder: "",
        libfolder: "/lib/",

        stylepath:"",

        //// Prefixes included in queries:
        namespace: {
            'rdf' : "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            'rdfs': "http://www.w3.org/2000/01/rdf-schema#",
            'owl' : "http://www.w3.org/2002/07/owl#",
            'xsd' : "http://www.w3.org/2001/XMLSchema#"
        },

        query: {}, // holds options set by user in html file.
        chart: {}  // ditto.
    };
    sgvizler.ui = {

        //// #id's to html elements:
        id: {
            script:       'sgvzlr_script',    // #id to the script tag for this file
            chartCon:     'sgvzlr_gchart',    // #id to the container to hold the chart
            queryForm:    'sgvzlr_formQuery', //
            queryTxt:     'sgvzlr_cQuery',    // query text area.
            formQuery:    'sgvzlr_strQuery',  // hidden query string. "trick" taken from snorql.
            formWidth:    'sgvzlr_strWidth',  //
            formHeight:   'sgvzlr_strHeight', //
            formChart:    'sgvzlr_optChart',  //
            prefixCon:    'sgvzlr_cPrefix',   // print prefixes
            messageCon:   'sgvzlr_cMessage'  // print messages
        },

        attr: {
            prefix:      'data-sgvizler-',
            prefixChart: 'data-sgvizler-chart-options',

            valueAssign: '=',
            valueSplit:  '|'
        },

        params: [ 'query', 'chart', 'width', 'height' ], // permissible URL parameters.

        displayUI: function (queryOpt) {
            this.displayPrefixes();
            this.displayChartTypesMenu();
            this.displayUserInput(queryOpt);
        },
        displayPrefixes: function () {
            this.setElementText(this.id.prefixCon, sgvizler.query.prototype.getPrefixes());
        },
        displayUserInput: function (queryOpt) {
            this.setElementValue(this.id.queryTxt, queryOpt.query);
            this.setElementValue(this.id.formChart, queryOpt.chart);
            this.setElementValue(this.id.formWidth, queryOpt.chartOptions.width);
            this.setElementValue(this.id.formHeight, queryOpt.chartOptions.height);
        },
        displayChartTypesMenu: function () {
            var chart,
                i;
            if (this.isElement(this.id.formChart)) {
                chart = sgvizler.charts.all;
                for (i = 0; i < chart.length; i += 1) {
                    $('#' + this.id.formChart)
                        .append($('<option/>')
                                .val(chart[i].id)
                                .html(chart[i].id));
                }
            }
        },

        displayFeedback: function (queryOpt, messageName) {
            var message,
                container = queryOpt.container;
            if (queryOpt.container === this.id.chartCon && this.isElement(this.id.messageCon)) {
                container = this.id.messageCon;
            }

            if (queryOpt.loglevel === 0) {
                message = "";
            } else if (queryOpt.loglevel === 1) {
                if (messageName === "LOADING") {
                    message = "Loading...";
                } else if (messageName === "ERROR_ENDPOINT" || messageName === "ERROR_UNKNOWN") {
                    message = "Error.";
                }
            } else {
                if (messageName === "LOADING") {
                    message = "Sending query...";
                } else if (messageName === "ERROR_ENDPOINT") {
                    message = "Error querying endpoint. Possible errors:" +
                        this.html.ul(
                            this.html.a(queryOpt.endpoint, "SPARQL endpoint") + " down? " +
                                this.html.a(queryOpt.endpoint + queryOpt.endpoint_query_url + queryOpt.encodedQuery,
                                            "Check if query runs at the endpoint") + ".",
                            "Malformed SPARQL query? " +
                                this.html.a(queryOpt.validator_query_url + queryOpt.encodedQuery, "Check if it validates") + ".",
                            "CORS supported and enabled? Read more about " +
                                this.html.a("http://code.google.com/p/sgvizler/wiki/Compatibility", "CORS and compatibility") + ".",
                            "Is your " + this.html.a("http://code.google.com/p/sgvizler/wiki/Compatibility", "browser support") + "ed?",
                            "Hmm.. it might be a bug! Please file a report to " +
                                this.html.a("http://code.google.com/p/sgvizler/issues/", "the issues") + "."
                        );
                } else if (messageName === "ERROR_UNKNOWN") {
                    message = "Unknown error.";
                } else if (messageName === "NO_RESULTS") {
                    message = "Query returned no results.";
                } else if (messageName === "DRAWING") {
                    message = "Received " + queryOpt.noRows + " rows. Drawing chart...<br/>" +
                        this.html.a(queryOpt.endpoint + queryOpt.endpoint_query_url + queryOpt.encodedQuery,
                                    "View query results", "target='_blank'") + " (in new window).";
                }
            }
            this.setElementHTML(container, this.html.tag("p", message));
        },

        setElementValue: function (elementID, value) {
            if (this.isElement(elementID)) {
                $('#' + elementID).val(value);
            }
        },
        setElementText: function (elementID, value) {
            if (this.isElement(elementID)) {
                $('#' + elementID).text(value);
            }
        },
        setElementHTML: function (elementID, value) {
            if (this.isElement(elementID)) {
                $('#' + elementID).html(value);
            }
        },
        isElement: function (elementID) {
            return $('#' + elementID).length > 0;
        },

        getQueryOptionAttr: function (element) {
            var i,
                queryOpt = {container: $(element).attr('id')},
                attr = element.attributes;
            for (i = 0; i < attr.length; i += 1) {
                if (attr[i].name.lastIndexOf(this.attr.prefix, 0) === 0) { // starts-with attr.prefix.
                    queryOpt[attr[i].name.substring(this.attr.prefix.length)] = attr[i].value;
                }
            }
            return queryOpt;
        },
        getChartOptionAttr: function (element) {
            var i,
                options,
                assignment,
                path,
                o,
                j,
                chartOpt = {},
                attrValue = $(element).attr(sgvizler.ui.attr.prefixChart);
            if (typeof attrValue !== 'undefined') {
                options = attrValue.split(this.attr.valueSplit);
                for (i = 0; i < options.length; i += 1) {
                    assignment = options[i].split(this.attr.valueAssign);
                    path = assignment[0].split(".");
                    o = chartOpt;
                    for (j = 0; j < path.length - 1; j += 1) {
                        if (typeof o[path[j]] === 'undefined') {
                            o[path[j]] = {};
                        }
                        o = o[path[j]];
                    }
                    o[path[j]] = assignment[1];
                }
            }
            // get width and heigth from css. take only numbers.
            chartOpt.width = /(\d+)/.exec($(element).css('width'))[1];
            chartOpt.height = /(\d+)/.exec($(element).css('height'))[1];
            return chartOpt;
        },

        getUrlParams: function () {
            /*jslint regexp: true */
            var urlParams = {},
                e,
                r = /([^&=]+)=?([^&]*)/g, // parameter, value pairs.
                d = function (s) { return decodeURIComponent(s.replace(/\+/g, " ")); }, // replace '+' with space.
                q = window.location.search.substring(1); // URL query string part.

            while ((e = r.exec(q))) {
                if (e[2].length > 0 && this.params.indexOf(e[1]) !== -1) {
                    urlParams[d(e[1])] = d(e[2]);
                }
            }
            return urlParams;
        },

        resetPage: function () {
            document.location = sgvizler.home;
        },
        submitQuery: function () {
            $('#' + this.id.formQuery).val($('#' + this.id.queryTxt).val());
            $('#' + this.id.queryForm).submit();
        },

        html: {
            a: function (href, link, attr) {
                if (typeof attr === 'undefined') { attr = ""; }
                if (typeof href !== 'undefined' && typeof link !== 'undefined') {
                    return "<a " + attr + " href='" + href + "'>" + link + "</a>";
                }
            },
            ul: function () {
                var i,
                    txt;
                if (arguments.length) {
                    txt = "<ul>";
                    for (i = 0; i < arguments.length; i += 1) {
                        txt += "<li>" + arguments[i] + "</li>";
                    }
                    return txt + "</ul>";
                }
            },
            tag: function (tag, content) {
                return "<" + tag + ">" + content + "</" + tag + ">";
            }
        }
    };

    sgvizler.parser = {

        // variable notation: xtable, xcol(s), xrow(s) -- x is 's'(parql) or 'g'(oogle).

        defaultGDatatype: 'string',

        countRowsSparqlXML: function (sxml) {
            return $(sxml).find('sparql').find('results').find('result').length;
        },

        countRowsSparqlJSON: function (stable) {
            if (typeof stable.results.bindings !== 'undefined') {
                return stable.results.bindings.length;
            }
        },

        SparqlXML2GoogleJSON: function (sxml) {
            var c,
                r,
                gcols = [],
                grows = [],
                gdatatype = [], // for easy reference of datatypes
                sresults = $(sxml).find('sparql').find('results').find('result');

            // gcols
            c = 0;
            $(sxml).find('sparql').find('head').find('variable').each(function () {
                var stype = null,
                    sdatatype = null,
                    name = $(this).attr('name'),
                    scell = null,
                    scells = $(sresults).find('binding[name="' + name + '"]');
                if (scells.length) {
                    scell = $(scells).first().children().first()[0]; // uri, literal element
                    stype = scell.nodeName;
                    sdatatype = $(scell).attr('datatype');
                }
                gdatatype[c] = sgvizler.parser.getGoogleJsonDatatype(stype, sdatatype);
                gcols[c] = {'id': name, 'label': name, 'type': gdatatype[c]};
                c += 1;
            });

            // grows
            r = 0;
            $(sresults).each(function () {
                var gvalue,
                    scells,
                    scell,
                    stype,
                    svalue,
                    grow = [];
                for (c = 0; c < gcols.length; c += 1) {
                    gvalue = null;
                    scells = $(this).find('binding[name="' + gcols[c].id + '"]');
                    if (scells.length &&
                            typeof $(scells).first().children().first() !== 'undefined' &&
                            $(scells).first().children().first().firstChild !== null) {
                        scell = $(scells).first().children().first()[0]; // uri, literal element
                        stype = scell.nodeName;
                        svalue = $(scell).first().text();
                        gvalue = sgvizler.parser.getGoogleJsonValue(svalue, gdatatype[c], stype);
                    }
                    grow[c] = {'v': gvalue};
                }
                grows[r] = {'c': grow};
                r += 1;
            });
            return {'cols': gcols, 'rows': grows};
        },

        SparqlJSON2GoogleJSON: function (stable) {
            var c,
                r,
                srow,
                grow,
                gvalue,
                stype,
                sdatatype,
                gcols = [],
                grows = [],
                gdatatype = [], // for easy reference of datatypes
                scols = stable.head.vars,
                srows = stable.results.bindings;

            for (c = 0; c < scols.length; c += 1) {
                r = 0;
                stype = null;
                sdatatype = null;
                // find a row where there is a value for this column
                while (typeof srows[r][scols[c]] === 'undefined' && r + 1 < srows.length) { r += 1; }
                if (typeof srows[r][scols[c]] !== 'undefined') {
                    stype = srows[r][scols[c]].type;
                    sdatatype = srows[r][scols[c]].datatype;
                }
                gdatatype[c] = this.getGoogleJsonDatatype(stype, sdatatype);
                gcols[c] = {'id': scols[c], 'label': scols[c], 'type': gdatatype[c]};
            }

            // loop rows
            for (r = 0; r < srows.length; r += 1) {
                srow = srows[r];
                grow = [];
                // loop cells
                for (c = 0; c < scols.length; c += 1) {
                    gvalue = null;
                    if (typeof srow[scols[c]] !== 'undefined' &&
                            typeof srow[scols[c]].value !== 'undefined') {
                        gvalue = this.getGoogleJsonValue(srow[scols[c]].value, gdatatype[c], srow[scols[c]].type);
                    }
                    grow[c] = { 'v': gvalue };
                }
                grows[r] = {'c': grow};
            }
            return {'cols': gcols, 'rows': grows};
        },

        getGoogleJsonValue: function (value, gdatatype, stype) {
            var newvalue;
            if (gdatatype === 'number') {
                newvalue = Number(value);
            } else if (gdatatype === 'date') {
                //assume format yyyy-MM-dd
                newvalue = new Date(value.substr(0, 4),
                                value.substr(5, 2),
                                value.substr(8, 2));
            } else if (gdatatype === 'datetime') {
                //assume format yyyy-MM-ddZHH:mm:ss
                newvalue = new Date(value.substr(0, 4),
                                value.substr(5, 2),
                                value.substr(8, 2),
                                value.substr(11, 2),
                                value.substr(14, 2),
                                value.substr(17, 2));
            } else if (gdatatype === 'timeofday') {
                //assume format HH:mm:ss
                newvalue = [value.substr(0, 2),
                        value.substr(3, 2),
                        value.substr(6, 2)];
            } else { // datatype === 'string' || datatype === 'boolean'
                if (stype === 'uri') { // replace namespace with prefix
                    newvalue = this.prefixify(value);
                }
                newvalue = value;
            }
            return newvalue;
        },

        getGoogleJsonDatatype: function (stype, sdatatype) {
            var gdatatype = this.defaultGDatatype,
                xsdns = sgvizler.option.namespace.xsd;
            if (typeof stype !== 'undefined' && (stype === 'typed-literal' || stype === 'literal')) {
                if (sdatatype === xsdns + "float"   ||
                        sdatatype === xsdns + "double"  ||
                        sdatatype === xsdns + "decimal" ||
                        sdatatype === xsdns + "int"     ||
                        sdatatype === xsdns + "long"    ||
                        sdatatype === xsdns + "integer") {
                    gdatatype =  'number';
                } else if (sdatatype === xsdns + "boolean") {
                    gdatatype =  'boolean';
                } else if (sdatatype === xsdns + "date") {
                    gdatatype =  'date';
                } else if (sdatatype === xsdns + "dateTime") {
                    gdatatype =  'datetime';
                } else if (sdatatype === xsdns + "time") {
                    gdatatype =  'timeofday';
                }
            }
            return gdatatype;
        },

        prefixify: function (url) {
            var ns;
            for (ns in sgvizler.option.namespace) {
                if (sgvizler.option.namespace.hasOwnProperty(ns) &&
                        url.lastIndexOf(sgvizler.option.namespace[ns], 0) === 0) {
                    return url.replace(sgvizler.option.namespace[ns], ns + ":");
                }
            }
            return url;
        },
        unprefixify: function (qname) {
            var ns;
            for (ns in sgvizler.option.namespace) {
                if (sgvizler.option.namespace.hasOwnProperty(ns) &&
                        qname.lastIndexOf(ns + ":", 0) === 0) {
                    return qname.replace(ns + ":", sgvizler.option.namespace[ns]);
                }
            }
            return qname;
        }
    };


    /*global XDomainRequest */

    sgvizler.query = function (container) {
        this.container = container;

        //defaults
        this.query = "SELECT ?class (count(?instance) AS ?noOfInstances)\nWHERE{ ?instance a ?class }\nGROUP BY ?class\nORDER BY ?class";
        this.endpoint = "http://sws.ifi.uio.no/sparql/world";
        this.endpoint_output = 'json';  // xml, json, jsonp
        this.endpoint_query_url = "?output=text&amp;query=";
        this.validator_query_url = "http://www.sparql.org/query-validator?languageSyntax=SPARQL&amp;outputFormat=sparql&amp;linenumbers=true&amp;query=";
        this.chart = 'gLineChart';
        this.loglevel = 2;

        this.chartOptions = {
            'width':           '800',
            'height':          '400',
            'chartArea':       { left: '5%', top: '5%', width: '75%', height: '80%' },
            'gGeoMap': {
                'dataMode':           'markers'
            },
            'gMap': {
                'dataMode':           'markers'
            },
            'sMap': {
                'dataMode':           'markers',
                'showTip':            true,
                'useMapTypeControl':  true
            },
            'gSparkline': {
                'showAxisLines':      false
            }
        };
    };

    sgvizler.query.prototype.draw = function (listeners,options,callback) {
        var that = this,
            chartFunc = sgvizler.charts.getChart(this.container, this.chart);
        this.setChartSpecificOptions();
        this.insertFrom();
        $.extend(this.chartOptions,options);
        this.runQuery(function (data) {
            var dataTable = new google.visualization.DataTable(that.processQueryResults(data));
            for(var listener in listeners) {
                google.visualization.events.addListener(chartFunc, listener, function(){
                    listeners[listener](chartFunc,dataTable);
                });
            }
            chartFunc.draw(dataTable,that.chartOptions);
            if(callback)callback(dataTable);
        });
    };

    sgvizler.query.prototype.runQuery = function (callback) {
        var xdr,
            url,
            endpoint_output = this.endpoint_output;
        sgvizler.ui.displayFeedback(this, "LOADING");
        this.encodedQuery = encodeURIComponent(this.getPrefixes() + this.query);
        if (this.endpoint_output !== 'jsonp' && $.browser.msie && window.XDomainRequest) {
            xdr = new XDomainRequest();
            url = this.endpoint +
                "?query=" + this.encodedQuery +
                "&output=" + this.endpoint_output;
            xdr.open("GET", url);
            xdr.onload = function () {
                var data;
                if (endpoint_output === "xml") {
                    data = $.parseXML(xdr.responseText);
                } else {
                    data = $.parseJSON(xdr.responseText);
                }
                callback(data);
            };
            xdr.send();
        } else {
            $.get(this.endpoint,
                  { query: this.getPrefixes() + this.query,
                    output: (this.endpoint_output === 'jsonp') ? 'json' : this.endpoint_output },
                  function (data) { callback(data); },
                  this.endpoint_output)
                .error(function () {
                    sgvizler.ui.displayFeedback(this, "ERROR_ENDPOINT");
                });
        }
    };

    sgvizler.query.prototype.processQueryResults = function (data) {
        this.setResultRowCount(data);
        if (this.noRows === null) {
            sgvizler.ui.displayFeedback(this, "ERROR_UNKNOWN");
        } else if (this.noRows === 0) {
            sgvizler.ui.displayFeedback(this, "NO_RESULTS");
        } else {
            sgvizler.ui.displayFeedback(this, "DRAWING");
            return this.getGoogleJSON(data);
        }
    };

    sgvizler.query.prototype.setResultRowCount = function (data) {
        if (this.endpoint_output === 'xml') {
            this.noRows = sgvizler.parser.countRowsSparqlXML(data);
        } else {
            this.noRows = sgvizler.parser.countRowsSparqlJSON(data);
        }
    };

    sgvizler.query.prototype.getGoogleJSON = function (data) {
        if (this.endpoint_output === 'xml') {
            data = sgvizler.parser.SparqlXML2GoogleJSON(data);
        } else {
            data = sgvizler.parser.SparqlJSON2GoogleJSON(data);
        }
        return data;
    };

    sgvizler.query.prototype.insertFrom = function () {
        if (typeof this.rdf !== 'undefined') {
            var i,
                froms = this.rdf.split(sgvizler.ui.attr.valueSplit),
                from = "";
            for (i = 0; i < froms.length; i += 1) {
                from += 'FROM <' + froms[i] + '>\n';
            }
            this.query = this.query.replace(/(WHERE)?(\s)*\{/, '\n' + from + 'WHERE {');
        }
    };

    sgvizler.query.prototype.getPrefixes = function () {
        var prefix,
            prefixes = "";
        for (prefix in sgvizler.option.namespace) {
            if (sgvizler.option.namespace.hasOwnProperty(prefix)) {
                prefixes += "PREFIX " + prefix + ": <" + sgvizler.option.namespace[prefix] + ">\n";
            }
        }
        return prefixes;
    };

    sgvizler.query.prototype.setChartSpecificOptions = function () {
        var level1,
            level2;
        for (level1 in this.chartOptions) {
            if (this.chartOptions.hasOwnProperty(level1) &&
                    level1 === this.chart) {
                for (level2 in this.chartOptions[level1]) {
                    if (this.chartOptions[level1].hasOwnProperty(level2)) {
                        this.chartOptions[level2] = this.chartOptions[level1][level2];
                    }
                }
            }
        }
    };

    sgvizler.charts = {
        // Package for handling rendering functions. The rendering
        // functions themselves are kept in sgvizler.chart.*

        all: [],

        loadCharts: function () {
            var googlecharts = [
                { 'id': "gLineChart",        'func': google.visualization.LineChart },
                { 'id': "gAreaChart",        'func': google.visualization.AreaChart },
                { 'id': "gSteppedAreaChart", 'func': google.visualization.SteppedAreaChart },
                { 'id': "gPieChart",         'func': google.visualization.PieChart },
                { 'id': "gBubbleChart",      'func': google.visualization.BubbleChart },
                { 'id': "gColumnChart",      'func': google.visualization.ColumnChart },
                { 'id': "gBarChart",         'func': google.visualization.BarChart },
                { 'id': "gSparkline",        'func': google.visualization.ImageSparkLine },
                { 'id': "gScatterChart",     'func': google.visualization.ScatterChart },
                { 'id': "gCandlestickChart", 'func': google.visualization.CandlestickChart },
                { 'id': "gGauge",            'func': google.visualization.Gauge },
                { 'id': "gOrgChart",         'func': google.visualization.OrgChart },
                { 'id': "gTreeMap",          'func': google.visualization.TreeMap },
                { 'id': "gTimeline",         'func': google.visualization.AnnotatedTimeLine },
                { 'id': "gMotionChart",      'func': google.visualization.MotionChart },
                { 'id': "gGeoChart",         'func': google.visualization.GeoChart },
                { 'id': "gGeoMap",           'func': google.visualization.GeoMap },
                { 'id': "gMap",              'func': google.visualization.Map },
                { 'id': "gTable",            'func': google.visualization.Table }
            ],
                chart;

            $.merge(this.all, googlecharts);
            for (chart in sgvizler.chart) {
                if (sgvizler.chart.hasOwnProperty(chart)) {
                    this.register(
                        sgvizler.chart[chart].prototype.id,
                        sgvizler.chart[chart]
                    );
                }
            }
        },

        register: function (id, func) {
            this.all.push({'id': id, 'func': func});
        },

        getChart: function (containerId, chartId) {
            var i,
                container = document.getElementById(containerId);
            for (i = 0; i < this.all.length; i += 1) {
                if (chartId === this.all[i].id) {
                    return new this.all[i].func(container);
                }
            }
        }
    };


    /*global d3 */
    /** dForceGraph **


        D3 force directed graph. Under development.
    */
    sgvizler.chart.dForceGraph = function (container) { this.container = container; };
    sgvizler.chart.dForceGraph.prototype = {
        id:   "dForceGraph",
        draw: function (data, chartOpt) {
            var noColumns = data.getNumberOfColumns(),
                noRows = data.getNumberOfRows(),
                opt = $.extend({'maxnodesize': 15, 'minnodesize': 2 }, chartOpt), // set defaults
                colors = d3.scale.category20(),
                w = chartOpt.width,
                h = chartOpt.height,
                isNumber = function (n) {  return !isNaN(parseFloat(n)) && isFinite(n); },

                // build arrays of nodes and links.
                nodes = [],
                edges = [],
                t_color = {},
                t_size = {},
                t_maxnodesize = 0,

                r,
                source,
                target,

                nodesizeratio,
                i,
                color,
                size,

                vis,
                force,
                link,
                node,
                ticks;

            for (r = 0; r < noRows; r += 1) {
                source = data.getValue(r, 0);
                target = data.getValue(r, 1);
                // nodes
                if (source !== null && $.inArray(source, nodes) === -1) {
                    nodes.push(source);
                    t_size[source] = (noColumns > 2) ? Math.sqrt(data.getValue(r, 2)) : 0;
                    t_color[source] = (noColumns > 3) ? data.getValue(r, 3) : 0;
                    if (t_size[source] > t_maxnodesize) {
                        t_maxnodesize = t_size[source];
                    }
                }
                if (target !== null && $.inArray(target, nodes) === -1) {
                    nodes.push(target);
                }
                // edges
                if (source !== null && target !== null) {
                    edges.push({'source': $.inArray(source, nodes),
                                'target': $.inArray(target, nodes)
                            }
                        );
                }
            }
            if (t_maxnodesize === 0) {
                t_maxnodesize = 1;
            }
            nodesizeratio = opt.maxnodesize / t_maxnodesize;
            for (i = 0; i < nodes.length; i += 1) {
                color = typeof t_color[nodes[i]] !== 'undefined' ?
                        t_color[nodes[i]] :
                        1;
                size = isNumber(t_size[nodes[i]]) ?
                        opt.minnodesize + t_size[nodes[i]] * nodesizeratio :
                        opt.minnodesize;

                nodes[i] = {'name': nodes[i], 'color': color, 'size': size };
            }

            $(this.container).empty();

            vis = d3.select(this.container)
                .append("svg:svg")
                .attr("width", w)
                .attr("height", h)
                .attr("pointer-events", "all")
                .append('svg:g')
                .call(d3.behavior.zoom().on("zoom", function () {
                    vis.attr("transform", "translate(" + d3.event.translate + ")" +
                         " scale(" + d3.event.scale + ")");
                }))
                .append('svg:g');

            vis.append('svg:rect')
                .attr('width', w)
                .attr('height', h)
                .attr('fill', 'white');

            force = d3.layout.force()
                .gravity(0.05)
                .distance(100)
                .charge(-100)
                .nodes(nodes)
                .links(edges)
                .size([w, h])
                .start();

            link = vis.selectAll("line.link")
                .data(edges)
                .enter().append("svg:line")
                .attr("class", "link")
                //.style("stroke-width", function (d) { return Math.sqrt(d.value); })
                .attr("x1", function (d) { return d.source.x; })
                .attr("y1", function (d) { return d.source.y; })
                .attr("x2", function (d) { return d.target.x; })
                .attr("y2", function (d) { return d.target.y; });

            node = vis.selectAll("g.node")
                .data(nodes)
                .enter().append("svg:g")
                .attr("class", "node")
                .call(force.drag);

            node.append("svg:circle")
                .style("fill", function (d) { return colors(d.color); })
                .attr("class", "node")
                .attr("r", function (d) { return d.size; });

            node.append("svg:title")
                .text(function (d) { return d.name; });

            node.append("svg:text")
                .attr("class", "nodetext")
                .attr("dx", 12)
                .attr("dy", ".35em")
                .text(function (d) { return d.name; });

            ticks = 0;
            force.on("tick", function () {
                ticks += 1;
                if (ticks > 250) {
                    force.stop();
                    force.charge(0)
                        .linkStrength(0)
                        .linkDistance(0)
                        .gravity(0)
                        .start();
                }

                link.attr("x1", function (d) { return d.source.x; })
                    .attr("y1", function (d) { return d.source.y; })
                    .attr("x2", function (d) { return d.target.x; })
                    .attr("y2", function (d) { return d.target.y; });

                node.attr("transform", function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
            });
        }
    };


    /*global Graph */
    /** rdGraph **

      Original version written by Magnus Stuhr.

      Draws a graph with clickable and movable nodes. 

      Input format:
      - 7 columns, last three are optional.
      - each row represents a source node, a target node and an edge from source to target.
      - the URIs are the id's for the nodes, and make the nodes clickable.
      
      1             2         3         4             5           6             7
      sourceURI sourceLabel   targetURI targetLabel   edgeLabel   sourceColor   targetColor

    */
    sgvizler.chart.rdGraph = function (container) { this.container = container; };
    sgvizler.chart.rdGraph.prototype = {
        id: "rdGraph",
        draw: function (data, chartOpt) {

            var numberOfColumns = data.getNumberOfColumns(),
                numberOfRows = data.getNumberOfRows(),

                // set defaults.
                opt = $.extend({
                    noderadius: 0.5,
                    nodefontsize: "10px",
                    nodeheight: 20,
                    nodestrokewidth: "1px",
                    nodecornerradius: "1px",
                    nodepadding: 7,
                    nodecolor: "green",
                    edgestroke: "blue",
                    edgefill: "blue",
                    edgestrokewidth: 1,
                    edgefontsize: "10px",
                    edgeseparator: ", "
                }, chartOpt),

                graph = new Graph(),
                layouter,
                renderer,
                row,
                i,
                edge,
                source,
                target,
                label,

                // custom node rendering using Raphael.
                nodeRenderer = function (color, URL) {
                    return function (r, n) {
                        return r.set()
                            // rectangle
                            .push(r.rect(n.point[0],
                                        n.point[1],
                                        n.label.length * opt.nodepadding,
                                        opt.nodeheight)
                                 .attr({"fill": color,
                                        "stroke-width": opt.nodestrokewidth,
                                        "r" : opt.nodecornerradius}))
                           // label inside rectangle
                            .push(r.text(n.point[0] + n.label.length * opt.nodepadding / 2,
                                        n.point[1] + opt.nodeheight / 2,
                                        n.label)
                                 .attr({"font-size": opt.nodefontsize})
                                 .click(function () { if (URL) { window.open(sgvizler.parser.unprefixify(URL)); } })
                                );
                    };
                },

                // helper function.
                addNode = function (URL, name, color) {
                    graph.addNode(URL, {label: name, render: nodeRenderer(color, URL)});
                    //console.log("add node - name: " + name + ", URL: " + URL);
                },
                edges = {},
                keys_edges = [];

            for (row = 0; row < numberOfRows; row += 1) {
                source = data.getValue(row, 0);
                target = data.getValue(row, 2);

                // add source node
                // Note: dracula library takes care of duplicates?
                if (source) {
                    addNode(source,
                            data.getValue(row, 1) || source,
                            numberOfColumns > 5 ? data.getValue(row, 5) : opt.nodecolor);
                }
                // add target node
                if (target) {
                    addNode(target,
                            data.getValue(row, 3) || target,
                            numberOfColumns > 6 ? data.getValue(row, 6) : opt.nodecolor);
                }

                // collect edge labels. Only one edge per pair of nodes,
                // so we concatinate labels of multiple edges into one.
                if (source && target) {
                    label = "";
                    // test if source--target pair is seen before:
                    if (typeof edges[source + target] !== 'undefined') {
                        label = edges[source + target].label; // retrieve accumulated label.
                    } else {
                        keys_edges.push(source + target);
                    }

                    if (numberOfColumns > 4 && data.getValue(row, 4).length > 0) {
                        if (label.length > 0) {
                            label += opt.edgeseparator;
                        }
                        label += data.getValue(row, 4);
                    }

                    edges[source + target] = {
                        'source': source,
                        'target': target,
                        'label': label
                    };
                }
            }

            // add edges
            for (i = 0; i < keys_edges.length; i += 1) {
                edge = edges[keys_edges[i]];
                //console.log("add edge - source: " + edge.source + ", target " + edge.target);
                graph.addEdge(edge.source, edge.target,
                              { "stroke": opt.edgestroke,
                                "fill": opt.edgefill,
                                "label": edge.label,
                                "width": opt.edgestrokewidth,
                                "fontsize": opt.edgefontsize
                              });
            }

            layouter = new Graph.Layout.Spring(graph);
            layouter.layout();

            $(this.container).empty();
            renderer = new Graph.Renderer.Raphael(this.container, graph, opt.width, opt.height, {"noderadius": opt.nodeheight * opt.noderadius});
            renderer.draw();
        }
    };    /** sDefList **


     Make a html dt list.


     Format, 2--N columns:
     1. Term
     2--N. Definition


     Available options:
     'cellSep'   :  string (can be html) to separate cells in definition columns. (default: ' ')
     'termPrefix  :  string (can be html) to prefix each term with. (default: '')
     'termPostfix :  string (can be html) to postfix each term with. (default: ':')
     'definitionPrefix  :  string (can be html) to prefix each definition with. (default: '')
     'definitionPostfix :  string (can be html) to postfix each definition with. (default: '')
    */
    sgvizler.chart.DefList = function (container) { this.container = container; };
    sgvizler.chart.DefList.prototype = {
        id:   "sDefList",
        draw: function (data, chartOpt) {
            var r,
                c,
                term,
                definition,
                noColumns = data.getNumberOfColumns(),
                noRows = data.getNumberOfRows(),
                opt = $.extend({ cellSep: ' ', termPrefix: '', termPostfix: ':', definitionPrefix: '', definitionPostfix: '' }, chartOpt),
                list = $(document.createElement('dl'));


            for (r = 0; r < noRows; r += 1) {
                term = opt.termPrefix + data.getValue(r, 0) + opt.termPostfix;
                list.append($(document.createElement('dt')).html(term));
                definition = opt.definitionPrefix;
                for (c = 1; c < noColumns; c += 1) {
                    definition += data.getValue(r, c);
                    if (c + 1 !== noColumns) {
                        definition += opt.cellSep;
                    }
                }
                definition += opt.definitionPostfix;
                list.append($(document.createElement('dd')).html(definition));
            }
            $(this.container).empty();
            $(this.container).append(list);
        }
    };
    /** sList **


     Make a html list, either numbered (ol) or bullets (ul). Each row
     becomes a list item.


     Any number of columns in any format. Everything is displayed as text.


     Available options:
     'list'      :  "ol" / "ul"  (default: "ul")
     'cellSep'   :  string (can be html) to separate cells in row. (default: ', ')
     'rowPrefix  :  string (can be html) to prefix each row with. (default: '')
     'rowPostfix :  string (can be html) to postfix each row with. (default: '')
    */
    sgvizler.chart.List = function (container) { this.container = container; };
    sgvizler.chart.List.prototype = {
        id:   "sList",
        draw: function (data, chartOpt) {
            var noColumns = data.getNumberOfColumns(),
                noRows = data.getNumberOfRows(),
                opt = $.extend({ list: 'ul', cellSep: ', ', rowPrefix: '', rowPostfix: '' }, chartOpt),
                list = $(document.createElement(opt.list)),
                r,
                c,
                rowtext;


            for (r = 0; r < noRows; r += 1) {
                rowtext = opt.rowPrefix;
                for (c = 0; c < noColumns; c += 1) {
                    rowtext += data.getValue(r, c);
                    if (c + 1 !== noColumns) {
                        rowtext += opt.cellSep;
                    }
                }
                rowtext += opt.rowPostfix;
                list.append($(document.createElement('li')).html(rowtext));
            }
            $(this.container).empty();
            $(this.container).append(list);
        }
    };


    /** sMap **


     Extends gMap in markers dataMode. Draws textboxes with heading,
     paragraph, link and image. The idea is to put all columns > 2 into
     the 3. column with html formatting.


     - Data Format 2--6 columns:
       1. lat
       2. long
       3. name  (optional)
       4. text  (optional)
       5. link  (optional)
       6. image (optional)


     - If < 4 columns, then behaves just as gMap
     - Only 6 columns will be read, columns > 6 are ignored.
    */
    sgvizler.chart.sMap = function (container) { this.container = container; };
    sgvizler.chart.sMap.prototype = {
        id:   "sMap",
        draw: function (data, chartOpt) {
            var chart,
                newData,
                newValue,
                noColumns = data.getNumberOfColumns(),
                r,
                c;


            if (noColumns > 3) {
                newData = data.clone();
                // drop columns > 3 from new
                for (c = noColumns - 1; c > 2; c -= 1) {
                    newData.removeColumn(c);
                }


                // build new 3. column
                for (r = 0; r < data.getNumberOfRows(); r += 1) {
                    newValue = "<div class='sgvizler sgvizler-sMap'>";
                    newValue += "<h1>" + data.getValue(r, 2) + "</h1>";
                    if (5 < noColumns && data.getValue(r, 5) !== null) {
                        newValue += "<div class='img'><img src='" + data.getValue(r, 5) + "'/></div>";
                    }
                    if (3 < noColumns && data.getValue(r, 3) !== null) {
                        newValue += "<p class='text'>" + data.getValue(r, 3) + "</p>";
                    }
                    if (4 < noColumns && data.getValue(r, 4) !== null) {
                        newValue += "<p class='link'><a href='" + sgvizler.parser.unprefixify(data.getValue(r, 4)) + "'>" + data.getValue(r, 4) + "</a></p>";
                    }
                    newValue += "</div>";
                    newData.setCell(r, 2, newValue);
                }
            } else { // do nothing.
                newData = data;
            }


            chart = new google.visualization.Map(this.container);
            chart.draw(newData, chartOpt);
        }
    };


    /** sTable **


     Make a html table.


     Available options:
     'headings'   :  "true" / "false"  (default: "true")
    */
    sgvizler.chart.Table = function (container) { this.container = container; };
    sgvizler.chart.Table.prototype = {
        id:   "sTable",
        draw: function (data, chartOpt) {
            var noColumns = data.getNumberOfColumns(),
                noRows = data.getNumberOfRows(),
                opt = $.extend({'headings': true }, chartOpt),
                table = $(document.createElement('table')),
                c,
                r,
                row;


            if (opt.headings) {
                row = $(document.createElement('tr'));
                for (c = 0; c < noColumns; c += 1) {
                    row.append($(document.createElement('th')).html(data.getColumnLabel(c)));
                }
                table.append(row);
            }


            for (r = 0; r < noRows; r += 1) {
                row = $(document.createElement('tr'));
                for (c = 0; c < noColumns; c += 1) {
                    row.append($(document.createElement('td')).html(data.getValue(r, c)));
                }
                table.append(row);
            }
            $(this.container).empty();
            $(this.container).append(table);
        }
    };

    /** sText **


     Write text.


     Any number of columns. Everything is displayed as text.


     Available options:
     'cellSep'       :  string (can be html) to separate cells in each column. (default: ', ')
     'cellPrefix     :  string (can be html) to prefix each cell with. (default: '')
     'cellPostfix    :  string (can be html) to postfix each cell  with. (default: '')
     'rowPrefix      :  string (can be html) to prefix each row with. (default: '<p>')
     'rowPostfix     :  string (can be html) to postfix each row with. (default: '</p>')
     'resultsPrefix  :  string (can be html) to prefix the results with. (default: '<div>')
     'resultsPostfix :  string (can be html) to postfix the results with. (default: '</div>')
    */
    sgvizler.chart.Text = function (container) { this.container = container; };
    sgvizler.chart.Text.prototype = {
        id:   "sText",
        draw: function (data, chartOpt) {
            var noColumns = data.getNumberOfColumns(),
                noRows = data.getNumberOfRows(),
                opt = $.extend({ cellSep: ', ',
                                 cellPrefix: '', cellPostfix: '',
                                 rowPrefix: '<p>', rowPostfix: '</p>',
                                 resultsPrefix: '<div>', resultsPostfix: '</div>' },
                               chartOpt),
                text = opt.resultsPrefix,
                r,
                c,
                row;


            for (r = 0; r < noRows; r += 1) {
                row = opt.rowPrefix;
                for (c = 0; c < noColumns; c += 1) {
                    row += opt.cellPrefix + data.getValue(r, c) + opt.cellPostfix;
                    if (c + 1 !== noColumns) {
                        row += opt.cellSep;
                    }
                }
                text += row + opt.rowPostfix;
            }
            text += opt.resultsPostfix;


            $(this.container).empty();
            $(this.container).html(text);
        }
    };
    global.sgvizler = sgvizler;
}(window));
