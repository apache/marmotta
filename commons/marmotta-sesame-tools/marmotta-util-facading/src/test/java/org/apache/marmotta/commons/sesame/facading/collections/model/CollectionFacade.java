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

package org.apache.marmotta.commons.sesame.facading.collections.model;


import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.marmotta.commons.sesame.facading.annotations.RDF;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

public interface CollectionFacade extends Facade {

    @RDF("http://www.example.com/rdf/vocab/date")
    public List<Date> getDates();
    public void setDates(List<Date> dates);
    public void addDate(Date date);
    public void deleteDates();

    @RDF("http://www.example.com/rdf/vocab/autor")
    public void addAutor(String autor);
    public void setAutors(Collection<String> authors);

}
