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
 * Namespace MA
 */
public class MA {

    public static final String NAMESPACE = "http://www.w3.org/ns/ma-ont#";

    public static final String PREFIX = "ma";

    /**
     * A person or organisation contributing to the media resource.
     */
    public static final IRI Agent;

    /**
     * A specialisation of Track for Audio to provide a link to specific data properties such as sampleRate, etc. Specialisation is defined through object properties.
     */
    public static final IRI AudioTrack;

    /**
     * Any group of media resource e.g. a series.
     */
    public static final IRI Collection;

    /**
     * Ancillary data track e.g. ¨captioning"  in addition to video and audio tracks. Specialisation is made through the use of appropriate object properties.
     */
    public static final IRI DataTrack;

    /**
     * A still image / thumbnail / key frame related to the media resource or being the media resource itself.
     */
    public static final IRI Image;

    public static final IRI IsRatingOf;

    /**
     * A location related to the media resource, e.g. depicted in the resource (possibly fictional) or where the resource was created (shooting location), etc.
     */
    public static final IRI Location;

    /**
     * A media fragment (spatial, temporal, track...) composing a media resource. In other ontologies fragment is sometimes referred to as a 'part' or 'segment'.
     */
    public static final IRI MediaFragment;

    /**
     * An image or an audiovisual media resource, which can be composed of one or more fragment / track.
     */
    public static final IRI MediaResource;

    /**
     * An organisation or moral agent.
     */
    public static final IRI Organisation;

    /**
     * A physical person.
     */
    public static final IRI Person;

    /**
     * Information about the rating given to a media resource.
     */
    public static final IRI Rating;

    /**
     * Information about The target audience (target region, target audience category but also parental guidance recommendation) for which a media resource is intended.
     */
    public static final IRI TargetAudience;

    /**
     * A specialisation of MediaFragment for audiovisual content.
     */
    public static final IRI Track;

    /**
     * A specialisation of Track for Video to provide a link to specific data properties such as frameRate, etc. Signing is another possible example of video track. Specialisation is defined through object properties.
     */
    public static final IRI VideoTrack;

    /**
     * Corresponds to 'title.title' in the Ontology for Media Resources with a 'title.type' meaning "alternative".
     */
    public static final IRI alternativeTitle;

    /**
     * Corresponds to 'averageBitRate' in the Ontology for Media Resources, expressed in kilobits/second.
     */
    public static final IRI averageBitRate;

    /**
     * The name by which a collection (e.g. series) is known.
     */
    public static final IRI collectionName;

    /**
     * Corresponds to 'copyright.copyright' in the Ontology for Media Resources.
     */
    public static final IRI copyright;

    /**
     * A subproperty of 'hasRelatedLocation" used to specify where material shooting took place.
     */
    public static final IRI createdIn;

    /**
     * Corresponds to 'date.date' in the Ontology for Media Resources with a 'date.type' meaning "creationDate".
     */
    public static final IRI creationDate;

    /**
     * Corresponds to date.date in the ontology for Media Resources. Subproperties can be used to distinguish different values of 'date.type'. The recommended range is 'xsd:dateTime' (for compliance with OWL2-QL and OWL2-RL) but other time-related datatypes may be used (e.g. 'xsd:gYear', 'xsd:date'...).
     */
    public static final IRI date;

    /**
     * A subproperty of 'hasRelatedLocation' used to specify where the action depicted in the media is supposed to take place, as opposed to the location where shooting actually took place (see 'createdIn').
     */
    public static final IRI depictsFictionalLocation;

    /**
     * Corresponds to 'description' in the Ontology for Media Resources. This can be specialised by using sub-properties e.g. 'summary' or 'script'.
     */
    public static final IRI description;

    /**
     * Corresponds to 'duration' in the Ontology for Media Resources.
     */
    public static final IRI duration;

    /**
     * Corresponds to 'date.date' in the Ontology for Media Resources with a 'date.type' meaning "editDate".
     */
    public static final IRI editDate;

    /**
     * Corresponds to 'contributor.contributor' in the Ontology for Media Resources with a 'contributor.role' meaning "actor".
     */
    public static final IRI features;

    /**
     * Corresponds to 'namedFragment.label' in the Ontology for Media Resources.
     */
    public static final IRI fragmentName;

    /**
     * Corresponds to 'frameSize.height' in the Ontology for Media Resources, measured in frameSizeUnit.
     */
    public static final IRI frameHeight;

    /**
     * Corresponds to 'frameRate' in the Ontology for Media Resources, in frame per second.
     */
    public static final IRI frameRate;

    /**
     * Corresponds to 'frameSize.unit' in the Ontology for Media Resources.
     */
    public static final IRI frameSizeUnit;

    /**
     * Corresponds to 'frameSize.width' in the Ontology for Media Resources measured in frameSizeUnit.
     */
    public static final IRI frameWidth;

    /**
     * Corresponds to 'policy' in the Ontology for Media Resources with a 'policy.type' "access conditions".
     */
    public static final IRI hasAccessConditions;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "audio-description".
     */
    public static final IRI hasAudioDescription;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "captioning". This property can for example point to a spatial fragment, a VideoTrack or a DataTrack. The language of the captioning track can be expressed by attaching a 'hasLanguage' property to the specific track.
     */
    public static final IRI hasCaptioning;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "actor".
     */
    public static final IRI hasChapter;

    /**
     * Corresponds to 'targetAudience.classification' in the Ontology for Media Resources. This property is used to provide a value characterising the target audience.
     */
    public static final IRI hasClassification;

    /**
     * Corresponds to 'targetAudience.identifier' in the Ontology for Media Resources. This is used to identify the reference sheme against which the target audience has been characterised.
     */
    public static final IRI hasClassificationSystem;

    /**
     * Corresponds to 'compression' in the Ontology for Media Resources.
     */
    public static final IRI hasCompression;

    public static final IRI hasContributedTo;

    /**
     * Corresponds to 'contributor.contributor' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'contributor.role'.
     */
    public static final IRI hasContributor;

    public static final IRI hasCopyrightOver;

    public static final IRI hasCreated;

    /**
     * Corresponds to 'creator.creator' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'creator.role'. Note that this property is semantically a subproperty of 'hasContributor'.
     */
    public static final IRI hasCreator;

    /**
     * Corresponds to 'format' in the Ontology for Media Resources.
     */
    public static final IRI hasFormat;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'fragment.role'.
     */
    public static final IRI hasFragment;

    /**
     * Corresponds to 'genre' in the Ontology for Media Resources.
     */
    public static final IRI hasGenre;

    /**
     * Corresponds to 'keyword' in the Ontology for Media Resources.
     */
    public static final IRI hasKeyword;

    /**
     * Corresponds to 'language' in the Ontology for Media Resources. The language used in the resource. A controlled vocabulary such as defined in BCP 47 SHOULD be used. This property can also be used to identify the presence of sign language (RFC 5646). By inheritance, the hasLanguage property applies indifferently at the media resource / fragment / track levels.  Best practice recommends to use to best possible level of granularity fo describe the usage of language within a media resource including at fragment and track levels.
     */
    public static final IRI hasLanguage;

    /**
     * Corresponds to 'location.coordinateSystem' in the Ontology for Media Resources.
     */
    public static final IRI hasLocationCoordinateSystem;

    public static final IRI hasMember;

    /**
     * Corresponds to 'namedFragment' in the Ontology for Media Resources.
     */
    public static final IRI hasNamedFragment;

    /**
     * Corresponds to 'policy' in the Ontology for Media Resources with a  'policy.type' meaning "permissions".
     */
    public static final IRI hasPermissions;

    /**
     * Corresponds to 'policy' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'policy.type'.
     */
    public static final IRI hasPolicy;

    public static final IRI hasPublished;

    /**
     * Corresponds to 'publisher' in the Ontology for Media Resources.
     */
    public static final IRI hasPublisher;

    /**
     * Corresponds to 'rating' in the Ontology for Media Resources.
     */
    public static final IRI hasRating;

    /**
     * Corresponds to 'rating.type' in the Ontology for Media Resources.
     */
    public static final IRI hasRatingSystem;

    /**
     * Corresponds to 'relation' and in the Ontology for Media Resources with a 'relation.type' meaning "related image".
     */
    public static final IRI hasRelatedImage;

    /**
     * Corresponds to 'location' in the Ontology for Media Resources. Subproperties are provided to specify, when possible, the relation between the media resource and the location.
     */
    public static final IRI hasRelatedLocation;

    /**
     * Corresponds to 'relation' and in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'relation.type'.
     */
    public static final IRI hasRelatedResource;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "signing". This property can for example point to a spatial fragment or a VideoTrack. The sign language of the captioning track can be expressed by attaching a 'hasLanguage' property to the specific track.
     */
    public static final IRI hasSigning;

    /**
     * Corresponds to 'relation' and in the Ontology for Media Resources with a 'relation.type' meaning "source".
     */
    public static final IRI hasSource;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "subtitling".
     */
    public static final IRI hasSubtitling;

    /**
     * Corresponds to 'targetAudience' in the Ontology for Media Resources.
     */
    public static final IRI hasTargetAudience;

    /**
     * Corresponds to 'fragment' in the Ontology for Media Resources with a 'fragment.role' meaning "track".
     */
    public static final IRI hasTrack;

    public static final IRI isCaptioningOf;

    public static final IRI isChapterOf;

    /**
     * Corresponds to 'copyright.identifier' in the Ontology for Media Resources.
     */
    public static final IRI isCopyrightedBy;

    public static final IRI isCreationLocationOf;

    public static final IRI isFictionalLocationDepictedIn;

    public static final IRI isFragmentOf;

    public static final IRI isImageRelatedTo;

    public static final IRI isLocationRelatedTo;

    /**
     * Corresponds to 'collection' in the Ontology for Media Resources.
     */
    public static final IRI isMemberOf;

    public static final IRI isNamedFragmentOf;

    /**
     * Corresponds to 'rating.identifier' in the Ontology for Media Resources.
     */
    public static final IRI isProvidedBy;

    public static final IRI isRelatedTo;

    public static final IRI isSigningOf;

    public static final IRI isSourceOf;

    public static final IRI isTargetAudienceOf;

    public static final IRI isTrackOf;

    /**
     * Corresponds to 'location.altitude' in the Ontology for Media Resources.
     */
    public static final IRI locationAltitude;

    /**
     * Corresponds to 'location.latitude' in the Ontology for Media Resources.
     */
    public static final IRI locationLatitude;

    /**
     * Corresponds to 'location.longitude' in the Ontology for Media Resources.
     */
    public static final IRI locationLongitude;

    /**
     * Corresponds to 'location.name' in the Ontology for Media Resources.
     */
    public static final IRI locationName;

    /**
     * Corresponds to 'locator' in the Ontology for Media Resources.
     */
    public static final IRI locator;

    /**
     * Corresponds to 'title.title' in the Ontology for Media Resources with a 'title.type' meaning "original".
     */
    public static final IRI mainOriginalTitle;

    /**
     * Corresponds to 'numTracks.number' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'numTracks.type'.
     */
    public static final IRI numberOfTracks;

    public static final IRI playsIn;

    public static final IRI provides;

    /**
     * Corresponds to 'rating.max' in the Ontology for Media Resources.
     */
    public static final IRI ratingScaleMax;

    /**
     * Corresponds to 'rating.min' in the Ontology for Media Resources.
     */
    public static final IRI ratingScaleMin;

    /**
     * Corresponds to 'rating.value' in the Ontology for Media Resources.
     */
    public static final IRI ratingValue;

    /**
     * Corresponds to 'date.date' in the Ontology for Media Resources with a 'date.type' meaning "recordDate".
     */
    public static final IRI recordDate;

    /**
     * Corresponds to 'date.date' in the Ontology for Media Resources with a 'date.type' meaning "releaseDate".
     */
    public static final IRI releaseDate;

    /**
     * Corresponds to 'samplingRate' in the Ontology for Media Resources, in samples per second.
     */
    public static final IRI samplingRate;

    /**
     * Corresponds to 'title.title' in the Ontology for Media Resources. Subproperties can be used to distinguish different values of 'title.type'.
     */
    public static final IRI title;

    /**
     * Corresponds to 'fragment.name' in the Ontology for Media Resources, for Track fragments.
     */
    public static final IRI trackName;


    static{
        ValueFactory factory = SimpleValueFactory.getInstance();
        Agent = factory.createIRI(MA.NAMESPACE, "Agent");
        AudioTrack = factory.createIRI(MA.NAMESPACE, "AudioTrack");
        Collection = factory.createIRI(MA.NAMESPACE, "Collection");
        DataTrack = factory.createIRI(MA.NAMESPACE, "DataTrack");
        Image = factory.createIRI(MA.NAMESPACE, "Image");
        IsRatingOf = factory.createIRI(MA.NAMESPACE, "IsRatingOf");
        Location = factory.createIRI(MA.NAMESPACE, "Location");
        MediaFragment = factory.createIRI(MA.NAMESPACE, "MediaFragment");
        MediaResource = factory.createIRI(MA.NAMESPACE, "MediaResource");
        Organisation = factory.createIRI(MA.NAMESPACE, "Organisation");
        Person = factory.createIRI(MA.NAMESPACE, "Person");
        Rating = factory.createIRI(MA.NAMESPACE, "Rating");
        TargetAudience = factory.createIRI(MA.NAMESPACE, "TargetAudience");
        Track = factory.createIRI(MA.NAMESPACE, "Track");
        VideoTrack = factory.createIRI(MA.NAMESPACE, "VideoTrack");
        alternativeTitle = factory.createIRI(MA.NAMESPACE, "alternativeTitle");
        averageBitRate = factory.createIRI(MA.NAMESPACE, "averageBitRate");
        collectionName = factory.createIRI(MA.NAMESPACE, "collectionName");
        copyright = factory.createIRI(MA.NAMESPACE, "copyright");
        createdIn = factory.createIRI(MA.NAMESPACE, "createdIn");
        creationDate = factory.createIRI(MA.NAMESPACE, "creationDate");
        date = factory.createIRI(MA.NAMESPACE, "date");
        depictsFictionalLocation = factory.createIRI(MA.NAMESPACE, "depictsFictionalLocation");
        description = factory.createIRI(MA.NAMESPACE, "description");
        duration = factory.createIRI(MA.NAMESPACE, "duration");
        editDate = factory.createIRI(MA.NAMESPACE, "editDate");
        features = factory.createIRI(MA.NAMESPACE, "features");
        fragmentName = factory.createIRI(MA.NAMESPACE, "fragmentName");
        frameHeight = factory.createIRI(MA.NAMESPACE, "frameHeight");
        frameRate = factory.createIRI(MA.NAMESPACE, "frameRate");
        frameSizeUnit = factory.createIRI(MA.NAMESPACE, "frameSizeUnit");
        frameWidth = factory.createIRI(MA.NAMESPACE, "frameWidth");
        hasAccessConditions = factory.createIRI(MA.NAMESPACE, "hasAccessConditions");
        hasAudioDescription = factory.createIRI(MA.NAMESPACE, "hasAudioDescription");
        hasCaptioning = factory.createIRI(MA.NAMESPACE, "hasCaptioning");
        hasChapter = factory.createIRI(MA.NAMESPACE, "hasChapter");
        hasClassification = factory.createIRI(MA.NAMESPACE, "hasClassification");
        hasClassificationSystem = factory.createIRI(MA.NAMESPACE, "hasClassificationSystem");
        hasCompression = factory.createIRI(MA.NAMESPACE, "hasCompression");
        hasContributedTo = factory.createIRI(MA.NAMESPACE, "hasContributedTo");
        hasContributor = factory.createIRI(MA.NAMESPACE, "hasContributor");
        hasCopyrightOver = factory.createIRI(MA.NAMESPACE, "hasCopyrightOver");
        hasCreated = factory.createIRI(MA.NAMESPACE, "hasCreated");
        hasCreator = factory.createIRI(MA.NAMESPACE, "hasCreator");
        hasFormat = factory.createIRI(MA.NAMESPACE, "hasFormat");
        hasFragment = factory.createIRI(MA.NAMESPACE, "hasFragment");
        hasGenre = factory.createIRI(MA.NAMESPACE, "hasGenre");
        hasKeyword = factory.createIRI(MA.NAMESPACE, "hasKeyword");
        hasLanguage = factory.createIRI(MA.NAMESPACE, "hasLanguage");
        hasLocationCoordinateSystem = factory.createIRI(MA.NAMESPACE, "hasLocationCoordinateSystem");
        hasMember = factory.createIRI(MA.NAMESPACE, "hasMember");
        hasNamedFragment = factory.createIRI(MA.NAMESPACE, "hasNamedFragment");
        hasPermissions = factory.createIRI(MA.NAMESPACE, "hasPermissions");
        hasPolicy = factory.createIRI(MA.NAMESPACE, "hasPolicy");
        hasPublished = factory.createIRI(MA.NAMESPACE, "hasPublished");
        hasPublisher = factory.createIRI(MA.NAMESPACE, "hasPublisher");
        hasRating = factory.createIRI(MA.NAMESPACE, "hasRating");
        hasRatingSystem = factory.createIRI(MA.NAMESPACE, "hasRatingSystem");
        hasRelatedImage = factory.createIRI(MA.NAMESPACE, "hasRelatedImage");
        hasRelatedLocation = factory.createIRI(MA.NAMESPACE, "hasRelatedLocation");
        hasRelatedResource = factory.createIRI(MA.NAMESPACE, "hasRelatedResource");
        hasSigning = factory.createIRI(MA.NAMESPACE, "hasSigning");
        hasSource = factory.createIRI(MA.NAMESPACE, "hasSource");
        hasSubtitling = factory.createIRI(MA.NAMESPACE, "hasSubtitling");
        hasTargetAudience = factory.createIRI(MA.NAMESPACE, "hasTargetAudience");
        hasTrack = factory.createIRI(MA.NAMESPACE, "hasTrack");
        isCaptioningOf = factory.createIRI(MA.NAMESPACE, "isCaptioningOf");
        isChapterOf = factory.createIRI(MA.NAMESPACE, "isChapterOf");
        isCopyrightedBy = factory.createIRI(MA.NAMESPACE, "isCopyrightedBy");
        isCreationLocationOf = factory.createIRI(MA.NAMESPACE, "isCreationLocationOf");
        isFictionalLocationDepictedIn = factory.createIRI(MA.NAMESPACE, "isFictionalLocationDepictedIn");
        isFragmentOf = factory.createIRI(MA.NAMESPACE, "isFragmentOf");
        isImageRelatedTo = factory.createIRI(MA.NAMESPACE, "isImageRelatedTo");
        isLocationRelatedTo = factory.createIRI(MA.NAMESPACE, "isLocationRelatedTo");
        isMemberOf = factory.createIRI(MA.NAMESPACE, "isMemberOf");
        isNamedFragmentOf = factory.createIRI(MA.NAMESPACE, "isNamedFragmentOf");
        isProvidedBy = factory.createIRI(MA.NAMESPACE, "isProvidedBy");
        isRelatedTo = factory.createIRI(MA.NAMESPACE, "isRelatedTo");
        isSigningOf = factory.createIRI(MA.NAMESPACE, "isSigningOf");
        isSourceOf = factory.createIRI(MA.NAMESPACE, "isSourceOf");
        isTargetAudienceOf = factory.createIRI(MA.NAMESPACE, "isTargetAudienceOf");
        isTrackOf = factory.createIRI(MA.NAMESPACE, "isTrackOf");
        locationAltitude = factory.createIRI(MA.NAMESPACE, "locationAltitude");
        locationLatitude = factory.createIRI(MA.NAMESPACE, "locationLatitude");
        locationLongitude = factory.createIRI(MA.NAMESPACE, "locationLongitude");
        locationName = factory.createIRI(MA.NAMESPACE, "locationName");
        locator = factory.createIRI(MA.NAMESPACE, "locator");
        mainOriginalTitle = factory.createIRI(MA.NAMESPACE, "mainOriginalTitle");
        numberOfTracks = factory.createIRI(MA.NAMESPACE, "numberOfTracks");
        playsIn = factory.createIRI(MA.NAMESPACE, "playsIn");
        provides = factory.createIRI(MA.NAMESPACE, "provides");
        ratingScaleMax = factory.createIRI(MA.NAMESPACE, "ratingScaleMax");
        ratingScaleMin = factory.createIRI(MA.NAMESPACE, "ratingScaleMin");
        ratingValue = factory.createIRI(MA.NAMESPACE, "ratingValue");
        recordDate = factory.createIRI(MA.NAMESPACE, "recordDate");
        releaseDate = factory.createIRI(MA.NAMESPACE, "releaseDate");
        samplingRate = factory.createIRI(MA.NAMESPACE, "samplingRate");
        title = factory.createIRI(MA.NAMESPACE, "title");
        trackName = factory.createIRI(MA.NAMESPACE, "trackName");
    }
}
