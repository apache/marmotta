package org.apache.marmotta.commons.sesame.rio.rdfhdt;

import java.util.Arrays;

import org.openrdf.rio.RDFFormat;

/**
 * HDT (Header, Dictionary, Triples) is a compact data structure and binary
 * serialization format for RDF that keeps big datasets compressed to save space
 * while maintaining search and browse operations without prior decompression.
 * <p/>
 * Author: Junyue Wang
 */
public class RDFHDTFormat {

	public static final RDFFormat FORMAT = new RDFFormat("RDFHDT",
			Arrays.asList("application/rdf+hdt"), null, Arrays.asList("hdt"),
			false, false);

}
