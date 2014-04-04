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
 * Namespace GEO
 */
public class GEO {

    public static final String NAMESPACE = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    public static final String PREFIX = "geo";

    /**
     * A point, typically described using a coordinate system relative to Earth, such as WGS84.
     */
    public static final URI Point;

    /**
     * Anything with spatial extent, i.e. size, shape, or position.
     e.g. people, places, bowling balls, as well as abstract areas like cubes.
     */
    public static final URI SpatialThing;

    /**
     * The WGS84 altitude of a SpatialThing (decimal meters
     above the local reference ellipsoid).
     */
    public static final URI alt;

    /**
     * The WGS84 latitude of a SpatialThing (decimal degrees).
     */
    public static final URI lat;

    /**
     * A comma-separated representation of a latitude, longitude coordinate.
     */
    public static final URI lat_long;

    /**
     * The relation between something and the point,
     or other geometrical thing in space, where it is.  For example, the realtionship between
     a radio tower and a Point with a given lat and long.
     Or a relationship between a park and its outline as a closed arc of points, or a road and
     its location as a arc (a sequence of points).
     Clearly in practice there will be limit to the accuracy of any such statement, but one would expect
     an accuracy appropriate for the size of the object and uses such as mapping .
     */
    public static final URI location;

    /**
     * The WGS84 longitude of a SpatialThing (decimal degrees).
     */
    public static final URI long_;


    static{
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Point = factory.createURI(GEO.NAMESPACE, "Point");
        SpatialThing = factory.createURI(GEO.NAMESPACE, "SpatialThing");
        alt = factory.createURI(GEO.NAMESPACE, "alt");
        lat = factory.createURI(GEO.NAMESPACE, "lat");
        lat_long = factory.createURI(GEO.NAMESPACE, "lat_long");
        location = factory.createURI(GEO.NAMESPACE, "location");
        long_ = factory.createURI(GEO.NAMESPACE, "long");
    }
}
