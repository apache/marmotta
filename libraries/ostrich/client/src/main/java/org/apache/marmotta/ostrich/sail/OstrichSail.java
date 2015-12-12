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

package org.apache.marmotta.ostrich.sail;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class OstrichSail extends NotifyingSailBase implements Sail {
    private static Logger log = LoggerFactory.getLogger(OstrichSail.class);

    private OstrichValueFactory valueFactory = new OstrichValueFactory();

    private String host;
    private int port;

    public OstrichSail(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Do store-specific operations to initialize the store. The default
     * implementation of this method does nothing.
     */
    @Override
    protected void initializeInternal() throws SailException {
        log.info("Initialising CMarmotta Sail (host={}, port={})", host, port);
    }

    @Override
    protected NotifyingSailConnection getConnectionInternal() throws SailException {
        return new OstrichSailConnection(this, host, port);
    }

    /**
     * Do store-specific operations to ensure proper shutdown of the store.
     */
    @Override
    protected void shutDownInternal() throws SailException {

    }

    /**
     * Checks whether this Sail object is writable, i.e. if the data contained in
     * this Sail object can be changed.
     */
    @Override
    public boolean isWritable() throws SailException {
        return true;
    }

    /**
     * Gets a ValueFactory object that can be used to create URI-, blank node-,
     * literal- and statement objects.
     *
     * @return a ValueFactory object for this Sail object.
     */
    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }
}
