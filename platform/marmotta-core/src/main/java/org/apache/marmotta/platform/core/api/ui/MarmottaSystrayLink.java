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
package org.apache.marmotta.platform.core.api.ui;

/**
 * Implementing classes provide a menu entry for the Systray menu and possibly other user interfaces.
 * The SystrayService in lmf-systray will inject all such service classes; with the interface in lmf-core
 * it is however not necessary to always have a dependency on lmf-systray.
 * <p/>
 * Author: Sebastian Schaffert
 */
public interface MarmottaSystrayLink {

    enum Section {
        ADMIN, DEMO
    }

    /**
     * Get the label of the systray entry for displaying in the menu.
     * @return
     */
    String getLabel();


    /**
     * Get the link to point the browser to when the user selects the menu entry.
     * @return
     */
    String getLink();

    /**
     * Get the section where to add the systray link in the menu (currently either ADMIN or DEMO).
     * @return
     */
    Section getSection();
}
