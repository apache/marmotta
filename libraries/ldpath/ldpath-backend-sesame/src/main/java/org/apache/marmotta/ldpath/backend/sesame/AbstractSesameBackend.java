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
package org.apache.marmotta.ldpath.backend.sesame;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;


import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractSesameBackend extends SesameValueBackend implements RDFBackend<Value> {

	private static final Logger log = LoggerFactory.getLogger(AbstractSesameBackend.class);

	protected org.openrdf.model.URI createURIInternal(final ValueFactory valueFactory, String uri) {
		return valueFactory.createURI(uri);
	}

	protected Literal createLiteralInternal(final ValueFactory valueFactory, String content) {
		log.debug("creating literal with content \"{}\"",content);
		return valueFactory.createLiteral(content);
	}

	protected Literal createLiteralInternal(final ValueFactory valueFactory, String content,
			Locale language, URI type) {
		log.debug("creating literal with content \"{}\", language {}, datatype {}",new Object[]{content,language,type});
		if(language == null && type == null) {
			return valueFactory.createLiteral(content);
		} else if(type == null) {
			return valueFactory.createLiteral(content,language.getLanguage());
		} else  {
			return valueFactory.createLiteral(content, valueFactory.createURI(type.toString()));
		}
	}

	protected Collection<Value> listObjectsInternal(RepositoryConnection connection, Resource subject, org.openrdf.model.URI property)
			throws RepositoryException {
		Set<Value> result = new HashSet<Value>();
		RepositoryResult<Statement> qResult = connection.getStatements(subject, property, null, true);
		try {
			while(qResult.hasNext()) {
				result.add(qResult.next().getObject());
			}
		} finally {
			qResult.close();
		}
		return  result;
	}

	protected Collection<Value> listSubjectsInternal(final RepositoryConnection connection, org.openrdf.model.URI property, Value object)
			throws RepositoryException {
		Set<Value> result = new HashSet<Value>();
		RepositoryResult<Statement> qResult = connection.getStatements(null, property, object, true);
		try {
			while(qResult.hasNext()) {
				result.add(qResult.next().getSubject());
			}
		} finally {
			qResult.close();
		}
		return  result;
	}

	@Override
	public abstract Literal createLiteral(String content);

	@Override
	public abstract Literal createLiteral(String content, Locale language, URI type);

	@Override
	public abstract org.openrdf.model.URI createURI(String uri);

	@Override
	public abstract Collection<Value> listObjects(Value subject, Value property);

	@Override
	public abstract Collection<Value> listSubjects(Value property, Value object);

	@Override
	@Deprecated
	public boolean supportsThreading() {
		return false;
	}

	@Override
	@Deprecated
	public ThreadPoolExecutor getThreadPool() {
		return null;
	}


}
