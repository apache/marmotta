/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.commons.sesame.facading.builder.model;


import java.util.Set;

import org.apache.marmotta.commons.sesame.facading.annotations.RDFPropertyBuilder;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

@RDFPropertyBuilder(ExamplePropBuilder.class)
public interface ExampleFacade extends Facade {

    public static final String NS = "http://www.example.com/vocab/test#";

    void setTitle(String title);
    String getTitle();

    void setTags(Set<String> tags);
    void addTag(String tag);
    Set<String> getTags();

}
