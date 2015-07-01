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
package org.apache.marmotta.platform.core.api.templating;

import org.apache.marmotta.platform.core.exception.TemplatingException;

import javax.servlet.ServletContext;

/**
 * User: Thomas Kurz
 * Date: 22.07.11
 * Time: 13:06
 */
public interface AdminInterfaceService {
    
    public final static String PATH = "/templates/";

    /**
     * inits a freebase template service with a servlet context
     * @param context
     */
    public void init(ServletContext context) throws TemplatingException;
    
    /**
     * this method wraps a file with a default template
     * @param bytes
     * @return
     */
    public byte[] process(byte[] bytes, String path) throws TemplatingException;

    /**
     * Check whether the templating service considers the resource passed in the path as a menu entry it is
     * responsible for.
     *
     * @param path
     * @return
     */
    boolean isMenuEntry(String path);

}
