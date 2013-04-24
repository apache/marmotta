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

package org.apache.marmotta.kiwi.sparql.sail;

import org.apache.marmotta.kiwi.sail.KiWiSailConnection;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.sparql.persistence.KiWiSparqlConnection;
import org.openrdf.sail.*;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.sail.helpers.SailWrapper;

import java.sql.SQLException;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiSparqlSail extends NotifyingSailWrapper {

    private KiWiStore parent;

    public KiWiSparqlSail(NotifyingSail baseSail) {
        super(baseSail);

        this.parent = getRootSail(baseSail);

    }

    /**
     * Get the root sail in the wrapped sail stack
     * @param sail
     * @return
     */
    private KiWiStore getRootSail(Sail sail) {
        if(sail instanceof KiWiStore) {
            return (KiWiStore) sail;
        } else if(sail instanceof SailWrapper) {
            return getRootSail(((SailWrapper) sail).getBaseSail());
        } else {
            throw new IllegalArgumentException("root sail is not a KiWiStore or could not be found");
        }
    }

    /**
     * Get the root connection in a wrapped sail connection stack
     * @param connection
     * @return
     */
    private KiWiSailConnection getRootConnection(SailConnection connection) {
        if(connection instanceof KiWiSailConnection) {
            return (KiWiSailConnection) connection;
        } else if(connection instanceof SailConnectionWrapper) {
            return getRootConnection(((SailConnectionWrapper) connection).getWrappedConnection());
        } else {
            throw new IllegalArgumentException("root connection is not a KiWiSailConnection or could not be found");
        }
    }

    @Override
    public NotifyingSailConnection getConnection() throws SailException {
        NotifyingSailConnection connection = super.getConnection();
        KiWiSailConnection root   = getRootConnection(connection);

        try {
            return new KiWiSparqlSailConnection(connection, new KiWiSparqlConnection(root.getDatabaseConnection()), root.getValueFactory());
        } catch (SQLException e) {
            throw new SailException(e);
        }
    }
}
