package org.apache.marmotta.kiwi.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class FN_MARMOTTA {

    public static final String NAMESPACE = "http://marmotta.apache.org/vocabulary/sparql-functions#";

    /**
     * Recommended prefix for the XPath Functions namespace: "fn"
     */
    public static final String PREFIX = "mm";

    /**
     * An immutable {@link org.openrdf.model.Namespace} constant that represents the XPath
     * Functions namespace.
     */
    public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);



    public static final URI SEARCH_FULLTEXT;

    public static final URI QUERY_FULLTEXT;

    static {
        ValueFactory f = new ValueFactoryImpl();

        SEARCH_FULLTEXT = f.createURI(NAMESPACE,"fulltext-search");
        QUERY_FULLTEXT = f.createURI(NAMESPACE,"fulltext-query");
    }
}
