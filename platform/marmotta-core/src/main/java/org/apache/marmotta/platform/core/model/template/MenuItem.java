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

    private Map<String,Object> properties;
    private List<MenuItem> submenu;

    public MenuItem() {
        properties = new HashMap<String, Object>();
        submenu = new ArrayList<MenuItem>();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public List<MenuItem> getSubmenu() {
        return submenu;
    }
}
