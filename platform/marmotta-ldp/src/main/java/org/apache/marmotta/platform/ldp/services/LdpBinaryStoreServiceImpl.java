/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.ldp.services;

import org.apache.commons.lang.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.events.SystemStartupEvent;
import org.apache.marmotta.platform.ldp.api.LdpBinaryStoreService;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.*;
import java.net.URISyntaxException;

/**
 * Very basic disk-based implementation of the LDP Binary Store
 *
 * @author Sergio Fernández
 */
@ApplicationScoped
public class LdpBinaryStoreServiceImpl implements LdpBinaryStoreService {

    private static final Logger log = LoggerFactory.getLogger(LdpBinaryStoreServiceImpl.class);

    @Inject
    private ConfigurationService configurationService;

    private File base;

    public void init(@Observes SystemStartupEvent e) {
        base = new File(configurationService.getHome(), "data");
        if (!base.exists()) {
            base.mkdir();
        }
        log.info("Initialized binary data store over {}", base.getAbsolutePath());
    }

    private File getFile(String resource) throws URISyntaxException {
        java.net.URI uri = new java.net.URI(resource);
        String schema = uri.getScheme();
        if ("http".compareTo(schema) != 0) {
            String msg = "Binary storage not supported for URIs with " + schema + " as schema";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return new File(base, StringUtils.removeStart(resource, "http://"));
    }

    @Override
    public boolean store(String resource, InputStream stream)  {
        try {
            File file = getFile(resource);
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            int token = -1;
            while((token = stream.read()) != -1) {
                bufferedOutputStream.write(token);
            }
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            stream.close();

            return true;
        } catch (URISyntaxException | IOException e) {
            log.error("{} resource cannot be stored on disk: {}", resource, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean store(URI resource, InputStream stream) {
        return store(resource.stringValue(), stream);
    }

    @Override
    public InputStream read(String resource) {
        return null;
    }

    @Override
    public InputStream read(URI resource) {
        return read(resource.stringValue());
    }

}
