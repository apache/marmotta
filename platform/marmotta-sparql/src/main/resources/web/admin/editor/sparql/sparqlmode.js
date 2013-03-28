/*
 * Copyright (c) 2011 TSO Ltd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is furnished 
 * to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS 
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
CodeMirror.defineMode("sparql", function(config, parserConfig) {
  var indentUnit = config.indentUnit;


    function wordRegexp(words) {
	return new RegExp("^(?:" + words.join("|") + ")$", "i");
    }

    var operatorChars = /[*+\-<>=!&|\^\@]/;
    var ops = wordRegexp(["str", "lang", "langmatches", "datatype", "bound", "sameterm", "isiri", "isuri", "isblank", "isliteral", "union", "regex"]);
    var keywords = wordRegexp(["base", "prefix", "select", "distinct", "reduced", "construct", "describe", "ask", "from", "named", "where", "order", "limit", "offset", "filter", "optional", "graph", "by", "asc", "desc"]);

    // Used as scratch variables to communicate multiple values without
    // consing up tons of objects.
    var type, content;
    function ret(tp, style, cont) {
	type = tp; content = cont;
	return style;
    }

    function tokenBase(stream, state) {
//	alert(stream.col);
	var ch = stream.next();
	if (ch == "$" || ch == "?") {
            stream.eatWhile(/[\w\d]/);
	    if (state.stack.length > 0) state.stack.push("arg");
            return ret("variable","sp-var"); }
	else if (ch=="<") {
            stream.eatWhile(/[^\s\u00a0>]/);
            if (stream.current() == ">") stream.next();
	    if (state.stack.length > 0) state.stack.push("arg");
            return ret("uri","sp-uri");
	}
	else if (ch == '"' || ch == "'") {
	    if (state.stack.length > 0) state.stack.push("arg");
	    return chain(stream, state, tokenString(ch));
	}
	else if (/[{\(\[]/.test(ch)) {
	    state.stack.push(ch);
	    return "sp-punc";
	}
	else if (/^\]/.test(ch)) {
	    while(state.stack[state.stack.length-1]=="arg")
		state.stack.pop();
	    if (state.stack.pop()=="[")
		state.stack.push("arg");
	    // else syntax is mangled anyway
	    return "sp-punc";
	}
	else if (/^[}\)]/.test(ch)) {
	    while(state.stack[state.stack.length-1]=="arg")
		state.stack.pop();
	    state.stack.pop();
	    return "sp-punc";
	}
	else if (/^\./.test(ch)) {
	    while(state.stack[state.stack.length-1]=="arg")
		state.stack.pop();
	    return "sp-punc";
	}
	else if (/^;/.test(ch)) {
	    while(state.stack[state.stack.length-1]=="arg")
		state.stack.pop();
	    if (state.stack[state.stack.length-1]!="[") 
		state.stack.push("arg");
	    return "sp-punc";
	}
	else if (/^,/.test(ch)) {
	    while(state.stack[state.stack.length-1]=="arg")
		state.stack.pop();
	    if (state.stack[state.stack.length-1]!="[") 
		state.stack.push("arg");
	    state.stack.push("arg");
	    return "sp-punc";
	}
	else if (/^\d/.test(ch)) {
	    stream.match(/^\d*(?:\.\d*)?(?:e[+\-]?\d+)?/);
	    return ret("number", "sp-number");
	}
	else if (ch == "#") {
	    stream.skipToEnd();
            return ret("comment", "sp-comment");
	}
	else if (operatorChars.test(ch)) {
            stream.eatWhile(ch);
            return ret(stream.current(),"sp-operator");
	}
	else if (ch == ":") {
	    stream.next();
            stream.eatWhile(/[\w\d\._\-]/);
	    if (state.stack.length > 0) state.stack.push("arg");
            return "sp-prefixed";
	} else {
            stream.eatWhile(/[_\-\w\d]/);
            if (stream.peek()==":") {
		stream.next();
		stream.eatWhile(/[\w\d_\-]/);
		if (state.stack.length > 0) state.stack.push("arg");
		return "sp-prefixed";
            }
	    var word = stream.current(), type;
	    word= word.replace(/[ \n\t]/,"");

	    if (word=="a")
	    {
		type= "sp-operator"
		if (state.stack.length > 0) state.stack.push("arg");
	    }
            else if (ops.test(word))
		type = "sp-operator";
            else if (keywords.test(word))
		type = "sp-keyword";
            else
		type = "sp-word";
            return type;
	}
    }

  function tokenString(quote) {
    return function(stream, state) {
      if (!nextUntilUnescaped(stream, quote))
        state.tokenize = tokenBase;
      return ret("string", "sp-literal");
    };
  }

  function chain(stream, state, f) {
    state.tokenize = f;
    return f(stream, state);
  }

  function nextUntilUnescaped(stream, end) {
    var escaped = false, next;
    while ((next = stream.next()) != null) {
      if (next == end && !escaped)
        return false;
      escaped = !escaped && next == "\\";
    }
    return escaped;
  }


  return {

      token: tokenBase,
      
      startState: function(base) {
	  return {tokenize: tokenBase,
		  baseIndent: base,
		  stack: [] }; },

      indent: function(state, textAfter) {
	  var n = state.stack.length;

	  if (/^[\}\]\)]/.test(textAfter)) 
	  {
    	      while(state.stack[state.stack.length-1]=="arg")
		state.stack.pop();
	      n= state.stack.length-1;
	  }

	  return n * config.indentUnit;
      },

      electricChars: "}])"
  };
});

