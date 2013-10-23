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
/* CORE MANAGEMENT */
$(function() {
    var cService = _SERVER_URL + 'ldpath/';
    
    var editor = null;
	var editorContainer = $("#editor");
	var templateChooser = $('select#templates');
	
	var statusTimeout;
    function tell(msg, css, autohide) {
	        var status = $(".status", editorContainer);
	        clearTimeout(statusTimeout);
	        if (msg && msg !== '') {
	            status.removeClass('ok error warning loading');
	            status.text(msg);
	            if (css) status.addClass(css);
	            if (autohide > 0) statusTimeout = setTimeout(function() {tell();}, autohide);
	            status.fadeIn();
	        } else {
	            status.fadeOut(function() {
	                status.text('');
	                status.removeClass('ok error warning loading');
	            });
	        }
	    }

	
/*
	function loadTemplateNames() {
		$('#chooserStatus').addClass('loading').text('Loading...').show();
		templateChooser.empty().append($('<option />').text('--').val(''));
		$.getJSON(cService, function(data) {
			for (i in data) {
			    templateChooser.append($('<option />').val(data[i]).text(data[i]));
			}
			$('#chooserStatus').fadeOut(function() {
				$(this).removeClass('loading').text('');
			});
		});
	}
*/

/*
	function loadTemplate(template) {
		var body = $("textarea", editorContainer);
		if (template !== "") {
			tell("Loading...", 'loading');
			$.get(cService + encodeURI(template), function(data) {
			    if (editorContainer.hasClass("editor_cm"))
			        createCodeMirror(data);
			    else
			        body.val(data);
			    tell("Loaded", 'ok', 2000);
			});
		}
		if (editor) editor.refresh();
	}
*/

	function runTest() {
		if (editor) editor.save();
		var program = $("textarea", editorContainer).val();
		
		var params = [];
		$("#test_context .context > a.context").each(function() {
			var uri = $(this).text();
			params.push(encodeURIComponent(uri));
		});
		tell("Testing LDPath...", 'loading');
		
		$.ajax(cService + "debug?context=" + params.join("&context="), {
			type: 'POST',
			data: program,
			contentType: "text/plain",
			success: function(data) {
				var tbl = $("table#testResults tbody").empty();
				for (var ctx in data) {
				    tbl.append($("<tr>", {class: "subheading"}).append($("<td>", { text: ctx, colspan: 3})));
				    if (typeof data[ctx] === 'string') {
	                    tbl.append($("<tr>").append($("<td>", { text: data[ctx], colspan: 3})));
				    } else
				    for (var field in data[ctx]) {
                        var row = $("<tr>").append($("<td>").html("&nbsp;"));
                        var k = $("<td>").text(field).appendTo(row);
                        var v = $("<td>").appendTo(row);
                        if ($.isArray(data[ctx][field])) {
                            for (var i in data[ctx][field]) {
                                v.append($('<div class="fieldValue">').text(data[ctx][field][i].value));
                            }
                        } else {
                            v.text(result[key]);
                        } 
                        tbl.append(row);
				    }
				}
				tell();
			},
			error: function(xhr) {
				tell(xhr.responseText, 'error');
			}
		});
	}
	
/*
	templateChooser.change(function() {
		var opt = $(':selected', $(this));
		var template = opt.val();
		loadTemplate(template);
	});
*/

	function addTestContext(uri) {
	    (function() {
	        var c = $("#test_context");
	        var span = $("<span>", {class: 'context'});
	        $("<a>", {text: uri, href: _SERVER_URL + "inspect?uri=" + encodeURIComponent(uri), target: "_blank", class: "context"}).appendTo(span);
	        span.append("&nbsp;");
	        $("<a>", {text: 'x', href: '#', class: "delete"}).click(function() {
	            span.remove(); 
	        }).appendTo(span);
	        span.appendTo(c);
	    })();
	    
	}
	
	
	function createCodeMirror(content) {
        if (editor) {
            removeCodeMirror();
        }
        var textarea = document.getElementById("ldpath");
        if (content !== undefined) {
            textarea.value = content;
        }
        editorContainer.removeClass("editor_plain").addClass("editor_cm");
        var defaultNamespaces = {}
        $.getJSON(_SERVER_URL + "ldpath/util/namespaces", function(data) {
            defaultNamespaces = data;
        }).complete(function() {
            CodeMirror.commands.autocomplete = function(cm) {
                CodeMirror.showHint(cm, CodeMirror.hint.ldpath);
            };
          editor = CodeMirror.fromTextArea(document.getElementById("ldpath"), {
              lineNumbers : true,
              matchBrackets : true,
              extraKeys: {"Ctrl-Space": "autocomplete"},
              mode : {
                name: "ldpath",
                baseURL: _SERVER_URL,
                namespaces: defaultNamespaces
              }
          });
        });
	}
	function removeCodeMirror() {
	    editorContainer.removeClass("editor_cm").addClass("editor_plain");
        if (editor) {
            editor.toTextArea();
            editor = null;
        }
	}
	$(".chooseEditor .editor_cm").click(function() {createCodeMirror(); return false; });
    $(".chooseEditor .editor_plain").click(function() {removeCodeMirror(); return false; });
    $("button#addContext").click(function() {
        var uri = $("input#addContextUri").val();
        addTestContext(uri);
        $("input#addContextUri").val('');
        return false;
    });
    $("button#runTest").click(runTest);
    
    createCodeMirror();
//	loadTemplateNames();
});
