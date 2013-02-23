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
package org.apache.marmotta.ldpath.backend.sesame;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class SesameConnectionBackend extends AbstractSesameBackend {

	private final RepositoryConnection connection;
	private final ValueFactory valueFactory;

	public SesameConnectionBackend(RepositoryConnection connection) {
		this.connection = connection;
		valueFactory = connection.getValueFactory();
	}

	@Override
	public Value createLiteral(String content) {
		return createLiteralInternal(valueFactory, content);
	}

	@Override
	public Value createLiteral(String content, Locale language, URI type) {
		return createLiteralInternal(valueFactory, content, language, type);
	}

	@Override
	public Value createURI(String uri) {
		return createURIInternal(valueFactory, uri);
	}

	@Override
	public Collection<Value> listObjects(Value subject, Value property) {
		try {
			return listObjectsInternal(connection, (Resource) subject, (org.openrdf.model.URI) property);
		} catch (RepositoryException e) {
			throw new RuntimeException(
					"error while querying Sesame repository!", e);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(String.format(
					"Subject needs to be a URI or blank node, property a URI node "
							+ "(types: [subject: %s, property: %s])",
					debugType(subject), debugType(property)), e);
		}
	}

	@Override
	public Collection<Value> listSubjects(Value property, Value object) {
        try {
        	return listSubjectsInternal(connection, (org.openrdf.model.URI) property, object);
        } catch (RepositoryException e) {
            throw new RuntimeException("error while querying Sesame repository!",e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                    "Property needs to be a URI node (property type: %s)",
                    isURI(property)?"URI":isBlank(property)?"bNode":"literal"),e);
        }
	}

	public static SesameConnectionBackend withConnection(RepositoryConnection connection) {
		return new SesameConnectionBackend(connection);
	}
	
}
