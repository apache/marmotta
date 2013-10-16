/**
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
package org.apache.marmotta.platform.versioning.services;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.platform.versioning.api.VersionSerializerService;
import org.apache.marmotta.platform.versioning.io.VersionSerializer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * Returns a adequate serializer for a given list of ContentTypes
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
@ApplicationScoped
public class VersionSerializerServiceImpl implements VersionSerializerService {

    @Inject @Any
    Instance<VersionSerializer> serializers;

    /**
     * returns an adequate serializer for a mimetype
     * @param type a list of mimetype (from Accept header)
     * @return a serializer
     * @throws IOException if there is no serializer for mimetype
     */
    @Override
    public VersionSerializer getSerializer(List<ContentType> type) throws IOException {
        for(VersionSerializer serializer : serializers) {
            if(MarmottaHttpUtils.bestContentType(serializer.getContentTypes(),type) != null) return serializer;
        }
        throw new IOException("Cannot find serializer for " + type);
    }
}
