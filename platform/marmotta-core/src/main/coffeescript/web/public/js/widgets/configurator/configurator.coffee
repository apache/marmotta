#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

### TODO:
  - make it extendable
###
### Configurator for LMF properties ###
class window.Configurator

  #load model and draw
  constructor:(options)->
    # merge options
    @options =
      url       : "http://localhost:8080/"
      prefix    : undefined
      container : "configurator"
      blacklist : []
    $.extend(@options,options)

    # create client, datamodel and tableview
    @client = new Client @options.url, @options.prefix, @options.blacklist
    @model = new Model()
    @datatTable = new DataTable @options.container

    onsuccess = =>
      @datatTable.draw @model,@client
      @addValue = new AddValue @options.container, @options.prefix, @options.blacklist, @options.url
      @addValue.onAdd = ->
        window.location.reload()

    onfailure = ->
      throw "cannot load model"

    # load data into client model and draw datatable afterwards
    @client.load @model,onsuccess,onfailure

  # should allow extenion with new types
  extend:(options)->
    alert "not implemented yet"


### MODEL ###
class Model

  # model is an array of properties
  constructor:->
    @properties = new Array()

  # add property for each serverside property
  init:(data)->
    for property,value of data
      clazz = undefined
      if value.type.match /java.lang.Boolean.*/ then clazz = BooleanProperty
      else if value.type.match /java.lang.Enum.*/ then clazz = EnumProperty
      else if value.type.match /java.lang.Integer.*/ then clazz = IntegerProperty
      else if value.type.match /java.lang.String.*/ then clazz = StringProperty
      else if value.type.match /java.net.URL.*/ then clazz = URIProperty
      else if value.type.match /java.util.List.*/ then clazz = ListProperty
      else if value.type.match /org.marmotta.type.Program/ then clazz = ProgramProperty
      else if value.type.match /org.marmotta.type.Text/ then clazz = TextProperty
      else clazz = Property

      @properties.push new clazz property,value

### superclass of all properties ###
class Property
  constructor:(@key,value)->
    # parse key
    @view = $("<tr></tr>")
    @options = new Array()
    @value = value.value
    @comment = ""
    if value.comment != undefined && value.comment != null
      @comment = value.comment
    mapping = value.type.match(/\((.+)\)/)
    @options = mapping[1] if mapping && mapping[1]
    @tdtitle = "<td class='config_tdtitle'><h3>#{@key}</h2><span>#{@comment}</span></td>"

  setValue:(@value)->

  getValue:->
    @value

  show:()->
    @view.show()

  hide:()->
    @view.hide()

  hasChanged:->
    false

  onchange:->
    false

  onstorage:->
    return

  # get tr for this property
  draw:->
    @view.append(@tdtitle).append("<td>#{@value}</td>")

  toString:->
    @key+" = "+@value

### for boolean type: a checkbox ###
class BooleanProperty extends Property
  constructor:(@key,value,@comment)->
    super(@key,value,@comment)
    @value = if @value instanceof String then @value == "true" else @value
    @checkbox = $('<input type="checkbox">')
    @checkbox.change =>
      @onchange @

  hasChanged:->
    @value != @checkbox.is(':checked')

  onstorage:->
     @value = @checkbox.is(':checked')

  getValue:->
    @checkbox.is(':checked')

  draw:->
    @checkbox.attr("checked","checked") if @value
    @view.append(@tdtitle).append $("<td></td>").append @checkbox

### for enum type: a selector ###
class EnumProperty extends Property
  constructor:(@key,value,@comment)->
    super(@key,value,@comment)
    @td = $("<td></td>")
    @select = $("<select></select>").addClass("config_selectfield").appendTo @td
    values = value.type.substring(15,value.type.length-1).split "|"
    for index,value of values
      @select.append $("<option>"+value.substring(1,value.length-1)+"</option>")
    @select.change ()=>
      @onchange @

  hasChanged:->
    @select.val() != @value

  getValue:->
    @select.val()

  onstorage:->
    @value = @select.val()

  draw:->
    @select.val @value
    @view.append(@tdtitle).append @td

### for integer type: an input field with validation or an input field with change buttons ###
class IntegerProperty extends Property
  constructor:(@key,value,@comment)->
    super(@key,value,@comment)
    @input = $("<input >").addClass "config_intergerfield_short"
    @field = $("<div></div>").append @input

    values = []
    if value.type.length > 17 then values = value.type.substring(18,value.type.length-1).split "|"

    step = undefined
    @min = undefined
    @max = undefined

    if values[0] && values[0]!="*" then step = parseInt(values[0])
    if values[1] && values[1]!="*" then @min = parseInt(values[1])
    if values[2] && values[2]!="*" then @max = parseInt(values[2])

    if step
      @input.attr("readonly","readonly")
      interval = undefined

      up_func = =>
        if @max != undefined
          if parseInt(@input.val())+step <= @max
            @input.val(parseInt(@input.val())+step)
            @onchange @
        else
          @input.val(parseInt(@input.val())+step)
          @onchange @


      up = $("<button>+</button>").mousedown =>
        interval = setInterval(up_func,100)
      .mouseup =>
        clearInterval interval
      .mouseout =>
        clearInterval interval

      down_func = =>
        if @min != undefined
          if parseInt(@input.val())-step >= @min
            @input.val(parseInt(@input.val())-step)
            @onchange @
        else
          @input.val(parseInt(@input.val())-step)
          @onchange @

      down = $("<button>-</button>").mousedown =>
        interval = setInterval(down_func,100)
      .mouseup =>
        clearInterval interval
      .mouseout =>
        clearInterval interval

      @field.append(down).append(up)
    else
      @input.removeClass("config_intergerfield_short").addClass("config_intergerfield")
    @input.keydown ()=>
      @onchange @

  hasChanged:->
    parseInt(@value) != parseInt(@input.val())

  onstorage:->
    @value = parseInt(@input.val())

  getValue:->
    if(@min && parseInt(@input.val()) < @min) then throw @key + "is under min value"
    if(@max && parseInt(@input.val()) > @max) then throw @key + "is under min value"
    parseInt(@input.val())

  draw:->
    @input.val(parseInt(@value))
    @view.append(@tdtitle).append $("<td></td>").append @field

### for string property: a input box or (for password) an edit button ###
class StringProperty extends Property
  constructor:(@key,value,@comment)->
    super(@key,value,@comment)
    @input = $('<input>').addClass "config_inputfield"
    @field = $("<div></div>").append @input

    # clean lists
    if @value instanceof Array
      @value = @value.join(",")

    # check for password
    match = value.type.match /".+"/
    if match and match[0] == "\"password\""
      @input.css "display","none"
      @button = $("<button>edit</button>").click =>
        if prompt("insert old password","")==@value
          val = prompt "insert new password",""
          if val != ""
            if prompt("confirm new password")==val
              @input.val val
              @onchange @
            else alert "password cannot be confirmed"
          else alert "password may not be empty"
        else alert "wrong password"
      @field.append @button

    @input.keydown =>
      @onchange @

  hasChanged:->
    @value != @input.val()

  onstorage:->
    @value = @input.val()

  getValue:->
    @input.val()

  draw:->
    @input.val(@value)
    @view.append(@tdtitle).append $("<td></td>").append @field

### for URI properties: input field with pattern validation ###
class URIProperty extends StringProperty
  constructor:(@key,value,@comment)->
    super(@key,value,@comment)

  getValue:->
    url_pattern = /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi
    if @.hasChanged()
      if !url_pattern.test(@input.val()) then throw "#{@key}: #{@input.val()} is not a valid URL"
    @input.val()

### a textual property: textarea ###
class TextProperty extends Property
  constructor:(@key,value,@comment)->
    super(@key,value,@comment)
    @input = $('<textarea>').addClass "config_textfield"
    @input.keydown =>
      @onchange @

  hasChanged:->
    @value != @input.val()

  onstorage:->
    @value = @input.val()

  getValue:->
    @input.val()

  draw:->
    @input.val(@value)
    @view.append(@tdtitle).append $("<td></td>").append @input

### a textarea for Programs (not editable) ###
class ProgramProperty extends TextProperty
  constructor:(@key,value,@comment)->
    super(@key,value,@comment)
    @input.addClass "config_solrprorgamfield"
    @input.attr "readonly","readonly"
    @input.unbind "keydown"
    @input.keydown ->
      alert "not editable here, try configuration interface for program!"

  hasChanged:->
    false

### for lists: TODO ###
class ListProperty extends StringProperty
  constructor:(@key,value,@comment)->
    super(@key,value,@comment)


### VIEW ###
class AddValue
  constructor:(container,@prefix,@blacklist,@url)->
    @container = $("#"+container)
    @background = $('<div style="display:none"></div>').css({display:'none'}).addClass('config_background').appendTo 'body'
    prefix_span = if prefix == undefined then '' else '<span>'+prefix+'.</span>'
    @popup = $("
                <div id='config_popup' style='display:none'>
                  <h1>Add new value</h1>
                  <table>
                    <tr><td>Key</td><td>#{prefix_span}<input type='text' id='config_add_label'/></td></tr>
                    <tr><td>Type</td><td><select id='config_add_type'>
                      <option value='java.lang.String'>String</option>
                      <option value='java.lang.Integer'>Integer</option>
                      <option value='java.net.URL'>URL</option>
                      <option value='java.lang.Boolean'>Boolean</option>
                      <option value='java.lang.Enum'>Enum</option>
                      <option value='java.util.List'>List</option>
                    </select></td></tr>
                    <tr><td>Parameters</td><td><input type='text' id='config_add_parameters'/></td></tr>
                    <tr><td>Value</td><td><input type='text' id='config_add_value'/></td></tr>
                    <tr><td>Comment</td><td><textarea cols='20' rows='3' type='text' id='config_add_comment'/></td></tr>
                  </table>
                </div>
               ").appendTo 'body'
    @popup.find('#config_add_type').change =>
      ps = @popup.find('#config_add_parameters')
      switch @popup.find('#config_add_type').val()
        when 'java.lang.Integer' then ps.val('10|0|*')
        when 'java.lang.Enum' then ps.val('"one 1"|"two 2"')
        else ps.val("")

    @button = $("<button></button>").css({margin:"0px auto",display:"block",marginBottom:"40px"}).text('Add Value').click =>
      @open()

    @container.append @button

    clean = =>
      @popup.find('#config_add_label').val ''
      @popup.find('#config_add_parameters').val ''
      @popup.find('#config_add_value').val ''
      @popup.find('#config_add_comment').val ''

    buttons = $("<div id='config_add_buttons'></div>").appendTo @popup

    buttons.append $('<button></button>').text('cancel').click =>
      @popup.hide()
      @background.hide()
      clean()

    buttons.append $('<button></button>').text('add').click =>
      # store data
      label = @popup.find('#config_add_label').val()
      type = @popup.find('#config_add_type').val()
      params = @popup.find('#config_add_parameters').val()
      value = @popup.find('#config_add_value').val()
      comment = @popup.find('#config_add_comment').val()

      if label == ""
        alert "key may not be empty"
        return

      inBlacklist = false
      lab = label
      if @prefix != undefined then lab = prefix + "." + lab

      for v,index in @blacklist
        if (lab.slice 0, v.length) == v
          inBlacklist = true
          break

      if inBlacklist
        if !confirm("label will be filtered by blacklist and thus not be displayed in this configuration view. Still create?")
          return

      if params != "" then params = '('+params+')'

      if prefix != undefined then label = prefix+"."+label

      if comment != "" then comment = "&comment="+encodeURIComponent(comment)

      encodeData = (data)->
        d = '["'+data+'"]'

      $.ajax
        url:@url+"config/data/"+label+"?type="+encodeURIComponent(type+params)+comment,
        type:"POST",
        data:encodeData(value),
        contentType:"application/json; charset=utf-8",
        success: =>
          @onAdd()


  open:->
    @background.show()
    # open window
    @popup.show()

  onAdd:->
    console.log "must be overwritten"

class DataTable

  constructor:(container)->
    @container = $("#"+container)
    @saver = $("<div>SAVE</div>").addClass("config_saveButton").css("display","none");

  draw:(model,client)->
    @container.html "<h2>Configurator:</h2>"
    @saver.appendTo @container
    #sort
    sortFunction = (a,b)->
      return -1 if a.key < b.key
      return 1 if a.key > b.key
      0
    model.properties.sort sortFunction

    @table = $("<table></table>").addClass("config_datatable").appendTo @container

    filter = (key)=>
      regex = new RegExp ".*"+key+".*"
      for row,index in model.properties
        if row.key.match regex
          row.show()
        else
          row.hide()

    filter_input = $("<input>").addClass("config_filterinput").keyup ()->
      filter filter_input.val()

    $("<tr></tr>").append($("<td style='text-align:center' colspan='3'></td>").append("<span style='font-weight:bold;'>Filter: </span>").append(filter_input)).appendTo(@table)

    remover = (val)->
      button = $("<button>remove</button>").click =>
        client.delete val.key

      $("<td></td>").append button

    for value,index in model.properties
      value.draw().append(remover(value)).appendTo @table
      value.onchange = ()=>
        @saver.show()

    # saver
    onsave = ()=>
      for value,index in model.properties
        value.onstorage()
      @saver.hide()
      alert "saved values"

    onfailure = ()->
      alert "cannot store values"

    @saver.click ()=>
      client.store model,onsave,onfailure

  redraw:(model,key)->
    # TODO

### Controler ###
class Client

  constructor:(@url,@prefix,@blacklist)->

  delete:(key)->

    if(!confirm("delete "+key)) then return

    $.ajax
      url:@url+"config/data/"+key,
      type:"DELETE",
      success: =>
        window.location.reload()

  load:(model,onsuccess,onfailure)->

    filter = (data)=>
      toDelete = []
      for value1 of data
        for value2 in @blacklist
          if (value1.slice 0,value2.length) == value2
            toDelete.push value1
            break
      for value in toDelete
        delete data[value]
      data

    afterRequest = (data)->
      model.init filter data
      onsuccess()

    if @prefix then prefix = "?prefix=#{@prefix}" else prefix = ""
    $.getJSON @url + "config/list" + prefix,afterRequest,onfailure

  store:(model,onsuccess,onfailure)->
    try
      str = ""
      data = []
      for value,index in model.properties
        if value.hasChanged()
          data.push(value)
          str += value.key + ":" + value.getValue()
          if index < model.properties.length-1
            str += "\n"

      if( confirm("Store values: \n"+str) )

        str = "{"
        for value,index in data
          v = '"'+value.getValue()+'"'
          if ((typeof value.getValue() != "boolean") && (value.getValue().split(",").length > 1))
            x = value.getValue().split(",")
            v = "["
            for val,i in x
              v += '"'+val+'"'
              if i < x.length-1
                v += ','

            v += "]"
          str += '"'+value.key + '":'+v
          str += "," if index < data.length-1
        str += "}"
        console.log str
        $.ajax data =
          type : 'POST'
          contentType: "application/json"
          url : @url + "config/list"
          data : str
          success : onsuccess
          failure : onfailure

    catch e
      alert "cannot store content: "+e
      if onfailure then onfailure()
