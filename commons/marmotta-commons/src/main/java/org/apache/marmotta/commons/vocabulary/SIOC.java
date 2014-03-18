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
 * Namespace SIOC
 */
public class SIOC {

    public static final String NAMESPACE = "http://rdfs.org/sioc/ns#";

    public static final String PREFIX = "sioc";

    /**
     * Community is a high-level concept that defines an online community and what it consists of.
     */
    public static final URI Community;

    /**
     * An area in which content Items are contained.
     */
    public static final URI Container;

    /**
     * A discussion area on which Posts or entries are made.
     */
    public static final URI Forum;

    /**
     * An Item is something which can be in a Container.
     */
    public static final URI Item;

    /**
     * An article or message that can be posted to a Forum.
     */
    public static final URI Post;

    /**
     * A Role is a function of a UserAccount within a scope of a particular Forum, Site, etc.
     */
    public static final URI Role;

    /**
     * A Site can be the location of an online community or set of communities, with UserAccounts and Usergroups creating Items in a set of Containers. It can be thought of as a web-accessible data Space.
     */
    public static final URI Site;

    /**
     * A Space is a place where data resides, e.g. on a website, desktop, fileshare, etc.
     */
    public static final URI Space;

    /**
     * A container for a series of threaded discussion Posts or Items.
     */
    public static final URI Thread;

    /**
     * UserAccount is now preferred. This is a deprecated class for a User in an online community site.
     */
    public static final URI User;

    /**
     * A user account in an online community site.
     */
    public static final URI UserAccount;

    /**
     * A set of UserAccounts whose owners have a common purpose or interest. Can be used for access control purposes.
     */
    public static final URI Usergroup;

    /**
     * Specifies that this Item is about a particular resource, e.g. a Post describing a book, hotel, etc.
     */
    public static final URI about;

    /**
     * Refers to the foaf:Agent or foaf:Person who owns this sioc:UserAccount.
     */
    public static final URI account_of;

    /**
     * Refers to who (e.g. a UserAccount, e-mail address, etc.) a particular Item is addressed to.
     */
    public static final URI addressed_to;

    /**
     * A Site that the UserAccount is an administrator of.
     */
    public static final URI administrator_of;

    /**
     * The URI of a file attached to an Item.
     */
    public static final URI attachment;

    /**
     * An image or depiction used to represent this UserAccount.
     */
    public static final URI avatar;

    /**
     * An Item that this Container contains.
     */
    public static final URI container_of;

    /**
     * The content of the Item in plain text format.
     */
    public static final URI content;

    /**
     * The encoded content of the Post, contained in CDATA areas.
     */
    public static final URI content_encoded;

    /**
     * When this was created, in ISO 8601 format.
     */
    public static final URI created_at;

    /**
     * A resource that the UserAccount is a creator of.
     */
    public static final URI creator_of;

    /**
     * The content of the Post.
     */
    public static final URI description;

    /**
     * Links to a previous (older) revision of this Item or Post.
     */
    public static final URI earlier_version;

    /**
     * An electronic mail address of the UserAccount.
     */
    public static final URI email;

    /**
     * An electronic mail address of the UserAccount, encoded using SHA1.
     */
    public static final URI email_sha1;

    /**
     * This links Items to embedded statements, facts and structured content.
     */
    public static final URI embeds_knowledge;

    /**
     * A feed (e.g. RSS, Atom, etc.) pertaining to this resource (e.g. for a Forum, Site, UserAccount, etc.).
     */
    public static final URI feed;

    /**
     * First (real) name of this User. Synonyms include given name or christian name.
     */
    public static final URI first_name;

    /**
     * Indicates that one UserAccount follows another UserAccount (e.g. for microblog posts or other content item updates).
     */
    public static final URI follows;

    /**
     * A UserAccount that has this Role.
     */
    public static final URI function_of;

    /**
     * This property has been renamed. Use sioc:usergroup_of instead.
     */
    public static final URI group_of;

    /**
     * A UserAccount that is an administrator of this Site.
     */
    public static final URI has_administrator;

    /**
     * The Container to which this Item belongs.
     */
    public static final URI has_container;

    /**
     * This is the UserAccount that made this resource.
     */
    public static final URI has_creator;

    /**
     * The discussion that is related to this Item.
     */
    public static final URI has_discussion;

    /**
     * A Role that this UserAccount has.
     */
    public static final URI has_function;

    /**
     * This property has been renamed. Use sioc:has_usergroup instead.
     */
    public static final URI has_group;

    /**
     * The Site that hosts this Forum.
     */
    public static final URI has_host;

    /**
     * A UserAccount that is a member of this Usergroup.
     */
    public static final URI has_member;

    /**
     * A UserAccount that is a moderator of this Forum.
     */
    public static final URI has_moderator;

    /**
     * A UserAccount that modified this Item.
     */
    public static final URI has_modifier;

    /**
     * A UserAccount that this resource is owned by.
     */
    public static final URI has_owner;

    /**
     * A Container or Forum that this Container or Forum is a child of.
     */
    public static final URI has_parent;

    /**
     * An resource that is a part of this subject.
     */
    public static final URI has_part;

    /**
     * Points to an Item or Post that is a reply or response to this Item or Post.
     */
    public static final URI has_reply;

    /**
     * A resource that this Role applies to.
     */
    public static final URI has_scope;

    /**
     * A data Space which this resource is a part of.
     */
    public static final URI has_space;

    /**
     * A UserAccount that is subscribed to this Container.
     */
    public static final URI has_subscriber;

    /**
     * Points to a Usergroup that has certain access to this Space.
     */
    public static final URI has_usergroup;

    /**
     * A Forum that is hosted on this Site.
     */
    public static final URI host_of;

    /**
     * An identifier of a SIOC concept instance. For example, a user ID. Must be unique for instances of each type of SIOC concept within the same site.
     */
    public static final URI id;

    /**
     * The IP address used when creating this Item. This can be associated with a creator. Some wiki articles list the IP addresses for the creator or modifiers when the usernames are absent.
     */
    public static final URI ip_address;

    /**
     * The date and time of the last activity associated with a SIOC concept instance, and expressed in ISO 8601 format. This could be due to a reply Post or Comment, a modification to an Item, etc.
     */
    public static final URI last_activity_date;

    /**
     * The date and time of the last Post (or Item) in a Forum (or a Container), in ISO 8601 format.
     */
    public static final URI last_item_date;

    /**
     * Last (real) name of this user. Synonyms include surname or family name.
     */
    public static final URI last_name;

    /**
     * The date and time of the last reply Post or Comment, which could be associated with a starter Item or Post or with a Thread, and expressed in ISO 8601 format.
     */
    public static final URI last_reply_date;

    /**
     * Links to a later (newer) revision of this Item or Post.
     */
    public static final URI later_version;

    /**
     * Links to the latest revision of this Item or Post.
     */
    public static final URI latest_version;

    /**
     * A URI of a document which contains this SIOC object.
     */
    public static final URI link;

    /**
     * Links extracted from hyperlinks within a SIOC concept, e.g. Post or Site.
     */
    public static final URI links_to;

    /**
     * A Usergroup that this UserAccount is a member of.
     */
    public static final URI member_of;

    /**
     * A Forum that a UserAccount is a moderator of.
     */
    public static final URI moderator_of;

    /**
     * When this was modified, in ISO 8601 format.
     */
    public static final URI modified_at;

    /**
     * An Item that this UserAccount has modified.
     */
    public static final URI modifier_of;

    /**
     * The name of a SIOC concept instance, e.g. a username for a UserAccount, group name for a Usergroup, etc.
     */
    public static final URI name;

    /**
     * Next Item or Post in a given Container sorted by date.
     */
    public static final URI next_by_date;

    /**
     * Links to the next revision of this Item or Post.
     */
    public static final URI next_version;

    /**
     * A note associated with this resource, for example, if it has been edited by a UserAccount.
     */
    public static final URI note;

    /**
     * The number of unique authors (UserAccounts and unregistered posters) who have contributed to this Item, Thread, Post, etc.
     */
    public static final URI num_authors;

    /**
     * The number of Posts (or Items) in a Forum (or a Container).
     */
    public static final URI num_items;

    /**
     * The number of replies that this Item, Thread, Post, etc. has. Useful for when the reply structure is absent.
     */
    public static final URI num_replies;

    /**
     * The number of Threads (AKA discussion topics) in a Forum.
     */
    public static final URI num_threads;

    /**
     * The number of times this Item, Thread, UserAccount profile, etc. has been viewed.
     */
    public static final URI num_views;

    /**
     * A resource owned by a particular UserAccount, for example, a weblog or image gallery.
     */
    public static final URI owner_of;

    /**
     * A child Container or Forum that this Container or Forum is a parent of.
     */
    public static final URI parent_of;

    /**
     * A resource that the subject is a part of.
     */
    public static final URI part_of;

    /**
     * Previous Item or Post in a given Container sorted by date.
     */
    public static final URI previous_by_date;

    /**
     * Links to the previous revision of this Item or Post.
     */
    public static final URI previous_version;

    /**
     * Links either created explicitly or extracted implicitly on the HTML level from the Post.
     */
    public static final URI reference;

    /**
     * Related Posts for this Post, perhaps determined implicitly from topics or references.
     */
    public static final URI related_to;

    /**
     * Links to an Item or Post which this Item or Post is a reply to.
     */
    public static final URI reply_of;

    /**
     * A Role that has a scope of this resource.
     */
    public static final URI scope_of;

    /**
     * An Item may have a sibling or a twin that exists in a different Container, but the siblings may differ in some small way (for example, language, category, etc.). The sibling of this Item should be self-describing (that is, it should contain all available information).
     */
    public static final URI sibling;

    /**
     * A resource which belongs to this data Space.
     */
    public static final URI space_of;

    /**
     * Keyword(s) describing subject of the Post.
     */
    public static final URI subject;

    /**
     * A Container that a UserAccount is subscribed to.
     */
    public static final URI subscriber_of;

    /**
     * This is the title (subject line) of the Post. Note that for a Post within a threaded discussion that has no parents, it would detail the topic thread.
     */
    public static final URI title;

    /**
     * A topic of interest, linking to the appropriate URI, e.g. in the Open Directory Project or of a SKOS category.
     */
    public static final URI topic;

    /**
     * A Space that the Usergroup has access to.
     */
    public static final URI usergroup_of;


    static{
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Community = factory.createURI(SIOC.NAMESPACE, "Community");
        Container = factory.createURI(SIOC.NAMESPACE, "Container");
        Forum = factory.createURI(SIOC.NAMESPACE, "Forum");
        Item = factory.createURI(SIOC.NAMESPACE, "Item");
        Post = factory.createURI(SIOC.NAMESPACE, "Post");
        Role = factory.createURI(SIOC.NAMESPACE, "Role");
        Site = factory.createURI(SIOC.NAMESPACE, "Site");
        Space = factory.createURI(SIOC.NAMESPACE, "Space");
        Thread = factory.createURI(SIOC.NAMESPACE, "Thread");
        User = factory.createURI(SIOC.NAMESPACE, "User");
        UserAccount = factory.createURI(SIOC.NAMESPACE, "UserAccount");
        Usergroup = factory.createURI(SIOC.NAMESPACE, "Usergroup");
        about = factory.createURI(SIOC.NAMESPACE, "about");
        account_of = factory.createURI(SIOC.NAMESPACE, "account_of");
        addressed_to = factory.createURI(SIOC.NAMESPACE, "addressed_to");
        administrator_of = factory.createURI(SIOC.NAMESPACE, "administrator_of");
        attachment = factory.createURI(SIOC.NAMESPACE, "attachment");
        avatar = factory.createURI(SIOC.NAMESPACE, "avatar");
        container_of = factory.createURI(SIOC.NAMESPACE, "container_of");
        content = factory.createURI(SIOC.NAMESPACE, "content");
        content_encoded = factory.createURI(SIOC.NAMESPACE, "content_encoded");
        created_at = factory.createURI(SIOC.NAMESPACE, "created_at");
        creator_of = factory.createURI(SIOC.NAMESPACE, "creator_of");
        description = factory.createURI(SIOC.NAMESPACE, "description");
        earlier_version = factory.createURI(SIOC.NAMESPACE, "earlier_version");
        email = factory.createURI(SIOC.NAMESPACE, "email");
        email_sha1 = factory.createURI(SIOC.NAMESPACE, "email_sha1");
        embeds_knowledge = factory.createURI(SIOC.NAMESPACE, "embeds_knowledge");
        feed = factory.createURI(SIOC.NAMESPACE, "feed");
        first_name = factory.createURI(SIOC.NAMESPACE, "first_name");
        follows = factory.createURI(SIOC.NAMESPACE, "follows");
        function_of = factory.createURI(SIOC.NAMESPACE, "function_of");
        group_of = factory.createURI(SIOC.NAMESPACE, "group_of");
        has_administrator = factory.createURI(SIOC.NAMESPACE, "has_administrator");
        has_container = factory.createURI(SIOC.NAMESPACE, "has_container");
        has_creator = factory.createURI(SIOC.NAMESPACE, "has_creator");
        has_discussion = factory.createURI(SIOC.NAMESPACE, "has_discussion");
        has_function = factory.createURI(SIOC.NAMESPACE, "has_function");
        has_group = factory.createURI(SIOC.NAMESPACE, "has_group");
        has_host = factory.createURI(SIOC.NAMESPACE, "has_host");
        has_member = factory.createURI(SIOC.NAMESPACE, "has_member");
        has_moderator = factory.createURI(SIOC.NAMESPACE, "has_moderator");
        has_modifier = factory.createURI(SIOC.NAMESPACE, "has_modifier");
        has_owner = factory.createURI(SIOC.NAMESPACE, "has_owner");
        has_parent = factory.createURI(SIOC.NAMESPACE, "has_parent");
        has_part = factory.createURI(SIOC.NAMESPACE, "has_part");
        has_reply = factory.createURI(SIOC.NAMESPACE, "has_reply");
        has_scope = factory.createURI(SIOC.NAMESPACE, "has_scope");
        has_space = factory.createURI(SIOC.NAMESPACE, "has_space");
        has_subscriber = factory.createURI(SIOC.NAMESPACE, "has_subscriber");
        has_usergroup = factory.createURI(SIOC.NAMESPACE, "has_usergroup");
        host_of = factory.createURI(SIOC.NAMESPACE, "host_of");
        id = factory.createURI(SIOC.NAMESPACE, "id");
        ip_address = factory.createURI(SIOC.NAMESPACE, "ip_address");
        last_activity_date = factory.createURI(SIOC.NAMESPACE, "last_activity_date");
        last_item_date = factory.createURI(SIOC.NAMESPACE, "last_item_date");
        last_name = factory.createURI(SIOC.NAMESPACE, "last_name");
        last_reply_date = factory.createURI(SIOC.NAMESPACE, "last_reply_date");
        later_version = factory.createURI(SIOC.NAMESPACE, "later_version");
        latest_version = factory.createURI(SIOC.NAMESPACE, "latest_version");
        link = factory.createURI(SIOC.NAMESPACE, "link");
        links_to = factory.createURI(SIOC.NAMESPACE, "links_to");
        member_of = factory.createURI(SIOC.NAMESPACE, "member_of");
        moderator_of = factory.createURI(SIOC.NAMESPACE, "moderator_of");
        modified_at = factory.createURI(SIOC.NAMESPACE, "modified_at");
        modifier_of = factory.createURI(SIOC.NAMESPACE, "modifier_of");
        name = factory.createURI(SIOC.NAMESPACE, "name");
        next_by_date = factory.createURI(SIOC.NAMESPACE, "next_by_date");
        next_version = factory.createURI(SIOC.NAMESPACE, "next_version");
        note = factory.createURI(SIOC.NAMESPACE, "note");
        num_authors = factory.createURI(SIOC.NAMESPACE, "num_authors");
        num_items = factory.createURI(SIOC.NAMESPACE, "num_items");
        num_replies = factory.createURI(SIOC.NAMESPACE, "num_replies");
        num_threads = factory.createURI(SIOC.NAMESPACE, "num_threads");
        num_views = factory.createURI(SIOC.NAMESPACE, "num_views");
        owner_of = factory.createURI(SIOC.NAMESPACE, "owner_of");
        parent_of = factory.createURI(SIOC.NAMESPACE, "parent_of");
        part_of = factory.createURI(SIOC.NAMESPACE, "part_of");
        previous_by_date = factory.createURI(SIOC.NAMESPACE, "previous_by_date");
        previous_version = factory.createURI(SIOC.NAMESPACE, "previous_version");
        reference = factory.createURI(SIOC.NAMESPACE, "reference");
        related_to = factory.createURI(SIOC.NAMESPACE, "related_to");
        reply_of = factory.createURI(SIOC.NAMESPACE, "reply_of");
        scope_of = factory.createURI(SIOC.NAMESPACE, "scope_of");
        sibling = factory.createURI(SIOC.NAMESPACE, "sibling");
        space_of = factory.createURI(SIOC.NAMESPACE, "space_of");
        subject = factory.createURI(SIOC.NAMESPACE, "subject");
        subscriber_of = factory.createURI(SIOC.NAMESPACE, "subscriber_of");
        title = factory.createURI(SIOC.NAMESPACE, "title");
        topic = factory.createURI(SIOC.NAMESPACE, "topic");
        usergroup_of = factory.createURI(SIOC.NAMESPACE, "usergroup_of");
    }
}
