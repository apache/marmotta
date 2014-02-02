package org.apache.marmotta.loader.api;

import org.apache.commons.cli.Option;
import org.apache.commons.configuration.Configuration;

import java.util.Collection;

/**
 * Specification for loader backends. Implementations will be injected using the Java ServiceLoader API
 * and provide singleton factories to create RDFHandlers.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface LoaderBackend {

    /**
     * Return a unique identifier for the loader; used for identifying the loader to choose on the command line
     * in case more than one loader implementation is available.
     * <p/>
     * Should match with the regular expression [a-z][a-z0-9]*
     *
     * @return
     */
    public String getIdentifier();

    /**
     * Create the RDFHandler to be used for bulk-loading, optionally using the configuration passed as argument.
     *
     * @return a newly created RDFHandler instance
     */
    public LoaderHandler createLoader(Configuration configuration);


    /**
     * Return any additional options that this backend offers (e.g. for connecting to a database etc).
     * If there are no additional options, return an empty collection.
     *
     * @return
     */
    public Collection<Option> getOptions();

}
