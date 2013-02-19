(function() {
  /* TODO:
    - add new line
  */
  /* Configurator for LMF properties */
  var AddValue, BooleanProperty, Client, DataTable, EnumProperty, IntegerProperty, ListProperty, Model, Property, SolrProgramProperty, StringProperty, TextProperty, URIProperty;
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; }, __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) {
    for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; }
    function ctor() { this.constructor = child; }
    ctor.prototype = parent.prototype;
    child.prototype = new ctor;
    child.__super__ = parent.prototype;
    return child;
  };
  window.Configurator = (function() {
    function Configurator(options) {
      var onfailure, onsuccess;
      this.options = {
        url: "http://localhost:8080/",
        prefix: void 0,
        container: "configurator",
        blacklist: []
      };
      $.extend(this.options, options);
      this.client = new Client(this.options.url, this.options.prefix, this.options.blacklist);
      this.model = new Model();
      this.datatTable = new DataTable(this.options.container);
      onsuccess = __bind(function() {
        this.datatTable.draw(this.model, this.client);
        this.addValue = new AddValue(this.options.container, this.options.prefix, this.options.blacklist, this.options.url);
        return this.addValue.onAdd = function() {
          console.log(213);
          return window.location.reload();
        };
      }, this);
      onfailure = function() {
        throw "cannot load model";
      };
      this.client.load(this.model, onsuccess, onfailure);
    }
    Configurator.prototype.extend = function(options) {
      return alert("not implemented yet");
    };
    return Configurator;
  })();
  /* MODEL */
  Model = (function() {
    function Model() {
      this.properties = new Array();
    }
    Model.prototype.init = function(data) {
      var clazz, property, value, _results;
      _results = [];
      for (property in data) {
        value = data[property];
        clazz = void 0;
        if (value.type.match(/java.lang.Boolean.*/)) {
          clazz = BooleanProperty;
        } else if (value.type.match(/java.lang.Enum.*/)) {
          clazz = EnumProperty;
        } else if (value.type.match(/java.lang.Integer.*/)) {
          clazz = IntegerProperty;
        } else if (value.type.match(/java.lang.String.*/)) {
          clazz = StringProperty;
        } else if (value.type.match(/java.net.URL.*/)) {
          clazz = URIProperty;
        } else if (value.type.match(/at.newmedialab.lmf.SolrProgram.*/)) {
          clazz = SolrProgramProperty;
        } else if (value.type.match(/java.util.List.*/)) {
          clazz = ListProperty;
        } else {
          clazz = Property;
        }
        _results.push(this.properties.push(new clazz(property, value)));
      }
      return _results;
    };
    return Model;
  })();
  /* superclass of all properties */
  Property = (function() {
    function Property(key, value) {
      var mapping;
      this.key = key;
      this.view = $("<tr></tr>");
      this.options = new Array();
      this.value = value.value;
      this.comment = "";
      if (value.comment !== void 0 && value.comment !== null) {
        this.comment = value.comment;
      }
      mapping = value.type.match(/\((.+)\)/);
      if (mapping && mapping[1]) {
        this.options = mapping[1];
      }
      this.tdtitle = "<td class='config_tdtitle'><h3>" + this.key + "</h2><span>" + this.comment + "</span></td>";
    }
    Property.prototype.setValue = function(value) {
      this.value = value;
    };
    Property.prototype.getValue = function() {
      return this.value;
    };
    Property.prototype.show = function() {
      return this.view.show();
    };
    Property.prototype.hide = function() {
      return this.view.hide();
    };
    Property.prototype.hasChanged = function() {
      return false;
    };
    Property.prototype.onchange = function() {
      return false;
    };
    Property.prototype.onstorage = function() {};
    Property.prototype.draw = function() {
      return this.view.append(this.tdtitle).append("<td>" + this.value + "</td>");
    };
    Property.prototype.toString = function() {
      return this.key + " = " + this.value;
    };
    return Property;
  })();
  /* for boolean type: a checkbox */
  BooleanProperty = (function() {
    __extends(BooleanProperty, Property);
    function BooleanProperty(key, value, comment) {
      this.key = key;
      this.comment = comment;
      BooleanProperty.__super__.constructor.call(this, this.key, value, this.comment);
      this.value = this.value === "true";
      this.checkbox = $('<input type="checkbox">');
      this.checkbox.change(__bind(function() {
        return this.onchange(this);
      }, this));
    }
    BooleanProperty.prototype.hasChanged = function() {
      return this.value !== this.checkbox.is(':checked');
    };
    BooleanProperty.prototype.onstorage = function() {
      return this.value = this.checkbox.is(':checked');
    };
    BooleanProperty.prototype.getValue = function() {
      return this.checkbox.is(':checked');
    };
    BooleanProperty.prototype.draw = function() {
      if (this.value) {
        this.checkbox.attr("checked", "checked");
      }
      return this.view.append(this.tdtitle).append($("<td></td>").append(this.checkbox));
    };
    return BooleanProperty;
  })();
  /* for enum type: a selector */
  EnumProperty = (function() {
    __extends(EnumProperty, Property);
    function EnumProperty(key, value, comment) {
      var index, values;
      this.key = key;
      this.comment = comment;
      EnumProperty.__super__.constructor.call(this, this.key, value, this.comment);
      this.td = $("<td></td>");
      this.select = $("<select></select>").addClass("config_selectfield").appendTo(this.td);
      values = value.type.substring(15, value.type.length - 1).split("|");
      for (index in values) {
        value = values[index];
        this.select.append($("<option>" + value.substring(1, value.length - 1) + "</option>"));
      }
      this.select.change(__bind(function() {
        return this.onchange(this);
      }, this));
    }
    EnumProperty.prototype.hasChanged = function() {
      return this.select.val() !== this.value;
    };
    EnumProperty.prototype.getValue = function() {
      return this.select.val();
    };
    EnumProperty.prototype.onstorage = function() {
      return this.value = this.select.val();
    };
    EnumProperty.prototype.draw = function() {
      this.select.val(this.value);
      return this.view.append(this.tdtitle).append(this.td);
    };
    return EnumProperty;
  })();
  /* for integer type: an input field with validation or an input field with change buttons */
  IntegerProperty = (function() {
    __extends(IntegerProperty, Property);
    function IntegerProperty(key, value, comment) {
      var down, down_func, interval, step, up, up_func, values;
      this.key = key;
      this.comment = comment;
      IntegerProperty.__super__.constructor.call(this, this.key, value, this.comment);
      this.input = $("<input >").addClass("config_intergerfield_short");
      this.field = $("<div></div>").append(this.input);
      values = [];
      if (value.type.length > 17) {
        values = value.type.substring(18, value.type.length - 1).split("|");
      }
      step = void 0;
      this.min = void 0;
      this.max = void 0;
      if (values[0] && values[0] !== "*") {
        step = parseInt(values[0]);
      }
      if (values[1] && values[1] !== "*") {
        this.min = parseInt(values[1]);
      }
      if (values[2] && values[2] !== "*") {
        this.max = parseInt(values[2]);
      }
      if (step) {
        this.input.attr("readonly", "readonly");
        interval = void 0;
        up_func = __bind(function() {
          if (this.max !== void 0) {
            if (parseInt(this.input.val()) + step <= this.max) {
              this.input.val(parseInt(this.input.val()) + step);
              return this.onchange(this);
            }
          } else {
            this.input.val(parseInt(this.input.val()) + step);
            return this.onchange(this);
          }
        }, this);
        up = $("<button>+</button>").mousedown(__bind(function() {
          return interval = setInterval(up_func, 100);
        }, this)).mouseup(__bind(function() {
          return clearInterval(interval);
        }, this)).mouseout(__bind(function() {
          return clearInterval(interval);
        }, this));
        down_func = __bind(function() {
          if (this.min !== void 0) {
            if (parseInt(this.input.val()) - step >= this.min) {
              this.input.val(parseInt(this.input.val()) - step);
              return this.onchange(this);
            }
          } else {
            this.input.val(parseInt(this.input.val()) - step);
            return this.onchange(this);
          }
        }, this);
        down = $("<button>-</button>").mousedown(__bind(function() {
          return interval = setInterval(down_func, 100);
        }, this)).mouseup(__bind(function() {
          return clearInterval(interval);
        }, this)).mouseout(__bind(function() {
          return clearInterval(interval);
        }, this));
        this.field.append(down).append(up);
      } else {
        this.input.removeClass("config_intergerfield_short").addClass("config_intergerfield");
      }
      this.input.keydown(__bind(function() {
        return this.onchange(this);
      }, this));
    }
    IntegerProperty.prototype.hasChanged = function() {
      return parseInt(this.value) !== parseInt(this.input.val());
    };
    IntegerProperty.prototype.onstorage = function() {
      return this.value = parseInt(this.input.val());
    };
    IntegerProperty.prototype.getValue = function() {
      if (this.min && parseInt(this.input.val()) < this.min) {
        throw this.key + "is under min value";
      }
      if (this.max && parseInt(this.input.val()) > this.max) {
        throw this.key + "is under min value";
      }
      return parseInt(this.input.val());
    };
    IntegerProperty.prototype.draw = function() {
      this.input.val(parseInt(this.value));
      return this.view.append(this.tdtitle).append($("<td></td>").append(this.field));
    };
    return IntegerProperty;
  })();
  /* for string property: a input box or (for password) an edit button */
  StringProperty = (function() {
    __extends(StringProperty, Property);
    function StringProperty(key, value, comment) {
      var match;
      this.key = key;
      this.comment = comment;
      StringProperty.__super__.constructor.call(this, this.key, value, this.comment);
      this.input = $('<input>').addClass("config_inputfield");
      this.field = $("<div></div>").append(this.input);
      if (this.value instanceof Array) {
        this.value = this.value.join(",");
      }
      match = value.type.match(/".+"/);
      if (match && match[0] === "\"password\"") {
        this.input.css("display", "none");
        this.button = $("<button>edit</button>").click(__bind(function() {
          var val;
          if (prompt("insert old password", "") === this.value) {
            val = prompt("insert new password", "");
            if (val !== "") {
              if (prompt("confirm new password") === val) {
                this.input.val(val);
                return this.onchange(this);
              } else {
                return alert("password cannot be confirmed");
              }
            } else {
              return alert("password may not be empty");
            }
          } else {
            return alert("wrong password");
          }
        }, this));
        this.field.append(this.button);
      }
      this.input.keydown(__bind(function() {
        return this.onchange(this);
      }, this));
    }
    StringProperty.prototype.hasChanged = function() {
      return this.value !== this.input.val();
    };
    StringProperty.prototype.onstorage = function() {
      return this.value = this.input.val();
    };
    StringProperty.prototype.getValue = function() {
      return this.input.val();
    };
    StringProperty.prototype.draw = function() {
      this.input.val(this.value);
      return this.view.append(this.tdtitle).append($("<td></td>").append(this.field));
    };
    return StringProperty;
  })();
  /* for URI properties: input field with pattern validation */
  URIProperty = (function() {
    __extends(URIProperty, StringProperty);
    function URIProperty(key, value, comment) {
      this.key = key;
      this.comment = comment;
      URIProperty.__super__.constructor.call(this, this.key, value, this.comment);
    }
    URIProperty.prototype.getValue = function() {
      var url_pattern;
      url_pattern = /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi;
      if (this.hasChanged()) {
        if (!url_pattern.test(this.input.val())) {
          throw "" + this.key + ": " + (this.input.val()) + " is not a valid URL";
        }
      }
      return this.input.val();
    };
    return URIProperty;
  })();
  /* a textual property: textarea */
  TextProperty = (function() {
    __extends(TextProperty, Property);
    function TextProperty(key, value, comment) {
      this.key = key;
      this.comment = comment;
      TextProperty.__super__.constructor.call(this, this.key, value, this.comment);
      this.input = $('<textarea>').addClass("config_textfield");
      this.input.keydown(__bind(function() {
        return this.onchange(this);
      }, this));
    }
    TextProperty.prototype.hasChanged = function() {
      return this.value !== this.input.val();
    };
    TextProperty.prototype.onstorage = function() {
      return this.value = this.input.val();
    };
    TextProperty.prototype.getValue = function() {
      return this.input.val();
    };
    TextProperty.prototype.draw = function() {
      this.input.val(this.value);
      return this.view.append(this.tdtitle).append($("<td></td>").append(this.input));
    };
    return TextProperty;
  })();
  /* a textarea for Solr Programs (not editable) */
  SolrProgramProperty = (function() {
    __extends(SolrProgramProperty, TextProperty);
    function SolrProgramProperty(key, value, comment) {
      this.key = key;
      this.comment = comment;
      SolrProgramProperty.__super__.constructor.call(this, this.key, value, this.comment);
      this.input.addClass("config_solrprorgamfield");
      this.input.attr("readonly", "readonly");
      this.input.unbind("keydown");
      this.input.keydown(function() {
        return alert("not editable here, try configuration interface in search module");
      });
    }
    SolrProgramProperty.prototype.hasChanged = function() {
      return false;
    };
    return SolrProgramProperty;
  })();
  /* for lists: TODO */
  ListProperty = (function() {
    __extends(ListProperty, StringProperty);
    function ListProperty(key, value, comment) {
      this.key = key;
      this.comment = comment;
      ListProperty.__super__.constructor.call(this, this.key, value, this.comment);
    }
    return ListProperty;
  })();
  /* VIEW */
  AddValue = (function() {
    function AddValue(container, prefix, blacklist, url) {
      var buttons, clean, prefix_span;
      this.prefix = prefix;
      this.blacklist = blacklist;
      this.url = url;
      this.container = $("#" + container);
      this.background = $('<div style="display:none"></div>').css({
        display: 'none'
      }).addClass('config_background').appendTo('body');
      prefix_span = prefix === void 0 ? '' : '<span>' + prefix + '.</span>';
      this.popup = $("                <div id='config_popup' style='display:none'>                  <h1>Add new value</h1>                  <table>                    <tr><td>Key</td><td>" + prefix_span + "<input type='text' id='config_add_label'/></td></tr>                    <tr><td>Type</td><td><select id='config_add_type'>                      <option value='java.lang.String'>String</option>                      <option value='java.lang.Integer'>Integer</option>                      <option value='java.net.URL'>URL</option>                      <option value='java.lang.Boolean'>Boolean</option>                      <option value='java.lang.Enum'>Enum</option>                      <option value='java.util.List'>List</option>                    </select></td></tr>                    <tr><td>Parameters</td><td><input type='text' id='config_add_parameters'/></td></tr>                    <tr><td>Value</td><td><input type='text' id='config_add_value'/></td></tr>                    <tr><td>Comment</td><td><textarea cols='20' rows='3' type='text' id='config_add_comment'/></td></tr>                  </table>                </div>               ").appendTo('body');
      this.popup.find('#config_add_type').change(__bind(function() {
        var ps;
        ps = this.popup.find('#config_add_parameters');
        switch (this.popup.find('#config_add_type').val()) {
          case 'java.lang.Integer':
            return ps.val('10|0|*');
          case 'java.lang.Enum':
            return ps.val('"one 1"|"two 2"');
          default:
            return ps.val("");
        }
      }, this));
      this.button = $("<button></button>").css({
        margin: "0px auto",
        display: "block",
        marginBottom: "40px"
      }).text('Add Value').click(__bind(function() {
        return this.open();
      }, this));
      this.container.append(this.button);
      clean = __bind(function() {
        this.popup.find('#config_add_label').val('');
        this.popup.find('#config_add_parameters').val('');
        this.popup.find('#config_add_value').val('');
        return this.popup.find('#config_add_comment').val('');
      }, this);
      buttons = $("<div id='config_add_buttons'></div>").appendTo(this.popup);
      buttons.append($('<button></button>').text('cancel').click(__bind(function() {
        this.popup.hide();
        this.background.hide();
        return clean();
      }, this)));
      buttons.append($('<button></button>').text('add').click(__bind(function() {
        var comment, encodeData, inBlacklist, index, lab, label, params, type, v, value, _len, _ref;
        label = this.popup.find('#config_add_label').val();
        type = this.popup.find('#config_add_type').val();
        params = this.popup.find('#config_add_parameters').val();
        value = this.popup.find('#config_add_value').val();
        comment = this.popup.find('#config_add_comment').val();
        if (label === "") {
          alert("key may not be empty");
          return;
        }
        inBlacklist = false;
        lab = label;
        if (this.prefix !== void 0) {
          lab = prefix + "." + lab;
        }
        _ref = this.blacklist;
        for (index = 0, _len = _ref.length; index < _len; index++) {
          v = _ref[index];
          if ((lab.slice(0, v.length)) === v) {
            inBlacklist = true;
            break;
          }
        }
        if (inBlacklist) {
          if (!confirm("label will be filtered by blacklist and thus not be displayed in this configuration view. Still create?")) {
            return;
          }
        }
        if (params !== "") {
          params = '(' + params + ')';
        }
        if (prefix !== void 0) {
          label = prefix + "." + label;
        }
        if (comment !== "") {
          comment = "&comment=" + encodeURIComponent(comment);
        }
        encodeData = function(data) {
          var d;
          return d = '["' + data + '"]';
        };
        return $.ajax({
          url: this.url + "config/data/" + label + "?type=" + encodeURIComponent(type + params) + comment,
          type: "POST",
          data: encodeData(value),
          contentType: "application/json; charset=utf-8",
          success: __bind(function() {
            return this.onAdd();
          }, this)
        });
      }, this)));
    }
    AddValue.prototype.open = function() {
      this.background.show();
      return this.popup.show();
    };
    AddValue.prototype.onAdd = function() {
      return console.log("must be overwritten");
    };
    return AddValue;
  })();
  DataTable = (function() {
    function DataTable(container) {
      this.container = $("#" + container);
      this.saver = $("<div>SAVE</div>").addClass("config_saveButton").css("display", "none");
    }
    DataTable.prototype.draw = function(model, client) {
      var filter, filter_input, index, onfailure, onsave, remover, sortFunction, value, _len, _ref;
      this.container.html("<h2>Configurator:</h2>");
      this.saver.appendTo(this.container);
      sortFunction = function(a, b) {
        if (a.key < b.key) {
          return -1;
        }
        if (a.key > b.key) {
          return 1;
        }
        return 0;
      };
      model.properties.sort(sortFunction);
      this.table = $("<table></table>").addClass("config_datatable").appendTo(this.container);
      filter = __bind(function(key) {
        var index, regex, row, _len, _ref, _results;
        regex = new RegExp(".*" + key + ".*");
        _ref = model.properties;
        _results = [];
        for (index = 0, _len = _ref.length; index < _len; index++) {
          row = _ref[index];
          _results.push(row.key.match(regex) ? row.show() : row.hide());
        }
        return _results;
      }, this);
      filter_input = $("<input>").addClass("config_filterinput").keyup(function() {
        return filter(filter_input.val());
      });
      $("<tr></tr>").append($("<td style='text-align:center' colspan='3'></td>").append("<span style='font-weight:bold;'>Filter: </span>").append(filter_input)).appendTo(this.table);
      remover = function(val) {
        var button;
        button = $("<button>remove</button>").click(__bind(function() {
          return client["delete"](val.key);
        }, this));
        return $("<td></td>").append(button);
      };
      _ref = model.properties;
      for (index = 0, _len = _ref.length; index < _len; index++) {
        value = _ref[index];
        value.draw().append(remover(value)).appendTo(this.table);
        value.onchange = __bind(function() {
          return this.saver.show();
        }, this);
      }
      onsave = __bind(function() {
        var index, value, _len2, _ref2;
        _ref2 = model.properties;
        for (index = 0, _len2 = _ref2.length; index < _len2; index++) {
          value = _ref2[index];
          value.onstorage();
        }
        this.saver.hide();
        return alert("saved values");
      }, this);
      onfailure = function() {
        return alert("cannot store values");
      };
      return this.saver.click(__bind(function() {
        return client.store(model, onsave, onfailure);
      }, this));
    };
    DataTable.prototype.redraw = function(model, key) {};
    return DataTable;
  })();
  /* Controler */
  Client = (function() {
    function Client(url, prefix, blacklist) {
      this.url = url;
      this.prefix = prefix;
      this.blacklist = blacklist;
    }
    Client.prototype["delete"] = function(key) {
      if (!confirm("delete " + key)) {
        return;
      }
      return $.ajax({
        url: this.url + "config/data/" + key,
        type: "DELETE",
        success: __bind(function() {
          return window.location.reload();
        }, this)
      });
    };
    Client.prototype.load = function(model, onsuccess, onfailure) {
      var afterRequest, filter, prefix;
      filter = __bind(function(data) {
        var toDelete, value, value1, value2, _i, _j, _len, _len2, _ref;
        toDelete = [];
        for (value1 in data) {
          _ref = this.blacklist;
          for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            value2 = _ref[_i];
            if ((value1.slice(0, value2.length)) === value2) {
              toDelete.push(value1);
              break;
            }
          }
        }
        for (_j = 0, _len2 = toDelete.length; _j < _len2; _j++) {
          value = toDelete[_j];
          delete data[value];
        }
        return data;
      }, this);
      afterRequest = function(data) {
        model.init(filter(data));
        return onsuccess();
      };
      if (this.prefix) {
        prefix = "?prefix=" + this.prefix;
      } else {
        prefix = "";
      }
      return $.getJSON(this.url + "config/list" + prefix, afterRequest, onfailure);
    };
    Client.prototype.store = function(model, onsuccess, onfailure) {
      var data, index, str, value, _len, _len2, _ref;
      try {
        str = "";
        data = [];
        _ref = model.properties;
        for (index = 0, _len = _ref.length; index < _len; index++) {
          value = _ref[index];
          console.log(model.properties.length, index);
          if (value.hasChanged()) {
            data.push(value);
            str += value.key + ":" + value.getValue();
            if (index < model.properties.length - 1) {
              str += "\n";
            }
          }
        }
        if (confirm("Store values: \n" + str)) {
          str = "{";
          for (index = 0, _len2 = data.length; index < _len2; index++) {
            value = data[index];
            str += '"' + value.key + '":"' + value.getValue() + '"';
            if (index < data.length - 1) {
              str += ",";
            }
          }
          str += "}";
          return $.ajax(data = {
            type: 'POST',
            contentType: "application/json",
            url: this.url + "config/list",
            data: str,
            success: onsuccess,
            failure: onfailure
          });
        }
      } catch (e) {
        alert("cannot store content: " + e);
        if (onfailure) {
          return onfailure();
        }
      }
    };
    return Client;
  })();
}).call(this);
