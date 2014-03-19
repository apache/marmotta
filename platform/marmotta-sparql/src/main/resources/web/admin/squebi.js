requirejs.config({
    paths: {
        async : "bower_components/requirejs-plugins/src/async",
        propertyParser : "bower_components/requirejs-plugins/src/propertyParser",
        goog : "bower_components/requirejs-plugins/src/goog",
        jquery : "bower_components/jquery/dist/jquery",
        angular : "bower_components/angular/angular",
        bootstrap : "bower_components/bootstrap/dist/js/bootstrap",
        bootstrapUI : "bower_components/angular-bootstrap/ui-bootstrap",
        codemirror : "bower_components/codemirror/lib/codemirror",
        codemirrorSparql : "bower_components/codemirror/mode/sparql/sparql",
        codemirrorUI : "bower_components/angular-ui-codemirror/ui-codemirror",
        codemirrorHint : "bower_components/codemirror/addon/hint/show-hint",
        _squebi : "bower_components/squebi/squebi/js/squebi",
        squebiBrowse : "bower_components/squebi/squebi/js/writer/squebi.browse",
        squebiJson : "bower_components/squebi/squebi/js/writer/squebi.json",
        squebiXml : "bower_components/squebi/squebi/js/writer/squebi.xml",
        squebiPie: "bower_components/squebi/squebi-writer/piechart/squebi.pie"
    },
    shim: {
        'goog': ['async','propertyParser'],
        'angular' : ['jquery'],
        'bootstrap' : ['jquery'],
        'bootstrapUI' : ['angular','bootstrap'],
        'codemirrorSparql' : ['codemirror'],
        'codemirrorUI' : ['codemirror','bootstrapUI'],
        'codemirrorHint' : ['codemirror'],
        '_squebi' : ['codemirrorHint','codemirrorUI','codemirrorSparql','bootstrapUI'],
        'squebiBrowse' : ['_squebi'],
        'squebiJson' : ['_squebi'],
        'squebiXml' : ['_squebi'],
        'squebiPie' : ['_squebi']
    },map: {
        '*': {
            'css': 'bower_components/require-css/css'
        }
    }
});

require([
    "css",
    "squebiBrowse",
    "squebiJson",
    "squebiXml",
    'goog!visualization,1,packages:[corechart]',
    "squebiPie",
    "css!bower_components/squebi/squebi/css/flags",
    "css!bower_components/bootstrap/dist/css/bootstrap",
    "css!bower_components/codemirror/lib/codemirror",
    "css!bower_components/codemirror/theme/neat",
    "css!bower_components/codemirror/addon/hint/show-hint",
    "css!bower_components/fontawesome/css/font-awesome",
    "css!bower_components/squebi/squebi/css/style"
], function() {
    angular.element(document).ready(function($http,$rootScope) {
        $http.get("bower_components/squebi/squebi/config.json",function(data) {
            data = jQuery.parseJSON(data);
            data.serviceURL.select = "../select";
            data.serviceURL.update = "../update" ;
            jQuery('#squebi_container').show();
            squeby.constant('SQUEBY',data);
            angular.bootstrap('#squebi_container', ['Squeby']);
        });
    });
});