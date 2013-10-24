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
package org.apache.marmotta.platform.core.services.sesame;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.base.RepositoryConnectionInterceptorAdapter;

/**
 * A filter/view on repositories that displays only the metadata for a resource
 * 
 * @author Sebastian Schaffert
 *
 */
public class ResourceSubjectMetadata extends RepositoryConnectionInterceptorAdapter {

    private Resource subject;

    public ResourceSubjectMetadata(Resource subject) {
        this.subject = subject;
    }

    @Override
    public boolean add(RepositoryConnection conn, Resource s, org.openrdf.model.URI p, Value o, Resource... contexts) {
        boolean denyAdd = true;
        if (s instanceof org.openrdf.model.URI && subject instanceof org.openrdf.model.URI ) {
            // if s is a URI and subject a KiWiUriResource, return
            // true if they are different
            denyAdd = !s.stringValue().equals(subject.stringValue());
        } else if (s instanceof BNode && subject instanceof BNode) {
            // if s is a BNode and subject a KiWiAnonResource,
            // return true if they are different
            denyAdd = !s.stringValue().equals(subject.stringValue());
        } else {
            // in all other cases, return true to filter out the
            // triple
            denyAdd = true;
        }
        return denyAdd;
    };

    @Override
    public boolean remove(RepositoryConnection conn, Resource s, org.openrdf.model.URI p, Value o, Resource... contexts) {
        boolean denyRemove = true;
        if (s instanceof org.openrdf.model.URI && subject instanceof org.openrdf.model.URI ) {
            // if s is a URI and subject a KiWiUriResource, return
            // true if they are different
            denyRemove = !s.stringValue().equals(subject.stringValue());
        } else if (s instanceof BNode && subject instanceof BNode) {
            // if s is a BNode and subject a KiWiAnonResource,
            // return true if they are different
            denyRemove = !s.stringValue().equals(subject.stringValue());
        } else {
            // in all other cases, return true to filter out the
            // triple
            denyRemove = true;
        }
        return denyRemove;
    }

}
