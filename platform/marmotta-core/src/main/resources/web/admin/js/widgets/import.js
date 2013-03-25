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
function Importer(id,host) {

    var LMF = new MarmottaClient(host);
    //TODO
    var loader =$("<img style='position: relative;top: 4px;margin-left: 10px;' src='../public/img/loader/ajax-loader_small.gif'>");

    var container = $("#"+id);

    var style = $("<style type='text/css'>.td_title{font-weight:bold;width:100px}</style>")

    var step1 = $("<div></div>");
    var step2 = $("<div></div>");
    var step3 = $("<div></div>");
    var step4 = $("<div></div>");
    var button = $("<div style='margin-top:20px'></div>");

    var metadata_types;
    var contexts;
    var example_context;

    function init() {

        $.getJSON("../../import/types",function(data) {
            metadata_types = data;
        });

        $.getJSON("../../context/list",function(data) {
            loader.hide();
            contexts = data;
        });
        
        $.getJSON("../../config/data/kiwi.host",function(data) {
            example_context = data["kiwi.host"] + "context/name";
        });        
        
        container.empty();
        container.append(style);
        container.append($("<h1></h1>").append("<span>Import</span>").append(loader));
        container.append(step1);
        container.append(step2);
        container.append(step3);
        container.append(step4);
        container.append(button);

        step1.append("<h2>1. Select input source-type:</h2>");
        step1.append($("<a class='import_type'></a>").text("File").click(function(){
          button.empty();
          step4.empty();
          step3.empty();
          step2.empty();
          step2.append("<h2>2. Select file:</h2>");
          var input = $("<input type='file'>");
          step2.append(input);
          input.change(function(){
              step3.empty();
              if(input.val()==undefined || input.val()=="") alert("Select a file first!");
              else predefine(input,"file");
          });
       }));
       step1.append("<span>|</span>");
       step1.append($("<a class='import_type' ></a>").text("URL").click(function(){
          button.empty();
          step4.empty()
          step3.empty();
          step2.empty();
          step2.append("<h2>2. Define url:</h2>");
          var input = $("<input type='text' style='width: 300px'>");
          step2.append(input);
          step2.append($("<button></button>").text("ok").click(function(){
              step3.empty();
              if(input.val()==undefined || input.val()=="") alert("Define an URL first!");
              else if(!isUrl(input.val())) alert("URL is not valid!")
              else predefine(input,"url");
          }));
       }));
    }

    function predefine(input_field,source_type) {
        var source_relation;
        var source_filetype;
        var source_filetype_input;
        var context;
        var context_input;
        var context_type="default";

        var url = $("<input type='text' style='width: 300px'>");

        function waitForMetadataTypes() {
            step3.append("<h2>3. Import (..loading)</h2>");
            if(metadata_types==undefined) setTimeout(waitForMetadataTypes,1000);
            else writeTable()
        }
        waitForMetadataTypes();

        function writeTable() {
            step4.empty();
            button.empty();
            step3.empty().append("<h2>3. Import</h2>");
            checkFileType();
            var table = $("<table></table>").addClass("importer_table");

            var td_mime = $("<td></td>");

            var st = source_relation!="content" ? "style='display:none;'" :  "style='display:inline;'";
            var url_div = $("<div "+st+"></div>").append("<span> url:</span>").append(url);

            table.append($("<tr></tr>").append("<td class='td_title'>Source</td>").append("<td>"+source_type+"</td>"));
            table.append($("<tr></tr>").append("<td class='td_title'>Relation</td>").append($("<td></td>").append(
                $("<select></select>").append("<option>content</option><option>meta</option>").val(source_relation).change(function(){
                    source_relation = $(this).val();
                    createMimeTD(td_mime);
                    if(source_relation=="content") url_div.css("display","inline");
                    else url_div.css("display","none");
                })
            ).append(url_div)));
            createMimeTD(td_mime);
            table.append($("<tr></tr>").append("<td class='td_title'>Mime</td>").append(td_mime));

            step3.append(table);

            table.append($("<tr></tr>").append("<td class='td_title'>Context</td>").append($("<td></td>").append(
                $("<select></select>").append("<option>default</option><option>use existing</option><option>define new</option>").change(function(){
                    context_type = $(this).val();
                    createContexts();
                })
            )));

            var b= $("<button  style='font-weight:bold'></button>").text("Import!").click(function(){
                context = context_type=="default"?undefined:context_input.val();
                context = context==null?context=null:context;
                var _url=undefined;
                if(context!=null && !isUrl(context)) {
                    alert("context must be an url!"); return;
                }
                if(source_relation=="content") {
                    if(url.val() != "" && !isUrl(url.val())) alert("content url must be empty or valid!");
                    else if(url.val()!="") _url=url.val();
                }
                if(source_relation=="content") {
                    resource(source_type,input_field,source_relation,source_filetype_input.val(),context,_url);
                } else {
                    if(source_type=="file") {
                        upload(input_field,source_filetype_input.val(),context);
                    } else {
                        external(input_field,source_filetype_input.val(),context);
                    }
                }
            });
            button.append(b);

        }

        function createMimeTD(td) {
            if(source_relation=="meta") {
                source_filetype_input = $("<select></select>");
                for(var i in metadata_types) {
                    source_filetype_input.append("<option>"+metadata_types[i]+"</option>");
                }
            } else {
                source_filetype_input = $("<input type='text' style='width:300px'>");
            }
            td.empty().append(source_filetype_input);
            if(source_filetype)source_filetype_input.val(source_filetype);
        }

        function createContexts() {
            step4.empty();
            context=undefined;
            if(context_type=="use existing") {
                context_input = $("<select></select>");
                if(contexts.length==0) {
                    step4.append("<h2>4. Select context uri:</h2>").append("no existing context, default is used.");
                }  else {
                    for(var i in contexts) {
                        context_input.append("<option>"+contexts[i]+"</option>")
                    }
                    step4.append("<h2>4. Select context url:</h2>").append(context_input);
                }
            } else if(context_type=="define new"){
                context_input = $("<input size='60' value='" + example_context + "' />");
                step4.append("<h2>4. Defined context url:</h2>").append(context_input);
            }
        }

        function checkFileType() {
            function checkRDF() {
                var inp = input_field.val();
                var mimeType = null;
                $.ajax({
                    url:   "../../import/types",
                    data:  { 'filename': inp },
                    async: false,
                    dataType: 'json',
                    success: function(data) {
                        if(data.length > 0) {
                            mimeType = data[0];
                        }
                    }
                });
                return mimeType;
            }
            function checkFile() {
                var inp = input_field.val();
                if(/.gif$/.test(inp)) return "image/gif";
                if(/.jpg$/.test(inp)) return "image/jpg";
                if(/.png$/.test(inp)) return "image/png";
                if(/.ogv$/.test(inp)) return "video/ogg";
                if(/.mp4$/.test(inp)) return "video/mp4";
                if(/.html$/.test(inp)) return "text/html";
                if(/.txt$/.test(inp)) return "text/plain";
                if(/.pdf$/.test(inp)) return "application/x-pdf";
                else return null;
            }
            var x = checkRDF();
            if(x) {
                source_relation="meta";
                source_filetype=x;
            } else {
                x = checkFile();
                source_relation="content";
                source_filetype=x;
            }
        }
    }

    function isUrl(s) {
	    var regexp = /(file|ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/
	    return regexp.test(s);
    }

    function upload(source_filetype_input,source_filetype,context) {
       loader.show();
       LMF.importClient.upload(source_filetype_input.get(0).files[0],source_filetype,context,function(){
          alert("import was successful");
           loader.hide();
       },function(error){
          alert(error.name+": "+error.message);
           loader.hide();
      });
    }

    function external(source_filetype_input,source_filetype,context) {
      loader.show();
      LMF.importClient.uploadFromUrl(source_filetype_input.val(),source_filetype,context,function(){
          alert("upload is running; you can control the running import tasks on $LMF/core/admin/tasks.html");
           loader.hide();
      },function(error){
          alert(error.name+": "+error.message);
          loader.hide();
      })
    }

    function resource(source_type,source_filetype_input,source_relation,source_filetype,context,content)  {
        if(source_type!="file") {
            alert("import content from url is not implemented yet");return;
        }
            if(!source_filetype) {
                alert("mimetype must be defined");return;
            }
            loader.show();
            LMF.resourceClient.createResource(content,function(data){
                LMF.resourceClient.updateResourceContent(data,source_filetype_input.get(0).files[0],source_filetype,function(){
                    alert("set content of "+data);
                    loader.hide();
                },function(error){
                    loader.hide();
                    alert(error.name+": "+error.message);
                })
            },function(error){
                alert(error.name+": "+error.message);
                loader.hide();
            });
    }

    init();
}
