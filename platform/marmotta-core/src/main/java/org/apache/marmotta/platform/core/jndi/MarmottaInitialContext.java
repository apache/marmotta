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

import org.apache.marmotta.platform.core.util.CDIContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import java.util.Hashtable;

/**
 * This is our own simplified JNDI implementation providing the required functionality for looking up the bean
 * manager and the SOLR home directory.
 * <p/>
 * The implementation is based on Simple-JNDI, which already offers the core functionality for memory-based JNDI
 * implementations.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaInitialContext extends MarmottaContext {

    private static Logger log = LoggerFactory.getLogger(MarmottaInitialContext.class);

    private static MarmottaInitialContext instance;


    /**
     * @param env
     */
    public MarmottaInitialContext(Hashtable<Object, Object> env) {
        super(env);

        log.info("JNDI: creating Apache Marmotta Initial Context ...");

        try {
            Context ctx_java = this.createSubcontext("java:comp");
            Context ctx_env  = ctx_java.createSubcontext("env");

            registerBeanManager(ctx_env);

        } catch (NamingException e) {
            log.error("error while initialising Apache Marmotta JNDI context",e);
        }

        CDIContext.showJndiContext(this, "java:", "");

    }

    public static MarmottaInitialContext getInstance(Hashtable<Object, Object> env) {
        if(instance == null) {
            instance = new MarmottaInitialContext(env);
        }
        return instance;
    }


    private void registerBeanManager(Context ctx_java) throws NamingException {
        ctx_java.bind("BeanManager", new Reference("javax.enterprise.inject.spi.BeanManager", "org.jboss.weld.resources.ManagerObjectFactory", null));
    }


}
