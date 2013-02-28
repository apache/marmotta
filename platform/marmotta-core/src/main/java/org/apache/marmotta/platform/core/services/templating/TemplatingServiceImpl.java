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

import freemarker.template.Template;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.ModuleService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.apache.marmotta.platform.core.exception.TemplatingException;
import org.apache.marmotta.platform.core.model.template.MenuItem;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Thomas Kurz
 * Date: 22.07.11
 * Time: 13:06
 */
@ApplicationScoped
public class TemplatingServiceImpl implements TemplatingService {

    private ServletContext context;

    private static enum Properties { HEAD, CONTENT }

    @Inject
    private ModuleService moduleService;

    @Inject
    private ConfigurationService configurationService;

    //some statics
    private static final String TEMPLATE_STRING = "admin.ftl";
    private static final String DEFAULT_REST_PATH = "/doc/rest/";
    private static final String DEFAULT_REST_FILE = "overview-summary.html";
    private static final String DEFAULT_STYLE = "screen";
    private static final String DEFAULT_TITLE_FOR_WEBSERVICES = "webservices";
    private static final String DEFAULT_PROJECT = "lmf";

    //pattern to filter comments content
    private static final Pattern PATTERN = Pattern.compile("\\<!--###BEGIN_([^#]+)###--\\>(.+)\\<!--###END_\\1###--\\>",Pattern.DOTALL);
    private Template tpl;
    private Menu menu;

    /**
     * inits a freemarker template service with a servlet context
     * @param context a servlet context
     */
    @Override
    public void init(ServletContext context) throws TemplatingException {
        this.context = context;
        initDataModel();
        try {
            tpl = TemplatingHelper.getTemplate(TEMPLATE_STRING);
        } catch (IOException e) {
            //e.printStackTrace();
            throw new TemplatingException(e.getMessage());
        }
    }

    /**
     * Check whether the templating service considers the resource passed in the path as a menu entry it is
     * responsible for.
     *
     * @param path a url path
     * @return if the give path points to an admin page
     */
    @Override
    public boolean isMenuEntry(String path) {
        if(menu.path_titles.keySet().contains(configurationService.getPath()+path)) return true;
        if(path.contains(DEFAULT_REST_PATH)) return true;
        else return false;
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
        //apply template
        if(!isMenuEntry(path)) return bytes;
        //activate
        String module = menu.getCurrentModule(configurationService.getPath() + path);
        //fill data model
        @SuppressWarnings("unchecked")
        Map<String, Object> datamodel = (Map<String, Object>)basic_map.clone();
        //begin hack!!!
        datamodel.put("USER_MODULE_IS_ACTIVE", moduleService.listModules().contains("Users"));
        //end hack!!!
        datamodel.put("MODULE_MENU",menu.menuItems);
        datamodel.put("DEFAULT_STYLE", configurationService.getStringConfiguration("kiwi.pages.style", DEFAULT_STYLE));
        datamodel.put("CURRENT_TITLE", getNameFromPath(path));
        datamodel.put("CURRENT_MODULE",module);
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
            tpl.process(datamodel, new OutputStreamWriter(bos));
            bytes = bos.toByteArray();
            bos.flush();
            bos.close();
        } catch (Exception e) {
            throw new TemplatingException(e.getMessage());
        }
        return bytes;
    }

    //datamodel may not be empty
    private HashMap<String,String> basic_map;
    /**
     * creates a data model, which contains all properties (with empty values)
     */
    private void initDataModel() {
        basic_map = new HashMap<String,String>();
        for(Properties p : Properties.values()) {
            basic_map.put(p.name(),"<!-- "+p.name()+" not defined -->");
        }
        basic_map.put("SERVER_URL", configurationService.getServerUri());
        basic_map.put("BASIC_URL", configurationService.getBaseUri());
        String project = configurationService.getStringConfiguration("kiwi.pages.project", DEFAULT_PROJECT);
        basic_map.put("PROJECT", project);
        basic_map.put("LOGO", configurationService.getStringConfiguration("kiwi.pages.project."+project+".logo", project+".png"));
        basic_map.put("FOOTER", configurationService.getStringConfiguration("kiwi.pages.project."+project+".footer", "(footer not properly configured for project "+project+")"));

        menu = new Menu();
    }

    /**
     * Update the data model in case an important value has changed
     * @param event
     */
    public void configurationChangedEvent(@Observes ConfigurationChangedEvent event) {
        if (event.getKeys().contains("kiwi.context")
                || event.getKeys().contains("kiwi.host")
                || event.getKeys().contains("templating.sort_by_weight")
                || event.getKeys().contains("kiwi.pages.project")) {
            initDataModel();
        }
    }

    /**
     * This object represents a menu for the admin interface. It is build using the ModuleService.
     */
    class Menu {

        public List<MenuItem> menuItems;
        public Map<String,String> path_titles;

        public Menu() {
            //instantiate
            menuItems = new ArrayList<MenuItem>();
            path_titles = new HashMap<String, String>();
            //sort menu
            ArrayList<String> menuSorted = new ArrayList<String>(moduleService.listModules());
            if(configurationService.getBooleanConfiguration("templating.sort_by_weight",true)) {
                Collections.sort(menuSorted, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        final int w1 = moduleService.getWeight(o1), w2 = moduleService.getWeight(o2);
                        if (w1 == w2) return o1.compareTo(o2);
                        return w1 - w2;
                    }
                });
            } else {
                Collections.sort(menuSorted);
            }

            //build structure
            for(String module : menuSorted) {
                String path = configurationService.getPath() + moduleService.getModuleWeb(module);
                if(moduleHasAdminPages(module)) {
                    MenuItem menu_item = new MenuItem();
                    menu_item.getProperties().put("title",module);
                    menu_item.getProperties().put("baseurl",moduleService.getModuleConfiguration(module).getConfiguration().getString("baseurl","/"+module));
                    for(String page : moduleService.getAdminPages(module)) {
                        if(page.equals("")) {
                            continue;
                        }
                        MenuItem submenu = new MenuItem();
                        submenu.getProperties().put("title",getNameFromPath(page));
                        submenu.getProperties().put("path",path+page);
                        //test if it is active
                        menu_item.getSubmenu().add(submenu);
                        path_titles.put(path+page,page);
                    }
                    if(moduleService.getWebservices(module)!=null &&
                            !moduleService.getWebservices(module).isEmpty() &&
                            !moduleService.getWebservices(module).iterator().next().equals("")) {
                        MenuItem submenu = new MenuItem();
                        submenu.getProperties().put("title",DEFAULT_TITLE_FOR_WEBSERVICES);
                        submenu.getProperties().put("path",path+DEFAULT_REST_PATH+DEFAULT_REST_FILE);
                        //test if it is active
                        menu_item.getSubmenu().add(submenu);
                        path_titles.put(path+DEFAULT_REST_PATH+DEFAULT_REST_FILE,DEFAULT_TITLE_FOR_WEBSERVICES);
                    }
                    menuItems.add(menu_item);
                }
            }
        }

        /**
         * get current module and set submenu to active
         * @param path the current system path
         * @return current module name
         */
        public String getCurrentModule(String path) {
            String module = "";
            boolean active = false;
            //test with module and submenu must be active
            for(MenuItem menuItem : menuItems) {
                if(path.startsWith((String)menuItem.getProperties().get("baseurl"))) {
                    module = (String)menuItem.getProperties().get("title");
                }
                for(MenuItem submenu : menuItem.getSubmenu()) {
                    if(submenu.getProperties().get("path").equals(path)) {
                        submenu.getProperties().put("active",true);
                        module = (String)menuItem.getProperties().get("title");
                        active = true;
                    } else {
                        submenu.getProperties().put("active",false);
                    }
                }
            }
            //workaround for webservices (autogenerated pages that are nit fix stored in the menu structure)
            if(!active) {
                for(MenuItem menuItem : menuItems) {
                    if(module.equals(menuItem.getProperties().get("title"))) {
                        for(MenuItem submenu : menuItem.getSubmenu()) {
                            if(submenu.getProperties().get("title").equals(DEFAULT_TITLE_FOR_WEBSERVICES)) {
                                submenu.getProperties().put("active",true);
                            }
                        }
                    }
                }
            }
            return module;
        }

        /**
         * Tests if a module should appear in the menu
         * @param module a module name
         * @return true is module should appear, false if not
         */
        private boolean moduleHasAdminPages(String module) {
            if(moduleService.getAdminPages(module)!=null &&
                    !moduleService.getAdminPages(module).isEmpty() &&
                    !moduleService.getAdminPages(module).get(0).equals(""))
                return true;
            else if(moduleService.getWebservices(module)!=null &&
                    !moduleService.getWebservices(module).isEmpty() &&
                    !moduleService.getWebservices(module).iterator().next().equals("")) return true;
            return false;
        }

    }

    /**
     * returns a proper name for a path by using the filename.
     * @param path
     * @return
     */
    private String getNameFromPath(String path) {
        if(path.contains(DEFAULT_REST_PATH)) return DEFAULT_TITLE_FOR_WEBSERVICES;
        return path.substring(path.lastIndexOf("/")).replaceAll("/"," ").replaceAll("_"," ").replaceAll(".html","").replaceAll(".jsp","");
    }

}
