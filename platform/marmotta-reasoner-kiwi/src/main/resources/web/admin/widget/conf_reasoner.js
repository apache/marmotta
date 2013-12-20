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
 * Created by .
 * User: Thomas Kurz
 * Date: 18.02.11
 * Time: 18:46
 * To change this template use File | Settings | File Templates.
 */
(function( $ ){

    var div;

    var title_input;
    var program_input;
    var list;
    
    CodeMirror.commands.autocomplete = function(cm) {
        CodeMirror.showHint(cm, CodeMirror.hint.skwrl);
    };

    $.fn.reasoning_config = function(options) {
        var settings = {
            host: 'http://localhost:8080/LMF/',
            samples :{
                skos:"@prefix skos: <http://www.w3.org/2004/02/skos/core#>\n\n($1 skos:broader $2) -> ($1 skos:broaderTransitive $2)\n($1 skos:narrower $2) -> ($1 skos:narrowerTransitive $2)\n($1 skos:broaderTransitive $2), ($2 skos:broaderTransitive $3) -> ($1 skos:broaderTransitive $3)\n($1 skos:narrowerTransitive $2), ($2 skos:narrowerTransitive $3) -> ($1 skos:narrowerTransitive $3)\n($1 skos:broader $2) -> ($2 skos:narrower $1)\n($1 skos:narrower $2) -> ($2 skos:broader $1)\n($1 skos:broader $2) -> ($1 skos:related $2)\n($1 skos:narrower $2) -> ($1 skos:related $2)\n($1 skos:related $2) -> ($2 skos:related $1)",
                rdfs:"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n\n($1 rdfs:subClassOf $2), ($2 rdfs:subClassOf $3) -> ($1 rdfs:subClassOf $3)\n($1 rdfs:subPropertyOf $2), ($2 rdfs:subPropertyOf $3) -> ($1 rdfs:subPropertyOf $3)\n($1 rdf:type $2), ($2 rdfs:subClassOf $3) -> ($1 rdf:type $3)\n($p rdfs:range $r), ($1 $p $2) -> ($2 rdf:type $r)\n($p rdfs:domain $d), ($1 $p $2) -> ($1 rdf:type $d)"
            },
            loader:"../../core/public/img/ajax-loader_small.gif"
        }

        var reload = function() {
            $.getJSON(settings.host+"reasoner/program/list",function(data) {
                writePrograms(data);
            });
        }

        var do_remove = function(name) {
            var url = settings.host+"reasoner/program/"+name;
            $(".loader").show();
            $.ajax({
                type:"DELETE",
                url: url,
                success: function() {
                    reload();
                    $(".loader").hide();
                    alert("Program successfully removed");
                },
                error: function(jXHR,textStatus) {
                    $(".loader").hide();
                    alert("Error: "+jXHR.responseText);
                }
            });
        }

        var do_upload = function() {
            var editor = $(program_input)[0]._cmEditor || {};
            if (editor && editor.save) {
                editor.save();
            }
            var data = program_input.val();
            if(data=="" || title_input.val() == "") {
                alert("Values may not be empty!");
                return;
            }
            var url = settings.host+"reasoner/program/"+title_input.val();

            //set img
            $(".loader").show();

            //upload
            $.ajax({
                type:"POST",
                url: url,
                contentType: "text/plain",
                data: data,
                success: function() {
                    reload();
                    $(".loader").hide();
                    alert("program was successfully uploaded");
                },
                error: function(jXHR) {
                    $(".loader").hide();
                    alert("Error: "+jXHR.responseText);
                }
            });
        }

        var writePrograms = function(programs) {
            list.html("");

            function writePS(ps) {
                for(var i=0;i<ps.length;i++) {
                    var name = ps[i].name;
                    var li = $("<li></li>");
                    var button = $("<button name='"+name+"' style='position:relative;left:50%;margin-top:7px;margin-left:-30px;'></button>").text("remove");
                    button.bind("click",function(){
                        do_remove($(this).attr('name'));
                    });
                    var title = $("<h4>").text(name);
                    var pr = $("<div>");
                    li.append(title);
                    li.append(pr);
                    li.append(button);
                    li.append('<img class="loader" src="'+settings.loader+'" style="display:none;float:right;margin:5px"></div>');
                    list.append(li);
                    createCodeMirror(pr, ps[i].rules, true);
                }
            }

            if(programs.length != 0) {
                writePS(programs)
            } else {
                list.html("<li>no programs loaded</li>");
            }
        }
        
        function createCodeMirror(target, content, readOnly) {
            target = $(target)[0];
            if (target._cmEditor || false) {
                removeCodeMirror(target);
            }
            if (readOnly) {
                target._cmEditor = new CodeMirror(target, {
                    readOnly: true,
                    lineNumbers: true,
                    matchBrackets: true,
                    mode: "skwrl"
                });
                target._cmEditor.toTextArea = function() {}; // To avoid errors because of a missing function in removeCodeMirror()
            } else {
                target._cmEditor = CodeMirror.fromTextArea(target, {
                    readOnly: false,
                    lineNumbers : true,
                    matchBrackets : true,
                    extraKeys: {"Ctrl-Space": "autocomplete"},
                    mode : "skwrl"
                });
            }
            if (content !== undefined) {
                target._cmEditor.setValue(content);
                if (!readOnly)
                    target._cmEditor.save();
            }
        }
        function removeCodeMirror(target) {
            if (target._cmEditor || false) {
                target._cmEditor.toTextArea();
                target._cmEditor = null;
            }
        }

        var write = function(programs) {
            div.html("");

            title_input = $("<input type='text' size='121'>");
            program_input = $("<textarea rows='10' cols='105'></textarea>");
            list = $("<ul></ul>");

            var table = $("<table/>");
            var tr1=$("<tr></tr>");
            var tr2=$("<tr></tr>");
            tr1.append("<td>Name:</td>");
            var td1 = $("<td></td>").append(title_input);
            tr1.append(td1);
            tr2.append("<td>Program:</td>");
            var td2 = $("<td></td>").append(program_input);
            tr2.append(td2);
            var button = $("<button style='position:relative;left:50%;margin-top:10px;margin-left:10px;'></button>").text("upload and run");
            button.bind("click",function(){
                do_upload();
            });
            var button2 = $("<button style='position:relative;left:50%;margin-top:10px;margin-left:-50px;'></button>").text("clear");
            button2.bind("click",function(){
                title_input.val("");
                createCodeMirror(program_input, "");
            });

            table.append(tr1);
            table.append(tr2);

            //set samples

            var options = "<option>---</option>";
            for(property in settings.samples) {
                options += "<option>"+property+"</option>";
            }
            var sam = $("<select></select>").html(options);
            sam.change(function(){
                var x = $(this).val();
                if(x=="---"){
                    title_input.val("");
                    createCodeMirror(program_input, "");
                    return;
                }
                title_input.val(x);
                createCodeMirror(program_input, settings.samples[x]);
            });
            var sam_div = $('<div style="position: relative; float: right; margin-bottom: 5px; margin-top: -20px;"><span style="font-size:12px;margin-right:5px">Samples:</span>');
            sam_div.append(sam);

            div.append("<h3>Running Programs</h3>")
            div.append(list);
            div.append("<h3 style='margin-top:30px;'>New Program</h3>");
            div.append(sam_div);
            div.append(table);
            div.append(button2);
            div.append(button);
            div.append('<img class="loader" src="'+settings.loader+'" style="display:none;float:right;margin:5px"></div>');

            createCodeMirror(program_input)
            
            writePrograms(programs);
        }

        return this.each(function() {
            // merge options
            if ( options ) {
                $.extend( settings, options );
            }
            div = $(this);
            //build skeleton;
            $.getJSON(settings.host+"reasoner/program/list",function(data) {
                write(data);
            });
        });
    };
})( jQuery );

