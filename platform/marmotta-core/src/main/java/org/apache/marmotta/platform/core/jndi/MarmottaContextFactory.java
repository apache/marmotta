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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import java.util.Hashtable;

/**
 * A custom LMF JNDI implementation returning an LMFContext. This implementation should be considerably faster than
 * the one provided by the application server, and in addition requires no configuration on the server side.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaContextFactory implements InitialContextFactory {

    private static Logger log = LoggerFactory.getLogger(MarmottaContextFactory.class);

    private static MarmottaContextFactory instance;

    public MarmottaContextFactory() {
        log.info("JNDI: initialising Apache Marmotta Context Factory ...");

    }

    public static MarmottaContextFactory getInstance() {
        if(instance == null) {
            instance = new MarmottaContextFactory();
        }
        return instance;
    }


    @Override
    public Context getInitialContext(Hashtable<?, ?> hashtable) throws NamingException {

        Hashtable<Object,Object> env = new Hashtable<Object, Object>();
        env.put("jndi.syntax.direction", "left_to_right");
        env.put("jndi.syntax.separator","/");

        env.putAll(hashtable);

        return MarmottaInitialContext.getInstance(env);
    }
}
