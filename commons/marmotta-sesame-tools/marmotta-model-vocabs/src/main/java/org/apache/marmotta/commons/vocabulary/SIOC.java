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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Namespace SIOC
 */
public class SIOC {

    public static final String NAMESPACE = "http://rdfs.org/sioc/ns#";

    public static final String PREFIX = "sioc";

    /**
     * Community is a high-level concept that defines an online community and what it consists of.
     */
    public static final IRI Community;

    /**
     * An area in which content Items are contained.
     */
    public static final IRI Container;

    /**
     * A discussion area on which Posts or entries are made.
     */
    public static final IRI Forum;

    /**
     * An Item is something which can be in a Container.
     */
    public static final IRI Item;

    /**
     * An article or message that can be posted to a Forum.
     */
    public static final IRI Post;

    /**
     * A Role is a function of a UserAccount within a scope of a particular Forum, Site, etc.
     */
    public static final IRI Role;

    /**
     * A Site can be the location of an online community or set of communities, with UserAccounts and Usergroups creating Items in a set of Containers. It can be thought of as a web-accessible data Space.
     */
    public static final IRI Site;

    /**
     * A Space is a place where data resides, e.g. on a website, desktop, fileshare, etc.
     */
    public static final IRI Space;

    /**
     * A container for a series of threaded discussion Posts or Items.
     */
    public static final IRI Thread;

    /**
     * UserAccount is now preferred. This is a deprecated class for a User in an online community site.
     */
    public static final IRI User;

    /**
     * A user account in an online community site.
     */
    public static final IRI UserAccount;

    /**
     * A set of UserAccounts whose owners have a common purpose or interest. Can be used for access control purposes.
     */
    public static final IRI Usergroup;

    /**
     * Specifies that this Item is about a particular resource, e.g. a Post describing a book, hotel, etc.
     */
    public static final IRI about;

    /**
     * Refers to the foaf:Agent or foaf:Person who owns this sioc:UserAccount.
     */
    public static final IRI account_of;

    /**
     * Refers to who (e.g. a UserAccount, e-mail address, etc.) a particular Item is addressed to.
     */
    public static final IRI addressed_to;

    /**
     * A Site that the UserAccount is an administrator of.
     */
    public static final IRI administrator_of;

    /**
     * The IRI of a file attached to an Item.
     */
    public static final IRI attachment;

    /**
     * An image or depiction used to represent this UserAccount.
     */
    public static final IRI avatar;

    /**
     * An Item that this Container contains.
     */
    public static final IRI container_of;

    /**
     * The content of the Item in plain text format.
     */
    public static final IRI content;

    /**
     * The encoded content of the Post, contained in CDATA areas.
     */
    public static final IRI content_encoded;

    /**
     * When this was created, in ISO 8601 format.
     */
    public static final IRI created_at;

    /**
     * A resource that the UserAccount is a creator of.
     */
    public static final IRI creator_of;

    /**
     * The content of the Post.
     */
    public static final IRI description;

    /**
     * Links to a previous (older) revision of this Item or Post.
     */
    public static final IRI earlier_version;

    /**
     * An electronic mail address of the UserAccount.
     */
    public static final IRI email;

    /**
     * An electronic mail address of the UserAccount, encoded using SHA1.
     */
    public static final IRI email_sha1;

    /**
     * This links Items to embedded statements, facts and structured content.
     */
    public static final IRI embeds_knowledge;

    /**
     * A feed (e.g. RSS, Atom, etc.) pertaining to this resource (e.g. for a Forum, Site, UserAccount, etc.).
     */
    public static final IRI feed;

    /**
     * First (real) name of this User. Synonyms include given name or christian name.
     */
    public static final IRI first_name;

    /**
     * Indicates that one UserAccount follows another UserAccount (e.g. for microblog posts or other content item updates).
     */
    public static final IRI follows;

    /**
     * A UserAccount that has this Role.
     */
    public static final IRI function_of;

    /**
     * This property has been renamed. Use sioc:usergroup_of instead.
     */
    public static final IRI group_of;

    /**
     * A UserAccount that is an administrator of this Site.
     */
    public static final IRI has_administrator;

    /**
     * The Container to which this Item belongs.
     */
    public static final IRI has_container;

    /**
     * This is the UserAccount that made this resource.
     */
    public static final IRI has_creator;

    /**
     * The discussion that is related to this Item.
     */
    public static final IRI has_discussion;

    /**
     * A Role that this UserAccount has.
     */
    public static final IRI has_function;

    /**
     * This property has been renamed. Use sioc:has_usergroup instead.
     */
    public static final IRI has_group;

    /**
     * The Site that hosts this Forum.
     */
    public static final IRI has_host;

    /**
     * A UserAccount that is a member of this Usergroup.
     */
    public static final IRI has_member;

    /**
     * A UserAccount that is a moderator of this Forum.
     */
    public static final IRI has_moderator;

    /**
     * A UserAccount that modified this Item.
     */
    public static final IRI has_modifier;

    /**
     * A UserAccount that this resource is owned by.
     */
    public static final IRI has_owner;

    /**
     * A Container or Forum that this Container or Forum is a child of.
     */
    public static final IRI has_parent;

    /**
     * An resource that is a part of this subject.
     */
    public static final IRI has_part;

    /**
     * Points to an Item or Post that is a reply or response to this Item or Post.
     */
    public static final IRI has_reply;

    /**
     * A resource that this Role applies to.
     */
    public static final IRI has_scope;

    /**
     * A data Space which this resource is a part of.
     */
    public static final IRI has_space;

    /**
     * A UserAccount that is subscribed to this Container.
     */
    public static final IRI has_subscriber;

    /**
     * Points to a Usergroup that has certain access to this Space.
     */
    public static final IRI has_usergroup;

    /**
     * A Forum that is hosted on this Site.
     */
    public static final IRI host_of;

    /**
     * An identifier of a SIOC concept instance. For example, a user ID. Must be unique for instances of each type of SIOC concept within the same site.
     */
    public static final IRI id;

    /**
     * The IP address used when creating this Item. This can be associated with a creator. Some wiki articles list the IP addresses for the creator or modifiers when the usernames are absent.
     */
    public static final IRI ip_address;

    /**
     * The date and time of the last activity associated with a SIOC concept instance, and expressed in ISO 8601 format. This could be due to a reply Post or Comment, a modification to an Item, etc.
     */
    public static final IRI last_activity_date;

    /**
     * The date and time of the last Post (or Item) in a Forum (or a Container), in ISO 8601 format.
     */
    public static final IRI last_item_date;

    /**
     * Last (real) name of this user. Synonyms include surname or family name.
     */
    public static final IRI last_name;

    /**
     * The date and time of the last reply Post or Comment, which could be associated with a starter Item or Post or with a Thread, and expressed in ISO 8601 format.
     */
    public static final IRI last_reply_date;

    /**
     * Links to a later (newer) revision of this Item or Post.
     */
    public static final IRI later_version;

    /**
     * Links to the latest revision of this Item or Post.
     */
    public static final IRI latest_version;

    /**
     * A IRI of a document which contains this SIOC object.
     */
    public static final IRI link;

    /**
     * Links extracted from hyperlinks within a SIOC concept, e.g. Post or Site.
     */
    public static final IRI links_to;

    /**
     * A Usergroup that this UserAccount is a member of.
     */
    public static final IRI member_of;

    /**
     * A Forum that a UserAccount is a moderator of.
     */
    public static final IRI moderator_of;

    /**
     * When this was modified, in ISO 8601 format.
     */
    public static final IRI modified_at;

    /**
     * An Item that this UserAccount has modified.
     */
    public static final IRI modifier_of;

    /**
     * The name of a SIOC concept instance, e.g. a username for a UserAccount, group name for a Usergroup, etc.
     */
    public static final IRI name;

    /**
     * Next Item or Post in a given Container sorted by date.
     */
    public static final IRI next_by_date;

    /**
     * Links to the next revision of this Item or Post.
     */
    public static final IRI next_version;

    /**
     * A note associated with this resource, for example, if it has been edited by a UserAccount.
     */
    public static final IRI note;

    /**
     * The number of unique authors (UserAccounts and unregistered posters) who have contributed to this Item, Thread, Post, etc.
     */
    public static final IRI num_authors;

    /**
     * The number of Posts (or Items) in a Forum (or a Container).
     */
    public static final IRI num_items;

    /**
     * The number of replies that this Item, Thread, Post, etc. has. Useful for when the reply structure is absent.
     */
    public static final IRI num_replies;

    /**
     * The number of Threads (AKA discussion topics) in a Forum.
     */
    public static final IRI num_threads;

    /**
     * The number of times this Item, Thread, UserAccount profile, etc. has been viewed.
     */
    public static final IRI num_views;

    /**
     * A resource owned by a particular UserAccount, for example, a weblog or image gallery.
     */
    public static final IRI owner_of;

    /**
     * A child Container or Forum that this Container or Forum is a parent of.
     */
    public static final IRI parent_of;

    /**
     * A resource that the subject is a part of.
     */
    public static final IRI part_of;

    /**
     * Previous Item or Post in a given Container sorted by date.
     */
    public static final IRI previous_by_date;

    /**
     * Links to the previous revision of this Item or Post.
     */
    public static final IRI previous_version;

    /**
     * Links either created explicitly or extracted implicitly on the HTML level from the Post.
     */
    public static final IRI reference;

    /**
     * Related Posts for this Post, perhaps determined implicitly from topics or references.
     */
    public static final IRI related_to;

    /**
     * Links to an Item or Post which this Item or Post is a reply to.
     */
    public static final IRI reply_of;

    /**
     * A Role that has a scope of this resource.
     */
    public static final IRI scope_of;

    /**
     * An Item may have a sibling or a twin that exists in a different Container, but the siblings may differ in some small way (for example, language, category, etc.). The sibling of this Item should be self-describing (that is, it should contain all available information).
     */
    public static final IRI sibling;

    /**
     * A resource which belongs to this data Space.
     */
    public static final IRI space_of;

    /**
     * Keyword(s) describing subject of the Post.
     */
    public static final IRI subject;

    /**
     * A Container that a UserAccount is subscribed to.
     */
    public static final IRI subscriber_of;

    /**
     * This is the title (subject line) of the Post. Note that for a Post within a threaded discussion that has no parents, it would detail the topic thread.
     */
    public static final IRI title;

    /**
     * A topic of interest, linking to the appropriate IRI, e.g. in the Open Directory Project or of a SKOS category.
     */
    public static final IRI topic;

    /**
     * A Space that the Usergroup has access to.
     */
    public static final IRI usergroup_of;


    static{
        ValueFactory factory = SimpleValueFactory.getInstance();
        Community = factory.createIRI(SIOC.NAMESPACE, "Community");
        Container = factory.createIRI(SIOC.NAMESPACE, "Container");
        Forum = factory.createIRI(SIOC.NAMESPACE, "Forum");
        Item = factory.createIRI(SIOC.NAMESPACE, "Item");
        Post = factory.createIRI(SIOC.NAMESPACE, "Post");
        Role = factory.createIRI(SIOC.NAMESPACE, "Role");
        Site = factory.createIRI(SIOC.NAMESPACE, "Site");
        Space = factory.createIRI(SIOC.NAMESPACE, "Space");
        Thread = factory.createIRI(SIOC.NAMESPACE, "Thread");
        User = factory.createIRI(SIOC.NAMESPACE, "User");
        UserAccount = factory.createIRI(SIOC.NAMESPACE, "UserAccount");
        Usergroup = factory.createIRI(SIOC.NAMESPACE, "Usergroup");
        about = factory.createIRI(SIOC.NAMESPACE, "about");
        account_of = factory.createIRI(SIOC.NAMESPACE, "account_of");
        addressed_to = factory.createIRI(SIOC.NAMESPACE, "addressed_to");
        administrator_of = factory.createIRI(SIOC.NAMESPACE, "administrator_of");
        attachment = factory.createIRI(SIOC.NAMESPACE, "attachment");
        avatar = factory.createIRI(SIOC.NAMESPACE, "avatar");
        container_of = factory.createIRI(SIOC.NAMESPACE, "container_of");
        content = factory.createIRI(SIOC.NAMESPACE, "content");
        content_encoded = factory.createIRI(SIOC.NAMESPACE, "content_encoded");
        created_at = factory.createIRI(SIOC.NAMESPACE, "created_at");
        creator_of = factory.createIRI(SIOC.NAMESPACE, "creator_of");
        description = factory.createIRI(SIOC.NAMESPACE, "description");
        earlier_version = factory.createIRI(SIOC.NAMESPACE, "earlier_version");
        email = factory.createIRI(SIOC.NAMESPACE, "email");
        email_sha1 = factory.createIRI(SIOC.NAMESPACE, "email_sha1");
        embeds_knowledge = factory.createIRI(SIOC.NAMESPACE, "embeds_knowledge");
        feed = factory.createIRI(SIOC.NAMESPACE, "feed");
        first_name = factory.createIRI(SIOC.NAMESPACE, "first_name");
        follows = factory.createIRI(SIOC.NAMESPACE, "follows");
        function_of = factory.createIRI(SIOC.NAMESPACE, "function_of");
        group_of = factory.createIRI(SIOC.NAMESPACE, "group_of");
        has_administrator = factory.createIRI(SIOC.NAMESPACE, "has_administrator");
        has_container = factory.createIRI(SIOC.NAMESPACE, "has_container");
        has_creator = factory.createIRI(SIOC.NAMESPACE, "has_creator");
        has_discussion = factory.createIRI(SIOC.NAMESPACE, "has_discussion");
        has_function = factory.createIRI(SIOC.NAMESPACE, "has_function");
        has_group = factory.createIRI(SIOC.NAMESPACE, "has_group");
        has_host = factory.createIRI(SIOC.NAMESPACE, "has_host");
        has_member = factory.createIRI(SIOC.NAMESPACE, "has_member");
        has_moderator = factory.createIRI(SIOC.NAMESPACE, "has_moderator");
        has_modifier = factory.createIRI(SIOC.NAMESPACE, "has_modifier");
        has_owner = factory.createIRI(SIOC.NAMESPACE, "has_owner");
        has_parent = factory.createIRI(SIOC.NAMESPACE, "has_parent");
        has_part = factory.createIRI(SIOC.NAMESPACE, "has_part");
        has_reply = factory.createIRI(SIOC.NAMESPACE, "has_reply");
        has_scope = factory.createIRI(SIOC.NAMESPACE, "has_scope");
        has_space = factory.createIRI(SIOC.NAMESPACE, "has_space");
        has_subscriber = factory.createIRI(SIOC.NAMESPACE, "has_subscriber");
        has_usergroup = factory.createIRI(SIOC.NAMESPACE, "has_usergroup");
        host_of = factory.createIRI(SIOC.NAMESPACE, "host_of");
        id = factory.createIRI(SIOC.NAMESPACE, "id");
        ip_address = factory.createIRI(SIOC.NAMESPACE, "ip_address");
        last_activity_date = factory.createIRI(SIOC.NAMESPACE, "last_activity_date");
        last_item_date = factory.createIRI(SIOC.NAMESPACE, "last_item_date");
        last_name = factory.createIRI(SIOC.NAMESPACE, "last_name");
        last_reply_date = factory.createIRI(SIOC.NAMESPACE, "last_reply_date");
        later_version = factory.createIRI(SIOC.NAMESPACE, "later_version");
        latest_version = factory.createIRI(SIOC.NAMESPACE, "latest_version");
        link = factory.createIRI(SIOC.NAMESPACE, "link");
        links_to = factory.createIRI(SIOC.NAMESPACE, "links_to");
        member_of = factory.createIRI(SIOC.NAMESPACE, "member_of");
        moderator_of = factory.createIRI(SIOC.NAMESPACE, "moderator_of");
        modified_at = factory.createIRI(SIOC.NAMESPACE, "modified_at");
        modifier_of = factory.createIRI(SIOC.NAMESPACE, "modifier_of");
        name = factory.createIRI(SIOC.NAMESPACE, "name");
        next_by_date = factory.createIRI(SIOC.NAMESPACE, "next_by_date");
        next_version = factory.createIRI(SIOC.NAMESPACE, "next_version");
        note = factory.createIRI(SIOC.NAMESPACE, "note");
        num_authors = factory.createIRI(SIOC.NAMESPACE, "num_authors");
        num_items = factory.createIRI(SIOC.NAMESPACE, "num_items");
        num_replies = factory.createIRI(SIOC.NAMESPACE, "num_replies");
        num_threads = factory.createIRI(SIOC.NAMESPACE, "num_threads");
        num_views = factory.createIRI(SIOC.NAMESPACE, "num_views");
        owner_of = factory.createIRI(SIOC.NAMESPACE, "owner_of");
        parent_of = factory.createIRI(SIOC.NAMESPACE, "parent_of");
        part_of = factory.createIRI(SIOC.NAMESPACE, "part_of");
        previous_by_date = factory.createIRI(SIOC.NAMESPACE, "previous_by_date");
        previous_version = factory.createIRI(SIOC.NAMESPACE, "previous_version");
        reference = factory.createIRI(SIOC.NAMESPACE, "reference");
        related_to = factory.createIRI(SIOC.NAMESPACE, "related_to");
        reply_of = factory.createIRI(SIOC.NAMESPACE, "reply_of");
        scope_of = factory.createIRI(SIOC.NAMESPACE, "scope_of");
        sibling = factory.createIRI(SIOC.NAMESPACE, "sibling");
        space_of = factory.createIRI(SIOC.NAMESPACE, "space_of");
        subject = factory.createIRI(SIOC.NAMESPACE, "subject");
        subscriber_of = factory.createIRI(SIOC.NAMESPACE, "subscriber_of");
        title = factory.createIRI(SIOC.NAMESPACE, "title");
        topic = factory.createIRI(SIOC.NAMESPACE, "topic");
        usergroup_of = factory.createIRI(SIOC.NAMESPACE, "usergroup_of");
    }
}
