package org.apache.marmotta.platform.sparql.logging;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableSet;
import org.apache.marmotta.platform.core.logging.BaseLoggingModule;

import java.util.Collection;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SparqlLoggingModule extends BaseLoggingModule {

    public SparqlLoggingModule() {
    }

    /**
     * Return the default (logback) level used by this logging module. Should in most cases be INFO or WARN.
     *
     * @return
     */
    @Override
    public Level getDefaultLevel() {
        return Level.INFO;
    }

    /**
     * Return a unique identifier for this logging module. This identifier will e.g. be used in the configuration file
     * to store the configuration for this module. For this reason it should only consist of alpha-numeric characters
     * plus _ and _.
     *
     * @return a unique identifier for the module, suitable for use in the configuration file
     */
    @Override
    public String getId() {
        return "sparql";
    }

    /**
     * Return a human-readable name for this logging module. This name is used for displaying information about the
     * module to the user, e.g. in a configuration interface.
     *
     * @return a human-readable name for the module, suitable for displaying in a user interface
     */
    @Override
    public String getName() {
        return "SPARQL";
    }

    /**
     * Return a collection of packages covered by this logging module. This method should be used to group together
     * those packages that conceptually make up the functionality described by the logging module (e.g. "SPARQL").
     *
     * @return a collection of package names
     */
    @Override
    public Collection<String> getPackages() {
        return ImmutableSet.of(
                "org.apache.marmotta.platform.sparql",
                "org.apache.marmotta.kiwi.sparql",
                "org.openrdf.query"
        );
    }
}
