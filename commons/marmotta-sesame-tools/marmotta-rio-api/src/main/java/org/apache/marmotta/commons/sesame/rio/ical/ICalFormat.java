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
package org.apache.marmotta.commons.sesame.rio.ical;

import org.openrdf.rio.RDFFormat;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * ICal Format is using ical4j for parsing.
 * <p/>
 * If you encounter problems parsing iCal files, e.g. a {@link net.fortuna.ical4j.data.ParserException}, 
 * try enabling the relaxed/compatibility mode:
 * <ul>
 * <li>use {@link net.fortuna.ical4j.util.CompatibilityHints#setHintEnabled(String, boolean)}, or
 * <li>create a file called {@code ical4j.properties} in the root of your classpath, or
 * <li>specify a compatibility hint as {@code system property}
 * </ul>
 * 
 * @author Sebastian Schaffert
 * @see <a href="http://wiki.modularity.net.au/ical4j/index.php?title=Compatibility">http://wiki.modularity.net.au/ical4j/index.php?title=Compatibility</a>
 */
public class ICalFormat {

    /**
     * ICal Format is using ical4j for parsing.
     * <p/>
     * If you encounter problems parsing iCal files, e.g. a {@link net.fortuna.ical4j.data.ParserException}, 
     * try enabling the relaxed/compatibility mode:
     * <ul>
     * <li>use {@link net.fortuna.ical4j.util.CompatibilityHints#setHintEnabled(String, boolean)}, or
     * <li>create a file called {@code ical4j.properties} in the root of your classpath, or
     * <li>specify a compatibility hint as {@code system property}
     * </ul>
     * <p/>
     * Complete list of <strong>hints</strong> (valid settings: {@code true}/{@code false}):
     * <ul>
     * <li>{@code ical4j.unfolding.relaxed}
     * <li>{@code ical4j.parsing.relaxed}
     * <li>{@code ical4j.validation.relaxed}
     * <li>{@code ical4j.compatibility.outlook}
     * <li>{@code ical4j.compatibility.notes}
     * </ul>
     * 
     * @see <a href="http://wiki.modularity.net.au/ical4j/index.php?title=Compatibility">http://wiki.modularity.net.au/ical4j/index.php?title=Compatibility</a>
     */
    public static final RDFFormat FORMAT = new RDFFormat(
            "ICal",
            Arrays.asList("text/calendar"),
            Charset.forName("UTF-8"),
            Arrays.asList("ics", "ifb", "iCal", "iFBf"),
            false,
            false
    );

}
