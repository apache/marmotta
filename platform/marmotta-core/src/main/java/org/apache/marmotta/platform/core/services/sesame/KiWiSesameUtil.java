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
package org.apache.marmotta.platform.core.services.sesame;

import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.util.Date;

/**
 * Some utils to work with KiWi model and Sesame
 * 
 * @author Sebastian Schaffert
 */
public class KiWiSesameUtil {


    /**
     * Get the last modification of the set of triples passed as argument.
     *
     * @deprecated this class will be removed before the 2.6 release, the helper function should be moved elsewhere
     * @return date
     * @throws RepositoryException
     */
    public static Date lastModified(Resource resource, RepositoryConnection conn) throws RepositoryException {
        Date last_modified = new Date(0);
        RepositoryResult<Statement> triples = conn.getStatements(resource, null, null, false);
        try {
            while (triples.hasNext()) {
                Statement triple = triples.next();
                if (triple instanceof KiWiTriple) {
                    KiWiTriple t = (KiWiTriple) triple;
                    if (t.getCreated().getTime() > last_modified.getTime()) {
                        last_modified = t.getCreated();
                    }
                }
            }
        } finally {
            triples.close();
        }
        return last_modified;
    }

}
