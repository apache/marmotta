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
function Importer(host) {

    var LMF = new MarmottaClient(host);
    var loader = $(".loading");
    var notice = $("#notice");
    var step2 = $("#step2");
    var droparea = $('#droparea');

    var metadata_types;
    var contexts;
    var example_context;

    function init() {
        loader.hide();
        step2.hide();

        /**
         * Inits Import from file
         * shows right path in input field
         */
        $('input[type=file]').change(function() {
          var path = $(this).val().split('\\');
          path = path[path.length-1];
          $('#fileupload').val(path);

          predefine($(this).get(0).files[0], "file");
        });

        /**
         * Inits Import from file per Drag and Drop
         */
        droparea.on('dragover', function() {
          droparea.addClass('hover');
          return false;
        });
        droparea.on('dragleave', function() {
          droparea.removeClass('hover');
          return false;
        });
        droparea.on('drop', function(e) {
          //prevent browser from open the file when drop off
          e.stopPropagation();
          e.preventDefault();
          droparea.removeClass('hover');

          //retrieve uploaded files data
          var files = e.originalEvent.dataTransfer.files;
          // processFiles(files);
          var file = files[0];
          $('#fileupload').val(file.name);
          predefine(file, "file");

          return false;
        });

        /**
         * Inits Import from URL
         * with validations: empty or invalid
         */
        $('#urlimport button').click(function() {
          var group = $('#urlimport');
          var input = $('#urlimport input');
          var error = $('#urlimport .help-inline');
          var value = input.val();
          group.removeClass('error');
          if(value === undefined || value === '' || !isUrl(value)) {
            group.addClass('error');
            error.text(error.data('invalid'));
            return;
          }

          predefine(value, "url");
        });

        $.getJSON("../../import/types", function(data) {
          metadata_types = data;
        });

        $.getJSON("../../context/list", function(data) {
          contexts = data;
        });

        $.getJSON("../../config/data/kiwi.host", function(data) {
          example_context = data["kiwi.host"] + "context/name";
        });

    }

    function predefine(file_or_url, source_type) {
      step2.show();
      droparea.addClass('upload'); // makes it smaller
      var source_relation;
      var source_filetype;
      var source_filetype_input = $('#step2 select[name="mime"]');
      var contenturl = $('#step2 #contenturl');
      var context;
      var context_input = $('#step2 select[name="context"]');
      var context_type = context_input.val();

      function waitForMetadataTypes() {
        if (metadata_types === undefined) {
          loader.show();
          setTimeout(waitForMetadataTypes, 1000);
        } else {
          loader.hide();
          writeTable();
        }
      }
      waitForMetadataTypes();

      function writeTable() {
        checkFileType();

        // only show 'meta or content' if
        if(source_relation === 'content') {
          $('#step2 #relations').removeClass('hidden');
          $('#step2 #relation'+source_relation).attr('checked', true);
        }
        $('#step2 #relations input[type="radio"]').change(function () {
          if ($(this).attr('checked')) {
            source_relation = $(this).val();
            createMimeTD();
            (source_relation === 'content') ? contenturl.show() : contenturl.hide();
            return;
          }
        });

        createMimeTD();

        context_input.change(function() {
          context_type = $(this).val();
          createContexts();
        });

        /**
         * Starts import
         */
        $('form.import').submit(function(e) {
          e.preventDefault();
          context = context_type === 'default' ? undefined : context_input.val();
          var _url = undefined;

          if(context_type === 'define new' && (context === '' || !isUrl(context))) {
            var error = $('#step2 #contexturl span.help-inline');
            error.text(error.data('invalid'));
            return;
          }

          if(source_relation === 'content') {
            var value = contenturl.val();
            // can be empty or has to be url
            if(value !== '' && !isUrl(value)) {
              var error = $('#step2 #relations span.help-inline');
              error.text(error.data('error'));
            } else if(value !== '') {
              _url = value;
            }

            resource(source_type, file_or_url, source_relation, source_filetype_input.val(), context, _url);

          } else {
              if(source_type === 'file') {
                upload(file_or_url, source_filetype_input.val(), context);
              } else {
                external(file_or_url, source_filetype_input.val(), context);
              }
          }
          return false;
        });

      }

      function createMimeTD() {
        if(source_relation === 'meta') {
          for(var i in metadata_types) {
            source_filetype_input.append("<option>"+metadata_types[i]+"</option>");
          }
        } else {
          var changed_input = $('#step2 #filemime');
          source_filetype_input.replaceWith(changed_input);
          source_filetype_input = changed_input;
          source_filetype_input.removeClass('hidden');
        }
        if(source_filetype) {
          source_filetype_input.val(source_filetype);
        }
      }

      function createContexts() {
        context = undefined;
        if(context_type === 'use existing') {
          $('#step2 #contexturl').addClass('hidden');
          $('#step2 #existingcontexts').removeClass('hidden');
          context_input = $("#step2 #existingcontexts select");
          if(contexts.length === 0) {
            var error = $('#step2 #existingcontexts span.help-inline');
            error.text(error.data('nocontexts'));
          }  else {
            for(var i in contexts) {
              context_input.append("<option>"+contexts[i]+"</option>")
            }
          }
        } else if(context_type === 'define new') {
          $('#step2 #existingcontexts').addClass('hidden');
          $('#step2 #contexturl').removeClass('hidden');
          context_input = $('#step2 #contexturl input');
          context_input.val(example_context);
        }
      }

      function checkFileType() {
        var name = file_or_url.name === undefined ? file_or_url : file_or_url.name;
          function checkRDF() {
              var mimeType = null;
              $.ajax({
                  url:   "../../import/types",
                  data:  { 'filename': name },
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
              if(/.gif$/.test(name)) return "image/gif";
              if(/.jpg$/.test(name)) return "image/jpg";
              if(/.png$/.test(name)) return "image/png";
              if(/.ogv$/.test(name)) return "video/ogg";
              if(/.mp4$/.test(name)) return "video/mp4";
              if(/.html$/.test(name)) return "text/html";
              if(/.txt$/.test(name)) return "text/plain";
              if(/.pdf$/.test(name)) return "application/x-pdf";
              else return null;
          }
          var x = checkRDF();
          if(x) {
              source_relation = 'meta';
              source_filetype = x;
          } else {
              x = checkFile();
              source_relation = 'content';
              source_filetype = x;
          }
      }
    }

    function isUrl(s) {
      var regexp = /(file|ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/
      return regexp.test(s);
    }

    function upload(source_file, source_filetype, context) {
       loader.show();
       LMF.importClient.upload(source_file, source_filetype, context, function() {
        showNotice('success', notice.data('success'));
       }, function(error) {
        showNotice('error', error.name+": "+error.message);
      });
    }

    function external(source_url, source_filetype, context) {
      loader.show();
      LMF.importClient.uploadFromUrl(source_url, source_filetype, context, function() {
        showNotice('success', notice.data('externalsuccess'));
      }, function(error) {
        showNotice('error', error.name+": "+error.message);
      });
    }

    function resource(source_type, source, source_relation, source_filetype, context, content)  {
        if(source_type !== "file") {
          showNotice('error', 'import content from url is not implemented yet');
          return;
        }
        if(!source_filetype) {
          showNotice('error', 'mimetype must be defined');
          return;
        }
        loader.show();
        LMF.resourceClient.createResource(content, function(data) {
            LMF.resourceClient.updateResourceContent(data, source, source_filetype, function() {
              showNotice('success', 'set content of '+data);
            },function(error){
              showNotice('error', error.name+": "+error.message);
            })
        },function(error){
          showNotice('error', error.name+": "+error.message);
        });
    }

    function showNotice(type, text) {
      notice.removeClass('hidden', 'alert-success', 'alert-error');
      notice.addClass('alert-'+type);
      notice.html(text);
      loader.hide();
    }

    init();
}
