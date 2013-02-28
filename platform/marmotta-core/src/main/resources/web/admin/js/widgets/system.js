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
/* QUARTZ CONFIGURATION */
$(function() {
    var root = _SERVER_URL;
    var service = "statistics/";
    var vcs = "http://code.google.com/p/lmf/" 

    var auto_refresh_interval = ($("input#refresh_interval").val() || 5) * 1000;
    
    var pollerU;
    var refresh = function() {
        clearTimeout(pollerU);
        $("#refresh_box").css({"marginTop":"4px"}).addClass("updating");

        var tBody = $("tbody", document.getElementById("stats"));
        var jl = $(document.getElementById("system_stats_jump"));
        $.getJSON(root + service + "list", function(data, status, xhr) {
            $("tr", tBody).addClass("toRemove");
            $("a", jl).addClass("toRemove");
            $.each(data, function(module, stats) {
                var modKey = "mod_" + module.replace(/ /g, "_");
                var mRow = $(document.getElementById(modKey));
                if (mRow.size() == 0) {
                    mRow = $("<tr>", {"id": modKey, 'class': 'subheading'});
                    mRow.append($("<th>", {colspan:4, 'class': 'module'}).text(module));
                    mRow.append($("<th>").append($("<a>",{ 'class': 'stat_status', click: function() {
                        var status = $(this).hasClass("on");
                        $.ajax(root + service + encodeURI(module) + "/enabled?value=" + (!status), {
                            type: 'PUT'
                        }).complete(refresh);
                    }})));
                    
                    mRow.appendTo(tBody);
                    jl.append($("<a>", {href: '#'+modKey, text: "[" + module + "]"}));
                }
                mRow.removeClass("toRemove");
                $('a[href="#' + modKey + '"]', jl).removeClass("toRemove");
                
                $("a.stat_status", mRow).removeClass("on").attr('title', "Activate Statistics Module");
                var pointer = mRow;
                for(var key in stats) {
                    var val = stats[key];
                    var statKey = modKey + "." + key.replace(/ /g, "_");
                    
                    var row = $(document.getElementById(statKey));
                    if (row.size() == 0) {
                        row = $("<tr>", {"id" : statKey});
                        row.append($("<td/>"));
                        row.append($("<td>", { 'class': 'key', colspan: 2}).text(key));
                        row.append($("<td>", { 'class': 'val' }).text(val || ""));
                        row.append($("<td class='action' />"));
                        
                        row.insertAfter(pointer);
                    } else {
                        $("td.key", row).text(key);
                        $("td.val", row).text(val || "");
                    }
                    
                    row.removeClass("toRemove");
                    $("a.stat_status", mRow).addClass("on").attr('title', "Dectivate Statistics Module");
                    pointer = row;
                }
            });
            
            $("tr.toRemove", tBody).slideUp("slow", function() { $(this).remove(); });
            $("a.toRemove", jl).hide("slow", function() { $(this).remove(); });
            $('#messages div.error').slideUp('slow', function() { $(this).remove(); });
        }).error(function() {
            var t = $('#messages div.error'); 
            if (t.size() == 0) {
                $('#messages').append($("<div>", {'class':'error'}).text("Statistic update failed!"));
            } else {
                t.slideDown();
            }
        }).complete(function() {
            $("#refresh_box").removeClass("updating");
            if ($("input#refresh_auto").prop('checked')) {
                pollerU = setTimeout(refresh, auto_refresh_interval);
            }
        });
        
    };
    var fetchBuildInfo = function() {
        $.getJSON(root + "modules/buildinfo",function(data, status, xhr) {
            function createRow(col1, col2) {
                var row = $("<tr>");
                if (typeof col1 == 'string')
                    row.append($("<td>", {}).text(col1));
                else
                    row.append($("<td>", {}).append(col1));
                if (typeof col2 == 'string')
                    row.append($("<td>", {}).text(col2));
                else
                    row.append($("<td>", {}).append(col2));

                return row;
            }
            var tab = $("<table style='margin-top:10px;margin-bottom:20px' class='simple_table'>").hide();
            var sl = $(document.getElementById("system_mod_jump"));
            $.each(data, function(modTitle, info) {
                var mod_id;
                if (info !== null) mod_id = "mod_" + info.id;
                else mod_id = modTitle.toLowerCase().replace(/[.#\s]/, "_");
                
                $("<tr>", {id: mod_id ,'class': 'subheading'}).append($("<th>", {colspan: 2}).text(modTitle)).appendTo(tab);
                sl.append($("<a>", { href: "#"+mod_id, text: "[" + modTitle + "]"})).append(" ");
                
                if (!info) {
                    // No build-info for this module
                    $("<tr>").append($("<td>", { colspan: 2}).html("<i>No build-data available...</i>")).appendTo(tab);
                    return true;
                }
                if (info.admin)
                    createRow("Module ID", $("<a>", {href: root.replace(/\/$/, "") + info.admin}).text(info.id)).appendTo(tab);
                else
                    createRow("Module ID", info.id).appendTo(tab);
                createRow("Version", info.version).appendTo(tab);
                createRow("Build Date", info.timestamp).appendTo(tab);
                createRow("Build Revision", info.revNumber).appendTo(tab);
                if (info.id.search(/^lmf-/) == 0)
                    createRow("Revision ID", $("<a>", {href: vcs + "source/browse/?r=" + info.revHash + "#hg/" + info.id, target: '_blank'}).text(info.revHash)).appendTo(tab);
                else
                    createRow("Revision ID", info.revHash).appendTo(tab);
                createRow("Build User", info.user).appendTo(tab);
                createRow("Build Host", info.host).appendTo(tab);
                createRow("Build OS", info.os).appendTo(tab);
            });
            $(document.getElementById("build_info")).empty().append(tab);
            tab.slideDown("slow");
        });
    };
    
    
    
    $("button#refresh_now").click(refresh);
    $("input#refresh_interval").change(function(event) {
        if ($(this).val() * 1000 > 0) {
            var oVal = auto_refresh_interval;
            auto_refresh_interval = $(this).val() * 1000;
            if (oVal > auto_refresh_interval && $("input#refresh_auto").prop('checked')) {
                refresh();          
            }   
        } else {
            $(this).val(auto_refresh_interval / 1000);
        }
    });
    $("input#refresh_auto").change(function(event) {
        if (this.checked) {
            $("button#refresh_now").attr('disabled','disabled')
            pollerU = setTimeout(refresh, auto_refresh_interval);
        } else {
            $("button#refresh_now").removeAttr('disabled');
            clearTimeout(pollerU);
        }
    });
    fetchBuildInfo();
    refresh();

});
