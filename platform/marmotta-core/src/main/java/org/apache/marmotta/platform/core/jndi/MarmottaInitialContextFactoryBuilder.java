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
package org.apache.marmotta.platform.core.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import java.util.Hashtable;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaInitialContextFactoryBuilder implements InitialContextFactoryBuilder {

    public MarmottaInitialContextFactoryBuilder() {
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> hashtable) throws NamingException {
        // check if we are inside the Marmotta or outside; inside the Marmotta we return our own context factory,
        // outside the system default
        try {
            return (InitialContextFactory) Thread.currentThread().getContextClassLoader().loadClass(MarmottaContextFactory.class.getName()).getMethod("getInstance").invoke(null);
        } catch (Exception e) {
            String factoryName = Context.INITIAL_CONTEXT_FACTORY;

            try {
                return (InitialContextFactory) Thread.currentThread().getContextClassLoader().loadClass(factoryName).newInstance();
            } catch (Exception e1) {
                throw new NamingException("default context factory "+factoryName+" could not be initialised");
            }
        }
    }
}
