/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.services.logging;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.PropertyDefiner;
import ch.qos.logback.core.status.Status;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.util.CDIContext;

/**
 * An abstract class with access to the LMF Configuration Service to get access to LMF runtime configuration.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LMFLogbackPropertyDefiner implements PropertyDefiner {


    protected ConfigurationService configurationService;

    protected Context context;
    
    protected String key;

    public LMFLogbackPropertyDefiner() {
        configurationService = CDIContext.getInstance(ConfigurationService.class);
    }

    /**
     * Get the property value, defined by this property definer
     *
     * @return defined property value
     */
    @Override
    public String getPropertyValue() {
        if(key != null) {
            return configurationService.getStringConfiguration(key);
        } else {
            return null;
        }
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void addStatus(Status status) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addInfo(String msg) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addWarn(String msg) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addError(String msg) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addError(String msg, Throwable ex) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
