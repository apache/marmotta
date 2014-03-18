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
 * Namespace SCHEMA
 */
public class SCHEMA {

    public static final String NAMESPACE = "http://schema.org/";

    public static final String PREFIX = "schema";

    /**
     * Web page type: About page. 
     */
    public static final URI AboutPage;

    /**
     * Accountancy business. 
     */
    public static final URI AccountingService;

    /**
     * A geographical region under the jurisdiction of a particular government. 
     */
    public static final URI AdministrativeArea;

    /**
     * An adult entertainment establishment. 
     */
    public static final URI AdultEntertainment;

    /**
     * When a single product that has different offers (for example, the same pair of shoes is offered by different merchants), then AggregateOffer can be used. 
     */
    public static final URI AggregateOffer;

    /**
     * The average rating based on multiple ratings or reviews. 
     */
    public static final URI AggregateRating;

    /**
     * An airport. 
     */
    public static final URI Airport;

    /**
     * An amusement park. 
     */
    public static final URI AmusementPark;

    /**
     * Any part of the human body, typically a component of an anatomical system. Organs, tissues, and cells are all anatomical structures. 
     */
    public static final URI AnatomicalStructure;

    /**
     * An anatomical system is a group of anatomical structures that work together to perform a certain task. Anatomical systems, such as organ systems, are one organizing principle of anatomy, and can includes circulatory, digestive, endocrine, integumentary, immune, lymphatic, muscular, nervous, reproductive, respiratory, skeletal, urinary, vestibular, and other systems. 
     */
    public static final URI AnatomicalSystem;

    /**
     * Animal shelter. 
     */
    public static final URI AnimalShelter;

    /**
     * Residence type: Apartment complex. 
     */
    public static final URI ApartmentComplex;

    /**
     * An indication for a medical therapy that has been formally specified or approved by a regulatory body that regulates use of the therapy; for example, the US FDA approves indications for most drugs in the US. 
     */
    public static final URI ApprovedIndication;

    /**
     * Aquarium. 
     */
    public static final URI Aquarium;

    /**
     * An art gallery. 
     */
    public static final URI ArtGallery;

    /**
     * A type of blood vessel that specifically carries blood away from the heart. 
     */
    public static final URI Artery;

    /**
     * An article, such as a news article or piece of investigative report. Newspapers and magazines have articles of many different types and this is intended to cover them all. 
     */
    public static final URI Article;

    /**
     * Professional service: Attorney. 
     */
    public static final URI Attorney;

    /**
     * Intended audience for a creative work, i.e. the group for whom the work was created. 
     */
    public static final URI Audience;

    /**
     * An audio file. 
     */
    public static final URI AudioObject;

    /**
     * Auto body shop. 
     */
    public static final URI AutoBodyShop;

    /**
     * An car dealership. 
     */
    public static final URI AutoDealer;

    /**
     * An auto parts store. 
     */
    public static final URI AutoPartsStore;

    /**
     * A car rental business. 
     */
    public static final URI AutoRental;

    /**
     * Car repair business. 
     */
    public static final URI AutoRepair;

    /**
     * A car wash business. 
     */
    public static final URI AutoWash;

    /**
     * ATM/cash machine. 
     */
    public static final URI AutomatedTeller;

    /**
     * Car repair, sales, or parts. 
     */
    public static final URI AutomotiveBusiness;

    /**
     * A bakery. 
     */
    public static final URI Bakery;

    /**
     * Bank or credit union. 
     */
    public static final URI BankOrCreditUnion;

    /**
     * A bar or pub. 
     */
    public static final URI BarOrPub;

    /**
     * Beach. 
     */
    public static final URI Beach;

    /**
     * Beauty salon. 
     */
    public static final URI BeautySalon;

    /**
     * Bed and breakfast. 
     */
    public static final URI BedAndBreakfast;

    /**
     * A bike store. 
     */
    public static final URI BikeStore;

    /**
     * A blog 
     */
    public static final URI Blog;

    /**
     * A blog post. 
     */
    public static final URI BlogPosting;

    /**
     * A medical test performed on a sample of a patient's blood. 
     */
    public static final URI BloodTest;

    /**
     * A body of water, such as a sea, ocean, or lake. 
     */
    public static final URI BodyOfWater;

    /**
     * Rigid connective tissue that comprises up the skeletal structure of the human body. 
     */
    public static final URI Bone;

    /**
     * A book. 
     */
    public static final URI Book;

    /**
     * The publication format of the book. 
     */
    public static final URI BookFormatType;

    /**
     * A bookstore. 
     */
    public static final URI BookStore;

    /**
     * A bowling alley. 
     */
    public static final URI BowlingAlley;

    /**
     * Any anatomical structure which pertains to the soft nervous tissue functioning as the coordinating center of sensation and intellectual and nervous activity. 
     */
    public static final URI BrainStructure;

    /**
     * A brand is a name used by an organization or business person for labeling a product, product group, or similar. 
     */
    public static final URI Brand;

    /**
     * Brewery. 
     */
    public static final URI Brewery;

    /**
     * A Buddhist temple. 
     */
    public static final URI BuddhistTemple;

    /**
     * A bus station. 
     */
    public static final URI BusStation;

    /**
     * A bus stop. 
     */
    public static final URI BusStop;

    /**
     * A business entity type is a conceptual entity representing the legal form, the size, the main line of business, the position in the value chain, or any combination thereof, of an organization or business person.

     Commonly used values:

     http://purl.org/goodrelations/v1#Business
     http://purl.org/goodrelations/v1#Enduser
     http://purl.org/goodrelations/v1#PublicInstitution
     http://purl.org/goodrelations/v1#Reseller 
     */
    public static final URI BusinessEntityType;

    /**
     * Event type: Business event. 
     */
    public static final URI BusinessEvent;

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
    public static final URI BusinessFunction;

    /**
     * A cafe or coffee shop. 
     */
    public static final URI CafeOrCoffeeShop;

    /**
     * A campground. 
     */
    public static final URI Campground;

    /**
     * A canal, like the Panama Canal 
     */
    public static final URI Canal;

    /**
     * A casino. 
     */
    public static final URI Casino;

    /**
     * A Catholic church. 
     */
    public static final URI CatholicChurch;

    /**
     * A graveyard. 
     */
    public static final URI Cemetery;

    /**
     * Web page type: Checkout page. 
     */
    public static final URI CheckoutPage;

    /**
     * A Childcare center. 
     */
    public static final URI ChildCare;

    /**
     * Event type: Children's event. 
     */
    public static final URI ChildrensEvent;

    /**
     * A church. 
     */
    public static final URI Church;

    /**
     * A city or town. 
     */
    public static final URI City;

    /**
     * A city hall. 
     */
    public static final URI CityHall;

    /**
     * A public structure, such as a town hall or concert hall. 
     */
    public static final URI CivicStructure;

    /**
     * A clothing store. 
     */
    public static final URI ClothingStore;

    /**
     * Web page type: Collection page. 
     */
    public static final URI CollectionPage;

    /**
     * A college, university, or other third-level educational institution. 
     */
    public static final URI CollegeOrUniversity;

    /**
     * A comedy club. 
     */
    public static final URI ComedyClub;

    /**
     * Event type: Comedy event. 
     */
    public static final URI ComedyEvent;

    /**
     * A comment on an item - for example, a comment on a blog post. The comment's content is expressed via the "text" property, and its topic via "about", properties shared with all CreativeWorks. 
     */
    public static final URI Comment;

    /**
     * A computer store. 
     */
    public static final URI ComputerStore;

    /**
     * Web page type: Contact page. 
     */
    public static final URI ContactPage;

    /**
     * A contact point—for example, a Customer Complaints department. 
     */
    public static final URI ContactPoint;

    /**
     * One of the continents (for example, Europe or Africa). 
     */
    public static final URI Continent;

    /**
     * A convenience store. 
     */
    public static final URI ConvenienceStore;

    /**
     * Organization: A business corporation. 
     */
    public static final URI Corporation;

    /**
     * A country. 
     */
    public static final URI Country;

    /**
     * A courthouse. 
     */
    public static final URI Courthouse;

    /**
     * The most generic kind of creative work, including books, movies, photographs, software programs, etc. 
     */
    public static final URI CreativeWork;

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
    public static final URI CreditCard;

    /**
     * A crematorium. 
     */
    public static final URI Crematorium;

    /**
     * An alternative, closely-related condition typically considered later in the differential diagnosis process along with the signs that are used to distinguish it. 
     */
    public static final URI DDxElement;

    /**
     * Event type: A social dance. 
     */
    public static final URI DanceEvent;

    /**
     * A dance group—for example, the Alvin Ailey Dance Theater or Riverdance. 
     */
    public static final URI DanceGroup;

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
    public static final URI DayOfWeek;

    /**
     * A day spa. 
     */
    public static final URI DaySpa;

    /**
     * A defence establishment, such as an army or navy base. 
     */
    public static final URI DefenceEstablishment;

    /**
     * The price for the delivery of an offer using a particular delivery method. 
     */
    public static final URI DeliveryChargeSpecification;

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
    public static final URI DeliveryMethod;

    /**
     * A demand entity represents the public, not necessarily binding, not necessarily exclusive, announcement by an organization or person to seek a certain type of goods or services. For describing demand using this type, the very same properties used for Offer apply. 
     */
    public static final URI Demand;

    /**
     * A dentist. 
     */
    public static final URI Dentist;

    /**
     * A department store. 
     */
    public static final URI DepartmentStore;

    /**
     * A medical laboratory that offers on-site or off-site diagnostic services. 
     */
    public static final URI DiagnosticLab;

    /**
     * A medical procedure intended primarly for diagnostic, as opposed to therapeutic, purposes. 
     */
    public static final URI DiagnosticProcedure;

    /**
     * A strategy of regulating the intake of food to achieve or maintain a specific health-related goal. 
     */
    public static final URI Diet;

    /**
     * A product taken by mouth that contains a dietary ingredient intended to supplement the diet. Dietary ingredients may include vitamins, minerals, herbs or other botanicals, amino acids, and substances such as enzymes, organ tissues, glandulars and metabolites. 
     */
    public static final URI DietarySupplement;

    /**
     * Properties that take Distances as values are of the form '<Number> <Length unit of measure>'. E.g., '7 ft' 
     */
    public static final URI Distance;

    /**
     * A specific dosing schedule for a drug or supplement. 
     */
    public static final URI DoseSchedule;

    /**
     * A chemical or biologic substance, used as a medical therapy, that has a physiological effect on an organism. 
     */
    public static final URI Drug;

    /**
     * A class of medical drugs, e.g., statins. Classes can represent general pharmacological class, common mechanisms of action, common physiological effects, etc. 
     */
    public static final URI DrugClass;

    /**
     * The cost per unit of a medical drug. Note that this type is not meant to represent the price in an offer of a drug for sale; see the Offer type for that. This type will typically be used to tag wholesale or average retail cost of a drug, or maximum reimbursable cost. Costs of medical drugs vary widely depending on how and where they are paid for, so while this type captures some of the variables, costs should be used with caution by consumers of this schema's markup. 
     */
    public static final URI DrugCost;

    /**
     * Enumerated categories of medical drug costs. 
     */
    public static final URI DrugCostCategory;

    /**
     * The legal availability status of a medical drug. 
     */
    public static final URI DrugLegalStatus;

    /**
     * Categories that represent an assessment of the risk of fetal injury due to a drug or pharmaceutical used as directed by the mother during pregnancy. 
     */
    public static final URI DrugPregnancyCategory;

    /**
     * Indicates whether this drug is available by prescription or over-the-counter. 
     */
    public static final URI DrugPrescriptionStatus;

    /**
     * A specific strength in which a medical drug is available in a specific country. 
     */
    public static final URI DrugStrength;

    /**
     * A dry-cleaning business. 
     */
    public static final URI DryCleaningOrLaundry;

    /**
     * Quantity: Duration (use  ISO 8601 duration format). 
     */
    public static final URI Duration;

    /**
     * Event type: Education event. 
     */
    public static final URI EducationEvent;

    /**
     * An educational organization. 
     */
    public static final URI EducationalOrganization;

    /**
     * An electrician. 
     */
    public static final URI Electrician;

    /**
     * An electronics store. 
     */
    public static final URI ElectronicsStore;

    /**
     * An elementary school. 
     */
    public static final URI ElementarySchool;

    /**
     * An embassy. 
     */
    public static final URI Embassy;

    /**
     * An emergency service, such as a fire station or ER. 
     */
    public static final URI EmergencyService;

    /**
     * An employment agency. 
     */
    public static final URI EmploymentAgency;

    /**
     * Properties that take Enerygy as values are of the form '<Number> <Energy unit of measure>' 
     */
    public static final URI Energy;

    /**
     * A business providing entertainment. 
     */
    public static final URI EntertainmentBusiness;

    /**
     * Lists or enumerations—for example, a list of cuisines or music genres, etc. 
     */
    public static final URI Enumeration;

    /**
     * An event happening at a certain time at a certain location. 
     */
    public static final URI Event;

    /**
     * An event venue. 
     */
    public static final URI EventVenue;

    /**
     * A gym. 
     */
    public static final URI ExerciseGym;

    /**
     * Fitness-related activity designed for a specific health-related purpose, including defined exercise routines as well as activity prescribed by a clinician. 
     */
    public static final URI ExercisePlan;

    /**
     * A fast-food restaurant. 
     */
    public static final URI FastFoodRestaurant;

    /**
     * Event type: Festival. 
     */
    public static final URI Festival;

    /**
     * Financial services business. 
     */
    public static final URI FinancialService;

    /**
     * A fire station. With firemen. 
     */
    public static final URI FireStation;

    /**
     * A florist. 
     */
    public static final URI Florist;

    /**
     * A food-related business. 
     */
    public static final URI FoodEstablishment;

    /**
     * Event type: Food event. 
     */
    public static final URI FoodEvent;

    /**
     * A furniture store. 
     */
    public static final URI FurnitureStore;

    /**
     * A garden store. 
     */
    public static final URI GardenStore;

    /**
     * A gas station. 
     */
    public static final URI GasStation;

    /**
     * Residence type: Gated community. 
     */
    public static final URI GatedResidenceCommunity;

    /**
     * A general contractor. 
     */
    public static final URI GeneralContractor;

    /**
     * The geographic coordinates of a place or event. 
     */
    public static final URI GeoCoordinates;

    /**
     * The geographic shape of a place. 
     */
    public static final URI GeoShape;

    /**
     * A golf course. 
     */
    public static final URI GolfCourse;

    /**
     * A government building. 
     */
    public static final URI GovernmentBuilding;

    /**
     * A government office—for example, an IRS or DMV office. 
     */
    public static final URI GovernmentOffice;

    /**
     * A governmental organization or agency. 
     */
    public static final URI GovernmentOrganization;

    /**
     * A grocery store. 
     */
    public static final URI GroceryStore;

    /**
     * An HVAC service. 
     */
    public static final URI HVACBusiness;

    /**
     * A hair salon. 
     */
    public static final URI HairSalon;

    /**
     * A hardware store. 
     */
    public static final URI HardwareStore;

    /**
     * Health and beauty. 
     */
    public static final URI HealthAndBeautyBusiness;

    /**
     * A health club. 
     */
    public static final URI HealthClub;

    /**
     * A high school. 
     */
    public static final URI HighSchool;

    /**
     * A Hindu temple. 
     */
    public static final URI HinduTemple;

    /**
     * A hobby store. 
     */
    public static final URI HobbyShop;

    /**
     * A construction business. 
     */
    public static final URI HomeAndConstructionBusiness;

    /**
     * A home goods store. 
     */
    public static final URI HomeGoodsStore;

    /**
     * A hospital. 
     */
    public static final URI Hospital;

    /**
     * A hostel. 
     */
    public static final URI Hostel;

    /**
     * A hotel. 
     */
    public static final URI Hotel;

    /**
     * A house painting service. 
     */
    public static final URI HousePainter;

    /**
     * An ice cream shop 
     */
    public static final URI IceCreamShop;

    /**
     * Web page type: Image gallery page. 
     */
    public static final URI ImageGallery;

    /**
     * An image file. 
     */
    public static final URI ImageObject;

    /**
     * Any medical imaging modality typically used for diagnostic purposes. 
     */
    public static final URI ImagingTest;

    /**
     * A single, identifiable product instance (e.g. a laptop with a particular serial number). 
     */
    public static final URI IndividualProduct;

    /**
     * Classes of agents or pathogens that transmit infectious diseases. Enumerated type. 
     */
    public static final URI InfectiousAgentClass;

    /**
     * An infectious disease is a clinically evident human disease resulting from the presence of pathogenic microbial agents, like pathogenic viruses, pathogenic bacteria, fungi, protozoa, multicellular parasites, and prions. To be considered an infectious disease, such pathogens are known to be able to cause this disease. 
     */
    public static final URI InfectiousDisease;

    /**
     * Insurance agency. 
     */
    public static final URI InsuranceAgency;

    /**
     * A utility class that serves as the umbrella for a number of 'intangible' things such as quantities, structured values, etc. 
     */
    public static final URI Intangible;

    /**
     * An internet cafe. 
     */
    public static final URI InternetCafe;

    /**
     * A list of possible product availablity options. 
     */
    public static final URI ItemAvailability;

    /**
     * A list of items of any sort—for example, Top 10 Movies About Weathermen, or Top 100 Party Songs. Not to be confused with HTML lists, which are often used only for formatting. 
     */
    public static final URI ItemList;

    /**
     * A page devoted to a single item, such as a particular product or hotel. 
     */
    public static final URI ItemPage;

    /**
     * A jewelry store. 
     */
    public static final URI JewelryStore;

    /**
     * A listing that describes a job opening in a certain organization. 
     */
    public static final URI JobPosting;

    /**
     * The anatomical location at which two or more bones make contact. 
     */
    public static final URI Joint;

    /**
     * A lake (for example, Lake Pontrachain). 
     */
    public static final URI LakeBodyOfWater;

    /**
     * A landform or physical feature.  Landform elements include mountains, plains, lakes, rivers, seascape and oceanic waterbody interface features such as bays, peninsulas, seas and so forth, including sub-aqueous terrain features such as submersed mountain ranges, volcanoes, and the great ocean basins. 
     */
    public static final URI Landform;

    /**
     * An historical landmark or building. 
     */
    public static final URI LandmarksOrHistoricalBuildings;

    /**
     * Natural languages such as Spanish, Tamil, Hindi, English, etc. and programming languages such as Scheme and Lisp. 
     */
    public static final URI Language;

    /**
     * A legislative building—for example, the state capitol. 
     */
    public static final URI LegislativeBuilding;

    /**
     * A library. 
     */
    public static final URI Library;

    /**
     * A process of care involving exercise, changes to diet, fitness routines, and other lifestyle changes aimed at improving a health condition. 
     */
    public static final URI LifestyleModification;

    /**
     * A short band of tough, flexible, fibrous connective tissue that functions to connect multiple bones, cartilages, and structurally support joints. 
     */
    public static final URI Ligament;

    /**
     * A liquor store. 
     */
    public static final URI LiquorStore;

    /**
     * Event type: Literary event. 
     */
    public static final URI LiteraryEvent;

    /**
     * A particular physical business or branch of an organization. Examples of LocalBusiness include a restaurant, a particular branch of a restaurant chain, a branch of a bank, a medical practice, a club, a bowling alley, etc. 
     */
    public static final URI LocalBusiness;

    /**
     * A locksmith. 
     */
    public static final URI Locksmith;

    /**
     * A lodging business, such as a motel, hotel, or inn. 
     */
    public static final URI LodgingBusiness;

    /**
     * A type of blood vessel that specifically carries lymph fluid unidirectionally toward the heart. 
     */
    public static final URI LymphaticVessel;

    /**
     * A map. 
     */
    public static final URI Map;

    /**
     * Properties that take Mass as values are of the form '<Number> <Mass unit of measure>'. E.g., '7 kg' 
     */
    public static final URI Mass;

    /**
     * The maximum dosing schedule considered safe for a drug or supplement as recommended by an authority or by the drug/supplement's manufacturer. Capture the recommending authority in the recognizingAuthority property of MedicalEntity. 
     */
    public static final URI MaximumDoseSchedule;

    /**
     * An image, video, or audio object embedded in a web page. Note that a creative work may have many media objects associated with it on the same web page. For example, a page about a single song (MusicRecording) may have a music video (VideoObject), and a high and low bandwidth audio stream (2 AudioObject's). 
     */
    public static final URI MediaObject;

    /**
     * Target audiences for medical web pages. Enumerated type. 
     */
    public static final URI MedicalAudience;

    /**
     * The causative agent(s) that are responsible for the pathophysiologic process that eventually results in a medical condition, symptom or sign. In this schema, unless otherwise specified this is meant to be the proximate cause of the medical condition, symptom or sign. The proximate cause is defined as the causative agent that most directly results in the medical condition, symptom or sign. For example, the HIV virus could be considered a cause of AIDS. Or in a diagnostic context, if a patient fell and sustained a hip fracture and two days later sustained a pulmonary embolism which eventuated in a cardiac arrest, the cause of the cardiac arrest (the proximate cause) would be the pulmonary embolism and not the fall. Medical causes can include cardiovascular, chemical, dermatologic, endocrine, environmental, gastroenterologic, genetic, hematologic, gynecologic, iatrogenic, infectious, musculoskeletal, neurologic, nutritional, obstetric, oncologic, otolaryngologic, pharmacologic, psychiatric, pulmonary, renal, rheumatologic, toxic, traumatic, or urologic causes; medical conditions can be causes as well. 
     */
    public static final URI MedicalCause;

    /**
     * A medical clinic. 
     */
    public static final URI MedicalClinic;

    /**
     * A code for a medical entity. 
     */
    public static final URI MedicalCode;

    /**
     * Any condition of the human body that affects the normal functioning of a person, whether physically or mentally. Includes diseases, injuries, disabilities, disorders, syndromes, etc. 
     */
    public static final URI MedicalCondition;

    /**
     * A stage of a medical condition, such as 'Stage IIIa'. 
     */
    public static final URI MedicalConditionStage;

    /**
     * A condition or factor that serves as a reason to withhold a certain medical therapy. Contraindications can be absolute (there are no reasonable circumstances for undertaking a course of action) or relative (the patient is at higher risk of complications, but that these risks may be outweighed by other considerations or mitigated by other measures). 
     */
    public static final URI MedicalContraindication;

    /**
     * Any object used in a medical capacity, such as to diagnose or treat a patient. 
     */
    public static final URI MedicalDevice;

    /**
     * Categories of medical devices, organized by the purpose or intended use of the device. 
     */
    public static final URI MedicalDevicePurpose;

    /**
     * The most generic type of entity related to health and the practice of medicine. 
     */
    public static final URI MedicalEntity;

    /**
     * Enumerations related to health and the practice of medicine. 
     */
    public static final URI MedicalEnumeration;

    /**
     * Level of evidence for a medical guideline. Enumerated type. 
     */
    public static final URI MedicalEvidenceLevel;

    /**
     * Any recommendation made by a standard society (e.g. ACC/AHA) or consensus statement that denotes how to diagnose and treat a particular condition. Note: this type should be used to tag the actual guideline recommendation; if the guideline recommendation occurs in a larger scholarly article, use MedicalScholarlyArticle to tag the overall article, not this type. Note also: the organization making the recommendation should be captured in the recognizingAuthority base property of MedicalEntity. 
     */
    public static final URI MedicalGuideline;

    /**
     * A guideline contraindication that designates a process as harmful and where quality of the data supporting the contraindication is sound. 
     */
    public static final URI MedicalGuidelineContraindication;

    /**
     * A guideline recommendation that is regarded as efficacious and where quality of the data supporting the recommendation is sound. 
     */
    public static final URI MedicalGuidelineRecommendation;

    /**
     * Any medical imaging modality typically used for diagnostic purposes. Enumerated type. 
     */
    public static final URI MedicalImagingTechnique;

    /**
     * A condition or factor that indicates use of a medical therapy, including signs, symptoms, risk factors, anatomical states, etc. 
     */
    public static final URI MedicalIndication;

    /**
     * A utility class that serves as the umbrella for a number of 'intangible' things in the medical space. 
     */
    public static final URI MedicalIntangible;

    /**
     * An observational study is a type of medical study that attempts to infer the possible effect of a treatment through observation of a cohort of subjects over a period of time. In an observational study, the assignment of subjects into treatment groups versus control groups is outside the control of the investigator. This is in contrast with controlled studies, such as the randomized controlled trials represented by MedicalTrial, where each subject is randomly assigned to a treatment group or a control group before the start of the treatment. 
     */
    public static final URI MedicalObservationalStudy;

    /**
     * Design models for observational medical studies. Enumerated type. 
     */
    public static final URI MedicalObservationalStudyDesign;

    /**
     * A medical organization, such as a doctor's office or clinic. 
     */
    public static final URI MedicalOrganization;

    /**
     * A process of care used in either a diagnostic, therapeutic, or palliative capacity that relies on invasive (surgical), non-invasive, or percutaneous techniques. 
     */
    public static final URI MedicalProcedure;

    /**
     * An enumeration that describes different types of medical procedures. 
     */
    public static final URI MedicalProcedureType;

    /**
     * A complex mathematical calculation requiring an online calculator, used to assess prognosis. Note: use the url property of Thing to record any URLs for online calculators. 
     */
    public static final URI MedicalRiskCalculator;

    /**
     * Any rule set or interactive tool for estimating the risk of developing a complication or condition. 
     */
    public static final URI MedicalRiskEstimator;

    /**
     * A risk factor is anything that increases a person's likelihood of developing or contracting a disease, medical condition, or complication. 
     */
    public static final URI MedicalRiskFactor;

    /**
     * A simple system that adds up the number of risk factors to yield a score that is associated with prognosis, e.g. CHAD score, TIMI risk score. 
     */
    public static final URI MedicalRiskScore;

    /**
     * A scholarly article in the medical domain. 
     */
    public static final URI MedicalScholarlyArticle;

    /**
     * Any physical manifestation of a person's medical condition discoverable by objective diagnostic tests or physical examination. 
     */
    public static final URI MedicalSign;

    /**
     * Any indication of the existence of a medical condition or disease. 
     */
    public static final URI MedicalSignOrSymptom;

    /**
     * Any specific branch of medical science or practice. Medical specialities include clinical specialties that pertain to particular organ systems and their respective disease states, as well as allied health specialties. Enumerated type. 
     */
    public static final URI MedicalSpecialty;

    /**
     * A medical study is an umbrella type covering all kinds of research studies relating to human medicine or health, including observational studies and interventional trials and registries, randomized, controlled or not. When the specific type of study is known, use one of the extensions of this type, such as MedicalTrial or MedicalObservationalStudy. Also, note that this type should be used to mark up data that describes the study itself; to tag an article that publishes the results of a study, use MedicalScholarlyArticle. Note: use the code property of MedicalEntity to store study IDs, e.g. clinicaltrials.gov ID. 
     */
    public static final URI MedicalStudy;

    /**
     * The status of a medical study. Enumerated type. 
     */
    public static final URI MedicalStudyStatus;

    /**
     * Any indication of the existence of a medical condition or disease that is apparent to the patient. 
     */
    public static final URI MedicalSymptom;

    /**
     * Any medical test, typically performed for diagnostic purposes. 
     */
    public static final URI MedicalTest;

    /**
     * Any collection of tests commonly ordered together. 
     */
    public static final URI MedicalTestPanel;

    /**
     * Any medical intervention designed to prevent, treat, and cure human diseases and medical conditions, including both curative and palliative therapies. Medical therapies are typically processes of care relying upon pharmacotherapy, behavioral therapy, supportive therapy (with fluid or nutrition for example), or detoxification (e.g. hemodialysis) aimed at improving or preventing a health condition. 
     */
    public static final URI MedicalTherapy;

    /**
     * A medical trial is a type of medical study that uses scientific process used to compare the safety and efficacy of medical therapies or medical procedures. In general, medical trials are controlled and subjects are allocated at random to the different treatment and/or control groups. 
     */
    public static final URI MedicalTrial;

    /**
     * Design models for medical trials. Enumerated type. 
     */
    public static final URI MedicalTrialDesign;

    /**
     * A web page that provides medical information. 
     */
    public static final URI MedicalWebPage;

    /**
     * Systems of medical practice. 
     */
    public static final URI MedicineSystem;

    /**
     * A men's clothing store. 
     */
    public static final URI MensClothingStore;

    /**
     * A middle school. 
     */
    public static final URI MiddleSchool;

    /**
     * A mobile software application. 
     */
    public static final URI MobileApplication;

    /**
     * A mobile-phone store. 
     */
    public static final URI MobilePhoneStore;

    /**
     * A mosque. 
     */
    public static final URI Mosque;

    /**
     * A motel. 
     */
    public static final URI Motel;

    /**
     * A motorcycle dealer. 
     */
    public static final URI MotorcycleDealer;

    /**
     * A motorcycle repair shop. 
     */
    public static final URI MotorcycleRepair;

    /**
     * A mountain, like Mount Whitney or Mount Everest 
     */
    public static final URI Mountain;

    /**
     * A movie. 
     */
    public static final URI Movie;

    /**
     * A movie rental store. 
     */
    public static final URI MovieRentalStore;

    /**
     * A movie theater. 
     */
    public static final URI MovieTheater;

    /**
     * A moving company. 
     */
    public static final URI MovingCompany;

    /**
     * A muscle is an anatomical structure consisting of a contractile form of tissue that animals use to effect movement. 
     */
    public static final URI Muscle;

    /**
     * A museum. 
     */
    public static final URI Museum;

    /**
     * A collection of music tracks. 
     */
    public static final URI MusicAlbum;

    /**
     * Event type: Music event. 
     */
    public static final URI MusicEvent;

    /**
     * A musical group, such as a band, an orchestra, or a choir. Can also be a solo musician. 
     */
    public static final URI MusicGroup;

    /**
     * A collection of music tracks in playlist form. 
     */
    public static final URI MusicPlaylist;

    /**
     * A music recording (track), usually a single song. 
     */
    public static final URI MusicRecording;

    /**
     * A music store. 
     */
    public static final URI MusicStore;

    /**
     * A music venue. 
     */
    public static final URI MusicVenue;

    /**
     * A music video file. 
     */
    public static final URI MusicVideoObject;

    /**
     * Organization: Non-governmental Organization. 
     */
    public static final URI NGO;

    /**
     * A nail salon. 
     */
    public static final URI NailSalon;

    /**
     * A common pathway for the electrochemical nerve impulses that are transmitted along each of the axons. 
     */
    public static final URI Nerve;

    /**
     * A news article 
     */
    public static final URI NewsArticle;

    /**
     * A nightclub or discotheque. 
     */
    public static final URI NightClub;

    /**
     * A notary. 
     */
    public static final URI Notary;

    /**
     * Nutritional information about the recipe. 
     */
    public static final URI NutritionInformation;

    /**
     * An ocean (for example, the Pacific). 
     */
    public static final URI OceanBodyOfWater;

    /**
     * An offer to sell an item—for example, an offer to sell a product, the DVD of a movie, or tickets to an event. 
     */
    public static final URI Offer;

    /**
     * A list of possible conditions for the item for sale. 
     */
    public static final URI OfferItemCondition;

    /**
     * An office equipment store. 
     */
    public static final URI OfficeEquipmentStore;

    /**
     * A structured value providing information about the opening hours of a place or a certain service inside a place. 
     */
    public static final URI OpeningHoursSpecification;

    /**
     * An optician's store. 
     */
    public static final URI Optician;

    /**
     * An organization such as a school, NGO, corporation, club, etc. 
     */
    public static final URI Organization;

    /**
     * An outlet store. 
     */
    public static final URI OutletStore;

    /**
     * A structured value providing information about when a certain organization or person owned a certain product. 
     */
    public static final URI OwnershipInfo;

    /**
     * A painting. 
     */
    public static final URI Painting;

    /**
     * A medical procedure intended primarly for palliative purposes, aimed at relieving the symptoms of an underlying health condition. 
     */
    public static final URI PalliativeProcedure;

    /**
     * A private parcel service as the delivery mode available for a certain offer.

     Commonly used values:

     http://purl.org/goodrelations/v1#DHL
     http://purl.org/goodrelations/v1#FederalExpress
     http://purl.org/goodrelations/v1#UPS 
     */
    public static final URI ParcelService;

    /**
     * A park. 
     */
    public static final URI Park;

    /**
     * A parking lot or other parking facility. 
     */
    public static final URI ParkingFacility;

    /**
     * A medical test performed by a laboratory that typically involves examination of a tissue sample by a pathologist. 
     */
    public static final URI PathologyTest;

    /**
     * A pawnstore. 
     */
    public static final URI PawnShop;

    /**
     * The costs of settling the payment using a particular payment method. 
     */
    public static final URI PaymentChargeSpecification;

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
    public static final URI PaymentMethod;

    /**
     * A theatre or other performing art center. 
     */
    public static final URI PerformingArtsTheater;

    /**
     * A performance group, such as a band, an orchestra, or a circus. 
     */
    public static final URI PerformingGroup;

    /**
     * A person (alive, dead, undead, or fictional). 
     */
    public static final URI Person;

    /**
     * A pet store. 
     */
    public static final URI PetStore;

    /**
     * A pharmacy or drugstore. 
     */
    public static final URI Pharmacy;

    /**
     * A photograph. 
     */
    public static final URI Photograph;

    /**
     * Any bodily activity that enhances or maintains physical fitness and overall health and wellness. Includes activity that is part of daily living and routine, structured exercise, and exercise prescribed as part of a medical treatment or recovery plan. 
     */
    public static final URI PhysicalActivity;

    /**
     * Categories of physical activity, organized by physiologic classification. 
     */
    public static final URI PhysicalActivityCategory;

    /**
     * A type of physical examination of a patient performed by a physician. Enumerated type. 
     */
    public static final URI PhysicalExam;

    /**
     * A process of progressive physical care and rehabilitation aimed at improving a health condition. 
     */
    public static final URI PhysicalTherapy;

    /**
     * A doctor's office. 
     */
    public static final URI Physician;

    /**
     * Entities that have a somewhat fixed, physical extension. 
     */
    public static final URI Place;

    /**
     * Place of worship, such as a church, synagogue, or mosque. 
     */
    public static final URI PlaceOfWorship;

    /**
     * A playground. 
     */
    public static final URI Playground;

    /**
     * A plumbing service. 
     */
    public static final URI Plumber;

    /**
     * A police station. 
     */
    public static final URI PoliceStation;

    /**
     * A pond 
     */
    public static final URI Pond;

    /**
     * A post office. 
     */
    public static final URI PostOffice;

    /**
     * The mailing address. 
     */
    public static final URI PostalAddress;

    /**
     * A preschool. 
     */
    public static final URI Preschool;

    /**
     * An indication for preventing an underlying condition, symptom, etc. 
     */
    public static final URI PreventionIndication;

    /**
     * A structured value representing a monetary amount. Typically, only the subclasses of this type are used for markup. 
     */
    public static final URI PriceSpecification;

    /**
     * A product is anything that is made available for sale—for example, a pair of shoes, a concert ticket, or a car. Commodity services, like haircuts, can also be represented using this type. 
     */
    public static final URI Product;

    /**
     * A datasheet or vendor specification of a product (in the sense of a prototypical description). 
     */
    public static final URI ProductModel;

    /**
     * Provider of professional services. 
     */
    public static final URI ProfessionalService;

    /**
     * Web page type: Profile page. 
     */
    public static final URI ProfilePage;

    /**
     * A process of care relying upon counseling, dialogue, communication, verbalization aimed at improving a mental health condition. 
     */
    public static final URI PsychologicalTreatment;

    /**
     * A public swimming pool. 
     */
    public static final URI PublicSwimmingPool;

    /**
     * A predefined value for a product characteristic, e.g. the the power cord plug type "US" or the garment sizes "S", "M", "L", and "XL" 
     */
    public static final URI QualitativeValue;

    /**
     * A point value or interval for product characteristics and other purposes. 
     */
    public static final URI QuantitativeValue;

    /**
     * Quantities such as distance, time, mass, weight, etc. Particular instances of say Mass are entities like '3 Kg' or '4 milligrams'. 
     */
    public static final URI Quantity;

    /**
     * An RV park. 
     */
    public static final URI RVPark;

    /**
     * A process of care using radiation aimed at improving a health condition. 
     */
    public static final URI RadiationTherapy;

    /**
     * A radio station. 
     */
    public static final URI RadioStation;

    /**
     * The rating of the video. 
     */
    public static final URI Rating;

    /**
     * A real-estate agent. 
     */
    public static final URI RealEstateAgent;

    /**
     * A recipe. 
     */
    public static final URI Recipe;

    /**
     * A recommended dosing schedule for a drug or supplement as prescribed or recommended by an authority or by the drug/supplement's manufacturer. Capture the recommending authority in the recognizingAuthority property of MedicalEntity. 
     */
    public static final URI RecommendedDoseSchedule;

    /**
     * A recycling center. 
     */
    public static final URI RecyclingCenter;

    /**
     * A patient-reported or observed dosing schedule for a drug or supplement. 
     */
    public static final URI ReportedDoseSchedule;

    /**
     * A reservoir, like the Lake Kariba reservoir. 
     */
    public static final URI Reservoir;

    /**
     * The place where a person lives. 
     */
    public static final URI Residence;

    /**
     * A restaurant. 
     */
    public static final URI Restaurant;

    /**
     * A review of an item - for example, a restaurant, movie, or store. 
     */
    public static final URI Review;

    /**
     * A river (for example, the broad majestic Shannon). 
     */
    public static final URI RiverBodyOfWater;

    /**
     * A roofing contractor. 
     */
    public static final URI RoofingContractor;

    /**
     * Event type: Sales event. 
     */
    public static final URI SaleEvent;

    /**
     * A scholarly article. 
     */
    public static final URI ScholarlyArticle;

    /**
     * A school. 
     */
    public static final URI School;

    /**
     * A piece of sculpture. 
     */
    public static final URI Sculpture;

    /**
     * A sea (for example, the Caspian sea). 
     */
    public static final URI SeaBodyOfWater;

    /**
     * Web page type: Search results page. 
     */
    public static final URI SearchResultsPage;

    /**
     * Self-storage facility. 
     */
    public static final URI SelfStorage;

    /**
     * A shoe store. 
     */
    public static final URI ShoeStore;

    /**
     * A shopping center or mall. 
     */
    public static final URI ShoppingCenter;

    /**
     * Residence type: Single-family home. 
     */
    public static final URI SingleFamilyResidence;

    /**
     * A navigation element of the page. 
     */
    public static final URI SiteNavigationElement;

    /**
     * A ski resort. 
     */
    public static final URI SkiResort;

    /**
     * Event type: Social event. 
     */
    public static final URI SocialEvent;

    /**
     * A software application. 
     */
    public static final URI SoftwareApplication;

    /**
     * A placeholder for multiple similar products of the same kind. 
     */
    public static final URI SomeProducts;

    /**
     * Any branch of a field in which people typically develop specific expertise, usually after significant study, time, and effort. 
     */
    public static final URI Specialty;

    /**
     * A sporting goods store. 
     */
    public static final URI SportingGoodsStore;

    /**
     * A sports location, such as a playing field. 
     */
    public static final URI SportsActivityLocation;

    /**
     * A sports club. 
     */
    public static final URI SportsClub;

    /**
     * Event type: Sports event. 
     */
    public static final URI SportsEvent;

    /**
     * Organization: Sports team. 
     */
    public static final URI SportsTeam;

    /**
     * A stadium. 
     */
    public static final URI StadiumOrArena;

    /**
     * A state or province. 
     */
    public static final URI State;

    /**
     * A retail good store. 
     */
    public static final URI Store;

    /**
     * Structured values are strings—for example, addresses—that have certain constraints on their structure. 
     */
    public static final URI StructuredValue;

    /**
     * A subway station. 
     */
    public static final URI SubwayStation;

    /**
     * Anatomical features that can be observed by sight (without dissection), including the form and proportions of the human body as well as surface landmarks that correspond to deeper subcutaneous structures. Superficial anatomy plays an important role in sports medicine, phlebotomy, and other medical specialties as underlying anatomical structures can be identified through surface palpation. For example, during back surgery, superficial anatomy can be used to palpate and count vertebrae to find the site of incision. Or in phlebotomy, superficial anatomy can be used to locate an underlying vein; for example, the median cubital vein can be located by palpating the borders of the cubital fossa (such as the epicondyles of the humerus) and then looking for the superficial signs of the vein, such as size, prominence, ability to refill after depression, and feel of surrounding tissue support. As another example, in a subluxation (dislocation) of the glenohumeral joint, the bony structure becomes pronounced with the deltoid muscle failing to cover the glenohumeral joint allowing the edges of the scapula to be superficially visible. Here, the superficial anatomy is the visible edges of the scapula, implying the underlying dislocation of the joint (the related anatomical structure). 
     */
    public static final URI SuperficialAnatomy;

    /**
     * A synagogue. 
     */
    public static final URI Synagogue;

    /**
     * An episode of a TV series or season. 
     */
    public static final URI TVEpisode;

    /**
     * A TV season. 
     */
    public static final URI TVSeason;

    /**
     * A television series. 
     */
    public static final URI TVSeries;

    /**
     * A table on the page. 
     */
    public static final URI Table;

    /**
     * A tattoo parlor. 
     */
    public static final URI TattooParlor;

    /**
     * A taxi stand. 
     */
    public static final URI TaxiStand;

    /**
     * A television station. 
     */
    public static final URI TelevisionStation;

    /**
     * A tennis complex. 
     */
    public static final URI TennisComplex;

    /**
     * Event type: Theater performance. 
     */
    public static final URI TheaterEvent;

    /**
     * A theater group or company—for example, the Royal Shakespeare Company or Druid Theatre. 
     */
    public static final URI TheaterGroup;

    /**
     * A medical procedure intended primarly for therapeutic purposes, aimed at improving a health condition. 
     */
    public static final URI TherapeuticProcedure;

    /**
     * The most generic type of item. 
     */
    public static final URI Thing;

    /**
     * A tire shop. 
     */
    public static final URI TireShop;

    /**
     * A tourist attraction. 
     */
    public static final URI TouristAttraction;

    /**
     * A tourist information center. 
     */
    public static final URI TouristInformationCenter;

    /**
     * A toystore. 
     */
    public static final URI ToyStore;

    /**
     * A train station. 
     */
    public static final URI TrainStation;

    /**
     * A travel agency. 
     */
    public static final URI TravelAgency;

    /**
     * An indication for treating an underlying condition, symptom, etc. 
     */
    public static final URI TreatmentIndication;

    /**
     * A structured value indicating the quantity, unit of measurement, and business function of goods included in a bundle offer. 
     */
    public static final URI TypeAndQuantityNode;

    /**
     * The price asked for a given offer by the respective organization or person. 
     */
    public static final URI UnitPriceSpecification;

    /**
     * User interaction: Block this content. 
     */
    public static final URI UserBlocks;

    /**
     * User interaction: Check-in at a place. 
     */
    public static final URI UserCheckins;

    /**
     * The UserInteraction event in which a user comments on an item. 
     */
    public static final URI UserComments;

    /**
     * User interaction: Download of an item. 
     */
    public static final URI UserDownloads;

    /**
     * A user interacting with a page 
     */
    public static final URI UserInteraction;

    /**
     * User interaction: Like an item. 
     */
    public static final URI UserLikes;

    /**
     * User interaction: Visit to a web page. 
     */
    public static final URI UserPageVisits;

    /**
     * User interaction: Play count of an item, for example a video or a song. 
     */
    public static final URI UserPlays;

    /**
     * User interaction: +1. 
     */
    public static final URI UserPlusOnes;

    /**
     * User interaction: Tweets. 
     */
    public static final URI UserTweets;

    /**
     * A type of blood vessel that specifically carries blood to the heart. 
     */
    public static final URI Vein;

    /**
     * A component of the human body circulatory system comprised of an intricate network of hollow tubes that transport blood throughout the entire body. 
     */
    public static final URI Vessel;

    /**
     * A vet's office. 
     */
    public static final URI VeterinaryCare;

    /**
     * Web page type: Video gallery page. 
     */
    public static final URI VideoGallery;

    /**
     * A video file. 
     */
    public static final URI VideoObject;

    /**
     * Event type: Visual arts event. 
     */
    public static final URI VisualArtsEvent;

    /**
     * A volcano, like Fuji san 
     */
    public static final URI Volcano;

    /**
     * An advertising section of the page. 
     */
    public static final URI WPAdBlock;

    /**
     * The footer section of the page. 
     */
    public static final URI WPFooter;

    /**
     * The header section of the page. 
     */
    public static final URI WPHeader;

    /**
     * A sidebar section of the page. 
     */
    public static final URI WPSideBar;

    /**
     * A structured value representing the duration and scope of services that will be provided to a customer free of charge in case of a defect or malfunction of a product. 
     */
    public static final URI WarrantyPromise;

    /**
     * A range of of services that will be provided to a customer free of charge in case of a defect or malfunction of a product.

     Commonly used values:

     http://purl.org/goodrelations/v1#Labor-BringIn
     http://purl.org/goodrelations/v1#PartsAndLabor-BringIn
     http://purl.org/goodrelations/v1#PartsAndLabor-PickUp 
     */
    public static final URI WarrantyScope;

    /**
     * A waterfall, like Niagara 
     */
    public static final URI Waterfall;

    /**
     * Web applications. 
     */
    public static final URI WebApplication;

    /**
     * A web page. Every web page is implicitly assumed to be declared to be of type WebPage, so the various properties about that webpage, such as breadcrumb may be used. We recommend explicit declaration if these properties are specified, but if they are found outside of an itemscope, they will be assumed to be about the page 
     */
    public static final URI WebPage;

    /**
     * A web page element, like a table or an image 
     */
    public static final URI WebPageElement;

    /**
     * A wholesale store. 
     */
    public static final URI WholesaleStore;

    /**
     * A winery. 
     */
    public static final URI Winery;

    /**
     * A zoo. 
     */
    public static final URI Zoo;

    /**
     * The subject matter of the content. 
     */
    public static final URI about;

    /**
     * The payment method(s) accepted by seller for this offer. 
     */
    public static final URI acceptedPaymentMethod;

    /**
     * Either Yes/No, or a URL at which reservations can be made. 
     */
    public static final URI acceptsReservations;

    /**
     * Specifies the Person that is legally accountable for the CreativeWork. 
     */
    public static final URI accountablePerson;

    /**
     * The organization or person from which the product was acquired. 
     */
    public static final URI acquiredFrom;

    /**
     * The movement the muscle generates. 
     */
    public static final URI action;

    /**
     * An active ingredient, typically chemical compounds and/or biologic substances. 
     */
    public static final URI activeIngredient;

    /**
     * Length of time to engage in the activity. 
     */
    public static final URI activityDuration;

    /**
     * How often one should engage in the activity. 
     */
    public static final URI activityFrequency;

    /**
     * A cast member of the movie, TV series, season, or episode, or video. 
     */
    public static final URI actor;

    /**
     * A cast member of the movie, TV series, season, or episode, or video. (legacy spelling; see singular form, actor) 
     */
    public static final URI actors;

    /**
     * An additional offer that can only be obtained in combination with the first base offer (e.g. supplements and extensions that are available for a surcharge). 
     */
    public static final URI addOn;

    /**
     * An additional name for a Person, can be used for a middle name. 
     */
    public static final URI additionalName;

    /**
     * An additional type for the item, typically used for adding more specific types from external vocabularies in microdata syntax. This is a relationship between something and a class that the thing is in. In RDFa syntax, it is better to use the native RDFa syntax - the 'typeof' attribute - for multiple types. Schema.org tools may have only weaker understanding of extra types, in particular those defined externally. 
     */
    public static final URI additionalType;

    /**
     * Any additional component of the exercise prescription that may need to be articulated to the patient. This may include the order of exercises, the number of repetitions of movement, quantitative distance, progressions over time, etc. 
     */
    public static final URI additionalVariable;

    /**
     * Physical address of the item. 
     */
    public static final URI address;

    /**
     * The country. For example, USA. You can also provide the two-letter ISO 3166-1 alpha-2 country code. 
     */
    public static final URI addressCountry;

    /**
     * The locality. For example, Mountain View. 
     */
    public static final URI addressLocality;

    /**
     * The region. For example, CA. 
     */
    public static final URI addressRegion;

    /**
     * A route by which this drug may be administered, e.g. 'oral'. 
     */
    public static final URI administrationRoute;

    /**
     * The amount of time that is required between accepting the offer and the actual usage of the resource or service. 
     */
    public static final URI advanceBookingRequirement;

    /**
     * A possible complication and/or side effect of this therapy. If it is known that an adverse outcome is serious (resulting in death, disability, or permanent damage; requiring hospitalization; or is otherwise life-threatening or requires immediate medical attention), tag it as a seriouseAdverseOutcome instead. 
     */
    public static final URI adverseOutcome;

    /**
     * Drugs that affect the test's results. 
     */
    public static final URI affectedBy;

    /**
     * An organization that this person is affiliated with. For example, a school/university, a club, or a team. 
     */
    public static final URI affiliation;

    /**
     * The overall rating, based on a collection of reviews or ratings, of the item. 
     */
    public static final URI aggregateRating;

    /**
     * A music album. 
     */
    public static final URI album;

    /**
     * A collection of music albums (legacy spelling; see singular form, album). 
     */
    public static final URI albums;

    /**
     * Any precaution, guidance, contraindication, etc. related to consumption of alcohol while taking this drug. 
     */
    public static final URI alcoholWarning;

    /**
     * The algorithm or rules to follow to compute the score. 
     */
    public static final URI algorithm;

    /**
     * Any alternate name for this medical entity. 
     */
    public static final URI alternateName;

    /**
     * A secondary title of the CreativeWork. 
     */
    public static final URI alternativeHeadline;

    /**
     * Alumni of educational organization. 
     */
    public static final URI alumni;

    /**
     * An educational organizations that the person is an alumni of. 
     */
    public static final URI alumniOf;

    /**
     * The quantity of the goods included in the offer. 
     */
    public static final URI amountOfThisGood;

    /**
     * The muscle whose action counteracts the specified muscle. 
     */
    public static final URI antagonist;

    /**
     * The location in which the status applies. 
     */
    public static final URI applicableLocation;

    /**
     * Type of software application, e.g. "Game, Multimedia". 
     */
    public static final URI applicationCategory;

    /**
     * Subcategory of the application, e.g. "Arcade Game". 
     */
    public static final URI applicationSubCategory;

    /**
     * The name of the application suite to which the application belongs (e.g. Excel belongs to Office) 
     */
    public static final URI applicationSuite;

    /**
     * The delivery method(s) to which the delivery charge or payment charge specification applies. 
     */
    public static final URI appliesToDeliveryMethod;

    /**
     * The payment method(s) to which the payment charge specification applies. 
     */
    public static final URI appliesToPaymentMethod;

    /**
     * The branches that comprise the arterial structure. 
     */
    public static final URI arterialBranch;

    /**
     * The actual body of the article. 
     */
    public static final URI articleBody;

    /**
     * Articles may belong to one or more 'sections' in a magazine or newspaper, such as Sports, Lifestyle, etc. 
     */
    public static final URI articleSection;

    /**
     * An aspect of medical practice that is considered on the page, such as 'diagnosis', 'treatment', 'causes', 'prognosis', 'etiology', 'epidemiology', etc. 
     */
    public static final URI aspect;

    /**
     * The anatomy of the underlying organ system or structures associated with this entity. 
     */
    public static final URI associatedAnatomy;

    /**
     * A NewsArticle associated with the Media Object. 
     */
    public static final URI associatedArticle;

    /**
     * The media objects that encode this creative work. This property is a synonym for encodings. 
     */
    public static final URI associatedMedia;

    /**
     * If applicable, a description of the pathophysiology associated with the anatomical system, including potential abnormal changes in the mechanical, physical, and biochemical functions of the system. 
     */
    public static final URI associatedPathophysiology;

    /**
     * A person or organization attending the event. 
     */
    public static final URI attendee;

    /**
     * A person attending the event (legacy spelling; see singular form, attendee). 
     */
    public static final URI attendees;

    /**
     * The intended audience of the work, i.e. the group for whom the work was created. 
     */
    public static final URI audience;

    /**
     * An embedded audio object. 
     */
    public static final URI audio;

    /**
     * The author of this content. Please note that author is special in that HTML 5 provides a special mechanism for indicating authorship via the rel tag. That is equivalent to this and may be used interchangeably. 
     */
    public static final URI author;

    /**
     * The availability of this item—for example In stock, Out of stock, Pre-order, etc. 
     */
    public static final URI availability;

    /**
     * The end of the availability of the product or service included in the offer. 
     */
    public static final URI availabilityEnds;

    /**
     * The beginning of the availability of the product or service included in the offer. 
     */
    public static final URI availabilityStarts;

    /**
     * The place(s) from which the offer can be obtained (e.g. store locations). 
     */
    public static final URI availableAtOrFrom;

    /**
     * The delivery method(s) available for this offer. 
     */
    public static final URI availableDeliveryMethod;

    /**
     * The location in which the strength is available. 
     */
    public static final URI availableIn;

    /**
     * A medical service available from this provider. 
     */
    public static final URI availableService;

    /**
     * An available dosage strength for the drug. 
     */
    public static final URI availableStrength;

    /**
     * A diagnostic test or procedure offered by this lab. 
     */
    public static final URI availableTest;

    /**
     * An award won by this person or for this creative work. 
     */
    public static final URI award;

    /**
     * Awards won by this person or for this creative work. (legacy spelling; see singular form, award) 
     */
    public static final URI awards;

    /**
     * Descriptive information establishing a historical perspective on the supplement. May include the rationale for the name, the population where the supplement first came to prominence, etc. 
     */
    public static final URI background;

    /**
     * The base salary of the job. 
     */
    public static final URI baseSalary;

    /**
     * Description of benefits associated with the job. 
     */
    public static final URI benefits;

    /**
     * The highest value allowed in this rating system. If bestRating is omitted, 5 is assumed. 
     */
    public static final URI bestRating;

    /**
     * This property specifies the minimal quantity and rounding increment that will be the basis for the billing. The unit of measurement is specified by the unitCode property. 
     */
    public static final URI billingIncrement;

    /**
     * The biomechanical properties of the bone. 
     */
    public static final URI biomechnicalClass;

    /**
     * Date of birth. 
     */
    public static final URI birthDate;

    /**
     * The bitrate of the media object. 
     */
    public static final URI bitrate;

    /**
     * A posting that is part of this blog. 
     */
    public static final URI blogPost;

    /**
     * The postings that are part of this blog (legacy spelling; see singular form, blogPost). 
     */
    public static final URI blogPosts;

    /**
     * The blood vessel that carries blood from the heart to the muscle. 
     */
    public static final URI bloodSupply;

    /**
     * Location in the body of the anatomical structure. 
     */
    public static final URI bodyLocation;

    /**
     * The edition of the book. 
     */
    public static final URI bookEdition;

    /**
     * The format of the book. 
     */
    public static final URI bookFormat;

    /**
     * A polygon is the area enclosed by a point-to-point path for which the starting and ending points are the same. A polygon is expressed as a series of four or more spacedelimited points where the first and final points are identical. 
     */
    public static final URI box;

    /**
     * The branches that delineate from the nerve bundle. 
     */
    public static final URI branch;

    /**
     * The larger organization that this local business is a branch of, if any. 
     */
    public static final URI branchOf;

    /**
     * The brand(s) associated with a product or service, or the brand(s) maintained by an organization or business person. 
     */
    public static final URI brand;

    /**
     * A set of links that can help a user understand and navigate a website hierarchy. 
     */
    public static final URI breadcrumb;

    /**
     * Any precaution, guidance, contraindication, etc. related to this drug's use by breastfeeding mothers. 
     */
    public static final URI breastfeedingWarning;

    /**
     * Specifies browser requirements in human-readable text. For example,"requires HTML5 support". 
     */
    public static final URI browserRequirements;

    /**
     * The business function (e.g. sell, lease, repair, dispose) of the offer or component of a bundle (TypeAndQuantityNode). The default is http://purl.org/goodrelations/v1#Sell. 
     */
    public static final URI businessFunction;

    /**
     * The artist that performed this album or recording. 
     */
    public static final URI byArtist;

    /**
     * The number of calories 
     */
    public static final URI calories;

    /**
     * The caption for this object. 
     */
    public static final URI caption;

    /**
     * The number of grams of carbohydrates. 
     */
    public static final URI carbohydrateContent;

    /**
     * Specifies specific carrier(s) requirements for the application (e.g. an application may only work on a specific carrier network). 
     */
    public static final URI carrierRequirements;

    /**
     * A category for the item. Greater signs or slashes can be used to informally indicate a category hierarchy. 
     */
    public static final URI category;

    /**
     * An underlying cause. More specifically, one of the causative agent(s) that are most directly responsible for the pathophysiologic process that eventually results in the occurrence. 
     */
    public static final URI cause;

    /**
     * The condition, complication, symptom, sign, etc. caused. 
     */
    public static final URI causeOf;

    /**
     * A child of the person. 
     */
    public static final URI children;

    /**
     * The number of milligrams of cholesterol. 
     */
    public static final URI cholesterolContent;

    /**
     * A circle is the circular region of a specified radius centered at a specified latitude and longitude. A circle is expressed as a pair followed by a radius in meters. 
     */
    public static final URI circle;

    /**
     * A citation or reference to another creative work, such as another publication, web page, scholarly article, etc. NOTE: Candidate for promotion to ScholarlyArticle. 
     */
    public static final URI citation;

    /**
     * Description of the absorption and elimination of drugs, including their concentration (pharmacokinetics, pK) and biological effects (pharmacodynamics, pD). 
     */
    public static final URI clincalPharmacology;

    /**
     * The closing hour of the place or service on the given day(s) of the week. 
     */
    public static final URI closes;

    /**
     * A medical code for the entity, taken from a controlled vocabulary or ontology such as ICD-9, DiseasesDB, MeSH, SNOMED-CT, RxNorm, etc. 
     */
    public static final URI code;

    /**
     * The actual code. 
     */
    public static final URI codeValue;

    /**
     * The coding system, e.g. 'ICD-10'. 
     */
    public static final URI codingSystem;

    /**
     * A colleague of the person. 
     */
    public static final URI colleague;

    /**
     * A colleague of the person (legacy spelling; see singular form, colleague). 
     */
    public static final URI colleagues;

    /**
     * The color of the product. 
     */
    public static final URI color;

    /**
     * Comments, typically from users, on this CreativeWork. 
     */
    public static final URI comment;

    /**
     * The text of the UserComment. 
     */
    public static final URI commentText;

    /**
     * The time at which the UserComment was made. 
     */
    public static final URI commentTime;

    /**
     * The underlying anatomical structures, such as organs, that comprise the anatomical system. 
     */
    public static final URI comprisedOf;

    /**
     * Other anatomical structures to which this structure is connected. 
     */
    public static final URI connectedTo;

    /**
     * A contact point for a person or organization. 
     */
    public static final URI contactPoint;

    /**
     * A contact point for a person or organization (legacy spelling; see singular form, contactPoint). 
     */
    public static final URI contactPoints;

    /**
     * A person or organization can have different contact points, for different purposes. For example, a sales contact point, a PR contact point and so on. This property is used to specify the kind of contact point. 
     */
    public static final URI contactType;

    /**
     * The basic containment relation between places. 
     */
    public static final URI containedIn;

    /**
     * The location of the content. 
     */
    public static final URI contentLocation;

    /**
     * Official rating of a piece of content—for example,'MPAA PG-13'. 
     */
    public static final URI contentRating;

    /**
     * File size in (mega/kilo) bytes. 
     */
    public static final URI contentSize;

    /**
     * Actual bytes of the media object, for example the image file or video file. (previous spelling: contentURL) 
     */
    public static final URI contentUrl;

    /**
     * A contraindication for this therapy. 
     */
    public static final URI contraindication;

    /**
     * A secondary contributor to the CreativeWork. 
     */
    public static final URI contributor;

    /**
     * The time it takes to actually cook the dish, in ISO 8601 duration format. 
     */
    public static final URI cookTime;

    /**
     * The method of cooking, such as Frying, Steaming, ... 
     */
    public static final URI cookingMethod;

    /**
     * The party holding the legal copyright to the CreativeWork. 
     */
    public static final URI copyrightHolder;

    /**
     * The year during which the claimed copyright for the CreativeWork was first asserted. 
     */
    public static final URI copyrightYear;

    /**
     * Cost per unit of the drug, as reported by the source being tagged. 
     */
    public static final URI cost;

    /**
     * The category of cost, such as wholesale, retail, reimbursement cap, etc. 
     */
    public static final URI costCategory;

    /**
     * The currency (in 3-letter ISO 4217 format) of the drug cost. 
     */
    public static final URI costCurrency;

    /**
     * Additional details to capture the origin of the cost data. For example, 'Medicare Part B'. 
     */
    public static final URI costOrigin;

    /**
     * The cost per unit of the drug. 
     */
    public static final URI costPerUnit;

    /**
     * Countries for which the application is not supported. You can also provide the two-letter ISO 3166-1 alpha-2 country code. 
     */
    public static final URI countriesNotSupported;

    /**
     * Countries for which the application is supported. You can also provide the two-letter ISO 3166-1 alpha-2 country code. 
     */
    public static final URI countriesSupported;

    /**
     * The creator/author of this CreativeWork or UserComments. This is the same as the Author property for CreativeWork. 
     */
    public static final URI creator;

    /**
     * The currency accepted (in ISO 4217 currency format). 
     */
    public static final URI currenciesAccepted;

    /**
     * The date on which the CreativeWork was created. 
     */
    public static final URI dateCreated;

    /**
     * The date on which the CreativeWork was most recently modified. 
     */
    public static final URI dateModified;

    /**
     * Publication date for the job posting. 
     */
    public static final URI datePosted;

    /**
     * Date of first broadcast/publication. 
     */
    public static final URI datePublished;

    /**
     * The location where the NewsArticle was produced. 
     */
    public static final URI dateline;

    /**
     * The day of the week for which these opening hours are valid. 
     */
    public static final URI dayOfWeek;

    /**
     * Date of death. 
     */
    public static final URI deathDate;

    /**
     * The typical delay between the receipt of the order and the goods leaving the warehouse. 
     */
    public static final URI deliveryLeadTime;

    /**
     * The depth of the product. 
     */
    public static final URI depth;

    /**
     * A short description of the item. 
     */
    public static final URI description;

    /**
     * Device required to run the application. Used in cases where a specific make/model is required to run the application. 
     */
    public static final URI device;

    /**
     * One or more alternative conditions considered in the differential diagnosis process. 
     */
    public static final URI diagnosis;

    /**
     * An image containing a diagram that illustrates the structure and/or its component substructures and/or connections with other structures. 
     */
    public static final URI diagram;

    /**
     * Nutritional information specific to the dietary plan. May include dietary recommendations on what foods to avoid, what foods to consume, and specific alterations/deviations from the USDA or other regulatory body's approved dietary guidelines. 
     */
    public static final URI dietFeatures;

    /**
     * One of a set of differential diagnoses for the condition. Specifically, a closely-related or competing diagnosis typically considered later in the cognitive process whereby this medical condition is distinguished from others most likely responsible for a similar collection of signs and symptoms to reach the most parsimonious diagnosis or diagnoses in a patient. 
     */
    public static final URI differentialDiagnosis;

    /**
     * The director of the movie, TV episode, or series. 
     */
    public static final URI director;

    /**
     * Specifies the CreativeWork associated with the UserComment. 
     */
    public static final URI discusses;

    /**
     * A link to the page containing the comments of the CreativeWork. 
     */
    public static final URI discussionUrl;

    /**
     * One of a set of signs and symptoms that can be used to distinguish this diagnosis from others in the differential diagnosis. 
     */
    public static final URI distinguishingSign;

    /**
     * A dosage form in which this drug/supplement is available, e.g. 'tablet', 'suspension', 'injection'. 
     */
    public static final URI dosageForm;

    /**
     * A dosing schedule for the drug for a given population, either observed, recommended, or maximum dose based on the type used. 
     */
    public static final URI doseSchedule;

    /**
     * The unit of the dose, e.g. 'mg'. 
     */
    public static final URI doseUnit;

    /**
     * The value of the dose, e.g. 500. 
     */
    public static final URI doseValue;

    /**
     * If the file can be downloaded, URL to download the binary. 
     */
    public static final URI downloadUrl;

    /**
     * The vasculature that the vein drains into. 
     */
    public static final URI drainsTo;

    /**
     * A drug in this drug class. 
     */
    public static final URI drug;

    /**
     * The class of drug this belongs to (e.g., statins). 
     */
    public static final URI drugClass;

    /**
     * The unit in which the drug is measured, e.g. '5 mg tablet'. 
     */
    public static final URI drugUnit;

    /**
     * The Dun & Bradstreet DUNS number for identifying an organization or business person. 
     */
    public static final URI duns;

    /**
     * A therapy that duplicates or overlaps this one. 
     */
    public static final URI duplicateTherapy;

    /**
     * The duration of the item (movie, audio recording, event, etc.) in ISO 8601 date format. 
     */
    public static final URI duration;

    /**
     * The duration of the warranty promise. Common unitCode values are ANN for year, MON for months, or DAY for days. 
     */
    public static final URI durationOfWarranty;

    /**
     * Specifies the Person who edited the CreativeWork. 
     */
    public static final URI editor;

    /**
     * Educational background needed for the position. 
     */
    public static final URI educationRequirements;

    /**
     * The elevation of a location. 
     */
    public static final URI elevation;

    /**
     * The type(s) of customers for which the given offer is valid. 
     */
    public static final URI eligibleCustomerType;

    /**
     * The duration for which the given offer is valid. 
     */
    public static final URI eligibleDuration;

    /**
     * The interval and unit of measurement of ordering quantities for which the offer or price specification is valid. This allows e.g. specifying that a certain freight charge is valid only for a certain quantity. 
     */
    public static final URI eligibleQuantity;

    /**
     * The ISO 3166-1 (ISO 3166-1 alpha-2) or ISO 3166-2 code, or the GeoShape for the geo-political region(s) for which the offer or delivery charge specification is valid. 
     */
    public static final URI eligibleRegion;

    /**
     * The transaction volume, in a monetary unit, for which the offer or price specification is valid, e.g. for indicating a minimal purchasing volume, to express free shipping above a certain order volume, or to limit the acceptance of credit cards to purchases to a certain minimal amount. 
     */
    public static final URI eligibleTransactionVolume;

    /**
     * Email address. 
     */
    public static final URI email;

    /**
     * A URL pointing to a player for a specific video. In general, this is the information in the src element of an embed tag and should not be the same as the content of the loc tag. (previous spelling: embedURL) 
     */
    public static final URI embedUrl;

    /**
     * Someone working for this organization. 
     */
    public static final URI employee;

    /**
     * People working for this organization. (legacy spelling; see singular form, employee) 
     */
    public static final URI employees;

    /**
     * Type of employment (e.g. full-time, part-time, contract, temporary, seasonal, internship). 
     */
    public static final URI employmentType;

    /**
     * The creative work encoded by this media object 
     */
    public static final URI encodesCreativeWork;

    /**
     * A media object that encode this CreativeWork. 
     */
    public static final URI encoding;

    /**
     * mp3, mpeg4, etc. 
     */
    public static final URI encodingFormat;

    /**
     * The media objects that encode this creative work (legacy spelling; see singular form, encoding). 
     */
    public static final URI encodings;

    /**
     * The end date and time of the event (in ISO 8601 date format). 
     */
    public static final URI endDate;

    /**
     * People or organizations that endorse the plan. 
     */
    public static final URI endorsers;

    /**
     * The characteristics of associated patients, such as age, gender, race etc. 
     */
    public static final URI epidemiology;

    /**
     * An episode of a TV series or season. 
     */
    public static final URI episode;

    /**
     * The episode number. 
     */
    public static final URI episodeNumber;

    /**
     * The episode of a TV series or season (legacy spelling; see singular form, episode). 
     */
    public static final URI episodes;

    /**
     * This ordering relation for qualitative values indicates that the subject is equal to the object. 
     */
    public static final URI equal;

    /**
     * The condition, complication, or symptom whose risk is being estimated. 
     */
    public static final URI estimatesRiskOf;

    /**
     * Upcoming or past event associated with this place or organization. 
     */
    public static final URI event;

    /**
     * Upcoming or past events associated with this place or organization (legacy spelling; see singular form, event). 
     */
    public static final URI events;

    /**
     * Strength of evidence of the data used to formulate the guideline (enumerated). 
     */
    public static final URI evidenceLevel;

    /**
     * Source of the data used to formulate the guidance, e.g. RCT, consensus opinion, etc. 
     */
    public static final URI evidenceOrigin;

    /**
     * Type(s) of exercise or activity, such as strength training, flexibility training, aerobics, cardiac rehabilitation, etc. 
     */
    public static final URI exerciseType;

    /**
     * exif data for this object. 
     */
    public static final URI exifData;

    /**
     * The likely outcome in either the short term or long term of the medical condition. 
     */
    public static final URI expectedPrognosis;

    /**
     * Description of skills and experience needed for the position. 
     */
    public static final URI experienceRequirements;

    /**
     * Medical expert advice related to the plan. 
     */
    public static final URI expertConsiderations;

    /**
     * Date the content expires and is no longer useful or available. Useful for videos. 
     */
    public static final URI expires;

    /**
     * Family name. In the U.S., the last name of an Person. This can be used along with givenName instead of the Name property. 
     */
    public static final URI familyName;

    /**
     * The number of grams of fat. 
     */
    public static final URI fatContent;

    /**
     * The fax number. 
     */
    public static final URI faxNumber;

    /**
     * Features or modules provided by this application (and possibly required by other applications). 
     */
    public static final URI featureList;

    /**
     * The number of grams of fiber. 
     */
    public static final URI fiberContent;

    /**
     * MIME format of the binary (e.g. application/zip). 
     */
    public static final URI fileFormat;

    /**
     * Size of the application / package (e.g. 18MB). In the absence of a unit (MB, KB etc.), KB will be assumed. 
     */
    public static final URI fileSize;

    /**
     * The most generic uni-directional social relation. 
     */
    public static final URI follows;

    /**
     * Typical or recommended followup care after the procedure is performed. 
     */
    public static final URI followup;

    /**
     * Any precaution, guidance, contraindication, etc. related to consumption of specific foods while taking this drug. 
     */
    public static final URI foodWarning;

    /**
     * A person who founded this organization. 
     */
    public static final URI founder;

    /**
     * A person who founded this organization (legacy spelling; see singular form, founder). 
     */
    public static final URI founders;

    /**
     * The date that this organization was founded. 
     */
    public static final URI foundingDate;

    /**
     * How often the dose is taken, e.g. 'daily'. 
     */
    public static final URI frequency;

    /**
     * Function of the anatomical structure. 
     */
    public static final URI function;

    /**
     * The degree of mobility the joint allows. 
     */
    public static final URI functionalClass;

    /**
     * Gender of the person. 
     */
    public static final URI gender;

    /**
     * Genre of the creative work 
     */
    public static final URI genre;

    /**
     * The geo coordinates of the place. 
     */
    public static final URI geo;

    /**
     * Given name. In the U.S., the first name of a Person. This can be used along with familyName instead of the Name property. 
     */
    public static final URI givenName;

    /**
     * The Global Location Number (GLN, sometimes also referred to as International Location Number or ILN) of the respective organization, person, or place. The GLN is a 13-digit number used to identify parties and physical locations. 
     */
    public static final URI globalLocationNumber;

    /**
     * This ordering relation for qualitative values indicates that the subject is greater than the object. 
     */
    public static final URI greater;

    /**
     * This ordering relation for qualitative values indicates that the subject is greater than or equal to the object. 
     */
    public static final URI greaterOrEqual;

    /**
     * The GTIN-13 code of the product, or the product to which the offer refers. This is equivalent to 13-digit ISBN codes and EAN UCC-13. Former 12-digit UPC codes can be converted into a GTIN-13 code by simply adding a preceeding zero. 
     */
    public static final URI gtin13;

    /**
     * The GTIN-14 code of the product, or the product to which the offer refers. 
     */
    public static final URI gtin14;

    /**
     * The GTIN-8 code of the product, or the product to which the offer refers. This code is also known as EAN/UCC-8 or 8-digit EAN. 
     */
    public static final URI gtin8;

    /**
     * A medical guideline related to this entity. 
     */
    public static final URI guideline;

    /**
     * Date on which this guideline's recommendation was made. 
     */
    public static final URI guidelineDate;

    /**
     * The medical conditions, treatments, etc. that are the subject of the guideline. 
     */
    public static final URI guidelineSubject;

    /**
     * Points-of-Sales operated by the organization or person. 
     */
    public static final URI hasPOS;

    /**
     * Headline of the article 
     */
    public static final URI headline;

    /**
     * The height of the item. 
     */
    public static final URI height;

    /**
     * The highest price of all offers available. 
     */
    public static final URI highPrice;

    /**
     * Organization offering the job position. 
     */
    public static final URI hiringOrganization;

    /**
     * A contact location for a person's residence. 
     */
    public static final URI homeLocation;

    /**
     * An honorific prefix preceding a Person's name such as Dr/Mrs/Mr. 
     */
    public static final URI honorificPrefix;

    /**
     * An honorific suffix preceding a Person's name such as M.D. /PhD/MSCSW. 
     */
    public static final URI honorificSuffix;

    /**
     * A hospital with which the physician or office is affiliated. 
     */
    public static final URI hospitalAffiliation;

    /**
     * How the procedure is performed. 
     */
    public static final URI howPerformed;

    /**
     * A physical examination that can identify this sign. 
     */
    public static final URI identifyingExam;

    /**
     * A diagnostic test that can identify this sign. 
     */
    public static final URI identifyingTest;

    /**
     * The illustrator of the book. 
     */
    public static final URI illustrator;

    /**
     * URL of an image of the item. 
     */
    public static final URI image;

    /**
     * Imaging technique used. 
     */
    public static final URI imagingTechnique;

    /**
     * The album to which this recording belongs. 
     */
    public static final URI inAlbum;

    /**
     * The language of the content. please use one of the language codes from the IETF BCP 47 standard. 
     */
    public static final URI inLanguage;

    /**
     * The playlist to which this recording belongs. 
     */
    public static final URI inPlaylist;

    /**
     * Description of bonus and commission compensation aspects of the job. 
     */
    public static final URI incentives;

    /**
     * A modifiable or non-modifiable risk factor included in the calculation, e.g. age, coexisting condition. 
     */
    public static final URI includedRiskFactor;

    /**
     * This links to a node or nodes indicating the exact quantity of the products included in the offer. 
     */
    public static final URI includesObject;

    /**
     * The condition, complication, etc. influenced by this factor. 
     */
    public static final URI increasesRiskOf;

    /**
     * A factor that indicates use of this therapy for treatment and/or prevention of a condition, symptom, etc. For therapies such as drugs, indications can include both officially-approved indications as well as off-label uses. These can be distinguished by using the ApprovedIndication subtype of MedicalIndication. 
     */
    public static final URI indication;

    /**
     * The industry associated with the job position. 
     */
    public static final URI industry;

    /**
     * The actual infectious agent, such as a specific bacterium. 
     */
    public static final URI infectiousAgent;

    /**
     * The class of infectious agent (bacteria, prion, etc.) that causes the disease. 
     */
    public static final URI infectiousAgentClass;

    /**
     * An ingredient used in the recipe. 
     */
    public static final URI ingredients;

    /**
     * The place of attachment of a muscle, or what the muscle moves. 
     */
    public static final URI insertion;

    /**
     * URL at which the app may be installed, if different from the URL of the item. 
     */
    public static final URI installUrl;

    /**
     * Quantitative measure gauging the degree of force involved in the exercise, for example, heartbeats per minute. May include the velocity of the movement. 
     */
    public static final URI intensity;

    /**
     * Another drug that is known to interact with this drug in a way that impacts the effect of this drug or causes a risk to the patient. Note: disease interactions are typically captured as contraindications. 
     */
    public static final URI interactingDrug;

    /**
     * A count of a specific user interactions with this item—for example, 20 UserLikes, 5 UserComments, or 300 UserDownloads. The user interaction type should be one of the sub types of UserInteraction. 
     */
    public static final URI interactionCount;

    /**
     * The current approximate inventory level for the item or items. 
     */
    public static final URI inventoryLevel;

    /**
     * A pointer to another product (or multiple products) for which this product is an accessory or spare part. 
     */
    public static final URI isAccessoryOrSparePartFor;

    /**
     * True if the drug is available in a generic form (regardless of name). 
     */
    public static final URI isAvailableGenerically;

    /**
     * A pointer to another product (or multiple products) for which this product is a consumable. 
     */
    public static final URI isConsumableFor;

    /**
     * Indicates whether this content is family friendly. 
     */
    public static final URI isFamilyFriendly;

    /**
     * Indicates the collection or gallery to which the item belongs. 
     */
    public static final URI isPartOf;

    /**
     * True if this item's name is a proprietary/brand name (vs. generic name). 
     */
    public static final URI isProprietary;

    /**
     * A pointer to another, somehow related product (or multiple products). 
     */
    public static final URI isRelatedTo;

    /**
     * A pointer to another, functionally similar product (or multiple products). 
     */
    public static final URI isSimilarTo;

    /**
     * A pointer to a base product from which this product is a variant. It is safe to infer that the variant inherits all product features from the base model, unless defined locally. This is not transitive. 
     */
    public static final URI isVariantOf;

    /**
     * The ISBN of the book. 
     */
    public static final URI isbn;

    /**
     * The International Standard of Industrial Classification of All Economic Activities (ISIC), Revision 4 code for a particular organization, business person, or place. 
     */
    public static final URI isicV4;

    /**
     * A predefined value from OfferItemCondition or a textual description of the condition of the product or service, or the products or services included in the offer. 
     */
    public static final URI itemCondition;

    /**
     * A single list item. 
     */
    public static final URI itemListElement;

    /**
     * Type of ordering (e.g. Ascending, Descending, Unordered). 
     */
    public static final URI itemListOrder;

    /**
     * The item being sold. 
     */
    public static final URI itemOffered;

    /**
     * The item that is being reviewed/rated. 
     */
    public static final URI itemReviewed;

    /**
     * A (typically single) geographic location associated with the job position. 
     */
    public static final URI jobLocation;

    /**
     * The job title of the person (for example, Financial Manager). 
     */
    public static final URI jobTitle;

    /**
     * The keywords/tags used to describe this content. 
     */
    public static final URI keywords;

    /**
     * The most generic bi-directional social/work relation. 
     */
    public static final URI knows;

    /**
     * Link to the drug's label details. 
     */
    public static final URI labelDetails;

    /**
     * Date on which the content on this web page was last reviewed for accuracy and/or completeness. 
     */
    public static final URI lastReviewed;

    /**
     * The latitude of a location. For example 37.42242. 
     */
    public static final URI latitude;

    /**
     * The official name of the organization, e.g. the registered company name. 
     */
    public static final URI legalName;

    /**
     * The drug or supplement's legal status, including any controlled substance schedules that apply. 
     */
    public static final URI legalStatus;

    /**
     * This ordering relation for qualitative values indicates that the subject is lesser than the object. 
     */
    public static final URI lesser;

    /**
     * This ordering relation for qualitative values indicates that the subject is lesser than or equal to the object. 
     */
    public static final URI lesserOrEqual;

    /**
     * A line is a point-to-point path consisting of two or more points. A line is expressed as a series of two or more point objects separated by space. 
     */
    public static final URI line;

    /**
     * The location of the event or organization. 
     */
    public static final URI location;

    /**
     * URL of an image for the logo of the item. 
     */
    public static final URI logo;

    /**
     * The longitude of a location. For example -122.08585. 
     */
    public static final URI longitude;

    /**
     * The lowest price of all offers available. 
     */
    public static final URI lowPrice;

    /**
     * Indicates if this web page element is the main subject of the page. 
     */
    public static final URI mainContentOfPage;

    /**
     * A pointer to products or services offered by the organization or person. 
     */
    public static final URI makesOffer;

    /**
     * The manufacturer of the product. 
     */
    public static final URI manufacturer;

    /**
     * A URL to a map of the place. 
     */
    public static final URI map;

    /**
     * A URL to a map of the place (legacy spelling; see singular form, map). 
     */
    public static final URI maps;

    /**
     * The highest price if the price is a range. 
     */
    public static final URI maxPrice;

    /**
     * The upper of the product characteristic. 
     */
    public static final URI maxValue;

    /**
     * Recommended intake of this supplement for a given population as defined by a specific recommending authority. 
     */
    public static final URI maximumIntake;

    /**
     * The specific biochemical interaction through which this drug or supplement produces its pharmacological effect. 
     */
    public static final URI mechanismOfAction;

    /**
     * A medical specialty of the provider. 
     */
    public static final URI medicalSpecialty;

    /**
     * The system of medicine that includes this MedicalEntity, for example 'evidence-based', 'homeopathic', 'chiropractic', etc. 
     */
    public static final URI medicineSystem;

    /**
     * A member of this organization. 
     */
    public static final URI member;

    /**
     * An organization to which the person belongs. 
     */
    public static final URI memberOf;

    /**
     * A member of this organization (legacy spelling; see singular form, member). 
     */
    public static final URI members;

    /**
     * Minimum memory requirements. 
     */
    public static final URI memoryRequirements;

    /**
     * Indicates that the CreativeWork contains a reference to, but is not necessarily about a concept. 
     */
    public static final URI mentions;

    /**
     * Either the actual menu or a URL of the menu. 
     */
    public static final URI menu;

    /**
     * The lowest price if the price is a range. 
     */
    public static final URI minPrice;

    /**
     * The lower value of the product characteristic. 
     */
    public static final URI minValue;

    /**
     * The model of the product. Use with the URL of a ProductModel or a textual representation of the model identifier. The URL of the ProductModel can be from an external source. It is recommended to additionally provide strong product identifiers via the gtin8/gtin13/gtin14 and mpn properties. 
     */
    public static final URI model;

    /**
     * The Manufacturer Part Number (MPN) of the product, or the product to which the offer refers. 
     */
    public static final URI mpn;

    /**
     * The composer of the movie or TV soundtrack. 
     */
    public static final URI musicBy;

    /**
     * A member of the music group—for example, John, Paul, George, or Ringo. 
     */
    public static final URI musicGroupMember;

    /**
     * The North American Industry Classification System (NAICS) code for a particular organization or business person. 
     */
    public static final URI naics;

    /**
     * The name of the item. 
     */
    public static final URI name;

    /**
     * Nationality of the person. 
     */
    public static final URI nationality;

    /**
     * The expected progression of the condition if it is not treated and allowed to progress naturally. 
     */
    public static final URI naturalProgression;

    /**
     * The underlying innervation associated with the muscle. 
     */
    public static final URI nerve;

    /**
     * The neurological pathway extension that involves muscle control. 
     */
    public static final URI nerveMotor;

    /**
     * This ordering relation for qualitative values indicates that the subject is not equal to the object. 
     */
    public static final URI nonEqual;

    /**
     * The generic name of this drug or supplement. 
     */
    public static final URI nonProprietaryName;

    /**
     * Range of acceptable values for a typical patient, when applicable. 
     */
    public static final URI normalRange;

    /**
     * The number of tracks in this album or playlist. 
     */
    public static final URI numTracks;

    /**
     * The number of episodes in this season or series. 
     */
    public static final URI numberOfEpisodes;

    /**
     * The number of pages in the book. 
     */
    public static final URI numberOfPages;

    /**
     * Nutrition information about the recipe. 
     */
    public static final URI nutrition;

    /**
     * Category or categories describing the job. Use BLS O*NET-SOC taxonomy: http://www.onetcenter.org/taxonomy.html. Ideally includes textual label and formal code, with the property repeated for each applicable value. 
     */
    public static final URI occupationalCategory;

    /**
     * The number of offers for the product. 
     */
    public static final URI offerCount;

    /**
     * An offer to sell this item—for example, an offer to sell a product, the DVD of a movie, or tickets to an event. 
     */
    public static final URI offers;

    /**
     * The opening hours for a business. Opening hours can be specified as a weekly time range, starting with days, then times per day. Multiple days can be listed with commas ',' separating each day. Day or time ranges are specified using a hyphen '-'.- Days are specified using the following two-letter combinations: Mo, Tu, We, Th, Fr, Sa, Su.- Times are specified using 24:00 time. For example, 3pm is specified as 15:00. - Here is an example: <time itemprop="openingHours" datetime="Tu,Th 16:00-20:00">Tuesdays and Thursdays 4-8pm</time>. - If a business is open 7 days a week, then it can be specified as <time itemprop="openingHours" datetime="Mo-Su">Monday through Sunday, all day</time>. 
     */
    public static final URI openingHours;

    /**
     * The opening hours of a certain place. 
     */
    public static final URI openingHoursSpecification;

    /**
     * The opening hour of the place or service on the given day(s) of the week. 
     */
    public static final URI opens;

    /**
     * Operating systems supported (Windows 7, OSX 10.6, Android 1.6). 
     */
    public static final URI operatingSystem;

    /**
     * The place or point where a muscle arises. 
     */
    public static final URI origin;

    /**
     * The vasculature the lymphatic structure originates, or afferents, from. 
     */
    public static final URI originatesFrom;

    /**
     * Expected or actual outcomes of the study. 
     */
    public static final URI outcome;

    /**
     * Any information related to overdose on a drug, including signs or symptoms, treatments, contact information for emergency response. 
     */
    public static final URI overdosage;

    /**
     * Descriptive information establishing the overarching theory/philosophy of the plan. May include the rationale for the name, the population where the plan first came to prominence, etc. 
     */
    public static final URI overview;

    /**
     * The date and time of obtaining the product. 
     */
    public static final URI ownedFrom;

    /**
     * The date and time of giving up ownership on the product. 
     */
    public static final URI ownedThrough;

    /**
     * Products owned by the organization or person. 
     */
    public static final URI owns;

    /**
     * A parent of this person. 
     */
    public static final URI parent;

    /**
     * A parents of the person (legacy spelling; see singular form, parent). 
     */
    public static final URI parents;

    /**
     * The season to which this episode belongs. 
     */
    public static final URI partOfSeason;

    /**
     * The anatomical or organ system that this structure is part of. 
     */
    public static final URI partOfSystem;

    /**
     * The TV series to which this episode or season belongs. 
     */
    public static final URI partOfTVSeries;

    /**
     * Changes in the normal mechanical, physical, and biochemical functions that are associated with this activity or condition. 
     */
    public static final URI pathophysiology;

    /**
     * Cash, credit card, etc. 
     */
    public static final URI paymentAccepted;

    /**
     * A performer at the event—for example, a presenter, musician, musical group or actor. 
     */
    public static final URI performer;

    /**
     * Event that this person is a performer or participant in. 
     */
    public static final URI performerIn;

    /**
     * The main performer or performers of the event—for example, a presenter, musician, or actor (legacy spelling; see singular form, performer). 
     */
    public static final URI performers;

    /**
     * Permission(s) required to run the app (for example, a mobile app may require full internet access or may run only on wifi). 
     */
    public static final URI permissions;

    /**
     * The phase of the trial. 
     */
    public static final URI phase;

    /**
     * A photograph of this place. 
     */
    public static final URI photo;

    /**
     * Photographs of this place (legacy spelling; see singular form, photo). 
     */
    public static final URI photos;

    /**
     * Specific physiologic benefits associated to the plan. 
     */
    public static final URI physiologicalBenefits;

    /**
     * Player type required—for example, Flash or Silverlight. 
     */
    public static final URI playerType;

    /**
     * A polygon is the area enclosed by a point-to-point path for which the starting and ending points are the same. A polygon is expressed as a series of four or more spacedelimited points where the first and final points are identical. 
     */
    public static final URI polygon;

    /**
     * Any characteristics of the population used in the study, e.g. 'males under 65'. 
     */
    public static final URI population;

    /**
     * A possible unexpected and unfavorable evolution of a medical condition. Complications may include worsening of the signs or symptoms of the disease, extension of the condition to other organ systems, etc. 
     */
    public static final URI possibleComplication;

    /**
     * A possible treatment to address this condition, sign or symptom. 
     */
    public static final URI possibleTreatment;

    /**
     * The post offce box number for PO box addresses. 
     */
    public static final URI postOfficeBoxNumber;

    /**
     * A description of the postoperative procedures, care, and/or followups for this device. 
     */
    public static final URI postOp;

    /**
     * The postal code. For example, 94043. 
     */
    public static final URI postalCode;

    /**
     * A description of the workup, testing, and other preparations required before implanting this device. 
     */
    public static final URI preOp;

    /**
     * A pointer from a previous, often discontinued variant of the product to its newer variant. 
     */
    public static final URI predecessorOf;

    /**
     * Pregnancy category of this drug. 
     */
    public static final URI pregnancyCategory;

    /**
     * Any precaution, guidance, contraindication, etc. related to this drug's use during pregnancy. 
     */
    public static final URI pregnancyWarning;

    /**
     * The length of time it takes to prepare the recipe, in ISO 8601 duration format. 
     */
    public static final URI prepTime;

    /**
     * Typical preparation that a patient must undergo before having the procedure performed. 
     */
    public static final URI preparation;

    /**
     * Link to prescribing information for the drug. 
     */
    public static final URI prescribingInfo;

    /**
     * Indicates whether this drug is available by prescription or over-the-counter. 
     */
    public static final URI prescriptionStatus;

    /**
     * The offer price of a product, or of a price component when attached to PriceSpecification and its subtypes. 
     */
    public static final URI price;

    /**
     * The currency (in 3-letter ISO 4217 format) of the offer price or a price component, when attached to PriceSpecification and its subtypes. 
     */
    public static final URI priceCurrency;

    /**
     * The price range of the business, for example $$$. 
     */
    public static final URI priceRange;

    /**
     * One or more detailed price specifications, indicating the unit price and delivery or payment charges. 
     */
    public static final URI priceSpecification;

    /**
     * A short text or acronym indicating multiple price specifications for the same offer, e.g. SRP for the suggested retail price or INVOICE for the invoice price, mostly used in the car industry. 
     */
    public static final URI priceType;

    /**
     * The date after which the price is no longer available. 
     */
    public static final URI priceValidUntil;

    /**
     * Indicates the main image on the page 
     */
    public static final URI primaryImageOfPage;

    /**
     * A preventative therapy used to prevent an initial occurrence of the medical condition, such as vaccination. 
     */
    public static final URI primaryPrevention;

    /**
     * The number of the column in which the NewsArticle appears in the print edition. 
     */
    public static final URI printColumn;

    /**
     * The edition of the print product in which the NewsArticle appears. 
     */
    public static final URI printEdition;

    /**
     * If this NewsArticle appears in print, this field indicates the name of the page on which the article is found. Please note that this field is intended for the exact page name (e.g. A5, B18). 
     */
    public static final URI printPage;

    /**
     * If this NewsArticle appears in print, this field indicates the print section in which the article appeared. 
     */
    public static final URI printSection;

    /**
     * A description of the procedure involved in setting up, using, and/or installing the device. 
     */
    public static final URI procedure;

    /**
     * The type of procedure, for example Surgical, Noninvasive, or Percutaneous. 
     */
    public static final URI procedureType;

    /**
     * Processor architecture required to run the application (e.g. IA64). 
     */
    public static final URI processorRequirements;

    /**
     * The producer of the movie, TV series, season, or episode, or video. 
     */
    public static final URI producer;

    /**
     * The product identifier, such as ISBN. For example: <meta itemprop='productID' content='isbn:123-456-789'/>. 
     */
    public static final URI productID;

    /**
     * The production company or studio that made the movie, TV series, season, or episode, or video. 
     */
    public static final URI productionCompany;

    /**
     * Proprietary name given to the diet plan, typically by its originator or creator. 
     */
    public static final URI proprietaryName;

    /**
     * The number of grams of protein. 
     */
    public static final URI proteinContent;

    /**
     * Specifies the Person or Organization that distributed the CreativeWork. 
     */
    public static final URI provider;

    /**
     * The type of the medical article, taken from the US NLM MeSH publication type catalog. 
     */
    public static final URI publicationType;

    /**
     * The publisher of the creative work. 
     */
    public static final URI publisher;

    /**
     * Link to page describing the editorial principles of the organization primarily responsible for the creation of the CreativeWork. 
     */
    public static final URI publishingPrinciples;

    /**
     * The purpose or purposes of this device, for example whether it is intended for diagnostic or therapeutic use. 
     */
    public static final URI purpose;

    /**
     * Specific qualifications required for this role. 
     */
    public static final URI qualifications;

    /**
     * The count of total number of ratings. 
     */
    public static final URI ratingCount;

    /**
     * The rating for the content. 
     */
    public static final URI ratingValue;

    /**
     * The category of the recipe—for example, appetizer, entree, etc. 
     */
    public static final URI recipeCategory;

    /**
     * The cuisine of the recipe (for example, French or Ethopian). 
     */
    public static final URI recipeCuisine;

    /**
     * The steps to make the dish. 
     */
    public static final URI recipeInstructions;

    /**
     * The quantity produced by the recipe (for example, number of people served, number of servings, etc). 
     */
    public static final URI recipeYield;

    /**
     * If applicable, the organization that officially recognizes this entity as part of its endorsed system of medicine. 
     */
    public static final URI recognizingAuthority;

    /**
     * Strength of the guideline's recommendation (e.g. 'class I'). 
     */
    public static final URI recommendationStrength;

    /**
     * Recommended intake of this supplement for a given population as defined by a specific recommending authority. 
     */
    public static final URI recommendedIntake;

    /**
     * The anatomical or organ system drained by this vessel; generally refers to a specific part of an organ. 
     */
    public static final URI regionDrained;

    /**
     * The regions where the media is allowed. If not specified, then it's assumed to be allowed everywhere. Specify the countries in ISO 3166 format. 
     */
    public static final URI regionsAllowed;

    /**
     * Anatomical systems or structures that relate to the superficial anatomy. 
     */
    public static final URI relatedAnatomy;

    /**
     * A medical condition associated with this anatomy. 
     */
    public static final URI relatedCondition;

    /**
     * Any other drug related to this one, for example commonly-prescribed alternatives. 
     */
    public static final URI relatedDrug;

    /**
     * A link related to this web page, for example to other related web pages. 
     */
    public static final URI relatedLink;

    /**
     * Related anatomical structure(s) that are not part of the system but relate or connect to it, such as vascular bundles associated with an organ system. 
     */
    public static final URI relatedStructure;

    /**
     * A medical therapy related to this anatomy. 
     */
    public static final URI relatedTherapy;

    /**
     * The most generic familial relation. 
     */
    public static final URI relatedTo;

    /**
     * The release date of a product or product model. This can be used to distinguish the exact variant of a product. 
     */
    public static final URI releaseDate;

    /**
     * Description of what changed in this version. 
     */
    public static final URI releaseNotes;

    /**
     * If applicable, a medical specialty in which this entity is relevant. 
     */
    public static final URI relevantSpecialty;

    /**
     * Number of times one should repeat the activity. 
     */
    public static final URI repetitions;

    /**
     * The URL at which a reply may be posted to the specified UserComment. 
     */
    public static final URI replyToUrl;

    /**
     * Indicates whether this image is representative of the content of the page. 
     */
    public static final URI representativeOfPage;

    /**
     * Component dependency requirements for application. This includes runtime environments and shared libraries that are not included in the application distribution package, but required to run the application (Examples: DirectX, Java or .NET runtime). 
     */
    public static final URI requirements;

    /**
     * Indicates if use of the media require a subscription  (either paid or free). Allowed values are true or false (note that an earlier version had 'yes', 'no'). 
     */
    public static final URI requiresSubscription;

    /**
     * Responsibilities associated with this role. 
     */
    public static final URI responsibilities;

    /**
     * How often one should break from the activity. 
     */
    public static final URI restPeriods;

    /**
     * A review of the item. 
     */
    public static final URI review;

    /**
     * The actual body of the review 
     */
    public static final URI reviewBody;

    /**
     * The count of total number of reviews. 
     */
    public static final URI reviewCount;

    /**
     * The rating given in this review. Note that reviews can themselves be rated. The reviewRating applies to rating given by the review. The aggregateRating property applies to the review itself, as a creative work. 
     */
    public static final URI reviewRating;

    /**
     * People or organizations that have reviewed the content on this web page for accuracy and/or completeness. 
     */
    public static final URI reviewedBy;

    /**
     * Review of the item (legacy spelling; see singular form, review). 
     */
    public static final URI reviews;

    /**
     * A modifiable or non-modifiable factor that increases the risk of a patient contracting this condition, e.g. age,  coexisting condition. 
     */
    public static final URI riskFactor;

    /**
     * Specific physiologic risks associated to the plan. 
     */
    public static final URI risks;

    /**
     * The vasculature the lymphatic structure runs, or efferents, to. 
     */
    public static final URI runsTo;

    /**
     * Any potential safety concern associated with the supplement. May include interactions with other drugs and foods, pregnancy, breastfeeding, known adverse reactions, and documented efficacy of the supplement. 
     */
    public static final URI safetyConsideration;

    /**
     * The currency (coded using ISO 4217, http://en.wikipedia.org/wiki/ISO_4217 used for the main salary information in this job posting. 
     */
    public static final URI salaryCurrency;

    /**
     * The number of grams of saturated fat. 
     */
    public static final URI saturatedFatContent;

    /**
     * A link to a screenshot image of the app. 
     */
    public static final URI screenshot;

    /**
     * A season of a TV series. 
     */
    public static final URI season;

    /**
     * The season number. 
     */
    public static final URI seasonNumber;

    /**
     * The seasons of the TV series (legacy spelling; see singular form, season). 
     */
    public static final URI seasons;

    /**
     * A preventative therapy used to prevent reoccurrence of the medical condition after an initial episode of the condition. 
     */
    public static final URI secondaryPrevention;

    /**
     * A pointer to products or services sought by the organization or person (demand). 
     */
    public static final URI seeks;

    /**
     * The seller. 
     */
    public static final URI seller;

    /**
     * The neurological pathway extension that inputs and sends information to the brain or spinal cord. 
     */
    public static final URI sensoryUnit;

    /**
     * The serial number or any alphanumeric identifier of a particular product. When attached to an offer, it is a shortcut for the serial number of the product included in the offer. 
     */
    public static final URI serialNumber;

    /**
     * A possible serious complication and/or serious side effect of this therapy. Serious adverse outcomes include those that are life-threatening; result in death, disability, or permanent damage; require hospitalization or prolong existing hospitalization; cause congenital anomalies or birth defects; or jeopardize the patient and may require medical or surgical intervention to prevent one of the outcomes in this definition. 
     */
    public static final URI seriousAdverseOutcome;

    /**
     * The cuisine of the restaurant. 
     */
    public static final URI servesCuisine;

    /**
     * The serving size, in terms of the number of volume or mass 
     */
    public static final URI servingSize;

    /**
     * A sibling of the person. 
     */
    public static final URI sibling;

    /**
     * A sibling of the person (legacy spelling; see singular form, sibling). 
     */
    public static final URI siblings;

    /**
     * A sign detected by the test. 
     */
    public static final URI signDetected;

    /**
     * A sign or symptom of this condition. Signs are objective or physically observable manifestations of the medical condition while symptoms are the subjective experienceof the medical condition. 
     */
    public static final URI signOrSymptom;

    /**
     * The significance associated with the superficial anatomy; as an example, how characteristics of the superficial anatomy can suggest underlying medical conditions or courses of treatment. 
     */
    public static final URI significance;

    /**
     * One of the more significant URLs on the page. Typically, these are the non-navigation links that are clicked on the most. 
     */
    public static final URI significantLink;

    /**
     * The most significant URLs on the page. Typically, these are the non-navigation links that are clicked on the most (legacy spelling; see singular form, significantLink). 
     */
    public static final URI significantLinks;

    /**
     * Skills required to fulfill this role. 
     */
    public static final URI skills;

    /**
     * The Stock Keeping Unit (SKU), i.e. a merchant-specific identifier for a product or service, or the product to which the offer refers. 
     */
    public static final URI sku;

    /**
     * The number of milligrams of sodium. 
     */
    public static final URI sodiumContent;

    /**
     * Version of the software instance. 
     */
    public static final URI softwareVersion;

    /**
     * The anatomical or organ system that the artery originates from. 
     */
    public static final URI source;

    /**
     * The Organization on whose behalf the creator was working. 
     */
    public static final URI sourceOrganization;

    /**
     * The neurological pathway that originates the neurons. 
     */
    public static final URI sourcedFrom;

    /**
     * Any special commitments associated with this job posting. Valid entries include VeteranCommit, MilitarySpouseCommit, etc. 
     */
    public static final URI specialCommitments;

    /**
     * One of the domain specialities to which this web page's content applies. 
     */
    public static final URI specialty;

    /**
     * Sponsor of the study. 
     */
    public static final URI sponsor;

    /**
     * The person's spouse. 
     */
    public static final URI spouse;

    /**
     * The stage of the condition, if applicable. 
     */
    public static final URI stage;

    /**
     * The stage represented as a number, e.g. 3. 
     */
    public static final URI stageAsNumber;

    /**
     * The start date and time of the event (in ISO 8601 date format). 
     */
    public static final URI startDate;

    /**
     * The status of the study (enumerated). 
     */
    public static final URI status;

    /**
     * Storage requirements (free space required). 
     */
    public static final URI storageRequirements;

    /**
     * The street address. For example, 1600 Amphitheatre Pkwy. 
     */
    public static final URI streetAddress;

    /**
     * The units of an active ingredient's strength, e.g. mg. 
     */
    public static final URI strengthUnit;

    /**
     * The value of an active ingredient's strength, e.g. 325. 
     */
    public static final URI strengthValue;

    /**
     * The name given to how bone physically connects to each other. 
     */
    public static final URI structuralClass;

    /**
     * A medical study or trial related to this entity. 
     */
    public static final URI study;

    /**
     * Specifics about the observational study design (enumerated). 
     */
    public static final URI studyDesign;

    /**
     * The location in which the study is taking/took place. 
     */
    public static final URI studyLocation;

    /**
     * A subject of the study, i.e. one of the medical conditions, therapies, devices, drugs, etc. investigated by the study. 
     */
    public static final URI studySubject;

    /**
     * An Event that is part of this event. For example, a conference event includes many presentations, each are a subEvent of the conference. 
     */
    public static final URI subEvent;

    /**
     * Events that are a part of this event. For example, a conference event includes many presentations, each are subEvents of the conference (legacy spelling; see singular form, subEvent). 
     */
    public static final URI subEvents;

    /**
     * The substage, e.g. 'a' for Stage IIIa. 
     */
    public static final URI subStageSuffix;

    /**
     * Component (sub-)structure(s) that comprise this anatomical structure. 
     */
    public static final URI subStructure;

    /**
     * A component test of the panel. 
     */
    public static final URI subTest;

    /**
     * A more specific type of the condition, where applicable, for example 'Type 1 Diabetes', 'Type 2 Diabetes', or 'Gestational Diabetes' for Diabetes. 
     */
    public static final URI subtype;

    /**
     * A pointer from a newer variant of a product  to its previous, often discontinued predecessor. 
     */
    public static final URI successorOf;

    /**
     * The number of grams of sugar. 
     */
    public static final URI sugarContent;

    /**
     * An event that this event is a part of. For example, a collection of individual music performances might each have a music festival as their superEvent. 
     */
    public static final URI superEvent;

    /**
     * The area to which the artery supplies blood to. 
     */
    public static final URI supplyTo;

    /**
     * Characteristics of the population for which this is intended, or which typically uses it, e.g. 'adults'. 
     */
    public static final URI targetPopulation;

    /**
     * The Tax / Fiscal ID of the organization or person, e.g. the TIN in the US or the CIF/NIF in Spain. 
     */
    public static final URI taxID;

    /**
     * The telephone number. 
     */
    public static final URI telephone;

    /**
     * The textual content of this CreativeWork. 
     */
    public static final URI text;

    /**
     * Thumbnail image for an image or video. 
     */
    public static final URI thumbnail;

    /**
     * A thumbnail image relevant to the Thing. 
     */
    public static final URI thumbnailUrl;

    /**
     * The exchange traded instrument associated with a Corporation object. The tickerSymbol is expressed as an exchange and an instrument name separated by a space character. For the exchange component of the tickerSymbol attribute, we reccommend using the controlled vocaulary of Market Identifier Codes (MIC) specified in ISO15022. 
     */
    public static final URI tickerSymbol;

    /**
     * The type of tissue sample required for the test. 
     */
    public static final URI tissueSample;

    /**
     * The title of the job. 
     */
    public static final URI title;

    /**
     * The total time it takes to prepare and cook the recipe, in ISO 8601 duration format. 
     */
    public static final URI totalTime;

    /**
     * A music recording (track)—usually a single song. 
     */
    public static final URI track;

    /**
     * A music recording (track)—usually a single song (legacy spelling; see singular form, track). 
     */
    public static final URI tracks;

    /**
     * The trailer of the movie or TV series, season, or episode. 
     */
    public static final URI trailer;

    /**
     * The number of grams of trans fat. 
     */
    public static final URI transFatContent;

    /**
     * If this MediaObject is an AudioObject or VideoObject, the transcript of that object. 
     */
    public static final URI transcript;

    /**
     * How the disease spreads, either as a route or vector, for example 'direct contact', 'Aedes aegypti', etc. 
     */
    public static final URI transmissionMethod;

    /**
     * Specifics about the trial design (enumerated). 
     */
    public static final URI trialDesign;

    /**
     * The anatomical or organ system that the vein flows into; a larger structure that the vein connects to. 
     */
    public static final URI tributary;

    /**
     * The product that this structured value is referring to. 
     */
    public static final URI typeOfGood;

    /**
     * A medical test typically performed given this condition. 
     */
    public static final URI typicalTest;

    /**
     * The unit of measurement given using the UN/CEFACT Common Code (3 characters). 
     */
    public static final URI unitCode;

    /**
     * The number of grams of unsaturated fat. 
     */
    public static final URI unsaturatedFatContent;

    /**
     * Date when this media object was uploaded to this site. 
     */
    public static final URI uploadDate;

    /**
     * URL of the item. 
     */
    public static final URI url;

    /**
     * A condition the test is used to diagnose. 
     */
    public static final URI usedToDiagnose;

    /**
     * Device used to perform the test. 
     */
    public static final URI usesDevice;

    /**
     * The beginning of the validity of offer, price specification, or opening hours data. 
     */
    public static final URI validFrom;

    /**
     * The end of the validity of offer, price specification, or opening hours data. 
     */
    public static final URI validThrough;

    /**
     * The value of the product characteristic. 
     */
    public static final URI value;

    /**
     * Specifies whether the applicable value-added tax (VAT) is included in the price specification or not. 
     */
    public static final URI valueAddedTaxIncluded;

    /**
     * A pointer to a secondary value that provides additional information on the original value, e.g. a reference temperature. 
     */
    public static final URI valueReference;

    /**
     * The Value-added Tax ID of the organisation or person. 
     */
    public static final URI vatID;

    /**
     * The version of the CreativeWork embodied by a specified resource. 
     */
    public static final URI version;

    /**
     * An embedded video object. 
     */
    public static final URI video;

    /**
     * The frame size of the video. 
     */
    public static final URI videoFrameSize;

    /**
     * The quality of the video. 
     */
    public static final URI videoQuality;

    /**
     * Any FDA or other warnings about the drug (text or URL). 
     */
    public static final URI warning;

    /**
     * The warranty promise(s) included in the offer. 
     */
    public static final URI warranty;

    /**
     * The scope of the warranty promise. 
     */
    public static final URI warrantyScope;

    /**
     * The weight of the product. 
     */
    public static final URI weight;

    /**
     * The width of the item. 
     */
    public static final URI width;

    /**
     * The number of words in the text of the Article. 
     */
    public static final URI wordCount;

    /**
     * The typical working hours for this job (e.g. 1st shift, night shift, 8am-5pm). 
     */
    public static final URI workHours;

    /**
     * A contact location for a person's place of work. 
     */
    public static final URI workLocation;

    /**
     * Quantitative measure of the physiologic output of the exercise; also referred to as energy expenditure. 
     */
    public static final URI workload;

    /**
     * Organizations that the person works for. 
     */
    public static final URI worksFor;

    /**
     * The lowest value allowed in this rating system. If worstRating is omitted, 1 is assumed. 
     */
    public static final URI worstRating;


    static{
        ValueFactory factory = ValueFactoryImpl.getInstance();
        AboutPage = factory.createURI(SCHEMA.NAMESPACE, "AboutPage");
        AccountingService = factory.createURI(SCHEMA.NAMESPACE, "AccountingService");
        AdministrativeArea = factory.createURI(SCHEMA.NAMESPACE, "AdministrativeArea");
        AdultEntertainment = factory.createURI(SCHEMA.NAMESPACE, "AdultEntertainment");
        AggregateOffer = factory.createURI(SCHEMA.NAMESPACE, "AggregateOffer");
        AggregateRating = factory.createURI(SCHEMA.NAMESPACE, "AggregateRating");
        Airport = factory.createURI(SCHEMA.NAMESPACE, "Airport");
        AmusementPark = factory.createURI(SCHEMA.NAMESPACE, "AmusementPark");
        AnatomicalStructure = factory.createURI(SCHEMA.NAMESPACE, "AnatomicalStructure");
        AnatomicalSystem = factory.createURI(SCHEMA.NAMESPACE, "AnatomicalSystem");
        AnimalShelter = factory.createURI(SCHEMA.NAMESPACE, "AnimalShelter");
        ApartmentComplex = factory.createURI(SCHEMA.NAMESPACE, "ApartmentComplex");
        ApprovedIndication = factory.createURI(SCHEMA.NAMESPACE, "ApprovedIndication");
        Aquarium = factory.createURI(SCHEMA.NAMESPACE, "Aquarium");
        ArtGallery = factory.createURI(SCHEMA.NAMESPACE, "ArtGallery");
        Artery = factory.createURI(SCHEMA.NAMESPACE, "Artery");
        Article = factory.createURI(SCHEMA.NAMESPACE, "Article");
        Attorney = factory.createURI(SCHEMA.NAMESPACE, "Attorney");
        Audience = factory.createURI(SCHEMA.NAMESPACE, "Audience");
        AudioObject = factory.createURI(SCHEMA.NAMESPACE, "AudioObject");
        AutoBodyShop = factory.createURI(SCHEMA.NAMESPACE, "AutoBodyShop");
        AutoDealer = factory.createURI(SCHEMA.NAMESPACE, "AutoDealer");
        AutoPartsStore = factory.createURI(SCHEMA.NAMESPACE, "AutoPartsStore");
        AutoRental = factory.createURI(SCHEMA.NAMESPACE, "AutoRental");
        AutoRepair = factory.createURI(SCHEMA.NAMESPACE, "AutoRepair");
        AutoWash = factory.createURI(SCHEMA.NAMESPACE, "AutoWash");
        AutomatedTeller = factory.createURI(SCHEMA.NAMESPACE, "AutomatedTeller");
        AutomotiveBusiness = factory.createURI(SCHEMA.NAMESPACE, "AutomotiveBusiness");
        Bakery = factory.createURI(SCHEMA.NAMESPACE, "Bakery");
        BankOrCreditUnion = factory.createURI(SCHEMA.NAMESPACE, "BankOrCreditUnion");
        BarOrPub = factory.createURI(SCHEMA.NAMESPACE, "BarOrPub");
        Beach = factory.createURI(SCHEMA.NAMESPACE, "Beach");
        BeautySalon = factory.createURI(SCHEMA.NAMESPACE, "BeautySalon");
        BedAndBreakfast = factory.createURI(SCHEMA.NAMESPACE, "BedAndBreakfast");
        BikeStore = factory.createURI(SCHEMA.NAMESPACE, "BikeStore");
        Blog = factory.createURI(SCHEMA.NAMESPACE, "Blog");
        BlogPosting = factory.createURI(SCHEMA.NAMESPACE, "BlogPosting");
        BloodTest = factory.createURI(SCHEMA.NAMESPACE, "BloodTest");
        BodyOfWater = factory.createURI(SCHEMA.NAMESPACE, "BodyOfWater");
        Bone = factory.createURI(SCHEMA.NAMESPACE, "Bone");
        Book = factory.createURI(SCHEMA.NAMESPACE, "Book");
        BookFormatType = factory.createURI(SCHEMA.NAMESPACE, "BookFormatType");
        BookStore = factory.createURI(SCHEMA.NAMESPACE, "BookStore");
        BowlingAlley = factory.createURI(SCHEMA.NAMESPACE, "BowlingAlley");
        BrainStructure = factory.createURI(SCHEMA.NAMESPACE, "BrainStructure");
        Brand = factory.createURI(SCHEMA.NAMESPACE, "Brand");
        Brewery = factory.createURI(SCHEMA.NAMESPACE, "Brewery");
        BuddhistTemple = factory.createURI(SCHEMA.NAMESPACE, "BuddhistTemple");
        BusStation = factory.createURI(SCHEMA.NAMESPACE, "BusStation");
        BusStop = factory.createURI(SCHEMA.NAMESPACE, "BusStop");
        BusinessEntityType = factory.createURI(SCHEMA.NAMESPACE, "BusinessEntityType");
        BusinessEvent = factory.createURI(SCHEMA.NAMESPACE, "BusinessEvent");
        BusinessFunction = factory.createURI(SCHEMA.NAMESPACE, "BusinessFunction");
        CafeOrCoffeeShop = factory.createURI(SCHEMA.NAMESPACE, "CafeOrCoffeeShop");
        Campground = factory.createURI(SCHEMA.NAMESPACE, "Campground");
        Canal = factory.createURI(SCHEMA.NAMESPACE, "Canal");
        Casino = factory.createURI(SCHEMA.NAMESPACE, "Casino");
        CatholicChurch = factory.createURI(SCHEMA.NAMESPACE, "CatholicChurch");
        Cemetery = factory.createURI(SCHEMA.NAMESPACE, "Cemetery");
        CheckoutPage = factory.createURI(SCHEMA.NAMESPACE, "CheckoutPage");
        ChildCare = factory.createURI(SCHEMA.NAMESPACE, "ChildCare");
        ChildrensEvent = factory.createURI(SCHEMA.NAMESPACE, "ChildrensEvent");
        Church = factory.createURI(SCHEMA.NAMESPACE, "Church");
        City = factory.createURI(SCHEMA.NAMESPACE, "City");
        CityHall = factory.createURI(SCHEMA.NAMESPACE, "CityHall");
        CivicStructure = factory.createURI(SCHEMA.NAMESPACE, "CivicStructure");
        ClothingStore = factory.createURI(SCHEMA.NAMESPACE, "ClothingStore");
        CollectionPage = factory.createURI(SCHEMA.NAMESPACE, "CollectionPage");
        CollegeOrUniversity = factory.createURI(SCHEMA.NAMESPACE, "CollegeOrUniversity");
        ComedyClub = factory.createURI(SCHEMA.NAMESPACE, "ComedyClub");
        ComedyEvent = factory.createURI(SCHEMA.NAMESPACE, "ComedyEvent");
        Comment = factory.createURI(SCHEMA.NAMESPACE, "Comment");
        ComputerStore = factory.createURI(SCHEMA.NAMESPACE, "ComputerStore");
        ContactPage = factory.createURI(SCHEMA.NAMESPACE, "ContactPage");
        ContactPoint = factory.createURI(SCHEMA.NAMESPACE, "ContactPoint");
        Continent = factory.createURI(SCHEMA.NAMESPACE, "Continent");
        ConvenienceStore = factory.createURI(SCHEMA.NAMESPACE, "ConvenienceStore");
        Corporation = factory.createURI(SCHEMA.NAMESPACE, "Corporation");
        Country = factory.createURI(SCHEMA.NAMESPACE, "Country");
        Courthouse = factory.createURI(SCHEMA.NAMESPACE, "Courthouse");
        CreativeWork = factory.createURI(SCHEMA.NAMESPACE, "CreativeWork");
        CreditCard = factory.createURI(SCHEMA.NAMESPACE, "CreditCard");
        Crematorium = factory.createURI(SCHEMA.NAMESPACE, "Crematorium");
        DDxElement = factory.createURI(SCHEMA.NAMESPACE, "DDxElement");
        DanceEvent = factory.createURI(SCHEMA.NAMESPACE, "DanceEvent");
        DanceGroup = factory.createURI(SCHEMA.NAMESPACE, "DanceGroup");
        DayOfWeek = factory.createURI(SCHEMA.NAMESPACE, "DayOfWeek");
        DaySpa = factory.createURI(SCHEMA.NAMESPACE, "DaySpa");
        DefenceEstablishment = factory.createURI(SCHEMA.NAMESPACE, "DefenceEstablishment");
        DeliveryChargeSpecification = factory.createURI(SCHEMA.NAMESPACE, "DeliveryChargeSpecification");
        DeliveryMethod = factory.createURI(SCHEMA.NAMESPACE, "DeliveryMethod");
        Demand = factory.createURI(SCHEMA.NAMESPACE, "Demand");
        Dentist = factory.createURI(SCHEMA.NAMESPACE, "Dentist");
        DepartmentStore = factory.createURI(SCHEMA.NAMESPACE, "DepartmentStore");
        DiagnosticLab = factory.createURI(SCHEMA.NAMESPACE, "DiagnosticLab");
        DiagnosticProcedure = factory.createURI(SCHEMA.NAMESPACE, "DiagnosticProcedure");
        Diet = factory.createURI(SCHEMA.NAMESPACE, "Diet");
        DietarySupplement = factory.createURI(SCHEMA.NAMESPACE, "DietarySupplement");
        Distance = factory.createURI(SCHEMA.NAMESPACE, "Distance");
        DoseSchedule = factory.createURI(SCHEMA.NAMESPACE, "DoseSchedule");
        Drug = factory.createURI(SCHEMA.NAMESPACE, "Drug");
        DrugClass = factory.createURI(SCHEMA.NAMESPACE, "DrugClass");
        DrugCost = factory.createURI(SCHEMA.NAMESPACE, "DrugCost");
        DrugCostCategory = factory.createURI(SCHEMA.NAMESPACE, "DrugCostCategory");
        DrugLegalStatus = factory.createURI(SCHEMA.NAMESPACE, "DrugLegalStatus");
        DrugPregnancyCategory = factory.createURI(SCHEMA.NAMESPACE, "DrugPregnancyCategory");
        DrugPrescriptionStatus = factory.createURI(SCHEMA.NAMESPACE, "DrugPrescriptionStatus");
        DrugStrength = factory.createURI(SCHEMA.NAMESPACE, "DrugStrength");
        DryCleaningOrLaundry = factory.createURI(SCHEMA.NAMESPACE, "DryCleaningOrLaundry");
        Duration = factory.createURI(SCHEMA.NAMESPACE, "Duration");
        EducationEvent = factory.createURI(SCHEMA.NAMESPACE, "EducationEvent");
        EducationalOrganization = factory.createURI(SCHEMA.NAMESPACE, "EducationalOrganization");
        Electrician = factory.createURI(SCHEMA.NAMESPACE, "Electrician");
        ElectronicsStore = factory.createURI(SCHEMA.NAMESPACE, "ElectronicsStore");
        ElementarySchool = factory.createURI(SCHEMA.NAMESPACE, "ElementarySchool");
        Embassy = factory.createURI(SCHEMA.NAMESPACE, "Embassy");
        EmergencyService = factory.createURI(SCHEMA.NAMESPACE, "EmergencyService");
        EmploymentAgency = factory.createURI(SCHEMA.NAMESPACE, "EmploymentAgency");
        Energy = factory.createURI(SCHEMA.NAMESPACE, "Energy");
        EntertainmentBusiness = factory.createURI(SCHEMA.NAMESPACE, "EntertainmentBusiness");
        Enumeration = factory.createURI(SCHEMA.NAMESPACE, "Enumeration");
        Event = factory.createURI(SCHEMA.NAMESPACE, "Event");
        EventVenue = factory.createURI(SCHEMA.NAMESPACE, "EventVenue");
        ExerciseGym = factory.createURI(SCHEMA.NAMESPACE, "ExerciseGym");
        ExercisePlan = factory.createURI(SCHEMA.NAMESPACE, "ExercisePlan");
        FastFoodRestaurant = factory.createURI(SCHEMA.NAMESPACE, "FastFoodRestaurant");
        Festival = factory.createURI(SCHEMA.NAMESPACE, "Festival");
        FinancialService = factory.createURI(SCHEMA.NAMESPACE, "FinancialService");
        FireStation = factory.createURI(SCHEMA.NAMESPACE, "FireStation");
        Florist = factory.createURI(SCHEMA.NAMESPACE, "Florist");
        FoodEstablishment = factory.createURI(SCHEMA.NAMESPACE, "FoodEstablishment");
        FoodEvent = factory.createURI(SCHEMA.NAMESPACE, "FoodEvent");
        FurnitureStore = factory.createURI(SCHEMA.NAMESPACE, "FurnitureStore");
        GardenStore = factory.createURI(SCHEMA.NAMESPACE, "GardenStore");
        GasStation = factory.createURI(SCHEMA.NAMESPACE, "GasStation");
        GatedResidenceCommunity = factory.createURI(SCHEMA.NAMESPACE, "GatedResidenceCommunity");
        GeneralContractor = factory.createURI(SCHEMA.NAMESPACE, "GeneralContractor");
        GeoCoordinates = factory.createURI(SCHEMA.NAMESPACE, "GeoCoordinates");
        GeoShape = factory.createURI(SCHEMA.NAMESPACE, "GeoShape");
        GolfCourse = factory.createURI(SCHEMA.NAMESPACE, "GolfCourse");
        GovernmentBuilding = factory.createURI(SCHEMA.NAMESPACE, "GovernmentBuilding");
        GovernmentOffice = factory.createURI(SCHEMA.NAMESPACE, "GovernmentOffice");
        GovernmentOrganization = factory.createURI(SCHEMA.NAMESPACE, "GovernmentOrganization");
        GroceryStore = factory.createURI(SCHEMA.NAMESPACE, "GroceryStore");
        HVACBusiness = factory.createURI(SCHEMA.NAMESPACE, "HVACBusiness");
        HairSalon = factory.createURI(SCHEMA.NAMESPACE, "HairSalon");
        HardwareStore = factory.createURI(SCHEMA.NAMESPACE, "HardwareStore");
        HealthAndBeautyBusiness = factory.createURI(SCHEMA.NAMESPACE, "HealthAndBeautyBusiness");
        HealthClub = factory.createURI(SCHEMA.NAMESPACE, "HealthClub");
        HighSchool = factory.createURI(SCHEMA.NAMESPACE, "HighSchool");
        HinduTemple = factory.createURI(SCHEMA.NAMESPACE, "HinduTemple");
        HobbyShop = factory.createURI(SCHEMA.NAMESPACE, "HobbyShop");
        HomeAndConstructionBusiness = factory.createURI(SCHEMA.NAMESPACE, "HomeAndConstructionBusiness");
        HomeGoodsStore = factory.createURI(SCHEMA.NAMESPACE, "HomeGoodsStore");
        Hospital = factory.createURI(SCHEMA.NAMESPACE, "Hospital");
        Hostel = factory.createURI(SCHEMA.NAMESPACE, "Hostel");
        Hotel = factory.createURI(SCHEMA.NAMESPACE, "Hotel");
        HousePainter = factory.createURI(SCHEMA.NAMESPACE, "HousePainter");
        IceCreamShop = factory.createURI(SCHEMA.NAMESPACE, "IceCreamShop");
        ImageGallery = factory.createURI(SCHEMA.NAMESPACE, "ImageGallery");
        ImageObject = factory.createURI(SCHEMA.NAMESPACE, "ImageObject");
        ImagingTest = factory.createURI(SCHEMA.NAMESPACE, "ImagingTest");
        IndividualProduct = factory.createURI(SCHEMA.NAMESPACE, "IndividualProduct");
        InfectiousAgentClass = factory.createURI(SCHEMA.NAMESPACE, "InfectiousAgentClass");
        InfectiousDisease = factory.createURI(SCHEMA.NAMESPACE, "InfectiousDisease");
        InsuranceAgency = factory.createURI(SCHEMA.NAMESPACE, "InsuranceAgency");
        Intangible = factory.createURI(SCHEMA.NAMESPACE, "Intangible");
        InternetCafe = factory.createURI(SCHEMA.NAMESPACE, "InternetCafe");
        ItemAvailability = factory.createURI(SCHEMA.NAMESPACE, "ItemAvailability");
        ItemList = factory.createURI(SCHEMA.NAMESPACE, "ItemList");
        ItemPage = factory.createURI(SCHEMA.NAMESPACE, "ItemPage");
        JewelryStore = factory.createURI(SCHEMA.NAMESPACE, "JewelryStore");
        JobPosting = factory.createURI(SCHEMA.NAMESPACE, "JobPosting");
        Joint = factory.createURI(SCHEMA.NAMESPACE, "Joint");
        LakeBodyOfWater = factory.createURI(SCHEMA.NAMESPACE, "LakeBodyOfWater");
        Landform = factory.createURI(SCHEMA.NAMESPACE, "Landform");
        LandmarksOrHistoricalBuildings = factory.createURI(SCHEMA.NAMESPACE, "LandmarksOrHistoricalBuildings");
        Language = factory.createURI(SCHEMA.NAMESPACE, "Language");
        LegislativeBuilding = factory.createURI(SCHEMA.NAMESPACE, "LegislativeBuilding");
        Library = factory.createURI(SCHEMA.NAMESPACE, "Library");
        LifestyleModification = factory.createURI(SCHEMA.NAMESPACE, "LifestyleModification");
        Ligament = factory.createURI(SCHEMA.NAMESPACE, "Ligament");
        LiquorStore = factory.createURI(SCHEMA.NAMESPACE, "LiquorStore");
        LiteraryEvent = factory.createURI(SCHEMA.NAMESPACE, "LiteraryEvent");
        LocalBusiness = factory.createURI(SCHEMA.NAMESPACE, "LocalBusiness");
        Locksmith = factory.createURI(SCHEMA.NAMESPACE, "Locksmith");
        LodgingBusiness = factory.createURI(SCHEMA.NAMESPACE, "LodgingBusiness");
        LymphaticVessel = factory.createURI(SCHEMA.NAMESPACE, "LymphaticVessel");
        Map = factory.createURI(SCHEMA.NAMESPACE, "Map");
        Mass = factory.createURI(SCHEMA.NAMESPACE, "Mass");
        MaximumDoseSchedule = factory.createURI(SCHEMA.NAMESPACE, "MaximumDoseSchedule");
        MediaObject = factory.createURI(SCHEMA.NAMESPACE, "MediaObject");
        MedicalAudience = factory.createURI(SCHEMA.NAMESPACE, "MedicalAudience");
        MedicalCause = factory.createURI(SCHEMA.NAMESPACE, "MedicalCause");
        MedicalClinic = factory.createURI(SCHEMA.NAMESPACE, "MedicalClinic");
        MedicalCode = factory.createURI(SCHEMA.NAMESPACE, "MedicalCode");
        MedicalCondition = factory.createURI(SCHEMA.NAMESPACE, "MedicalCondition");
        MedicalConditionStage = factory.createURI(SCHEMA.NAMESPACE, "MedicalConditionStage");
        MedicalContraindication = factory.createURI(SCHEMA.NAMESPACE, "MedicalContraindication");
        MedicalDevice = factory.createURI(SCHEMA.NAMESPACE, "MedicalDevice");
        MedicalDevicePurpose = factory.createURI(SCHEMA.NAMESPACE, "MedicalDevicePurpose");
        MedicalEntity = factory.createURI(SCHEMA.NAMESPACE, "MedicalEntity");
        MedicalEnumeration = factory.createURI(SCHEMA.NAMESPACE, "MedicalEnumeration");
        MedicalEvidenceLevel = factory.createURI(SCHEMA.NAMESPACE, "MedicalEvidenceLevel");
        MedicalGuideline = factory.createURI(SCHEMA.NAMESPACE, "MedicalGuideline");
        MedicalGuidelineContraindication = factory.createURI(SCHEMA.NAMESPACE, "MedicalGuidelineContraindication");
        MedicalGuidelineRecommendation = factory.createURI(SCHEMA.NAMESPACE, "MedicalGuidelineRecommendation");
        MedicalImagingTechnique = factory.createURI(SCHEMA.NAMESPACE, "MedicalImagingTechnique");
        MedicalIndication = factory.createURI(SCHEMA.NAMESPACE, "MedicalIndication");
        MedicalIntangible = factory.createURI(SCHEMA.NAMESPACE, "MedicalIntangible");
        MedicalObservationalStudy = factory.createURI(SCHEMA.NAMESPACE, "MedicalObservationalStudy");
        MedicalObservationalStudyDesign = factory.createURI(SCHEMA.NAMESPACE, "MedicalObservationalStudyDesign");
        MedicalOrganization = factory.createURI(SCHEMA.NAMESPACE, "MedicalOrganization");
        MedicalProcedure = factory.createURI(SCHEMA.NAMESPACE, "MedicalProcedure");
        MedicalProcedureType = factory.createURI(SCHEMA.NAMESPACE, "MedicalProcedureType");
        MedicalRiskCalculator = factory.createURI(SCHEMA.NAMESPACE, "MedicalRiskCalculator");
        MedicalRiskEstimator = factory.createURI(SCHEMA.NAMESPACE, "MedicalRiskEstimator");
        MedicalRiskFactor = factory.createURI(SCHEMA.NAMESPACE, "MedicalRiskFactor");
        MedicalRiskScore = factory.createURI(SCHEMA.NAMESPACE, "MedicalRiskScore");
        MedicalScholarlyArticle = factory.createURI(SCHEMA.NAMESPACE, "MedicalScholarlyArticle");
        MedicalSign = factory.createURI(SCHEMA.NAMESPACE, "MedicalSign");
        MedicalSignOrSymptom = factory.createURI(SCHEMA.NAMESPACE, "MedicalSignOrSymptom");
        MedicalSpecialty = factory.createURI(SCHEMA.NAMESPACE, "MedicalSpecialty");
        MedicalStudy = factory.createURI(SCHEMA.NAMESPACE, "MedicalStudy");
        MedicalStudyStatus = factory.createURI(SCHEMA.NAMESPACE, "MedicalStudyStatus");
        MedicalSymptom = factory.createURI(SCHEMA.NAMESPACE, "MedicalSymptom");
        MedicalTest = factory.createURI(SCHEMA.NAMESPACE, "MedicalTest");
        MedicalTestPanel = factory.createURI(SCHEMA.NAMESPACE, "MedicalTestPanel");
        MedicalTherapy = factory.createURI(SCHEMA.NAMESPACE, "MedicalTherapy");
        MedicalTrial = factory.createURI(SCHEMA.NAMESPACE, "MedicalTrial");
        MedicalTrialDesign = factory.createURI(SCHEMA.NAMESPACE, "MedicalTrialDesign");
        MedicalWebPage = factory.createURI(SCHEMA.NAMESPACE, "MedicalWebPage");
        MedicineSystem = factory.createURI(SCHEMA.NAMESPACE, "MedicineSystem");
        MensClothingStore = factory.createURI(SCHEMA.NAMESPACE, "MensClothingStore");
        MiddleSchool = factory.createURI(SCHEMA.NAMESPACE, "MiddleSchool");
        MobileApplication = factory.createURI(SCHEMA.NAMESPACE, "MobileApplication");
        MobilePhoneStore = factory.createURI(SCHEMA.NAMESPACE, "MobilePhoneStore");
        Mosque = factory.createURI(SCHEMA.NAMESPACE, "Mosque");
        Motel = factory.createURI(SCHEMA.NAMESPACE, "Motel");
        MotorcycleDealer = factory.createURI(SCHEMA.NAMESPACE, "MotorcycleDealer");
        MotorcycleRepair = factory.createURI(SCHEMA.NAMESPACE, "MotorcycleRepair");
        Mountain = factory.createURI(SCHEMA.NAMESPACE, "Mountain");
        Movie = factory.createURI(SCHEMA.NAMESPACE, "Movie");
        MovieRentalStore = factory.createURI(SCHEMA.NAMESPACE, "MovieRentalStore");
        MovieTheater = factory.createURI(SCHEMA.NAMESPACE, "MovieTheater");
        MovingCompany = factory.createURI(SCHEMA.NAMESPACE, "MovingCompany");
        Muscle = factory.createURI(SCHEMA.NAMESPACE, "Muscle");
        Museum = factory.createURI(SCHEMA.NAMESPACE, "Museum");
        MusicAlbum = factory.createURI(SCHEMA.NAMESPACE, "MusicAlbum");
        MusicEvent = factory.createURI(SCHEMA.NAMESPACE, "MusicEvent");
        MusicGroup = factory.createURI(SCHEMA.NAMESPACE, "MusicGroup");
        MusicPlaylist = factory.createURI(SCHEMA.NAMESPACE, "MusicPlaylist");
        MusicRecording = factory.createURI(SCHEMA.NAMESPACE, "MusicRecording");
        MusicStore = factory.createURI(SCHEMA.NAMESPACE, "MusicStore");
        MusicVenue = factory.createURI(SCHEMA.NAMESPACE, "MusicVenue");
        MusicVideoObject = factory.createURI(SCHEMA.NAMESPACE, "MusicVideoObject");
        NGO = factory.createURI(SCHEMA.NAMESPACE, "NGO");
        NailSalon = factory.createURI(SCHEMA.NAMESPACE, "NailSalon");
        Nerve = factory.createURI(SCHEMA.NAMESPACE, "Nerve");
        NewsArticle = factory.createURI(SCHEMA.NAMESPACE, "NewsArticle");
        NightClub = factory.createURI(SCHEMA.NAMESPACE, "NightClub");
        Notary = factory.createURI(SCHEMA.NAMESPACE, "Notary");
        NutritionInformation = factory.createURI(SCHEMA.NAMESPACE, "NutritionInformation");
        OceanBodyOfWater = factory.createURI(SCHEMA.NAMESPACE, "OceanBodyOfWater");
        Offer = factory.createURI(SCHEMA.NAMESPACE, "Offer");
        OfferItemCondition = factory.createURI(SCHEMA.NAMESPACE, "OfferItemCondition");
        OfficeEquipmentStore = factory.createURI(SCHEMA.NAMESPACE, "OfficeEquipmentStore");
        OpeningHoursSpecification = factory.createURI(SCHEMA.NAMESPACE, "OpeningHoursSpecification");
        Optician = factory.createURI(SCHEMA.NAMESPACE, "Optician");
        Organization = factory.createURI(SCHEMA.NAMESPACE, "Organization");
        OutletStore = factory.createURI(SCHEMA.NAMESPACE, "OutletStore");
        OwnershipInfo = factory.createURI(SCHEMA.NAMESPACE, "OwnershipInfo");
        Painting = factory.createURI(SCHEMA.NAMESPACE, "Painting");
        PalliativeProcedure = factory.createURI(SCHEMA.NAMESPACE, "PalliativeProcedure");
        ParcelService = factory.createURI(SCHEMA.NAMESPACE, "ParcelService");
        Park = factory.createURI(SCHEMA.NAMESPACE, "Park");
        ParkingFacility = factory.createURI(SCHEMA.NAMESPACE, "ParkingFacility");
        PathologyTest = factory.createURI(SCHEMA.NAMESPACE, "PathologyTest");
        PawnShop = factory.createURI(SCHEMA.NAMESPACE, "PawnShop");
        PaymentChargeSpecification = factory.createURI(SCHEMA.NAMESPACE, "PaymentChargeSpecification");
        PaymentMethod = factory.createURI(SCHEMA.NAMESPACE, "PaymentMethod");
        PerformingArtsTheater = factory.createURI(SCHEMA.NAMESPACE, "PerformingArtsTheater");
        PerformingGroup = factory.createURI(SCHEMA.NAMESPACE, "PerformingGroup");
        Person = factory.createURI(SCHEMA.NAMESPACE, "Person");
        PetStore = factory.createURI(SCHEMA.NAMESPACE, "PetStore");
        Pharmacy = factory.createURI(SCHEMA.NAMESPACE, "Pharmacy");
        Photograph = factory.createURI(SCHEMA.NAMESPACE, "Photograph");
        PhysicalActivity = factory.createURI(SCHEMA.NAMESPACE, "PhysicalActivity");
        PhysicalActivityCategory = factory.createURI(SCHEMA.NAMESPACE, "PhysicalActivityCategory");
        PhysicalExam = factory.createURI(SCHEMA.NAMESPACE, "PhysicalExam");
        PhysicalTherapy = factory.createURI(SCHEMA.NAMESPACE, "PhysicalTherapy");
        Physician = factory.createURI(SCHEMA.NAMESPACE, "Physician");
        Place = factory.createURI(SCHEMA.NAMESPACE, "Place");
        PlaceOfWorship = factory.createURI(SCHEMA.NAMESPACE, "PlaceOfWorship");
        Playground = factory.createURI(SCHEMA.NAMESPACE, "Playground");
        Plumber = factory.createURI(SCHEMA.NAMESPACE, "Plumber");
        PoliceStation = factory.createURI(SCHEMA.NAMESPACE, "PoliceStation");
        Pond = factory.createURI(SCHEMA.NAMESPACE, "Pond");
        PostOffice = factory.createURI(SCHEMA.NAMESPACE, "PostOffice");
        PostalAddress = factory.createURI(SCHEMA.NAMESPACE, "PostalAddress");
        Preschool = factory.createURI(SCHEMA.NAMESPACE, "Preschool");
        PreventionIndication = factory.createURI(SCHEMA.NAMESPACE, "PreventionIndication");
        PriceSpecification = factory.createURI(SCHEMA.NAMESPACE, "PriceSpecification");
        Product = factory.createURI(SCHEMA.NAMESPACE, "Product");
        ProductModel = factory.createURI(SCHEMA.NAMESPACE, "ProductModel");
        ProfessionalService = factory.createURI(SCHEMA.NAMESPACE, "ProfessionalService");
        ProfilePage = factory.createURI(SCHEMA.NAMESPACE, "ProfilePage");
        PsychologicalTreatment = factory.createURI(SCHEMA.NAMESPACE, "PsychologicalTreatment");
        PublicSwimmingPool = factory.createURI(SCHEMA.NAMESPACE, "PublicSwimmingPool");
        QualitativeValue = factory.createURI(SCHEMA.NAMESPACE, "QualitativeValue");
        QuantitativeValue = factory.createURI(SCHEMA.NAMESPACE, "QuantitativeValue");
        Quantity = factory.createURI(SCHEMA.NAMESPACE, "Quantity");
        RVPark = factory.createURI(SCHEMA.NAMESPACE, "RVPark");
        RadiationTherapy = factory.createURI(SCHEMA.NAMESPACE, "RadiationTherapy");
        RadioStation = factory.createURI(SCHEMA.NAMESPACE, "RadioStation");
        Rating = factory.createURI(SCHEMA.NAMESPACE, "Rating");
        RealEstateAgent = factory.createURI(SCHEMA.NAMESPACE, "RealEstateAgent");
        Recipe = factory.createURI(SCHEMA.NAMESPACE, "Recipe");
        RecommendedDoseSchedule = factory.createURI(SCHEMA.NAMESPACE, "RecommendedDoseSchedule");
        RecyclingCenter = factory.createURI(SCHEMA.NAMESPACE, "RecyclingCenter");
        ReportedDoseSchedule = factory.createURI(SCHEMA.NAMESPACE, "ReportedDoseSchedule");
        Reservoir = factory.createURI(SCHEMA.NAMESPACE, "Reservoir");
        Residence = factory.createURI(SCHEMA.NAMESPACE, "Residence");
        Restaurant = factory.createURI(SCHEMA.NAMESPACE, "Restaurant");
        Review = factory.createURI(SCHEMA.NAMESPACE, "Review");
        RiverBodyOfWater = factory.createURI(SCHEMA.NAMESPACE, "RiverBodyOfWater");
        RoofingContractor = factory.createURI(SCHEMA.NAMESPACE, "RoofingContractor");
        SaleEvent = factory.createURI(SCHEMA.NAMESPACE, "SaleEvent");
        ScholarlyArticle = factory.createURI(SCHEMA.NAMESPACE, "ScholarlyArticle");
        School = factory.createURI(SCHEMA.NAMESPACE, "School");
        Sculpture = factory.createURI(SCHEMA.NAMESPACE, "Sculpture");
        SeaBodyOfWater = factory.createURI(SCHEMA.NAMESPACE, "SeaBodyOfWater");
        SearchResultsPage = factory.createURI(SCHEMA.NAMESPACE, "SearchResultsPage");
        SelfStorage = factory.createURI(SCHEMA.NAMESPACE, "SelfStorage");
        ShoeStore = factory.createURI(SCHEMA.NAMESPACE, "ShoeStore");
        ShoppingCenter = factory.createURI(SCHEMA.NAMESPACE, "ShoppingCenter");
        SingleFamilyResidence = factory.createURI(SCHEMA.NAMESPACE, "SingleFamilyResidence");
        SiteNavigationElement = factory.createURI(SCHEMA.NAMESPACE, "SiteNavigationElement");
        SkiResort = factory.createURI(SCHEMA.NAMESPACE, "SkiResort");
        SocialEvent = factory.createURI(SCHEMA.NAMESPACE, "SocialEvent");
        SoftwareApplication = factory.createURI(SCHEMA.NAMESPACE, "SoftwareApplication");
        SomeProducts = factory.createURI(SCHEMA.NAMESPACE, "SomeProducts");
        Specialty = factory.createURI(SCHEMA.NAMESPACE, "Specialty");
        SportingGoodsStore = factory.createURI(SCHEMA.NAMESPACE, "SportingGoodsStore");
        SportsActivityLocation = factory.createURI(SCHEMA.NAMESPACE, "SportsActivityLocation");
        SportsClub = factory.createURI(SCHEMA.NAMESPACE, "SportsClub");
        SportsEvent = factory.createURI(SCHEMA.NAMESPACE, "SportsEvent");
        SportsTeam = factory.createURI(SCHEMA.NAMESPACE, "SportsTeam");
        StadiumOrArena = factory.createURI(SCHEMA.NAMESPACE, "StadiumOrArena");
        State = factory.createURI(SCHEMA.NAMESPACE, "State");
        Store = factory.createURI(SCHEMA.NAMESPACE, "Store");
        StructuredValue = factory.createURI(SCHEMA.NAMESPACE, "StructuredValue");
        SubwayStation = factory.createURI(SCHEMA.NAMESPACE, "SubwayStation");
        SuperficialAnatomy = factory.createURI(SCHEMA.NAMESPACE, "SuperficialAnatomy");
        Synagogue = factory.createURI(SCHEMA.NAMESPACE, "Synagogue");
        TVEpisode = factory.createURI(SCHEMA.NAMESPACE, "TVEpisode");
        TVSeason = factory.createURI(SCHEMA.NAMESPACE, "TVSeason");
        TVSeries = factory.createURI(SCHEMA.NAMESPACE, "TVSeries");
        Table = factory.createURI(SCHEMA.NAMESPACE, "Table");
        TattooParlor = factory.createURI(SCHEMA.NAMESPACE, "TattooParlor");
        TaxiStand = factory.createURI(SCHEMA.NAMESPACE, "TaxiStand");
        TelevisionStation = factory.createURI(SCHEMA.NAMESPACE, "TelevisionStation");
        TennisComplex = factory.createURI(SCHEMA.NAMESPACE, "TennisComplex");
        TheaterEvent = factory.createURI(SCHEMA.NAMESPACE, "TheaterEvent");
        TheaterGroup = factory.createURI(SCHEMA.NAMESPACE, "TheaterGroup");
        TherapeuticProcedure = factory.createURI(SCHEMA.NAMESPACE, "TherapeuticProcedure");
        Thing = factory.createURI(SCHEMA.NAMESPACE, "Thing");
        TireShop = factory.createURI(SCHEMA.NAMESPACE, "TireShop");
        TouristAttraction = factory.createURI(SCHEMA.NAMESPACE, "TouristAttraction");
        TouristInformationCenter = factory.createURI(SCHEMA.NAMESPACE, "TouristInformationCenter");
        ToyStore = factory.createURI(SCHEMA.NAMESPACE, "ToyStore");
        TrainStation = factory.createURI(SCHEMA.NAMESPACE, "TrainStation");
        TravelAgency = factory.createURI(SCHEMA.NAMESPACE, "TravelAgency");
        TreatmentIndication = factory.createURI(SCHEMA.NAMESPACE, "TreatmentIndication");
        TypeAndQuantityNode = factory.createURI(SCHEMA.NAMESPACE, "TypeAndQuantityNode");
        UnitPriceSpecification = factory.createURI(SCHEMA.NAMESPACE, "UnitPriceSpecification");
        UserBlocks = factory.createURI(SCHEMA.NAMESPACE, "UserBlocks");
        UserCheckins = factory.createURI(SCHEMA.NAMESPACE, "UserCheckins");
        UserComments = factory.createURI(SCHEMA.NAMESPACE, "UserComments");
        UserDownloads = factory.createURI(SCHEMA.NAMESPACE, "UserDownloads");
        UserInteraction = factory.createURI(SCHEMA.NAMESPACE, "UserInteraction");
        UserLikes = factory.createURI(SCHEMA.NAMESPACE, "UserLikes");
        UserPageVisits = factory.createURI(SCHEMA.NAMESPACE, "UserPageVisits");
        UserPlays = factory.createURI(SCHEMA.NAMESPACE, "UserPlays");
        UserPlusOnes = factory.createURI(SCHEMA.NAMESPACE, "UserPlusOnes");
        UserTweets = factory.createURI(SCHEMA.NAMESPACE, "UserTweets");
        Vein = factory.createURI(SCHEMA.NAMESPACE, "Vein");
        Vessel = factory.createURI(SCHEMA.NAMESPACE, "Vessel");
        VeterinaryCare = factory.createURI(SCHEMA.NAMESPACE, "VeterinaryCare");
        VideoGallery = factory.createURI(SCHEMA.NAMESPACE, "VideoGallery");
        VideoObject = factory.createURI(SCHEMA.NAMESPACE, "VideoObject");
        VisualArtsEvent = factory.createURI(SCHEMA.NAMESPACE, "VisualArtsEvent");
        Volcano = factory.createURI(SCHEMA.NAMESPACE, "Volcano");
        WPAdBlock = factory.createURI(SCHEMA.NAMESPACE, "WPAdBlock");
        WPFooter = factory.createURI(SCHEMA.NAMESPACE, "WPFooter");
        WPHeader = factory.createURI(SCHEMA.NAMESPACE, "WPHeader");
        WPSideBar = factory.createURI(SCHEMA.NAMESPACE, "WPSideBar");
        WarrantyPromise = factory.createURI(SCHEMA.NAMESPACE, "WarrantyPromise");
        WarrantyScope = factory.createURI(SCHEMA.NAMESPACE, "WarrantyScope");
        Waterfall = factory.createURI(SCHEMA.NAMESPACE, "Waterfall");
        WebApplication = factory.createURI(SCHEMA.NAMESPACE, "WebApplication");
        WebPage = factory.createURI(SCHEMA.NAMESPACE, "WebPage");
        WebPageElement = factory.createURI(SCHEMA.NAMESPACE, "WebPageElement");
        WholesaleStore = factory.createURI(SCHEMA.NAMESPACE, "WholesaleStore");
        Winery = factory.createURI(SCHEMA.NAMESPACE, "Winery");
        Zoo = factory.createURI(SCHEMA.NAMESPACE, "Zoo");
        about = factory.createURI(SCHEMA.NAMESPACE, "about");
        acceptedPaymentMethod = factory.createURI(SCHEMA.NAMESPACE, "acceptedPaymentMethod");
        acceptsReservations = factory.createURI(SCHEMA.NAMESPACE, "acceptsReservations");
        accountablePerson = factory.createURI(SCHEMA.NAMESPACE, "accountablePerson");
        acquiredFrom = factory.createURI(SCHEMA.NAMESPACE, "acquiredFrom");
        action = factory.createURI(SCHEMA.NAMESPACE, "action");
        activeIngredient = factory.createURI(SCHEMA.NAMESPACE, "activeIngredient");
        activityDuration = factory.createURI(SCHEMA.NAMESPACE, "activityDuration");
        activityFrequency = factory.createURI(SCHEMA.NAMESPACE, "activityFrequency");
        actor = factory.createURI(SCHEMA.NAMESPACE, "actor");
        actors = factory.createURI(SCHEMA.NAMESPACE, "actors");
        addOn = factory.createURI(SCHEMA.NAMESPACE, "addOn");
        additionalName = factory.createURI(SCHEMA.NAMESPACE, "additionalName");
        additionalType = factory.createURI(SCHEMA.NAMESPACE, "additionalType");
        additionalVariable = factory.createURI(SCHEMA.NAMESPACE, "additionalVariable");
        address = factory.createURI(SCHEMA.NAMESPACE, "address");
        addressCountry = factory.createURI(SCHEMA.NAMESPACE, "addressCountry");
        addressLocality = factory.createURI(SCHEMA.NAMESPACE, "addressLocality");
        addressRegion = factory.createURI(SCHEMA.NAMESPACE, "addressRegion");
        administrationRoute = factory.createURI(SCHEMA.NAMESPACE, "administrationRoute");
        advanceBookingRequirement = factory.createURI(SCHEMA.NAMESPACE, "advanceBookingRequirement");
        adverseOutcome = factory.createURI(SCHEMA.NAMESPACE, "adverseOutcome");
        affectedBy = factory.createURI(SCHEMA.NAMESPACE, "affectedBy");
        affiliation = factory.createURI(SCHEMA.NAMESPACE, "affiliation");
        aggregateRating = factory.createURI(SCHEMA.NAMESPACE, "aggregateRating");
        album = factory.createURI(SCHEMA.NAMESPACE, "album");
        albums = factory.createURI(SCHEMA.NAMESPACE, "albums");
        alcoholWarning = factory.createURI(SCHEMA.NAMESPACE, "alcoholWarning");
        algorithm = factory.createURI(SCHEMA.NAMESPACE, "algorithm");
        alternateName = factory.createURI(SCHEMA.NAMESPACE, "alternateName");
        alternativeHeadline = factory.createURI(SCHEMA.NAMESPACE, "alternativeHeadline");
        alumni = factory.createURI(SCHEMA.NAMESPACE, "alumni");
        alumniOf = factory.createURI(SCHEMA.NAMESPACE, "alumniOf");
        amountOfThisGood = factory.createURI(SCHEMA.NAMESPACE, "amountOfThisGood");
        antagonist = factory.createURI(SCHEMA.NAMESPACE, "antagonist");
        applicableLocation = factory.createURI(SCHEMA.NAMESPACE, "applicableLocation");
        applicationCategory = factory.createURI(SCHEMA.NAMESPACE, "applicationCategory");
        applicationSubCategory = factory.createURI(SCHEMA.NAMESPACE, "applicationSubCategory");
        applicationSuite = factory.createURI(SCHEMA.NAMESPACE, "applicationSuite");
        appliesToDeliveryMethod = factory.createURI(SCHEMA.NAMESPACE, "appliesToDeliveryMethod");
        appliesToPaymentMethod = factory.createURI(SCHEMA.NAMESPACE, "appliesToPaymentMethod");
        arterialBranch = factory.createURI(SCHEMA.NAMESPACE, "arterialBranch");
        articleBody = factory.createURI(SCHEMA.NAMESPACE, "articleBody");
        articleSection = factory.createURI(SCHEMA.NAMESPACE, "articleSection");
        aspect = factory.createURI(SCHEMA.NAMESPACE, "aspect");
        associatedAnatomy = factory.createURI(SCHEMA.NAMESPACE, "associatedAnatomy");
        associatedArticle = factory.createURI(SCHEMA.NAMESPACE, "associatedArticle");
        associatedMedia = factory.createURI(SCHEMA.NAMESPACE, "associatedMedia");
        associatedPathophysiology = factory.createURI(SCHEMA.NAMESPACE, "associatedPathophysiology");
        attendee = factory.createURI(SCHEMA.NAMESPACE, "attendee");
        attendees = factory.createURI(SCHEMA.NAMESPACE, "attendees");
        audience = factory.createURI(SCHEMA.NAMESPACE, "audience");
        audio = factory.createURI(SCHEMA.NAMESPACE, "audio");
        author = factory.createURI(SCHEMA.NAMESPACE, "author");
        availability = factory.createURI(SCHEMA.NAMESPACE, "availability");
        availabilityEnds = factory.createURI(SCHEMA.NAMESPACE, "availabilityEnds");
        availabilityStarts = factory.createURI(SCHEMA.NAMESPACE, "availabilityStarts");
        availableAtOrFrom = factory.createURI(SCHEMA.NAMESPACE, "availableAtOrFrom");
        availableDeliveryMethod = factory.createURI(SCHEMA.NAMESPACE, "availableDeliveryMethod");
        availableIn = factory.createURI(SCHEMA.NAMESPACE, "availableIn");
        availableService = factory.createURI(SCHEMA.NAMESPACE, "availableService");
        availableStrength = factory.createURI(SCHEMA.NAMESPACE, "availableStrength");
        availableTest = factory.createURI(SCHEMA.NAMESPACE, "availableTest");
        award = factory.createURI(SCHEMA.NAMESPACE, "award");
        awards = factory.createURI(SCHEMA.NAMESPACE, "awards");
        background = factory.createURI(SCHEMA.NAMESPACE, "background");
        baseSalary = factory.createURI(SCHEMA.NAMESPACE, "baseSalary");
        benefits = factory.createURI(SCHEMA.NAMESPACE, "benefits");
        bestRating = factory.createURI(SCHEMA.NAMESPACE, "bestRating");
        billingIncrement = factory.createURI(SCHEMA.NAMESPACE, "billingIncrement");
        biomechnicalClass = factory.createURI(SCHEMA.NAMESPACE, "biomechnicalClass");
        birthDate = factory.createURI(SCHEMA.NAMESPACE, "birthDate");
        bitrate = factory.createURI(SCHEMA.NAMESPACE, "bitrate");
        blogPost = factory.createURI(SCHEMA.NAMESPACE, "blogPost");
        blogPosts = factory.createURI(SCHEMA.NAMESPACE, "blogPosts");
        bloodSupply = factory.createURI(SCHEMA.NAMESPACE, "bloodSupply");
        bodyLocation = factory.createURI(SCHEMA.NAMESPACE, "bodyLocation");
        bookEdition = factory.createURI(SCHEMA.NAMESPACE, "bookEdition");
        bookFormat = factory.createURI(SCHEMA.NAMESPACE, "bookFormat");
        box = factory.createURI(SCHEMA.NAMESPACE, "box");
        branch = factory.createURI(SCHEMA.NAMESPACE, "branch");
        branchOf = factory.createURI(SCHEMA.NAMESPACE, "branchOf");
        brand = factory.createURI(SCHEMA.NAMESPACE, "brand");
        breadcrumb = factory.createURI(SCHEMA.NAMESPACE, "breadcrumb");
        breastfeedingWarning = factory.createURI(SCHEMA.NAMESPACE, "breastfeedingWarning");
        browserRequirements = factory.createURI(SCHEMA.NAMESPACE, "browserRequirements");
        businessFunction = factory.createURI(SCHEMA.NAMESPACE, "businessFunction");
        byArtist = factory.createURI(SCHEMA.NAMESPACE, "byArtist");
        calories = factory.createURI(SCHEMA.NAMESPACE, "calories");
        caption = factory.createURI(SCHEMA.NAMESPACE, "caption");
        carbohydrateContent = factory.createURI(SCHEMA.NAMESPACE, "carbohydrateContent");
        carrierRequirements = factory.createURI(SCHEMA.NAMESPACE, "carrierRequirements");
        category = factory.createURI(SCHEMA.NAMESPACE, "category");
        cause = factory.createURI(SCHEMA.NAMESPACE, "cause");
        causeOf = factory.createURI(SCHEMA.NAMESPACE, "causeOf");
        children = factory.createURI(SCHEMA.NAMESPACE, "children");
        cholesterolContent = factory.createURI(SCHEMA.NAMESPACE, "cholesterolContent");
        circle = factory.createURI(SCHEMA.NAMESPACE, "circle");
        citation = factory.createURI(SCHEMA.NAMESPACE, "citation");
        clincalPharmacology = factory.createURI(SCHEMA.NAMESPACE, "clincalPharmacology");
        closes = factory.createURI(SCHEMA.NAMESPACE, "closes");
        code = factory.createURI(SCHEMA.NAMESPACE, "code");
        codeValue = factory.createURI(SCHEMA.NAMESPACE, "codeValue");
        codingSystem = factory.createURI(SCHEMA.NAMESPACE, "codingSystem");
        colleague = factory.createURI(SCHEMA.NAMESPACE, "colleague");
        colleagues = factory.createURI(SCHEMA.NAMESPACE, "colleagues");
        color = factory.createURI(SCHEMA.NAMESPACE, "color");
        comment = factory.createURI(SCHEMA.NAMESPACE, "comment");
        commentText = factory.createURI(SCHEMA.NAMESPACE, "commentText");
        commentTime = factory.createURI(SCHEMA.NAMESPACE, "commentTime");
        comprisedOf = factory.createURI(SCHEMA.NAMESPACE, "comprisedOf");
        connectedTo = factory.createURI(SCHEMA.NAMESPACE, "connectedTo");
        contactPoint = factory.createURI(SCHEMA.NAMESPACE, "contactPoint");
        contactPoints = factory.createURI(SCHEMA.NAMESPACE, "contactPoints");
        contactType = factory.createURI(SCHEMA.NAMESPACE, "contactType");
        containedIn = factory.createURI(SCHEMA.NAMESPACE, "containedIn");
        contentLocation = factory.createURI(SCHEMA.NAMESPACE, "contentLocation");
        contentRating = factory.createURI(SCHEMA.NAMESPACE, "contentRating");
        contentSize = factory.createURI(SCHEMA.NAMESPACE, "contentSize");
        contentUrl = factory.createURI(SCHEMA.NAMESPACE, "contentUrl");
        contraindication = factory.createURI(SCHEMA.NAMESPACE, "contraindication");
        contributor = factory.createURI(SCHEMA.NAMESPACE, "contributor");
        cookTime = factory.createURI(SCHEMA.NAMESPACE, "cookTime");
        cookingMethod = factory.createURI(SCHEMA.NAMESPACE, "cookingMethod");
        copyrightHolder = factory.createURI(SCHEMA.NAMESPACE, "copyrightHolder");
        copyrightYear = factory.createURI(SCHEMA.NAMESPACE, "copyrightYear");
        cost = factory.createURI(SCHEMA.NAMESPACE, "cost");
        costCategory = factory.createURI(SCHEMA.NAMESPACE, "costCategory");
        costCurrency = factory.createURI(SCHEMA.NAMESPACE, "costCurrency");
        costOrigin = factory.createURI(SCHEMA.NAMESPACE, "costOrigin");
        costPerUnit = factory.createURI(SCHEMA.NAMESPACE, "costPerUnit");
        countriesNotSupported = factory.createURI(SCHEMA.NAMESPACE, "countriesNotSupported");
        countriesSupported = factory.createURI(SCHEMA.NAMESPACE, "countriesSupported");
        creator = factory.createURI(SCHEMA.NAMESPACE, "creator");
        currenciesAccepted = factory.createURI(SCHEMA.NAMESPACE, "currenciesAccepted");
        dateCreated = factory.createURI(SCHEMA.NAMESPACE, "dateCreated");
        dateModified = factory.createURI(SCHEMA.NAMESPACE, "dateModified");
        datePosted = factory.createURI(SCHEMA.NAMESPACE, "datePosted");
        datePublished = factory.createURI(SCHEMA.NAMESPACE, "datePublished");
        dateline = factory.createURI(SCHEMA.NAMESPACE, "dateline");
        dayOfWeek = factory.createURI(SCHEMA.NAMESPACE, "dayOfWeek");
        deathDate = factory.createURI(SCHEMA.NAMESPACE, "deathDate");
        deliveryLeadTime = factory.createURI(SCHEMA.NAMESPACE, "deliveryLeadTime");
        depth = factory.createURI(SCHEMA.NAMESPACE, "depth");
        description = factory.createURI(SCHEMA.NAMESPACE, "description");
        device = factory.createURI(SCHEMA.NAMESPACE, "device");
        diagnosis = factory.createURI(SCHEMA.NAMESPACE, "diagnosis");
        diagram = factory.createURI(SCHEMA.NAMESPACE, "diagram");
        dietFeatures = factory.createURI(SCHEMA.NAMESPACE, "dietFeatures");
        differentialDiagnosis = factory.createURI(SCHEMA.NAMESPACE, "differentialDiagnosis");
        director = factory.createURI(SCHEMA.NAMESPACE, "director");
        discusses = factory.createURI(SCHEMA.NAMESPACE, "discusses");
        discussionUrl = factory.createURI(SCHEMA.NAMESPACE, "discussionUrl");
        distinguishingSign = factory.createURI(SCHEMA.NAMESPACE, "distinguishingSign");
        dosageForm = factory.createURI(SCHEMA.NAMESPACE, "dosageForm");
        doseSchedule = factory.createURI(SCHEMA.NAMESPACE, "doseSchedule");
        doseUnit = factory.createURI(SCHEMA.NAMESPACE, "doseUnit");
        doseValue = factory.createURI(SCHEMA.NAMESPACE, "doseValue");
        downloadUrl = factory.createURI(SCHEMA.NAMESPACE, "downloadUrl");
        drainsTo = factory.createURI(SCHEMA.NAMESPACE, "drainsTo");
        drug = factory.createURI(SCHEMA.NAMESPACE, "drug");
        drugClass = factory.createURI(SCHEMA.NAMESPACE, "drugClass");
        drugUnit = factory.createURI(SCHEMA.NAMESPACE, "drugUnit");
        duns = factory.createURI(SCHEMA.NAMESPACE, "duns");
        duplicateTherapy = factory.createURI(SCHEMA.NAMESPACE, "duplicateTherapy");
        duration = factory.createURI(SCHEMA.NAMESPACE, "duration");
        durationOfWarranty = factory.createURI(SCHEMA.NAMESPACE, "durationOfWarranty");
        editor = factory.createURI(SCHEMA.NAMESPACE, "editor");
        educationRequirements = factory.createURI(SCHEMA.NAMESPACE, "educationRequirements");
        elevation = factory.createURI(SCHEMA.NAMESPACE, "elevation");
        eligibleCustomerType = factory.createURI(SCHEMA.NAMESPACE, "eligibleCustomerType");
        eligibleDuration = factory.createURI(SCHEMA.NAMESPACE, "eligibleDuration");
        eligibleQuantity = factory.createURI(SCHEMA.NAMESPACE, "eligibleQuantity");
        eligibleRegion = factory.createURI(SCHEMA.NAMESPACE, "eligibleRegion");
        eligibleTransactionVolume = factory.createURI(SCHEMA.NAMESPACE, "eligibleTransactionVolume");
        email = factory.createURI(SCHEMA.NAMESPACE, "email");
        embedUrl = factory.createURI(SCHEMA.NAMESPACE, "embedUrl");
        employee = factory.createURI(SCHEMA.NAMESPACE, "employee");
        employees = factory.createURI(SCHEMA.NAMESPACE, "employees");
        employmentType = factory.createURI(SCHEMA.NAMESPACE, "employmentType");
        encodesCreativeWork = factory.createURI(SCHEMA.NAMESPACE, "encodesCreativeWork");
        encoding = factory.createURI(SCHEMA.NAMESPACE, "encoding");
        encodingFormat = factory.createURI(SCHEMA.NAMESPACE, "encodingFormat");
        encodings = factory.createURI(SCHEMA.NAMESPACE, "encodings");
        endDate = factory.createURI(SCHEMA.NAMESPACE, "endDate");
        endorsers = factory.createURI(SCHEMA.NAMESPACE, "endorsers");
        epidemiology = factory.createURI(SCHEMA.NAMESPACE, "epidemiology");
        episode = factory.createURI(SCHEMA.NAMESPACE, "episode");
        episodeNumber = factory.createURI(SCHEMA.NAMESPACE, "episodeNumber");
        episodes = factory.createURI(SCHEMA.NAMESPACE, "episodes");
        equal = factory.createURI(SCHEMA.NAMESPACE, "equal");
        estimatesRiskOf = factory.createURI(SCHEMA.NAMESPACE, "estimatesRiskOf");
        event = factory.createURI(SCHEMA.NAMESPACE, "event");
        events = factory.createURI(SCHEMA.NAMESPACE, "events");
        evidenceLevel = factory.createURI(SCHEMA.NAMESPACE, "evidenceLevel");
        evidenceOrigin = factory.createURI(SCHEMA.NAMESPACE, "evidenceOrigin");
        exerciseType = factory.createURI(SCHEMA.NAMESPACE, "exerciseType");
        exifData = factory.createURI(SCHEMA.NAMESPACE, "exifData");
        expectedPrognosis = factory.createURI(SCHEMA.NAMESPACE, "expectedPrognosis");
        experienceRequirements = factory.createURI(SCHEMA.NAMESPACE, "experienceRequirements");
        expertConsiderations = factory.createURI(SCHEMA.NAMESPACE, "expertConsiderations");
        expires = factory.createURI(SCHEMA.NAMESPACE, "expires");
        familyName = factory.createURI(SCHEMA.NAMESPACE, "familyName");
        fatContent = factory.createURI(SCHEMA.NAMESPACE, "fatContent");
        faxNumber = factory.createURI(SCHEMA.NAMESPACE, "faxNumber");
        featureList = factory.createURI(SCHEMA.NAMESPACE, "featureList");
        fiberContent = factory.createURI(SCHEMA.NAMESPACE, "fiberContent");
        fileFormat = factory.createURI(SCHEMA.NAMESPACE, "fileFormat");
        fileSize = factory.createURI(SCHEMA.NAMESPACE, "fileSize");
        follows = factory.createURI(SCHEMA.NAMESPACE, "follows");
        followup = factory.createURI(SCHEMA.NAMESPACE, "followup");
        foodWarning = factory.createURI(SCHEMA.NAMESPACE, "foodWarning");
        founder = factory.createURI(SCHEMA.NAMESPACE, "founder");
        founders = factory.createURI(SCHEMA.NAMESPACE, "founders");
        foundingDate = factory.createURI(SCHEMA.NAMESPACE, "foundingDate");
        frequency = factory.createURI(SCHEMA.NAMESPACE, "frequency");
        function = factory.createURI(SCHEMA.NAMESPACE, "function");
        functionalClass = factory.createURI(SCHEMA.NAMESPACE, "functionalClass");
        gender = factory.createURI(SCHEMA.NAMESPACE, "gender");
        genre = factory.createURI(SCHEMA.NAMESPACE, "genre");
        geo = factory.createURI(SCHEMA.NAMESPACE, "geo");
        givenName = factory.createURI(SCHEMA.NAMESPACE, "givenName");
        globalLocationNumber = factory.createURI(SCHEMA.NAMESPACE, "globalLocationNumber");
        greater = factory.createURI(SCHEMA.NAMESPACE, "greater");
        greaterOrEqual = factory.createURI(SCHEMA.NAMESPACE, "greaterOrEqual");
        gtin13 = factory.createURI(SCHEMA.NAMESPACE, "gtin13");
        gtin14 = factory.createURI(SCHEMA.NAMESPACE, "gtin14");
        gtin8 = factory.createURI(SCHEMA.NAMESPACE, "gtin8");
        guideline = factory.createURI(SCHEMA.NAMESPACE, "guideline");
        guidelineDate = factory.createURI(SCHEMA.NAMESPACE, "guidelineDate");
        guidelineSubject = factory.createURI(SCHEMA.NAMESPACE, "guidelineSubject");
        hasPOS = factory.createURI(SCHEMA.NAMESPACE, "hasPOS");
        headline = factory.createURI(SCHEMA.NAMESPACE, "headline");
        height = factory.createURI(SCHEMA.NAMESPACE, "height");
        highPrice = factory.createURI(SCHEMA.NAMESPACE, "highPrice");
        hiringOrganization = factory.createURI(SCHEMA.NAMESPACE, "hiringOrganization");
        homeLocation = factory.createURI(SCHEMA.NAMESPACE, "homeLocation");
        honorificPrefix = factory.createURI(SCHEMA.NAMESPACE, "honorificPrefix");
        honorificSuffix = factory.createURI(SCHEMA.NAMESPACE, "honorificSuffix");
        hospitalAffiliation = factory.createURI(SCHEMA.NAMESPACE, "hospitalAffiliation");
        howPerformed = factory.createURI(SCHEMA.NAMESPACE, "howPerformed");
        identifyingExam = factory.createURI(SCHEMA.NAMESPACE, "identifyingExam");
        identifyingTest = factory.createURI(SCHEMA.NAMESPACE, "identifyingTest");
        illustrator = factory.createURI(SCHEMA.NAMESPACE, "illustrator");
        image = factory.createURI(SCHEMA.NAMESPACE, "image");
        imagingTechnique = factory.createURI(SCHEMA.NAMESPACE, "imagingTechnique");
        inAlbum = factory.createURI(SCHEMA.NAMESPACE, "inAlbum");
        inLanguage = factory.createURI(SCHEMA.NAMESPACE, "inLanguage");
        inPlaylist = factory.createURI(SCHEMA.NAMESPACE, "inPlaylist");
        incentives = factory.createURI(SCHEMA.NAMESPACE, "incentives");
        includedRiskFactor = factory.createURI(SCHEMA.NAMESPACE, "includedRiskFactor");
        includesObject = factory.createURI(SCHEMA.NAMESPACE, "includesObject");
        increasesRiskOf = factory.createURI(SCHEMA.NAMESPACE, "increasesRiskOf");
        indication = factory.createURI(SCHEMA.NAMESPACE, "indication");
        industry = factory.createURI(SCHEMA.NAMESPACE, "industry");
        infectiousAgent = factory.createURI(SCHEMA.NAMESPACE, "infectiousAgent");
        infectiousAgentClass = factory.createURI(SCHEMA.NAMESPACE, "infectiousAgentClass");
        ingredients = factory.createURI(SCHEMA.NAMESPACE, "ingredients");
        insertion = factory.createURI(SCHEMA.NAMESPACE, "insertion");
        installUrl = factory.createURI(SCHEMA.NAMESPACE, "installUrl");
        intensity = factory.createURI(SCHEMA.NAMESPACE, "intensity");
        interactingDrug = factory.createURI(SCHEMA.NAMESPACE, "interactingDrug");
        interactionCount = factory.createURI(SCHEMA.NAMESPACE, "interactionCount");
        inventoryLevel = factory.createURI(SCHEMA.NAMESPACE, "inventoryLevel");
        isAccessoryOrSparePartFor = factory.createURI(SCHEMA.NAMESPACE, "isAccessoryOrSparePartFor");
        isAvailableGenerically = factory.createURI(SCHEMA.NAMESPACE, "isAvailableGenerically");
        isConsumableFor = factory.createURI(SCHEMA.NAMESPACE, "isConsumableFor");
        isFamilyFriendly = factory.createURI(SCHEMA.NAMESPACE, "isFamilyFriendly");
        isPartOf = factory.createURI(SCHEMA.NAMESPACE, "isPartOf");
        isProprietary = factory.createURI(SCHEMA.NAMESPACE, "isProprietary");
        isRelatedTo = factory.createURI(SCHEMA.NAMESPACE, "isRelatedTo");
        isSimilarTo = factory.createURI(SCHEMA.NAMESPACE, "isSimilarTo");
        isVariantOf = factory.createURI(SCHEMA.NAMESPACE, "isVariantOf");
        isbn = factory.createURI(SCHEMA.NAMESPACE, "isbn");
        isicV4 = factory.createURI(SCHEMA.NAMESPACE, "isicV4");
        itemCondition = factory.createURI(SCHEMA.NAMESPACE, "itemCondition");
        itemListElement = factory.createURI(SCHEMA.NAMESPACE, "itemListElement");
        itemListOrder = factory.createURI(SCHEMA.NAMESPACE, "itemListOrder");
        itemOffered = factory.createURI(SCHEMA.NAMESPACE, "itemOffered");
        itemReviewed = factory.createURI(SCHEMA.NAMESPACE, "itemReviewed");
        jobLocation = factory.createURI(SCHEMA.NAMESPACE, "jobLocation");
        jobTitle = factory.createURI(SCHEMA.NAMESPACE, "jobTitle");
        keywords = factory.createURI(SCHEMA.NAMESPACE, "keywords");
        knows = factory.createURI(SCHEMA.NAMESPACE, "knows");
        labelDetails = factory.createURI(SCHEMA.NAMESPACE, "labelDetails");
        lastReviewed = factory.createURI(SCHEMA.NAMESPACE, "lastReviewed");
        latitude = factory.createURI(SCHEMA.NAMESPACE, "latitude");
        legalName = factory.createURI(SCHEMA.NAMESPACE, "legalName");
        legalStatus = factory.createURI(SCHEMA.NAMESPACE, "legalStatus");
        lesser = factory.createURI(SCHEMA.NAMESPACE, "lesser");
        lesserOrEqual = factory.createURI(SCHEMA.NAMESPACE, "lesserOrEqual");
        line = factory.createURI(SCHEMA.NAMESPACE, "line");
        location = factory.createURI(SCHEMA.NAMESPACE, "location");
        logo = factory.createURI(SCHEMA.NAMESPACE, "logo");
        longitude = factory.createURI(SCHEMA.NAMESPACE, "longitude");
        lowPrice = factory.createURI(SCHEMA.NAMESPACE, "lowPrice");
        mainContentOfPage = factory.createURI(SCHEMA.NAMESPACE, "mainContentOfPage");
        makesOffer = factory.createURI(SCHEMA.NAMESPACE, "makesOffer");
        manufacturer = factory.createURI(SCHEMA.NAMESPACE, "manufacturer");
        map = factory.createURI(SCHEMA.NAMESPACE, "map");
        maps = factory.createURI(SCHEMA.NAMESPACE, "maps");
        maxPrice = factory.createURI(SCHEMA.NAMESPACE, "maxPrice");
        maxValue = factory.createURI(SCHEMA.NAMESPACE, "maxValue");
        maximumIntake = factory.createURI(SCHEMA.NAMESPACE, "maximumIntake");
        mechanismOfAction = factory.createURI(SCHEMA.NAMESPACE, "mechanismOfAction");
        medicalSpecialty = factory.createURI(SCHEMA.NAMESPACE, "medicalSpecialty");
        medicineSystem = factory.createURI(SCHEMA.NAMESPACE, "medicineSystem");
        member = factory.createURI(SCHEMA.NAMESPACE, "member");
        memberOf = factory.createURI(SCHEMA.NAMESPACE, "memberOf");
        members = factory.createURI(SCHEMA.NAMESPACE, "members");
        memoryRequirements = factory.createURI(SCHEMA.NAMESPACE, "memoryRequirements");
        mentions = factory.createURI(SCHEMA.NAMESPACE, "mentions");
        menu = factory.createURI(SCHEMA.NAMESPACE, "menu");
        minPrice = factory.createURI(SCHEMA.NAMESPACE, "minPrice");
        minValue = factory.createURI(SCHEMA.NAMESPACE, "minValue");
        model = factory.createURI(SCHEMA.NAMESPACE, "model");
        mpn = factory.createURI(SCHEMA.NAMESPACE, "mpn");
        musicBy = factory.createURI(SCHEMA.NAMESPACE, "musicBy");
        musicGroupMember = factory.createURI(SCHEMA.NAMESPACE, "musicGroupMember");
        naics = factory.createURI(SCHEMA.NAMESPACE, "naics");
        name = factory.createURI(SCHEMA.NAMESPACE, "name");
        nationality = factory.createURI(SCHEMA.NAMESPACE, "nationality");
        naturalProgression = factory.createURI(SCHEMA.NAMESPACE, "naturalProgression");
        nerve = factory.createURI(SCHEMA.NAMESPACE, "nerve");
        nerveMotor = factory.createURI(SCHEMA.NAMESPACE, "nerveMotor");
        nonEqual = factory.createURI(SCHEMA.NAMESPACE, "nonEqual");
        nonProprietaryName = factory.createURI(SCHEMA.NAMESPACE, "nonProprietaryName");
        normalRange = factory.createURI(SCHEMA.NAMESPACE, "normalRange");
        numTracks = factory.createURI(SCHEMA.NAMESPACE, "numTracks");
        numberOfEpisodes = factory.createURI(SCHEMA.NAMESPACE, "numberOfEpisodes");
        numberOfPages = factory.createURI(SCHEMA.NAMESPACE, "numberOfPages");
        nutrition = factory.createURI(SCHEMA.NAMESPACE, "nutrition");
        occupationalCategory = factory.createURI(SCHEMA.NAMESPACE, "occupationalCategory");
        offerCount = factory.createURI(SCHEMA.NAMESPACE, "offerCount");
        offers = factory.createURI(SCHEMA.NAMESPACE, "offers");
        openingHours = factory.createURI(SCHEMA.NAMESPACE, "openingHours");
        openingHoursSpecification = factory.createURI(SCHEMA.NAMESPACE, "openingHoursSpecification");
        opens = factory.createURI(SCHEMA.NAMESPACE, "opens");
        operatingSystem = factory.createURI(SCHEMA.NAMESPACE, "operatingSystem");
        origin = factory.createURI(SCHEMA.NAMESPACE, "origin");
        originatesFrom = factory.createURI(SCHEMA.NAMESPACE, "originatesFrom");
        outcome = factory.createURI(SCHEMA.NAMESPACE, "outcome");
        overdosage = factory.createURI(SCHEMA.NAMESPACE, "overdosage");
        overview = factory.createURI(SCHEMA.NAMESPACE, "overview");
        ownedFrom = factory.createURI(SCHEMA.NAMESPACE, "ownedFrom");
        ownedThrough = factory.createURI(SCHEMA.NAMESPACE, "ownedThrough");
        owns = factory.createURI(SCHEMA.NAMESPACE, "owns");
        parent = factory.createURI(SCHEMA.NAMESPACE, "parent");
        parents = factory.createURI(SCHEMA.NAMESPACE, "parents");
        partOfSeason = factory.createURI(SCHEMA.NAMESPACE, "partOfSeason");
        partOfSystem = factory.createURI(SCHEMA.NAMESPACE, "partOfSystem");
        partOfTVSeries = factory.createURI(SCHEMA.NAMESPACE, "partOfTVSeries");
        pathophysiology = factory.createURI(SCHEMA.NAMESPACE, "pathophysiology");
        paymentAccepted = factory.createURI(SCHEMA.NAMESPACE, "paymentAccepted");
        performer = factory.createURI(SCHEMA.NAMESPACE, "performer");
        performerIn = factory.createURI(SCHEMA.NAMESPACE, "performerIn");
        performers = factory.createURI(SCHEMA.NAMESPACE, "performers");
        permissions = factory.createURI(SCHEMA.NAMESPACE, "permissions");
        phase = factory.createURI(SCHEMA.NAMESPACE, "phase");
        photo = factory.createURI(SCHEMA.NAMESPACE, "photo");
        photos = factory.createURI(SCHEMA.NAMESPACE, "photos");
        physiologicalBenefits = factory.createURI(SCHEMA.NAMESPACE, "physiologicalBenefits");
        playerType = factory.createURI(SCHEMA.NAMESPACE, "playerType");
        polygon = factory.createURI(SCHEMA.NAMESPACE, "polygon");
        population = factory.createURI(SCHEMA.NAMESPACE, "population");
        possibleComplication = factory.createURI(SCHEMA.NAMESPACE, "possibleComplication");
        possibleTreatment = factory.createURI(SCHEMA.NAMESPACE, "possibleTreatment");
        postOfficeBoxNumber = factory.createURI(SCHEMA.NAMESPACE, "postOfficeBoxNumber");
        postOp = factory.createURI(SCHEMA.NAMESPACE, "postOp");
        postalCode = factory.createURI(SCHEMA.NAMESPACE, "postalCode");
        preOp = factory.createURI(SCHEMA.NAMESPACE, "preOp");
        predecessorOf = factory.createURI(SCHEMA.NAMESPACE, "predecessorOf");
        pregnancyCategory = factory.createURI(SCHEMA.NAMESPACE, "pregnancyCategory");
        pregnancyWarning = factory.createURI(SCHEMA.NAMESPACE, "pregnancyWarning");
        prepTime = factory.createURI(SCHEMA.NAMESPACE, "prepTime");
        preparation = factory.createURI(SCHEMA.NAMESPACE, "preparation");
        prescribingInfo = factory.createURI(SCHEMA.NAMESPACE, "prescribingInfo");
        prescriptionStatus = factory.createURI(SCHEMA.NAMESPACE, "prescriptionStatus");
        price = factory.createURI(SCHEMA.NAMESPACE, "price");
        priceCurrency = factory.createURI(SCHEMA.NAMESPACE, "priceCurrency");
        priceRange = factory.createURI(SCHEMA.NAMESPACE, "priceRange");
        priceSpecification = factory.createURI(SCHEMA.NAMESPACE, "priceSpecification");
        priceType = factory.createURI(SCHEMA.NAMESPACE, "priceType");
        priceValidUntil = factory.createURI(SCHEMA.NAMESPACE, "priceValidUntil");
        primaryImageOfPage = factory.createURI(SCHEMA.NAMESPACE, "primaryImageOfPage");
        primaryPrevention = factory.createURI(SCHEMA.NAMESPACE, "primaryPrevention");
        printColumn = factory.createURI(SCHEMA.NAMESPACE, "printColumn");
        printEdition = factory.createURI(SCHEMA.NAMESPACE, "printEdition");
        printPage = factory.createURI(SCHEMA.NAMESPACE, "printPage");
        printSection = factory.createURI(SCHEMA.NAMESPACE, "printSection");
        procedure = factory.createURI(SCHEMA.NAMESPACE, "procedure");
        procedureType = factory.createURI(SCHEMA.NAMESPACE, "procedureType");
        processorRequirements = factory.createURI(SCHEMA.NAMESPACE, "processorRequirements");
        producer = factory.createURI(SCHEMA.NAMESPACE, "producer");
        productID = factory.createURI(SCHEMA.NAMESPACE, "productID");
        productionCompany = factory.createURI(SCHEMA.NAMESPACE, "productionCompany");
        proprietaryName = factory.createURI(SCHEMA.NAMESPACE, "proprietaryName");
        proteinContent = factory.createURI(SCHEMA.NAMESPACE, "proteinContent");
        provider = factory.createURI(SCHEMA.NAMESPACE, "provider");
        publicationType = factory.createURI(SCHEMA.NAMESPACE, "publicationType");
        publisher = factory.createURI(SCHEMA.NAMESPACE, "publisher");
        publishingPrinciples = factory.createURI(SCHEMA.NAMESPACE, "publishingPrinciples");
        purpose = factory.createURI(SCHEMA.NAMESPACE, "purpose");
        qualifications = factory.createURI(SCHEMA.NAMESPACE, "qualifications");
        ratingCount = factory.createURI(SCHEMA.NAMESPACE, "ratingCount");
        ratingValue = factory.createURI(SCHEMA.NAMESPACE, "ratingValue");
        recipeCategory = factory.createURI(SCHEMA.NAMESPACE, "recipeCategory");
        recipeCuisine = factory.createURI(SCHEMA.NAMESPACE, "recipeCuisine");
        recipeInstructions = factory.createURI(SCHEMA.NAMESPACE, "recipeInstructions");
        recipeYield = factory.createURI(SCHEMA.NAMESPACE, "recipeYield");
        recognizingAuthority = factory.createURI(SCHEMA.NAMESPACE, "recognizingAuthority");
        recommendationStrength = factory.createURI(SCHEMA.NAMESPACE, "recommendationStrength");
        recommendedIntake = factory.createURI(SCHEMA.NAMESPACE, "recommendedIntake");
        regionDrained = factory.createURI(SCHEMA.NAMESPACE, "regionDrained");
        regionsAllowed = factory.createURI(SCHEMA.NAMESPACE, "regionsAllowed");
        relatedAnatomy = factory.createURI(SCHEMA.NAMESPACE, "relatedAnatomy");
        relatedCondition = factory.createURI(SCHEMA.NAMESPACE, "relatedCondition");
        relatedDrug = factory.createURI(SCHEMA.NAMESPACE, "relatedDrug");
        relatedLink = factory.createURI(SCHEMA.NAMESPACE, "relatedLink");
        relatedStructure = factory.createURI(SCHEMA.NAMESPACE, "relatedStructure");
        relatedTherapy = factory.createURI(SCHEMA.NAMESPACE, "relatedTherapy");
        relatedTo = factory.createURI(SCHEMA.NAMESPACE, "relatedTo");
        releaseDate = factory.createURI(SCHEMA.NAMESPACE, "releaseDate");
        releaseNotes = factory.createURI(SCHEMA.NAMESPACE, "releaseNotes");
        relevantSpecialty = factory.createURI(SCHEMA.NAMESPACE, "relevantSpecialty");
        repetitions = factory.createURI(SCHEMA.NAMESPACE, "repetitions");
        replyToUrl = factory.createURI(SCHEMA.NAMESPACE, "replyToUrl");
        representativeOfPage = factory.createURI(SCHEMA.NAMESPACE, "representativeOfPage");
        requirements = factory.createURI(SCHEMA.NAMESPACE, "requirements");
        requiresSubscription = factory.createURI(SCHEMA.NAMESPACE, "requiresSubscription");
        responsibilities = factory.createURI(SCHEMA.NAMESPACE, "responsibilities");
        restPeriods = factory.createURI(SCHEMA.NAMESPACE, "restPeriods");
        review = factory.createURI(SCHEMA.NAMESPACE, "review");
        reviewBody = factory.createURI(SCHEMA.NAMESPACE, "reviewBody");
        reviewCount = factory.createURI(SCHEMA.NAMESPACE, "reviewCount");
        reviewRating = factory.createURI(SCHEMA.NAMESPACE, "reviewRating");
        reviewedBy = factory.createURI(SCHEMA.NAMESPACE, "reviewedBy");
        reviews = factory.createURI(SCHEMA.NAMESPACE, "reviews");
        riskFactor = factory.createURI(SCHEMA.NAMESPACE, "riskFactor");
        risks = factory.createURI(SCHEMA.NAMESPACE, "risks");
        runsTo = factory.createURI(SCHEMA.NAMESPACE, "runsTo");
        safetyConsideration = factory.createURI(SCHEMA.NAMESPACE, "safetyConsideration");
        salaryCurrency = factory.createURI(SCHEMA.NAMESPACE, "salaryCurrency");
        saturatedFatContent = factory.createURI(SCHEMA.NAMESPACE, "saturatedFatContent");
        screenshot = factory.createURI(SCHEMA.NAMESPACE, "screenshot");
        season = factory.createURI(SCHEMA.NAMESPACE, "season");
        seasonNumber = factory.createURI(SCHEMA.NAMESPACE, "seasonNumber");
        seasons = factory.createURI(SCHEMA.NAMESPACE, "seasons");
        secondaryPrevention = factory.createURI(SCHEMA.NAMESPACE, "secondaryPrevention");
        seeks = factory.createURI(SCHEMA.NAMESPACE, "seeks");
        seller = factory.createURI(SCHEMA.NAMESPACE, "seller");
        sensoryUnit = factory.createURI(SCHEMA.NAMESPACE, "sensoryUnit");
        serialNumber = factory.createURI(SCHEMA.NAMESPACE, "serialNumber");
        seriousAdverseOutcome = factory.createURI(SCHEMA.NAMESPACE, "seriousAdverseOutcome");
        servesCuisine = factory.createURI(SCHEMA.NAMESPACE, "servesCuisine");
        servingSize = factory.createURI(SCHEMA.NAMESPACE, "servingSize");
        sibling = factory.createURI(SCHEMA.NAMESPACE, "sibling");
        siblings = factory.createURI(SCHEMA.NAMESPACE, "siblings");
        signDetected = factory.createURI(SCHEMA.NAMESPACE, "signDetected");
        signOrSymptom = factory.createURI(SCHEMA.NAMESPACE, "signOrSymptom");
        significance = factory.createURI(SCHEMA.NAMESPACE, "significance");
        significantLink = factory.createURI(SCHEMA.NAMESPACE, "significantLink");
        significantLinks = factory.createURI(SCHEMA.NAMESPACE, "significantLinks");
        skills = factory.createURI(SCHEMA.NAMESPACE, "skills");
        sku = factory.createURI(SCHEMA.NAMESPACE, "sku");
        sodiumContent = factory.createURI(SCHEMA.NAMESPACE, "sodiumContent");
        softwareVersion = factory.createURI(SCHEMA.NAMESPACE, "softwareVersion");
        source = factory.createURI(SCHEMA.NAMESPACE, "source");
        sourceOrganization = factory.createURI(SCHEMA.NAMESPACE, "sourceOrganization");
        sourcedFrom = factory.createURI(SCHEMA.NAMESPACE, "sourcedFrom");
        specialCommitments = factory.createURI(SCHEMA.NAMESPACE, "specialCommitments");
        specialty = factory.createURI(SCHEMA.NAMESPACE, "specialty");
        sponsor = factory.createURI(SCHEMA.NAMESPACE, "sponsor");
        spouse = factory.createURI(SCHEMA.NAMESPACE, "spouse");
        stage = factory.createURI(SCHEMA.NAMESPACE, "stage");
        stageAsNumber = factory.createURI(SCHEMA.NAMESPACE, "stageAsNumber");
        startDate = factory.createURI(SCHEMA.NAMESPACE, "startDate");
        status = factory.createURI(SCHEMA.NAMESPACE, "status");
        storageRequirements = factory.createURI(SCHEMA.NAMESPACE, "storageRequirements");
        streetAddress = factory.createURI(SCHEMA.NAMESPACE, "streetAddress");
        strengthUnit = factory.createURI(SCHEMA.NAMESPACE, "strengthUnit");
        strengthValue = factory.createURI(SCHEMA.NAMESPACE, "strengthValue");
        structuralClass = factory.createURI(SCHEMA.NAMESPACE, "structuralClass");
        study = factory.createURI(SCHEMA.NAMESPACE, "study");
        studyDesign = factory.createURI(SCHEMA.NAMESPACE, "studyDesign");
        studyLocation = factory.createURI(SCHEMA.NAMESPACE, "studyLocation");
        studySubject = factory.createURI(SCHEMA.NAMESPACE, "studySubject");
        subEvent = factory.createURI(SCHEMA.NAMESPACE, "subEvent");
        subEvents = factory.createURI(SCHEMA.NAMESPACE, "subEvents");
        subStageSuffix = factory.createURI(SCHEMA.NAMESPACE, "subStageSuffix");
        subStructure = factory.createURI(SCHEMA.NAMESPACE, "subStructure");
        subTest = factory.createURI(SCHEMA.NAMESPACE, "subTest");
        subtype = factory.createURI(SCHEMA.NAMESPACE, "subtype");
        successorOf = factory.createURI(SCHEMA.NAMESPACE, "successorOf");
        sugarContent = factory.createURI(SCHEMA.NAMESPACE, "sugarContent");
        superEvent = factory.createURI(SCHEMA.NAMESPACE, "superEvent");
        supplyTo = factory.createURI(SCHEMA.NAMESPACE, "supplyTo");
        targetPopulation = factory.createURI(SCHEMA.NAMESPACE, "targetPopulation");
        taxID = factory.createURI(SCHEMA.NAMESPACE, "taxID");
        telephone = factory.createURI(SCHEMA.NAMESPACE, "telephone");
        text = factory.createURI(SCHEMA.NAMESPACE, "text");
        thumbnail = factory.createURI(SCHEMA.NAMESPACE, "thumbnail");
        thumbnailUrl = factory.createURI(SCHEMA.NAMESPACE, "thumbnailUrl");
        tickerSymbol = factory.createURI(SCHEMA.NAMESPACE, "tickerSymbol");
        tissueSample = factory.createURI(SCHEMA.NAMESPACE, "tissueSample");
        title = factory.createURI(SCHEMA.NAMESPACE, "title");
        totalTime = factory.createURI(SCHEMA.NAMESPACE, "totalTime");
        track = factory.createURI(SCHEMA.NAMESPACE, "track");
        tracks = factory.createURI(SCHEMA.NAMESPACE, "tracks");
        trailer = factory.createURI(SCHEMA.NAMESPACE, "trailer");
        transFatContent = factory.createURI(SCHEMA.NAMESPACE, "transFatContent");
        transcript = factory.createURI(SCHEMA.NAMESPACE, "transcript");
        transmissionMethod = factory.createURI(SCHEMA.NAMESPACE, "transmissionMethod");
        trialDesign = factory.createURI(SCHEMA.NAMESPACE, "trialDesign");
        tributary = factory.createURI(SCHEMA.NAMESPACE, "tributary");
        typeOfGood = factory.createURI(SCHEMA.NAMESPACE, "typeOfGood");
        typicalTest = factory.createURI(SCHEMA.NAMESPACE, "typicalTest");
        unitCode = factory.createURI(SCHEMA.NAMESPACE, "unitCode");
        unsaturatedFatContent = factory.createURI(SCHEMA.NAMESPACE, "unsaturatedFatContent");
        uploadDate = factory.createURI(SCHEMA.NAMESPACE, "uploadDate");
        url = factory.createURI(SCHEMA.NAMESPACE, "url");
        usedToDiagnose = factory.createURI(SCHEMA.NAMESPACE, "usedToDiagnose");
        usesDevice = factory.createURI(SCHEMA.NAMESPACE, "usesDevice");
        validFrom = factory.createURI(SCHEMA.NAMESPACE, "validFrom");
        validThrough = factory.createURI(SCHEMA.NAMESPACE, "validThrough");
        value = factory.createURI(SCHEMA.NAMESPACE, "value");
        valueAddedTaxIncluded = factory.createURI(SCHEMA.NAMESPACE, "valueAddedTaxIncluded");
        valueReference = factory.createURI(SCHEMA.NAMESPACE, "valueReference");
        vatID = factory.createURI(SCHEMA.NAMESPACE, "vatID");
        version = factory.createURI(SCHEMA.NAMESPACE, "version");
        video = factory.createURI(SCHEMA.NAMESPACE, "video");
        videoFrameSize = factory.createURI(SCHEMA.NAMESPACE, "videoFrameSize");
        videoQuality = factory.createURI(SCHEMA.NAMESPACE, "videoQuality");
        warning = factory.createURI(SCHEMA.NAMESPACE, "warning");
        warranty = factory.createURI(SCHEMA.NAMESPACE, "warranty");
        warrantyScope = factory.createURI(SCHEMA.NAMESPACE, "warrantyScope");
        weight = factory.createURI(SCHEMA.NAMESPACE, "weight");
        width = factory.createURI(SCHEMA.NAMESPACE, "width");
        wordCount = factory.createURI(SCHEMA.NAMESPACE, "wordCount");
        workHours = factory.createURI(SCHEMA.NAMESPACE, "workHours");
        workLocation = factory.createURI(SCHEMA.NAMESPACE, "workLocation");
        workload = factory.createURI(SCHEMA.NAMESPACE, "workload");
        worksFor = factory.createURI(SCHEMA.NAMESPACE, "worksFor");
        worstRating = factory.createURI(SCHEMA.NAMESPACE, "worstRating");
    }
}
