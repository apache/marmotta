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
        Agent = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/Agent");
        Document = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/Document");
        Group = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/Group");
        Image = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/Image");
        LabelProperty = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/LabelProperty");
        OnlineAccount = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/OnlineAccount");
        OnlineChatAccount = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/OnlineChatAccount");
        OnlineEcommerceAccount = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/OnlineEcommerceAccount");
        OnlineGamingAccount = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/OnlineGamingAccount");
        Organization = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/Organization");
        Person = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/Person");
        PersonalProfileDocument = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/PersonalProfileDocument");
        Project = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/Project");
        account = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/account");
        accountName = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/accountName");
        accountServiceHomepage = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/accountServiceHomepage");
        age = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/age");
        aimChatID = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/aimChatID");
        based_near = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/based_near");
        birthday = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/birthday");
        currentProject = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/currentProject");
        depiction = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/depiction");
        depicts = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/depicts");
        dnaChecksum = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/dnaChecksum");
        familyName = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/familyName");
        family_name = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/family_name");
        firstName = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/firstName");
        focus = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/focus");
        fundedBy = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/fundedBy");
        geekcode = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/geekcode");
        gender = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/gender");
        givenName = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/givenName");
        givenname = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/givenname");
        holdsAccount = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/holdsAccount");
        homepage = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/homepage");
        icqChatID = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/icqChatID");
        img = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/img");
        interest = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/interest");
        isPrimaryTopicOf = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/isPrimaryTopicOf");
        jabberID = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/jabberID");
        knows = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/knows");
        lastName = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/lastName");
        logo = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/logo");
        made = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/made");
        maker = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/maker");
        mbox = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/mbox");
        mbox_sha1sum = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/mbox_sha1sum");
        member = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/member");
        membershipClass = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/membershipClass");
        msnChatID = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/msnChatID");
        myersBriggs = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/myersBriggs");
        name = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/name");
        nick = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/nick");
        openid = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/openid");
        page = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/page");
        pastProject = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/pastProject");
        phone = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/phone");
        plan = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/plan");
        primaryTopic = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/primaryTopic");
        publications = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/publications");
        schoolHomepage = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/schoolHomepage");
        sha1 = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/sha1");
        skypeID = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/skypeID");
        status = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/status");
        surname = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/surname");
        theme = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/theme");
        thumbnail = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/thumbnail");
        tipjar = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/tipjar");
        title = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/title");
        topic = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/topic");
        topic_interest = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/topic_interest");
        weblog = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/weblog");
        workInfoHomepage = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/workInfoHomepage");
        workplaceHomepage = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/workplaceHomepage");
        yahooChatID = factory.createURI(FOAF.NAMESPACE, "http://xmlns.com/foaf/0.1/yahooChatID");
    }
}
