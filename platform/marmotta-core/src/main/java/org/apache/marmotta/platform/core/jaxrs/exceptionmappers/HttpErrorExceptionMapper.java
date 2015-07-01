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
package org.apache.marmotta.platform.core.jaxrs.exceptionmappers;

import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.commons.http.MarmottaHttpUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.exception.HttpErrorException;
import org.apache.marmotta.platform.core.jaxrs.ErrorMessage;
import org.slf4j.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.net.HttpHeaders.ACCEPT;

/**
 * Map HttpErrorExceptionMapper to a internal server error and return the default error object
 *
 * @author Sergio Fernández
 */
@Provider
@Dependent
public class HttpErrorExceptionMapper implements CDIExceptionMapper<HttpErrorException> {

    private final String TEMPLATE = "error.ftl";

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private TemplatingService templatingService;

    /**
     * Map an exception to a {@link javax.ws.rs.core.Response}. Returning
     * {@code null} results in a {@link javax.ws.rs.core.Response.Status#NO_CONTENT}
     * response. Throwing a runtime exception results in a
     * {@link javax.ws.rs.core.Response.Status#INTERNAL_SERVER_ERROR} response
     *
     * @param exception the exception to map to a response
     * @return a response mapped from the supplied exception
     */
    @Override
    public Response toResponse(HttpErrorException exception) {

        final Map<String, String> exceptionHeaders = exception.getHeaders();

        boolean htmlError = true; //HTML still by default
        if (exceptionHeaders.containsKey(ACCEPT)) {
            final String acceptHeader = exceptionHeaders.get(ACCEPT);
            final ContentType bestContentType = MarmottaHttpUtils.bestContentType(Arrays.asList(new ContentType("text", "html"), new ContentType("application", "json")),
                    MarmottaHttpUtils.parseAcceptHeader(acceptHeader));
            htmlError = bestContentType == null || !bestContentType.matches(new ContentType("application", "json"));
        }

        Response.ResponseBuilder responseBuilder;
        if (htmlError) {
            //html rendering
            Map<String, Object> data = new HashMap<>();
            data.put("status", exception.getStatus());
            data.put("reason", exception.getReason());

            data.put("message", exception.getMessage());
            if (StringUtils.isNotBlank(exception.getUri())) {
                data.put("uri", exception.getUri());
                try {
                    data.put("encoded_uri", URLEncoder.encode(exception.getUri(), "UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                    data.put("encoded_uri", exception.getUri());
                }
            } else {
                data.put("uri", "");
                data.put("encoded_uri", "");
            }

            try {
                responseBuilder = Response.status(exception.getStatus()).entity(templatingService.process(TEMPLATE, data));
            } catch (IOException | TemplateException e) {
                log.error("Error rendering html error template {}: {}", TEMPLATE, e.getMessage());
                responseBuilder = Response.status(exception.getStatus()).entity(e.getMessage());
            }
        } else {
            //simple json error message
            responseBuilder = Response.status(exception.getStatus()).entity(new ErrorMessage(exception));
        }

        //forward headers
        for (Map.Entry<String, String> entry : exceptionHeaders.entrySet()) {
            responseBuilder.header(entry.getKey(), entry.getValue());
        }

        return responseBuilder.build();
    }
}
