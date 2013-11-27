package org.apache.marmotta.platform.core.model.logging;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ConsoleOutput extends LoggingOutput {


    public ConsoleOutput(ConfigurationService configurationService) {
        super("console", configurationService);
    }

    /**
     * Get the type identifier for this kind of logging output (e.g. "file", "console", "syslog"). Used for
     * properly resolving the configuration keys.
     *
     * @return
     */
    @Override
    protected String getTypeIdentifier() {
        return "console";
    }

    /**
     * Internal method: return the configuration key for this logging output and the given key suffix.
     *
     * @param key
     * @return
     */
    @Override
    protected String getConfigKey(String key) {
        return String.format("logging.console.%s", key);
    }
}

