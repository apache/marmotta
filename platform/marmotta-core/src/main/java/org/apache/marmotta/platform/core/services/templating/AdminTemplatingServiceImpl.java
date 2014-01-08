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
package org.apache.marmotta.platform.core.services.templating;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.ModuleService;
import org.apache.marmotta.platform.core.api.templating.AdminInterfaceService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.exception.TemplatingException;
import org.apache.marmotta.platform.core.model.template.MenuItem;
import org.apache.marmotta.platform.core.model.template.MenuItemType;

/**
 * User: Thomas Kurz
 * Date: 22.07.11
 * Time: 13:06
 */
@ApplicationScoped
public class AdminTemplatingServiceImpl implements AdminInterfaceService {

    private ServletContext context;

    private static enum Properties { HEAD, CONTENT }

    @Inject
    private ModuleService moduleService;

    @Inject
    private TemplatingService templatingService;

    @Inject
    private ConfigurationService configurationService;

    //some statics
    private static final String DEFAULT_ICON = "icon-beaker";

    //pattern to filter comments content
    private static final Pattern PATTERN = Pattern.compile("\\<!--###BEGIN_([^#]+)###--\\>(.+)\\<!--###END_\\1###--\\>",Pattern.DOTALL);
    private MenuItem menu;

    /**
     * inits a freemarker template service with a servlet context
     * @param context a servlet context
     */
    @Override
    public void init(ServletContext context) throws TemplatingException {
        menu = buildMenu();
        this.context = context;
    }

    /**
     * this method wraps a file with a specified admin template. If the file is not a admin page,
     * the bytes are returned unprocessed
     * @param bytes content represented in a byte array
     * @return the processed (templated) byte array
     */
    @Override
    public byte[] process(byte[] bytes, String path) throws TemplatingException {

        if(!configurationService.getBooleanConfiguration("templating.cache.enabled",true) && context!=null) {
            init(context);
        }

        //set active
        menu.setActive(path);

        //fill data model
        Map<String, Object> datamodel = new HashMap<String,Object>();
        for(Properties p : Properties.values()) {
            datamodel.put(p.name(),"<!-- "+p.name()+" not defined -->");
        }
        //begin hack!!!
        datamodel.put("USER_MODULE_IS_ACTIVE", moduleService.listModules().contains("Users"));
        //end hack!!!

        //add menu
        datamodel.put("MENU",menu.getProperties());
        try {
            String s = new String(bytes);
            Matcher m = PATTERN.matcher(s);
            while (m.find()) {
                datamodel.put(m.group(1),m.group(2));
            }
        } catch (Exception e) {
            throw new TemplatingException(e.getMessage());
        }

        //make magic happen
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            templatingService.process(TemplatingService.ADMIN_TPL, datamodel, new OutputStreamWriter(bos));
            bytes = bos.toByteArray();
            bos.flush();
            bos.close();
        } catch (Exception e) {
            throw new TemplatingException(e.getMessage());
        }
        return bytes;
    }

    @Override
    public boolean isMenuEntry(String path) {
        return menu.setActive(path);
    }

    /**
     * build menu
     * @return
     */
    private MenuItem buildMenu() {
        MenuItem menu = new MenuItem("MENU", MenuItemType.ROOT);

        for(String container_string : moduleService.listSortedContainers()) {
            MenuItem container = new MenuItem(container_string, MenuItemType.CONTAINER);

            //add modules
            for(String module_string : moduleService.listSortedModules(container_string)) {
                MenuItem module = new MenuItem(module_string, MenuItemType.MODULE);
                module.set("path",moduleService.getModuleWeb(module_string));
                if(moduleService.getIcon(module_string) != null)
                    module.set("icon",moduleService.getIcon(module_string));

                //add pages
                for(HashMap<String,String> page_object : moduleService.getAdminPageObjects(module_string)) {
                    MenuItem page = new MenuItem(page_object.get("title"), MenuItemType.PAGE);
                    page.set("path",page_object.get("link"));
                    module.addItem(page);
                }

                //add webservice
                if(!moduleService.getWebservices(module_string).isEmpty()) {
                    MenuItem page = new MenuItem(TemplatingService.DEFAULT_WEBSERVICE_TITLE, MenuItemType.WEBSERVICE);
                    page.set("path",module.get("path")+TemplatingService.DEFAULT_REST_PATH+TemplatingService.DEFAULT_REST_FILE);
                    module.addItem(page);
                }

                //add if there are pages to display
                if(!module.isEmpty()) container.addItem(module);
            }
            menu.addItem(container);
        }

        return menu;
    }

}