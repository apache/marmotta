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

package org.apache.marmotta.loader.rio;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.RDFParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * A specialised RDF parser for Geonames data. Geonames dumps are usually a big text file with a complete
 * RDF/XML document per line, so the parser will split the lines and then send each line individually to a
 * wrapped
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class GeonamesParser extends RDFParserBase implements ParseErrorListener {

    private static Logger log = LoggerFactory.getLogger(GeonamesParser.class);

    private RDFParser lineParser;

    private int lineNumber = 0;

    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    public GeonamesParser() {
        super();

        lineParser = Rio.createParser(RDFFormat.RDFXML);
        lineParser.setParseErrorListener(this);
        lineParser.setParserConfig(getParserConfig());
    }

    /**
     * Creates a new RDFParserBase that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public GeonamesParser(ValueFactory valueFactory) {
        super(valueFactory);

        lineParser = Rio.createParser(RDFFormat.RDFXML);
        lineParser.setParseErrorListener(this);
        lineParser.setValueFactory(valueFactory);
        lineParser.setParserConfig(getParserConfig());
    }



    /**
     * Gets the RDF format that this parser can parse.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return GeonamesFormat.FORMAT;
    }

    @Override
    public void setRDFHandler(RDFHandler handler) {
        super.setRDFHandler(handler);

        lineParser.setRDFHandler(handler);
    }


    @Override
    public void setParserConfig(ParserConfig config) {
        super.setParserConfig(config);

        if(lineParser != null) {
            // called by super.init when lineParser is still null
            lineParser.setParserConfig(config);
        }
    }

    @Override
    public void setValueFactory(ValueFactory valueFactory) {
        super.setValueFactory(valueFactory);

        if(lineParser != null) {
            // called by super.init when lineParser is still null
            lineParser.setValueFactory(valueFactory);
        }
    }


    /**
     * Parses the data from the supplied InputStream, using the supplied baseURI
     * to resolve any relative URI references.
     *
     * @param in      The InputStream from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException                 If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException   If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException If the configured statement handler has encountered an
     *                                             unrecoverable error.
     */
    @Override
    public void parse(InputStream in, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        LineIterator it = IOUtils.lineIterator(in, RDFFormat.RDFXML.getCharset());
        try {
            while(it.hasNext()) {
                lineNumber++;

                String line = it.nextLine();
                if(lineNumber % 2 == 0) {
                    // only odd line numbers contain triples
                    StringReader buffer = new StringReader(line);
                    lineParser.parse(buffer, baseURI);
                }
            }
        } finally {
            it.close();
        }
    }

    /**
     * Parses the data from the supplied Reader, using the supplied baseURI to
     * resolve any relative URI references.
     *
     * @param reader  The Reader from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException                 If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException   If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException If the configured statement handler has encountered an
     *                                             unrecoverable error.
     */
    @Override
    public void parse(Reader reader, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        LineIterator it = IOUtils.lineIterator(reader);
        try {
            while(it.hasNext()) {
                lineNumber++;

                String line = it.nextLine();
                if(lineNumber % 2 == 1) {
                    // only odd line numbers contain triples
                    StringReader buffer = new StringReader(line);
                    lineParser.parse(buffer, baseURI);
                }
            }
        } finally {
            it.close();
        }
    }


    /**
     * Reports a warning from the parser. Warning messages are generated
     * by the parser when it encounters data that is syntactically correct
     * but which is likely to be a typo. Examples are the use of unknown
     * or deprecated RDF URIs, e.g. <tt>rdfs:Property</tt> instead of
     * <tt>rdf:Property</tt>.
     *
     * @param msg    A warning message.
     * @param lineNo A line number related to the warning, or -1 if not
     *               available or applicable.
     * @param colNo  A column number related to the warning, or -1 if not
     */
    @Override
    public void warning(String msg, int lineNo, int colNo) {
        if(getParseErrorListener() != null) {
            getParseErrorListener().warning(msg, lineNumber, colNo);
        } else {
            log.warn("{} [line {}, column {}]", msg, lineNumber, colNo);
        }
    }

    /**
     * Reports an error from the parser. Error messages are generated by
     * the parser when it encounters an error in the RDF document. The
     * parser will try its best to recover from the error and continue
     * parsing when <tt>stopAtFirstError</tt> has been set to
     * <tt>false</tt>.
     *
     * @param msg    A error message.
     * @param lineNo A line number related to the error, or -1 if not
     *               available or applicable.
     * @param colNo  A column number related to the error, or -1 if not
     *               available or applicable.
     * @see org.openrdf.rio.RDFParser#setStopAtFirstError
     */
    @Override
    public void error(String msg, int lineNo, int colNo) {
        if(getParseErrorListener() != null) {
            getParseErrorListener().error(msg, lineNumber, colNo);
        } else {
            log.error("{} [line {}, column {}]", msg, lineNumber, colNo);
        }
    }

    /**
     * Reports a fatal error from the parser. A fatal error is an error
     * of which the RDF parser cannot recover. The parser will stop parsing
     * directly after it reported the fatal error. Example fatal errors are
     * unbalanced start- and end-tags in an XML-encoded RDF document.
     *
     * @param msg    A error message.
     * @param lineNo A line number related to the error, or -1 if not
     *               available or applicable.
     * @param colNo  A column number related to the error, or -1 if not
     */
    @Override
    public void fatalError(String msg, int lineNo, int colNo) {
        if(getParseErrorListener() != null) {
            getParseErrorListener().fatalError(msg, lineNumber, colNo);
        } else {
            log.error("{} [line {}, column {}]", msg, lineNumber, colNo);
        }
    }
}
