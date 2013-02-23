/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.services.templating;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Some methods useful for template working
 * 
 * @author Sergio Fernández
 *
 */
public class TemplatingHelper {
    
    public static Configuration getConfiguration() {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(TemplatingHelper.class, TemplatingService.PATH);
        return cfg;
    }
    
    public static Template getTemplate(String name) throws IOException {
        return getConfiguration().getTemplate(name);
    }
    
    public static Template getTemplate(Configuration conf, String name) throws IOException {
        return conf.getTemplate(name);
    }
    
    public static String processTemplate(String name) throws IOException, TemplateException {
        return processTemplate(name, new HashMap<String, Object>());
    }

    public static String processTemplate(String name, Map<String, Object> data) throws IOException, TemplateException {
        Template tpl = getTemplate(name);
        OutputStream os = new ByteArrayOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        tpl.process(data, writer);
        writer.flush();
        return os.toString();
    }

    public static void processTemplate(String name, Writer writer) throws IOException, TemplateException {
        processTemplate(name, new HashMap<String, Object>(), writer);
    }

    public static void processTemplate(String name, Map<String, Object> data, Writer writer) throws IOException, TemplateException {
        Template tpl = getTemplate(name);
        tpl.process(data, writer);
        writer.flush();
    }

}
