/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.ostrich.model;

import org.apache.marmotta.ostrich.model.proto.Model;
import org.openrdf.model.Namespace;

/**
 * An implementation of a Sesame Namespace backed by a protocol buffer.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ProtoNamespace implements Namespace {
    private Model.Namespace message;

    public ProtoNamespace(Model.Namespace message) {
        this.message = message;
    }

    public ProtoNamespace(String prefix, String uri) {
        message = Model.Namespace.newBuilder()
                .setUri(uri)
                .setPrefix(prefix).build();
    }

    public Model.Namespace getMessage() {
        return message;
    }

    /**
     * Gets the name of the current namespace (i.e. it's URI).
     *
     * @return name of namespace
     */
    @Override
    public String getName() {
        return message.getUri();
    }

    /**
     * Gets the prefix of the current namespace. The default namespace is
     * represented by an empty prefix string.
     *
     * @return prefix of namespace, or an empty string in case of the default
     * namespace.
     */
    @Override
    public String getPrefix() {
        return message.getPrefix();
    }

    @Override
    public int compareTo(Namespace namespace) {
        return getPrefix().compareTo(namespace.getPrefix());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Namespace)) return false;

        Namespace that = (Namespace) o;

        return getPrefix().equals(that.getPrefix());
    }

    @Override
    public int hashCode() {
        return getPrefix().hashCode();
    }
}
