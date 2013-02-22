/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.jndi;

import org.apache.marmotta.platform.core.util.KiWiContext;
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
public class LMFInitialContext extends LMFContext {

    private static Logger log = LoggerFactory.getLogger(LMFInitialContext.class);

    private static LMFInitialContext instance;


    /**
     * @param env
     */
    public LMFInitialContext(Hashtable<Object, Object> env) {
        super(env);

        log.info("JNDI: creating Apache Marmotta Initial Context ...");

        try {
            Context ctx_java = this.createSubcontext("java:comp");
            Context ctx_env  = ctx_java.createSubcontext("env");

            registerBeanManager(ctx_env);
            registerSolrHome(ctx_env);

        } catch (NamingException e) {
            log.error("error while initialising Apache Marmotta JNDI context",e);
        }

        KiWiContext.showJndiContext(this,"java:", "");

    }

    public static LMFInitialContext getInstance(Hashtable<Object, Object> env) {
        if(instance == null) {
            instance = new LMFInitialContext(env);
        }
        return instance;
    }


    private void registerSolrHome(Context ctx_java) throws NamingException {

        Context ctx_solr = ctx_java.createSubcontext("solr");
        ctx_solr.bind("home", new Reference("java.lang.String", "at.newmedialab.lmf.search.filters.SolrHomeFactory", null));

    }

    private void registerBeanManager(Context ctx_java) throws NamingException {
        ctx_java.bind("BeanManager", new Reference("javax.enterprise.inject.spi.BeanManager", "org.jboss.weld.resources.ManagerObjectFactory", null));
    }


}
