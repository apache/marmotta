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
 * Namespace MA
 */
public class MA {

    public static final String NAMESPACE = "http://www.w3.org/ns/ma-ont#";

    public static final String PREFIX = "ma";

    /**
     * A person or organisation contributing to the media resource.
     */
    public static final URI Agent;

    /**
     * A specialisation of Track for Audio to provide a link to specific data properties such as sampleRate, etc. Specialisation is defined through object properties.
     */
    public static final URI AudioTrack;

    /**
     * Any group of media resource e.g. a series.
     */
    public static final URI Collection;

    /**
     * Ancillary data track e.g. ¨captioning"  in addition to video and audio tracks. Specialisation is made through the use of appropriate object properties.
     */
    public static final URI DataTrack;

    /**
     * A still image / thumbnail / key frame related to the media resource or being the media resource itself.
     */
    public static final URI Image;

    public static final URI IsRatingOf;

    /**
     * A location related to the media resource, e.g. depicted in the resource (possibly fictional) or where the resource was created (shooting location), etc.
     */
    public static final URI Location;

    /**
     * A media fragment (spatial, temporal, track...) composing a media resource. In other ontologies fragment is sometimes referred to as a 'part' or 'segment'.
     */
    public static final URI MediaFragment;

    /**
     * An image or an audiovisual media resource, which can be composed of one or more fragment / track.
     */
    public static final URI MediaResource;

    /**
     * An organisation or moral agent.
     */
    public static final URI Organisation;

    /**
     * A physical person.
     */
    public static final URI Person;

    /**
     * Information about the rating given to a media resource.
     */
    public static final URI Rating;

    /**
     * Information about The target audience (target region, target audience category but also parental guidance recommendation) for which a media resource is intended.
     */
    public static final URI TargetAudience;

    /**
     * A specialisation of MediaFragment for audiovisual content.
     */
    public static final URI Track;

    /**
     * A specialisation of Track for Video to provide a link to specific data properties such as frameRate, etc. Signing is another possible example of video track. Specialisation is defined through object properties.
     */
    public static final URI VideoTrack;

    /**
     * Corresponds to 'title.title' in the Ontology for Media Resources with a 'title.type' meaning "alternative".
     */
    public static final URI alternativeTitle;

    /**
     * Corresponds to 'averageBitRate' in the Ontology for Media Resources, expressed in kilobits/second.
     */
    public static final URI averageBitRate;

    /**
     * The name by which a collection (e.g. series) is known.
     */
    public static final URI collectionName;

    /**
     * Corresponds to 'copyright.copyright' in the Ontology for Media Resources.
     */
    public static final URI copyright;

    /**
     * A subproperty of 'hasRelatedLocation" used to specify where material shooting took place.
     */
    public static final URI createdIn;

    /**
     * Corresponds to 'date.date' in the Ontology for Media Resources with a 'date.type' meaning "creationDate".
     */
    public static final URI creationDate;

    /**
     * Corresponds to date.date in the ontology for Media Resources. Subproperties can be used to distinguish different values of 'date.type'. The recommended range is 'xsd:dateTime' (for compliance with OWL2-QL and OWL2-RL) but other time-related datatypes may be used (e.g. 'xsd:gYear', 'xsd:date'...).
     */
    public static final URI date;

    /**
     * A subproperty of 'hasRelatedLocation' used to specify where the action depicted in the media is supposed to take place, as opposed to the location where shooting actually took place (see 'createdIn').
     */
    public static final URI depictsFictionalLocation;

    /**
     * Corresponds to 'description' in the Ontology for Media Resources. This can be specialised by using sub-properties e.g. 'summary' or 'script'.
     */
    public static final URI description;

    /**
     * Corresponds to 'duration' in the Ontology for Media Resources.
     */
    public static final URI duration;

    /**
     * Corresponds to 'date.date' in the Ontology for Media Resources with a 'date.type' meaning "editDate".
     */
    public static final URI editDate;

    /**
     * Corresponds to 'contributor.contributor' in the Ontology for Media Resources with a 'contributor.role' meaning "actor".
     */
    public static final URI features;

    /**
     * Corresponds to 'namedFragment.label' in the Ontology for Media Resources.
     */
    public static final URI fragmentName;

    /**
     * Corresponds to 'frameSize.height' in the Ontology for Media Resources, measured in frameSizeUnit.
     */
    public static final URI frameHeight;

    /**
     * Corresponds to 'frameRate' in the Ontology for Media Resources, in frame per second.
     */
    public static final URI frameRate;

    /**
     * Corresponds to 'frameSize.unit' in the Ontology for Media Resources.
     */
    public static final URI frameSizeUnit;

    /**
     * Corresponds to 'frameSize.width' in the Ontology for Media Resources measured in frameSizeUnit.
     */
    public static final URI frameWidth;

    /**
     * Corresponds to 'policy' in the Ontology for Media Resources with a 'policy.type' "access conditions".
     */
    public static final URI hasAccessConditions;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "audio-description".
     */
    public static final URI hasAudioDescription;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "captioning". This property can for example point to a spatial fragment, a VideoTrack or a DataTrack. The language of the captioning track can be expressed by attaching a 'hasLanguage' property to the specific track.
     */
    public static final URI hasCaptioning;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "actor".
     */
    public static final URI hasChapter;

    /**
     * Corresponds to 'targetAudience.classification' in the Ontology for Media Resources. This property is used to provide a value characterising the target audience.
     */
    public static final URI hasClassification;

    /**
     * Corresponds to 'targetAudience.identifier' in the Ontology for Media Resources. This is used to identify the reference sheme against which the target audience has been characterised.
     */
    public static final URI hasClassificationSystem;

    /**
     * Corresponds to 'compression' in the Ontology for Media Resources.
     */
    public static final URI hasCompression;

    public static final URI hasContributedTo;

    /**
     * Corresponds to 'contributor.contributor' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'contributor.role'.
     */
    public static final URI hasContributor;

    public static final URI hasCopyrightOver;

    public static final URI hasCreated;

    /**
     * Corresponds to 'creator.creator' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'creator.role'. Note that this property is semantically a subproperty of 'hasContributor'.
     */
    public static final URI hasCreator;

    /**
     * Corresponds to 'format' in the Ontology for Media Resources.
     */
    public static final URI hasFormat;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'fragment.role'.
     */
    public static final URI hasFragment;

    /**
     * Corresponds to 'genre' in the Ontology for Media Resources.
     */
    public static final URI hasGenre;

    /**
     * Corresponds to 'keyword' in the Ontology for Media Resources.
     */
    public static final URI hasKeyword;

    /**
     * Corresponds to 'language' in the Ontology for Media Resources. The language used in the resource. A controlled vocabulary such as defined in BCP 47 SHOULD be used. This property can also be used to identify the presence of sign language (RFC 5646). By inheritance, the hasLanguage property applies indifferently at the media resource / fragment / track levels.  Best practice recommends to use to best possible level of granularity fo describe the usage of language within a media resource including at fragment and track levels.
     */
    public static final URI hasLanguage;

    /**
     * Corresponds to 'location.coordinateSystem' in the Ontology for Media Resources.
     */
    public static final URI hasLocationCoordinateSystem;

    public static final URI hasMember;

    /**
     * Corresponds to 'namedFragment' in the Ontology for Media Resources.
     */
    public static final URI hasNamedFragment;

    /**
     * Corresponds to 'policy' in the Ontology for Media Resources with a  'policy.type' meaning "permissions".
     */
    public static final URI hasPermissions;

    /**
     * Corresponds to 'policy' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'policy.type'.
     */
    public static final URI hasPolicy;

    public static final URI hasPublished;

    /**
     * Corresponds to 'publisher' in the Ontology for Media Resources.
     */
    public static final URI hasPublisher;

    /**
     * Corresponds to 'rating' in the Ontology for Media Resources.
     */
    public static final URI hasRating;

    /**
     * Corresponds to 'rating.type' in the Ontology for Media Resources.
     */
    public static final URI hasRatingSystem;

    /**
     * Corresponds to 'relation' and in the Ontology for Media Resources with a 'relation.type' meaning "related image".
     */
    public static final URI hasRelatedImage;

    /**
     * Corresponds to 'location' in the Ontology for Media Resources. Subproperties are provided to specify, when possible, the relation between the media resource and the location.
     */
    public static final URI hasRelatedLocation;

    /**
     * Corresponds to 'relation' and in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'relation.type'.
     */
    public static final URI hasRelatedResource;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "signing". This property can for example point to a spatial fragment or a VideoTrack. The sign language of the captioning track can be expressed by attaching a 'hasLanguage' property to the specific track.
     */
    public static final URI hasSigning;

    /**
     * Corresponds to 'relation' and in the Ontology for Media Resources with a 'relation.type' meaning "source".
     */
    public static final URI hasSource;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "subtitling".
     */
    public static final URI hasSubtitling;

    /**
     * Corresponds to 'targetAudience' in the Ontology for Media Resources.
     */
    public static final URI hasTargetAudience;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "track".
     */
    public static final URI hasTrack;

    public static final URI isCaptioningOf;

    public static final URI isChapterOf;

    /**
     * Corresponds to 'copyright.identifier' in the Ontology for Media Resources.
     */
    public static final URI isCopyrightedBy;

    public static final URI isCreationLocationOf;

    public static final URI isFictionalLocationDepictedIn;

    public static final URI isFragmentOf;

    public static final URI isImageRelatedTo;

    public static final URI isLocationRelatedTo;

    /**
     * Corresponds to 'collection' in the Ontology for Media Resources.
     */
    public static final URI isMemberOf;

    public static final URI isNamedFragmentOf;

    /**
     * Corresponds to 'rating.identifier' in the Ontology for Media Resources.
     */
    public static final URI isProvidedBy;

    public static final URI isRelatedTo;

    public static final URI isSigningOf;

    public static final URI isSourceOf;

    public static final URI isTargetAudienceOf;

    public static final URI isTrackOf;

    /**
     * Corresponds to 'location.altitude' in the Ontology for Media Resources.
     */
    public static final URI locationAltitude;

    /**
     * Corresponds to 'location.latitude' in the Ontology for Media Resources.
     */
    public static final URI locationLatitude;

    /**
     * Corresponds to 'location.longitude' in the Ontology for Media Resources.
     */
    public static final URI locationLongitude;

    /**
     * Corresponds to 'location.name' in the Ontology for Media Resources.
     */
    public static final URI locationName;

    /**
     * Corresponds to 'locator' in the Ontology for Media Resources.
     */
    public static final URI locator;

    /**
     * Corresponds to 'title.title' in the Ontology for Media Resources with a 'title.type' meaning "original".
     */
    public static final URI mainOriginalTitle;

    /**
     * Corresponds to 'numTracks.number' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'numTracks.type'.
     */
    public static final URI numberOfTracks;

    public static final URI playsIn;

    public static final URI provides;

    /**
     * Corresponds to 'rating.max' in the Ontology for Media Resources.
     */
    public static final URI ratingScaleMax;

    /**
     * Corresponds to 'rating.min' in the Ontology for Media Resources.
     */
    public static final URI ratingScaleMin;

    /**
     * Corresponds to 'rating.value' in the Ontology for Media Resources.
     */
    public static final URI ratingValue;

    /**
     * Corresponds to 'date.date' in the Ontology for Media Resources with a 'date.type' meaning "recordDate".
     */
    public static final URI recordDate;

    /**
     * Corresponds to 'date.date' in the Ontology for Media Resources with a 'date.type' meaning "releaseDate".
     */
    public static final URI releaseDate;

    /**
     * Corresponds to 'samplingRate' in the Ontology for Media Resources, in samples per second.
     */
    public static final URI samplingRate;

    /**
     * Corresponds to 'title.title' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'title.type'.
     */
    public static final URI title;

    /**
     * Corresponds to 'fragment.name' in the Ontology for Media Resources, for Track fragments.
     */
    public static final URI trackName;


    static{
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Agent = factory.createURI(MA.NAMESPACE, "Agent");
        AudioTrack = factory.createURI(MA.NAMESPACE, "AudioTrack");
        Collection = factory.createURI(MA.NAMESPACE, "Collection");
        DataTrack = factory.createURI(MA.NAMESPACE, "DataTrack");
        Image = factory.createURI(MA.NAMESPACE, "Image");
        IsRatingOf = factory.createURI(MA.NAMESPACE, "IsRatingOf");
        Location = factory.createURI(MA.NAMESPACE, "Location");
        MediaFragment = factory.createURI(MA.NAMESPACE, "MediaFragment");
        MediaResource = factory.createURI(MA.NAMESPACE, "MediaResource");
        Organisation = factory.createURI(MA.NAMESPACE, "Organisation");
        Person = factory.createURI(MA.NAMESPACE, "Person");
        Rating = factory.createURI(MA.NAMESPACE, "Rating");
        TargetAudience = factory.createURI(MA.NAMESPACE, "TargetAudience");
        Track = factory.createURI(MA.NAMESPACE, "Track");
        VideoTrack = factory.createURI(MA.NAMESPACE, "VideoTrack");
        alternativeTitle = factory.createURI(MA.NAMESPACE, "alternativeTitle");
        averageBitRate = factory.createURI(MA.NAMESPACE, "averageBitRate");
        collectionName = factory.createURI(MA.NAMESPACE, "collectionName");
        copyright = factory.createURI(MA.NAMESPACE, "copyright");
        createdIn = factory.createURI(MA.NAMESPACE, "createdIn");
        creationDate = factory.createURI(MA.NAMESPACE, "creationDate");
        date = factory.createURI(MA.NAMESPACE, "date");
        depictsFictionalLocation = factory.createURI(MA.NAMESPACE, "depictsFictionalLocation");
        description = factory.createURI(MA.NAMESPACE, "description");
        duration = factory.createURI(MA.NAMESPACE, "duration");
        editDate = factory.createURI(MA.NAMESPACE, "editDate");
        features = factory.createURI(MA.NAMESPACE, "features");
        fragmentName = factory.createURI(MA.NAMESPACE, "fragmentName");
        frameHeight = factory.createURI(MA.NAMESPACE, "frameHeight");
        frameRate = factory.createURI(MA.NAMESPACE, "frameRate");
        frameSizeUnit = factory.createURI(MA.NAMESPACE, "frameSizeUnit");
        frameWidth = factory.createURI(MA.NAMESPACE, "frameWidth");
        hasAccessConditions = factory.createURI(MA.NAMESPACE, "hasAccessConditions");
        hasAudioDescription = factory.createURI(MA.NAMESPACE, "hasAudioDescription");
        hasCaptioning = factory.createURI(MA.NAMESPACE, "hasCaptioning");
        hasChapter = factory.createURI(MA.NAMESPACE, "hasChapter");
        hasClassification = factory.createURI(MA.NAMESPACE, "hasClassification");
        hasClassificationSystem = factory.createURI(MA.NAMESPACE, "hasClassificationSystem");
        hasCompression = factory.createURI(MA.NAMESPACE, "hasCompression");
        hasContributedTo = factory.createURI(MA.NAMESPACE, "hasContributedTo");
        hasContributor = factory.createURI(MA.NAMESPACE, "hasContributor");
        hasCopyrightOver = factory.createURI(MA.NAMESPACE, "hasCopyrightOver");
        hasCreated = factory.createURI(MA.NAMESPACE, "hasCreated");
        hasCreator = factory.createURI(MA.NAMESPACE, "hasCreator");
        hasFormat = factory.createURI(MA.NAMESPACE, "hasFormat");
        hasFragment = factory.createURI(MA.NAMESPACE, "hasFragment");
        hasGenre = factory.createURI(MA.NAMESPACE, "hasGenre");
        hasKeyword = factory.createURI(MA.NAMESPACE, "hasKeyword");
        hasLanguage = factory.createURI(MA.NAMESPACE, "hasLanguage");
        hasLocationCoordinateSystem = factory.createURI(MA.NAMESPACE, "hasLocationCoordinateSystem");
        hasMember = factory.createURI(MA.NAMESPACE, "hasMember");
        hasNamedFragment = factory.createURI(MA.NAMESPACE, "hasNamedFragment");
        hasPermissions = factory.createURI(MA.NAMESPACE, "hasPermissions");
        hasPolicy = factory.createURI(MA.NAMESPACE, "hasPolicy");
        hasPublished = factory.createURI(MA.NAMESPACE, "hasPublished");
        hasPublisher = factory.createURI(MA.NAMESPACE, "hasPublisher");
        hasRating = factory.createURI(MA.NAMESPACE, "hasRating");
        hasRatingSystem = factory.createURI(MA.NAMESPACE, "hasRatingSystem");
        hasRelatedImage = factory.createURI(MA.NAMESPACE, "hasRelatedImage");
        hasRelatedLocation = factory.createURI(MA.NAMESPACE, "hasRelatedLocation");
        hasRelatedResource = factory.createURI(MA.NAMESPACE, "hasRelatedResource");
        hasSigning = factory.createURI(MA.NAMESPACE, "hasSigning");
        hasSource = factory.createURI(MA.NAMESPACE, "hasSource");
        hasSubtitling = factory.createURI(MA.NAMESPACE, "hasSubtitling");
        hasTargetAudience = factory.createURI(MA.NAMESPACE, "hasTargetAudience");
        hasTrack = factory.createURI(MA.NAMESPACE, "hasTrack");
        isCaptioningOf = factory.createURI(MA.NAMESPACE, "isCaptioningOf");
        isChapterOf = factory.createURI(MA.NAMESPACE, "isChapterOf");
        isCopyrightedBy = factory.createURI(MA.NAMESPACE, "isCopyrightedBy");
        isCreationLocationOf = factory.createURI(MA.NAMESPACE, "isCreationLocationOf");
        isFictionalLocationDepictedIn = factory.createURI(MA.NAMESPACE, "isFictionalLocationDepictedIn");
        isFragmentOf = factory.createURI(MA.NAMESPACE, "isFragmentOf");
        isImageRelatedTo = factory.createURI(MA.NAMESPACE, "isImageRelatedTo");
        isLocationRelatedTo = factory.createURI(MA.NAMESPACE, "isLocationRelatedTo");
        isMemberOf = factory.createURI(MA.NAMESPACE, "isMemberOf");
        isNamedFragmentOf = factory.createURI(MA.NAMESPACE, "isNamedFragmentOf");
        isProvidedBy = factory.createURI(MA.NAMESPACE, "isProvidedBy");
        isRelatedTo = factory.createURI(MA.NAMESPACE, "isRelatedTo");
        isSigningOf = factory.createURI(MA.NAMESPACE, "isSigningOf");
        isSourceOf = factory.createURI(MA.NAMESPACE, "isSourceOf");
        isTargetAudienceOf = factory.createURI(MA.NAMESPACE, "isTargetAudienceOf");
        isTrackOf = factory.createURI(MA.NAMESPACE, "isTrackOf");
        locationAltitude = factory.createURI(MA.NAMESPACE, "locationAltitude");
        locationLatitude = factory.createURI(MA.NAMESPACE, "locationLatitude");
        locationLongitude = factory.createURI(MA.NAMESPACE, "locationLongitude");
        locationName = factory.createURI(MA.NAMESPACE, "locationName");
        locator = factory.createURI(MA.NAMESPACE, "locator");
        mainOriginalTitle = factory.createURI(MA.NAMESPACE, "mainOriginalTitle");
        numberOfTracks = factory.createURI(MA.NAMESPACE, "numberOfTracks");
        playsIn = factory.createURI(MA.NAMESPACE, "playsIn");
        provides = factory.createURI(MA.NAMESPACE, "provides");
        ratingScaleMax = factory.createURI(MA.NAMESPACE, "ratingScaleMax");
        ratingScaleMin = factory.createURI(MA.NAMESPACE, "ratingScaleMin");
        ratingValue = factory.createURI(MA.NAMESPACE, "ratingValue");
        recordDate = factory.createURI(MA.NAMESPACE, "recordDate");
        releaseDate = factory.createURI(MA.NAMESPACE, "releaseDate");
        samplingRate = factory.createURI(MA.NAMESPACE, "samplingRate");
        title = factory.createURI(MA.NAMESPACE, "title");
        trackName = factory.createURI(MA.NAMESPACE, "trackName");
    }
}
