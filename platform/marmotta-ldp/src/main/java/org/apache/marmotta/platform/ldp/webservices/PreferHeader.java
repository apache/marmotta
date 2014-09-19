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
package org.apache.marmotta.platform.ldp.webservices;

import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTML Prefer Header.
 *
 * @author Jakob Frank
 * @see <a href="http://www.ietf.org/rfc/rfc7240.txt">http://www.ietf.org/rfc/rfc7240.txt</a>
 */
public class PreferHeader {

    public static final String PREFERENCE_RESPOND_ASYNC ="respond-async";
    public static final String PREFERENCE_RETURN = "return";
    public static final String RETURN_REPRESENTATION = "representation";
    public static final String RETURN_MINIMAL = "minimal";
    public static final String PREFERENCE_WAIT = "wait";
    public static final String PREFERENCE_HANDLING = "handling";
    public static final String HANDLING_STRICT = "strict";
    public static final String HANDLING_LENIENT = "lenient";

    public static final String RETURN_PARAM_INCLUDE = "include";
    public static final String RETURN_PARAM_OMIT = "omit";

    public static Logger log = LoggerFactory.getLogger(PreferHeader.class);

    private String preference, preferenceValue;

    private Map<String, String> params;

    private PreferHeader(String preference) {
        this.preference = preference;
        this.params = new LinkedHashMap<>();
    }

    /**
     * Get the preference,
     * e.g. {@code foo} from {@code Prefer: foo="bar"}
     *
     * @return the preference of the prefer-header
     */
    public String getPreference() {
        return preference;
    }

    /**
     * Get the value of the preference,
     * e.g. {@code bar} from {@code Prefer: foo="bar"}.
     * @return the preference value of the prefer-header
     */
    public String getPreferenceValue() {
        return preferenceValue;
    }

    /**
     * Get the parameters of the prefer-header.
     * @return the prefer-parameters
     */
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * Get the parameter value of the prefer-header,
     * e,g, {@code val2} from {@code Prefer: foo="bar"; a1="val1"; a2="val2"} for {@code header.getParamValue("a2")}.
     * @param param the param to get the value of
     * @return the value of the requested parameter, or {@code null}
     */
    public String getParamValue(String param) {
        return params.get(param);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(preference);
        if (StringUtils.isNotBlank(preferenceValue)) {
            sb.append("=\"").append(preferenceValue).append("\"");
        }
        for (String param: params.keySet()) {
            sb.append("; ").append(param);
            final String value = params.get(param);
            if (StringUtils.isNotBlank(value)) {
                sb.append("=\"").append(value).append("\"");
            }
        }

        return sb.toString();
    }

    /**
     * Parse a PreferHeader.
     * @param headerValue the header value to parse
     * @return the parsed PreferHeader
     */
    public static PreferHeader valueOf(String headerValue) {
        if (StringUtils.isBlank(headerValue)) {
            log.error("Empty Prefer-Header - what should I do now?");
            throw new InvalidArgumentException();
        }

        String pref = null, val = null;
        final Map<String, String> params = new LinkedHashMap<>();
        final String[] parts = headerValue.split("\\s*;\\s");
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            final String[] kv = part.split("\\s*=\\s*", 2);
            if (i == 0) {
                pref = StringUtils.trimToNull(kv[0]);
                if (kv.length > 1) {
                    val = StringUtils.trimToNull(StringUtils.removeStart(StringUtils.removeEnd(kv[1], "\""), "\""));
                }
            } else {
                String p, pval = null;
                p = StringUtils.trimToNull(kv[0]);
                if (kv.length > 1) {
                    pval = StringUtils.trimToNull(StringUtils.removeStart(StringUtils.removeEnd(kv[1], "\""), "\""));
                }
                params.put(p, pval);
            }
        }

        final PreferHeader header = new PreferHeader(pref);
        header.preferenceValue = val;
        header.params = params;

        return header;
    }

    /**
     * Create and initialize a PreferBuilder for a {@code Prefer: respond-async} header.
     * @return initialized PreferBuilder
     */
    public static PreferBuilder preferRespondAsync() {
        return new PreferBuilder(PREFERENCE_RESPOND_ASYNC);
    }

    /**
     * Create and initialize a PreferBuilder for a {@code Prefer: return="representation"} header.
     * @return initialized PreferBuilder
     */
    public static PreferBuilder preferReturnRepresentation() {
        return new PreferBuilder(PREFERENCE_RETURN, RETURN_REPRESENTATION);
    }

    /**
     * Create and initialize a PreferBuilder for a {@code Prefer: return="minimal"} header.
     * @return initialized PreferBuilder
     */
    public static PreferBuilder preferReturnMinimal() {
        return new PreferBuilder(PREFERENCE_RETURN, RETURN_MINIMAL);
    }

    /**
     * Create and initialize a PreferBuilder for a {@code Prefer: wait="X"} header.
     * @param seconds seconds to wait, the <em>X</em> in the example.
     * @return initialized PreferBuilder
     */
    public static PreferBuilder preferWait(int seconds) {
        return new PreferBuilder(PREFERENCE_WAIT, String.valueOf(seconds));
    }

    /**
     * Create and initialize a PreferBuilder for a {@code Prefer: handling="strict"} header.
     * @return initialized PreferBuilder
     */
    public static PreferBuilder preferHandlingStrict() {
        return new PreferBuilder(PREFERENCE_HANDLING, HANDLING_STRICT);
    }

    /**
     * Create and initialize a PreferBuilder for a {@code Prefer: handling="lenient"} header.
     * @return initialized PreferBuilder
     */
    public static PreferBuilder preferHandlingLenient() {
        return new PreferBuilder(PREFERENCE_HANDLING, HANDLING_LENIENT);
    }

    /**
     * Create a PreferBuilder initialized with the provided PreferHeader.
     * @param prefer the PreferHeader used for initialisation
     * @return initialized PreferBuilder
     */
    public static PreferBuilder fromPrefer(PreferHeader prefer) {
        final PreferBuilder builder = new PreferBuilder(prefer.preference, prefer.preferenceValue);
        builder.params.putAll(prefer.params);
        return builder;
    }

    /**
     * Create a PreferBuilder for an arbitrary preference
     * @param preference the preference
     * @return initialized PreferBuilder
     */
    public static PreferBuilder prefer(String preference) {
        return prefer(preference, null);
    }

    /**
     * Create a PreferBuilder for an arbitrary preference
     * @param preference the preference
     * @param value the value of the preference
     * @return initialized PreferBuilder
     */
    private static PreferBuilder prefer(String preference, String value) {
        return new PreferBuilder(preference, value);
    }

    /**
     * Builder for PreferHeader
     */
    public static class PreferBuilder {

        private String preference;
        private String preferenceValue;
        private Map<String, String> params;

        private PreferBuilder(String preference) {
            this(preference, null);
        }

        private PreferBuilder(String preference, String preferenceValue) {
            this.preference = preference;
            this.preferenceValue = preferenceValue;
            this.params = new HashMap<>();
        }

        private PreferBuilder preference(String preference) {
            return preference(preference, null);
        }

        private PreferBuilder preference(String preference, String value) {
            this.preference = preference;
            this.preferenceValue = value;
            return this;
        }

        /**
         * Add a parameter (without value)
         * @param parameter the parameter to add
         * @return the PreferBuilder for chaining
         */
        public PreferBuilder parameter(String parameter) {
            this.params.put(parameter, null);
            return this;
        }

        /**
         * Add a parameter with the provided value. If the value is {@code null}, the parameter is removed.
         * @param parameter the parameter to add (or remove)
         * @param value the parameter value (or {@code null} to remove
         * @return the PreferBuilder for chaining
         */
        public PreferBuilder parameter(String parameter, String value) {
            if (value == null) {
                this.params.remove(parameter);
            } else {
                this.params.put(parameter, value);
            }
            return this;
        }

        /**
         * Add the provided parameters and their values. If the argument is {@code null}, all parameters will be removed.
         * @param params the parameters to add (or {@code null} to remove all parameters)
         * @return the PreferBuilder for chaining
         */
        public PreferBuilder parameters(Map<String, String> params) {
            if (params == null) {
                this.params.clear();
            } else {
                this.params.putAll(params);
            }
            return this;
        }

        /**
         * <strong>LDP specific:</strong> Add a "include" parameter for the given URIs.
         * @param ldpPreferUri the URIs to "include"
         * @return the PreferBuilder for chaining
         */
        public PreferBuilder include(String... ldpPreferUri) {
            return _ldp(RETURN_PARAM_INCLUDE, ldpPreferUri);
        }

        /**
         * <strong>LDP specific:</strong> Add a "omit" parameter for the given URIs.
         * @param ldpPreferUri the URIs to "omit"
         * @return the PreferBuilder for chaining
         */
        public PreferBuilder omit(String... ldpPreferUri) {
            return _ldp(RETURN_PARAM_OMIT, ldpPreferUri);
        }

        private PreferBuilder _ldp(String param, String... values) {
            if (values == null) {
                this.params.remove(param);
            } else {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        sb.append(" ");
                    }
                    sb.append(values[i]);
                }
                this.params.put(param, sb.toString());
            }
            return this;
        }

        /**
         * Create the PreferHeader. The builder can be reused.
         * @return the PreferHeader
         */
        public PreferHeader build() {
            final PreferHeader header = new PreferHeader(this.preference);
            header.preferenceValue = preferenceValue;
            header.params.putAll(params);
            return header;

        }

    }
}
