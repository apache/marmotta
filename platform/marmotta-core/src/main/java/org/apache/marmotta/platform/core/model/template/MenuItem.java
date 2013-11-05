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
package org.apache.marmotta.platform.core.model.template;

import org.apache.marmotta.platform.core.api.templating.TemplatingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tkurz
 * Date: 18.01.13
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class MenuItem {

    private boolean initialized = false;
    public final static String DEFAULT_MENU_ICON = "icon-asterisk";

    private HashMap<String,Object> properties;
    private List<MenuItem> items;
    private MenuItemType type;

    public MenuItem(String label, MenuItemType type) {
        this.properties = new HashMap<String, Object>();
        this.type = type;
        this.items = new ArrayList<MenuItem>();

        properties.put("items",items);
        properties.put("label",label);
        properties.put("isActive",false);
        properties.put("icon",DEFAULT_MENU_ICON);
    }

    public HashMap<String,Object> getProperties() {
        if(properties.get("items") != null) {
            List<Object> os = new ArrayList<Object>();
            for(MenuItem item : items) {
                os.add(item.getProperties());
            }
            properties.put("items",os);
        }
        return properties;
    }

    public void set(String name, Object value) {
        properties.put(name,value);
    }

    public Object get(String name) {
        return properties.get(name);
    }

    public void addItem(MenuItem item) {
        items.add(item);
    }

    public boolean setActive(String path) {
        boolean isActive = false;
        switch(type) {
            case ROOT:
            case CONTAINER:
            case MODULE:
                for(MenuItem item : items) {
                    if(item.setActive(path)) {
                        isActive = true;
                    }
                }
                break;
            case PAGE:
                isActive  = get("path").equals(path);
                break;
            case WEBSERVICE:
                String s = (String)properties.get("path");
                isActive = (
                        path.startsWith(s.substring(0,s.lastIndexOf("/"))) &&
                                path.contains(TemplatingService.DEFAULT_REST_PATH));
                break;
            default:
                isActive = false;
        }
        set("isActive",isActive);
        return isActive;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
