/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.commons.sesame.rio.ical;

import com.google.common.base.Preconditions;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.*;
import net.fortuna.ical4j.model.property.*;

import org.apache.marmotta.commons.sesame.rio.ical.ICalFormat;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A parser for parsing iCalendar (http://de.wikipedia.org/wiki/ICalendar) files into RDF format. Represents
 * data using the iCal vocabulary (http://www.w3.org/2002/12/cal/icaltzd,
 * http://www.w3.org/wiki/RdfCalendarDocumentation)
 * <p/>
 * @author Sebastian Schaffert
 * @see http://wiki.modularity.net.au/ical4j/index.php
 * @see http://wiki.modularity.net.au/ical4j/index.php?title=Compatibility
 */
public class ICalParser extends RDFParserBase {
    private static Logger log = LoggerFactory.getLogger(ICalParser.class);

    private final static SimpleDateFormat DF_DIGITS = new SimpleDateFormat("yyyyMMdd-hhmmss");

    public final static String NS_ICAL = "http://www.w3.org/2002/12/cal/icaltzd#";
    public static final String NS_RDF    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String NS_GEO                      = "http://www.w3.org/2003/01/geo/wgs84_pos#";


    private ValueFactory valueFactory;

    /**
     * Creates a new RDFParserBase that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create RDF model objects.
     */
    public ICalParser() {
        this(new ValueFactoryImpl());
    }

    /**
     * Creates a new RDFParserBase that will use the supplied ValueFactory to
     * create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public ICalParser(ValueFactory valueFactory) {
        super(valueFactory);
        this.valueFactory = valueFactory;
    }


    @Override
    public void setValueFactory(ValueFactory valueFactory) {
        super.setValueFactory(valueFactory);
        this.valueFactory = valueFactory;
    }

    /**
     * Gets the RDF format that this parser can parse.
     */
    @Override
    public RDFFormat getRDFFormat() {
        return ICalFormat.FORMAT;
    }

    /**
     * Parses the data from the supplied InputStream, using the supplied baseURI
     * to resolve any relative URI references.
     *
     * @param in      The InputStream from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                             If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                             If the configured statement handler has encountered an
     *                             unrecoverable error.
     */
    @Override
    public void parse(InputStream in, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        Preconditions.checkNotNull(baseURI);

        setBaseURI(baseURI);
        try {
            parseCalendar(new CalendarBuilder().build(in));
        } catch (ParserException e) {
            throw new RDFParseException(e);
        }
    }

    /**
     * Parses the data from the supplied Reader, using the supplied baseURI to
     * resolve any relative URI references.
     *
     * @param reader  The Reader from which to read the data.
     * @param baseURI The URI associated with the data in the InputStream.
     * @throws java.io.IOException If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                             If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                             If the configured statement handler has encountered an
     *                             unrecoverable error.
     */
    @Override
    public void parse(Reader reader, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        Preconditions.checkNotNull(baseURI);

        setBaseURI(baseURI);
        try {
            parseCalendar(new CalendarBuilder().build(reader));
        } catch (ParserException e) {
            throw new RDFParseException(e);
        }
    }


    private void parseCalendar(Calendar calendar) throws RDFParseException, RDFHandlerException, UnsupportedEncodingException {
        for(Object component : calendar.getComponents()) {
            if(component instanceof VEvent) {
                parseEvent((VEvent) component);
            } else if(component instanceof VJournal) {
                parseJournal((VJournal) component);
            } else if(component instanceof VAlarm) {
                parseAlarm((VAlarm) component);
            } else if(component instanceof VFreeBusy) {
                parseFreeBusy((VFreeBusy) component);
            } else if(component instanceof VToDo) {
                parseToDo((VToDo) component);
            }
        }
    }

    private void parseEvent(VEvent event) throws RDFParseException, RDFHandlerException, UnsupportedEncodingException {
        log.debug("parsing event: {}", event.getUid());
        Resource r_event;

        String uriBase = "";
        if(event.getUid() != null) {
            uriBase = event.getUid().getValue();
        } else {
            uriBase = UUID.randomUUID().toString();
        }
        if(event.getRecurrenceId() != null) {
            uriBase += "-" + DF_DIGITS.format(event.getRecurrenceId().getDate());
        }
        if(event.getSequence() != null) {
            uriBase += "-"+event.getSequence().getSequenceNo();
        }
        r_event = resolveURI(uriBase);


        Resource t_vevent = createURI(NS_ICAL + "Vevent");
        URI p_type        = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_event,p_type,t_vevent));

        parseCalendarComponent(event,r_event);
    }

    private void parseJournal(VJournal journal) throws RDFParseException, RDFHandlerException, UnsupportedEncodingException {
        log.debug("parsing journal: {}", journal.getUid());
        Resource r_event;
        String uriBase = "";
        if(journal.getUid() != null) {
            uriBase = journal.getUid().getValue();
        } else {
            uriBase = UUID.randomUUID().toString();
        }
        if(journal.getRecurrenceId() != null) {
            uriBase += "-" + DF_DIGITS.format(journal.getRecurrenceId().getDate());
        }
        if(journal.getSequence() != null) {
            uriBase += "-"+journal.getSequence().getSequenceNo();
        }
        r_event = resolveURI(uriBase);

        Resource t_vevent = createURI(NS_ICAL + "Vjournal");
        URI p_type        = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_event,p_type,t_vevent));

        parseCalendarComponent(journal,r_event);
    }

    private void parseAlarm(VAlarm alarm) throws RDFParseException, RDFHandlerException, UnsupportedEncodingException {
        log.debug("parsing alarm");
        Resource r_event = resolveURI(UUID.randomUUID().toString());


        Resource t_vevent = createURI(NS_ICAL + "Valarm");
        URI p_type        = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_event,p_type,t_vevent));

        parseCalendarComponent(alarm,r_event);
    }

    private void parseFreeBusy(VFreeBusy freeBusy) throws RDFParseException, RDFHandlerException, UnsupportedEncodingException {
        log.debug("parsing free/busy: {}", freeBusy.getUid());
        Resource r_event;
        if(freeBusy.getUid() != null) {
            r_event = resolveURI(freeBusy.getUid().getValue());
        } else {
            r_event = resolveURI(UUID.randomUUID().toString());
        }

        Resource t_vevent = createURI(NS_ICAL + "Vfreebusy");
        URI p_type        = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_event,p_type,t_vevent));

        parseCalendarComponent(freeBusy,r_event);
    }

    private void parseToDo(VToDo toDo) throws RDFParseException, RDFHandlerException, UnsupportedEncodingException {
        log.debug("parsing todo: {}", toDo.getUid());
        Resource r_event;
        if(toDo.getUid() != null) {
            r_event = resolveURI(toDo.getUid().getValue());
        } else {
            r_event = resolveURI(UUID.randomUUID().toString());
        }

        Resource t_vevent = createURI(NS_ICAL + "Vtodo");
        URI p_type        = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_event,p_type,t_vevent));

        parseCalendarComponent(toDo,r_event);
    }

    @SuppressWarnings("unchecked")
    private void parseCalendarComponent(CalendarComponent component, Resource resource) throws RDFHandlerException, RDFParseException, UnsupportedEncodingException {
        if(component.getProperty(Property.ATTACH) != null) {
            Attach attach = (Attach)component.getProperty(Property.ATTACH);
            if(attach.getUri() != null) {
                URI r_attach = createURI(URLDecoder.decode(attach.getUri().toString(),"UTF-8"));
                URI p_attach = createURI(NS_ICAL + "attach");
                rdfHandler.handleStatement(createStatement(resource,p_attach,r_attach));
            } else {
                log.warn("calendar entry: binary attachments not supported!");
            }
        }


        if(component.getProperty(Property.CATEGORIES) != null) {
            Categories categories = (Categories)component.getProperty(Property.CATEGORIES);
            URI p_categories = createURI(NS_ICAL + "categories");
            for(Iterator<String> it = categories.getCategories().iterator(); it.hasNext(); ) {
                String value = it.next();
                Literal v_categories = createLiteral(value,null,null);
                rdfHandler.handleStatement(createStatement(resource,p_categories,v_categories));
            }
        }


        createStringProperty(component,resource,Property.CLASS, NS_ICAL + "class");
        createStringProperty(component,resource,Property.COMMENT, NS_ICAL + "comment");
        createStringProperty(component,resource,Property.DESCRIPTION, NS_ICAL + "description");

        if(component.getProperty(Property.GEO) != null) {
            Geo geo = (Geo)component.getProperty(Property.GEO);
            URI p_geo = createURI(NS_ICAL + "geo");
            createLocation(resource, p_geo, geo);
        }

        createStringProperty(component,resource,Property.LOCATION, NS_ICAL + "location");
        createIntProperty(component,resource,Property.PERCENT_COMPLETE, NS_ICAL + "percentComplete");
        createIntProperty(component,resource,Property.PRIORITY, NS_ICAL + "priority");

        if(component.getProperty(Property.RESOURCES) != null) {
            Resources resources = (Resources)component.getProperty(Property.RESOURCES);
            URI p_resources = createURI(NS_ICAL + "resources");
            for(Iterator<String> it = resources.getResources().iterator(); it.hasNext(); ) {
                String value = it.next();
                Literal v_resources = createLiteral(value,null,null);
                rdfHandler.handleStatement(createStatement(resource,p_resources,v_resources));
            }
        }

        createStringProperty(component,resource,Property.STATUS, NS_ICAL + "status");
        createStringProperty(component,resource,Property.SUMMARY, NS_ICAL + "summary");

        createDateProperty((DateProperty)component.getProperty(Property.COMPLETED), resource, NS_ICAL + "completed");
        createDateProperty((DateProperty)component.getProperty(Property.DUE), resource, NS_ICAL + "due");

        createDateProperty((DateProperty)component.getProperty(Property.DTEND), resource, NS_ICAL + "dtend");
        createDateProperty((DateProperty)component.getProperty(Property.DTSTART), resource, NS_ICAL + "dtstart");


        if(component.getProperty(Property.DURATION) != null) {
            Duration duration = (Duration)component.getProperty(Property.DURATION);
            URI p_duration = createURI(NS_ICAL + "duration");
            try {
                javax.xml.datatype.Duration dur = DatatypeFactory.newInstance().newDuration(duration.getDuration().getTime(new Date(0)).getTime());
                Literal v_duration = createLiteral(dur.toString(),null, createURI(dur.getXMLSchemaType().toString()));
                rdfHandler.handleStatement(createStatement(resource,p_duration,v_duration));
            } catch (DatatypeConfigurationException e) {
                log.warn("calendar entry: error while parsing duration");
            }

        }

        createStringProperty(component,resource,Property.TRANSP, NS_ICAL + "transp");

        URI p_attendee = createURI(NS_ICAL + "attendee");
        for(Iterator<Property> it = component.getProperties(Property.ATTENDEE).iterator(); it.hasNext(); ) {
            Attendee attendee = (Attendee) it.next();
            if(attendee.getCalAddress() != null) {
                URI v_attendee = createURI(attendee.getCalAddress().toString());
                rdfHandler.handleStatement(createStatement(resource,p_attendee,v_attendee));
            } else {
                log.warn("attendee without calendar address: {}",attendee);
            }
        }

        createStringProperty(component,resource,Property.CONTACT, NS_ICAL + "contact");

        if(component.getProperty(Property.ORGANIZER) != null) {
            Organizer organizer = (Organizer) component.getProperty(Property.ORGANIZER);
            URI p_organizer = createURI(NS_ICAL + "organizer");
            if(organizer.getCalAddress() != null) {
                URI v_organizer = createURI(organizer.getCalAddress().toString());
                rdfHandler.handleStatement(createStatement(resource,p_organizer,v_organizer));
            }
        }

        createStringProperty(component,resource,Property.RELATED_TO, NS_ICAL + "relatedTo");
        createUrlProperty(component,resource,Property.URL, NS_ICAL + "url");
        createStringProperty(component,resource,Property.UID, NS_ICAL + "uid");

        for(Iterator<Property> it = component.getProperties(Property.EXDATE).iterator(); it.hasNext(); ) {
            createDateProperty(it.next(),resource, NS_ICAL + "exdate");
        }
        for(Iterator<Property> it = component.getProperties(Property.EXRULE).iterator(); it.hasNext(); ) {
            createStringProperty(it.next(),resource, NS_ICAL + "exrule");
        }
        for(Iterator<Property> it = component.getProperties(Property.RDATE).iterator(); it.hasNext(); ) {
            createDateProperty((DateProperty)it.next(),resource, NS_ICAL + "rdate");
        }
        for(Iterator<Property> it = component.getProperties(Property.RRULE).iterator(); it.hasNext(); ) {
            createStringProperty(it.next(),resource, NS_ICAL + "rrule");
        }

        if(component.getProperty(Property.TRIGGER) != null) {
            Trigger duration = (Trigger)component.getProperty(Property.TRIGGER);
            URI p_duration = createURI(NS_ICAL + "trigger");
            try {
                javax.xml.datatype.Duration dur = DatatypeFactory.newInstance().newDuration(duration.getDuration().getTime(new Date(0)).getTime());
                Literal v_duration = createLiteral(dur.toString(),null, createURI(dur.getXMLSchemaType().toString()));
                rdfHandler.handleStatement(createStatement(resource,p_duration,v_duration));
            } catch (DatatypeConfigurationException e) {
                log.warn("calendar entry: error while parsing duration");
            }

        }

        createDateProperty((DateProperty) component.getProperty(Property.CREATED), resource, NS_ICAL + "created");
        createDateProperty((DateProperty) component.getProperty(Property.DTSTAMP), resource, NS_ICAL + "dtstamp");
        createDateProperty((DateProperty) component.getProperty(Property.LAST_MODIFIED), resource, NS_ICAL + "lastModified");

        createDateProperty((DateProperty) component.getProperty(Property.RECURRENCE_ID), resource, NS_ICAL + "recurrenceId");
        createIntProperty(component,resource,Property.SEQUENCE, NS_ICAL + "sequence");

        createStringProperty(component,resource,Property.REQUEST_STATUS,NS_ICAL+"requestStatus");

        createStringProperty(component,resource,Property.ACTION,NS_ICAL+"action");
        createStringProperty(component,resource,Property.REPEAT,NS_ICAL+"repeat");

    }


    private void createLocation(Resource uri, URI prop, Geo geo) throws RDFHandlerException, RDFParseException {
        Resource r_location = createBNode();
        Resource t_adr = createURI(NS_GEO + "Point");
        URI p_type     = createURI(NS_RDF + "type");
        rdfHandler.handleStatement(createStatement(r_location,p_type,t_adr));

        URI p_latitute = createURI(NS_GEO+"latitude");
        URI p_longitude = createURI(NS_GEO+"longitude");
        URI t_decimal   = createURI("http://www.w3.org/2001/XMLSchema#double");

        if(geo.getLatitude() != null) {
            Literal v_latitude = createLiteral(geo.getLatitude().toPlainString(),null,t_decimal);
            rdfHandler.handleStatement(createStatement(r_location,p_latitute,v_latitude));
        }

        if(geo.getLongitude() != null) {
            Literal v_longitude = createLiteral(geo.getLongitude().toPlainString(), null, t_decimal);
            rdfHandler.handleStatement(createStatement(r_location,p_longitude,v_longitude));
        }


        rdfHandler.handleStatement(createStatement(uri,prop,r_location));
    }

    private void createStringProperty(CalendarComponent event, Resource r_event, String icalProperty, String rdfProperty) throws RDFParseException, RDFHandlerException {
        if(event.getProperty(icalProperty) != null) {
            Property description = event.getProperty(icalProperty);
            URI p_description = createURI(rdfProperty);
            Literal v_description = createLiteral(description.getValue(), null, null);
            rdfHandler.handleStatement(createStatement(r_event,p_description,v_description));
        }
    }

    private void createStringProperty(Property property, Resource r_event, String rdfProperty) throws RDFParseException, RDFHandlerException {
        if(property != null) {
            URI p_description = createURI(rdfProperty);
            Literal v_description = createLiteral(property.getValue(), null, null);
            rdfHandler.handleStatement(createStatement(r_event,p_description,v_description));
        }
    }

    private void createIntProperty(CalendarComponent event, Resource r_event, String icalProperty, String rdfProperty) throws RDFParseException, RDFHandlerException {
        if(event.getProperty(icalProperty) != null) {
            Property description = event.getProperty(icalProperty);
            URI p_description = createURI(rdfProperty);
            Literal v_description = createLiteral(description.getValue(), null, createURI("http://www.w3.org/2001/XMLSchema#int"));
            rdfHandler.handleStatement(createStatement(r_event,p_description,v_description));
        }
    }

    private void createDateProperty(Property property, Resource r_event, String rdfProperty) throws RDFParseException, RDFHandlerException {
        if(property != null) {
            if(property instanceof DateProperty) {
                DateProperty dateProperty = (DateProperty)property;
                URI p_dateprop = createURI(rdfProperty);
                Literal v_dateprop = valueFactory.createLiteral(getXMLCalendar(dateProperty.getDate(),dateProperty.getTimeZone()));
                rdfHandler.handleStatement(createStatement(r_event,p_dateprop,v_dateprop));
            } else if(property instanceof DateListProperty) {
                DateListProperty dateProperty = (DateListProperty)property;
                URI p_dateprop = createURI(rdfProperty);
                for(@SuppressWarnings("unchecked") Iterator<Date> it = dateProperty.getDates().iterator(); it.hasNext(); ) {
                    Literal v_dateprop = valueFactory.createLiteral(getXMLCalendar(it.next(),dateProperty.getTimeZone()));
                    rdfHandler.handleStatement(createStatement(r_event,p_dateprop,v_dateprop));
                }
            }
        }
    }


    private void createUrlProperty(CalendarComponent event, Resource r_event, String icalProperty, String rdfProperty) throws RDFParseException, RDFHandlerException {
        if(event.getProperty(icalProperty) != null) {
            Property description = event.getProperty(icalProperty);
            URI p_description = createURI(rdfProperty);
            URI v_description = createURI(description.getValue());
            rdfHandler.handleStatement(createStatement(r_event,p_description,v_description));
        }
    }


    public static Date getDate(XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar().getTime();
    }


    public static XMLGregorianCalendar getXMLCalendar(Date date, TimeZone timezone) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        if(timezone != null)
            c.setTimeZone(timezone);
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }


}
