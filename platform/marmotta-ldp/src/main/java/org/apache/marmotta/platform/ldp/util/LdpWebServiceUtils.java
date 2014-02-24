package org.apache.marmotta.platform.ldp.util;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.UnionIteration;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.commons.vocabulary.XSD;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * Created by jakob on 2/18/14.
 */
public class LdpWebServiceUtils {

    /**
     * Urify the Slug: header value, i.e. replace all non-url chars with a single dash.
     *
     * @param slugHeaderValue
     * @return the slugHeaderValue "urified"
     */
    public static String urify(String slugHeaderValue) {
        return slugHeaderValue
                // Replace non-url chars with '-'
                .replaceAll("[^\\w]+", "-");
    }

    public static void exportIteration(RDFWriter writer, URI subject, CloseableIteration<Statement, RepositoryException> iteration) throws RDFHandlerException, RepositoryException {
        writer.startRDF();

        writer.handleNamespace(LDP.PREFIX, LDP.NAMESPACE);
        writer.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
        writer.handleNamespace(XSD.PREFIX, XSD.NAMESPACE);
        writer.handleNamespace(DCTERMS.PREFIX, DCTERMS.NAMESPACE);

        writer.handleNamespace("", subject.stringValue());

        while (iteration.hasNext()) {
            writer.handleStatement(iteration.next());
        }

        writer.endRDF();
    }
}
