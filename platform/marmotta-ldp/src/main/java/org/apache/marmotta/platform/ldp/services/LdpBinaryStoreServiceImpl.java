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

import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.util.HashUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.ldp.api.LdpBinaryStoreService;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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

    private Path base;

    @PostConstruct
    public void init() {
        base = Paths.get(configurationService.getHome(), "data");

        log.info("Initialized binary data store over {}", base.toString());
    }

    Path getFile(String resource) throws URISyntaxException {
        final java.net.URI uri = new java.net.URI(resource);
        final String schema = uri.getScheme();
        if ("http".compareTo(schema) != 0 && "https".compareTo(schema) != 0) {
            final String msg = "Binary storage not supported for URIs with " + schema + " as schema";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        final int port;
        if (uri.getPort() > 0) {
            port = uri.getPort();
        } else if ("http".compareTo(schema) == 0) {
            port = 80;
        } else if ("https".compareTo(schema) == 0) {
            port = 443;
        } else {
            port = 0;
        }

        return base.resolve(String.format("%s.%d/%s", uri.getHost(), port, uri.getRawPath()));
    }

    @Override
    public boolean store(String resource, InputStream stream)  {
        try {
            Path file = getFile(resource);
            Files.createDirectories(file.getParent());

            try (OutputStream outputStream = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                IOUtils.copy(stream, outputStream);
            }
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
    public InputStream read(String resource) throws IOException {
        try {
            Path file = getFile(resource);
            if (Files.exists(file)) {
                return Files.newInputStream(file, StandardOpenOption.READ);
            } else {
                log.warn("{} not found in binary storage ({})", resource, file);
                return null;
            }
        } catch (URISyntaxException e) {
            log.error("Error reading resource {}: {}", resource, e.getMessage());
            return null;
        }
    }

    @Override
    public InputStream read(URI resource) throws IOException {
        return read(resource.stringValue());
    }


    @Override
    public String getHash(String resource) {
        try(InputStream is = Files.newInputStream(getFile(resource))) {
            return HashUtils.md5sum(is);
        } catch (URISyntaxException | IOException e) {
            log.error("Error calculating file-md5 of {}: {}", resource, e);
            return null;
        }
    }

    @Override
    public String getHash(URI uri) {
        return getHash(uri.stringValue());
    }

    @Override
    public boolean delete(URI uri) {
        return delete(uri.stringValue());
    }

    @Override
    public boolean delete(String resource) {
        try {
            final Path file = getFile(resource);
            return Files.deleteIfExists(file);
        } catch (IOException | URISyntaxException e) {
            log.error("Error while deleting {}: {}", resource, e.getMessage());
            return false;
        }
    }
}
