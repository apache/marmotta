/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.ldcache.sail.test.dummy;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;

public class DummyProvider implements DataProvider {

	@Override
	public String getName() {
		return "Dummy";
	}

	@Override
	public String[] listMimeTypes() {
		return new String[] {"application/dummy"};
	}

	@Override
	public ClientResponse retrieveResource(String resource, LDClientService client, Endpoint endpoint) throws DataRetrievalException {
        String filename = resource.substring("http://remote/".length()) + ".ttl";

        try {
            Repository triples = new SailRepository(new MemoryStore());
            triples.initialize();

            RepositoryConnection con = triples.getConnection();
            try {
                con.begin();

                con.add(DummyProvider.class.getResourceAsStream(filename), resource, RDFFormat.TURTLE);

                con.commit();
            } catch(RepositoryException ex) {
                con.rollback();
            } catch (RDFParseException e) {
                throw new DataRetrievalException("could not parse resource data for file "+filename);
            } catch (IOException e) {
                throw new DataRetrievalException("could not load resource data for file "+filename);
            } finally {
                con.close();
            }

            ClientResponse response = new ClientResponse(triples);

            return response;
        } catch (RepositoryException e) {
            throw new DataRetrievalException("could not load resource data for file "+filename);
        }

    }


}
