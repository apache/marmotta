/*
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
package org.apache.marmotta.commons.sesame.rio.rdfhdt;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A parser for parsing RDF HDT.
 * <p/>
 * Author: Junyue Wang
 */
public class RDFHDTParser extends RDFParserBase {

	private static Logger log = LoggerFactory.getLogger(RDFHDTParser.class);

	/**
	 * Creates a new RDFParserBase that will use a
	 * {@link org.openrdf.model.impl.ValueFactoryImpl} to create RDF model
	 * objects.
	 */
	public RDFHDTParser() {
	}

	/**
	 * Creates a new RDFParserBase that will use the supplied ValueFactory to
	 * create RDF model objects.
	 *
	 * @param valueFactory
	 *            A ValueFactory.
	 */
	public RDFHDTParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/**
	 * Gets the RDF format that this parser can parse.
	 */
	@Override
	public RDFFormat getRDFFormat() {
		return RDFHDTFormat.FORMAT;
	}

	/**
	 * Parses the data from the supplied InputStream, using the supplied baseURI
	 * to resolve any relative URI references.
	 *
	 * @param in
	 *            The InputStream from which to read the data.
	 * @param baseURI
	 *            The URI associated with the data in the InputStream.
	 * @throws java.io.IOException
	 *             If an I/O error occurred while data was read from the
	 *             InputStream.
	 * @throws org.openrdf.rio.RDFParseException
	 *             If the parser has found an unrecoverable parse error.
	 * @throws org.openrdf.rio.RDFHandlerException
	 *             If the configured statement handler has encountered an
	 *             unrecoverable error.
	 */
	@Override
	public void parse(InputStream in, String baseURI) throws IOException,
			RDFParseException, RDFHandlerException {
		Preconditions.checkNotNull(baseURI);

		setBaseURI(baseURI);

		HDT hdt = HDTManager.loadHDT(in, null);
		rdfHandler.startRDF();

		// Search pattern: Empty string means "any"
		IteratorTripleString it;
		try {
			it = hdt.search("", "", "");

			while (it.hasNext()) {
				TripleString ts = it.next();
				System.out.println(ts);
				Statement statement = parseStatement(ts);
				rdfHandler.handleStatement(statement);
			}
		} catch (NotFoundException e) {
			throw new IOException(e);
		}

		rdfHandler.endRDF();

	}

	private Statement parseStatement(TripleString ts) throws RDFParseException,
			RDFHandlerException {
		Value subject = this.parseValue(ts.getSubject());
		Value predicate = this.parseValue(ts.getPredicate());
		Value object = this.parseValue(ts.getObject());

		Resource subj = null;
		if (subject instanceof Resource) {
			subj = (Resource) subject;
		} else {
			reportFatalError("Invalid subject type: " + subject);
		}

		URI pred = null;
		if (predicate instanceof URI) {
			pred = (URI) predicate;
		} else {
			reportFatalError("Invalid predicate type: " + predicate);
		}

		if (object == null) {
			reportFatalError("Invalid object type: " + null);
		}

		return createStatement(subj, pred, object);

	}

	private Value parseValue(CharSequence chars) throws RDFParseException {
		String str = chars.toString();
		char firstChar = chars.charAt(0);
		if (firstChar == '_') {
			return createBNode(str);
		} else if (firstChar == '"') {
			return createLiteral(str, null, null, -1, -1);
		} else {
			return createURI(str);
		}
	}

	@Override
	public void parse(Reader reader, String baseURI) throws IOException,
			RDFParseException, RDFHandlerException {
		throw new UnsupportedOperationException();

	}
	
	public static void main(String[] args){
		
		
		
		
	}
}
