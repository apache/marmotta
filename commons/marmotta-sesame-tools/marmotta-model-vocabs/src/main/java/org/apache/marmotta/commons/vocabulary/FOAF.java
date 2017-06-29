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

import org.openrdf.model.IRI;
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
    public static final IRI Agent;

    /**
     * A document.
     */
    public static final IRI Document;

    /**
     * A class of Agents.
     */
    public static final IRI Group;

    /**
     * An image.
     */
    public static final IRI Image;

    /**
     * A foaf:LabelProperty is any RDF property with texual values that serve as labels.
     */
    public static final IRI LabelProperty;

    /**
     * An online account.
     */
    public static final IRI OnlineAccount;

    /**
     * An online chat account.
     */
    public static final IRI OnlineChatAccount;

    /**
     * An online e-commerce account.
     */
    public static final IRI OnlineEcommerceAccount;

    /**
     * An online gaming account.
     */
    public static final IRI OnlineGamingAccount;

    /**
     * An organization.
     */
    public static final IRI Organization;

    /**
     * A person.
     */
    public static final IRI Person;

    /**
     * A personal profile RDF document.
     */
    public static final IRI PersonalProfileDocument;

    /**
     * A project (a collective endeavour of some kind).
     */
    public static final IRI Project;

    /**
     * Indicates an account held by this agent.
     */
    public static final IRI account;

    /**
     * Indicates the name (identifier) associated with this online account.
     */
    public static final IRI accountName;

    /**
     * Indicates a homepage of the service provide for this online account.
     */
    public static final IRI accountServiceHomepage;

    /**
     * The age in years of some agent.
     */
    public static final IRI age;

    /**
     * An AIM chat ID
     */
    public static final IRI aimChatID;

    /**
     * A location that something is based near, for some broadly human notion of near.
     */
    public static final IRI based_near;

    /**
     * The birthday of this Agent, represented in mm-dd string form, eg. '12-31'.
     */
    public static final IRI birthday;

    /**
     * A current project this person works on.
     */
    public static final IRI currentProject;

    /**
     * A depiction of some thing.
     */
    public static final IRI depiction;

    /**
     * A thing depicted in this representation.
     */
    public static final IRI depicts;

    /**
     * A checksum for the DNA of some thing. Joke.
     */
    public static final IRI dnaChecksum;

    /**
     * The family name of some person.
     */
    public static final IRI familyName;

    /**
     * The family name of some person.
     */
    public static final IRI family_name;

    /**
     * The first name of a person.
     */
    public static final IRI firstName;

    /**
     * The underlying or 'focal' entity associated with some SKOS-described concept.
     */
    public static final IRI focus;

    /**
     * An organization funding a project or person.
     */
    public static final IRI fundedBy;

    /**
     * A textual geekcode for this person, see http://www.geekcode.com/geek.html
     */
    public static final IRI geekcode;

    /**
     * The gender of this Agent (typically but not necessarily 'male' or 'female').
     */
    public static final IRI gender;

    /**
     * The given name of some person.
     */
    public static final IRI givenName;

    /**
     * The given name of some person.
     */
    public static final IRI givenname;

    /**
     * Indicates an account held by this agent.
     */
    public static final IRI holdsAccount;

    /**
     * A homepage for some thing.
     */
    public static final IRI homepage;

    /**
     * An ICQ chat ID
     */
    public static final IRI icqChatID;

    /**
     * An image that can be used to represent some thing (ie. those depictions which are particularly representative of something, eg. one's photo on a homepage).
     */
    public static final IRI img;

    /**
     * A page about a topic of interest to this person.
     */
    public static final IRI interest;

    /**
     * A document that this thing is the primary topic of.
     */
    public static final IRI isPrimaryTopicOf;

    /**
     * A jabber ID for something.
     */
    public static final IRI jabberID;

    /**
     * A person known by this person (indicating some level of reciprocated interaction between the parties).
     */
    public static final IRI knows;

    /**
     * The last name of a person.
     */
    public static final IRI lastName;

    /**
     * A logo representing some thing.
     */
    public static final IRI logo;

    /**
     * Something that was made by this agent.
     */
    public static final IRI made;

    /**
     * An agent that  made this thing.
     */
    public static final IRI maker;

    /**
     * A  personal mailbox, ie. an Internet mailbox associated with exactly one owner, the first owner of this mailbox. This is a 'static inverse functional property', in that  there is (across time and change) at most one individual that ever has any particular value for foaf:mbox.
     */
    public static final IRI mbox;

    /**
     * The sha1sum of the IRI of an Internet mailbox associated with exactly one owner, the  first owner of the mailbox.
     */
    public static final IRI mbox_sha1sum;

    /**
     * Indicates a member of a Group
     */
    public static final IRI member;

    /**
     * Indicates the class of individuals that are a member of a Group
     */
    public static final IRI membershipClass;

    /**
     * An MSN chat ID
     */
    public static final IRI msnChatID;

    /**
     * A Myers Briggs (MBTI) personality classification.
     */
    public static final IRI myersBriggs;

    /**
     * A name for some thing.
     */
    public static final IRI name;

    /**
     * A short informal nickname characterising an agent (includes login identifiers, IRC and other chat nicknames).
     */
    public static final IRI nick;

    /**
     * An OpenID for an Agent.
     */
    public static final IRI openid;

    /**
     * A page or document about this thing.
     */
    public static final IRI page;

    /**
     * A project this person has previously worked on.
     */
    public static final IRI pastProject;

    /**
     * A phone,  specified using fully qualified tel: IRI scheme (refs: http://www.w3.org/Addressing/schemes.html#tel).
     */
    public static final IRI phone;

    /**
     * A .plan comment, in the tradition of finger and '.plan' files.
     */
    public static final IRI plan;

    /**
     * The primary topic of some page or document.
     */
    public static final IRI primaryTopic;

    /**
     * A link to the publications of this person.
     */
    public static final IRI publications;

    /**
     * A homepage of a school attended by the person.
     */
    public static final IRI schoolHomepage;

    /**
     * A sha1sum hash, in hex.
     */
    public static final IRI sha1;

    /**
     * A Skype ID
     */
    public static final IRI skypeID;

    /**
     * A string expressing what the user is happy for the general public (normally) to know about their current activity.
     */
    public static final IRI status;

    /**
     * The surname of some person.
     */
    public static final IRI surname;

    /**
     * A theme.
     */
    public static final IRI theme;

    /**
     * A derived thumbnail image.
     */
    public static final IRI thumbnail;

    /**
     * A tipjar document for this agent, describing means for payment and reward.
     */
    public static final IRI tipjar;

    /**
     * Title (Mr, Mrs, Ms, Dr. etc)
     */
    public static final IRI title;

    /**
     * A topic of some page or document.
     */
    public static final IRI topic;

    /**
     * A thing of interest to this person.
     */
    public static final IRI topic_interest;

    /**
     * A weblog of some thing (whether person, group, company etc.).
     */
    public static final IRI weblog;

    /**
     * A work info homepage of some person; a page about their work for some organization.
     */
    public static final IRI workInfoHomepage;

    /**
     * A workplace homepage of some person; the homepage of an organization they work for.
     */
    public static final IRI workplaceHomepage;

    /**
     * A Yahoo chat ID
     */
    public static final IRI yahooChatID;


    static{
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Agent = factory.createIRI(FOAF.NAMESPACE, "Agent");
        Document = factory.createIRI(FOAF.NAMESPACE, "Document");
        Group = factory.createIRI(FOAF.NAMESPACE, "Group");
        Image = factory.createIRI(FOAF.NAMESPACE, "Image");
        LabelProperty = factory.createIRI(FOAF.NAMESPACE, "LabelProperty");
        OnlineAccount = factory.createIRI(FOAF.NAMESPACE, "OnlineAccount");
        OnlineChatAccount = factory.createIRI(FOAF.NAMESPACE, "OnlineChatAccount");
        OnlineEcommerceAccount = factory.createIRI(FOAF.NAMESPACE, "OnlineEcommerceAccount");
        OnlineGamingAccount = factory.createIRI(FOAF.NAMESPACE, "OnlineGamingAccount");
        Organization = factory.createIRI(FOAF.NAMESPACE, "Organization");
        Person = factory.createIRI(FOAF.NAMESPACE, "Person");
        PersonalProfileDocument = factory.createIRI(FOAF.NAMESPACE, "PersonalProfileDocument");
        Project = factory.createIRI(FOAF.NAMESPACE, "Project");
        account = factory.createIRI(FOAF.NAMESPACE, "account");
        accountName = factory.createIRI(FOAF.NAMESPACE, "accountName");
        accountServiceHomepage = factory.createIRI(FOAF.NAMESPACE, "accountServiceHomepage");
        age = factory.createIRI(FOAF.NAMESPACE, "age");
        aimChatID = factory.createIRI(FOAF.NAMESPACE, "aimChatID");
        based_near = factory.createIRI(FOAF.NAMESPACE, "based_near");
        birthday = factory.createIRI(FOAF.NAMESPACE, "birthday");
        currentProject = factory.createIRI(FOAF.NAMESPACE, "currentProject");
        depiction = factory.createIRI(FOAF.NAMESPACE, "depiction");
        depicts = factory.createIRI(FOAF.NAMESPACE, "depicts");
        dnaChecksum = factory.createIRI(FOAF.NAMESPACE, "dnaChecksum");
        familyName = factory.createIRI(FOAF.NAMESPACE, "familyName");
        family_name = factory.createIRI(FOAF.NAMESPACE, "family_name");
        firstName = factory.createIRI(FOAF.NAMESPACE, "firstName");
        focus = factory.createIRI(FOAF.NAMESPACE, "focus");
        fundedBy = factory.createIRI(FOAF.NAMESPACE, "fundedBy");
        geekcode = factory.createIRI(FOAF.NAMESPACE, "geekcode");
        gender = factory.createIRI(FOAF.NAMESPACE, "gender");
        givenName = factory.createIRI(FOAF.NAMESPACE, "givenName");
        givenname = factory.createIRI(FOAF.NAMESPACE, "givenname");
        holdsAccount = factory.createIRI(FOAF.NAMESPACE, "holdsAccount");
        homepage = factory.createIRI(FOAF.NAMESPACE, "homepage");
        icqChatID = factory.createIRI(FOAF.NAMESPACE, "icqChatID");
        img = factory.createIRI(FOAF.NAMESPACE, "img");
        interest = factory.createIRI(FOAF.NAMESPACE, "interest");
        isPrimaryTopicOf = factory.createIRI(FOAF.NAMESPACE, "isPrimaryTopicOf");
        jabberID = factory.createIRI(FOAF.NAMESPACE, "jabberID");
        knows = factory.createIRI(FOAF.NAMESPACE, "knows");
        lastName = factory.createIRI(FOAF.NAMESPACE, "lastName");
        logo = factory.createIRI(FOAF.NAMESPACE, "logo");
        made = factory.createIRI(FOAF.NAMESPACE, "made");
        maker = factory.createIRI(FOAF.NAMESPACE, "maker");
        mbox = factory.createIRI(FOAF.NAMESPACE, "mbox");
        mbox_sha1sum = factory.createIRI(FOAF.NAMESPACE, "mbox_sha1sum");
        member = factory.createIRI(FOAF.NAMESPACE, "member");
        membershipClass = factory.createIRI(FOAF.NAMESPACE, "membershipClass");
        msnChatID = factory.createIRI(FOAF.NAMESPACE, "msnChatID");
        myersBriggs = factory.createIRI(FOAF.NAMESPACE, "myersBriggs");
        name = factory.createIRI(FOAF.NAMESPACE, "name");
        nick = factory.createIRI(FOAF.NAMESPACE, "nick");
        openid = factory.createIRI(FOAF.NAMESPACE, "openid");
        page = factory.createIRI(FOAF.NAMESPACE, "page");
        pastProject = factory.createIRI(FOAF.NAMESPACE, "pastProject");
        phone = factory.createIRI(FOAF.NAMESPACE, "phone");
        plan = factory.createIRI(FOAF.NAMESPACE, "plan");
        primaryTopic = factory.createIRI(FOAF.NAMESPACE, "primaryTopic");
        publications = factory.createIRI(FOAF.NAMESPACE, "publications");
        schoolHomepage = factory.createIRI(FOAF.NAMESPACE, "schoolHomepage");
        sha1 = factory.createIRI(FOAF.NAMESPACE, "sha1");
        skypeID = factory.createIRI(FOAF.NAMESPACE, "skypeID");
        status = factory.createIRI(FOAF.NAMESPACE, "status");
        surname = factory.createIRI(FOAF.NAMESPACE, "surname");
        theme = factory.createIRI(FOAF.NAMESPACE, "theme");
        thumbnail = factory.createIRI(FOAF.NAMESPACE, "thumbnail");
        tipjar = factory.createIRI(FOAF.NAMESPACE, "tipjar");
        title = factory.createIRI(FOAF.NAMESPACE, "title");
        topic = factory.createIRI(FOAF.NAMESPACE, "topic");
        topic_interest = factory.createIRI(FOAF.NAMESPACE, "topic_interest");
        weblog = factory.createIRI(FOAF.NAMESPACE, "weblog");
        workInfoHomepage = factory.createIRI(FOAF.NAMESPACE, "workInfoHomepage");
        workplaceHomepage = factory.createIRI(FOAF.NAMESPACE, "workplaceHomepage");
        yahooChatID = factory.createIRI(FOAF.NAMESPACE, "yahooChatID");
    }
}
