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
package org.apache.marmotta.platform.versioning.model;

import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.platform.versioning.exception.MementoException;
import org.apache.marmotta.platform.versioning.utils.MementoUtils;
import org.openrdf.model.Resource;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This POJO represents a set of resource versions including last, first, prev, next and current versions
 * <p/>
 * Author: Thomas Kurz (tkurz@apache.org)
 */
public class MementoVersionSet {

    private Version first;
    private Version last;
    private Version previous;
    private Version next;
    private Version current;
    private Resource original;

    public MementoVersionSet(Resource resource) {
        this.original = resource;
    }

    public Set<String> buildLinks(String baseURI) throws MementoException {

        String prefix =  baseURI +
                MementoUtils.MEMENTO_WEBSERVICE + "/" +
                MementoUtils.MEMENTO_RESOURCE + "/";

        HashSet<String> links = new HashSet<String>();

        //first, last and current are mandatory
        if( first == null || last == null || current == null) throw new MementoException("Memento links cannot be produced");

        links.add(buildLink(prefix, original.toString(), first.getCommitTime(), "first memento"));
        links.add(buildLink(prefix,original.toString(),last.getCommitTime(),"last memento"));
        links.add(buildLink(prefix,original.toString(),current.getCommitTime(),"memento"));

        //add link to original
        links.add("<"+original.toString()+">;rel=\"original\"");

        //add next and previous if they exist
        if( next != null ) links.add(buildLink(prefix,original.toString(),next.getCommitTime(),"next memento"));
        if( previous != null ) links.add(buildLink(prefix,original.toString(),previous.getCommitTime(),"prev memento"));

        return links;
    }

    private String buildLink( String prefix, String resource, Date date, String rel ) {
        return  "<" + prefix + MementoUtils.MEMENTO_DATE_FORMAT.format(date) + "/" + resource +
                ">;datetime=\"" + MementoUtils.MEMENTO_DATE_FORMAT.format(date) + "\";rel=\"" + rel +"\"";
    }

    public Resource getOriginal() {
        return original;
    }

    public Version getFirst() {
        return first;
    }

    public void setFirst(Version first) {
        this.first = first;
    }

    public Version getLast() {
        return last;
    }

    public void setLast(Version last) {
        this.last = last;
    }

    public Version getPrevious() {
        return previous;
    }

    public void setPrevious(Version previous) {
        this.previous = previous;
    }

    public Version getNext() {
        return next;
    }

    public void setNext(Version next) {
        this.next = next;
    }

    public Version getCurrent() {
        return current;
    }

    public void setCurrent(Version current) {
        this.current = current;
    }
}
