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
package org.apache.marmotta.platform.ldp.api;

import org.apache.marmotta.commons.vocabulary.LDP;

/**
 * Preferences, which triples to include in a response.
 *
 * @see <a href="http://www.w3.org/TR/ldp/#prefer-parameters">http://www.w3.org/TR/ldp/#prefer-parameters</a>
 *
 * @author Jakob Frank
 */
public class Preference {

    private boolean content;
    private boolean minimal;
    private boolean membership;
    private boolean containment;
    private boolean minimalContainer;

    private Preference(boolean minimal, boolean elements) {
        this.content = !minimal;
        this.minimal = minimal;
        this.membership = elements;
        this.containment = elements;
        this.minimalContainer = elements;
    }

    /**
     * Reflects a {@code Prefer: return="minimal"} header
     * @return {@code true} if the minimal representation is preferred by the client
     */
    public boolean isMinimal() {
        return minimal;
    }

    /**
     * Should the LDP-RS content be included in the response?
     * @return {@code true} if the content triples should be included in the response
     */
    public boolean includeContent() {
        return content;
    }

    /**
     * Should membership triples be included in the response?
     *
     * @return {@code true} if membership triples should be included in the response
     * @see <a href="http://www.w3.org/TR/ldp/#dfn-membership-triples">http://www.w3.org/TR/ldp/#dfn-membership-triples</a>
     */
    public boolean includeMembership() {
        return membership;
    }

    /**
     * Should containment triples be included in the response?
     *
     * @return {@code true} if containment triples should be included in the response
     * @see <a href="http://www.w3.org/TR/ldp/#dfn-containment-triples">http://www.w3.org/TR/ldp/#dfn-containment-triples</a>
     */
    public boolean includeContainment() {
        return containment;
    }

    /**
     * Should minimal container triples be included in the response?
     *
     * @return {@code true} if minimal container triples should be included in the response
     * @see <a href="http://www.w3.org/TR/ldp/#dfn-minimal-container-triples">http://www.w3.org/TR/ldp/#dfn-minimal-container-triples</a>
     */
    public boolean includeMinimalContainer() {
        return minimalContainer;
    }

    /**
     * If minimal is set to {@code true}, all other settings will be set to false.
     * @param minimal whether minimal representation is preferred.
     */
    public void setMinimal(boolean minimal) {
        this.minimal = minimal;
        if (minimal) {
            this.content = this.membership = this.containment = this.minimalContainer = false;
        }
    }

    /**
     * Setting content to {@code true} will also set minimal to {@code false}
     * @param content should the LDP-RS content be included in the response?
     */
    public void setContent(boolean content) {
        this.content = content;
        if (content) {
            this.minimal = false;
        }
    }

    /**
     * Setting membership to {@code true} will also set minimal to {@code false}
     *
     * @param membership should membership triples be included in the response?
     * @see <a href="http://www.w3.org/TR/ldp/#dfn-membership-triples">http://www.w3.org/TR/ldp/#dfn-membership-triples</a>
     */
    public void setMembership(boolean membership) {
        this.membership = membership;
        if (membership) {
            this.minimal = false;
        }
    }

    /**
     * Setting containment to {@code true} will also set minimal to {@code false}
     *
     * @param containment should containment triples be included in the response?
     * @see <a href="http://www.w3.org/TR/ldp/#dfn-containment-triples">http://www.w3.org/TR/ldp/#dfn-containment-triples</a>
     */
    public void setContainment(boolean containment) {
        this.containment = containment;
        if (containment) {
            this.minimal = false;
        }
    }

    /**
     * Setting minimal container to {@code true} will also set minimal to {@code false}
     *
     * @param minimalContainer should minimal container triples be included in the response?
     * @see <a href="http://www.w3.org/TR/ldp/#dfn-minimal-container-triples">http://www.w3.org/TR/ldp/#dfn-minimal-container-triples</a>
     */
    public void setMinimalContainer(boolean minimalContainer) {
        this.minimalContainer = minimalContainer;
        if (minimalContainer) {
            this.minimal = false;
        }
    }

    /**
     * The default preference: not minimal, all included.
     * @return the default preference.
     */
    public static Preference defaultPreference() {
        return new Preference(false, true);
    }

    /**
     * The minimal preference: minimal is {@code true}, nothing included.
     * @return the minimal preference.
     */
    public static Preference minimalPreference() {
        return new Preference(true, false);
    }

    /**
     * Non-minimal preference, with only the args included.
     *
     * @param includes LDP-Preference URIs to include in the response
     * @return a non-minimal preference, with only the provided parts included.
     * @see <a href="http://www.w3.org/TR/ldp/#h5_prefer-uris">http://www.w3.org/TR/ldp/#h5_prefer-uris</a>
     */
    @SuppressWarnings("deprecation")
    public static Preference includePreference(String... includes) {
        final Preference pref = new Preference(false, false);
        pref.content = false;
        for (String i: includes) {
            if (LDP.PreferContainment.stringValue().equals(i)) {
                pref.setContainment(true);
            } else if (LDP.PreferMembership.stringValue().equals(i)) {
                pref.setMembership(true);
            } else if (LDP.PreferMinimalContainer.stringValue().equals(i)) {
                pref.setMinimalContainer(true);
            } else if (LDP.PreferEmptyContainer.stringValue().equals(i)) {
                pref.setMinimalContainer(true);
            } else {
                // ignore unknown includes
            }
        }
        return pref;
    }

    /**
     * Non-minimal preference, with all the provided args omitted.
     *
     * @param omits LDP-Preference URIs to omit in the response
     * @return a non-minimal preference, with all provided parts excluded.
     * @see <a href="http://www.w3.org/TR/ldp/#h5_prefer-uris">http://www.w3.org/TR/ldp/#h5_prefer-uris</a>
     */
    @SuppressWarnings("deprecation")
    public static Preference omitPreference(String... omits) {
        final Preference pref = new Preference(false, true);
        pref.content = true;
        for (String e: omits) {
            if (LDP.PreferContainment.stringValue().equals(e)) {
                pref.setContainment(false);
            } else if (LDP.PreferMembership.stringValue().equals(e)) {
                pref.setMembership(false);
            } else if (LDP.PreferMinimalContainer.stringValue().equals(e)) {
                pref.setMinimalContainer(false);
            } else if (LDP.PreferEmptyContainer.stringValue().equals(e)) {
                pref.setMinimalContainer(false);
            } else {
                // ignore unknown omits
            }
        }
        return pref;
    }

}
