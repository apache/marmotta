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
 * Namespace GEO
 */
public class GEO {

    public static final String NAMESPACE = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    public static final String PREFIX = "geo";

    /**
     * A point, typically described using a coordinate system relative to Earth, such as WGS84.
     */
    public static final IRI Point;

    /**
     * Anything with spatial extent, i.e. size, shape, or position.
     e.g. people, places, bowling balls, as well as abstract areas like cubes.
     */
    public static final IRI SpatialThing;

    /**
     * The WGS84 altitude of a SpatialThing (decimal meters
     above the local reference ellipsoid).
     */
    public static final IRI alt;

    /**
     * The WGS84 latitude of a SpatialThing (decimal degrees).
     */
    public static final IRI lat;

    /**
     * A comma-separated representation of a latitude, longitude coordinate.
     */
    public static final IRI lat_long;

    /**
     * The relation between something and the point,
     or other geometrical thing in space, where it is.  For example, the realtionship between
     a radio tower and a Point with a given lat and long.
     Or a relationship between a park and its outline as a closed arc of points, or a road and
     its location as a arc (a sequence of points).
     Clearly in practice there will be limit to the accuracy of any such statement, but one would expect
     an accuracy appropriate for the size of the object and uses such as mapping .
     */
    public static final IRI location;

    /**
     * The WGS84 longitude of a SpatialThing (decimal degrees).
     */
    public static final IRI long_;


    static{
        ValueFactory factory = SimpleValueFactory.getInstance();
        Point = factory.createIRI(GEO.NAMESPACE, "Point");
        SpatialThing = factory.createIRI(GEO.NAMESPACE, "SpatialThing");
        alt = factory.createIRI(GEO.NAMESPACE, "alt");
        lat = factory.createIRI(GEO.NAMESPACE, "lat");
        lat_long = factory.createIRI(GEO.NAMESPACE, "lat_long");
        location = factory.createIRI(GEO.NAMESPACE, "location");
        long_ = factory.createIRI(GEO.NAMESPACE, "long");
    }
}
