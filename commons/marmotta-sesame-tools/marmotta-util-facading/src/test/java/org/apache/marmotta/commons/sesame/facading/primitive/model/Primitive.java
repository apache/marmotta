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

import org.apache.marmotta.commons.sesame.facading.annotations.RDFPropertyBuilder;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

@RDFPropertyBuilder(PropBuilder.class)
public interface Primitive extends Facade {

    public int getInt();
    public void setInt(int i);
    
    public float getFloat();
    public void setFloat(float f);
    
    public double getDouble();
    public void setDouble(double d);
    
    public long getLong();
    public void setLong(long l);
    
    public boolean getBoolean();
    public void setBoolean(boolean b);
    
    public char getChar();
    public void setChar(char c);
    
    public byte getByte();
    public void setByte(byte b);
    
}
