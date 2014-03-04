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

package org.apache.marmotta.kiwi.infinispan.remote;

import org.apache.marmotta.kiwi.infinispan.externalizer.*;
import org.jboss.marshalling.ClassExternalizerFactory;
import org.jboss.marshalling.Externalizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class CustomClassExternalizerFactory implements ClassExternalizerFactory {

    Map<Class<?>,Externalizer> externalizers = new HashMap<>();

    public CustomClassExternalizerFactory() {

        addExternalizer(new UriExternalizer());
        addExternalizer(new BNodeExternalizer());
        addExternalizer(new BooleanLiteralExternalizer());
        addExternalizer(new DateLiteralExternalizer());
        addExternalizer(new DoubleLiteralExternalizer());
        addExternalizer(new IntLiteralExternalizer());
        addExternalizer(new StringLiteralExternalizer());
        addExternalizer(new TripleExternalizer());

    }

    private void addExternalizer(BaseExternalizer e) {
        for(Class c : (Set <Class>)e.getTypeClasses()) {
            externalizers.put(c,e);
        }
    }

    /**
     * Look up a custom externalizer for a given object class.  If no such externalizer exists, returns {@code null}.
     *
     * @param type the type to be externalized
     * @return the externalizer, or {@code null} if there is none
     */
    @Override
    public Externalizer getExternalizer(Class<?> type) {
        if(externalizers.containsKey(type)) {
            return externalizers.get(type);
        } else {
            return null;
        }
    }
}
