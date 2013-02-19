/*
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function( $ ){


    if(!Object.keys) Object.keys = function(o){
        if (o !== Object(o)) throw new TypeError('Object.keys called on non-object');
        var ret=[],p;
        for(p in o) if(Object.prototype.hasOwnProperty.call(o,p)) ret.push(p);
        return ret;
    }

    $.fn.lmf_configurator = function(options) {
        var settings = {
	        host: 'http://localhost:8080/LMF/',
            anker_name: 'generic',
            title: 'Core Properties <span style="font-size:12px">(parameter name MUST start with \'kiwi.\')</small>',
            loading_img: '../img/ajax-loader_small.gif',//TODO is blacklist necessary/correct set?
            blacklist_keys:['solr.cores.enabled','solr.cores.available','solr.conf.program','solr.conf.program.*'],
            toplink:true,
            prefix:'solr'
        }

        var helper = {
            clean_table:function(){
                table.html("");
                table.append("<tr class='title'><td width='50%'>Parameter</td><td>Value</td></tr>");
            },
            contains:function(a, obj){
              for(var i = 0; i < a.length; i++) {
                var last = a[i].substring(a[i].length-1);
                if(last == "*") {
                    if(obj.indexOf(a[i].substring(0,a[i].length-1))==0) return true;
                }
                if(a[i] === obj){
                  return true;
                }
              }
              return false;
            }
        }

        //jquery elements
        var table;
        var added=[];

        //data
        var data;

        var load = function() {
            //show loader
            $(".lmf_configurator_loader").show();
            //to write data
            var write = function(d) {
                //sort data
                data = d;
                var sorted = [];
                for (property in d) {
                    sorted.push(property);
                }
                sorted.sort();
                for(var i=0; i<sorted.length; i++) {
                    if(!helper.contains(settings.blacklist_keys,sorted[i])) {
                        //write line
                        var input="";
                        if(data[sorted[i]].comment!=null) {
                            input="<br><span class='small_text'>"+data[sorted[i]].comment+"</span>";
                        }
                        var tr = $("<tr class='lmf_configurator_rows'><td width='50%'><span style='font-weight:bold;'>"+sorted[i]+"</span>"+input+"</td><td valign='top'><input type='tex' style='width:100%' value='"+data[sorted[i]].value+"'/></td>");
                        table.append(tr);
                    }
                }
                //hide loader
                $(".lmf_configurator_loader").hide();
            };
            //load data
            var appendix = "";
            if(settings.prefix != '') {
            	appendix="?prefix="+settings.prefix;
            }
            var url = settings.host+"config/list"+appendix;

            $.getJSON(url, function(d) {
                //var d = {"a.value":{"value":"one"},"b.value":{"value":"two"}};
                write(d);
            });
        }

        /*** actions ***/
        var button_actions =  {
            reload: function() {
                helper.clean_table();
                added=[];
                load();
            },

            save: function() {
                var check = function() {
                    //check which data was changed
                    var changed = {};//'{"a.test.key1":"value","a.test.key2":"value2"}';
                    $(".lmf_configurator_rows").each(function(){
                        var key = $(this).find("td:nth-child(1) span:nth-child(1)").text();
                        var old_value = data[key].value;
                        var new_value = $(this).find("td:nth-child(2) input").val();
                        if(old_value != new_value) {
                            changed[key] = new_value;
                        }
                    });
                    for(property in added){
                        var tr = added[property];
                        var key =  tr.find("td:nth-child(1) input").val();  console.log(key);
                        if(key!=null && key!="") {
                            var value =  tr.find("td:nth-child(2) input").val();
                            changed[key] = value;
                        }
                    };
                    return changed;
                }
                var changed = check();
                var changesString = function(d) {
                    var s = "";
                    for(property in d) {
                        s=s+property+" : "+d[property]+"\n";
                    }
                    return s;
                };
                var changesJson = function(d) {
                    var length = Object.keys(d).length;
                    var i=0;
                    var s = "{";
                    for(property in d) {
                        s=s+'"'+property+'":"'+d[property]+'"';
                        i++;
                        if(i<length) s=s+",";
                    }
                    return s+"}";
                };
                var isEmpty = function(obj) {
                    for(var prop in obj) {
                        return false;
                    }
                    return true;
                };
                if(isEmpty(changed)){alert("No changes to save!"); return}
                if(!confirm("Save all changes below?\n"+changesString(changed))) return;
                var url = settings.host+"config/list";
                var send_data = changesJson(changed);
                $.ajax({
                    type:"POST",
                    contentType: "application/json",
                    url: url,
                    data: send_data,
                    success: function(result) {
                        button_actions.reload();
                    },
                    error: function(jXHR) {
                        //should not happen
                        alert(jXHR.statusText);
                    }
                });
            },

            add: function() {
                var tr = $("<tr><td width='50%' valign='top'><input type='text' style='width:100%'></td><td valign='top'><input type='text' style='width:100%' /></td></tr>");
                added.push(tr);
                table.append(tr);
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
            var top="";
            if(settings.toplink)top='<a href="#" target="_top" style="position:absolute;right:0;font-size:12px;top:5px;text-decoration:none;">top</a>';
            //build basic view elements
            var title = $("<h3 style='position:relative;margin-bottom:10px;'><a style='text-decoration:none' name='"+settings.anker_name+"'>"+settings.title+"</a><span style='margin-left:10px;display:none;' class='tiny_text,lmf_configurator_loader'><img src='"+settings.loading_img+"' alt='loading...'></span>"+top+"</h3>");
            table = $("<table style='border-collapse: collapse;'></table>");
            helper.clean_table();
            var buttons = $("<div style='width:100%;text-align:center;padding-top:10px;margin-bottom:30px;'></div>");
            var b1 = $("<button>Reload</button>").click(function(){if(confirm("Discard all changes?"))button_actions.reload()});
            var b2 = $("<button style='margin: 0 10px;'>Add Line</button>").click(function(){button_actions.add()});
            var b3 = $("<button>Save</button>").click(function(){button_actions.save()});
            buttons.append(b1);
            buttons.append(b2);
            buttons.append(b3);

            //append elements
            $(this).html("");
            $(this).append(title);
            $(this).append(table);
            $(this).append(buttons);

            button_actions.reload();
        });
    };
})( jQuery );

