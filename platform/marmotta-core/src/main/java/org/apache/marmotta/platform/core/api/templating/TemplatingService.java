/**
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
package org.apache.marmotta.platform.core.api.templating;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Templating Service
 * 
 * @author Sergio Fernández
 */
public interface TemplatingService {
    
    final static String PATH = "/templates/";

    public final static String DEFAULT_REST_PATH = "/doc/rest/";

    public final static String DEFAULT_REST_FILE = "overview-index.html";
    
    final static String ADMIN_TPL = "admin.ftl";

    @Deprecated
    final static String ERROR_404_TPL = "404.ftl";

    final static String ERROR_TPL = "error.ftl";
    
    final static String RDF_HTML_TPL = "rdfhtml.ftl";
    
    static final String DEFAULT_PROJECT = "marmotta";
    
    static final String DEFAULT_STYLE = "blue";

    final static String DEFAULT_WEBSERVICE_TITLE = "Webservice";
    
    void initDataModel();
    
    Configuration getConfiguration();
    
    Configuration getConfiguration(Class<?> cls);    
    
    Template getTemplate(String name) throws IOException; 
    
    Template getTemplate(Class<?> cls, String name) throws IOException;
    
    String process(String name) throws IOException, TemplateException;
    
    String process(String name, Map<String, Object> data) throws IOException, TemplateException;

    void process(String name, Writer writer) throws IOException, TemplateException;
    
    void process(String name, Map<String, Object> data, Writer writer) throws IOException, TemplateException;

    void process(Class<?> cls, String name, Map<String, Object> data, Writer writer) throws IOException, TemplateException;

}
