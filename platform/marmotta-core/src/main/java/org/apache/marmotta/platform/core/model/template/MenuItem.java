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

import org.apache.marmotta.platform.core.api.templating.AdminInterfaceService;
import org.apache.marmotta.platform.core.services.templating.AdminTemplatingServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.marmotta.platform.core.model.template.MenuItemType.*;

/**
 * Created with IntelliJ IDEA.
 * User: tkurz
 * Date: 18.01.13
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class MenuItem {

    private boolean active;
    private String label;
    private String path;
    private MenuItemType type;
    private List<MenuItem> items;
    private String icon;

    public MenuItem(String label, MenuItemType type) {
        this.type = type;
        this.label = label;
        this.items = new ArrayList<MenuItem>();
        this.active = false;
        this.icon = AdminInterfaceService.DEFAULT_MENU_ICON;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<MenuItem> getItems() {
        return this.items;
    }

    public void addItem(MenuItem item) {
        this.addItem(item);
    }

    public boolean setActive(String path) {
        switch(type) {
            case ROOT:
            case CONTAINER:
            case MODULE:
                for(MenuItem item : items) {
                    if(item.setActive(path)) {
                        active = true;
                    } else {
                        active = false;
                    }
                }
                return active;
            case PAGE:
                return active = this.path.equals(path);
            case WEBSERVICE:
                return active = path.contains(AdminInterfaceService.DEFAULT_REST_PATH);
            default:
                return false;
        }
    }
}
