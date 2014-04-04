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
package org.apache.marmotta.platform.ldp.api;

import org.openrdf.model.URI;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 *  LDP Store Service
 *
 *  @author Sergio Fernández
 */
public interface LdpBinaryStoreService {

    boolean store(String resource, InputStream stream);

    boolean store(URI resource, InputStream stream);

    InputStream read(String resource) throws IOException;

    InputStream read(URI resource) throws IOException;

    String getHash(String resource);

    String getHash(URI uri);

    boolean delete(URI uri);
    boolean delete(String resource);
}
