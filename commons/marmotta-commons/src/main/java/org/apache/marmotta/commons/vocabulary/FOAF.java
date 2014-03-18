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
package org.apache.marmotta.commons.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Namespace FOAF
 */
public class FOAF {

    public static final String NAMESPACE = "http://xmlns.com/foaf/0.1/";

    public static final String PREFIX = "foaf";

    /**
     * An agent (eg. person, group, software or physical artifact).
     */
    public static final URI Agent;

    /**
     * A document.
     */
    public static final URI Document;

    /**
     * A class of Agents.
     */
    public static final URI Group;

    /**
     * An image.
     */
    public static final URI Image;

    /**
     * A foaf:LabelProperty is any RDF property with texual values that serve as labels.
     */
    public static final URI LabelProperty;

    /**
     * An online account.
     */
    public static final URI OnlineAccount;

    /**
     * An online chat account.
     */
    public static final URI OnlineChatAccount;

    /**
     * An online e-commerce account.
     */
    public static final URI OnlineEcommerceAccount;

    /**
     * An online gaming account.
     */
    public static final URI OnlineGamingAccount;

    /**
     * An organization.
     */
    public static final URI Organization;

    /**
     * A person.
     */
    public static final URI Person;

    /**
     * A personal profile RDF document.
     */
    public static final URI PersonalProfileDocument;

    /**
     * A project (a collective endeavour of some kind).
     */
    public static final URI Project;

    /**
     * Indicates an account held by this agent.
     */
    public static final URI account;

    /**
     * Indicates the name (identifier) associated with this online account.
     */
    public static final URI accountName;

    /**
     * Indicates a homepage of the service provide for this online account.
     */
    public static final URI accountServiceHomepage;

    /**
     * The age in years of some agent.
     */
    public static final URI age;

    /**
     * An AIM chat ID
     */
    public static final URI aimChatID;

    /**
     * A location that something is based near, for some broadly human notion of near.
     */
    public static final URI based_near;

    /**
     * The birthday of this Agent, represented in mm-dd string form, eg. '12-31'.
     */
    public static final URI birthday;

    /**
     * A current project this person works on.
     */
    public static final URI currentProject;

    /**
     * A depiction of some thing.
     */
    public static final URI depiction;

    /**
     * A thing depicted in this representation.
     */
    public static final URI depicts;

    /**
     * A checksum for the DNA of some thing. Joke.
     */
    public static final URI dnaChecksum;

    /**
     * The family name of some person.
     */
    public static final URI familyName;

    /**
     * The family name of some person.
     */
    public static final URI family_name;

    /**
     * The first name of a person.
     */
    public static final URI firstName;

    /**
     * The underlying or 'focal' entity associated with some SKOS-described concept.
     */
    public static final URI focus;

    /**
     * An organization funding a project or person.
     */
    public static final URI fundedBy;

    /**
     * A textual geekcode for this person, see http://www.geekcode.com/geek.html
     */
    public static final URI geekcode;

    /**
     * The gender of this Agent (typically but not necessarily 'male' or 'female').
     */
    public static final URI gender;

    /**
     * The given name of some person.
     */
    public static final URI givenName;

    /**
     * The given name of some person.
     */
    public static final URI givenname;

    /**
     * Indicates an account held by this agent.
     */
    public static final URI holdsAccount;

    /**
     * A homepage for some thing.
     */
    public static final URI homepage;

    /**
     * An ICQ chat ID
     */
    public static final URI icqChatID;

    /**
     * An image that can be used to represent some thing (ie. those depictions which are particularly representative of something, eg. one's photo on a homepage).
     */
    public static final URI img;

    /**
     * A page about a topic of interest to this person.
     */
    public static final URI interest;

    /**
     * A document that this thing is the primary topic of.
     */
    public static final URI isPrimaryTopicOf;

    /**
     * A jabber ID for something.
     */
    public static final URI jabberID;

    /**
     * A person known by this person (indicating some level of reciprocated interaction between the parties).
     */
    public static final URI knows;

    /**
     * The last name of a person.
     */
    public static final URI lastName;

    /**
     * A logo representing some thing.
     */
    public static final URI logo;

    /**
     * Something that was made by this agent.
     */
    public static final URI made;

    /**
     * An agent that  made this thing.
     */
    public static final URI maker;

    /**
     * A  personal mailbox, ie. an Internet mailbox associated with exactly one owner, the first owner of this mailbox. This is a 'static inverse functional property', in that  there is (across time and change) at most one individual that ever has any particular value for foaf:mbox.
     */
    public static final URI mbox;

    /**
     * The sha1sum of the URI of an Internet mailbox associated with exactly one owner, the  first owner of the mailbox.
     */
    public static final URI mbox_sha1sum;

    /**
     * Indicates a member of a Group
     */
    public static final URI member;

    /**
     * Indicates the class of individuals that are a member of a Group
     */
    public static final URI membershipClass;

    /**
     * An MSN chat ID
     */
    public static final URI msnChatID;

    /**
     * A Myers Briggs (MBTI) personality classification.
     */
    public static final URI myersBriggs;

    /**
     * A name for some thing.
     */
    public static final URI name;

    /**
     * A short informal nickname characterising an agent (includes login identifiers, IRC and other chat nicknames).
     */
    public static final URI nick;

    /**
     * An OpenID for an Agent.
     */
    public static final URI openid;

    /**
     * A page or document about this thing.
     */
    public static final URI page;

    /**
     * A project this person has previously worked on.
     */
    public static final URI pastProject;

    /**
     * A phone,  specified using fully qualified tel: URI scheme (refs: http://www.w3.org/Addressing/schemes.html#tel).
     */
    public static final URI phone;

    /**
     * A .plan comment, in the tradition of finger and '.plan' files.
     */
    public static final URI plan;

    /**
     * The primary topic of some page or document.
     */
    public static final URI primaryTopic;

    /**
     * A link to the publications of this person.
     */
    public static final URI publications;

    /**
     * A homepage of a school attended by the person.
     */
    public static final URI schoolHomepage;

    /**
     * A sha1sum hash, in hex.
     */
    public static final URI sha1;

    /**
     * A Skype ID
     */
    public static final URI skypeID;

    /**
     * A string expressing what the user is happy for the general public (normally) to know about their current activity.
     */
    public static final URI status;

    /**
     * The surname of some person.
     */
    public static final URI surname;

    /**
     * A theme.
     */
    public static final URI theme;

    /**
     * A derived thumbnail image.
     */
    public static final URI thumbnail;

    /**
     * A tipjar document for this agent, describing means for payment and reward.
     */
    public static final URI tipjar;

    /**
     * Title (Mr, Mrs, Ms, Dr. etc)
     */
    public static final URI title;

    /**
     * A topic of some page or document.
     */
    public static final URI topic;

    /**
     * A thing of interest to this person.
     */
    public static final URI topic_interest;

    /**
     * A weblog of some thing (whether person, group, company etc.).
     */
    public static final URI weblog;

    /**
     * A work info homepage of some person; a page about their work for some organization.
     */
    public static final URI workInfoHomepage;

    /**
     * A workplace homepage of some person; the homepage of an organization they work for.
     */
    public static final URI workplaceHomepage;

    /**
     * A Yahoo chat ID
     */
    public static final URI yahooChatID;


    static{
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Agent = factory.createURI(FOAF.NAMESPACE, "Agent");
        Document = factory.createURI(FOAF.NAMESPACE, "Document");
        Group = factory.createURI(FOAF.NAMESPACE, "Group");
        Image = factory.createURI(FOAF.NAMESPACE, "Image");
        LabelProperty = factory.createURI(FOAF.NAMESPACE, "LabelProperty");
        OnlineAccount = factory.createURI(FOAF.NAMESPACE, "OnlineAccount");
        OnlineChatAccount = factory.createURI(FOAF.NAMESPACE, "OnlineChatAccount");
        OnlineEcommerceAccount = factory.createURI(FOAF.NAMESPACE, "OnlineEcommerceAccount");
        OnlineGamingAccount = factory.createURI(FOAF.NAMESPACE, "OnlineGamingAccount");
        Organization = factory.createURI(FOAF.NAMESPACE, "Organization");
        Person = factory.createURI(FOAF.NAMESPACE, "Person");
        PersonalProfileDocument = factory.createURI(FOAF.NAMESPACE, "PersonalProfileDocument");
        Project = factory.createURI(FOAF.NAMESPACE, "Project");
        account = factory.createURI(FOAF.NAMESPACE, "account");
        accountName = factory.createURI(FOAF.NAMESPACE, "accountName");
        accountServiceHomepage = factory.createURI(FOAF.NAMESPACE, "accountServiceHomepage");
        age = factory.createURI(FOAF.NAMESPACE, "age");
        aimChatID = factory.createURI(FOAF.NAMESPACE, "aimChatID");
        based_near = factory.createURI(FOAF.NAMESPACE, "based_near");
        birthday = factory.createURI(FOAF.NAMESPACE, "birthday");
        currentProject = factory.createURI(FOAF.NAMESPACE, "currentProject");
        depiction = factory.createURI(FOAF.NAMESPACE, "depiction");
        depicts = factory.createURI(FOAF.NAMESPACE, "depicts");
        dnaChecksum = factory.createURI(FOAF.NAMESPACE, "dnaChecksum");
        familyName = factory.createURI(FOAF.NAMESPACE, "familyName");
        family_name = factory.createURI(FOAF.NAMESPACE, "family_name");
        firstName = factory.createURI(FOAF.NAMESPACE, "firstName");
        focus = factory.createURI(FOAF.NAMESPACE, "focus");
        fundedBy = factory.createURI(FOAF.NAMESPACE, "fundedBy");
        geekcode = factory.createURI(FOAF.NAMESPACE, "geekcode");
        gender = factory.createURI(FOAF.NAMESPACE, "gender");
        givenName = factory.createURI(FOAF.NAMESPACE, "givenName");
        givenname = factory.createURI(FOAF.NAMESPACE, "givenname");
        holdsAccount = factory.createURI(FOAF.NAMESPACE, "holdsAccount");
        homepage = factory.createURI(FOAF.NAMESPACE, "homepage");
        icqChatID = factory.createURI(FOAF.NAMESPACE, "icqChatID");
        img = factory.createURI(FOAF.NAMESPACE, "img");
        interest = factory.createURI(FOAF.NAMESPACE, "interest");
        isPrimaryTopicOf = factory.createURI(FOAF.NAMESPACE, "isPrimaryTopicOf");
        jabberID = factory.createURI(FOAF.NAMESPACE, "jabberID");
        knows = factory.createURI(FOAF.NAMESPACE, "knows");
        lastName = factory.createURI(FOAF.NAMESPACE, "lastName");
        logo = factory.createURI(FOAF.NAMESPACE, "logo");
        made = factory.createURI(FOAF.NAMESPACE, "made");
        maker = factory.createURI(FOAF.NAMESPACE, "maker");
        mbox = factory.createURI(FOAF.NAMESPACE, "mbox");
        mbox_sha1sum = factory.createURI(FOAF.NAMESPACE, "mbox_sha1sum");
        member = factory.createURI(FOAF.NAMESPACE, "member");
        membershipClass = factory.createURI(FOAF.NAMESPACE, "membershipClass");
        msnChatID = factory.createURI(FOAF.NAMESPACE, "msnChatID");
        myersBriggs = factory.createURI(FOAF.NAMESPACE, "myersBriggs");
        name = factory.createURI(FOAF.NAMESPACE, "name");
        nick = factory.createURI(FOAF.NAMESPACE, "nick");
        openid = factory.createURI(FOAF.NAMESPACE, "openid");
        page = factory.createURI(FOAF.NAMESPACE, "page");
        pastProject = factory.createURI(FOAF.NAMESPACE, "pastProject");
        phone = factory.createURI(FOAF.NAMESPACE, "phone");
        plan = factory.createURI(FOAF.NAMESPACE, "plan");
        primaryTopic = factory.createURI(FOAF.NAMESPACE, "primaryTopic");
        publications = factory.createURI(FOAF.NAMESPACE, "publications");
        schoolHomepage = factory.createURI(FOAF.NAMESPACE, "schoolHomepage");
        sha1 = factory.createURI(FOAF.NAMESPACE, "sha1");
        skypeID = factory.createURI(FOAF.NAMESPACE, "skypeID");
        status = factory.createURI(FOAF.NAMESPACE, "status");
        surname = factory.createURI(FOAF.NAMESPACE, "surname");
        theme = factory.createURI(FOAF.NAMESPACE, "theme");
        thumbnail = factory.createURI(FOAF.NAMESPACE, "thumbnail");
        tipjar = factory.createURI(FOAF.NAMESPACE, "tipjar");
        title = factory.createURI(FOAF.NAMESPACE, "title");
        topic = factory.createURI(FOAF.NAMESPACE, "topic");
        topic_interest = factory.createURI(FOAF.NAMESPACE, "topic_interest");
        weblog = factory.createURI(FOAF.NAMESPACE, "weblog");
        workInfoHomepage = factory.createURI(FOAF.NAMESPACE, "workInfoHomepage");
        workplaceHomepage = factory.createURI(FOAF.NAMESPACE, "workplaceHomepage");
        yahooChatID = factory.createURI(FOAF.NAMESPACE, "yahooChatID");
    }
}
