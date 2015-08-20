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
package org.apache.marmotta.platform.core.util;

import javax.enterprise.inject.Instance;
import javax.inject.Named;
import java.lang.annotation.Annotation;

/**
 * Some utilities for working with CDI services
 */
public abstract class CDIUtils {

    public static <T> Instance<T> selectNamed(Instance<T> instance, String name) {
        if (name != null)
            return instance.select(createNamedLiteral(name));
        return instance;
    }

    private static Annotation createNamedLiteral(final String name) {
        return new Named() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Named.class;
            }

            @Override
            public String value() {
                return name;
            }
        };
    }

}
