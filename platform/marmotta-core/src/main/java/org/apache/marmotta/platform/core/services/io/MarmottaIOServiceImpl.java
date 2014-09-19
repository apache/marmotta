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
package org.apache.marmotta.platform.core.services.io;

import org.apache.marmotta.platform.core.api.io.MarmottaIOService;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * User: Thomas Kurz
 * Date: 18.02.11
 * Time: 10:41
 */
@ApplicationScoped
public class MarmottaIOServiceImpl implements MarmottaIOService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void initialise() {
        log.info("initialising Apache Marmotta I/O service ...");

        log.info(" - available parsers: {}", Arrays.toString(getAcceptTypes().toArray()));
        log.info(" - available writers: {}", Arrays.toString(getProducedTypes().toArray()));
    }

	/**
	 * returns a list of all mimetypes which can be parsed by implemented parsers
	 * @return
	 */
	@Override
	public List<String> getAcceptTypes() {
        Set<String> acceptTypes = new LinkedHashSet<String>();
        for(RDFFormat format : RDFParserRegistry.getInstance().getKeys()) {
            // Ignore binary formats
            if(format.hasCharset()) {
                acceptTypes.addAll(format.getMIMETypes());
            }
        }
        return new ArrayList<String>(acceptTypes);
	}

	/**
	 * returns a list of all mimetypes which can be produced by implemented serializers
	 * @return
	 */
	@Override
	public List<String> getProducedTypes() {
	    Set<String> producedTypes = new LinkedHashSet<String>();
        for(RDFFormat format : RDFWriterRegistry.getInstance().getKeys()) {
            // Ignore binary formats
            if(format.hasCharset()) {
                producedTypes.addAll(format.getMIMETypes());
            }
        }
        return new ArrayList<String>(producedTypes);
	}

	/**
	 * returns a serializer for a given mimetype; null if no serializer defined
	 * @param mimetype
	 * @return
	 */
	@Override
	public RDFFormat getSerializer(String mimetype) {
		return Rio.getWriterFormatForMIMEType(mimetype);
	}

	/**
	 * returns a parser for a given mimetype; null if no parser defined
	 * @param mimetype
	 * @return
	 */
	@Override
	public RDFFormat getParser(String mimetype) {
		return Rio.getParserFormatForMIMEType(mimetype);
	}
	
}
