package org.apache.marmotta.platform.core.api.logging;

import ch.qos.logback.classic.Level;
import org.apache.marmotta.platform.core.model.logging.LoggingOutput;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of a LoggingModule provides information about a component for which to provide logging
 * facilities. It is an abstraction from the "logging by package" concept used by lower-level logging frameworks.
 * A user can select to change the logging configuration for a module, resulting in all packages managed by this
 * module to be logged according to the new configuration. This allows to group packages together that conceptually
 * belong together.
 * <p/>
 * LoggingModule providers are injected by the LoggingService using CDI injection. As such they should be proper CDI
 * bearns and probably live in application scope.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface LoggingModule {


    /**
     * Return a unique identifier for this logging module. This identifier will e.g. be used in the configuration file
     * to store the configuration for this module. For this reason it should only consist of alpha-numeric characters
     * plus _ and _.
     *
     * @return a unique identifier for the module, suitable for use in the configuration file
     */
    public String getId();


    /**
     * Return a human-readable name for this logging module. This name is used for displaying information about the
     * module to the user, e.g. in a configuration interface.
     *
     * @return a human-readable name for the module, suitable for displaying in a user interface
     */
    public String getName();


    /**
     * Return a collection of packages covered by this logging module. This method should be used to group together
     * those packages that conceptually make up the functionality described by the logging module (e.g. "SPARQL").
     *
     * @return a collection of package names
     */
    public Collection<String> getPackages();


    /**
     * Return the default (logback) level used by this logging module. Should in most cases be INFO or WARN.
     *
     * @return
     */
    public Level getDefaultLevel();


    /**
     * Return the currently configured (logback) level used by this logging module. This field is read from the
     * configuration file and defaults to getDefaultLevel()
     *
     * @return
     */
    public Level getCurrentLevel();


    /**
     * Update the currently active (logback) level used by this logging module. This method directly updates the
     * configuration file.
     *
     * @param level
     */
    public void setCurrentLevel(Level level);


    /**
     * Return the identifiers of all logging outputs configured for this module
     * @return
     */
    public List<String> getLoggingOutputIds();


    /**
     * Set the identifiers of all logging outputs for this module
     * @param ids
     */
    public void setLoggingOutputIds(List<String> ids);

    /**
     * Return the logging outputs configured for this module (resolved using the LoggingService).
     *
     * @return
     */
    public List<LoggingOutput> getLoggingOutputs();


    /**
     * Set the logging outputs configured for this module (internally calls setLoggingOutputIds).
     *
     * @param outputs
     */
    public void setLoggingOutputs(List<LoggingOutput> outputs);
}
