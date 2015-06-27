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

import static org.apache.marmotta.commons.sesame.rio.rdfhdt.RDFHDTConstants.MAGIC;
import info.aduna.io.IOUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.rdfhdt.hdt.exceptions.IllegalFormatException;
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

		rdfHandler.startRDF();
		DataInputStream input = new DataInputStream(new BufferedInputStream(in));
		parseGlobalInfo(input);

		rdfHandler.endRDF();
	}

	private void parseGlobalInfo(DataInputStream in) throws RDFParseException,
			IOException {
		parseMagic(in);
		byte type = parseType(in);
		byte globalInfoType = 1;
		if (type != globalInfoType) {
			reportFatalError("The global Information setion type should be: "
					+ globalInfoType);
		}
		String format = parseFormat(in);
		if(!RDFHDTConstants.HDT_CONTAINER.equals(format)){
			throw new IllegalFormatException("This software cannot open this version of HDT File");
		}
		
		parseProperties(in);
	}

	private void parseMagic(DataInputStream input) throws IOException,
			RDFParseException {

		// Check magic number
		byte[] magicNumber = IOUtil.readBytes(input, MAGIC.length);
		if (!Arrays.equals(magicNumber, MAGIC)) {
			reportFatalError("File does not contain a binary RDF document");
		}
	}

	private byte parseType(DataInputStream input) throws IOException {
		return input.readByte();
	}

	private String parseFormat(DataInputStream input) throws IOException{
		return parseString(input);

	}
	
	private Properties parseProperties(DataInputStream input) throws IOException{
		Properties properties = new Properties();
        String propertiesStr = this.parseString(input);   
        for(String item : propertiesStr.split(";")) {
        	int pos = item.indexOf('=');
        	if(pos!=-1) {
        		String property = item.substring(0, pos);
        		String value = item.substring(pos+1);
        		properties.put(property, value);
        	}
        }
        return properties;
	}
	
	private String parseString(DataInputStream input) throws IOException{
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		while (true) {
			int value = input.read();
			if (value == -1) {
				throw new EOFException();
			}
			if (value == '\0') {
				break;
			}
			buf.write(value);
		}
		return new String(buf.toByteArray()); // Uses default encoding
	}

	@Override
	public void parse(Reader reader, String baseURI) throws IOException,
			RDFParseException, RDFHandlerException {
		throw new UnsupportedOperationException();
		
	}

}
