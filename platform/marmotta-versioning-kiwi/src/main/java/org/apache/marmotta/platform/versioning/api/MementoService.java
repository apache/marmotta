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
package org.apache.marmotta.platform.versioning.api;

import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.platform.versioning.exception.MementoException;
import org.apache.marmotta.platform.versioning.model.MementoVersionSet;
import org.openrdf.model.Resource;

import java.util.Date;

/**
 * ...
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public interface MementoService {

    //public Version getNextVersion(Resource resource, Version version) throws MementoException;

    //public Version getPreviousVersion(Resource resource, Version version) throws MementoException;

    /**
     * returns the version for a resource that was current on the given date
     * @param resource a uri resource
     * @param date a date
     * @return the version with respect to the date
     * @throws MementoException
     */
    public Version getVersion(Resource resource, Date date) throws MementoException;

    //public Version getLastVersion(Resource resource) throws MementoException;
    //public Version getFirstVersion(Resource resource) throws MementoException;

    /**
     * returns a memento version set that includes first, last, current, prev and next version with respect
     * to a given date and resource
     * @param resource a requested resource
     * @param date a requested date
     * @return a memento version set
     * @throws MementoException
     * @see MementoVersionSet
     */
    public MementoVersionSet getVersionSet(Resource resource, Date date) throws MementoException;

}
