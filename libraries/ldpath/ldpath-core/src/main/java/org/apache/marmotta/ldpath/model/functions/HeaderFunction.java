package org.apache.marmotta.ldpath.model.functions;

import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Extract the values of a response header from the RDFBackend
 * @param <Node>
 * Author: Chris Beer <cabeer@stanford.edu>
 */
public class HeaderFunction<Node> extends SelectorFunction<Node> {
    @Override
    protected String getLocalName() {
        return "header";
    }

    @Override
    public Collection<Node> apply(RDFBackend<Node> backend, Node context, @SuppressWarnings("unchecked") Collection<Node>... args) throws IllegalArgumentException {

        LinkedList<Node> result = new LinkedList<Node>();

        for (Collection<Node> arg : args) {
            for (Node node : arg) {
                result.addAll(backend.getHeaders(context, node));
            }
        }

        return result;
    }

    @Override
    public String getSignature() {
        return "fn:header(header : String) : String";
    }

    @Override
    public String getDescription() {
        return "Get a response header from the context resource";
    }
}
