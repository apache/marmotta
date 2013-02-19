/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.sesame.rio.rdfa;

import fi.tikesos.rdfa.core.datatype.Component;
import fi.tikesos.rdfa.core.datatype.Language;
import fi.tikesos.rdfa.core.datatype.Literal;
import fi.tikesos.rdfa.core.exception.ErrorHandler;
import fi.tikesos.rdfa.core.parser.sax.SAXRDFaParser;
import fi.tikesos.rdfa.core.profile.ProfileHandler;
import fi.tikesos.rdfa.core.profile.SimpleProfileHandler;
import fi.tikesos.rdfa.core.triple.TripleSink;
import fi.tikesos.rdfa.core.util.NullEntityResolver;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * A Sesame RDFa parser based on the Tikesos parser.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class RDFaParser extends RDFParserBase implements TripleSink, ErrorHandler {

    private Logger log = LoggerFactory.getLogger(RDFaParser.class);

    private static ProfileHandler profileHandler = null;

    private HashMap<String,Resource> anonResourceMap;

    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    public RDFaParser() {
        anonResourceMap = new HashMap<String, Resource>();
    }

    /**
     * Creates a new RDFParserBase that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public RDFaParser(ValueFactory valueFactory) {
        super(valueFactory);
        anonResourceMap = new HashMap<String, Resource>();
    }

    /**
     * Gets the RDF format that this parser can parse.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.RDFA;
    }

    /**
     * Parses the data from the supplied InputStream, using the supplied baseURI
     * to resolve any relative URI references.
     *
     * @param in      The InputStream from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                             If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                             If the configured statement handler has encountered an
     *                             unrecoverable error.
     */
    @Override
    public void parse(InputStream in, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if(baseURI != null) {
            setBaseURI(baseURI);
        }

        XMLReader reader;
        try {
            reader = XMLReaderFactory.createXMLReader();
            reader.setFeature("http://xml.org/sax/features/validation",
                    Boolean.FALSE);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                    Boolean.TRUE);
            reader.setEntityResolver(new NullEntityResolver());

            if (profileHandler == null) {
                profileHandler = new RDFaProfileHandler();
            }

            try {
                ErrorHandler errorHandler = this;
                ContentHandler parser = new SAXRDFaParser(baseURI, this,
                        profileHandler, errorHandler, fi.tikesos.rdfa.core.parser.RDFaParser.UNKNOWN_XML);
                reader.setContentHandler(parser);
                reader.parse(new InputSource(in));
            } catch (IOException e) {
                throw new RDFParseException(e);
            } catch (URISyntaxException e) {
                throw new RDFParseException(e);
            }
        } catch (SAXException e) {
            throw new RDFParseException(e);
        }

    }

    /**
     * Parses the data from the supplied Reader, using the supplied baseURI to
     * resolve any relative URI references.
     *
     * @param in  The Reader from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                             If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                             If the configured statement handler has encountered an
     *                             unrecoverable error.
     */
    @Override
    public void parse(Reader in, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if(baseURI != null) {
            setBaseURI(baseURI);
        }

        XMLReader reader;
        try {
            reader = XMLReaderFactory.createXMLReader();
            reader.setFeature("http://xml.org/sax/features/validation",
                    Boolean.FALSE);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                    Boolean.TRUE);
            reader.setEntityResolver(new NullEntityResolver());

            if (profileHandler == null) {
                profileHandler = new RDFaProfileHandler();
            }

            try {
                ErrorHandler errorHandler = this;
                ContentHandler parser = new SAXRDFaParser(baseURI, this,
                        profileHandler, errorHandler, fi.tikesos.rdfa.core.parser.RDFaParser.UNKNOWN_XML);
                reader.setContentHandler(parser);
                reader.parse(new InputSource(in));
            } catch (IOException e) {
                throw new RDFParseException(e);
            } catch (URISyntaxException e) {
                throw new RDFParseException(e);
            }
        } catch (Exception e) {
            throw new RDFParseException(e);
        }
    }



    @Override
    public void startRelativeTripleCaching() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stopRelativeTripleCaching() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void generateTriple(Component component, Component component1, Component component2) {
        generateTriple(component.getValue(),component1.getValue(),component2.getValue());
    }

    @Override
    public void generateTriple(String s, String s1, String s2) {
        try {
            Resource subject = createResource(s);
            URI property = createURI(s1);
            Resource object = createResource(s2);

            Statement statement = createStatement(subject,property,object);

            rdfHandler.handleStatement(statement);
        } catch (RDFParseException ex) {
            throw new IllegalArgumentException(ex);
        } catch (RDFHandlerException e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void generateTripleLiteral(Component component, Component component1, Literal literal, Language language, Component component2) {
        generateTripleLiteral(component.getValue(),component1.getValue(),literal.getValue(), language != null ? language.getValue() : null, component2 != null ? component2.getValue() : null);
    }

    @Override
    public void generateTripleLiteral(String subject, String predicate, String value, String language, String datatype) {
        try {
            Resource r_subject = createResource(subject);
            URI r_property = createURI(predicate);
            Value r_object;

            if(datatype != null) {
                r_object = createLiteral(value,null,createURI(datatype));
            } else if(language != null) {
                r_object = createLiteral(value,language,null);
            } else {
                r_object = createLiteral(value,null,null);
            }

            Statement statement = createStatement(r_subject,r_property,r_object);

            rdfHandler.handleStatement(statement);
        } catch (RDFParseException ex) {
            throw new IllegalArgumentException(ex);
        } catch (RDFHandlerException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public void warning(Exception exception) {
        log.warn(exception.getMessage());
    }

    @Override
    public void fatalError(Exception exception) {
        log.error(exception.getMessage());

        throw new IllegalStateException(exception);
    }

    private Resource createResource(String value) throws RDFParseException {
        Resource resource;
        if(value.startsWith("_:")) {
            if(anonResourceMap.containsKey(value)) {
                resource = anonResourceMap.get(value);
            } else {
                resource = createBNode();
                anonResourceMap.put(value,resource);
            }
        } else {
            resource = createURI(value);
        }
        return resource;
    }


    private static class LocalProfileHandler extends SimpleProfileHandler {

    }
}
