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
package org.apache.marmotta.ldpath.template.engine;

import freemarker.core.Environment;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.apache.marmotta.ldpath.model.Constants;
import org.apache.marmotta.ldpath.template.model.freemarker.TemplateNodeModel;
import org.apache.marmotta.ldpath.template.model.freemarker.TemplateStackModel;
import org.apache.marmotta.ldpath.template.model.freemarker.TemplateWrapperModel;
import org.apache.marmotta.ldpath.template.model.transformers.*;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LDPathMethod<Node> implements TemplateMethodModel {


    private LDPath<Node> ldpath;
    private NodeBackend<Node> backend;

    public LDPathMethod(RDFBackend<Node> backend) {
        this.ldpath  = new LDPath<Node>(backend);
        this.backend = backend;

        // register custom freemarker transformers for the parser so we get the results immediately in the freemarker model
        ldpath.registerTransformer(Constants.NS_XSD + "string", new TemplateScalarTransformer<Node>());
        ldpath.registerTransformer(Constants.NS_XSD + "decimal", new TemplateLongTransformer<Node>());
        ldpath.registerTransformer(Constants.NS_XSD + "integer", new TemplateIntegerTransformer<Node>());
        ldpath.registerTransformer(Constants.NS_XSD + "long", new TemplateLongTransformer<Node>());
        ldpath.registerTransformer(Constants.NS_XSD + "short", new TemplateIntegerTransformer<Node>());
        ldpath.registerTransformer(Constants.NS_XSD + "double", new TemplateDoubleTransformer<Node>());
        ldpath.registerTransformer(Constants.NS_XSD + "float", new TemplateFloatTransformer<Node>());
        ldpath.registerTransformer(Constants.NS_XSD + "dateTime", new TemplateDateTransformer<Node>(TemplateDateModel.DATETIME));
        ldpath.registerTransformer(Constants.NS_XSD + "date", new TemplateDateTransformer<Node>(TemplateDateModel.DATE));
        ldpath.registerTransformer(Constants.NS_XSD + "time", new TemplateDateTransformer<Node>(TemplateDateModel.TIME));
        ldpath.registerTransformer(Constants.NS_XSD + "boolean", new TemplateBooleanTransformer<Node>());
        ldpath.registerTransformer(Constants.NS_XSD + "anyURI", new TemplateScalarTransformer<Node>());

    }


    /**
     * Executes a method call. All arguments passed to the method call are
     * coerced to strings before being passed, if the FreeMarker rules allow
     * the coercion. If some of the passed arguments can not be coerced to a
     * string, an exception will be raised in the engine and the method will
     * not be called. If your method would like to act on actual data model
     * objects instead of on their string representations, implement the
     * {@link freemarker.template.TemplateMethodModelEx} instead.
     *
     * @param arguments a <tt>List</tt> of <tt>String</tt> objects
     *                  containing the values of the arguments passed to the method.
     * @return the return value of the method, or null. If the returned value
     *         does not implement {@link freemarker.template.TemplateModel}, it will be automatically
     *         wrapped using the {@link freemarker.core.Environment#getObjectWrapper() environment
     *         object wrapper}.
     */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        Environment env = Environment.getCurrentEnvironment();


        TemplateStackModel contextStack = (TemplateStackModel)env.getVariable("context");
        if(contextStack == null || contextStack.empty()) {
            throw new TemplateModelException("error; no context node available");
        }
        TemplateNodeModel<Node> context = (TemplateNodeModel<Node>)contextStack.peek();

        String path;
        if(arguments.size() != 1) {
            throw new TemplateModelException("the directive has been called without a path parameter");
        } else {
            path = (String)arguments.get(0);
            if(!path.contains("::")) {
                path = path + " :: xsd:string";
            }
        }

        TemplateWrapperModel<Map<String,String>> namespacesWrapped = (TemplateWrapperModel<Map<String,String>>)env.getGlobalVariable("namespaces");

        Map<String,String> namespaces;
        if(namespacesWrapped == null) {
            namespaces = new HashMap<String, String>();
            namespacesWrapped = new TemplateWrapperModel<Map<String, String>>(new HashMap<String, String>());
            env.setGlobalVariable("namespaces",namespacesWrapped);
        } else {
            namespaces = namespacesWrapped.getAdaptedObject(Map.class);
        }

        if(arguments.size() != 1) {
            throw new TemplateModelException("wrong number of arguments for method call");
        }

        try {
            Collection result = ldpath.pathTransform(context.getNode(),path,namespaces);
            if(result.size() > 0) {
                return result;
            } else {
                return null;
            }
        } catch (LDPathParseException e) {
            throw new TemplateModelException("could not parse path expression '"+path+"'",e);
        }
    }
}
