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

package org.apache.marmotta.commons.sesame.facading.foaf.model;



import org.apache.marmotta.commons.sesame.facading.annotations.RDF;
import org.apache.marmotta.commons.sesame.facading.annotations.RDFFilter;
import org.apache.marmotta.commons.sesame.facading.annotations.RDFType;
import org.apache.marmotta.commons.sesame.facading.model.Facade;
import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.openrdf.model.URI;

import java.util.Set;

/**
 * Sample facade to describe a foaf:Person
 * <p/>
 * Author: Sebastian Schaffert
 */
@RDFType(Namespaces.NS_FOAF + "Person")
@RDFFilter(Namespaces.NS_FOAF + "Person")
public interface Person extends Facade {

    @RDF(Namespaces.NS_FOAF + "nick")
    public String getNick();
    public void setNick(String nick);

    /**
     * The  name of the user; mapped to the foaf:name RDF property
     */
    @RDF(Namespaces.NS_FOAF + "name")
    public String getName();
    public void setName(String firstName);


    @RDF(Namespaces.NS_FOAF + "mbox")
    public String getMbox();
    public void setMbox(String mbox);

    @RDF(Namespaces.NS_FOAF + "depiction")
    public URI getDepiciton();
    public void setDepiction(URI depiction);

    @RDF(Namespaces.NS_FOAF + "account")
    public Set<OnlineAccount> getOnlineAccounts();
    public void setOnlineAccounts(Set<OnlineAccount> onlineAccounts);

    @RDF(Namespaces.NS_FOAF + "knows")
    public Set<Person> getFriends();
    public void setFriends(Set<Person> friends);
    public boolean hasFriends();
}
