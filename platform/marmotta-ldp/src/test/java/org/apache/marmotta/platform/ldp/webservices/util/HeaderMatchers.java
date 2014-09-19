/*
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
package org.apache.marmotta.platform.ldp.webservices.util;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jboss.resteasy.plugins.delegates.EntityTagDelegate;
import org.jboss.resteasy.plugins.delegates.LinkDelegate;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;

/**
 * Matcher collection to work with HttpHeaders.
 */
public class HeaderMatchers {

    public static Matcher<String> headerPresent() {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                return item != null && StringUtils.isNotBlank(item.toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("set");
            }
        };
    }

    public static Matcher<String> headerNotPresent() {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                return item == null || StringUtils.isBlank(item.toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("absent or empty");
            }
        };
    }

    public static Matcher<String> isLink(String uri, String rel) {
        final Link expected = Link.fromUri(uri).rel(rel).build();
        return new CustomTypeSafeMatcher<String>(String.format("a Link-Header to <%s> with rel='%s'", uri, rel)) {
            @Override
            protected boolean matchesSafely(String item) {
                return expected.equals(new LinkDelegate().fromString(item));
            }
        };
    }

    public static Matcher<String> hasEntityTag(final boolean weakTag) {
        return new CustomTypeSafeMatcher<String>(String.format("a %s EntityTag", weakTag?"weak":"strong")) {
            @Override
            protected boolean matchesSafely(String item) {
                return (new EntityTagDelegate().fromString(item).isWeak() == weakTag);
            }
        };
    }

    public static Matcher<String> hasEntityTag(String value, boolean weakTag) {
        return hasEntityTag(new EntityTag(value, weakTag));
    }
      
    public static Matcher<String> hasEntityTag(String value) {
        return hasEntityTag(value, false);
    }

    public static Matcher<String> hasEntityTag(final EntityTag expected) {
        return new CustomTypeSafeMatcher<String>(String.format("an EntityTag %s", expected)) {
            @Override
            protected boolean matchesSafely(String item) {
                return expected.equals(new EntityTagDelegate().fromString(item));
            }
        };
    }
}
