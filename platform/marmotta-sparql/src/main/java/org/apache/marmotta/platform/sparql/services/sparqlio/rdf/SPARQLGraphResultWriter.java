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
package org.apache.marmotta.platform.sparql.services.sparqlio.rdf;

import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class SPARQLGraphResultWriter {


    private OutputStream outputStream;


    private RDFFormat format;


    public SPARQLGraphResultWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        format = RDFFormat.RDFXML;
    }

    public SPARQLGraphResultWriter(OutputStream outputStream, String mimeType) {
        this.outputStream = outputStream;
        this.format = RDFFormat.forMIMEType(mimeType, RDFFormat.RDFXML);
    }


    public void write(GraphQueryResult result) throws IOException {
        Repository repository = new SailRepository(new MemoryStore());
        try {
            repository.initialize();

            RepositoryConnection con = repository.getConnection();
            for(Map.Entry<String,String> namespace : result.getNamespaces().entrySet()) {
                con.setNamespace(namespace.getKey(),namespace.getValue());
            }

            while(result.hasNext()) {
                con.add(result.next());
            }
            con.commit();

            RDFWriter writer = Rio.createWriter(format,outputStream);
            con.export(writer);
            con.close();
            repository.shutDown();

            outputStream.flush();
            outputStream.close();

        } catch (RepositoryException e) {
            throw new IOException("query result writing failed because there was an error while creating temporary triple store",e);
        } catch (QueryEvaluationException e) {
            throw new IOException("query result writing failed because query evaluation had a problem",e);
        } catch (RDFHandlerException e) {
            throw new IOException("query result writing failed because writer could not handle rdf data",e);
        }


    }
}
