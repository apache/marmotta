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
package org.apache.marmotta.platform.core.services.io;

import org.apache.marmotta.platform.core.api.io.LMFIOService;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Thomas Kurz
 * Date: 18.02.11
 * Time: 10:41
 */
@ApplicationScoped
public class LMFIOServiceImpl implements LMFIOService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private RDFParserRegistry parserRegistry;
    private RDFWriterRegistry writerRegistry;

    private List<String> acceptTypes;
    private List<String> producedTypes;

    @PostConstruct
    public void initialise() {
        log.info("initialising LMF I/O service ...");

        parserRegistry = RDFParserRegistry.getInstance();

        acceptTypes = new ArrayList<String>();
        for(RDFFormat format : parserRegistry.getKeys()) {
            acceptTypes.addAll(format.getMIMETypes());
        }
        log.info(" - available parsers: {}", Arrays.toString(acceptTypes.toArray()));

        writerRegistry = RDFWriterRegistry.getInstance();

        producedTypes = new ArrayList<String>();
        for(RDFFormat format : writerRegistry.getKeys()) {
            producedTypes.addAll(format.getMIMETypes());
        }
        log.info(" - available writers: {}", Arrays.toString(producedTypes.toArray()));



    }


	/**
	 * returns a list of all mimetypes which can be parsed by implemented parsers
	 * @return
	 */
	@Override
	public List<String> getAcceptTypes() {
		return acceptTypes;
	}

	/**
	 * returns a list of all mimetypes which can be produced by implemented serializers
	 * @return
	 */
	@Override
	public List<String> getProducedTypes() {
		return producedTypes;
	}

	/**
	 * returns a serializer for a given mimetype; null if no serializer defined
	 * @param mimetype
	 * @return
	 */
	@Override
	public RDFFormat getSerializer(String mimetype) {
		return writerRegistry.getFileFormatForMIMEType(mimetype);
	}

	/**
	 * returns a parser for a given mimetype; null if no parser defined
	 * @param mimetype
	 * @return
	 */
	@Override
	public RDFFormat getParser(String mimetype) {
		return parserRegistry.getFileFormatForMIMEType(mimetype);
	}
}
