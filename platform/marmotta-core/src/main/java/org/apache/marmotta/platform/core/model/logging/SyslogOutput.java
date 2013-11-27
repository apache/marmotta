package org.apache.marmotta.platform.core.model.logging;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class SyslogOutput extends LoggingOutput {


    public SyslogOutput(String id, ConfigurationService configurationService) {
        super(id, configurationService);
    }

    public String getFacility() {
        return configurationService.getStringConfiguration(getConfigKey("facility"), "LOCAL0");
    }

    public void setFacility(String facility) {
        configurationService.setConfiguration(getConfigKey("facility"), facility);
    }

    public String getHostName() {
        return configurationService.getStringConfiguration(getConfigKey("host"),"localhost");
    }

    public void setHostName(String hostName) {
        configurationService.setConfiguration(getConfigKey("host"), hostName);
    }

    /**
     * Get the type identifier for this kind of logging output (e.g. "file", "console", "syslog"). Used for
     * properly resolving the configuration keys.
     *
     * @return
     */
    @Override
    protected String getTypeIdentifier() {
        return "syslog";
    }
}
