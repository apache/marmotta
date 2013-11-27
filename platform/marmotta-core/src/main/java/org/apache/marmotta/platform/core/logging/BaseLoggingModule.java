package org.apache.marmotta.platform.core.logging;

import ch.qos.logback.classic.Level;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.logging.LoggingModule;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public abstract class BaseLoggingModule implements LoggingModule {

    private ConfigurationService configurationService;


    /**
     * Update the currently active (logback) level used by this logging module. This method directly updates the
     * configuration file.
     *
     * @param level
     */
    @Override
    public void setCurrentLevel(Level level) {
        configurationService.setConfiguration(String.format("logging.module.%s.level", getId()), level.toString());
    }

    /**
     * Return the currently configured (logback) level used by this logging module. This field is read from the
     * configuration file and defaults to getDefaultLevel()
     *
     * @return
     */
    @Override
    public Level getCurrentLevel() {
        return Level.toLevel(configurationService.getStringConfiguration(String.format("logging.module.%s.level", getId())), getDefaultLevel());
    }
}
