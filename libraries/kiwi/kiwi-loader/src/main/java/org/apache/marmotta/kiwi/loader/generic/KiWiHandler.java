package org.apache.marmotta.kiwi.loader.generic;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.kiwi.loader.KiWiLoaderConfiguration;
import org.apache.marmotta.kiwi.model.rdf.*;
import org.apache.marmotta.kiwi.persistence.KiWiConnection;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A fast-lane RDF import handler that allows bulk-importing triples into a KiWi triplestore. It directly accesses
 * the database using a KiWiConnection. Note that certain configuration options will make the import "unsafe"
 * because they turn off expensive existence checks. If you are not careful and import the same data twice, this
 * might mean duplicate entries in the database.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class KiWiHandler implements RDFHandler {

    private static Logger log = LoggerFactory.getLogger(KiWiHandler.class);

    protected KiWiConnection connection;
    protected KiWiStore store;

    protected long count = 0;
    protected long start = 0;
    protected long previous = 0;

    protected KiWiLoaderConfiguration config;

    protected LoadingCache<Literal, KiWiLiteral> literalCache;
    protected LoadingCache<URI, KiWiUriResource> uriCache;
    protected LoadingCache<BNode, KiWiAnonResource> bnodeCache;
    protected LoadingCache<String,Locale> localeCache;

    // if non-null, all imported statements will have this context (regardless whether they specified a different context)
    private KiWiResource overrideContext;

    public KiWiHandler(KiWiStore store, KiWiLoaderConfiguration config) {
        this.config     = config;
        this.store      = store;

        this.literalCache = CacheBuilder.newBuilder()
                .maximumSize(1000000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<Literal, KiWiLiteral>() {
                    @Override
                    public KiWiLiteral load(Literal l) throws Exception {
                        return createLiteral(l);
                    }
                });

        this.uriCache = CacheBuilder.newBuilder()
                .maximumSize(5000000)
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build(new CacheLoader<URI, KiWiUriResource>() {
                    @Override
                    public KiWiUriResource load(URI key) throws Exception {
                        return createURI(key.stringValue());
                    }
                });

        this.bnodeCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<BNode, KiWiAnonResource>() {
                    @Override
                    public KiWiAnonResource load(BNode key) throws Exception {
                        return createBNode(key.stringValue());
                    }
                });

        this.localeCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<String, Locale>() {
                    @Override
                    public Locale load(String lang) throws Exception {
                        try {
                            Locale.Builder builder = new Locale.Builder();
                            builder.setLanguageTag(lang);
                            return builder.build();
                        } catch (IllformedLocaleException ex) {
                            log.warn("malformed language literal (language: {})", lang);
                            return null;
                        }
                    }
                });

    }

    /**
     * Signals the end of the RDF data. This method is called when all data has
     * been reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            throw new RDFHandlerException(e);
        }

        log.info("KiWiLoader: RDF bulk import of {} triples finished after {} ms", count, System.currentTimeMillis() - start);
    }

    /**
     * Signals the start of the RDF data. This method is called before any data
     * is reported.
     *
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        log.info("KiWiLoader: starting RDF bulk import");
        try {
            this.connection = store.getPersistence().getConnection();
        } catch (SQLException e) {
            throw new RDFHandlerException(e);
        }

        this.start = System.currentTimeMillis();
        this.previous = System.currentTimeMillis();

        if(config.getContext() != null) {
            try {
                this.overrideContext = (KiWiResource)convertNode(new URIImpl(config.getContext()));
            } catch (ExecutionException e) {
                log.error("could not create/load resource",e);
            }
        }
    }

    /**
     * Handles a namespace declaration/definition. A namespace declaration
     * associates a (short) prefix string with the namespace's URI. The prefix
     * for default namespaces, which do not have an associated prefix, are
     * represented as empty strings.
     *
     * @param prefix The prefix for the namespace, or an empty string in case of a
     *               default namespace.
     * @param uri    The URI that the prefix maps to.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        try {
            connection.storeNamespace(new KiWiNamespace(prefix,uri));
        } catch (SQLException e) {
            throw new RDFHandlerException(e);
        }
    }

    /**
     * Handles a statement.
     *
     * @param st The statement.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        try {
            KiWiResource subject = (KiWiResource)convertNode(st.getSubject());
            KiWiUriResource predicate = (KiWiUriResource)convertNode(st.getPredicate());
            KiWiNode object = convertNode(st.getObject());
            KiWiResource context;

            if(this.overrideContext != null) {
                context = this.overrideContext;
            } else {
                context = (KiWiResource)convertNode(st.getContext());
            }

            KiWiTriple result = new KiWiTriple(subject,predicate,object,context);
            if(config.isStatementExistanceCheck()) {
                result.setId(connection.getTripleId(subject, predicate, object, context, true));
            }
            storeTriple(result);

        } catch (SQLException | ExecutionException e) {
            throw new RDFHandlerException(e);
        }

    }


    private KiWiNode convertNode(Value value) throws ExecutionException {
        if(value == null) {
            return null;
        } else if(value instanceof KiWiNode) {
            return (KiWiNode)value;
        } else if(value instanceof URI) {
            return uriCache.get((URI)value);
        } else if(value instanceof BNode) {
            return bnodeCache.get(((BNode)value));
        } else if(value instanceof Literal) {
            Literal l = (Literal)value;
            return literalCache.get(l);
        } else {
            throw new IllegalArgumentException("the value passed as argument does not have the correct type");
        }

    }

    protected KiWiLiteral createLiteral(Literal l) throws ExecutionException {
        String value = l.getLabel();
        String lang  = l.getLanguage();
        URI    type  = l.getDatatype();


        Locale locale;
        if(lang != null) {
            locale = localeCache.get(lang);
        } else {
            locale = null;
        }
        if(locale == null) {
            lang = null;
        }


        KiWiLiteral result;
        final KiWiUriResource rtype = type==null ? null : uriCache.get(type);

        try {

            try {
                // differentiate between the different types of the value
                if (type == null) {
                    // FIXME: MARMOTTA-39 (this is to avoid a NullPointerException in the following if-clauses)
                    result = connection.loadLiteral(sanitizeString(value.toString()), lang, rtype);

                    if(result == null) {
                        result = new KiWiStringLiteral(sanitizeString(value.toString()), locale, rtype);
                    }
                } else if(type.equals(Namespaces.NS_XSD+"dateTime")) {
                    // parse if necessary
                    final Date dvalue = DateUtils.parseDate(value.toString());

                    result = connection.loadLiteral(dvalue);

                    if(result == null) {
                        result= new KiWiDateLiteral(dvalue, rtype);
                    }
                } else if(type.equals(Namespaces.NS_XSD+"integer") || type.equals(Namespaces.NS_XSD+"long")) {
                    long ivalue = Long.parseLong(value.toString());

                    result = connection.loadLiteral(ivalue);

                    if(result == null) {
                        result= new KiWiIntLiteral(ivalue, rtype);
                    }
                } else if(type.equals(Namespaces.NS_XSD+"double") || type.equals(Namespaces.NS_XSD+"float")) {
                    double dvalue = Double.parseDouble(value.toString());

                    result = connection.loadLiteral(dvalue);

                    if(result == null) {
                        result= new KiWiDoubleLiteral(dvalue, rtype);
                    }
                } else if(type.equals(Namespaces.NS_XSD+"boolean")) {
                    boolean bvalue = Boolean.parseBoolean(value.toString());

                    result = connection.loadLiteral(bvalue);

                    if(result == null) {
                        result= new KiWiBooleanLiteral(bvalue, rtype);
                    }
                } else {
                    result = connection.loadLiteral(sanitizeString(value.toString()), lang, rtype);

                    if(result == null) {
                        result = new KiWiStringLiteral(sanitizeString(value.toString()), locale, rtype);
                    }
                }
            } catch(IllegalArgumentException ex) {
                // malformed number or date
                log.warn("malformed argument for typed literal of type {}: {}", rtype.stringValue(), value);
                KiWiUriResource mytype = createURI(Namespaces.NS_XSD+"string");

                result = connection.loadLiteral(sanitizeString(value.toString()), lang, mytype);

                if(result == null) {
                    result = new KiWiStringLiteral(sanitizeString(value.toString()), locale, mytype);
                }

            }

            if(result.getId() == null) {
                storeNode(result);
            }

            return result;


        } catch (SQLException e) {
            log.error("database error, could not load literal",e);
            throw new IllegalStateException("database error, could not load literal",e);
        }
    }

    protected KiWiUriResource createURI(String uri) {
        try {
            // first look in the registry for newly created resources if the resource has already been created and
            // is still volatile
            KiWiUriResource result = connection.loadUriResource(uri);

            if(result == null) {
                result = new KiWiUriResource(uri);

                storeNode(result);

            }
            if(result.getId() == null) {
                log.error("node ID is null!");
            }

            return result;
        } catch (SQLException e) {
            log.error("database error, could not load URI resource",e);
            throw new IllegalStateException("database error, could not load URI resource",e);
        }
    }

    protected KiWiAnonResource createBNode(String nodeID) {
        try {
            // first look in the registry for newly created resources if the resource has already been created and
            // is still volatile
            KiWiAnonResource result = connection.loadAnonResource(nodeID);

            if(result == null) {
                result = new KiWiAnonResource(nodeID);
                storeNode(result);
            }
            if(result.getId() == null) {
                log.error("node ID is null!");
            }

            return result;
        } catch (SQLException e) {
            log.error("database error, could not load anonymous resource",e);
            throw new IllegalStateException("database error, could not load anonymous resource",e);
        }
    }


    protected void storeNode(KiWiNode node) throws SQLException {
        connection.storeNode(node, false);
    }

    protected void storeTriple(KiWiTriple result) throws SQLException {
        connection.storeTriple(result);

        count++;

        if(count % config.getCommitBatchSize() == 0) {
            connection.commit();

            log.info("imported {} triples ({}/sec)", count, (config.getCommitBatchSize() * 1000) / (System.currentTimeMillis() - previous));
            previous = System.currentTimeMillis();
        }
    }


    /**
     * Handles a comment.
     *
     * @param comment The comment.
     * @throws org.openrdf.rio.RDFHandlerException
     *          If the RDF handler has encountered an unrecoverable error.
     */
    @Override
    public void handleComment(String comment) throws RDFHandlerException {
    }


    private static String sanitizeString(String in) {
        // clean up illegal characters
        return in.replaceAll("[\\00]","");
    }
}
