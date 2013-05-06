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

package org.apache.marmotta.platform.core.services.modules;

import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.platform.core.exception.TemplatingException;

import java.util.List;

/**
 * This class is used for more complex AdminPage information (e.g. for Templating).
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class AdminPage implements Comparable<AdminPage> {

    private String name;
    private String path;
    private boolean important = false;
    private int number = 0;

    /**
     * create AdminPage object
     * @param number for sorting
     * @param path path to page
     * @param name name (e.g. in the menu display)
     * @param important if true, the page can appear in a 'special link' section
     * @throws MarmottaException
     */
    AdminPage(int number, String path, String name, boolean important) throws MarmottaException {
        if(path==null) throw new MarmottaException("path for admin page must be defined");
        this.path = path;

        if(name != null) this.name = name;
        else this.name = getNameFromPath(path);

        this.important = important;
    }

    /**
     * Creates and AdminPage just from path
     * @param path
     */
    AdminPage(String path) {
        this.path= path;
        this.name = getNameFromPath(path);
    }

    /**
     * returns a 'useful' name from path
     * @param path
     * @return
     */
    private String getNameFromPath(String path) {
        return path.substring(path.lastIndexOf("/")).replaceAll("/"," ").replaceAll("_"," ").replaceAll(".html","").replaceAll(".jsp","");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * if numbers are used, the sorting is based on numbers, otherwise on alphabetical order of names
     * @param adminPage
     * @return
     */
    @Override
    public int compareTo(AdminPage adminPage) {
        if(number != adminPage.number) {
            return name.compareTo(adminPage.name);
        } else return ((Integer)number).compareTo(adminPage.number);
    }
}
