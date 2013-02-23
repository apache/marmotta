/**
 * Copyright (C) 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.kiwi.versioning.model;

import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.transactions.model.TransactionData;

/**
 * In-memory representation of a KiWi version. Consists of a set of added triples, a set of removed triples,
 * a commit date, and a database ID.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class Version extends TransactionData {

    private Long id;

    private KiWiResource creator;

    public Version() {
        super();
    }

    public Version(Long id) {
        super();
        this.id = id;
    }

    public Version(TransactionData data) {
        this.addedTriples   = data.getAddedTriples();
        this.removedTriples = data.getRemovedTriples();
        this.commitTime     = data.getCommitTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KiWiResource getCreator() {
        return creator;
    }

    public void setCreator(KiWiResource creator) {
        this.creator = creator;
    }
}
