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
if (CodeMirror && CodeMirror.defineMode) {
CodeMirror.defineMode("skwrl", function(config, parserConfig) {
    var token = {
        COMMENT: "comment",
        KWD: "keyword",
        IDENT: "atom",
        OP: "operator",
        BRACKET: "bracket",
        URL: "link",
        VAR: "variable-3",
        PREFIX: "qualifier",
        DEF: "variable",
        WARNING: "string-2",
        ERROR: "error"
    },
        predefinedNamespaces = parserConfig.namespaces || {},
        baseURL = parserConfig.baseURL || null;
    
    function log(stream, status, result) {
        return;
        if (console && console.log) {
            console.log(stream.current() + " := " + result + " (-> " + status.current() + ")");
        }
    }
    
    function getInitialState() {
        return {
            tmp: {},
            namespaces: {},
            predefinedNamespaces: predefinedNamespaces,
            lmfBaseURL: baseURL,
            stack: ['default'],
            /* STACKING */
            push: function(next) {
                this.stack.unshift(next);
            },
            pop: function() {
                if (this.stack.length <= 1) {
                    return this.current();
                } else {
                    return this.stack.shift();
                }
            },
            current: function() {
                return this.stack[0];
            },
            reset: function() {
                this.stack = ['default'];
                this.tmp = {};
            },
            height: function() {
                return this.stack.length;
            },
            /* PARSING */
            parser: function(stream, state) {
                var parser = parsers[this.current()] || parsers['default'];
                return parser(stream, state);
            },
            /* NAMESPACES */
            addPrefix: function(prefix, namespace) {
                if (prefix && namespace)
                    this.namespaces[prefix] = namespace;
            },
            getNamespace: function(prefix) {
                return this.namespaces[prefix] || this.predefinedNamespaces[prefix];
            }
        };
    }

    function tokenError(stream, state) {
        if (state.current() !== 'error') state.push('error');
        stream.skipToEnd();
        return token.ERROR;
    }
    
    function tokenDefault(stream, state) {
        // @...
        var kw = stream.match(/^@(\w+)/, true);
        if (kw) {
            if (kw[1] == "prefix") {
                state.push(kw[1]);
                return token.KWD;
            } else return token.ERROR;
        }

        // <URL>
        if (stream.eat('<')) {
            state.push('url');
            return token.BRACKET;
        }
               
        if (stream.eat('(')) {
            state.tmp['rule'] = 0;
            state.push('rule');
            return token.BRACKET;
        }
        if (stream.eat(')')) {
            if (state.current() != 'rule') return token.ERROR;
            state.pop();
            return token.BRACKET;
        }
        if (stream.eat(/[)(]/)) {
            return token.BRACKET;
        }

        // prefix:label
        if (stream.match(/^\w+:\w*/, false)) {
            stream.skipTo(":")
            if (state.current() == 'prefix') {
                state.tmp["prefix"] = stream.current();
                return token.PREFIX;
            } else if (state.current() == 'default') {
                return token.DEF;
            } else {
                px = stream.current();
                stream.eat(':');
                if (state.getNamespace(px))
                    return token.PREFIX;
                else return token.WARNING;
            }
        }

        if (stream.match("->", true)) {
            return token.DEF;
        }
        // OPERATORS
        if (stream.eat(/[:,]/)) {
            return token.OP;
        }

       
        // IDENT
        if (state.current() == "prefix") {

        } else {
            if (stream.match(/^[\w.-]+/, true)) {
                return token.IDENT;
            }
        }

        return tokenError(stream, state);
    }
    
    function tokenRule(stream, state) {
        if (stream.eat(')')) {
            state.pop();
            if (state.tmp['rule'] != 3) return token.ERROR;
            return token.BRACKET;
        } else if (state.tmp['rule'] >= 3) {
            state.tmp['rule'] = 4;
            stream.skipTo(')') || stream.next();
            return token.ERROR;
        } else
        // <URL>
        if (stream.eat('<')) {
            state.push('url');
            return token.BRACKET;
        } else if (stream.eat('>')) {
            state.tmp['rule']++;
            return token.BRACKET;
        } else        
        // $s
        if (stream.match(/^\$\w+/)) {
            state.tmp['rule']++;
            return token.VAR;
        } else
        // ns:local
        if (stream.match(/^\w+:\w*/, false)) {
            stream.skipTo(':');
            var px = stream.current();
            stream.eat(':');
            if (!stream.match(/^\w+/, false)) {
                return token.ERROR;
            } else if (state.getNamespace(px))
                return token.PREFIX;
            else return token.WARNING;
        } else if (stream.match(/^\w+/)) {
            state.tmp['rule']++;
            return token.IDENT;
        } else if (stream.eat(')')) {
            state.pop();
            if (state.tmp['rule'] != 3) return token.ERROR;
            return token.BRACKET;
        } else {
            stream.next();
            return token.ERROR;
        }

        return tokenError(stream, state);
    }
    
    function tokenPrefix(stream, state) {
        if (stream.match(/^\w+/, true)) {
            state.tmp["prefix"] = stream.current();
            return token.PREFIX;
        } else if (stream.eat(':')) {
            return token.OP;
        } else
        // <URL>
        if (stream.eat('<')) {
            state.push('url');
            return token.BRACKET;
        } else if (stream.eat('>')) {
            if (state.tmp['prefix'] && state.tmp['ns']) {
                state.addPrefix(state.tmp['prefix'], state.tmp['ns']);
                state.reset();
                // End of prefix state:
                state.pop();
                return token.BRACKET;
            } else return token.ERROR;
        }
        
        return tokenError(stream, state);
    }

    function tokenURL(stream, state) {
        if (stream.skipTo('>')) {
            state.pop();
            var url = stream.current();
            if (url.search(/\s/) >= 0) {
                return token.ERROR;
            }
            if (state.stack.indexOf("prefix") >= 0) {
                state.tmp["ns"] = url;
            }
            return token.URL;
        }
        return tokenError(stream, state);
    }

    var parsers = {
        'default': tokenDefault,
        rule: tokenRule,
        prefix: tokenPrefix,
        url: tokenURL,
        error: tokenError
    };
    


    return {
        startState: getInitialState,
        compareStates: function(state1, state2) {
            return state1.stack == state2.stack && state1.namespaces == state2.namespaces;
        },
        token: function(stream, state) {
            // parse comments
            if (state.current() == "comment") {
                stream.skipTo('*') || stream.skipToEnd();
                if (stream.match('*/')) {
                    state.pop();
                } else stream.eat('*');
                return token.COMMENT;
            } else if (stream.match('/*')) {
                state.tmp.commentStart = stream.column();
                state.push("comment");
                return this.token(stream, state);
            }
            // ignore spaces
            if (stream.eatSpace()) return null;
            // ; starts parsing from scratch
            /*
            if (stream.eat(';')) {
                if (state.current() == "prefix") {
                    state.addPrefix(state.tmp['prefix'], state.tmp['ns']); 
                }
                log(stream, state, "RESET");
                state.reset(); 
                return token.OP;
            }
            */
            var result = state.parser(stream, state);
            log(stream, state, result);
            return result;
        },
        electricChars: "/@=[];",
        indent: function(state, textAfter) {
            switch (state.current()) {
            case 'comment':
                return state.tmp.commentStart +(textAfter.search(/^\s*\*\//)==0?1:3);
                break;
            case 'default':
                // no indent for @prefix etc...
                if (textAfter.search(/^\s*@/) == 0) {
                    return 0;
                }
                return config.indentUnit;
                break;
            }
            return 0;
        }
    }
});

// Autocompletion
if (CodeMirror.showHint && jQuery) {
    function completePrefix(editor, cur, token) {
        var line = editor.getLine(cur.line);
        var match = line.match(/(^|>)\s*@prefix\s+(\w+)\s*(:\s*<?)?$/);
        if (match && match[2] && match[2] !== "") {
            var prefix = match[2], result;
            try {
//                jQuery.ajax(token.state.lmfBaseURL + "ldpath/util/prefix", {
                jQuery.ajax("http://prefix.cc/" + prefix + ".file.json", {
                    async: false,
                    data: {prefix: prefix},
                    success: function(data) {
                        result = data[prefix];
                    },
                    dataType: "json"
                });
            } catch (e) {}
            if (result !== undefined) {
                var pfx = line.substr(0,cur.ch);
                var st = pfx.search(/\s*(:\s*<?)?$/);
                return {
                    list: [ ": <"+result+">" ],
                    from: {line: cur.line, ch: st},
                    to: cur
                };
            }
        }
        return false;
    }
    function completeURI(editor, cur, token) {
        return false; // No completion here in skwrl
        var bC = token.string.substr(0, cur.ch - token.start),
        aC = token.string.substr(cur.ch - token.start),
        replUntil = token.state.current()=='url'?token.end+1:cur.ch + Math.max(aC.search(/[\s]/), 0);
        
        var suggestions;
        try {
            var qs = {};
            if (token.state.stack.indexOf("transformer") >= 0) qs['mode'] = "transformer";
            for (var n in token.state.namespaces) {
                qs['ns_'+n] = token.state.getNamespace(n);
            }
            qs['uri'] = bC;
            
            jQuery.ajax(token.state.lmfBaseURL + "ldpath/util/complete", {
               async: false,
               data: qs,
               success: function(data) {
                   suggestions = data;  
               },
               dataType: "json"
            });
        } catch (e) {}
        if (suggestions !== undefined) {
            for (var i = 0; i < suggestions.length; i++) {
                if (suggestions[i].match(/^\w+:\w+$/)) {
                    // curie!
                    suggestions[i] = suggestions[i] + " ";
                } else {
                    suggestions[i] = "<" + suggestions[i] + "> ";
                }
            }
            return {
              list: suggestions,
              from: {line: cur.line, ch: token.start - 1},
              to: {line: cur.line, ch: replUntil}
            };
        }
        return false;
    }
    function completeCUIE(editor, cur, token) {
        return false; // no completion here in skwrl
        var from = token.start, 
            to = token.end, 
            req = token.string.substr(0, cur.ch - token.start);
        var prevToken = editor.getTokenAt({line: cur.line, ch: token.start});
        if (token.className == 'atom' && prevToken.className == 'qualifier') {
            from = prevToken.start;
            req = prevToken.string + req;
        }
        
        var suggestions;
        try {
            var qs = {};
            if (token.state.stack.indexOf("transformer") >= 0) qs['mode'] = "transformer";
            for (var n in token.state.namespaces) {
                qs['ns_'+n] = token.state.getNamespace(n);
            }
            qs['prefix'] = req;

            jQuery.ajax(token.state.lmfBaseURL + "ldpath/util/complete", {
               async: false,
               data: qs,
               success: function(data) {
                   suggestions = data;  
               },
               dataType: "json"
            });
        } catch (e) {}
        if (suggestions !== undefined) {
            for (var i = 0; i < suggestions.length; i++) {
                if (suggestions[i].match(/^\w+:\w+(\(\))?$/)) {
                    // curie!
                    suggestions[i] = suggestions[i] + " ";
                } else {
                    // prefix only
                    suggestions[i] = suggestions[i] + ":";
                }
            }
            return {
              list: suggestions,
              from: {line: cur.line, ch: from},
              to: {line: cur.line, ch: to}
            };
        }
        
        return false;
    }
    function insertPrefixDef(editor, cur, token) {
        var prefix = token.string.replace(/:?$/, ""), result;
        try {
//            jQuery.ajax(token.state.lmfBaseURL + "ldpath/util/prefix", {
            jQuery.ajax("http://prefix.cc/" + prefix + ".file.json", {
                async: false,
                data: {prefix: prefix},
                success: function(data) {
                    result = data[prefix];
                },
                dataType: "json"
            });
        } catch (e) {}
        if (result !== undefined) {
            // check if this url is already prefixed
            var px;
            for (var i in token.state.namespaces) {
                if (token.state.namespaces[i] == result) {
                    px = i;
                    break;
                }
            } 
            if (px) {
                return {
                    list: [ px + ":" ],
                    from: { line: cur.line, ch: token.start },
                    to: { line: cur.line, ch: token.end }
                };
            } else {
            return {
                list: [ "@prefix " + prefix + ": <" + result + ">\n" ],
                from: {line: 0, ch: 0},
                to: {line: 0, ch: 0}
            };
            } 
        }
    }
    function skwrlAutocomplete(editor, options) {
        var cur = editor.getCursor(),
            line = editor.getLine(cur.line),
            token = editor.getTokenAt(cur);
        
        if (token.state.stack.indexOf('prefix') >= 0) {
            return completePrefix(editor, cur, token);
        } else if (token.className == "string-2") {
            return insertPrefixDef(editor, cur, token);
        } else {
            if (console && console.log) {
                console.log("State: " + token.state.stack);
            }
        }
    }
    CodeMirror.registerHelper("hint", "skwrl", skwrlAutocomplete);
}

}
