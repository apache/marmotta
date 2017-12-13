//
// Created by wastl on 15.02.16.
//

#ifndef MARMOTTA_PARSER_BASE_H
#define MARMOTTA_PARSER_BASE_H

#include <utility>
#include <map>
#include <string>

#include <model/rdf_model.h>

namespace marmotta {
namespace ldpath {
namespace parser {

template<Node>
class LdPathParser<Node> {

 private:
    enum Mode { RULE, SELECTOR, TEST, PROGRAM, PREFIX };

    /**
     * A map mapping from namespace prefix to namespace URI
     */
    std::map<std::string,std::string> namespaces;

    Mode mode;

 public:
    LdPathParser(NodeBackend<Node> backend, Reader in) {
        this(backend,null,in);
    }

 public LdPathParser(NodeBackend<Node> backend, Configuration config, Reader in) {
        this(in);
        this.backend = backend;
        if(config == null) {
            this.config = new DefaultConfiguration();
        } else {
            this.config = config;
        }

        initialise();
    }

 public LdPathParser(NodeBackend<Node> backend, InputStream in) {
        this(backend,null,in);
    }

 public LdPathParser(NodeBackend<Node> backend, Configuration config, InputStream in) {
        this(in);
        this.backend = backend;
        if(config == null) {
            this.config = new DefaultConfiguration();
        } else {
            this.config = config;
        }

        initialise();
    }

 public LdPathParser(NodeBackend<Node> backend, InputStream in, String encoding) {
        this(backend,null,in,encoding);
    }

 public LdPathParser(NodeBackend<Node> backend, Configuration config, InputStream in, String encoding) {
        this(in,encoding);
        this.backend = backend;
        if(config == null) {
            this.config = new DefaultConfiguration();
        } else {
            this.config = config;
        }

        initialise();
    }

 public Program<Node> parseProgram() throws ParseException {
            namespaces.clear();
            namespaces.putAll(config.getNamespaces());

            mode = Mode.PROGRAM;
            try {
                return Program();
            } catch(TokenMgrError error){
                throw new ParseException("Unable to parse Program: (Message: "+error.getMessage()+")");
            }
    }

 public Entry<String, String> parsePrefix() throws ParseException {
            namespaces.clear();
            namespaces.putAll(config.getNamespaces());
            mode = Mode.PREFIX;
            try {
                return Namespace();
            } catch (TokenMgrError error) {
                throw new ParseException("Unable to parse Prefix: (Message: "+ error.getMessage()+")");
            }
    }

 public Map<String, String> parsePrefixes() throws ParseException {
            namespaces.clear();
            namespaces.putAll(config.getNamespaces());
            mode = Mode.PREFIX;
            try {
                return Namespaces();
            } catch (TokenMgrError error) {
                throw new ParseException("Unable to parse Prefixes: (Message: "+ error.getMessage()+")");
            }
    }


 public NodeSelector<Node> parseSelector(Map<String,String> ctxNamespaces) throws ParseException {
            namespaces.clear();
            namespaces.putAll(config.getNamespaces());
            if(ctxNamespaces != null) {
                namespaces.putAll(ctxNamespaces);
            }

            mode = Mode.SELECTOR;

            try {
                return Selector();
            } catch(TokenMgrError error){
                throw new ParseException("Unable to parse Selector: (Message: "+error.getMessage()+")");
            }
    }

 public NodeTest<Node> parseTest(Map<String, String> ctxNamespaces) throws ParseException {
            namespaces.clear();
            namespaces.putAll(config.getNamespaces());
            if (ctxNamespaces != null) {
                namespaces.putAll(ctxNamespaces);
            }
            mode = Mode.TEST;
            try {
                return NodeTest();
            } catch (TokenMgrError error) {
                throw new ParseException("Unable to parse Test: (Message: "+ error.getMessage()+")");
            }
    }

 public <T> FieldMapping<T,Node> parseRule(Map<String,String> ctxNamespaces) throws ParseException {
            namespaces.clear();
            namespaces.putAll(config.getNamespaces());
            if(ctxNamespaces != null) {
                namespaces.putAll(ctxNamespaces);
            }

            mode = Mode.RULE;

            try {
                return Rule();
            } catch(TokenMgrError error){
                throw new ParseException("Unable to parse Rule: (Message: "+error.getMessage()+")");
            }
    }

 public Node resolveURI(URI uri) {
        return backend.createURI(uri.toString());
    }

 public Node resolveResource(String uri) throws ParseException {
            return backend.createURI(uri);
    }

 public Node resolveResource(String prefix, String local) throws ParseException {
            return resolveResource(resolveNamespace(prefix)+local);
    }


 public String resolveNamespace(String prefix) throws ParseException {
            String uri = namespaces.get(prefix);
            if(uri == null) {
                throw new ParseException("Namespace "+prefix+" not defined!");
            }
            return uri;
    }


 public SelectorFunction<Node> getFunction(String uri) throws ParseException {
            if(xsdNodeFunctionMap.get(uri) != null) {
                return xsdNodeFunctionMap.get(uri);
            } else {
                throw new ParseException("function with URI "+uri+" does not exist");
            }
    }

 public TestFunction<Node> getTestFunction(String uri) throws ParseException {
            if (xsdNodeTestMap.get(uri) != null) {
                return xsdNodeTestMap.get(uri);
            } else {
                throw new ParseException("test function with URI "+uri+" does not exist");
            }
    }

 public NodeTransformer<?,Node> getTransformer(URI type) throws ParseException {
            return getTransformer(type.toString());
    }

 public NodeTransformer<?,Node> getTransformer(Node node) throws ParseException {
            return getTransformer(backend.stringValue(node));
    }

 public NodeTransformer<?,Node> getTransformer(String uri) throws ParseException {
            if(xsdNodeTransformerMap.get(uri) != null) {
                return xsdNodeTransformerMap.get(uri);
            } else {
                throw new ParseException("transformer with URI "+uri+" does not exist");
            }
    }


 private void initialise() {
        initTransformerMappings();
        initFunctionMappings();
    }

    /**
     * Register the function passed as argument in this parser's function map.
     */
 public void registerFunction(SelectorFunction<Node> function) {
        registerFunction(xsdNodeFunctionMap,function);
    }

 public void registerFunction(TestFunction<Node> test) {
        registerTest(xsdNodeTestMap, test);
    }

    /**
     * Register the result transformer passed as argument for the given type uri.
     */
 public void registerTransformer(String typeUri, NodeTransformer<?,Node> transformer) {
        xsdNodeTransformerMap.put(typeUri,transformer);
    }


    /**
     * A map mapping from XSD types to node transformers.
     */
 private Map<String, NodeTransformer<?,Node>> xsdNodeTransformerMap;
 private void initTransformerMappings() {
        Map<String, NodeTransformer<?,Node>> transformerMap = new HashMap<String, NodeTransformer<?,Node>>();

        transformerMap.putAll(config.getTransformers());

        xsdNodeTransformerMap = transformerMap;
    }


 private Map<String, SelectorFunction<Node>> xsdNodeFunctionMap;
 private Map<String, TestFunction<Node>> xsdNodeTestMap;
 private void initFunctionMappings() {
        Map<String, SelectorFunction<Node>> functionMap = new HashMap<String, SelectorFunction<Node>>();

        functionMap.putAll(config.getFunctions());

        xsdNodeFunctionMap = functionMap;

        Map<String, TestFunction<Node>> testMap = new HashMap<String, TestFunction<Node>>();
        testMap.putAll(config.getTestFunctions());
        xsdNodeTestMap = testMap;
    }

 private void registerFunction(Map<String, SelectorFunction<Node>> register, final SelectorFunction<Node> function) {
        register.put(NS_LMF_FUNCS + function.getPathExpression(backend), function);
    }

 private void registerTest(Map<String, TestFunction<Node>> register, final TestFunction<Node> test) {
        register.put(NS_LMF_FUNCS + test.getLocalName(), test);
    }

 private class Namespace implements Entry<String, String> {
        private String key, val;
        public Namespace(String key, String val) {
            this.key = key;
            this.val = val;
        }
        @Override
        public String getKey() {
            return key;
        }
        @Override
        public String getValue() {
            return val;
        }
        @Override
        public String setValue(String value) {
            String oV = val;
            val = value;
            return oV;
        }
    }

}

}
}
}


#endif //MARMOTTA_PARSER_BASE_H
