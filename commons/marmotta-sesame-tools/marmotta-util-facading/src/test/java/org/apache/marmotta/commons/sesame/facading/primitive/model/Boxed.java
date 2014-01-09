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

package org.apache.marmotta.commons.sesame.facading.primitive.model;

import java.util.Date;
import java.util.Locale;

import org.apache.marmotta.commons.sesame.facading.annotations.RDFPropertyBuilder;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

@RDFPropertyBuilder(PropBuilder.class)
public interface Boxed extends Facade {

    public Integer getInteger();
    public void setInteger(Integer integer);
    public Long getLong();
    public void setLong(Long l);
    public Float getFloat();
    public void setFloat(Float f);
    public Double getDouble();
    public void setDouble(Double d);
    public Character getCharacter();
    public void setCharacter(Character character);
    public Byte getByte();
    public void setByte(Byte b);
    public Boolean getBoolean();
    public void setBoolean(Boolean b);
    public Locale getLocale();
    public void setLocale(Locale locale);
    public String getString();
    public void setString(String string);
    public Date getDate();
    public void setDate(Date date);
    
    
}
