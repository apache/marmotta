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
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Namespace SCHEMA
 */
public class SCHEMA {

    public static final String NAMESPACE = "http://schema.org/";

    public static final String PREFIX = "schema";

    /**
     * Web page type: About page. 
     */
    public static final IRI AboutPage;

    /**
     * Accountancy business. 
     */
    public static final IRI AccountingService;

    /**
     * A geographical region under the jurisdiction of a particular government. 
     */
    public static final IRI AdministrativeArea;

    /**
     * An adult entertainment establishment. 
     */
    public static final IRI AdultEntertainment;

    /**
     * When a single product that has different offers (for example, the same pair of shoes is offered by different merchants), then AggregateOffer can be used. 
     */
    public static final IRI AggregateOffer;

    /**
     * The average rating based on multiple ratings or reviews. 
     */
    public static final IRI AggregateRating;

    /**
     * An airport. 
     */
    public static final IRI Airport;

    /**
     * An amusement park. 
     */
    public static final IRI AmusementPark;

    /**
     * Any part of the human body, typically a component of an anatomical system. Organs, tissues, and cells are all anatomical structures. 
     */
    public static final IRI AnatomicalStructure;

    /**
     * An anatomical system is a group of anatomical structures that work together to perform a certain task. Anatomical systems, such as organ systems, are one organizing principle of anatomy, and can includes circulatory, digestive, endocrine, integumentary, immune, lymphatic, muscular, nervous, reproductive, respiratory, skeletal, urinary, vestibular, and other systems. 
     */
    public static final IRI AnatomicalSystem;

    /**
     * Animal shelter. 
     */
    public static final IRI AnimalShelter;

    /**
     * Residence type: Apartment complex. 
     */
    public static final IRI ApartmentComplex;

    /**
     * An indication for a medical therapy that has been formally specified or approved by a regulatory body that regulates use of the therapy; for example, the US FDA approves indications for most drugs in the US. 
     */
    public static final IRI ApprovedIndication;

    /**
     * Aquarium. 
     */
    public static final IRI Aquarium;

    /**
     * An art gallery. 
     */
    public static final IRI ArtGallery;

    /**
     * A type of blood vessel that specifically carries blood away from the heart. 
     */
    public static final IRI Artery;

    /**
     * An article, such as a news article or piece of investigative report. Newspapers and magazines have articles of many different types and this is intended to cover them all. 
     */
    public static final IRI Article;

    /**
     * Professional service: Attorney. 
     */
    public static final IRI Attorney;

    /**
     * Intended audience for a creative work, i.e. the group for whom the work was created. 
     */
    public static final IRI Audience;

    /**
     * An audio file. 
     */
    public static final IRI AudioObject;

    /**
     * Auto body shop. 
     */
    public static final IRI AutoBodyShop;

    /**
     * An car dealership. 
     */
    public static final IRI AutoDealer;

    /**
     * An auto parts store. 
     */
    public static final IRI AutoPartsStore;

    /**
     * A car rental business. 
     */
    public static final IRI AutoRental;

    /**
     * Car repair business. 
     */
    public static final IRI AutoRepair;

    /**
     * A car wash business. 
     */
    public static final IRI AutoWash;

    /**
     * ATM/cash machine. 
     */
    public static final IRI AutomatedTeller;

    /**
     * Car repair, sales, or parts. 
     */
    public static final IRI AutomotiveBusiness;

    /**
     * A bakery. 
     */
    public static final IRI Bakery;

    /**
     * Bank or credit union. 
     */
    public static final IRI BankOrCreditUnion;

    /**
     * A bar or pub. 
     */
    public static final IRI BarOrPub;

    /**
     * Beach. 
     */
    public static final IRI Beach;

    /**
     * Beauty salon. 
     */
    public static final IRI BeautySalon;

    /**
     * Bed and breakfast. 
     */
    public static final IRI BedAndBreakfast;

    /**
     * A bike store. 
     */
    public static final IRI BikeStore;

    /**
     * A blog 
     */
    public static final IRI Blog;

    /**
     * A blog post. 
     */
    public static final IRI BlogPosting;

    /**
     * A medical test performed on a sample of a patient's blood. 
     */
    public static final IRI BloodTest;

    /**
     * A body of water, such as a sea, ocean, or lake. 
     */
    public static final IRI BodyOfWater;

    /**
     * Rigid connective tissue that comprises up the skeletal structure of the human body. 
     */
    public static final IRI Bone;

    /**
     * A book. 
     */
    public static final IRI Book;

    /**
     * The publication format of the book. 
     */
    public static final IRI BookFormatType;

    /**
     * A bookstore. 
     */
    public static final IRI BookStore;

    /**
     * A bowling alley. 
     */
    public static final IRI BowlingAlley;

    /**
     * Any anatomical structure which pertains to the soft nervous tissue functioning as the coordinating center of sensation and intellectual and nervous activity. 
     */
    public static final IRI BrainStructure;

    /**
     * A brand is a name used by an organization or business person for labeling a product, product group, or similar. 
     */
    public static final IRI Brand;

    /**
     * Brewery. 
     */
    public static final IRI Brewery;

    /**
     * A Buddhist temple. 
     */
    public static final IRI BuddhistTemple;

    /**
     * A bus station. 
     */
    public static final IRI BusStation;

    /**
     * A bus stop. 
     */
    public static final IRI BusStop;

    /**
     * A business entity type is a conceptual entity representing the legal form, the size, the main line of business, the position in the value chain, or any combination thereof, of an organization or business person.

     Commonly used values:

     http://purl.org/goodrelations/v1#Business
     http://purl.org/goodrelations/v1#Enduser
     http://purl.org/goodrelations/v1#PublicInstitution
     http://purl.org/goodrelations/v1#Reseller 
     */
    public static final IRI BusinessEntityType;

    /**
     * Event type: Business event. 
     */
    public static final IRI BusinessEvent;

    /**
     * The business function specifies the type of activity or access (i.e., the bundle of rights) offered by the organization or business person through the offer. Typical are sell, rental or lease, maintenance or repair, manufacture / produce, recycle / dispose, engineering / construction, or installation. Proprietary specifications of access rights are also instances of this class.

     Commonly used values:

     http://purl.org/goodrelations/v1#ConstructionInstallation
     http://purl.org/goodrelations/v1#Dispose
     http://purl.org/goodrelations/v1#LeaseOut
     http://purl.org/goodrelations/v1#Maintain
     http://purl.org/goodrelations/v1#ProvideService
     http://purl.org/goodrelations/v1#Repair
     http://purl.org/goodrelations/v1#Sell
     http://purl.org/goodrelations/v1#Buy 
     */
    public static final IRI BusinessFunction;

    /**
     * A cafe or coffee shop. 
     */
    public static final IRI CafeOrCoffeeShop;

    /**
     * A campground. 
     */
    public static final IRI Campground;

    /**
     * A canal, like the Panama Canal 
     */
    public static final IRI Canal;

    /**
     * A casino. 
     */
    public static final IRI Casino;

    /**
     * A Catholic church. 
     */
    public static final IRI CatholicChurch;

    /**
     * A graveyard. 
     */
    public static final IRI Cemetery;

    /**
     * Web page type: Checkout page. 
     */
    public static final IRI CheckoutPage;

    /**
     * A Childcare center. 
     */
    public static final IRI ChildCare;

    /**
     * Event type: Children's event. 
     */
    public static final IRI ChildrensEvent;

    /**
     * A church. 
     */
    public static final IRI Church;

    /**
     * A city or town. 
     */
    public static final IRI City;

    /**
     * A city hall. 
     */
    public static final IRI CityHall;

    /**
     * A public structure, such as a town hall or concert hall. 
     */
    public static final IRI CivicStructure;

    /**
     * A clothing store. 
     */
    public static final IRI ClothingStore;

    /**
     * Web page type: Collection page. 
     */
    public static final IRI CollectionPage;

    /**
     * A college, university, or other third-level educational institution. 
     */
    public static final IRI CollegeOrUniversity;

    /**
     * A comedy club. 
     */
    public static final IRI ComedyClub;

    /**
     * Event type: Comedy event. 
     */
    public static final IRI ComedyEvent;

    /**
     * A comment on an item - for example, a comment on a blog post. The comment's content is expressed via the "text" property, and its topic via "about", properties shared with all CreativeWorks. 
     */
    public static final IRI Comment;

    /**
     * A computer store. 
     */
    public static final IRI ComputerStore;

    /**
     * Web page type: Contact page. 
     */
    public static final IRI ContactPage;

    /**
     * A contact point—for example, a Customer Complaints department. 
     */
    public static final IRI ContactPoint;

    /**
     * One of the continents (for example, Europe or Africa). 
     */
    public static final IRI Continent;

    /**
     * A convenience store. 
     */
    public static final IRI ConvenienceStore;

    /**
     * Organization: A business corporation. 
     */
    public static final IRI Corporation;

    /**
     * A country. 
     */
    public static final IRI Country;

    /**
     * A courthouse. 
     */
    public static final IRI Courthouse;

    /**
     * The most generic kind of creative work, including books, movies, photographs, software programs, etc. 
     */
    public static final IRI CreativeWork;

    /**
     * A credit or debit card type as a standardized procedure for transferring the monetary amount for a purchase.

     Commonly used values:

     http://purl.org/goodrelations/v1#AmericanExpress
     http://purl.org/goodrelations/v1#DinersClub
     http://purl.org/goodrelations/v1#Discover
     http://purl.org/goodrelations/v1#JCB
     http://purl.org/goodrelations/v1#MasterCard
     http://purl.org/goodrelations/v1#VISA 
     */
    public static final IRI CreditCard;

    /**
     * A crematorium. 
     */
    public static final IRI Crematorium;

    /**
     * An alternative, closely-related condition typically considered later in the differential diagnosis process along with the signs that are used to distinguish it. 
     */
    public static final IRI DDxElement;

    /**
     * Event type: A social dance. 
     */
    public static final IRI DanceEvent;

    /**
     * A dance group—for example, the Alvin Ailey Dance Theater or Riverdance. 
     */
    public static final IRI DanceGroup;

    /**
     * The day of the week, e.g. used to specify to which day the opening hours of an OpeningHoursSpecification refer.

     Commonly used values:

     http://purl.org/goodrelations/v1#Monday
     http://purl.org/goodrelations/v1#Tuesday
     http://purl.org/goodrelations/v1#Wednesday
     http://purl.org/goodrelations/v1#Thursday
     http://purl.org/goodrelations/v1#Friday
     http://purl.org/goodrelations/v1#Saturday
     http://purl.org/goodrelations/v1#Sunday
     http://purl.org/goodrelations/v1#PublicHolidays 
     */
    public static final IRI DayOfWeek;

    /**
     * A day spa. 
     */
    public static final IRI DaySpa;

    /**
     * A defence establishment, such as an army or navy base. 
     */
    public static final IRI DefenceEstablishment;

    /**
     * The price for the delivery of an offer using a particular delivery method. 
     */
    public static final IRI DeliveryChargeSpecification;

    /**
     * A delivery method is a standardized procedure for transferring the product or service to the destination of fulfilment chosen by the customer. Delivery methods are characterized by the means of transportation used, and by the organization or group that is the contracting party for the sending organization or person.

     Commonly used values:

     http://purl.org/goodrelations/v1#DeliveryModeDirectDownload
     http://purl.org/goodrelations/v1#DeliveryModeFreight
     http://purl.org/goodrelations/v1#DeliveryModeMail
     http://purl.org/goodrelations/v1#DeliveryModeOwnFleet
     http://purl.org/goodrelations/v1#DeliveryModePickUp
     http://purl.org/goodrelations/v1#DHL
     http://purl.org/goodrelations/v1#FederalExpress
     http://purl.org/goodrelations/v1#UPS 
     */
    public static final IRI DeliveryMethod;

    /**
     * A demand entity represents the public, not necessarily binding, not necessarily exclusive, announcement by an organization or person to seek a certain type of goods or services. For describing demand using this type, the very same properties used for Offer apply. 
     */
    public static final IRI Demand;

    /**
     * A dentist. 
     */
    public static final IRI Dentist;

    /**
     * A department store. 
     */
    public static final IRI DepartmentStore;

    /**
     * A medical laboratory that offers on-site or off-site diagnostic services. 
     */
    public static final IRI DiagnosticLab;

    /**
     * A medical procedure intended primarly for diagnostic, as opposed to therapeutic, purposes. 
     */
    public static final IRI DiagnosticProcedure;

    /**
     * A strategy of regulating the intake of food to achieve or maintain a specific health-related goal. 
     */
    public static final IRI Diet;

    /**
     * A product taken by mouth that contains a dietary ingredient intended to supplement the diet. Dietary ingredients may include vitamins, minerals, herbs or other botanicals, amino acids, and substances such as enzymes, organ tissues, glandulars and metabolites. 
     */
    public static final IRI DietarySupplement;

    /**
     * Properties that take Distances as values are of the form '<Number> <Length unit of measure>'. E.g., '7 ft' 
     */
    public static final IRI Distance;

    /**
     * A specific dosing schedule for a drug or supplement. 
     */
    public static final IRI DoseSchedule;

    /**
     * A chemical or biologic substance, used as a medical therapy, that has a physiological effect on an organism. 
     */
    public static final IRI Drug;

    /**
     * A class of medical drugs, e.g., statins. Classes can represent general pharmacological class, common mechanisms of action, common physiological effects, etc. 
     */
    public static final IRI DrugClass;

    /**
     * The cost per unit of a medical drug. Note that this type is not meant to represent the price in an offer of a drug for sale; see the Offer type for that. This type will typically be used to tag wholesale or average retail cost of a drug, or maximum reimbursable cost. Costs of medical drugs vary widely depending on how and where they are paid for, so while this type captures some of the variables, costs should be used with caution by consumers of this schema's markup. 
     */
    public static final IRI DrugCost;

    /**
     * Enumerated categories of medical drug costs. 
     */
    public static final IRI DrugCostCategory;

    /**
     * The legal availability status of a medical drug. 
     */
    public static final IRI DrugLegalStatus;

    /**
     * Categories that represent an assessment of the risk of fetal injury due to a drug or pharmaceutical used as directed by the mother during pregnancy. 
     */
    public static final IRI DrugPregnancyCategory;

    /**
     * Indicates whether this drug is available by prescription or over-the-counter. 
     */
    public static final IRI DrugPrescriptionStatus;

    /**
     * A specific strength in which a medical drug is available in a specific country. 
     */
    public static final IRI DrugStrength;

    /**
     * A dry-cleaning business. 
     */
    public static final IRI DryCleaningOrLaundry;

    /**
     * Quantity: Duration (use  ISO 8601 duration format). 
     */
    public static final IRI Duration;

    /**
     * Event type: Education event. 
     */
    public static final IRI EducationEvent;

    /**
     * An educational organization. 
     */
    public static final IRI EducationalOrganization;

    /**
     * An electrician. 
     */
    public static final IRI Electrician;

    /**
     * An electronics store. 
     */
    public static final IRI ElectronicsStore;

    /**
     * An elementary school. 
     */
    public static final IRI ElementarySchool;

    /**
     * An embassy. 
     */
    public static final IRI Embassy;

    /**
     * An emergency service, such as a fire station or ER. 
     */
    public static final IRI EmergencyService;

    /**
     * An employment agency. 
     */
    public static final IRI EmploymentAgency;

    /**
     * Properties that take Enerygy as values are of the form '<Number> <Energy unit of measure>' 
     */
    public static final IRI Energy;

    /**
     * A business providing entertainment. 
     */
    public static final IRI EntertainmentBusiness;

    /**
     * Lists or enumerations—for example, a list of cuisines or music genres, etc. 
     */
    public static final IRI Enumeration;

    /**
     * An event happening at a certain time at a certain location. 
     */
    public static final IRI Event;

    /**
     * An event venue. 
     */
    public static final IRI EventVenue;

    /**
     * A gym. 
     */
    public static final IRI ExerciseGym;

    /**
     * Fitness-related activity designed for a specific health-related purpose, including defined exercise routines as well as activity prescribed by a clinician. 
     */
    public static final IRI ExercisePlan;

    /**
     * A fast-food restaurant. 
     */
    public static final IRI FastFoodRestaurant;

    /**
     * Event type: Festival. 
     */
    public static final IRI Festival;

    /**
     * Financial services business. 
     */
    public static final IRI FinancialService;

    /**
     * A fire station. With firemen. 
     */
    public static final IRI FireStation;

    /**
     * A florist. 
     */
    public static final IRI Florist;

    /**
     * A food-related business. 
     */
    public static final IRI FoodEstablishment;

    /**
     * Event type: Food event. 
     */
    public static final IRI FoodEvent;

    /**
     * A furniture store. 
     */
    public static final IRI FurnitureStore;

    /**
     * A garden store. 
     */
    public static final IRI GardenStore;

    /**
     * A gas station. 
     */
    public static final IRI GasStation;

    /**
     * Residence type: Gated community. 
     */
    public static final IRI GatedResidenceCommunity;

    /**
     * A general contractor. 
     */
    public static final IRI GeneralContractor;

    /**
     * The geographic coordinates of a place or event. 
     */
    public static final IRI GeoCoordinates;

    /**
     * The geographic shape of a place. 
     */
    public static final IRI GeoShape;

    /**
     * A golf course. 
     */
    public static final IRI GolfCourse;

    /**
     * A government building. 
     */
    public static final IRI GovernmentBuilding;

    /**
     * A government office—for example, an IRS or DMV office. 
     */
    public static final IRI GovernmentOffice;

    /**
     * A governmental organization or agency. 
     */
    public static final IRI GovernmentOrganization;

    /**
     * A grocery store. 
     */
    public static final IRI GroceryStore;

    /**
     * An HVAC service. 
     */
    public static final IRI HVACBusiness;

    /**
     * A hair salon. 
     */
    public static final IRI HairSalon;

    /**
     * A hardware store. 
     */
    public static final IRI HardwareStore;

    /**
     * Health and beauty. 
     */
    public static final IRI HealthAndBeautyBusiness;

    /**
     * A health club. 
     */
    public static final IRI HealthClub;

    /**
     * A high school. 
     */
    public static final IRI HighSchool;

    /**
     * A Hindu temple. 
     */
    public static final IRI HinduTemple;

    /**
     * A hobby store. 
     */
    public static final IRI HobbyShop;

    /**
     * A construction business. 
     */
    public static final IRI HomeAndConstructionBusiness;

    /**
     * A home goods store. 
     */
    public static final IRI HomeGoodsStore;

    /**
     * A hospital. 
     */
    public static final IRI Hospital;

    /**
     * A hostel. 
     */
    public static final IRI Hostel;

    /**
     * A hotel. 
     */
    public static final IRI Hotel;

    /**
     * A house painting service. 
     */
    public static final IRI HousePainter;

    /**
     * An ice cream shop 
     */
    public static final IRI IceCreamShop;

    /**
     * Web page type: Image gallery page. 
     */
    public static final IRI ImageGallery;

    /**
     * An image file. 
     */
    public static final IRI ImageObject;

    /**
     * Any medical imaging modality typically used for diagnostic purposes. 
     */
    public static final IRI ImagingTest;

    /**
     * A single, identifiable product instance (e.g. a laptop with a particular serial number). 
     */
    public static final IRI IndividualProduct;

    /**
     * Classes of agents or pathogens that transmit infectious diseases. Enumerated type. 
     */
    public static final IRI InfectiousAgentClass;

    /**
     * An infectious disease is a clinically evident human disease resulting from the presence of pathogenic microbial agents, like pathogenic viruses, pathogenic bacteria, fungi, protozoa, multicellular parasites, and prions. To be considered an infectious disease, such pathogens are known to be able to cause this disease. 
     */
    public static final IRI InfectiousDisease;

    /**
     * Insurance agency. 
     */
    public static final IRI InsuranceAgency;

    /**
     * A utility class that serves as the umbrella for a number of 'intangible' things such as quantities, structured values, etc. 
     */
    public static final IRI Intangible;

    /**
     * An internet cafe. 
     */
    public static final IRI InternetCafe;

    /**
     * A list of possible product availablity options. 
     */
    public static final IRI ItemAvailability;

    /**
     * A list of items of any sort—for example, Top 10 Movies About Weathermen, or Top 100 Party Songs. Not to be confused with HTML lists, which are often used only for formatting. 
     */
    public static final IRI ItemList;

    /**
     * A page devoted to a single item, such as a particular product or hotel. 
     */
    public static final IRI ItemPage;

    /**
     * A jewelry store. 
     */
    public static final IRI JewelryStore;

    /**
     * A listing that describes a job opening in a certain organization. 
     */
    public static final IRI JobPosting;

    /**
     * The anatomical location at which two or more bones make contact. 
     */
    public static final IRI Joint;

    /**
     * A lake (for example, Lake Pontrachain). 
     */
    public static final IRI LakeBodyOfWater;

    /**
     * A landform or physical feature.  Landform elements include mountains, plains, lakes, rivers, seascape and oceanic waterbody interface features such as bays, peninsulas, seas and so forth, including sub-aqueous terrain features such as submersed mountain ranges, volcanoes, and the great ocean basins. 
     */
    public static final IRI Landform;

    /**
     * An historical landmark or building. 
     */
    public static final IRI LandmarksOrHistoricalBuildings;

    /**
     * Natural languages such as Spanish, Tamil, Hindi, English, etc. and programming languages such as Scheme and Lisp. 
     */
    public static final IRI Language;

    /**
     * A legislative building—for example, the state capitol. 
     */
    public static final IRI LegislativeBuilding;

    /**
     * A library. 
     */
    public static final IRI Library;

    /**
     * A process of care involving exercise, changes to diet, fitness routines, and other lifestyle changes aimed at improving a health condition. 
     */
    public static final IRI LifestyleModification;

    /**
     * A short band of tough, flexible, fibrous connective tissue that functions to connect multiple bones, cartilages, and structurally support joints. 
     */
    public static final IRI Ligament;

    /**
     * A liquor store. 
     */
    public static final IRI LiquorStore;

    /**
     * Event type: Literary event. 
     */
    public static final IRI LiteraryEvent;

    /**
     * A particular physical business or branch of an organization. Examples of LocalBusiness include a restaurant, a particular branch of a restaurant chain, a branch of a bank, a medical practice, a club, a bowling alley, etc. 
     */
    public static final IRI LocalBusiness;

    /**
     * A locksmith. 
     */
    public static final IRI Locksmith;

    /**
     * A lodging business, such as a motel, hotel, or inn. 
     */
    public static final IRI LodgingBusiness;

    /**
     * A type of blood vessel that specifically carries lymph fluid unidirectionally toward the heart. 
     */
    public static final IRI LymphaticVessel;

    /**
     * A map. 
     */
    public static final IRI Map;

    /**
     * Properties that take Mass as values are of the form '<Number> <Mass unit of measure>'. E.g., '7 kg' 
     */
    public static final IRI Mass;

    /**
     * The maximum dosing schedule considered safe for a drug or supplement as recommended by an authority or by the drug/supplement's manufacturer. Capture the recommending authority in the recognizingAuthority property of MedicalEntity. 
     */
    public static final IRI MaximumDoseSchedule;

    /**
     * An image, video, or audio object embedded in a web page. Note that a creative work may have many media objects associated with it on the same web page. For example, a page about a single song (MusicRecording) may have a music video (VideoObject), and a high and low bandwidth audio stream (2 AudioObject's). 
     */
    public static final IRI MediaObject;

    /**
     * Target audiences for medical web pages. Enumerated type. 
     */
    public static final IRI MedicalAudience;

    /**
     * The causative agent(s) that are responsible for the pathophysiologic process that eventually results in a medical condition, symptom or sign. In this schema, unless otherwise specified this is meant to be the proximate cause of the medical condition, symptom or sign. The proximate cause is defined as the causative agent that most directly results in the medical condition, symptom or sign. For example, the HIV virus could be considered a cause of AIDS. Or in a diagnostic context, if a patient fell and sustained a hip fracture and two days later sustained a pulmonary embolism which eventuated in a cardiac arrest, the cause of the cardiac arrest (the proximate cause) would be the pulmonary embolism and not the fall. Medical causes can include cardiovascular, chemical, dermatologic, endocrine, environmental, gastroenterologic, genetic, hematologic, gynecologic, iatrogenic, infectious, musculoskeletal, neurologic, nutritional, obstetric, oncologic, otolaryngologic, pharmacologic, psychiatric, pulmonary, renal, rheumatologic, toxic, traumatic, or urologic causes; medical conditions can be causes as well. 
     */
    public static final IRI MedicalCause;

    /**
     * A medical clinic. 
     */
    public static final IRI MedicalClinic;

    /**
     * A code for a medical entity. 
     */
    public static final IRI MedicalCode;

    /**
     * Any condition of the human body that affects the normal functioning of a person, whether physically or mentally. Includes diseases, injuries, disabilities, disorders, syndromes, etc. 
     */
    public static final IRI MedicalCondition;

    /**
     * A stage of a medical condition, such as 'Stage IIIa'. 
     */
    public static final IRI MedicalConditionStage;

    /**
     * A condition or factor that serves as a reason to withhold a certain medical therapy. Contraindications can be absolute (there are no reasonable circumstances for undertaking a course of action) or relative (the patient is at higher risk of complications, but that these risks may be outweighed by other considerations or mitigated by other measures). 
     */
    public static final IRI MedicalContraindication;

    /**
     * Any object used in a medical capacity, such as to diagnose or treat a patient. 
     */
    public static final IRI MedicalDevice;

    /**
     * Categories of medical devices, organized by the purpose or intended use of the device. 
     */
    public static final IRI MedicalDevicePurpose;

    /**
     * The most generic type of entity related to health and the practice of medicine. 
     */
    public static final IRI MedicalEntity;

    /**
     * Enumerations related to health and the practice of medicine. 
     */
    public static final IRI MedicalEnumeration;

    /**
     * Level of evidence for a medical guideline. Enumerated type. 
     */
    public static final IRI MedicalEvidenceLevel;

    /**
     * Any recommendation made by a standard society (e.g. ACC/AHA) or consensus statement that denotes how to diagnose and treat a particular condition. Note: this type should be used to tag the actual guideline recommendation; if the guideline recommendation occurs in a larger scholarly article, use MedicalScholarlyArticle to tag the overall article, not this type. Note also: the organization making the recommendation should be captured in the recognizingAuthority base property of MedicalEntity. 
     */
    public static final IRI MedicalGuideline;

    /**
     * A guideline contraindication that designates a process as harmful and where quality of the data supporting the contraindication is sound. 
     */
    public static final IRI MedicalGuidelineContraindication;

    /**
     * A guideline recommendation that is regarded as efficacious and where quality of the data supporting the recommendation is sound. 
     */
    public static final IRI MedicalGuidelineRecommendation;

    /**
     * Any medical imaging modality typically used for diagnostic purposes. Enumerated type. 
     */
    public static final IRI MedicalImagingTechnique;

    /**
     * A condition or factor that indicates use of a medical therapy, including signs, symptoms, risk factors, anatomical states, etc. 
     */
    public static final IRI MedicalIndication;

    /**
     * A utility class that serves as the umbrella for a number of 'intangible' things in the medical space. 
     */
    public static final IRI MedicalIntangible;

    /**
     * An observational study is a type of medical study that attempts to infer the possible effect of a treatment through observation of a cohort of subjects over a period of time. In an observational study, the assignment of subjects into treatment groups versus control groups is outside the control of the investigator. This is in contrast with controlled studies, such as the randomized controlled trials represented by MedicalTrial, where each subject is randomly assigned to a treatment group or a control group before the start of the treatment. 
     */
    public static final IRI MedicalObservationalStudy;

    /**
     * Design models for observational medical studies. Enumerated type. 
     */
    public static final IRI MedicalObservationalStudyDesign;

    /**
     * A medical organization, such as a doctor's office or clinic. 
     */
    public static final IRI MedicalOrganization;

    /**
     * A process of care used in either a diagnostic, therapeutic, or palliative capacity that relies on invasive (surgical), non-invasive, or percutaneous techniques. 
     */
    public static final IRI MedicalProcedure;

    /**
     * An enumeration that describes different types of medical procedures. 
     */
    public static final IRI MedicalProcedureType;

    /**
     * A complex mathematical calculation requiring an online calculator, used to assess prognosis. Note: use the url property of Thing to record any URLs for online calculators. 
     */
    public static final IRI MedicalRiskCalculator;

    /**
     * Any rule set or interactive tool for estimating the risk of developing a complication or condition. 
     */
    public static final IRI MedicalRiskEstimator;

    /**
     * A risk factor is anything that increases a person's likelihood of developing or contracting a disease, medical condition, or complication. 
     */
    public static final IRI MedicalRiskFactor;

    /**
     * A simple system that adds up the number of risk factors to yield a score that is associated with prognosis, e.g. CHAD score, TIMI risk score. 
     */
    public static final IRI MedicalRiskScore;

    /**
     * A scholarly article in the medical domain. 
     */
    public static final IRI MedicalScholarlyArticle;

    /**
     * Any physical manifestation of a person's medical condition discoverable by objective diagnostic tests or physical examination. 
     */
    public static final IRI MedicalSign;

    /**
     * Any indication of the existence of a medical condition or disease. 
     */
    public static final IRI MedicalSignOrSymptom;

    /**
     * Any specific branch of medical science or practice. Medical specialities include clinical specialties that pertain to particular organ systems and their respective disease states, as well as allied health specialties. Enumerated type. 
     */
    public static final IRI MedicalSpecialty;

    /**
     * A medical study is an umbrella type covering all kinds of research studies relating to human medicine or health, including observational studies and interventional trials and registries, randomized, controlled or not. When the specific type of study is known, use one of the extensions of this type, such as MedicalTrial or MedicalObservationalStudy. Also, note that this type should be used to mark up data that describes the study itself; to tag an article that publishes the results of a study, use MedicalScholarlyArticle. Note: use the code property of MedicalEntity to store study IDs, e.g. clinicaltrials.gov ID. 
     */
    public static final IRI MedicalStudy;

    /**
     * The status of a medical study. Enumerated type. 
     */
    public static final IRI MedicalStudyStatus;

    /**
     * Any indication of the existence of a medical condition or disease that is apparent to the patient. 
     */
    public static final IRI MedicalSymptom;

    /**
     * Any medical test, typically performed for diagnostic purposes. 
     */
    public static final IRI MedicalTest;

    /**
     * Any collection of tests commonly ordered together. 
     */
    public static final IRI MedicalTestPanel;

    /**
     * Any medical intervention designed to prevent, treat, and cure human diseases and medical conditions, including both curative and palliative therapies. Medical therapies are typically processes of care relying upon pharmacotherapy, behavioral therapy, supportive therapy (with fluid or nutrition for example), or detoxification (e.g. hemodialysis) aimed at improving or preventing a health condition. 
     */
    public static final IRI MedicalTherapy;

    /**
     * A medical trial is a type of medical study that uses scientific process used to compare the safety and efficacy of medical therapies or medical procedures. In general, medical trials are controlled and subjects are allocated at random to the different treatment and/or control groups. 
     */
    public static final IRI MedicalTrial;

    /**
     * Design models for medical trials. Enumerated type. 
     */
    public static final IRI MedicalTrialDesign;

    /**
     * A web page that provides medical information. 
     */
    public static final IRI MedicalWebPage;

    /**
     * Systems of medical practice. 
     */
    public static final IRI MedicineSystem;

    /**
     * A men's clothing store. 
     */
    public static final IRI MensClothingStore;

    /**
     * A middle school. 
     */
    public static final IRI MiddleSchool;

    /**
     * A mobile software application. 
     */
    public static final IRI MobileApplication;

    /**
     * A mobile-phone store. 
     */
    public static final IRI MobilePhoneStore;

    /**
     * A mosque. 
     */
    public static final IRI Mosque;

    /**
     * A motel. 
     */
    public static final IRI Motel;

    /**
     * A motorcycle dealer. 
     */
    public static final IRI MotorcycleDealer;

    /**
     * A motorcycle repair shop. 
     */
    public static final IRI MotorcycleRepair;

    /**
     * A mountain, like Mount Whitney or Mount Everest 
     */
    public static final IRI Mountain;

    /**
     * A movie. 
     */
    public static final IRI Movie;

    /**
     * A movie rental store. 
     */
    public static final IRI MovieRentalStore;

    /**
     * A movie theater. 
     */
    public static final IRI MovieTheater;

    /**
     * A moving company. 
     */
    public static final IRI MovingCompany;

    /**
     * A muscle is an anatomical structure consisting of a contractile form of tissue that animals use to effect movement. 
     */
    public static final IRI Muscle;

    /**
     * A museum. 
     */
    public static final IRI Museum;

    /**
     * A collection of music tracks. 
     */
    public static final IRI MusicAlbum;

    /**
     * Event type: Music event. 
     */
    public static final IRI MusicEvent;

    /**
     * A musical group, such as a band, an orchestra, or a choir. Can also be a solo musician. 
     */
    public static final IRI MusicGroup;

    /**
     * A collection of music tracks in playlist form. 
     */
    public static final IRI MusicPlaylist;

    /**
     * A music recording (track), usually a single song. 
     */
    public static final IRI MusicRecording;

    /**
     * A music store. 
     */
    public static final IRI MusicStore;

    /**
     * A music venue. 
     */
    public static final IRI MusicVenue;

    /**
     * A music video file. 
     */
    public static final IRI MusicVideoObject;

    /**
     * Organization: Non-governmental Organization. 
     */
    public static final IRI NGO;

    /**
     * A nail salon. 
     */
    public static final IRI NailSalon;

    /**
     * A common pathway for the electrochemical nerve impulses that are transmitted along each of the axons. 
     */
    public static final IRI Nerve;

    /**
     * A news article 
     */
    public static final IRI NewsArticle;

    /**
     * A nightclub or discotheque. 
     */
    public static final IRI NightClub;

    /**
     * A notary. 
     */
    public static final IRI Notary;

    /**
     * Nutritional information about the recipe. 
     */
    public static final IRI NutritionInformation;

    /**
     * An ocean (for example, the Pacific). 
     */
    public static final IRI OceanBodyOfWater;

    /**
     * An offer to sell an item—for example, an offer to sell a product, the DVD of a movie, or tickets to an event. 
     */
    public static final IRI Offer;

    /**
     * A list of possible conditions for the item for sale. 
     */
    public static final IRI OfferItemCondition;

    /**
     * An office equipment store. 
     */
    public static final IRI OfficeEquipmentStore;

    /**
     * A structured value providing information about the opening hours of a place or a certain service inside a place. 
     */
    public static final IRI OpeningHoursSpecification;

    /**
     * An optician's store. 
     */
    public static final IRI Optician;

    /**
     * An organization such as a school, NGO, corporation, club, etc. 
     */
    public static final IRI Organization;

    /**
     * An outlet store. 
     */
    public static final IRI OutletStore;

    /**
     * A structured value providing information about when a certain organization or person owned a certain product. 
     */
    public static final IRI OwnershipInfo;

    /**
     * A painting. 
     */
    public static final IRI Painting;

    /**
     * A medical procedure intended primarly for palliative purposes, aimed at relieving the symptoms of an underlying health condition. 
     */
    public static final IRI PalliativeProcedure;

    /**
     * A private parcel service as the delivery mode available for a certain offer.

     Commonly used values:

     http://purl.org/goodrelations/v1#DHL
     http://purl.org/goodrelations/v1#FederalExpress
     http://purl.org/goodrelations/v1#UPS 
     */
    public static final IRI ParcelService;

    /**
     * A park. 
     */
    public static final IRI Park;

    /**
     * A parking lot or other parking facility. 
     */
    public static final IRI ParkingFacility;

    /**
     * A medical test performed by a laboratory that typically involves examination of a tissue sample by a pathologist. 
     */
    public static final IRI PathologyTest;

    /**
     * A pawnstore. 
     */
    public static final IRI PawnShop;

    /**
     * The costs of settling the payment using a particular payment method. 
     */
    public static final IRI PaymentChargeSpecification;

    /**
     * A payment method is a standardized procedure for transferring the monetary amount for a purchase. Payment methods are characterized by the legal and technical structures used, and by the organization or group carrying out the transaction.

     Commonly used values:

     http://purl.org/goodrelations/v1#ByBankTransferInAdvance
     http://purl.org/goodrelations/v1#ByInvoice
     http://purl.org/goodrelations/v1#Cash
     http://purl.org/goodrelations/v1#CheckInAdvance
     http://purl.org/goodrelations/v1#COD
     http://purl.org/goodrelations/v1#DirectDebit
     http://purl.org/goodrelations/v1#GoogleCheckout
     http://purl.org/goodrelations/v1#PayPal
     http://purl.org/goodrelations/v1#PaySwarm 
     */
    public static final IRI PaymentMethod;

    /**
     * A theatre or other performing art center. 
     */
    public static final IRI PerformingArtsTheater;

    /**
     * A performance group, such as a band, an orchestra, or a circus. 
     */
    public static final IRI PerformingGroup;

    /**
     * A person (alive, dead, undead, or fictional). 
     */
    public static final IRI Person;

    /**
     * A pet store. 
     */
    public static final IRI PetStore;

    /**
     * A pharmacy or drugstore. 
     */
    public static final IRI Pharmacy;

    /**
     * A photograph. 
     */
    public static final IRI Photograph;

    /**
     * Any bodily activity that enhances or maintains physical fitness and overall health and wellness. Includes activity that is part of daily living and routine, structured exercise, and exercise prescribed as part of a medical treatment or recovery plan. 
     */
    public static final IRI PhysicalActivity;

    /**
     * Categories of physical activity, organized by physiologic classification. 
     */
    public static final IRI PhysicalActivityCategory;

    /**
     * A type of physical examination of a patient performed by a physician. Enumerated type. 
     */
    public static final IRI PhysicalExam;

    /**
     * A process of progressive physical care and rehabilitation aimed at improving a health condition. 
     */
    public static final IRI PhysicalTherapy;

    /**
     * A doctor's office. 
     */
    public static final IRI Physician;

    /**
     * Entities that have a somewhat fixed, physical extension. 
     */
    public static final IRI Place;

    /**
     * Place of worship, such as a church, synagogue, or mosque. 
     */
    public static final IRI PlaceOfWorship;

    /**
     * A playground. 
     */
    public static final IRI Playground;

    /**
     * A plumbing service. 
     */
    public static final IRI Plumber;

    /**
     * A police station. 
     */
    public static final IRI PoliceStation;

    /**
     * A pond 
     */
    public static final IRI Pond;

    /**
     * A post office. 
     */
    public static final IRI PostOffice;

    /**
     * The mailing address. 
     */
    public static final IRI PostalAddress;

    /**
     * A preschool. 
     */
    public static final IRI Preschool;

    /**
     * An indication for preventing an underlying condition, symptom, etc. 
     */
    public static final IRI PreventionIndication;

    /**
     * A structured value representing a monetary amount. Typically, only the subclasses of this type are used for markup. 
     */
    public static final IRI PriceSpecification;

    /**
     * A product is anything that is made available for sale—for example, a pair of shoes, a concert ticket, or a car. Commodity services, like haircuts, can also be represented using this type. 
     */
    public static final IRI Product;

    /**
     * A datasheet or vendor specification of a product (in the sense of a prototypical description). 
     */
    public static final IRI ProductModel;

    /**
     * Provider of professional services. 
     */
    public static final IRI ProfessionalService;

    /**
     * Web page type: Profile page. 
     */
    public static final IRI ProfilePage;

    /**
     * A process of care relying upon counseling, dialogue, communication, verbalization aimed at improving a mental health condition. 
     */
    public static final IRI PsychologicalTreatment;

    /**
     * A public swimming pool. 
     */
    public static final IRI PublicSwimmingPool;

    /**
     * A predefined value for a product characteristic, e.g. the the power cord plug type "US" or the garment sizes "S", "M", "L", and "XL" 
     */
    public static final IRI QualitativeValue;

    /**
     * A point value or interval for product characteristics and other purposes. 
     */
    public static final IRI QuantitativeValue;

    /**
     * Quantities such as distance, time, mass, weight, etc. Particular instances of say Mass are entities like '3 Kg' or '4 milligrams'. 
     */
    public static final IRI Quantity;

    /**
     * An RV park. 
     */
    public static final IRI RVPark;

    /**
     * A process of care using radiation aimed at improving a health condition. 
     */
    public static final IRI RadiationTherapy;

    /**
     * A radio station. 
     */
    public static final IRI RadioStation;

    /**
     * The rating of the video. 
     */
    public static final IRI Rating;

    /**
     * A real-estate agent. 
     */
    public static final IRI RealEstateAgent;

    /**
     * A recipe. 
     */
    public static final IRI Recipe;

    /**
     * A recommended dosing schedule for a drug or supplement as prescribed or recommended by an authority or by the drug/supplement's manufacturer. Capture the recommending authority in the recognizingAuthority property of MedicalEntity. 
     */
    public static final IRI RecommendedDoseSchedule;

    /**
     * A recycling center. 
     */
    public static final IRI RecyclingCenter;

    /**
     * A patient-reported or observed dosing schedule for a drug or supplement. 
     */
    public static final IRI ReportedDoseSchedule;

    /**
     * A reservoir, like the Lake Kariba reservoir. 
     */
    public static final IRI Reservoir;

    /**
     * The place where a person lives. 
     */
    public static final IRI Residence;

    /**
     * A restaurant. 
     */
    public static final IRI Restaurant;

    /**
     * A review of an item - for example, a restaurant, movie, or store. 
     */
    public static final IRI Review;

    /**
     * A river (for example, the broad majestic Shannon). 
     */
    public static final IRI RiverBodyOfWater;

    /**
     * A roofing contractor. 
     */
    public static final IRI RoofingContractor;

    /**
     * Event type: Sales event. 
     */
    public static final IRI SaleEvent;

    /**
     * A scholarly article. 
     */
    public static final IRI ScholarlyArticle;

    /**
     * A school. 
     */
    public static final IRI School;

    /**
     * A piece of sculpture. 
     */
    public static final IRI Sculpture;

    /**
     * A sea (for example, the Caspian sea). 
     */
    public static final IRI SeaBodyOfWater;

    /**
     * Web page type: Search results page. 
     */
    public static final IRI SearchResultsPage;

    /**
     * Self-storage facility. 
     */
    public static final IRI SelfStorage;

    /**
     * A shoe store. 
     */
    public static final IRI ShoeStore;

    /**
     * A shopping center or mall. 
     */
    public static final IRI ShoppingCenter;

    /**
     * Residence type: Single-family home. 
     */
    public static final IRI SingleFamilyResidence;

    /**
     * A navigation element of the page. 
     */
    public static final IRI SiteNavigationElement;

    /**
     * A ski resort. 
     */
    public static final IRI SkiResort;

    /**
     * Event type: Social event. 
     */
    public static final IRI SocialEvent;

    /**
     * A software application. 
     */
    public static final IRI SoftwareApplication;

    /**
     * A placeholder for multiple similar products of the same kind. 
     */
    public static final IRI SomeProducts;

    /**
     * Any branch of a field in which people typically develop specific expertise, usually after significant study, time, and effort. 
     */
    public static final IRI Specialty;

    /**
     * A sporting goods store. 
     */
    public static final IRI SportingGoodsStore;

    /**
     * A sports location, such as a playing field. 
     */
    public static final IRI SportsActivityLocation;

    /**
     * A sports club. 
     */
    public static final IRI SportsClub;

    /**
     * Event type: Sports event. 
     */
    public static final IRI SportsEvent;

    /**
     * Organization: Sports team. 
     */
    public static final IRI SportsTeam;

    /**
     * A stadium. 
     */
    public static final IRI StadiumOrArena;

    /**
     * A state or province. 
     */
    public static final IRI State;

    /**
     * A retail good store. 
     */
    public static final IRI Store;

    /**
     * Structured values are strings—for example, addresses—that have certain constraints on their structure. 
     */
    public static final IRI StructuredValue;

    /**
     * A subway station. 
     */
    public static final IRI SubwayStation;

    /**
     * Anatomical features that can be observed by sight (without dissection), including the form and proportions of the human body as well as surface landmarks that correspond to deeper subcutaneous structures. Superficial anatomy plays an important role in sports medicine, phlebotomy, and other medical specialties as underlying anatomical structures can be identified through surface palpation. For example, during back surgery, superficial anatomy can be used to palpate and count vertebrae to find the site of incision. Or in phlebotomy, superficial anatomy can be used to locate an underlying vein; for example, the median cubital vein can be located by palpating the borders of the cubital fossa (such as the epicondyles of the humerus) and then looking for the superficial signs of the vein, such as size, prominence, ability to refill after depression, and feel of surrounding tissue support. As another example, in a subluxation (dislocation) of the glenohumeral joint, the bony structure becomes pronounced with the deltoid muscle failing to cover the glenohumeral joint allowing the edges of the scapula to be superficially visible. Here, the superficial anatomy is the visible edges of the scapula, implying the underlying dislocation of the joint (the related anatomical structure). 
     */
    public static final IRI SuperficialAnatomy;

    /**
     * A synagogue. 
     */
    public static final IRI Synagogue;

    /**
     * An episode of a TV series or season. 
     */
    public static final IRI TVEpisode;

    /**
     * A TV season. 
     */
    public static final IRI TVSeason;

    /**
     * A television series. 
     */
    public static final IRI TVSeries;

    /**
     * A table on the page. 
     */
    public static final IRI Table;

    /**
     * A tattoo parlor. 
     */
    public static final IRI TattooParlor;

    /**
     * A taxi stand. 
     */
    public static final IRI TaxiStand;

    /**
     * A television station. 
     */
    public static final IRI TelevisionStation;

    /**
     * A tennis complex. 
     */
    public static final IRI TennisComplex;

    /**
     * Event type: Theater performance. 
     */
    public static final IRI TheaterEvent;

    /**
     * A theater group or company—for example, the Royal Shakespeare Company or Druid Theatre. 
     */
    public static final IRI TheaterGroup;

    /**
     * A medical procedure intended primarly for therapeutic purposes, aimed at improving a health condition. 
     */
    public static final IRI TherapeuticProcedure;

    /**
     * The most generic type of item. 
     */
    public static final IRI Thing;

    /**
     * A tire shop. 
     */
    public static final IRI TireShop;

    /**
     * A tourist attraction. 
     */
    public static final IRI TouristAttraction;

    /**
     * A tourist information center. 
     */
    public static final IRI TouristInformationCenter;

    /**
     * A toystore. 
     */
    public static final IRI ToyStore;

    /**
     * A train station. 
     */
    public static final IRI TrainStation;

    /**
     * A travel agency. 
     */
    public static final IRI TravelAgency;

    /**
     * An indication for treating an underlying condition, symptom, etc. 
     */
    public static final IRI TreatmentIndication;

    /**
     * A structured value indicating the quantity, unit of measurement, and business function of goods included in a bundle offer. 
     */
    public static final IRI TypeAndQuantityNode;

    /**
     * The price asked for a given offer by the respective organization or person. 
     */
    public static final IRI UnitPriceSpecification;

    /**
     * User interaction: Block this content. 
     */
    public static final IRI UserBlocks;

    /**
     * User interaction: Check-in at a place. 
     */
    public static final IRI UserCheckins;

    /**
     * The UserInteraction event in which a user comments on an item. 
     */
    public static final IRI UserComments;

    /**
     * User interaction: Download of an item. 
     */
    public static final IRI UserDownloads;

    /**
     * A user interacting with a page 
     */
    public static final IRI UserInteraction;

    /**
     * User interaction: Like an item. 
     */
    public static final IRI UserLikes;

    /**
     * User interaction: Visit to a web page. 
     */
    public static final IRI UserPageVisits;

    /**
     * User interaction: Play count of an item, for example a video or a song. 
     */
    public static final IRI UserPlays;

    /**
     * User interaction: +1. 
     */
    public static final IRI UserPlusOnes;

    /**
     * User interaction: Tweets. 
     */
    public static final IRI UserTweets;

    /**
     * A type of blood vessel that specifically carries blood to the heart. 
     */
    public static final IRI Vein;

    /**
     * A component of the human body circulatory system comprised of an intricate network of hollow tubes that transport blood throughout the entire body. 
     */
    public static final IRI Vessel;

    /**
     * A vet's office. 
     */
    public static final IRI VeterinaryCare;

    /**
     * Web page type: Video gallery page. 
     */
    public static final IRI VideoGallery;

    /**
     * A video file. 
     */
    public static final IRI VideoObject;

    /**
     * Event type: Visual arts event. 
     */
    public static final IRI VisualArtsEvent;

    /**
     * A volcano, like Fuji san 
     */
    public static final IRI Volcano;

    /**
     * An advertising section of the page. 
     */
    public static final IRI WPAdBlock;

    /**
     * The footer section of the page. 
     */
    public static final IRI WPFooter;

    /**
     * The header section of the page. 
     */
    public static final IRI WPHeader;

    /**
     * A sidebar section of the page. 
     */
    public static final IRI WPSideBar;

    /**
     * A structured value representing the duration and scope of services that will be provided to a customer free of charge in case of a defect or malfunction of a product. 
     */
    public static final IRI WarrantyPromise;

    /**
     * A range of of services that will be provided to a customer free of charge in case of a defect or malfunction of a product.

     Commonly used values:

     http://purl.org/goodrelations/v1#Labor-BringIn
     http://purl.org/goodrelations/v1#PartsAndLabor-BringIn
     http://purl.org/goodrelations/v1#PartsAndLabor-PickUp 
     */
    public static final IRI WarrantyScope;

    /**
     * A waterfall, like Niagara 
     */
    public static final IRI Waterfall;

    /**
     * Web applications. 
     */
    public static final IRI WebApplication;

    /**
     * A web page. Every web page is implicitly assumed to be declared to be of type WebPage, so the various properties about that webpage, such as breadcrumb may be used. We recommend explicit declaration if these properties are specified, but if they are found outside of an itemscope, they will be assumed to be about the page 
     */
    public static final IRI WebPage;

    /**
     * A web page element, like a table or an image 
     */
    public static final IRI WebPageElement;

    /**
     * A wholesale store. 
     */
    public static final IRI WholesaleStore;

    /**
     * A winery. 
     */
    public static final IRI Winery;

    /**
     * A zoo. 
     */
    public static final IRI Zoo;

    /**
     * The subject matter of the content. 
     */
    public static final IRI about;

    /**
     * The payment method(s) accepted by seller for this offer. 
     */
    public static final IRI acceptedPaymentMethod;

    /**
     * Either Yes/No, or a URL at which reservations can be made. 
     */
    public static final IRI acceptsReservations;

    /**
     * Specifies the Person that is legally accountable for the CreativeWork. 
     */
    public static final IRI accountablePerson;

    /**
     * The organization or person from which the product was acquired. 
     */
    public static final IRI acquiredFrom;

    /**
     * The movement the muscle generates. 
     */
    public static final IRI action;

    /**
     * An active ingredient, typically chemical compounds and/or biologic substances. 
     */
    public static final IRI activeIngredient;

    /**
     * Length of time to engage in the activity. 
     */
    public static final IRI activityDuration;

    /**
     * How often one should engage in the activity. 
     */
    public static final IRI activityFrequency;

    /**
     * A cast member of the movie, TV series, season, or episode, or video. 
     */
    public static final IRI actor;

    /**
     * A cast member of the movie, TV series, season, or episode, or video. (legacy spelling; see singular form, actor) 
     */
    public static final IRI actors;

    /**
     * An additional offer that can only be obtained in combination with the first base offer (e.g. supplements and extensions that are available for a surcharge). 
     */
    public static final IRI addOn;

    /**
     * An additional name for a Person, can be used for a middle name. 
     */
    public static final IRI additionalName;

    /**
     * An additional type for the item, typically used for adding more specific types from external vocabularies in microdata syntax. This is a relationship between something and a class that the thing is in. In RDFa syntax, it is better to use the native RDFa syntax - the 'typeof' attribute - for multiple types. Schema.org tools may have only weaker understanding of extra types, in particular those defined externally. 
     */
    public static final IRI additionalType;

    /**
     * Any additional component of the exercise prescription that may need to be articulated to the patient. This may include the order of exercises, the number of repetitions of movement, quantitative distance, progressions over time, etc. 
     */
    public static final IRI additionalVariable;

    /**
     * Physical address of the item. 
     */
    public static final IRI address;

    /**
     * The country. For example, USA. You can also provide the two-letter ISO 3166-1 alpha-2 country code. 
     */
    public static final IRI addressCountry;

    /**
     * The locality. For example, Mountain View. 
     */
    public static final IRI addressLocality;

    /**
     * The region. For example, CA. 
     */
    public static final IRI addressRegion;

    /**
     * A route by which this drug may be administered, e.g. 'oral'. 
     */
    public static final IRI administrationRoute;

    /**
     * The amount of time that is required between accepting the offer and the actual usage of the resource or service. 
     */
    public static final IRI advanceBookingRequirement;

    /**
     * A possible complication and/or side effect of this therapy. If it is known that an adverse outcome is serious (resulting in death, disability, or permanent damage; requiring hospitalization; or is otherwise life-threatening or requires immediate medical attention), tag it as a seriouseAdverseOutcome instead. 
     */
    public static final IRI adverseOutcome;

    /**
     * Drugs that affect the test's results. 
     */
    public static final IRI affectedBy;

    /**
     * An organization that this person is affiliated with. For example, a school/university, a club, or a team. 
     */
    public static final IRI affiliation;

    /**
     * The overall rating, based on a collection of reviews or ratings, of the item. 
     */
    public static final IRI aggregateRating;

    /**
     * A music album. 
     */
    public static final IRI album;

    /**
     * A collection of music albums (legacy spelling; see singular form, album). 
     */
    public static final IRI albums;

    /**
     * Any precaution, guidance, contraindication, etc. related to consumption of alcohol while taking this drug. 
     */
    public static final IRI alcoholWarning;

    /**
     * The algorithm or rules to follow to compute the score. 
     */
    public static final IRI algorithm;

    /**
     * Any alternate name for this medical entity. 
     */
    public static final IRI alternateName;

    /**
     * A secondary title of the CreativeWork. 
     */
    public static final IRI alternativeHeadline;

    /**
     * Alumni of educational organization. 
     */
    public static final IRI alumni;

    /**
     * An educational organizations that the person is an alumni of. 
     */
    public static final IRI alumniOf;

    /**
     * The quantity of the goods included in the offer. 
     */
    public static final IRI amountOfThisGood;

    /**
     * The muscle whose action counteracts the specified muscle. 
     */
    public static final IRI antagonist;

    /**
     * The location in which the status applies. 
     */
    public static final IRI applicableLocation;

    /**
     * Type of software application, e.g. "Game, Multimedia". 
     */
    public static final IRI applicationCategory;

    /**
     * Subcategory of the application, e.g. "Arcade Game". 
     */
    public static final IRI applicationSubCategory;

    /**
     * The name of the application suite to which the application belongs (e.g. Excel belongs to Office) 
     */
    public static final IRI applicationSuite;

    /**
     * The delivery method(s) to which the delivery charge or payment charge specification applies. 
     */
    public static final IRI appliesToDeliveryMethod;

    /**
     * The payment method(s) to which the payment charge specification applies. 
     */
    public static final IRI appliesToPaymentMethod;

    /**
     * The branches that comprise the arterial structure. 
     */
    public static final IRI arterialBranch;

    /**
     * The actual body of the article. 
     */
    public static final IRI articleBody;

    /**
     * Articles may belong to one or more 'sections' in a magazine or newspaper, such as Sports, Lifestyle, etc. 
     */
    public static final IRI articleSection;

    /**
     * An aspect of medical practice that is considered on the page, such as 'diagnosis', 'treatment', 'causes', 'prognosis', 'etiology', 'epidemiology', etc. 
     */
    public static final IRI aspect;

    /**
     * The anatomy of the underlying organ system or structures associated with this entity. 
     */
    public static final IRI associatedAnatomy;

    /**
     * A NewsArticle associated with the Media Object. 
     */
    public static final IRI associatedArticle;

    /**
     * The media objects that encode this creative work. This property is a synonym for encodings. 
     */
    public static final IRI associatedMedia;

    /**
     * If applicable, a description of the pathophysiology associated with the anatomical system, including potential abnormal changes in the mechanical, physical, and biochemical functions of the system. 
     */
    public static final IRI associatedPathophysiology;

    /**
     * A person or organization attending the event. 
     */
    public static final IRI attendee;

    /**
     * A person attending the event (legacy spelling; see singular form, attendee). 
     */
    public static final IRI attendees;

    /**
     * The intended audience of the work, i.e. the group for whom the work was created. 
     */
    public static final IRI audience;

    /**
     * An embedded audio object. 
     */
    public static final IRI audio;

    /**
     * The author of this content. Please note that author is special in that HTML 5 provides a special mechanism for indicating authorship via the rel tag. That is equivalent to this and may be used interchangeably. 
     */
    public static final IRI author;

    /**
     * The availability of this item—for example In stock, Out of stock, Pre-order, etc. 
     */
    public static final IRI availability;

    /**
     * The end of the availability of the product or service included in the offer. 
     */
    public static final IRI availabilityEnds;

    /**
     * The beginning of the availability of the product or service included in the offer. 
     */
    public static final IRI availabilityStarts;

    /**
     * The place(s) from which the offer can be obtained (e.g. store locations). 
     */
    public static final IRI availableAtOrFrom;

    /**
     * The delivery method(s) available for this offer. 
     */
    public static final IRI availableDeliveryMethod;

    /**
     * The location in which the strength is available. 
     */
    public static final IRI availableIn;

    /**
     * A medical service available from this provider. 
     */
    public static final IRI availableService;

    /**
     * An available dosage strength for the drug. 
     */
    public static final IRI availableStrength;

    /**
     * A diagnostic test or procedure offered by this lab. 
     */
    public static final IRI availableTest;

    /**
     * An award won by this person or for this creative work. 
     */
    public static final IRI award;

    /**
     * Awards won by this person or for this creative work. (legacy spelling; see singular form, award) 
     */
    public static final IRI awards;

    /**
     * Descriptive information establishing a historical perspective on the supplement. May include the rationale for the name, the population where the supplement first came to prominence, etc. 
     */
    public static final IRI background;

    /**
     * The base salary of the job. 
     */
    public static final IRI baseSalary;

    /**
     * Description of benefits associated with the job. 
     */
    public static final IRI benefits;

    /**
     * The highest value allowed in this rating system. If bestRating is omitted, 5 is assumed. 
     */
    public static final IRI bestRating;

    /**
     * This property specifies the minimal quantity and rounding increment that will be the basis for the billing. The unit of measurement is specified by the unitCode property. 
     */
    public static final IRI billingIncrement;

    /**
     * The biomechanical properties of the bone. 
     */
    public static final IRI biomechnicalClass;

    /**
     * Date of birth. 
     */
    public static final IRI birthDate;

    /**
     * The bitrate of the media object. 
     */
    public static final IRI bitrate;

    /**
     * A posting that is part of this blog. 
     */
    public static final IRI blogPost;

    /**
     * The postings that are part of this blog (legacy spelling; see singular form, blogPost). 
     */
    public static final IRI blogPosts;

    /**
     * The blood vessel that carries blood from the heart to the muscle. 
     */
    public static final IRI bloodSupply;

    /**
     * Location in the body of the anatomical structure. 
     */
    public static final IRI bodyLocation;

    /**
     * The edition of the book. 
     */
    public static final IRI bookEdition;

    /**
     * The format of the book. 
     */
    public static final IRI bookFormat;

    /**
     * A polygon is the area enclosed by a point-to-point path for which the starting and ending points are the same. A polygon is expressed as a series of four or more spacedelimited points where the first and final points are identical. 
     */
    public static final IRI box;

    /**
     * The branches that delineate from the nerve bundle. 
     */
    public static final IRI branch;

    /**
     * The larger organization that this local business is a branch of, if any. 
     */
    public static final IRI branchOf;

    /**
     * The brand(s) associated with a product or service, or the brand(s) maintained by an organization or business person. 
     */
    public static final IRI brand;

    /**
     * A set of links that can help a user understand and navigate a website hierarchy. 
     */
    public static final IRI breadcrumb;

    /**
     * Any precaution, guidance, contraindication, etc. related to this drug's use by breastfeeding mothers. 
     */
    public static final IRI breastfeedingWarning;

    /**
     * Specifies browser requirements in human-readable text. For example,"requires HTML5 support". 
     */
    public static final IRI browserRequirements;

    /**
     * The business function (e.g. sell, lease, repair, dispose) of the offer or component of a bundle (TypeAndQuantityNode). The default is http://purl.org/goodrelations/v1#Sell. 
     */
    public static final IRI businessFunction;

    /**
     * The artist that performed this album or recording. 
     */
    public static final IRI byArtist;

    /**
     * The number of calories 
     */
    public static final IRI calories;

    /**
     * The caption for this object. 
     */
    public static final IRI caption;

    /**
     * The number of grams of carbohydrates. 
     */
    public static final IRI carbohydrateContent;

    /**
     * Specifies specific carrier(s) requirements for the application (e.g. an application may only work on a specific carrier network). 
     */
    public static final IRI carrierRequirements;

    /**
     * A category for the item. Greater signs or slashes can be used to informally indicate a category hierarchy. 
     */
    public static final IRI category;

    /**
     * An underlying cause. More specifically, one of the causative agent(s) that are most directly responsible for the pathophysiologic process that eventually results in the occurrence. 
     */
    public static final IRI cause;

    /**
     * The condition, complication, symptom, sign, etc. caused. 
     */
    public static final IRI causeOf;

    /**
     * A child of the person. 
     */
    public static final IRI children;

    /**
     * The number of milligrams of cholesterol. 
     */
    public static final IRI cholesterolContent;

    /**
     * A circle is the circular region of a specified radius centered at a specified latitude and longitude. A circle is expressed as a pair followed by a radius in meters. 
     */
    public static final IRI circle;

    /**
     * A citation or reference to another creative work, such as another publication, web page, scholarly article, etc. NOTE: Candidate for promotion to ScholarlyArticle. 
     */
    public static final IRI citation;

    /**
     * Description of the absorption and elimination of drugs, including their concentration (pharmacokinetics, pK) and biological effects (pharmacodynamics, pD). 
     */
    public static final IRI clincalPharmacology;

    /**
     * The closing hour of the place or service on the given day(s) of the week. 
     */
    public static final IRI closes;

    /**
     * A medical code for the entity, taken from a controlled vocabulary or ontology such as ICD-9, DiseasesDB, MeSH, SNOMED-CT, RxNorm, etc. 
     */
    public static final IRI code;

    /**
     * The actual code. 
     */
    public static final IRI codeValue;

    /**
     * The coding system, e.g. 'ICD-10'. 
     */
    public static final IRI codingSystem;

    /**
     * A colleague of the person. 
     */
    public static final IRI colleague;

    /**
     * A colleague of the person (legacy spelling; see singular form, colleague). 
     */
    public static final IRI colleagues;

    /**
     * The color of the product. 
     */
    public static final IRI color;

    /**
     * Comments, typically from users, on this CreativeWork. 
     */
    public static final IRI comment;

    /**
     * The text of the UserComment. 
     */
    public static final IRI commentText;

    /**
     * The time at which the UserComment was made. 
     */
    public static final IRI commentTime;

    /**
     * The underlying anatomical structures, such as organs, that comprise the anatomical system. 
     */
    public static final IRI comprisedOf;

    /**
     * Other anatomical structures to which this structure is connected. 
     */
    public static final IRI connectedTo;

    /**
     * A contact point for a person or organization. 
     */
    public static final IRI contactPoint;

    /**
     * A contact point for a person or organization (legacy spelling; see singular form, contactPoint). 
     */
    public static final IRI contactPoints;

    /**
     * A person or organization can have different contact points, for different purposes. For example, a sales contact point, a PR contact point and so on. This property is used to specify the kind of contact point. 
     */
    public static final IRI contactType;

    /**
     * The basic containment relation between places. 
     */
    public static final IRI containedIn;

    /**
     * The location of the content. 
     */
    public static final IRI contentLocation;

    /**
     * Official rating of a piece of content—for example,'MPAA PG-13'. 
     */
    public static final IRI contentRating;

    /**
     * File size in (mega/kilo) bytes. 
     */
    public static final IRI contentSize;

    /**
     * Actual bytes of the media object, for example the image file or video file. (previous spelling: contentURL) 
     */
    public static final IRI contentUrl;

    /**
     * A contraindication for this therapy. 
     */
    public static final IRI contraindication;

    /**
     * A secondary contributor to the CreativeWork. 
     */
    public static final IRI contributor;

    /**
     * The time it takes to actually cook the dish, in ISO 8601 duration format. 
     */
    public static final IRI cookTime;

    /**
     * The method of cooking, such as Frying, Steaming, ... 
     */
    public static final IRI cookingMethod;

    /**
     * The party holding the legal copyright to the CreativeWork. 
     */
    public static final IRI copyrightHolder;

    /**
     * The year during which the claimed copyright for the CreativeWork was first asserted. 
     */
    public static final IRI copyrightYear;

    /**
     * Cost per unit of the drug, as reported by the source being tagged. 
     */
    public static final IRI cost;

    /**
     * The category of cost, such as wholesale, retail, reimbursement cap, etc. 
     */
    public static final IRI costCategory;

    /**
     * The currency (in 3-letter ISO 4217 format) of the drug cost. 
     */
    public static final IRI costCurrency;

    /**
     * Additional details to capture the origin of the cost data. For example, 'Medicare Part B'. 
     */
    public static final IRI costOrigin;

    /**
     * The cost per unit of the drug. 
     */
    public static final IRI costPerUnit;

    /**
     * Countries for which the application is not supported. You can also provide the two-letter ISO 3166-1 alpha-2 country code. 
     */
    public static final IRI countriesNotSupported;

    /**
     * Countries for which the application is supported. You can also provide the two-letter ISO 3166-1 alpha-2 country code. 
     */
    public static final IRI countriesSupported;

    /**
     * The creator/author of this CreativeWork or UserComments. This is the same as the Author property for CreativeWork. 
     */
    public static final IRI creator;

    /**
     * The currency accepted (in ISO 4217 currency format). 
     */
    public static final IRI currenciesAccepted;

    /**
     * The date on which the CreativeWork was created. 
     */
    public static final IRI dateCreated;

    /**
     * The date on which the CreativeWork was most recently modified. 
     */
    public static final IRI dateModified;

    /**
     * Publication date for the job posting. 
     */
    public static final IRI datePosted;

    /**
     * Date of first broadcast/publication. 
     */
    public static final IRI datePublished;

    /**
     * The location where the NewsArticle was produced. 
     */
    public static final IRI dateline;

    /**
     * The day of the week for which these opening hours are valid. 
     */
    public static final IRI dayOfWeek;

    /**
     * Date of death. 
     */
    public static final IRI deathDate;

    /**
     * The typical delay between the receipt of the order and the goods leaving the warehouse. 
     */
    public static final IRI deliveryLeadTime;

    /**
     * The depth of the product. 
     */
    public static final IRI depth;

    /**
     * A short description of the item. 
     */
    public static final IRI description;

    /**
     * Device required to run the application. Used in cases where a specific make/model is required to run the application. 
     */
    public static final IRI device;

    /**
     * One or more alternative conditions considered in the differential diagnosis process. 
     */
    public static final IRI diagnosis;

    /**
     * An image containing a diagram that illustrates the structure and/or its component substructures and/or connections with other structures. 
     */
    public static final IRI diagram;

    /**
     * Nutritional information specific to the dietary plan. May include dietary recommendations on what foods to avoid, what foods to consume, and specific alterations/deviations from the USDA or other regulatory body's approved dietary guidelines. 
     */
    public static final IRI dietFeatures;

    /**
     * One of a set of differential diagnoses for the condition. Specifically, a closely-related or competing diagnosis typically considered later in the cognitive process whereby this medical condition is distinguished from others most likely responsible for a similar collection of signs and symptoms to reach the most parsimonious diagnosis or diagnoses in a patient. 
     */
    public static final IRI differentialDiagnosis;

    /**
     * The director of the movie, TV episode, or series. 
     */
    public static final IRI director;

    /**
     * Specifies the CreativeWork associated with the UserComment. 
     */
    public static final IRI discusses;

    /**
     * A link to the page containing the comments of the CreativeWork. 
     */
    public static final IRI discussionUrl;

    /**
     * One of a set of signs and symptoms that can be used to distinguish this diagnosis from others in the differential diagnosis. 
     */
    public static final IRI distinguishingSign;

    /**
     * A dosage form in which this drug/supplement is available, e.g. 'tablet', 'suspension', 'injection'. 
     */
    public static final IRI dosageForm;

    /**
     * A dosing schedule for the drug for a given population, either observed, recommended, or maximum dose based on the type used. 
     */
    public static final IRI doseSchedule;

    /**
     * The unit of the dose, e.g. 'mg'. 
     */
    public static final IRI doseUnit;

    /**
     * The value of the dose, e.g. 500. 
     */
    public static final IRI doseValue;

    /**
     * If the file can be downloaded, URL to download the binary. 
     */
    public static final IRI downloadUrl;

    /**
     * The vasculature that the vein drains into. 
     */
    public static final IRI drainsTo;

    /**
     * A drug in this drug class. 
     */
    public static final IRI drug;

    /**
     * The class of drug this belongs to (e.g., statins). 
     */
    public static final IRI drugClass;

    /**
     * The unit in which the drug is measured, e.g. '5 mg tablet'. 
     */
    public static final IRI drugUnit;

    /**
     * The Dun & Bradstreet DUNS number for identifying an organization or business person. 
     */
    public static final IRI duns;

    /**
     * A therapy that duplicates or overlaps this one. 
     */
    public static final IRI duplicateTherapy;

    /**
     * The duration of the item (movie, audio recording, event, etc.) in ISO 8601 date format. 
     */
    public static final IRI duration;

    /**
     * The duration of the warranty promise. Common unitCode values are ANN for year, MON for months, or DAY for days. 
     */
    public static final IRI durationOfWarranty;

    /**
     * Specifies the Person who edited the CreativeWork. 
     */
    public static final IRI editor;

    /**
     * Educational background needed for the position. 
     */
    public static final IRI educationRequirements;

    /**
     * The elevation of a location. 
     */
    public static final IRI elevation;

    /**
     * The type(s) of customers for which the given offer is valid. 
     */
    public static final IRI eligibleCustomerType;

    /**
     * The duration for which the given offer is valid. 
     */
    public static final IRI eligibleDuration;

    /**
     * The interval and unit of measurement of ordering quantities for which the offer or price specification is valid. This allows e.g. specifying that a certain freight charge is valid only for a certain quantity. 
     */
    public static final IRI eligibleQuantity;

    /**
     * The ISO 3166-1 (ISO 3166-1 alpha-2) or ISO 3166-2 code, or the GeoShape for the geo-political region(s) for which the offer or delivery charge specification is valid. 
     */
    public static final IRI eligibleRegion;

    /**
     * The transaction volume, in a monetary unit, for which the offer or price specification is valid, e.g. for indicating a minimal purchasing volume, to express free shipping above a certain order volume, or to limit the acceptance of credit cards to purchases to a certain minimal amount. 
     */
    public static final IRI eligibleTransactionVolume;

    /**
     * Email address. 
     */
    public static final IRI email;

    /**
     * A URL pointing to a player for a specific video. In general, this is the information in the src element of an embed tag and should not be the same as the content of the loc tag. (previous spelling: embedURL) 
     */
    public static final IRI embedUrl;

    /**
     * Someone working for this organization. 
     */
    public static final IRI employee;

    /**
     * People working for this organization. (legacy spelling; see singular form, employee) 
     */
    public static final IRI employees;

    /**
     * Type of employment (e.g. full-time, part-time, contract, temporary, seasonal, internship). 
     */
    public static final IRI employmentType;

    /**
     * The creative work encoded by this media object 
     */
    public static final IRI encodesCreativeWork;

    /**
     * A media object that encode this CreativeWork. 
     */
    public static final IRI encoding;

    /**
     * mp3, mpeg4, etc. 
     */
    public static final IRI encodingFormat;

    /**
     * The media objects that encode this creative work (legacy spelling; see singular form, encoding). 
     */
    public static final IRI encodings;

    /**
     * The end date and time of the event (in ISO 8601 date format). 
     */
    public static final IRI endDate;

    /**
     * People or organizations that endorse the plan. 
     */
    public static final IRI endorsers;

    /**
     * The characteristics of associated patients, such as age, gender, race etc. 
     */
    public static final IRI epidemiology;

    /**
     * An episode of a TV series or season. 
     */
    public static final IRI episode;

    /**
     * The episode number. 
     */
    public static final IRI episodeNumber;

    /**
     * The episode of a TV series or season (legacy spelling; see singular form, episode). 
     */
    public static final IRI episodes;

    /**
     * This ordering relation for qualitative values indicates that the subject is equal to the object. 
     */
    public static final IRI equal;

    /**
     * The condition, complication, or symptom whose risk is being estimated. 
     */
    public static final IRI estimatesRiskOf;

    /**
     * Upcoming or past event associated with this place or organization. 
     */
    public static final IRI event;

    /**
     * Upcoming or past events associated with this place or organization (legacy spelling; see singular form, event). 
     */
    public static final IRI events;

    /**
     * Strength of evidence of the data used to formulate the guideline (enumerated). 
     */
    public static final IRI evidenceLevel;

    /**
     * Source of the data used to formulate the guidance, e.g. RCT, consensus opinion, etc. 
     */
    public static final IRI evidenceOrigin;

    /**
     * Type(s) of exercise or activity, such as strength training, flexibility training, aerobics, cardiac rehabilitation, etc. 
     */
    public static final IRI exerciseType;

    /**
     * exif data for this object. 
     */
    public static final IRI exifData;

    /**
     * The likely outcome in either the short term or long term of the medical condition. 
     */
    public static final IRI expectedPrognosis;

    /**
     * Description of skills and experience needed for the position. 
     */
    public static final IRI experienceRequirements;

    /**
     * Medical expert advice related to the plan. 
     */
    public static final IRI expertConsiderations;

    /**
     * Date the content expires and is no longer useful or available. Useful for videos. 
     */
    public static final IRI expires;

    /**
     * Family name. In the U.S., the last name of an Person. This can be used along with givenName instead of the Name property. 
     */
    public static final IRI familyName;

    /**
     * The number of grams of fat. 
     */
    public static final IRI fatContent;

    /**
     * The fax number. 
     */
    public static final IRI faxNumber;

    /**
     * Features or modules provided by this application (and possibly required by other applications). 
     */
    public static final IRI featureList;

    /**
     * The number of grams of fiber. 
     */
    public static final IRI fiberContent;

    /**
     * MIME format of the binary (e.g. application/zip). 
     */
    public static final IRI fileFormat;

    /**
     * Size of the application / package (e.g. 18MB). In the absence of a unit (MB, KB etc.), KB will be assumed. 
     */
    public static final IRI fileSize;

    /**
     * The most generic uni-directional social relation. 
     */
    public static final IRI follows;

    /**
     * Typical or recommended followup care after the procedure is performed. 
     */
    public static final IRI followup;

    /**
     * Any precaution, guidance, contraindication, etc. related to consumption of specific foods while taking this drug. 
     */
    public static final IRI foodWarning;

    /**
     * A person who founded this organization. 
     */
    public static final IRI founder;

    /**
     * A person who founded this organization (legacy spelling; see singular form, founder). 
     */
    public static final IRI founders;

    /**
     * The date that this organization was founded. 
     */
    public static final IRI foundingDate;

    /**
     * How often the dose is taken, e.g. 'daily'. 
     */
    public static final IRI frequency;

    /**
     * Function of the anatomical structure. 
     */
    public static final IRI function;

    /**
     * The degree of mobility the joint allows. 
     */
    public static final IRI functionalClass;

    /**
     * Gender of the person. 
     */
    public static final IRI gender;

    /**
     * Genre of the creative work 
     */
    public static final IRI genre;

    /**
     * The geo coordinates of the place. 
     */
    public static final IRI geo;

    /**
     * Given name. In the U.S., the first name of a Person. This can be used along with familyName instead of the Name property. 
     */
    public static final IRI givenName;

    /**
     * The Global Location Number (GLN, sometimes also referred to as International Location Number or ILN) of the respective organization, person, or place. The GLN is a 13-digit number used to identify parties and physical locations. 
     */
    public static final IRI globalLocationNumber;

    /**
     * This ordering relation for qualitative values indicates that the subject is greater than the object. 
     */
    public static final IRI greater;

    /**
     * This ordering relation for qualitative values indicates that the subject is greater than or equal to the object. 
     */
    public static final IRI greaterOrEqual;

    /**
     * The GTIN-13 code of the product, or the product to which the offer refers. This is equivalent to 13-digit ISBN codes and EAN UCC-13. Former 12-digit UPC codes can be converted into a GTIN-13 code by simply adding a preceeding zero. 
     */
    public static final IRI gtin13;

    /**
     * The GTIN-14 code of the product, or the product to which the offer refers. 
     */
    public static final IRI gtin14;

    /**
     * The GTIN-8 code of the product, or the product to which the offer refers. This code is also known as EAN/UCC-8 or 8-digit EAN. 
     */
    public static final IRI gtin8;

    /**
     * A medical guideline related to this entity. 
     */
    public static final IRI guideline;

    /**
     * Date on which this guideline's recommendation was made. 
     */
    public static final IRI guidelineDate;

    /**
     * The medical conditions, treatments, etc. that are the subject of the guideline. 
     */
    public static final IRI guidelineSubject;

    /**
     * Points-of-Sales operated by the organization or person. 
     */
    public static final IRI hasPOS;

    /**
     * Headline of the article 
     */
    public static final IRI headline;

    /**
     * The height of the item. 
     */
    public static final IRI height;

    /**
     * The highest price of all offers available. 
     */
    public static final IRI highPrice;

    /**
     * Organization offering the job position. 
     */
    public static final IRI hiringOrganization;

    /**
     * A contact location for a person's residence. 
     */
    public static final IRI homeLocation;

    /**
     * An honorific prefix preceding a Person's name such as Dr/Mrs/Mr. 
     */
    public static final IRI honorificPrefix;

    /**
     * An honorific suffix preceding a Person's name such as M.D. /PhD/MSCSW. 
     */
    public static final IRI honorificSuffix;

    /**
     * A hospital with which the physician or office is affiliated. 
     */
    public static final IRI hospitalAffiliation;

    /**
     * How the procedure is performed. 
     */
    public static final IRI howPerformed;

    /**
     * A physical examination that can identify this sign. 
     */
    public static final IRI identifyingExam;

    /**
     * A diagnostic test that can identify this sign. 
     */
    public static final IRI identifyingTest;

    /**
     * The illustrator of the book. 
     */
    public static final IRI illustrator;

    /**
     * URL of an image of the item. 
     */
    public static final IRI image;

    /**
     * Imaging technique used. 
     */
    public static final IRI imagingTechnique;

    /**
     * The album to which this recording belongs. 
     */
    public static final IRI inAlbum;

    /**
     * The language of the content. please use one of the language codes from the IETF BCP 47 standard. 
     */
    public static final IRI inLanguage;

    /**
     * The playlist to which this recording belongs. 
     */
    public static final IRI inPlaylist;

    /**
     * Description of bonus and commission compensation aspects of the job. 
     */
    public static final IRI incentives;

    /**
     * A modifiable or non-modifiable risk factor included in the calculation, e.g. age, coexisting condition. 
     */
    public static final IRI includedRiskFactor;

    /**
     * This links to a node or nodes indicating the exact quantity of the products included in the offer. 
     */
    public static final IRI includesObject;

    /**
     * The condition, complication, etc. influenced by this factor. 
     */
    public static final IRI increasesRiskOf;

    /**
     * A factor that indicates use of this therapy for treatment and/or prevention of a condition, symptom, etc. For therapies such as drugs, indications can include both officially-approved indications as well as off-label uses. These can be distinguished by using the ApprovedIndication subtype of MedicalIndication. 
     */
    public static final IRI indication;

    /**
     * The industry associated with the job position. 
     */
    public static final IRI industry;

    /**
     * The actual infectious agent, such as a specific bacterium. 
     */
    public static final IRI infectiousAgent;

    /**
     * The class of infectious agent (bacteria, prion, etc.) that causes the disease. 
     */
    public static final IRI infectiousAgentClass;

    /**
     * An ingredient used in the recipe. 
     */
    public static final IRI ingredients;

    /**
     * The place of attachment of a muscle, or what the muscle moves. 
     */
    public static final IRI insertion;

    /**
     * URL at which the app may be installed, if different from the URL of the item. 
     */
    public static final IRI installUrl;

    /**
     * Quantitative measure gauging the degree of force involved in the exercise, for example, heartbeats per minute. May include the velocity of the movement. 
     */
    public static final IRI intensity;

    /**
     * Another drug that is known to interact with this drug in a way that impacts the effect of this drug or causes a risk to the patient. Note: disease interactions are typically captured as contraindications. 
     */
    public static final IRI interactingDrug;

    /**
     * A count of a specific user interactions with this item—for example, 20 UserLikes, 5 UserComments, or 300 UserDownloads. The user interaction type should be one of the sub types of UserInteraction. 
     */
    public static final IRI interactionCount;

    /**
     * The current approximate inventory level for the item or items. 
     */
    public static final IRI inventoryLevel;

    /**
     * A pointer to another product (or multiple products) for which this product is an accessory or spare part. 
     */
    public static final IRI isAccessoryOrSparePartFor;

    /**
     * True if the drug is available in a generic form (regardless of name). 
     */
    public static final IRI isAvailableGenerically;

    /**
     * A pointer to another product (or multiple products) for which this product is a consumable. 
     */
    public static final IRI isConsumableFor;

    /**
     * Indicates whether this content is family friendly. 
     */
    public static final IRI isFamilyFriendly;

    /**
     * Indicates the collection or gallery to which the item belongs. 
     */
    public static final IRI isPartOf;

    /**
     * True if this item's name is a proprietary/brand name (vs. generic name). 
     */
    public static final IRI isProprietary;

    /**
     * A pointer to another, somehow related product (or multiple products). 
     */
    public static final IRI isRelatedTo;

    /**
     * A pointer to another, functionally similar product (or multiple products). 
     */
    public static final IRI isSimilarTo;

    /**
     * A pointer to a base product from which this product is a variant. It is safe to infer that the variant inherits all product features from the base model, unless defined locally. This is not transitive. 
     */
    public static final IRI isVariantOf;

    /**
     * The ISBN of the book. 
     */
    public static final IRI isbn;

    /**
     * The International Standard of Industrial Classification of All Economic Activities (ISIC), Revision 4 code for a particular organization, business person, or place. 
     */
    public static final IRI isicV4;

    /**
     * A predefined value from OfferItemCondition or a textual description of the condition of the product or service, or the products or services included in the offer. 
     */
    public static final IRI itemCondition;

    /**
     * A single list item. 
     */
    public static final IRI itemListElement;

    /**
     * Type of ordering (e.g. Ascending, Descending, Unordered). 
     */
    public static final IRI itemListOrder;

    /**
     * The item being sold. 
     */
    public static final IRI itemOffered;

    /**
     * The item that is being reviewed/rated. 
     */
    public static final IRI itemReviewed;

    /**
     * A (typically single) geographic location associated with the job position. 
     */
    public static final IRI jobLocation;

    /**
     * The job title of the person (for example, Financial Manager). 
     */
    public static final IRI jobTitle;

    /**
     * The keywords/tags used to describe this content. 
     */
    public static final IRI keywords;

    /**
     * The most generic bi-directional social/work relation. 
     */
    public static final IRI knows;

    /**
     * Link to the drug's label details. 
     */
    public static final IRI labelDetails;

    /**
     * Date on which the content on this web page was last reviewed for accuracy and/or completeness. 
     */
    public static final IRI lastReviewed;

    /**
     * The latitude of a location. For example 37.42242. 
     */
    public static final IRI latitude;

    /**
     * The official name of the organization, e.g. the registered company name. 
     */
    public static final IRI legalName;

    /**
     * The drug or supplement's legal status, including any controlled substance schedules that apply. 
     */
    public static final IRI legalStatus;

    /**
     * This ordering relation for qualitative values indicates that the subject is lesser than the object. 
     */
    public static final IRI lesser;

    /**
     * This ordering relation for qualitative values indicates that the subject is lesser than or equal to the object. 
     */
    public static final IRI lesserOrEqual;

    /**
     * A line is a point-to-point path consisting of two or more points. A line is expressed as a series of two or more point objects separated by space. 
     */
    public static final IRI line;

    /**
     * The location of the event or organization. 
     */
    public static final IRI location;

    /**
     * URL of an image for the logo of the item. 
     */
    public static final IRI logo;

    /**
     * The longitude of a location. For example -122.08585. 
     */
    public static final IRI longitude;

    /**
     * The lowest price of all offers available. 
     */
    public static final IRI lowPrice;

    /**
     * Indicates if this web page element is the main subject of the page. 
     */
    public static final IRI mainContentOfPage;

    /**
     * A pointer to products or services offered by the organization or person. 
     */
    public static final IRI makesOffer;

    /**
     * The manufacturer of the product. 
     */
    public static final IRI manufacturer;

    /**
     * A URL to a map of the place. 
     */
    public static final IRI map;

    /**
     * A URL to a map of the place (legacy spelling; see singular form, map). 
     */
    public static final IRI maps;

    /**
     * The highest price if the price is a range. 
     */
    public static final IRI maxPrice;

    /**
     * The upper of the product characteristic. 
     */
    public static final IRI maxValue;

    /**
     * Recommended intake of this supplement for a given population as defined by a specific recommending authority. 
     */
    public static final IRI maximumIntake;

    /**
     * The specific biochemical interaction through which this drug or supplement produces its pharmacological effect. 
     */
    public static final IRI mechanismOfAction;

    /**
     * A medical specialty of the provider. 
     */
    public static final IRI medicalSpecialty;

    /**
     * The system of medicine that includes this MedicalEntity, for example 'evidence-based', 'homeopathic', 'chiropractic', etc. 
     */
    public static final IRI medicineSystem;

    /**
     * A member of this organization. 
     */
    public static final IRI member;

    /**
     * An organization to which the person belongs. 
     */
    public static final IRI memberOf;

    /**
     * A member of this organization (legacy spelling; see singular form, member). 
     */
    public static final IRI members;

    /**
     * Minimum memory requirements. 
     */
    public static final IRI memoryRequirements;

    /**
     * Indicates that the CreativeWork contains a reference to, but is not necessarily about a concept. 
     */
    public static final IRI mentions;

    /**
     * Either the actual menu or a URL of the menu. 
     */
    public static final IRI menu;

    /**
     * The lowest price if the price is a range. 
     */
    public static final IRI minPrice;

    /**
     * The lower value of the product characteristic. 
     */
    public static final IRI minValue;

    /**
     * The model of the product. Use with the URL of a ProductModel or a textual representation of the model identifier. The URL of the ProductModel can be from an external source. It is recommended to additionally provide strong product identifiers via the gtin8/gtin13/gtin14 and mpn properties. 
     */
    public static final IRI model;

    /**
     * The Manufacturer Part Number (MPN) of the product, or the product to which the offer refers. 
     */
    public static final IRI mpn;

    /**
     * The composer of the movie or TV soundtrack. 
     */
    public static final IRI musicBy;

    /**
     * A member of the music group—for example, John, Paul, George, or Ringo. 
     */
    public static final IRI musicGroupMember;

    /**
     * The North American Industry Classification System (NAICS) code for a particular organization or business person. 
     */
    public static final IRI naics;

    /**
     * The name of the item. 
     */
    public static final IRI name;

    /**
     * Nationality of the person. 
     */
    public static final IRI nationality;

    /**
     * The expected progression of the condition if it is not treated and allowed to progress naturally. 
     */
    public static final IRI naturalProgression;

    /**
     * The underlying innervation associated with the muscle. 
     */
    public static final IRI nerve;

    /**
     * The neurological pathway extension that involves muscle control. 
     */
    public static final IRI nerveMotor;

    /**
     * This ordering relation for qualitative values indicates that the subject is not equal to the object. 
     */
    public static final IRI nonEqual;

    /**
     * The generic name of this drug or supplement. 
     */
    public static final IRI nonProprietaryName;

    /**
     * Range of acceptable values for a typical patient, when applicable. 
     */
    public static final IRI normalRange;

    /**
     * The number of tracks in this album or playlist. 
     */
    public static final IRI numTracks;

    /**
     * The number of episodes in this season or series. 
     */
    public static final IRI numberOfEpisodes;

    /**
     * The number of pages in the book. 
     */
    public static final IRI numberOfPages;

    /**
     * Nutrition information about the recipe. 
     */
    public static final IRI nutrition;

    /**
     * Category or categories describing the job. Use BLS O*NET-SOC taxonomy: http://www.onetcenter.org/taxonomy.html. Ideally includes textual label and formal code, with the property repeated for each applicable value. 
     */
    public static final IRI occupationalCategory;

    /**
     * The number of offers for the product. 
     */
    public static final IRI offerCount;

    /**
     * An offer to sell this item—for example, an offer to sell a product, the DVD of a movie, or tickets to an event. 
     */
    public static final IRI offers;

    /**
     * The opening hours for a business. Opening hours can be specified as a weekly time range, starting with days, then times per day. Multiple days can be listed with commas ',' separating each day. Day or time ranges are specified using a hyphen '-'.- Days are specified using the following two-letter combinations: Mo, Tu, We, Th, Fr, Sa, Su.- Times are specified using 24:00 time. For example, 3pm is specified as 15:00. - Here is an example: <time itemprop="openingHours" datetime="Tu,Th 16:00-20:00">Tuesdays and Thursdays 4-8pm</time>. - If a business is open 7 days a week, then it can be specified as <time itemprop="openingHours" datetime="Mo-Su">Monday through Sunday, all day</time>. 
     */
    public static final IRI openingHours;

    /**
     * The opening hours of a certain place. 
     */
    public static final IRI openingHoursSpecification;

    /**
     * The opening hour of the place or service on the given day(s) of the week. 
     */
    public static final IRI opens;

    /**
     * Operating systems supported (Windows 7, OSX 10.6, Android 1.6). 
     */
    public static final IRI operatingSystem;

    /**
     * The place or point where a muscle arises. 
     */
    public static final IRI origin;

    /**
     * The vasculature the lymphatic structure originates, or afferents, from. 
     */
    public static final IRI originatesFrom;

    /**
     * Expected or actual outcomes of the study. 
     */
    public static final IRI outcome;

    /**
     * Any information related to overdose on a drug, including signs or symptoms, treatments, contact information for emergency response. 
     */
    public static final IRI overdosage;

    /**
     * Descriptive information establishing the overarching theory/philosophy of the plan. May include the rationale for the name, the population where the plan first came to prominence, etc. 
     */
    public static final IRI overview;

    /**
     * The date and time of obtaining the product. 
     */
    public static final IRI ownedFrom;

    /**
     * The date and time of giving up ownership on the product. 
     */
    public static final IRI ownedThrough;

    /**
     * Products owned by the organization or person. 
     */
    public static final IRI owns;

    /**
     * A parent of this person. 
     */
    public static final IRI parent;

    /**
     * A parents of the person (legacy spelling; see singular form, parent). 
     */
    public static final IRI parents;

    /**
     * The season to which this episode belongs. 
     */
    public static final IRI partOfSeason;

    /**
     * The anatomical or organ system that this structure is part of. 
     */
    public static final IRI partOfSystem;

    /**
     * The TV series to which this episode or season belongs. 
     */
    public static final IRI partOfTVSeries;

    /**
     * Changes in the normal mechanical, physical, and biochemical functions that are associated with this activity or condition. 
     */
    public static final IRI pathophysiology;

    /**
     * Cash, credit card, etc. 
     */
    public static final IRI paymentAccepted;

    /**
     * A performer at the event—for example, a presenter, musician, musical group or actor. 
     */
    public static final IRI performer;

    /**
     * Event that this person is a performer or participant in. 
     */
    public static final IRI performerIn;

    /**
     * The main performer or performers of the event—for example, a presenter, musician, or actor (legacy spelling; see singular form, performer). 
     */
    public static final IRI performers;

    /**
     * Permission(s) required to run the app (for example, a mobile app may require full internet access or may run only on wifi). 
     */
    public static final IRI permissions;

    /**
     * The phase of the trial. 
     */
    public static final IRI phase;

    /**
     * A photograph of this place. 
     */
    public static final IRI photo;

    /**
     * Photographs of this place (legacy spelling; see singular form, photo). 
     */
    public static final IRI photos;

    /**
     * Specific physiologic benefits associated to the plan. 
     */
    public static final IRI physiologicalBenefits;

    /**
     * Player type required—for example, Flash or Silverlight. 
     */
    public static final IRI playerType;

    /**
     * A polygon is the area enclosed by a point-to-point path for which the starting and ending points are the same. A polygon is expressed as a series of four or more spacedelimited points where the first and final points are identical. 
     */
    public static final IRI polygon;

    /**
     * Any characteristics of the population used in the study, e.g. 'males under 65'. 
     */
    public static final IRI population;

    /**
     * A possible unexpected and unfavorable evolution of a medical condition. Complications may include worsening of the signs or symptoms of the disease, extension of the condition to other organ systems, etc. 
     */
    public static final IRI possibleComplication;

    /**
     * A possible treatment to address this condition, sign or symptom. 
     */
    public static final IRI possibleTreatment;

    /**
     * The post offce box number for PO box addresses. 
     */
    public static final IRI postOfficeBoxNumber;

    /**
     * A description of the postoperative procedures, care, and/or followups for this device. 
     */
    public static final IRI postOp;

    /**
     * The postal code. For example, 94043. 
     */
    public static final IRI postalCode;

    /**
     * A description of the workup, testing, and other preparations required before implanting this device. 
     */
    public static final IRI preOp;

    /**
     * A pointer from a previous, often discontinued variant of the product to its newer variant. 
     */
    public static final IRI predecessorOf;

    /**
     * Pregnancy category of this drug. 
     */
    public static final IRI pregnancyCategory;

    /**
     * Any precaution, guidance, contraindication, etc. related to this drug's use during pregnancy. 
     */
    public static final IRI pregnancyWarning;

    /**
     * The length of time it takes to prepare the recipe, in ISO 8601 duration format. 
     */
    public static final IRI prepTime;

    /**
     * Typical preparation that a patient must undergo before having the procedure performed. 
     */
    public static final IRI preparation;

    /**
     * Link to prescribing information for the drug. 
     */
    public static final IRI prescribingInfo;

    /**
     * Indicates whether this drug is available by prescription or over-the-counter. 
     */
    public static final IRI prescriptionStatus;

    /**
     * The offer price of a product, or of a price component when attached to PriceSpecification and its subtypes. 
     */
    public static final IRI price;

    /**
     * The currency (in 3-letter ISO 4217 format) of the offer price or a price component, when attached to PriceSpecification and its subtypes. 
     */
    public static final IRI priceCurrency;

    /**
     * The price range of the business, for example $$$. 
     */
    public static final IRI priceRange;

    /**
     * One or more detailed price specifications, indicating the unit price and delivery or payment charges. 
     */
    public static final IRI priceSpecification;

    /**
     * A short text or acronym indicating multiple price specifications for the same offer, e.g. SRP for the suggested retail price or INVOICE for the invoice price, mostly used in the car industry. 
     */
    public static final IRI priceType;

    /**
     * The date after which the price is no longer available. 
     */
    public static final IRI priceValidUntil;

    /**
     * Indicates the main image on the page 
     */
    public static final IRI primaryImageOfPage;

    /**
     * A preventative therapy used to prevent an initial occurrence of the medical condition, such as vaccination. 
     */
    public static final IRI primaryPrevention;

    /**
     * The number of the column in which the NewsArticle appears in the print edition. 
     */
    public static final IRI printColumn;

    /**
     * The edition of the print product in which the NewsArticle appears. 
     */
    public static final IRI printEdition;

    /**
     * If this NewsArticle appears in print, this field indicates the name of the page on which the article is found. Please note that this field is intended for the exact page name (e.g. A5, B18). 
     */
    public static final IRI printPage;

    /**
     * If this NewsArticle appears in print, this field indicates the print section in which the article appeared. 
     */
    public static final IRI printSection;

    /**
     * A description of the procedure involved in setting up, using, and/or installing the device. 
     */
    public static final IRI procedure;

    /**
     * The type of procedure, for example Surgical, Noninvasive, or Percutaneous. 
     */
    public static final IRI procedureType;

    /**
     * Processor architecture required to run the application (e.g. IA64). 
     */
    public static final IRI processorRequirements;

    /**
     * The producer of the movie, TV series, season, or episode, or video. 
     */
    public static final IRI producer;

    /**
     * The product identifier, such as ISBN. For example: <meta itemprop='productID' content='isbn:123-456-789'/>. 
     */
    public static final IRI productID;

    /**
     * The production company or studio that made the movie, TV series, season, or episode, or video. 
     */
    public static final IRI productionCompany;

    /**
     * Proprietary name given to the diet plan, typically by its originator or creator. 
     */
    public static final IRI proprietaryName;

    /**
     * The number of grams of protein. 
     */
    public static final IRI proteinContent;

    /**
     * Specifies the Person or Organization that distributed the CreativeWork. 
     */
    public static final IRI provider;

    /**
     * The type of the medical article, taken from the US NLM MeSH publication type catalog. 
     */
    public static final IRI publicationType;

    /**
     * The publisher of the creative work. 
     */
    public static final IRI publisher;

    /**
     * Link to page describing the editorial principles of the organization primarily responsible for the creation of the CreativeWork. 
     */
    public static final IRI publishingPrinciples;

    /**
     * The purpose or purposes of this device, for example whether it is intended for diagnostic or therapeutic use. 
     */
    public static final IRI purpose;

    /**
     * Specific qualifications required for this role. 
     */
    public static final IRI qualifications;

    /**
     * The count of total number of ratings. 
     */
    public static final IRI ratingCount;

    /**
     * The rating for the content. 
     */
    public static final IRI ratingValue;

    /**
     * The category of the recipe—for example, appetizer, entree, etc. 
     */
    public static final IRI recipeCategory;

    /**
     * The cuisine of the recipe (for example, French or Ethopian). 
     */
    public static final IRI recipeCuisine;

    /**
     * The steps to make the dish. 
     */
    public static final IRI recipeInstructions;

    /**
     * The quantity produced by the recipe (for example, number of people served, number of servings, etc). 
     */
    public static final IRI recipeYield;

    /**
     * If applicable, the organization that officially recognizes this entity as part of its endorsed system of medicine. 
     */
    public static final IRI recognizingAuthority;

    /**
     * Strength of the guideline's recommendation (e.g. 'class I'). 
     */
    public static final IRI recommendationStrength;

    /**
     * Recommended intake of this supplement for a given population as defined by a specific recommending authority. 
     */
    public static final IRI recommendedIntake;

    /**
     * The anatomical or organ system drained by this vessel; generally refers to a specific part of an organ. 
     */
    public static final IRI regionDrained;

    /**
     * The regions where the media is allowed. If not specified, then it's assumed to be allowed everywhere. Specify the countries in ISO 3166 format. 
     */
    public static final IRI regionsAllowed;

    /**
     * Anatomical systems or structures that relate to the superficial anatomy. 
     */
    public static final IRI relatedAnatomy;

    /**
     * A medical condition associated with this anatomy. 
     */
    public static final IRI relatedCondition;

    /**
     * Any other drug related to this one, for example commonly-prescribed alternatives. 
     */
    public static final IRI relatedDrug;

    /**
     * A link related to this web page, for example to other related web pages. 
     */
    public static final IRI relatedLink;

    /**
     * Related anatomical structure(s) that are not part of the system but relate or connect to it, such as vascular bundles associated with an organ system. 
     */
    public static final IRI relatedStructure;

    /**
     * A medical therapy related to this anatomy. 
     */
    public static final IRI relatedTherapy;

    /**
     * The most generic familial relation. 
     */
    public static final IRI relatedTo;

    /**
     * The release date of a product or product model. This can be used to distinguish the exact variant of a product. 
     */
    public static final IRI releaseDate;

    /**
     * Description of what changed in this version. 
     */
    public static final IRI releaseNotes;

    /**
     * If applicable, a medical specialty in which this entity is relevant. 
     */
    public static final IRI relevantSpecialty;

    /**
     * Number of times one should repeat the activity. 
     */
    public static final IRI repetitions;

    /**
     * The URL at which a reply may be posted to the specified UserComment. 
     */
    public static final IRI replyToUrl;

    /**
     * Indicates whether this image is representative of the content of the page. 
     */
    public static final IRI representativeOfPage;

    /**
     * Component dependency requirements for application. This includes runtime environments and shared libraries that are not included in the application distribution package, but required to run the application (Examples: DirectX, Java or .NET runtime). 
     */
    public static final IRI requirements;

    /**
     * Indicates if use of the media require a subscription  (either paid or free). Allowed values are true or false (note that an earlier version had 'yes', 'no'). 
     */
    public static final IRI requiresSubscription;

    /**
     * Responsibilities associated with this role. 
     */
    public static final IRI responsibilities;

    /**
     * How often one should break from the activity. 
     */
    public static final IRI restPeriods;

    /**
     * A review of the item. 
     */
    public static final IRI review;

    /**
     * The actual body of the review 
     */
    public static final IRI reviewBody;

    /**
     * The count of total number of reviews. 
     */
    public static final IRI reviewCount;

    /**
     * The rating given in this review. Note that reviews can themselves be rated. The reviewRating applies to rating given by the review. The aggregateRating property applies to the review itself, as a creative work. 
     */
    public static final IRI reviewRating;

    /**
     * People or organizations that have reviewed the content on this web page for accuracy and/or completeness. 
     */
    public static final IRI reviewedBy;

    /**
     * Review of the item (legacy spelling; see singular form, review). 
     */
    public static final IRI reviews;

    /**
     * A modifiable or non-modifiable factor that increases the risk of a patient contracting this condition, e.g. age,  coexisting condition. 
     */
    public static final IRI riskFactor;

    /**
     * Specific physiologic risks associated to the plan. 
     */
    public static final IRI risks;

    /**
     * The vasculature the lymphatic structure runs, or efferents, to. 
     */
    public static final IRI runsTo;

    /**
     * Any potential safety concern associated with the supplement. May include interactions with other drugs and foods, pregnancy, breastfeeding, known adverse reactions, and documented efficacy of the supplement. 
     */
    public static final IRI safetyConsideration;

    /**
     * The currency (coded using ISO 4217, http://en.wikipedia.org/wiki/ISO_4217 used for the main salary information in this job posting. 
     */
    public static final IRI salaryCurrency;

    /**
     * The number of grams of saturated fat. 
     */
    public static final IRI saturatedFatContent;

    /**
     * A link to a screenshot image of the app. 
     */
    public static final IRI screenshot;

    /**
     * A season of a TV series. 
     */
    public static final IRI season;

    /**
     * The season number. 
     */
    public static final IRI seasonNumber;

    /**
     * The seasons of the TV series (legacy spelling; see singular form, season). 
     */
    public static final IRI seasons;

    /**
     * A preventative therapy used to prevent reoccurrence of the medical condition after an initial episode of the condition. 
     */
    public static final IRI secondaryPrevention;

    /**
     * A pointer to products or services sought by the organization or person (demand). 
     */
    public static final IRI seeks;

    /**
     * The seller. 
     */
    public static final IRI seller;

    /**
     * The neurological pathway extension that inputs and sends information to the brain or spinal cord. 
     */
    public static final IRI sensoryUnit;

    /**
     * The serial number or any alphanumeric identifier of a particular product. When attached to an offer, it is a shortcut for the serial number of the product included in the offer. 
     */
    public static final IRI serialNumber;

    /**
     * A possible serious complication and/or serious side effect of this therapy. Serious adverse outcomes include those that are life-threatening; result in death, disability, or permanent damage; require hospitalization or prolong existing hospitalization; cause congenital anomalies or birth defects; or jeopardize the patient and may require medical or surgical intervention to prevent one of the outcomes in this definition. 
     */
    public static final IRI seriousAdverseOutcome;

    /**
     * The cuisine of the restaurant. 
     */
    public static final IRI servesCuisine;

    /**
     * The serving size, in terms of the number of volume or mass 
     */
    public static final IRI servingSize;

    /**
     * A sibling of the person. 
     */
    public static final IRI sibling;

    /**
     * A sibling of the person (legacy spelling; see singular form, sibling). 
     */
    public static final IRI siblings;

    /**
     * A sign detected by the test. 
     */
    public static final IRI signDetected;

    /**
     * A sign or symptom of this condition. Signs are objective or physically observable manifestations of the medical condition while symptoms are the subjective experienceof the medical condition. 
     */
    public static final IRI signOrSymptom;

    /**
     * The significance associated with the superficial anatomy; as an example, how characteristics of the superficial anatomy can suggest underlying medical conditions or courses of treatment. 
     */
    public static final IRI significance;

    /**
     * One of the more significant URLs on the page. Typically, these are the non-navigation links that are clicked on the most. 
     */
    public static final IRI significantLink;

    /**
     * The most significant URLs on the page. Typically, these are the non-navigation links that are clicked on the most (legacy spelling; see singular form, significantLink). 
     */
    public static final IRI significantLinks;

    /**
     * Skills required to fulfill this role. 
     */
    public static final IRI skills;

    /**
     * The Stock Keeping Unit (SKU), i.e. a merchant-specific identifier for a product or service, or the product to which the offer refers. 
     */
    public static final IRI sku;

    /**
     * The number of milligrams of sodium. 
     */
    public static final IRI sodiumContent;

    /**
     * Version of the software instance. 
     */
    public static final IRI softwareVersion;

    /**
     * The anatomical or organ system that the artery originates from. 
     */
    public static final IRI source;

    /**
     * The Organization on whose behalf the creator was working. 
     */
    public static final IRI sourceOrganization;

    /**
     * The neurological pathway that originates the neurons. 
     */
    public static final IRI sourcedFrom;

    /**
     * Any special commitments associated with this job posting. Valid entries include VeteranCommit, MilitarySpouseCommit, etc. 
     */
    public static final IRI specialCommitments;

    /**
     * One of the domain specialities to which this web page's content applies. 
     */
    public static final IRI specialty;

    /**
     * Sponsor of the study. 
     */
    public static final IRI sponsor;

    /**
     * The person's spouse. 
     */
    public static final IRI spouse;

    /**
     * The stage of the condition, if applicable. 
     */
    public static final IRI stage;

    /**
     * The stage represented as a number, e.g. 3. 
     */
    public static final IRI stageAsNumber;

    /**
     * The start date and time of the event (in ISO 8601 date format). 
     */
    public static final IRI startDate;

    /**
     * The status of the study (enumerated). 
     */
    public static final IRI status;

    /**
     * Storage requirements (free space required). 
     */
    public static final IRI storageRequirements;

    /**
     * The street address. For example, 1600 Amphitheatre Pkwy. 
     */
    public static final IRI streetAddress;

    /**
     * The units of an active ingredient's strength, e.g. mg. 
     */
    public static final IRI strengthUnit;

    /**
     * The value of an active ingredient's strength, e.g. 325. 
     */
    public static final IRI strengthValue;

    /**
     * The name given to how bone physically connects to each other. 
     */
    public static final IRI structuralClass;

    /**
     * A medical study or trial related to this entity. 
     */
    public static final IRI study;

    /**
     * Specifics about the observational study design (enumerated). 
     */
    public static final IRI studyDesign;

    /**
     * The location in which the study is taking/took place. 
     */
    public static final IRI studyLocation;

    /**
     * A subject of the study, i.e. one of the medical conditions, therapies, devices, drugs, etc. investigated by the study. 
     */
    public static final IRI studySubject;

    /**
     * An Event that is part of this event. For example, a conference event includes many presentations, each are a subEvent of the conference. 
     */
    public static final IRI subEvent;

    /**
     * Events that are a part of this event. For example, a conference event includes many presentations, each are subEvents of the conference (legacy spelling; see singular form, subEvent). 
     */
    public static final IRI subEvents;

    /**
     * The substage, e.g. 'a' for Stage IIIa. 
     */
    public static final IRI subStageSuffix;

    /**
     * Component (sub-)structure(s) that comprise this anatomical structure. 
     */
    public static final IRI subStructure;

    /**
     * A component test of the panel. 
     */
    public static final IRI subTest;

    /**
     * A more specific type of the condition, where applicable, for example 'Type 1 Diabetes', 'Type 2 Diabetes', or 'Gestational Diabetes' for Diabetes. 
     */
    public static final IRI subtype;

    /**
     * A pointer from a newer variant of a product  to its previous, often discontinued predecessor. 
     */
    public static final IRI successorOf;

    /**
     * The number of grams of sugar. 
     */
    public static final IRI sugarContent;

    /**
     * An event that this event is a part of. For example, a collection of individual music performances might each have a music festival as their superEvent. 
     */
    public static final IRI superEvent;

    /**
     * The area to which the artery supplies blood to. 
     */
    public static final IRI supplyTo;

    /**
     * Characteristics of the population for which this is intended, or which typically uses it, e.g. 'adults'. 
     */
    public static final IRI targetPopulation;

    /**
     * The Tax / Fiscal ID of the organization or person, e.g. the TIN in the US or the CIF/NIF in Spain. 
     */
    public static final IRI taxID;

    /**
     * The telephone number. 
     */
    public static final IRI telephone;

    /**
     * The textual content of this CreativeWork. 
     */
    public static final IRI text;

    /**
     * Thumbnail image for an image or video. 
     */
    public static final IRI thumbnail;

    /**
     * A thumbnail image relevant to the Thing. 
     */
    public static final IRI thumbnailUrl;

    /**
     * The exchange traded instrument associated with a Corporation object. The tickerSymbol is expressed as an exchange and an instrument name separated by a space character. For the exchange component of the tickerSymbol attribute, we reccommend using the controlled vocaulary of Market Identifier Codes (MIC) specified in ISO15022. 
     */
    public static final IRI tickerSymbol;

    /**
     * The type of tissue sample required for the test. 
     */
    public static final IRI tissueSample;

    /**
     * The title of the job. 
     */
    public static final IRI title;

    /**
     * The total time it takes to prepare and cook the recipe, in ISO 8601 duration format. 
     */
    public static final IRI totalTime;

    /**
     * A music recording (track)—usually a single song. 
     */
    public static final IRI track;

    /**
     * A music recording (track)—usually a single song (legacy spelling; see singular form, track). 
     */
    public static final IRI tracks;

    /**
     * The trailer of the movie or TV series, season, or episode. 
     */
    public static final IRI trailer;

    /**
     * The number of grams of trans fat. 
     */
    public static final IRI transFatContent;

    /**
     * If this MediaObject is an AudioObject or VideoObject, the transcript of that object. 
     */
    public static final IRI transcript;

    /**
     * How the disease spreads, either as a route or vector, for example 'direct contact', 'Aedes aegypti', etc. 
     */
    public static final IRI transmissionMethod;

    /**
     * Specifics about the trial design (enumerated). 
     */
    public static final IRI trialDesign;

    /**
     * The anatomical or organ system that the vein flows into; a larger structure that the vein connects to. 
     */
    public static final IRI tributary;

    /**
     * The product that this structured value is referring to. 
     */
    public static final IRI typeOfGood;

    /**
     * A medical test typically performed given this condition. 
     */
    public static final IRI typicalTest;

    /**
     * The unit of measurement given using the UN/CEFACT Common Code (3 characters). 
     */
    public static final IRI unitCode;

    /**
     * The number of grams of unsaturated fat. 
     */
    public static final IRI unsaturatedFatContent;

    /**
     * Date when this media object was uploaded to this site. 
     */
    public static final IRI uploadDate;

    /**
     * URL of the item. 
     */
    public static final IRI url;

    /**
     * A condition the test is used to diagnose. 
     */
    public static final IRI usedToDiagnose;

    /**
     * Device used to perform the test. 
     */
    public static final IRI usesDevice;

    /**
     * The beginning of the validity of offer, price specification, or opening hours data. 
     */
    public static final IRI validFrom;

    /**
     * The end of the validity of offer, price specification, or opening hours data. 
     */
    public static final IRI validThrough;

    /**
     * The value of the product characteristic. 
     */
    public static final IRI value;

    /**
     * Specifies whether the applicable value-added tax (VAT) is included in the price specification or not. 
     */
    public static final IRI valueAddedTaxIncluded;

    /**
     * A pointer to a secondary value that provides additional information on the original value, e.g. a reference temperature. 
     */
    public static final IRI valueReference;

    /**
     * The Value-added Tax ID of the organisation or person. 
     */
    public static final IRI vatID;

    /**
     * The version of the CreativeWork embodied by a specified resource. 
     */
    public static final IRI version;

    /**
     * An embedded video object. 
     */
    public static final IRI video;

    /**
     * The frame size of the video. 
     */
    public static final IRI videoFrameSize;

    /**
     * The quality of the video. 
     */
    public static final IRI videoQuality;

    /**
     * Any FDA or other warnings about the drug (text or URL). 
     */
    public static final IRI warning;

    /**
     * The warranty promise(s) included in the offer. 
     */
    public static final IRI warranty;

    /**
     * The scope of the warranty promise. 
     */
    public static final IRI warrantyScope;

    /**
     * The weight of the product. 
     */
    public static final IRI weight;

    /**
     * The width of the item. 
     */
    public static final IRI width;

    /**
     * The number of words in the text of the Article. 
     */
    public static final IRI wordCount;

    /**
     * The typical working hours for this job (e.g. 1st shift, night shift, 8am-5pm). 
     */
    public static final IRI workHours;

    /**
     * A contact location for a person's place of work. 
     */
    public static final IRI workLocation;

    /**
     * Quantitative measure of the physiologic output of the exercise; also referred to as energy expenditure. 
     */
    public static final IRI workload;

    /**
     * Organizations that the person works for. 
     */
    public static final IRI worksFor;

    /**
     * The lowest value allowed in this rating system. If worstRating is omitted, 1 is assumed. 
     */
    public static final IRI worstRating;


    static{
        ValueFactory factory = SimpleValueFactory.getInstance();
        AboutPage = factory.createIRI(SCHEMA.NAMESPACE, "AboutPage");
        AccountingService = factory.createIRI(SCHEMA.NAMESPACE, "AccountingService");
        AdministrativeArea = factory.createIRI(SCHEMA.NAMESPACE, "AdministrativeArea");
        AdultEntertainment = factory.createIRI(SCHEMA.NAMESPACE, "AdultEntertainment");
        AggregateOffer = factory.createIRI(SCHEMA.NAMESPACE, "AggregateOffer");
        AggregateRating = factory.createIRI(SCHEMA.NAMESPACE, "AggregateRating");
        Airport = factory.createIRI(SCHEMA.NAMESPACE, "Airport");
        AmusementPark = factory.createIRI(SCHEMA.NAMESPACE, "AmusementPark");
        AnatomicalStructure = factory.createIRI(SCHEMA.NAMESPACE, "AnatomicalStructure");
        AnatomicalSystem = factory.createIRI(SCHEMA.NAMESPACE, "AnatomicalSystem");
        AnimalShelter = factory.createIRI(SCHEMA.NAMESPACE, "AnimalShelter");
        ApartmentComplex = factory.createIRI(SCHEMA.NAMESPACE, "ApartmentComplex");
        ApprovedIndication = factory.createIRI(SCHEMA.NAMESPACE, "ApprovedIndication");
        Aquarium = factory.createIRI(SCHEMA.NAMESPACE, "Aquarium");
        ArtGallery = factory.createIRI(SCHEMA.NAMESPACE, "ArtGallery");
        Artery = factory.createIRI(SCHEMA.NAMESPACE, "Artery");
        Article = factory.createIRI(SCHEMA.NAMESPACE, "Article");
        Attorney = factory.createIRI(SCHEMA.NAMESPACE, "Attorney");
        Audience = factory.createIRI(SCHEMA.NAMESPACE, "Audience");
        AudioObject = factory.createIRI(SCHEMA.NAMESPACE, "AudioObject");
        AutoBodyShop = factory.createIRI(SCHEMA.NAMESPACE, "AutoBodyShop");
        AutoDealer = factory.createIRI(SCHEMA.NAMESPACE, "AutoDealer");
        AutoPartsStore = factory.createIRI(SCHEMA.NAMESPACE, "AutoPartsStore");
        AutoRental = factory.createIRI(SCHEMA.NAMESPACE, "AutoRental");
        AutoRepair = factory.createIRI(SCHEMA.NAMESPACE, "AutoRepair");
        AutoWash = factory.createIRI(SCHEMA.NAMESPACE, "AutoWash");
        AutomatedTeller = factory.createIRI(SCHEMA.NAMESPACE, "AutomatedTeller");
        AutomotiveBusiness = factory.createIRI(SCHEMA.NAMESPACE, "AutomotiveBusiness");
        Bakery = factory.createIRI(SCHEMA.NAMESPACE, "Bakery");
        BankOrCreditUnion = factory.createIRI(SCHEMA.NAMESPACE, "BankOrCreditUnion");
        BarOrPub = factory.createIRI(SCHEMA.NAMESPACE, "BarOrPub");
        Beach = factory.createIRI(SCHEMA.NAMESPACE, "Beach");
        BeautySalon = factory.createIRI(SCHEMA.NAMESPACE, "BeautySalon");
        BedAndBreakfast = factory.createIRI(SCHEMA.NAMESPACE, "BedAndBreakfast");
        BikeStore = factory.createIRI(SCHEMA.NAMESPACE, "BikeStore");
        Blog = factory.createIRI(SCHEMA.NAMESPACE, "Blog");
        BlogPosting = factory.createIRI(SCHEMA.NAMESPACE, "BlogPosting");
        BloodTest = factory.createIRI(SCHEMA.NAMESPACE, "BloodTest");
        BodyOfWater = factory.createIRI(SCHEMA.NAMESPACE, "BodyOfWater");
        Bone = factory.createIRI(SCHEMA.NAMESPACE, "Bone");
        Book = factory.createIRI(SCHEMA.NAMESPACE, "Book");
        BookFormatType = factory.createIRI(SCHEMA.NAMESPACE, "BookFormatType");
        BookStore = factory.createIRI(SCHEMA.NAMESPACE, "BookStore");
        BowlingAlley = factory.createIRI(SCHEMA.NAMESPACE, "BowlingAlley");
        BrainStructure = factory.createIRI(SCHEMA.NAMESPACE, "BrainStructure");
        Brand = factory.createIRI(SCHEMA.NAMESPACE, "Brand");
        Brewery = factory.createIRI(SCHEMA.NAMESPACE, "Brewery");
        BuddhistTemple = factory.createIRI(SCHEMA.NAMESPACE, "BuddhistTemple");
        BusStation = factory.createIRI(SCHEMA.NAMESPACE, "BusStation");
        BusStop = factory.createIRI(SCHEMA.NAMESPACE, "BusStop");
        BusinessEntityType = factory.createIRI(SCHEMA.NAMESPACE, "BusinessEntityType");
        BusinessEvent = factory.createIRI(SCHEMA.NAMESPACE, "BusinessEvent");
        BusinessFunction = factory.createIRI(SCHEMA.NAMESPACE, "BusinessFunction");
        CafeOrCoffeeShop = factory.createIRI(SCHEMA.NAMESPACE, "CafeOrCoffeeShop");
        Campground = factory.createIRI(SCHEMA.NAMESPACE, "Campground");
        Canal = factory.createIRI(SCHEMA.NAMESPACE, "Canal");
        Casino = factory.createIRI(SCHEMA.NAMESPACE, "Casino");
        CatholicChurch = factory.createIRI(SCHEMA.NAMESPACE, "CatholicChurch");
        Cemetery = factory.createIRI(SCHEMA.NAMESPACE, "Cemetery");
        CheckoutPage = factory.createIRI(SCHEMA.NAMESPACE, "CheckoutPage");
        ChildCare = factory.createIRI(SCHEMA.NAMESPACE, "ChildCare");
        ChildrensEvent = factory.createIRI(SCHEMA.NAMESPACE, "ChildrensEvent");
        Church = factory.createIRI(SCHEMA.NAMESPACE, "Church");
        City = factory.createIRI(SCHEMA.NAMESPACE, "City");
        CityHall = factory.createIRI(SCHEMA.NAMESPACE, "CityHall");
        CivicStructure = factory.createIRI(SCHEMA.NAMESPACE, "CivicStructure");
        ClothingStore = factory.createIRI(SCHEMA.NAMESPACE, "ClothingStore");
        CollectionPage = factory.createIRI(SCHEMA.NAMESPACE, "CollectionPage");
        CollegeOrUniversity = factory.createIRI(SCHEMA.NAMESPACE, "CollegeOrUniversity");
        ComedyClub = factory.createIRI(SCHEMA.NAMESPACE, "ComedyClub");
        ComedyEvent = factory.createIRI(SCHEMA.NAMESPACE, "ComedyEvent");
        Comment = factory.createIRI(SCHEMA.NAMESPACE, "Comment");
        ComputerStore = factory.createIRI(SCHEMA.NAMESPACE, "ComputerStore");
        ContactPage = factory.createIRI(SCHEMA.NAMESPACE, "ContactPage");
        ContactPoint = factory.createIRI(SCHEMA.NAMESPACE, "ContactPoint");
        Continent = factory.createIRI(SCHEMA.NAMESPACE, "Continent");
        ConvenienceStore = factory.createIRI(SCHEMA.NAMESPACE, "ConvenienceStore");
        Corporation = factory.createIRI(SCHEMA.NAMESPACE, "Corporation");
        Country = factory.createIRI(SCHEMA.NAMESPACE, "Country");
        Courthouse = factory.createIRI(SCHEMA.NAMESPACE, "Courthouse");
        CreativeWork = factory.createIRI(SCHEMA.NAMESPACE, "CreativeWork");
        CreditCard = factory.createIRI(SCHEMA.NAMESPACE, "CreditCard");
        Crematorium = factory.createIRI(SCHEMA.NAMESPACE, "Crematorium");
        DDxElement = factory.createIRI(SCHEMA.NAMESPACE, "DDxElement");
        DanceEvent = factory.createIRI(SCHEMA.NAMESPACE, "DanceEvent");
        DanceGroup = factory.createIRI(SCHEMA.NAMESPACE, "DanceGroup");
        DayOfWeek = factory.createIRI(SCHEMA.NAMESPACE, "DayOfWeek");
        DaySpa = factory.createIRI(SCHEMA.NAMESPACE, "DaySpa");
        DefenceEstablishment = factory.createIRI(SCHEMA.NAMESPACE, "DefenceEstablishment");
        DeliveryChargeSpecification = factory.createIRI(SCHEMA.NAMESPACE, "DeliveryChargeSpecification");
        DeliveryMethod = factory.createIRI(SCHEMA.NAMESPACE, "DeliveryMethod");
        Demand = factory.createIRI(SCHEMA.NAMESPACE, "Demand");
        Dentist = factory.createIRI(SCHEMA.NAMESPACE, "Dentist");
        DepartmentStore = factory.createIRI(SCHEMA.NAMESPACE, "DepartmentStore");
        DiagnosticLab = factory.createIRI(SCHEMA.NAMESPACE, "DiagnosticLab");
        DiagnosticProcedure = factory.createIRI(SCHEMA.NAMESPACE, "DiagnosticProcedure");
        Diet = factory.createIRI(SCHEMA.NAMESPACE, "Diet");
        DietarySupplement = factory.createIRI(SCHEMA.NAMESPACE, "DietarySupplement");
        Distance = factory.createIRI(SCHEMA.NAMESPACE, "Distance");
        DoseSchedule = factory.createIRI(SCHEMA.NAMESPACE, "DoseSchedule");
        Drug = factory.createIRI(SCHEMA.NAMESPACE, "Drug");
        DrugClass = factory.createIRI(SCHEMA.NAMESPACE, "DrugClass");
        DrugCost = factory.createIRI(SCHEMA.NAMESPACE, "DrugCost");
        DrugCostCategory = factory.createIRI(SCHEMA.NAMESPACE, "DrugCostCategory");
        DrugLegalStatus = factory.createIRI(SCHEMA.NAMESPACE, "DrugLegalStatus");
        DrugPregnancyCategory = factory.createIRI(SCHEMA.NAMESPACE, "DrugPregnancyCategory");
        DrugPrescriptionStatus = factory.createIRI(SCHEMA.NAMESPACE, "DrugPrescriptionStatus");
        DrugStrength = factory.createIRI(SCHEMA.NAMESPACE, "DrugStrength");
        DryCleaningOrLaundry = factory.createIRI(SCHEMA.NAMESPACE, "DryCleaningOrLaundry");
        Duration = factory.createIRI(SCHEMA.NAMESPACE, "Duration");
        EducationEvent = factory.createIRI(SCHEMA.NAMESPACE, "EducationEvent");
        EducationalOrganization = factory.createIRI(SCHEMA.NAMESPACE, "EducationalOrganization");
        Electrician = factory.createIRI(SCHEMA.NAMESPACE, "Electrician");
        ElectronicsStore = factory.createIRI(SCHEMA.NAMESPACE, "ElectronicsStore");
        ElementarySchool = factory.createIRI(SCHEMA.NAMESPACE, "ElementarySchool");
        Embassy = factory.createIRI(SCHEMA.NAMESPACE, "Embassy");
        EmergencyService = factory.createIRI(SCHEMA.NAMESPACE, "EmergencyService");
        EmploymentAgency = factory.createIRI(SCHEMA.NAMESPACE, "EmploymentAgency");
        Energy = factory.createIRI(SCHEMA.NAMESPACE, "Energy");
        EntertainmentBusiness = factory.createIRI(SCHEMA.NAMESPACE, "EntertainmentBusiness");
        Enumeration = factory.createIRI(SCHEMA.NAMESPACE, "Enumeration");
        Event = factory.createIRI(SCHEMA.NAMESPACE, "Event");
        EventVenue = factory.createIRI(SCHEMA.NAMESPACE, "EventVenue");
        ExerciseGym = factory.createIRI(SCHEMA.NAMESPACE, "ExerciseGym");
        ExercisePlan = factory.createIRI(SCHEMA.NAMESPACE, "ExercisePlan");
        FastFoodRestaurant = factory.createIRI(SCHEMA.NAMESPACE, "FastFoodRestaurant");
        Festival = factory.createIRI(SCHEMA.NAMESPACE, "Festival");
        FinancialService = factory.createIRI(SCHEMA.NAMESPACE, "FinancialService");
        FireStation = factory.createIRI(SCHEMA.NAMESPACE, "FireStation");
        Florist = factory.createIRI(SCHEMA.NAMESPACE, "Florist");
        FoodEstablishment = factory.createIRI(SCHEMA.NAMESPACE, "FoodEstablishment");
        FoodEvent = factory.createIRI(SCHEMA.NAMESPACE, "FoodEvent");
        FurnitureStore = factory.createIRI(SCHEMA.NAMESPACE, "FurnitureStore");
        GardenStore = factory.createIRI(SCHEMA.NAMESPACE, "GardenStore");
        GasStation = factory.createIRI(SCHEMA.NAMESPACE, "GasStation");
        GatedResidenceCommunity = factory.createIRI(SCHEMA.NAMESPACE, "GatedResidenceCommunity");
        GeneralContractor = factory.createIRI(SCHEMA.NAMESPACE, "GeneralContractor");
        GeoCoordinates = factory.createIRI(SCHEMA.NAMESPACE, "GeoCoordinates");
        GeoShape = factory.createIRI(SCHEMA.NAMESPACE, "GeoShape");
        GolfCourse = factory.createIRI(SCHEMA.NAMESPACE, "GolfCourse");
        GovernmentBuilding = factory.createIRI(SCHEMA.NAMESPACE, "GovernmentBuilding");
        GovernmentOffice = factory.createIRI(SCHEMA.NAMESPACE, "GovernmentOffice");
        GovernmentOrganization = factory.createIRI(SCHEMA.NAMESPACE, "GovernmentOrganization");
        GroceryStore = factory.createIRI(SCHEMA.NAMESPACE, "GroceryStore");
        HVACBusiness = factory.createIRI(SCHEMA.NAMESPACE, "HVACBusiness");
        HairSalon = factory.createIRI(SCHEMA.NAMESPACE, "HairSalon");
        HardwareStore = factory.createIRI(SCHEMA.NAMESPACE, "HardwareStore");
        HealthAndBeautyBusiness = factory.createIRI(SCHEMA.NAMESPACE, "HealthAndBeautyBusiness");
        HealthClub = factory.createIRI(SCHEMA.NAMESPACE, "HealthClub");
        HighSchool = factory.createIRI(SCHEMA.NAMESPACE, "HighSchool");
        HinduTemple = factory.createIRI(SCHEMA.NAMESPACE, "HinduTemple");
        HobbyShop = factory.createIRI(SCHEMA.NAMESPACE, "HobbyShop");
        HomeAndConstructionBusiness = factory.createIRI(SCHEMA.NAMESPACE, "HomeAndConstructionBusiness");
        HomeGoodsStore = factory.createIRI(SCHEMA.NAMESPACE, "HomeGoodsStore");
        Hospital = factory.createIRI(SCHEMA.NAMESPACE, "Hospital");
        Hostel = factory.createIRI(SCHEMA.NAMESPACE, "Hostel");
        Hotel = factory.createIRI(SCHEMA.NAMESPACE, "Hotel");
        HousePainter = factory.createIRI(SCHEMA.NAMESPACE, "HousePainter");
        IceCreamShop = factory.createIRI(SCHEMA.NAMESPACE, "IceCreamShop");
        ImageGallery = factory.createIRI(SCHEMA.NAMESPACE, "ImageGallery");
        ImageObject = factory.createIRI(SCHEMA.NAMESPACE, "ImageObject");
        ImagingTest = factory.createIRI(SCHEMA.NAMESPACE, "ImagingTest");
        IndividualProduct = factory.createIRI(SCHEMA.NAMESPACE, "IndividualProduct");
        InfectiousAgentClass = factory.createIRI(SCHEMA.NAMESPACE, "InfectiousAgentClass");
        InfectiousDisease = factory.createIRI(SCHEMA.NAMESPACE, "InfectiousDisease");
        InsuranceAgency = factory.createIRI(SCHEMA.NAMESPACE, "InsuranceAgency");
        Intangible = factory.createIRI(SCHEMA.NAMESPACE, "Intangible");
        InternetCafe = factory.createIRI(SCHEMA.NAMESPACE, "InternetCafe");
        ItemAvailability = factory.createIRI(SCHEMA.NAMESPACE, "ItemAvailability");
        ItemList = factory.createIRI(SCHEMA.NAMESPACE, "ItemList");
        ItemPage = factory.createIRI(SCHEMA.NAMESPACE, "ItemPage");
        JewelryStore = factory.createIRI(SCHEMA.NAMESPACE, "JewelryStore");
        JobPosting = factory.createIRI(SCHEMA.NAMESPACE, "JobPosting");
        Joint = factory.createIRI(SCHEMA.NAMESPACE, "Joint");
        LakeBodyOfWater = factory.createIRI(SCHEMA.NAMESPACE, "LakeBodyOfWater");
        Landform = factory.createIRI(SCHEMA.NAMESPACE, "Landform");
        LandmarksOrHistoricalBuildings = factory.createIRI(SCHEMA.NAMESPACE, "LandmarksOrHistoricalBuildings");
        Language = factory.createIRI(SCHEMA.NAMESPACE, "Language");
        LegislativeBuilding = factory.createIRI(SCHEMA.NAMESPACE, "LegislativeBuilding");
        Library = factory.createIRI(SCHEMA.NAMESPACE, "Library");
        LifestyleModification = factory.createIRI(SCHEMA.NAMESPACE, "LifestyleModification");
        Ligament = factory.createIRI(SCHEMA.NAMESPACE, "Ligament");
        LiquorStore = factory.createIRI(SCHEMA.NAMESPACE, "LiquorStore");
        LiteraryEvent = factory.createIRI(SCHEMA.NAMESPACE, "LiteraryEvent");
        LocalBusiness = factory.createIRI(SCHEMA.NAMESPACE, "LocalBusiness");
        Locksmith = factory.createIRI(SCHEMA.NAMESPACE, "Locksmith");
        LodgingBusiness = factory.createIRI(SCHEMA.NAMESPACE, "LodgingBusiness");
        LymphaticVessel = factory.createIRI(SCHEMA.NAMESPACE, "LymphaticVessel");
        Map = factory.createIRI(SCHEMA.NAMESPACE, "Map");
        Mass = factory.createIRI(SCHEMA.NAMESPACE, "Mass");
        MaximumDoseSchedule = factory.createIRI(SCHEMA.NAMESPACE, "MaximumDoseSchedule");
        MediaObject = factory.createIRI(SCHEMA.NAMESPACE, "MediaObject");
        MedicalAudience = factory.createIRI(SCHEMA.NAMESPACE, "MedicalAudience");
        MedicalCause = factory.createIRI(SCHEMA.NAMESPACE, "MedicalCause");
        MedicalClinic = factory.createIRI(SCHEMA.NAMESPACE, "MedicalClinic");
        MedicalCode = factory.createIRI(SCHEMA.NAMESPACE, "MedicalCode");
        MedicalCondition = factory.createIRI(SCHEMA.NAMESPACE, "MedicalCondition");
        MedicalConditionStage = factory.createIRI(SCHEMA.NAMESPACE, "MedicalConditionStage");
        MedicalContraindication = factory.createIRI(SCHEMA.NAMESPACE, "MedicalContraindication");
        MedicalDevice = factory.createIRI(SCHEMA.NAMESPACE, "MedicalDevice");
        MedicalDevicePurpose = factory.createIRI(SCHEMA.NAMESPACE, "MedicalDevicePurpose");
        MedicalEntity = factory.createIRI(SCHEMA.NAMESPACE, "MedicalEntity");
        MedicalEnumeration = factory.createIRI(SCHEMA.NAMESPACE, "MedicalEnumeration");
        MedicalEvidenceLevel = factory.createIRI(SCHEMA.NAMESPACE, "MedicalEvidenceLevel");
        MedicalGuideline = factory.createIRI(SCHEMA.NAMESPACE, "MedicalGuideline");
        MedicalGuidelineContraindication = factory.createIRI(SCHEMA.NAMESPACE, "MedicalGuidelineContraindication");
        MedicalGuidelineRecommendation = factory.createIRI(SCHEMA.NAMESPACE, "MedicalGuidelineRecommendation");
        MedicalImagingTechnique = factory.createIRI(SCHEMA.NAMESPACE, "MedicalImagingTechnique");
        MedicalIndication = factory.createIRI(SCHEMA.NAMESPACE, "MedicalIndication");
        MedicalIntangible = factory.createIRI(SCHEMA.NAMESPACE, "MedicalIntangible");
        MedicalObservationalStudy = factory.createIRI(SCHEMA.NAMESPACE, "MedicalObservationalStudy");
        MedicalObservationalStudyDesign = factory.createIRI(SCHEMA.NAMESPACE, "MedicalObservationalStudyDesign");
        MedicalOrganization = factory.createIRI(SCHEMA.NAMESPACE, "MedicalOrganization");
        MedicalProcedure = factory.createIRI(SCHEMA.NAMESPACE, "MedicalProcedure");
        MedicalProcedureType = factory.createIRI(SCHEMA.NAMESPACE, "MedicalProcedureType");
        MedicalRiskCalculator = factory.createIRI(SCHEMA.NAMESPACE, "MedicalRiskCalculator");
        MedicalRiskEstimator = factory.createIRI(SCHEMA.NAMESPACE, "MedicalRiskEstimator");
        MedicalRiskFactor = factory.createIRI(SCHEMA.NAMESPACE, "MedicalRiskFactor");
        MedicalRiskScore = factory.createIRI(SCHEMA.NAMESPACE, "MedicalRiskScore");
        MedicalScholarlyArticle = factory.createIRI(SCHEMA.NAMESPACE, "MedicalScholarlyArticle");
        MedicalSign = factory.createIRI(SCHEMA.NAMESPACE, "MedicalSign");
        MedicalSignOrSymptom = factory.createIRI(SCHEMA.NAMESPACE, "MedicalSignOrSymptom");
        MedicalSpecialty = factory.createIRI(SCHEMA.NAMESPACE, "MedicalSpecialty");
        MedicalStudy = factory.createIRI(SCHEMA.NAMESPACE, "MedicalStudy");
        MedicalStudyStatus = factory.createIRI(SCHEMA.NAMESPACE, "MedicalStudyStatus");
        MedicalSymptom = factory.createIRI(SCHEMA.NAMESPACE, "MedicalSymptom");
        MedicalTest = factory.createIRI(SCHEMA.NAMESPACE, "MedicalTest");
        MedicalTestPanel = factory.createIRI(SCHEMA.NAMESPACE, "MedicalTestPanel");
        MedicalTherapy = factory.createIRI(SCHEMA.NAMESPACE, "MedicalTherapy");
        MedicalTrial = factory.createIRI(SCHEMA.NAMESPACE, "MedicalTrial");
        MedicalTrialDesign = factory.createIRI(SCHEMA.NAMESPACE, "MedicalTrialDesign");
        MedicalWebPage = factory.createIRI(SCHEMA.NAMESPACE, "MedicalWebPage");
        MedicineSystem = factory.createIRI(SCHEMA.NAMESPACE, "MedicineSystem");
        MensClothingStore = factory.createIRI(SCHEMA.NAMESPACE, "MensClothingStore");
        MiddleSchool = factory.createIRI(SCHEMA.NAMESPACE, "MiddleSchool");
        MobileApplication = factory.createIRI(SCHEMA.NAMESPACE, "MobileApplication");
        MobilePhoneStore = factory.createIRI(SCHEMA.NAMESPACE, "MobilePhoneStore");
        Mosque = factory.createIRI(SCHEMA.NAMESPACE, "Mosque");
        Motel = factory.createIRI(SCHEMA.NAMESPACE, "Motel");
        MotorcycleDealer = factory.createIRI(SCHEMA.NAMESPACE, "MotorcycleDealer");
        MotorcycleRepair = factory.createIRI(SCHEMA.NAMESPACE, "MotorcycleRepair");
        Mountain = factory.createIRI(SCHEMA.NAMESPACE, "Mountain");
        Movie = factory.createIRI(SCHEMA.NAMESPACE, "Movie");
        MovieRentalStore = factory.createIRI(SCHEMA.NAMESPACE, "MovieRentalStore");
        MovieTheater = factory.createIRI(SCHEMA.NAMESPACE, "MovieTheater");
        MovingCompany = factory.createIRI(SCHEMA.NAMESPACE, "MovingCompany");
        Muscle = factory.createIRI(SCHEMA.NAMESPACE, "Muscle");
        Museum = factory.createIRI(SCHEMA.NAMESPACE, "Museum");
        MusicAlbum = factory.createIRI(SCHEMA.NAMESPACE, "MusicAlbum");
        MusicEvent = factory.createIRI(SCHEMA.NAMESPACE, "MusicEvent");
        MusicGroup = factory.createIRI(SCHEMA.NAMESPACE, "MusicGroup");
        MusicPlaylist = factory.createIRI(SCHEMA.NAMESPACE, "MusicPlaylist");
        MusicRecording = factory.createIRI(SCHEMA.NAMESPACE, "MusicRecording");
        MusicStore = factory.createIRI(SCHEMA.NAMESPACE, "MusicStore");
        MusicVenue = factory.createIRI(SCHEMA.NAMESPACE, "MusicVenue");
        MusicVideoObject = factory.createIRI(SCHEMA.NAMESPACE, "MusicVideoObject");
        NGO = factory.createIRI(SCHEMA.NAMESPACE, "NGO");
        NailSalon = factory.createIRI(SCHEMA.NAMESPACE, "NailSalon");
        Nerve = factory.createIRI(SCHEMA.NAMESPACE, "Nerve");
        NewsArticle = factory.createIRI(SCHEMA.NAMESPACE, "NewsArticle");
        NightClub = factory.createIRI(SCHEMA.NAMESPACE, "NightClub");
        Notary = factory.createIRI(SCHEMA.NAMESPACE, "Notary");
        NutritionInformation = factory.createIRI(SCHEMA.NAMESPACE, "NutritionInformation");
        OceanBodyOfWater = factory.createIRI(SCHEMA.NAMESPACE, "OceanBodyOfWater");
        Offer = factory.createIRI(SCHEMA.NAMESPACE, "Offer");
        OfferItemCondition = factory.createIRI(SCHEMA.NAMESPACE, "OfferItemCondition");
        OfficeEquipmentStore = factory.createIRI(SCHEMA.NAMESPACE, "OfficeEquipmentStore");
        OpeningHoursSpecification = factory.createIRI(SCHEMA.NAMESPACE, "OpeningHoursSpecification");
        Optician = factory.createIRI(SCHEMA.NAMESPACE, "Optician");
        Organization = factory.createIRI(SCHEMA.NAMESPACE, "Organization");
        OutletStore = factory.createIRI(SCHEMA.NAMESPACE, "OutletStore");
        OwnershipInfo = factory.createIRI(SCHEMA.NAMESPACE, "OwnershipInfo");
        Painting = factory.createIRI(SCHEMA.NAMESPACE, "Painting");
        PalliativeProcedure = factory.createIRI(SCHEMA.NAMESPACE, "PalliativeProcedure");
        ParcelService = factory.createIRI(SCHEMA.NAMESPACE, "ParcelService");
        Park = factory.createIRI(SCHEMA.NAMESPACE, "Park");
        ParkingFacility = factory.createIRI(SCHEMA.NAMESPACE, "ParkingFacility");
        PathologyTest = factory.createIRI(SCHEMA.NAMESPACE, "PathologyTest");
        PawnShop = factory.createIRI(SCHEMA.NAMESPACE, "PawnShop");
        PaymentChargeSpecification = factory.createIRI(SCHEMA.NAMESPACE, "PaymentChargeSpecification");
        PaymentMethod = factory.createIRI(SCHEMA.NAMESPACE, "PaymentMethod");
        PerformingArtsTheater = factory.createIRI(SCHEMA.NAMESPACE, "PerformingArtsTheater");
        PerformingGroup = factory.createIRI(SCHEMA.NAMESPACE, "PerformingGroup");
        Person = factory.createIRI(SCHEMA.NAMESPACE, "Person");
        PetStore = factory.createIRI(SCHEMA.NAMESPACE, "PetStore");
        Pharmacy = factory.createIRI(SCHEMA.NAMESPACE, "Pharmacy");
        Photograph = factory.createIRI(SCHEMA.NAMESPACE, "Photograph");
        PhysicalActivity = factory.createIRI(SCHEMA.NAMESPACE, "PhysicalActivity");
        PhysicalActivityCategory = factory.createIRI(SCHEMA.NAMESPACE, "PhysicalActivityCategory");
        PhysicalExam = factory.createIRI(SCHEMA.NAMESPACE, "PhysicalExam");
        PhysicalTherapy = factory.createIRI(SCHEMA.NAMESPACE, "PhysicalTherapy");
        Physician = factory.createIRI(SCHEMA.NAMESPACE, "Physician");
        Place = factory.createIRI(SCHEMA.NAMESPACE, "Place");
        PlaceOfWorship = factory.createIRI(SCHEMA.NAMESPACE, "PlaceOfWorship");
        Playground = factory.createIRI(SCHEMA.NAMESPACE, "Playground");
        Plumber = factory.createIRI(SCHEMA.NAMESPACE, "Plumber");
        PoliceStation = factory.createIRI(SCHEMA.NAMESPACE, "PoliceStation");
        Pond = factory.createIRI(SCHEMA.NAMESPACE, "Pond");
        PostOffice = factory.createIRI(SCHEMA.NAMESPACE, "PostOffice");
        PostalAddress = factory.createIRI(SCHEMA.NAMESPACE, "PostalAddress");
        Preschool = factory.createIRI(SCHEMA.NAMESPACE, "Preschool");
        PreventionIndication = factory.createIRI(SCHEMA.NAMESPACE, "PreventionIndication");
        PriceSpecification = factory.createIRI(SCHEMA.NAMESPACE, "PriceSpecification");
        Product = factory.createIRI(SCHEMA.NAMESPACE, "Product");
        ProductModel = factory.createIRI(SCHEMA.NAMESPACE, "ProductModel");
        ProfessionalService = factory.createIRI(SCHEMA.NAMESPACE, "ProfessionalService");
        ProfilePage = factory.createIRI(SCHEMA.NAMESPACE, "ProfilePage");
        PsychologicalTreatment = factory.createIRI(SCHEMA.NAMESPACE, "PsychologicalTreatment");
        PublicSwimmingPool = factory.createIRI(SCHEMA.NAMESPACE, "PublicSwimmingPool");
        QualitativeValue = factory.createIRI(SCHEMA.NAMESPACE, "QualitativeValue");
        QuantitativeValue = factory.createIRI(SCHEMA.NAMESPACE, "QuantitativeValue");
        Quantity = factory.createIRI(SCHEMA.NAMESPACE, "Quantity");
        RVPark = factory.createIRI(SCHEMA.NAMESPACE, "RVPark");
        RadiationTherapy = factory.createIRI(SCHEMA.NAMESPACE, "RadiationTherapy");
        RadioStation = factory.createIRI(SCHEMA.NAMESPACE, "RadioStation");
        Rating = factory.createIRI(SCHEMA.NAMESPACE, "Rating");
        RealEstateAgent = factory.createIRI(SCHEMA.NAMESPACE, "RealEstateAgent");
        Recipe = factory.createIRI(SCHEMA.NAMESPACE, "Recipe");
        RecommendedDoseSchedule = factory.createIRI(SCHEMA.NAMESPACE, "RecommendedDoseSchedule");
        RecyclingCenter = factory.createIRI(SCHEMA.NAMESPACE, "RecyclingCenter");
        ReportedDoseSchedule = factory.createIRI(SCHEMA.NAMESPACE, "ReportedDoseSchedule");
        Reservoir = factory.createIRI(SCHEMA.NAMESPACE, "Reservoir");
        Residence = factory.createIRI(SCHEMA.NAMESPACE, "Residence");
        Restaurant = factory.createIRI(SCHEMA.NAMESPACE, "Restaurant");
        Review = factory.createIRI(SCHEMA.NAMESPACE, "Review");
        RiverBodyOfWater = factory.createIRI(SCHEMA.NAMESPACE, "RiverBodyOfWater");
        RoofingContractor = factory.createIRI(SCHEMA.NAMESPACE, "RoofingContractor");
        SaleEvent = factory.createIRI(SCHEMA.NAMESPACE, "SaleEvent");
        ScholarlyArticle = factory.createIRI(SCHEMA.NAMESPACE, "ScholarlyArticle");
        School = factory.createIRI(SCHEMA.NAMESPACE, "School");
        Sculpture = factory.createIRI(SCHEMA.NAMESPACE, "Sculpture");
        SeaBodyOfWater = factory.createIRI(SCHEMA.NAMESPACE, "SeaBodyOfWater");
        SearchResultsPage = factory.createIRI(SCHEMA.NAMESPACE, "SearchResultsPage");
        SelfStorage = factory.createIRI(SCHEMA.NAMESPACE, "SelfStorage");
        ShoeStore = factory.createIRI(SCHEMA.NAMESPACE, "ShoeStore");
        ShoppingCenter = factory.createIRI(SCHEMA.NAMESPACE, "ShoppingCenter");
        SingleFamilyResidence = factory.createIRI(SCHEMA.NAMESPACE, "SingleFamilyResidence");
        SiteNavigationElement = factory.createIRI(SCHEMA.NAMESPACE, "SiteNavigationElement");
        SkiResort = factory.createIRI(SCHEMA.NAMESPACE, "SkiResort");
        SocialEvent = factory.createIRI(SCHEMA.NAMESPACE, "SocialEvent");
        SoftwareApplication = factory.createIRI(SCHEMA.NAMESPACE, "SoftwareApplication");
        SomeProducts = factory.createIRI(SCHEMA.NAMESPACE, "SomeProducts");
        Specialty = factory.createIRI(SCHEMA.NAMESPACE, "Specialty");
        SportingGoodsStore = factory.createIRI(SCHEMA.NAMESPACE, "SportingGoodsStore");
        SportsActivityLocation = factory.createIRI(SCHEMA.NAMESPACE, "SportsActivityLocation");
        SportsClub = factory.createIRI(SCHEMA.NAMESPACE, "SportsClub");
        SportsEvent = factory.createIRI(SCHEMA.NAMESPACE, "SportsEvent");
        SportsTeam = factory.createIRI(SCHEMA.NAMESPACE, "SportsTeam");
        StadiumOrArena = factory.createIRI(SCHEMA.NAMESPACE, "StadiumOrArena");
        State = factory.createIRI(SCHEMA.NAMESPACE, "State");
        Store = factory.createIRI(SCHEMA.NAMESPACE, "Store");
        StructuredValue = factory.createIRI(SCHEMA.NAMESPACE, "StructuredValue");
        SubwayStation = factory.createIRI(SCHEMA.NAMESPACE, "SubwayStation");
        SuperficialAnatomy = factory.createIRI(SCHEMA.NAMESPACE, "SuperficialAnatomy");
        Synagogue = factory.createIRI(SCHEMA.NAMESPACE, "Synagogue");
        TVEpisode = factory.createIRI(SCHEMA.NAMESPACE, "TVEpisode");
        TVSeason = factory.createIRI(SCHEMA.NAMESPACE, "TVSeason");
        TVSeries = factory.createIRI(SCHEMA.NAMESPACE, "TVSeries");
        Table = factory.createIRI(SCHEMA.NAMESPACE, "Table");
        TattooParlor = factory.createIRI(SCHEMA.NAMESPACE, "TattooParlor");
        TaxiStand = factory.createIRI(SCHEMA.NAMESPACE, "TaxiStand");
        TelevisionStation = factory.createIRI(SCHEMA.NAMESPACE, "TelevisionStation");
        TennisComplex = factory.createIRI(SCHEMA.NAMESPACE, "TennisComplex");
        TheaterEvent = factory.createIRI(SCHEMA.NAMESPACE, "TheaterEvent");
        TheaterGroup = factory.createIRI(SCHEMA.NAMESPACE, "TheaterGroup");
        TherapeuticProcedure = factory.createIRI(SCHEMA.NAMESPACE, "TherapeuticProcedure");
        Thing = factory.createIRI(SCHEMA.NAMESPACE, "Thing");
        TireShop = factory.createIRI(SCHEMA.NAMESPACE, "TireShop");
        TouristAttraction = factory.createIRI(SCHEMA.NAMESPACE, "TouristAttraction");
        TouristInformationCenter = factory.createIRI(SCHEMA.NAMESPACE, "TouristInformationCenter");
        ToyStore = factory.createIRI(SCHEMA.NAMESPACE, "ToyStore");
        TrainStation = factory.createIRI(SCHEMA.NAMESPACE, "TrainStation");
        TravelAgency = factory.createIRI(SCHEMA.NAMESPACE, "TravelAgency");
        TreatmentIndication = factory.createIRI(SCHEMA.NAMESPACE, "TreatmentIndication");
        TypeAndQuantityNode = factory.createIRI(SCHEMA.NAMESPACE, "TypeAndQuantityNode");
        UnitPriceSpecification = factory.createIRI(SCHEMA.NAMESPACE, "UnitPriceSpecification");
        UserBlocks = factory.createIRI(SCHEMA.NAMESPACE, "UserBlocks");
        UserCheckins = factory.createIRI(SCHEMA.NAMESPACE, "UserCheckins");
        UserComments = factory.createIRI(SCHEMA.NAMESPACE, "UserComments");
        UserDownloads = factory.createIRI(SCHEMA.NAMESPACE, "UserDownloads");
        UserInteraction = factory.createIRI(SCHEMA.NAMESPACE, "UserInteraction");
        UserLikes = factory.createIRI(SCHEMA.NAMESPACE, "UserLikes");
        UserPageVisits = factory.createIRI(SCHEMA.NAMESPACE, "UserPageVisits");
        UserPlays = factory.createIRI(SCHEMA.NAMESPACE, "UserPlays");
        UserPlusOnes = factory.createIRI(SCHEMA.NAMESPACE, "UserPlusOnes");
        UserTweets = factory.createIRI(SCHEMA.NAMESPACE, "UserTweets");
        Vein = factory.createIRI(SCHEMA.NAMESPACE, "Vein");
        Vessel = factory.createIRI(SCHEMA.NAMESPACE, "Vessel");
        VeterinaryCare = factory.createIRI(SCHEMA.NAMESPACE, "VeterinaryCare");
        VideoGallery = factory.createIRI(SCHEMA.NAMESPACE, "VideoGallery");
        VideoObject = factory.createIRI(SCHEMA.NAMESPACE, "VideoObject");
        VisualArtsEvent = factory.createIRI(SCHEMA.NAMESPACE, "VisualArtsEvent");
        Volcano = factory.createIRI(SCHEMA.NAMESPACE, "Volcano");
        WPAdBlock = factory.createIRI(SCHEMA.NAMESPACE, "WPAdBlock");
        WPFooter = factory.createIRI(SCHEMA.NAMESPACE, "WPFooter");
        WPHeader = factory.createIRI(SCHEMA.NAMESPACE, "WPHeader");
        WPSideBar = factory.createIRI(SCHEMA.NAMESPACE, "WPSideBar");
        WarrantyPromise = factory.createIRI(SCHEMA.NAMESPACE, "WarrantyPromise");
        WarrantyScope = factory.createIRI(SCHEMA.NAMESPACE, "WarrantyScope");
        Waterfall = factory.createIRI(SCHEMA.NAMESPACE, "Waterfall");
        WebApplication = factory.createIRI(SCHEMA.NAMESPACE, "WebApplication");
        WebPage = factory.createIRI(SCHEMA.NAMESPACE, "WebPage");
        WebPageElement = factory.createIRI(SCHEMA.NAMESPACE, "WebPageElement");
        WholesaleStore = factory.createIRI(SCHEMA.NAMESPACE, "WholesaleStore");
        Winery = factory.createIRI(SCHEMA.NAMESPACE, "Winery");
        Zoo = factory.createIRI(SCHEMA.NAMESPACE, "Zoo");
        about = factory.createIRI(SCHEMA.NAMESPACE, "about");
        acceptedPaymentMethod = factory.createIRI(SCHEMA.NAMESPACE, "acceptedPaymentMethod");
        acceptsReservations = factory.createIRI(SCHEMA.NAMESPACE, "acceptsReservations");
        accountablePerson = factory.createIRI(SCHEMA.NAMESPACE, "accountablePerson");
        acquiredFrom = factory.createIRI(SCHEMA.NAMESPACE, "acquiredFrom");
        action = factory.createIRI(SCHEMA.NAMESPACE, "action");
        activeIngredient = factory.createIRI(SCHEMA.NAMESPACE, "activeIngredient");
        activityDuration = factory.createIRI(SCHEMA.NAMESPACE, "activityDuration");
        activityFrequency = factory.createIRI(SCHEMA.NAMESPACE, "activityFrequency");
        actor = factory.createIRI(SCHEMA.NAMESPACE, "actor");
        actors = factory.createIRI(SCHEMA.NAMESPACE, "actors");
        addOn = factory.createIRI(SCHEMA.NAMESPACE, "addOn");
        additionalName = factory.createIRI(SCHEMA.NAMESPACE, "additionalName");
        additionalType = factory.createIRI(SCHEMA.NAMESPACE, "additionalType");
        additionalVariable = factory.createIRI(SCHEMA.NAMESPACE, "additionalVariable");
        address = factory.createIRI(SCHEMA.NAMESPACE, "address");
        addressCountry = factory.createIRI(SCHEMA.NAMESPACE, "addressCountry");
        addressLocality = factory.createIRI(SCHEMA.NAMESPACE, "addressLocality");
        addressRegion = factory.createIRI(SCHEMA.NAMESPACE, "addressRegion");
        administrationRoute = factory.createIRI(SCHEMA.NAMESPACE, "administrationRoute");
        advanceBookingRequirement = factory.createIRI(SCHEMA.NAMESPACE, "advanceBookingRequirement");
        adverseOutcome = factory.createIRI(SCHEMA.NAMESPACE, "adverseOutcome");
        affectedBy = factory.createIRI(SCHEMA.NAMESPACE, "affectedBy");
        affiliation = factory.createIRI(SCHEMA.NAMESPACE, "affiliation");
        aggregateRating = factory.createIRI(SCHEMA.NAMESPACE, "aggregateRating");
        album = factory.createIRI(SCHEMA.NAMESPACE, "album");
        albums = factory.createIRI(SCHEMA.NAMESPACE, "albums");
        alcoholWarning = factory.createIRI(SCHEMA.NAMESPACE, "alcoholWarning");
        algorithm = factory.createIRI(SCHEMA.NAMESPACE, "algorithm");
        alternateName = factory.createIRI(SCHEMA.NAMESPACE, "alternateName");
        alternativeHeadline = factory.createIRI(SCHEMA.NAMESPACE, "alternativeHeadline");
        alumni = factory.createIRI(SCHEMA.NAMESPACE, "alumni");
        alumniOf = factory.createIRI(SCHEMA.NAMESPACE, "alumniOf");
        amountOfThisGood = factory.createIRI(SCHEMA.NAMESPACE, "amountOfThisGood");
        antagonist = factory.createIRI(SCHEMA.NAMESPACE, "antagonist");
        applicableLocation = factory.createIRI(SCHEMA.NAMESPACE, "applicableLocation");
        applicationCategory = factory.createIRI(SCHEMA.NAMESPACE, "applicationCategory");
        applicationSubCategory = factory.createIRI(SCHEMA.NAMESPACE, "applicationSubCategory");
        applicationSuite = factory.createIRI(SCHEMA.NAMESPACE, "applicationSuite");
        appliesToDeliveryMethod = factory.createIRI(SCHEMA.NAMESPACE, "appliesToDeliveryMethod");
        appliesToPaymentMethod = factory.createIRI(SCHEMA.NAMESPACE, "appliesToPaymentMethod");
        arterialBranch = factory.createIRI(SCHEMA.NAMESPACE, "arterialBranch");
        articleBody = factory.createIRI(SCHEMA.NAMESPACE, "articleBody");
        articleSection = factory.createIRI(SCHEMA.NAMESPACE, "articleSection");
        aspect = factory.createIRI(SCHEMA.NAMESPACE, "aspect");
        associatedAnatomy = factory.createIRI(SCHEMA.NAMESPACE, "associatedAnatomy");
        associatedArticle = factory.createIRI(SCHEMA.NAMESPACE, "associatedArticle");
        associatedMedia = factory.createIRI(SCHEMA.NAMESPACE, "associatedMedia");
        associatedPathophysiology = factory.createIRI(SCHEMA.NAMESPACE, "associatedPathophysiology");
        attendee = factory.createIRI(SCHEMA.NAMESPACE, "attendee");
        attendees = factory.createIRI(SCHEMA.NAMESPACE, "attendees");
        audience = factory.createIRI(SCHEMA.NAMESPACE, "audience");
        audio = factory.createIRI(SCHEMA.NAMESPACE, "audio");
        author = factory.createIRI(SCHEMA.NAMESPACE, "author");
        availability = factory.createIRI(SCHEMA.NAMESPACE, "availability");
        availabilityEnds = factory.createIRI(SCHEMA.NAMESPACE, "availabilityEnds");
        availabilityStarts = factory.createIRI(SCHEMA.NAMESPACE, "availabilityStarts");
        availableAtOrFrom = factory.createIRI(SCHEMA.NAMESPACE, "availableAtOrFrom");
        availableDeliveryMethod = factory.createIRI(SCHEMA.NAMESPACE, "availableDeliveryMethod");
        availableIn = factory.createIRI(SCHEMA.NAMESPACE, "availableIn");
        availableService = factory.createIRI(SCHEMA.NAMESPACE, "availableService");
        availableStrength = factory.createIRI(SCHEMA.NAMESPACE, "availableStrength");
        availableTest = factory.createIRI(SCHEMA.NAMESPACE, "availableTest");
        award = factory.createIRI(SCHEMA.NAMESPACE, "award");
        awards = factory.createIRI(SCHEMA.NAMESPACE, "awards");
        background = factory.createIRI(SCHEMA.NAMESPACE, "background");
        baseSalary = factory.createIRI(SCHEMA.NAMESPACE, "baseSalary");
        benefits = factory.createIRI(SCHEMA.NAMESPACE, "benefits");
        bestRating = factory.createIRI(SCHEMA.NAMESPACE, "bestRating");
        billingIncrement = factory.createIRI(SCHEMA.NAMESPACE, "billingIncrement");
        biomechnicalClass = factory.createIRI(SCHEMA.NAMESPACE, "biomechnicalClass");
        birthDate = factory.createIRI(SCHEMA.NAMESPACE, "birthDate");
        bitrate = factory.createIRI(SCHEMA.NAMESPACE, "bitrate");
        blogPost = factory.createIRI(SCHEMA.NAMESPACE, "blogPost");
        blogPosts = factory.createIRI(SCHEMA.NAMESPACE, "blogPosts");
        bloodSupply = factory.createIRI(SCHEMA.NAMESPACE, "bloodSupply");
        bodyLocation = factory.createIRI(SCHEMA.NAMESPACE, "bodyLocation");
        bookEdition = factory.createIRI(SCHEMA.NAMESPACE, "bookEdition");
        bookFormat = factory.createIRI(SCHEMA.NAMESPACE, "bookFormat");
        box = factory.createIRI(SCHEMA.NAMESPACE, "box");
        branch = factory.createIRI(SCHEMA.NAMESPACE, "branch");
        branchOf = factory.createIRI(SCHEMA.NAMESPACE, "branchOf");
        brand = factory.createIRI(SCHEMA.NAMESPACE, "brand");
        breadcrumb = factory.createIRI(SCHEMA.NAMESPACE, "breadcrumb");
        breastfeedingWarning = factory.createIRI(SCHEMA.NAMESPACE, "breastfeedingWarning");
        browserRequirements = factory.createIRI(SCHEMA.NAMESPACE, "browserRequirements");
        businessFunction = factory.createIRI(SCHEMA.NAMESPACE, "businessFunction");
        byArtist = factory.createIRI(SCHEMA.NAMESPACE, "byArtist");
        calories = factory.createIRI(SCHEMA.NAMESPACE, "calories");
        caption = factory.createIRI(SCHEMA.NAMESPACE, "caption");
        carbohydrateContent = factory.createIRI(SCHEMA.NAMESPACE, "carbohydrateContent");
        carrierRequirements = factory.createIRI(SCHEMA.NAMESPACE, "carrierRequirements");
        category = factory.createIRI(SCHEMA.NAMESPACE, "category");
        cause = factory.createIRI(SCHEMA.NAMESPACE, "cause");
        causeOf = factory.createIRI(SCHEMA.NAMESPACE, "causeOf");
        children = factory.createIRI(SCHEMA.NAMESPACE, "children");
        cholesterolContent = factory.createIRI(SCHEMA.NAMESPACE, "cholesterolContent");
        circle = factory.createIRI(SCHEMA.NAMESPACE, "circle");
        citation = factory.createIRI(SCHEMA.NAMESPACE, "citation");
        clincalPharmacology = factory.createIRI(SCHEMA.NAMESPACE, "clincalPharmacology");
        closes = factory.createIRI(SCHEMA.NAMESPACE, "closes");
        code = factory.createIRI(SCHEMA.NAMESPACE, "code");
        codeValue = factory.createIRI(SCHEMA.NAMESPACE, "codeValue");
        codingSystem = factory.createIRI(SCHEMA.NAMESPACE, "codingSystem");
        colleague = factory.createIRI(SCHEMA.NAMESPACE, "colleague");
        colleagues = factory.createIRI(SCHEMA.NAMESPACE, "colleagues");
        color = factory.createIRI(SCHEMA.NAMESPACE, "color");
        comment = factory.createIRI(SCHEMA.NAMESPACE, "comment");
        commentText = factory.createIRI(SCHEMA.NAMESPACE, "commentText");
        commentTime = factory.createIRI(SCHEMA.NAMESPACE, "commentTime");
        comprisedOf = factory.createIRI(SCHEMA.NAMESPACE, "comprisedOf");
        connectedTo = factory.createIRI(SCHEMA.NAMESPACE, "connectedTo");
        contactPoint = factory.createIRI(SCHEMA.NAMESPACE, "contactPoint");
        contactPoints = factory.createIRI(SCHEMA.NAMESPACE, "contactPoints");
        contactType = factory.createIRI(SCHEMA.NAMESPACE, "contactType");
        containedIn = factory.createIRI(SCHEMA.NAMESPACE, "containedIn");
        contentLocation = factory.createIRI(SCHEMA.NAMESPACE, "contentLocation");
        contentRating = factory.createIRI(SCHEMA.NAMESPACE, "contentRating");
        contentSize = factory.createIRI(SCHEMA.NAMESPACE, "contentSize");
        contentUrl = factory.createIRI(SCHEMA.NAMESPACE, "contentUrl");
        contraindication = factory.createIRI(SCHEMA.NAMESPACE, "contraindication");
        contributor = factory.createIRI(SCHEMA.NAMESPACE, "contributor");
        cookTime = factory.createIRI(SCHEMA.NAMESPACE, "cookTime");
        cookingMethod = factory.createIRI(SCHEMA.NAMESPACE, "cookingMethod");
        copyrightHolder = factory.createIRI(SCHEMA.NAMESPACE, "copyrightHolder");
        copyrightYear = factory.createIRI(SCHEMA.NAMESPACE, "copyrightYear");
        cost = factory.createIRI(SCHEMA.NAMESPACE, "cost");
        costCategory = factory.createIRI(SCHEMA.NAMESPACE, "costCategory");
        costCurrency = factory.createIRI(SCHEMA.NAMESPACE, "costCurrency");
        costOrigin = factory.createIRI(SCHEMA.NAMESPACE, "costOrigin");
        costPerUnit = factory.createIRI(SCHEMA.NAMESPACE, "costPerUnit");
        countriesNotSupported = factory.createIRI(SCHEMA.NAMESPACE, "countriesNotSupported");
        countriesSupported = factory.createIRI(SCHEMA.NAMESPACE, "countriesSupported");
        creator = factory.createIRI(SCHEMA.NAMESPACE, "creator");
        currenciesAccepted = factory.createIRI(SCHEMA.NAMESPACE, "currenciesAccepted");
        dateCreated = factory.createIRI(SCHEMA.NAMESPACE, "dateCreated");
        dateModified = factory.createIRI(SCHEMA.NAMESPACE, "dateModified");
        datePosted = factory.createIRI(SCHEMA.NAMESPACE, "datePosted");
        datePublished = factory.createIRI(SCHEMA.NAMESPACE, "datePublished");
        dateline = factory.createIRI(SCHEMA.NAMESPACE, "dateline");
        dayOfWeek = factory.createIRI(SCHEMA.NAMESPACE, "dayOfWeek");
        deathDate = factory.createIRI(SCHEMA.NAMESPACE, "deathDate");
        deliveryLeadTime = factory.createIRI(SCHEMA.NAMESPACE, "deliveryLeadTime");
        depth = factory.createIRI(SCHEMA.NAMESPACE, "depth");
        description = factory.createIRI(SCHEMA.NAMESPACE, "description");
        device = factory.createIRI(SCHEMA.NAMESPACE, "device");
        diagnosis = factory.createIRI(SCHEMA.NAMESPACE, "diagnosis");
        diagram = factory.createIRI(SCHEMA.NAMESPACE, "diagram");
        dietFeatures = factory.createIRI(SCHEMA.NAMESPACE, "dietFeatures");
        differentialDiagnosis = factory.createIRI(SCHEMA.NAMESPACE, "differentialDiagnosis");
        director = factory.createIRI(SCHEMA.NAMESPACE, "director");
        discusses = factory.createIRI(SCHEMA.NAMESPACE, "discusses");
        discussionUrl = factory.createIRI(SCHEMA.NAMESPACE, "discussionUrl");
        distinguishingSign = factory.createIRI(SCHEMA.NAMESPACE, "distinguishingSign");
        dosageForm = factory.createIRI(SCHEMA.NAMESPACE, "dosageForm");
        doseSchedule = factory.createIRI(SCHEMA.NAMESPACE, "doseSchedule");
        doseUnit = factory.createIRI(SCHEMA.NAMESPACE, "doseUnit");
        doseValue = factory.createIRI(SCHEMA.NAMESPACE, "doseValue");
        downloadUrl = factory.createIRI(SCHEMA.NAMESPACE, "downloadUrl");
        drainsTo = factory.createIRI(SCHEMA.NAMESPACE, "drainsTo");
        drug = factory.createIRI(SCHEMA.NAMESPACE, "drug");
        drugClass = factory.createIRI(SCHEMA.NAMESPACE, "drugClass");
        drugUnit = factory.createIRI(SCHEMA.NAMESPACE, "drugUnit");
        duns = factory.createIRI(SCHEMA.NAMESPACE, "duns");
        duplicateTherapy = factory.createIRI(SCHEMA.NAMESPACE, "duplicateTherapy");
        duration = factory.createIRI(SCHEMA.NAMESPACE, "duration");
        durationOfWarranty = factory.createIRI(SCHEMA.NAMESPACE, "durationOfWarranty");
        editor = factory.createIRI(SCHEMA.NAMESPACE, "editor");
        educationRequirements = factory.createIRI(SCHEMA.NAMESPACE, "educationRequirements");
        elevation = factory.createIRI(SCHEMA.NAMESPACE, "elevation");
        eligibleCustomerType = factory.createIRI(SCHEMA.NAMESPACE, "eligibleCustomerType");
        eligibleDuration = factory.createIRI(SCHEMA.NAMESPACE, "eligibleDuration");
        eligibleQuantity = factory.createIRI(SCHEMA.NAMESPACE, "eligibleQuantity");
        eligibleRegion = factory.createIRI(SCHEMA.NAMESPACE, "eligibleRegion");
        eligibleTransactionVolume = factory.createIRI(SCHEMA.NAMESPACE, "eligibleTransactionVolume");
        email = factory.createIRI(SCHEMA.NAMESPACE, "email");
        embedUrl = factory.createIRI(SCHEMA.NAMESPACE, "embedUrl");
        employee = factory.createIRI(SCHEMA.NAMESPACE, "employee");
        employees = factory.createIRI(SCHEMA.NAMESPACE, "employees");
        employmentType = factory.createIRI(SCHEMA.NAMESPACE, "employmentType");
        encodesCreativeWork = factory.createIRI(SCHEMA.NAMESPACE, "encodesCreativeWork");
        encoding = factory.createIRI(SCHEMA.NAMESPACE, "encoding");
        encodingFormat = factory.createIRI(SCHEMA.NAMESPACE, "encodingFormat");
        encodings = factory.createIRI(SCHEMA.NAMESPACE, "encodings");
        endDate = factory.createIRI(SCHEMA.NAMESPACE, "endDate");
        endorsers = factory.createIRI(SCHEMA.NAMESPACE, "endorsers");
        epidemiology = factory.createIRI(SCHEMA.NAMESPACE, "epidemiology");
        episode = factory.createIRI(SCHEMA.NAMESPACE, "episode");
        episodeNumber = factory.createIRI(SCHEMA.NAMESPACE, "episodeNumber");
        episodes = factory.createIRI(SCHEMA.NAMESPACE, "episodes");
        equal = factory.createIRI(SCHEMA.NAMESPACE, "equal");
        estimatesRiskOf = factory.createIRI(SCHEMA.NAMESPACE, "estimatesRiskOf");
        event = factory.createIRI(SCHEMA.NAMESPACE, "event");
        events = factory.createIRI(SCHEMA.NAMESPACE, "events");
        evidenceLevel = factory.createIRI(SCHEMA.NAMESPACE, "evidenceLevel");
        evidenceOrigin = factory.createIRI(SCHEMA.NAMESPACE, "evidenceOrigin");
        exerciseType = factory.createIRI(SCHEMA.NAMESPACE, "exerciseType");
        exifData = factory.createIRI(SCHEMA.NAMESPACE, "exifData");
        expectedPrognosis = factory.createIRI(SCHEMA.NAMESPACE, "expectedPrognosis");
        experienceRequirements = factory.createIRI(SCHEMA.NAMESPACE, "experienceRequirements");
        expertConsiderations = factory.createIRI(SCHEMA.NAMESPACE, "expertConsiderations");
        expires = factory.createIRI(SCHEMA.NAMESPACE, "expires");
        familyName = factory.createIRI(SCHEMA.NAMESPACE, "familyName");
        fatContent = factory.createIRI(SCHEMA.NAMESPACE, "fatContent");
        faxNumber = factory.createIRI(SCHEMA.NAMESPACE, "faxNumber");
        featureList = factory.createIRI(SCHEMA.NAMESPACE, "featureList");
        fiberContent = factory.createIRI(SCHEMA.NAMESPACE, "fiberContent");
        fileFormat = factory.createIRI(SCHEMA.NAMESPACE, "fileFormat");
        fileSize = factory.createIRI(SCHEMA.NAMESPACE, "fileSize");
        follows = factory.createIRI(SCHEMA.NAMESPACE, "follows");
        followup = factory.createIRI(SCHEMA.NAMESPACE, "followup");
        foodWarning = factory.createIRI(SCHEMA.NAMESPACE, "foodWarning");
        founder = factory.createIRI(SCHEMA.NAMESPACE, "founder");
        founders = factory.createIRI(SCHEMA.NAMESPACE, "founders");
        foundingDate = factory.createIRI(SCHEMA.NAMESPACE, "foundingDate");
        frequency = factory.createIRI(SCHEMA.NAMESPACE, "frequency");
        function = factory.createIRI(SCHEMA.NAMESPACE, "function");
        functionalClass = factory.createIRI(SCHEMA.NAMESPACE, "functionalClass");
        gender = factory.createIRI(SCHEMA.NAMESPACE, "gender");
        genre = factory.createIRI(SCHEMA.NAMESPACE, "genre");
        geo = factory.createIRI(SCHEMA.NAMESPACE, "geo");
        givenName = factory.createIRI(SCHEMA.NAMESPACE, "givenName");
        globalLocationNumber = factory.createIRI(SCHEMA.NAMESPACE, "globalLocationNumber");
        greater = factory.createIRI(SCHEMA.NAMESPACE, "greater");
        greaterOrEqual = factory.createIRI(SCHEMA.NAMESPACE, "greaterOrEqual");
        gtin13 = factory.createIRI(SCHEMA.NAMESPACE, "gtin13");
        gtin14 = factory.createIRI(SCHEMA.NAMESPACE, "gtin14");
        gtin8 = factory.createIRI(SCHEMA.NAMESPACE, "gtin8");
        guideline = factory.createIRI(SCHEMA.NAMESPACE, "guideline");
        guidelineDate = factory.createIRI(SCHEMA.NAMESPACE, "guidelineDate");
        guidelineSubject = factory.createIRI(SCHEMA.NAMESPACE, "guidelineSubject");
        hasPOS = factory.createIRI(SCHEMA.NAMESPACE, "hasPOS");
        headline = factory.createIRI(SCHEMA.NAMESPACE, "headline");
        height = factory.createIRI(SCHEMA.NAMESPACE, "height");
        highPrice = factory.createIRI(SCHEMA.NAMESPACE, "highPrice");
        hiringOrganization = factory.createIRI(SCHEMA.NAMESPACE, "hiringOrganization");
        homeLocation = factory.createIRI(SCHEMA.NAMESPACE, "homeLocation");
        honorificPrefix = factory.createIRI(SCHEMA.NAMESPACE, "honorificPrefix");
        honorificSuffix = factory.createIRI(SCHEMA.NAMESPACE, "honorificSuffix");
        hospitalAffiliation = factory.createIRI(SCHEMA.NAMESPACE, "hospitalAffiliation");
        howPerformed = factory.createIRI(SCHEMA.NAMESPACE, "howPerformed");
        identifyingExam = factory.createIRI(SCHEMA.NAMESPACE, "identifyingExam");
        identifyingTest = factory.createIRI(SCHEMA.NAMESPACE, "identifyingTest");
        illustrator = factory.createIRI(SCHEMA.NAMESPACE, "illustrator");
        image = factory.createIRI(SCHEMA.NAMESPACE, "image");
        imagingTechnique = factory.createIRI(SCHEMA.NAMESPACE, "imagingTechnique");
        inAlbum = factory.createIRI(SCHEMA.NAMESPACE, "inAlbum");
        inLanguage = factory.createIRI(SCHEMA.NAMESPACE, "inLanguage");
        inPlaylist = factory.createIRI(SCHEMA.NAMESPACE, "inPlaylist");
        incentives = factory.createIRI(SCHEMA.NAMESPACE, "incentives");
        includedRiskFactor = factory.createIRI(SCHEMA.NAMESPACE, "includedRiskFactor");
        includesObject = factory.createIRI(SCHEMA.NAMESPACE, "includesObject");
        increasesRiskOf = factory.createIRI(SCHEMA.NAMESPACE, "increasesRiskOf");
        indication = factory.createIRI(SCHEMA.NAMESPACE, "indication");
        industry = factory.createIRI(SCHEMA.NAMESPACE, "industry");
        infectiousAgent = factory.createIRI(SCHEMA.NAMESPACE, "infectiousAgent");
        infectiousAgentClass = factory.createIRI(SCHEMA.NAMESPACE, "infectiousAgentClass");
        ingredients = factory.createIRI(SCHEMA.NAMESPACE, "ingredients");
        insertion = factory.createIRI(SCHEMA.NAMESPACE, "insertion");
        installUrl = factory.createIRI(SCHEMA.NAMESPACE, "installUrl");
        intensity = factory.createIRI(SCHEMA.NAMESPACE, "intensity");
        interactingDrug = factory.createIRI(SCHEMA.NAMESPACE, "interactingDrug");
        interactionCount = factory.createIRI(SCHEMA.NAMESPACE, "interactionCount");
        inventoryLevel = factory.createIRI(SCHEMA.NAMESPACE, "inventoryLevel");
        isAccessoryOrSparePartFor = factory.createIRI(SCHEMA.NAMESPACE, "isAccessoryOrSparePartFor");
        isAvailableGenerically = factory.createIRI(SCHEMA.NAMESPACE, "isAvailableGenerically");
        isConsumableFor = factory.createIRI(SCHEMA.NAMESPACE, "isConsumableFor");
        isFamilyFriendly = factory.createIRI(SCHEMA.NAMESPACE, "isFamilyFriendly");
        isPartOf = factory.createIRI(SCHEMA.NAMESPACE, "isPartOf");
        isProprietary = factory.createIRI(SCHEMA.NAMESPACE, "isProprietary");
        isRelatedTo = factory.createIRI(SCHEMA.NAMESPACE, "isRelatedTo");
        isSimilarTo = factory.createIRI(SCHEMA.NAMESPACE, "isSimilarTo");
        isVariantOf = factory.createIRI(SCHEMA.NAMESPACE, "isVariantOf");
        isbn = factory.createIRI(SCHEMA.NAMESPACE, "isbn");
        isicV4 = factory.createIRI(SCHEMA.NAMESPACE, "isicV4");
        itemCondition = factory.createIRI(SCHEMA.NAMESPACE, "itemCondition");
        itemListElement = factory.createIRI(SCHEMA.NAMESPACE, "itemListElement");
        itemListOrder = factory.createIRI(SCHEMA.NAMESPACE, "itemListOrder");
        itemOffered = factory.createIRI(SCHEMA.NAMESPACE, "itemOffered");
        itemReviewed = factory.createIRI(SCHEMA.NAMESPACE, "itemReviewed");
        jobLocation = factory.createIRI(SCHEMA.NAMESPACE, "jobLocation");
        jobTitle = factory.createIRI(SCHEMA.NAMESPACE, "jobTitle");
        keywords = factory.createIRI(SCHEMA.NAMESPACE, "keywords");
        knows = factory.createIRI(SCHEMA.NAMESPACE, "knows");
        labelDetails = factory.createIRI(SCHEMA.NAMESPACE, "labelDetails");
        lastReviewed = factory.createIRI(SCHEMA.NAMESPACE, "lastReviewed");
        latitude = factory.createIRI(SCHEMA.NAMESPACE, "latitude");
        legalName = factory.createIRI(SCHEMA.NAMESPACE, "legalName");
        legalStatus = factory.createIRI(SCHEMA.NAMESPACE, "legalStatus");
        lesser = factory.createIRI(SCHEMA.NAMESPACE, "lesser");
        lesserOrEqual = factory.createIRI(SCHEMA.NAMESPACE, "lesserOrEqual");
        line = factory.createIRI(SCHEMA.NAMESPACE, "line");
        location = factory.createIRI(SCHEMA.NAMESPACE, "location");
        logo = factory.createIRI(SCHEMA.NAMESPACE, "logo");
        longitude = factory.createIRI(SCHEMA.NAMESPACE, "longitude");
        lowPrice = factory.createIRI(SCHEMA.NAMESPACE, "lowPrice");
        mainContentOfPage = factory.createIRI(SCHEMA.NAMESPACE, "mainContentOfPage");
        makesOffer = factory.createIRI(SCHEMA.NAMESPACE, "makesOffer");
        manufacturer = factory.createIRI(SCHEMA.NAMESPACE, "manufacturer");
        map = factory.createIRI(SCHEMA.NAMESPACE, "map");
        maps = factory.createIRI(SCHEMA.NAMESPACE, "maps");
        maxPrice = factory.createIRI(SCHEMA.NAMESPACE, "maxPrice");
        maxValue = factory.createIRI(SCHEMA.NAMESPACE, "maxValue");
        maximumIntake = factory.createIRI(SCHEMA.NAMESPACE, "maximumIntake");
        mechanismOfAction = factory.createIRI(SCHEMA.NAMESPACE, "mechanismOfAction");
        medicalSpecialty = factory.createIRI(SCHEMA.NAMESPACE, "medicalSpecialty");
        medicineSystem = factory.createIRI(SCHEMA.NAMESPACE, "medicineSystem");
        member = factory.createIRI(SCHEMA.NAMESPACE, "member");
        memberOf = factory.createIRI(SCHEMA.NAMESPACE, "memberOf");
        members = factory.createIRI(SCHEMA.NAMESPACE, "members");
        memoryRequirements = factory.createIRI(SCHEMA.NAMESPACE, "memoryRequirements");
        mentions = factory.createIRI(SCHEMA.NAMESPACE, "mentions");
        menu = factory.createIRI(SCHEMA.NAMESPACE, "menu");
        minPrice = factory.createIRI(SCHEMA.NAMESPACE, "minPrice");
        minValue = factory.createIRI(SCHEMA.NAMESPACE, "minValue");
        model = factory.createIRI(SCHEMA.NAMESPACE, "model");
        mpn = factory.createIRI(SCHEMA.NAMESPACE, "mpn");
        musicBy = factory.createIRI(SCHEMA.NAMESPACE, "musicBy");
        musicGroupMember = factory.createIRI(SCHEMA.NAMESPACE, "musicGroupMember");
        naics = factory.createIRI(SCHEMA.NAMESPACE, "naics");
        name = factory.createIRI(SCHEMA.NAMESPACE, "name");
        nationality = factory.createIRI(SCHEMA.NAMESPACE, "nationality");
        naturalProgression = factory.createIRI(SCHEMA.NAMESPACE, "naturalProgression");
        nerve = factory.createIRI(SCHEMA.NAMESPACE, "nerve");
        nerveMotor = factory.createIRI(SCHEMA.NAMESPACE, "nerveMotor");
        nonEqual = factory.createIRI(SCHEMA.NAMESPACE, "nonEqual");
        nonProprietaryName = factory.createIRI(SCHEMA.NAMESPACE, "nonProprietaryName");
        normalRange = factory.createIRI(SCHEMA.NAMESPACE, "normalRange");
        numTracks = factory.createIRI(SCHEMA.NAMESPACE, "numTracks");
        numberOfEpisodes = factory.createIRI(SCHEMA.NAMESPACE, "numberOfEpisodes");
        numberOfPages = factory.createIRI(SCHEMA.NAMESPACE, "numberOfPages");
        nutrition = factory.createIRI(SCHEMA.NAMESPACE, "nutrition");
        occupationalCategory = factory.createIRI(SCHEMA.NAMESPACE, "occupationalCategory");
        offerCount = factory.createIRI(SCHEMA.NAMESPACE, "offerCount");
        offers = factory.createIRI(SCHEMA.NAMESPACE, "offers");
        openingHours = factory.createIRI(SCHEMA.NAMESPACE, "openingHours");
        openingHoursSpecification = factory.createIRI(SCHEMA.NAMESPACE, "openingHoursSpecification");
        opens = factory.createIRI(SCHEMA.NAMESPACE, "opens");
        operatingSystem = factory.createIRI(SCHEMA.NAMESPACE, "operatingSystem");
        origin = factory.createIRI(SCHEMA.NAMESPACE, "origin");
        originatesFrom = factory.createIRI(SCHEMA.NAMESPACE, "originatesFrom");
        outcome = factory.createIRI(SCHEMA.NAMESPACE, "outcome");
        overdosage = factory.createIRI(SCHEMA.NAMESPACE, "overdosage");
        overview = factory.createIRI(SCHEMA.NAMESPACE, "overview");
        ownedFrom = factory.createIRI(SCHEMA.NAMESPACE, "ownedFrom");
        ownedThrough = factory.createIRI(SCHEMA.NAMESPACE, "ownedThrough");
        owns = factory.createIRI(SCHEMA.NAMESPACE, "owns");
        parent = factory.createIRI(SCHEMA.NAMESPACE, "parent");
        parents = factory.createIRI(SCHEMA.NAMESPACE, "parents");
        partOfSeason = factory.createIRI(SCHEMA.NAMESPACE, "partOfSeason");
        partOfSystem = factory.createIRI(SCHEMA.NAMESPACE, "partOfSystem");
        partOfTVSeries = factory.createIRI(SCHEMA.NAMESPACE, "partOfTVSeries");
        pathophysiology = factory.createIRI(SCHEMA.NAMESPACE, "pathophysiology");
        paymentAccepted = factory.createIRI(SCHEMA.NAMESPACE, "paymentAccepted");
        performer = factory.createIRI(SCHEMA.NAMESPACE, "performer");
        performerIn = factory.createIRI(SCHEMA.NAMESPACE, "performerIn");
        performers = factory.createIRI(SCHEMA.NAMESPACE, "performers");
        permissions = factory.createIRI(SCHEMA.NAMESPACE, "permissions");
        phase = factory.createIRI(SCHEMA.NAMESPACE, "phase");
        photo = factory.createIRI(SCHEMA.NAMESPACE, "photo");
        photos = factory.createIRI(SCHEMA.NAMESPACE, "photos");
        physiologicalBenefits = factory.createIRI(SCHEMA.NAMESPACE, "physiologicalBenefits");
        playerType = factory.createIRI(SCHEMA.NAMESPACE, "playerType");
        polygon = factory.createIRI(SCHEMA.NAMESPACE, "polygon");
        population = factory.createIRI(SCHEMA.NAMESPACE, "population");
        possibleComplication = factory.createIRI(SCHEMA.NAMESPACE, "possibleComplication");
        possibleTreatment = factory.createIRI(SCHEMA.NAMESPACE, "possibleTreatment");
        postOfficeBoxNumber = factory.createIRI(SCHEMA.NAMESPACE, "postOfficeBoxNumber");
        postOp = factory.createIRI(SCHEMA.NAMESPACE, "postOp");
        postalCode = factory.createIRI(SCHEMA.NAMESPACE, "postalCode");
        preOp = factory.createIRI(SCHEMA.NAMESPACE, "preOp");
        predecessorOf = factory.createIRI(SCHEMA.NAMESPACE, "predecessorOf");
        pregnancyCategory = factory.createIRI(SCHEMA.NAMESPACE, "pregnancyCategory");
        pregnancyWarning = factory.createIRI(SCHEMA.NAMESPACE, "pregnancyWarning");
        prepTime = factory.createIRI(SCHEMA.NAMESPACE, "prepTime");
        preparation = factory.createIRI(SCHEMA.NAMESPACE, "preparation");
        prescribingInfo = factory.createIRI(SCHEMA.NAMESPACE, "prescribingInfo");
        prescriptionStatus = factory.createIRI(SCHEMA.NAMESPACE, "prescriptionStatus");
        price = factory.createIRI(SCHEMA.NAMESPACE, "price");
        priceCurrency = factory.createIRI(SCHEMA.NAMESPACE, "priceCurrency");
        priceRange = factory.createIRI(SCHEMA.NAMESPACE, "priceRange");
        priceSpecification = factory.createIRI(SCHEMA.NAMESPACE, "priceSpecification");
        priceType = factory.createIRI(SCHEMA.NAMESPACE, "priceType");
        priceValidUntil = factory.createIRI(SCHEMA.NAMESPACE, "priceValidUntil");
        primaryImageOfPage = factory.createIRI(SCHEMA.NAMESPACE, "primaryImageOfPage");
        primaryPrevention = factory.createIRI(SCHEMA.NAMESPACE, "primaryPrevention");
        printColumn = factory.createIRI(SCHEMA.NAMESPACE, "printColumn");
        printEdition = factory.createIRI(SCHEMA.NAMESPACE, "printEdition");
        printPage = factory.createIRI(SCHEMA.NAMESPACE, "printPage");
        printSection = factory.createIRI(SCHEMA.NAMESPACE, "printSection");
        procedure = factory.createIRI(SCHEMA.NAMESPACE, "procedure");
        procedureType = factory.createIRI(SCHEMA.NAMESPACE, "procedureType");
        processorRequirements = factory.createIRI(SCHEMA.NAMESPACE, "processorRequirements");
        producer = factory.createIRI(SCHEMA.NAMESPACE, "producer");
        productID = factory.createIRI(SCHEMA.NAMESPACE, "productID");
        productionCompany = factory.createIRI(SCHEMA.NAMESPACE, "productionCompany");
        proprietaryName = factory.createIRI(SCHEMA.NAMESPACE, "proprietaryName");
        proteinContent = factory.createIRI(SCHEMA.NAMESPACE, "proteinContent");
        provider = factory.createIRI(SCHEMA.NAMESPACE, "provider");
        publicationType = factory.createIRI(SCHEMA.NAMESPACE, "publicationType");
        publisher = factory.createIRI(SCHEMA.NAMESPACE, "publisher");
        publishingPrinciples = factory.createIRI(SCHEMA.NAMESPACE, "publishingPrinciples");
        purpose = factory.createIRI(SCHEMA.NAMESPACE, "purpose");
        qualifications = factory.createIRI(SCHEMA.NAMESPACE, "qualifications");
        ratingCount = factory.createIRI(SCHEMA.NAMESPACE, "ratingCount");
        ratingValue = factory.createIRI(SCHEMA.NAMESPACE, "ratingValue");
        recipeCategory = factory.createIRI(SCHEMA.NAMESPACE, "recipeCategory");
        recipeCuisine = factory.createIRI(SCHEMA.NAMESPACE, "recipeCuisine");
        recipeInstructions = factory.createIRI(SCHEMA.NAMESPACE, "recipeInstructions");
        recipeYield = factory.createIRI(SCHEMA.NAMESPACE, "recipeYield");
        recognizingAuthority = factory.createIRI(SCHEMA.NAMESPACE, "recognizingAuthority");
        recommendationStrength = factory.createIRI(SCHEMA.NAMESPACE, "recommendationStrength");
        recommendedIntake = factory.createIRI(SCHEMA.NAMESPACE, "recommendedIntake");
        regionDrained = factory.createIRI(SCHEMA.NAMESPACE, "regionDrained");
        regionsAllowed = factory.createIRI(SCHEMA.NAMESPACE, "regionsAllowed");
        relatedAnatomy = factory.createIRI(SCHEMA.NAMESPACE, "relatedAnatomy");
        relatedCondition = factory.createIRI(SCHEMA.NAMESPACE, "relatedCondition");
        relatedDrug = factory.createIRI(SCHEMA.NAMESPACE, "relatedDrug");
        relatedLink = factory.createIRI(SCHEMA.NAMESPACE, "relatedLink");
        relatedStructure = factory.createIRI(SCHEMA.NAMESPACE, "relatedStructure");
        relatedTherapy = factory.createIRI(SCHEMA.NAMESPACE, "relatedTherapy");
        relatedTo = factory.createIRI(SCHEMA.NAMESPACE, "relatedTo");
        releaseDate = factory.createIRI(SCHEMA.NAMESPACE, "releaseDate");
        releaseNotes = factory.createIRI(SCHEMA.NAMESPACE, "releaseNotes");
        relevantSpecialty = factory.createIRI(SCHEMA.NAMESPACE, "relevantSpecialty");
        repetitions = factory.createIRI(SCHEMA.NAMESPACE, "repetitions");
        replyToUrl = factory.createIRI(SCHEMA.NAMESPACE, "replyToUrl");
        representativeOfPage = factory.createIRI(SCHEMA.NAMESPACE, "representativeOfPage");
        requirements = factory.createIRI(SCHEMA.NAMESPACE, "requirements");
        requiresSubscription = factory.createIRI(SCHEMA.NAMESPACE, "requiresSubscription");
        responsibilities = factory.createIRI(SCHEMA.NAMESPACE, "responsibilities");
        restPeriods = factory.createIRI(SCHEMA.NAMESPACE, "restPeriods");
        review = factory.createIRI(SCHEMA.NAMESPACE, "review");
        reviewBody = factory.createIRI(SCHEMA.NAMESPACE, "reviewBody");
        reviewCount = factory.createIRI(SCHEMA.NAMESPACE, "reviewCount");
        reviewRating = factory.createIRI(SCHEMA.NAMESPACE, "reviewRating");
        reviewedBy = factory.createIRI(SCHEMA.NAMESPACE, "reviewedBy");
        reviews = factory.createIRI(SCHEMA.NAMESPACE, "reviews");
        riskFactor = factory.createIRI(SCHEMA.NAMESPACE, "riskFactor");
        risks = factory.createIRI(SCHEMA.NAMESPACE, "risks");
        runsTo = factory.createIRI(SCHEMA.NAMESPACE, "runsTo");
        safetyConsideration = factory.createIRI(SCHEMA.NAMESPACE, "safetyConsideration");
        salaryCurrency = factory.createIRI(SCHEMA.NAMESPACE, "salaryCurrency");
        saturatedFatContent = factory.createIRI(SCHEMA.NAMESPACE, "saturatedFatContent");
        screenshot = factory.createIRI(SCHEMA.NAMESPACE, "screenshot");
        season = factory.createIRI(SCHEMA.NAMESPACE, "season");
        seasonNumber = factory.createIRI(SCHEMA.NAMESPACE, "seasonNumber");
        seasons = factory.createIRI(SCHEMA.NAMESPACE, "seasons");
        secondaryPrevention = factory.createIRI(SCHEMA.NAMESPACE, "secondaryPrevention");
        seeks = factory.createIRI(SCHEMA.NAMESPACE, "seeks");
        seller = factory.createIRI(SCHEMA.NAMESPACE, "seller");
        sensoryUnit = factory.createIRI(SCHEMA.NAMESPACE, "sensoryUnit");
        serialNumber = factory.createIRI(SCHEMA.NAMESPACE, "serialNumber");
        seriousAdverseOutcome = factory.createIRI(SCHEMA.NAMESPACE, "seriousAdverseOutcome");
        servesCuisine = factory.createIRI(SCHEMA.NAMESPACE, "servesCuisine");
        servingSize = factory.createIRI(SCHEMA.NAMESPACE, "servingSize");
        sibling = factory.createIRI(SCHEMA.NAMESPACE, "sibling");
        siblings = factory.createIRI(SCHEMA.NAMESPACE, "siblings");
        signDetected = factory.createIRI(SCHEMA.NAMESPACE, "signDetected");
        signOrSymptom = factory.createIRI(SCHEMA.NAMESPACE, "signOrSymptom");
        significance = factory.createIRI(SCHEMA.NAMESPACE, "significance");
        significantLink = factory.createIRI(SCHEMA.NAMESPACE, "significantLink");
        significantLinks = factory.createIRI(SCHEMA.NAMESPACE, "significantLinks");
        skills = factory.createIRI(SCHEMA.NAMESPACE, "skills");
        sku = factory.createIRI(SCHEMA.NAMESPACE, "sku");
        sodiumContent = factory.createIRI(SCHEMA.NAMESPACE, "sodiumContent");
        softwareVersion = factory.createIRI(SCHEMA.NAMESPACE, "softwareVersion");
        source = factory.createIRI(SCHEMA.NAMESPACE, "source");
        sourceOrganization = factory.createIRI(SCHEMA.NAMESPACE, "sourceOrganization");
        sourcedFrom = factory.createIRI(SCHEMA.NAMESPACE, "sourcedFrom");
        specialCommitments = factory.createIRI(SCHEMA.NAMESPACE, "specialCommitments");
        specialty = factory.createIRI(SCHEMA.NAMESPACE, "specialty");
        sponsor = factory.createIRI(SCHEMA.NAMESPACE, "sponsor");
        spouse = factory.createIRI(SCHEMA.NAMESPACE, "spouse");
        stage = factory.createIRI(SCHEMA.NAMESPACE, "stage");
        stageAsNumber = factory.createIRI(SCHEMA.NAMESPACE, "stageAsNumber");
        startDate = factory.createIRI(SCHEMA.NAMESPACE, "startDate");
        status = factory.createIRI(SCHEMA.NAMESPACE, "status");
        storageRequirements = factory.createIRI(SCHEMA.NAMESPACE, "storageRequirements");
        streetAddress = factory.createIRI(SCHEMA.NAMESPACE, "streetAddress");
        strengthUnit = factory.createIRI(SCHEMA.NAMESPACE, "strengthUnit");
        strengthValue = factory.createIRI(SCHEMA.NAMESPACE, "strengthValue");
        structuralClass = factory.createIRI(SCHEMA.NAMESPACE, "structuralClass");
        study = factory.createIRI(SCHEMA.NAMESPACE, "study");
        studyDesign = factory.createIRI(SCHEMA.NAMESPACE, "studyDesign");
        studyLocation = factory.createIRI(SCHEMA.NAMESPACE, "studyLocation");
        studySubject = factory.createIRI(SCHEMA.NAMESPACE, "studySubject");
        subEvent = factory.createIRI(SCHEMA.NAMESPACE, "subEvent");
        subEvents = factory.createIRI(SCHEMA.NAMESPACE, "subEvents");
        subStageSuffix = factory.createIRI(SCHEMA.NAMESPACE, "subStageSuffix");
        subStructure = factory.createIRI(SCHEMA.NAMESPACE, "subStructure");
        subTest = factory.createIRI(SCHEMA.NAMESPACE, "subTest");
        subtype = factory.createIRI(SCHEMA.NAMESPACE, "subtype");
        successorOf = factory.createIRI(SCHEMA.NAMESPACE, "successorOf");
        sugarContent = factory.createIRI(SCHEMA.NAMESPACE, "sugarContent");
        superEvent = factory.createIRI(SCHEMA.NAMESPACE, "superEvent");
        supplyTo = factory.createIRI(SCHEMA.NAMESPACE, "supplyTo");
        targetPopulation = factory.createIRI(SCHEMA.NAMESPACE, "targetPopulation");
        taxID = factory.createIRI(SCHEMA.NAMESPACE, "taxID");
        telephone = factory.createIRI(SCHEMA.NAMESPACE, "telephone");
        text = factory.createIRI(SCHEMA.NAMESPACE, "text");
        thumbnail = factory.createIRI(SCHEMA.NAMESPACE, "thumbnail");
        thumbnailUrl = factory.createIRI(SCHEMA.NAMESPACE, "thumbnailUrl");
        tickerSymbol = factory.createIRI(SCHEMA.NAMESPACE, "tickerSymbol");
        tissueSample = factory.createIRI(SCHEMA.NAMESPACE, "tissueSample");
        title = factory.createIRI(SCHEMA.NAMESPACE, "title");
        totalTime = factory.createIRI(SCHEMA.NAMESPACE, "totalTime");
        track = factory.createIRI(SCHEMA.NAMESPACE, "track");
        tracks = factory.createIRI(SCHEMA.NAMESPACE, "tracks");
        trailer = factory.createIRI(SCHEMA.NAMESPACE, "trailer");
        transFatContent = factory.createIRI(SCHEMA.NAMESPACE, "transFatContent");
        transcript = factory.createIRI(SCHEMA.NAMESPACE, "transcript");
        transmissionMethod = factory.createIRI(SCHEMA.NAMESPACE, "transmissionMethod");
        trialDesign = factory.createIRI(SCHEMA.NAMESPACE, "trialDesign");
        tributary = factory.createIRI(SCHEMA.NAMESPACE, "tributary");
        typeOfGood = factory.createIRI(SCHEMA.NAMESPACE, "typeOfGood");
        typicalTest = factory.createIRI(SCHEMA.NAMESPACE, "typicalTest");
        unitCode = factory.createIRI(SCHEMA.NAMESPACE, "unitCode");
        unsaturatedFatContent = factory.createIRI(SCHEMA.NAMESPACE, "unsaturatedFatContent");
        uploadDate = factory.createIRI(SCHEMA.NAMESPACE, "uploadDate");
        url = factory.createIRI(SCHEMA.NAMESPACE, "url");
        usedToDiagnose = factory.createIRI(SCHEMA.NAMESPACE, "usedToDiagnose");
        usesDevice = factory.createIRI(SCHEMA.NAMESPACE, "usesDevice");
        validFrom = factory.createIRI(SCHEMA.NAMESPACE, "validFrom");
        validThrough = factory.createIRI(SCHEMA.NAMESPACE, "validThrough");
        value = factory.createIRI(SCHEMA.NAMESPACE, "value");
        valueAddedTaxIncluded = factory.createIRI(SCHEMA.NAMESPACE, "valueAddedTaxIncluded");
        valueReference = factory.createIRI(SCHEMA.NAMESPACE, "valueReference");
        vatID = factory.createIRI(SCHEMA.NAMESPACE, "vatID");
        version = factory.createIRI(SCHEMA.NAMESPACE, "version");
        video = factory.createIRI(SCHEMA.NAMESPACE, "video");
        videoFrameSize = factory.createIRI(SCHEMA.NAMESPACE, "videoFrameSize");
        videoQuality = factory.createIRI(SCHEMA.NAMESPACE, "videoQuality");
        warning = factory.createIRI(SCHEMA.NAMESPACE, "warning");
        warranty = factory.createIRI(SCHEMA.NAMESPACE, "warranty");
        warrantyScope = factory.createIRI(SCHEMA.NAMESPACE, "warrantyScope");
        weight = factory.createIRI(SCHEMA.NAMESPACE, "weight");
        width = factory.createIRI(SCHEMA.NAMESPACE, "width");
        wordCount = factory.createIRI(SCHEMA.NAMESPACE, "wordCount");
        workHours = factory.createIRI(SCHEMA.NAMESPACE, "workHours");
        workLocation = factory.createIRI(SCHEMA.NAMESPACE, "workLocation");
        workload = factory.createIRI(SCHEMA.NAMESPACE, "workload");
        worksFor = factory.createIRI(SCHEMA.NAMESPACE, "worksFor");
        worstRating = factory.createIRI(SCHEMA.NAMESPACE, "worstRating");
    }
}
