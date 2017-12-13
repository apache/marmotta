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
package org.apache.marmotta.platform.core.webservices.status;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.modules.ModuleService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.user.UserService;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Webservice for checking the current state of the Marmotta platform.
 */
@ApplicationScoped
@Path("/ping")
public class StatusWebservice {

    private static final String CONFIG_KEY_MARMOTTA_ENABLED = "marmotta.enabled";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private UserService userService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    @Inject
    private ModuleService moduleService;

    @PostConstruct
    private void init() {
        ResteasyProviderFactory.getInstance().register(PingResponseMessageBodyWriter.class);
    }

    @GET
    @Produces({"application/json", "text/plain"})
    public Response ping(@QueryParam("extended") @DefaultValue("1") int detailLevel, @QueryParam("echo") String echoMessage) {

        // Check if the server is out of order/down for mainenance
        if (!configurationService.getBooleanConfiguration(CONFIG_KEY_MARMOTTA_ENABLED, true)) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("this instance is currently not serving requests.")
                    .build();
        } else {
            try {
                final PingResponse response = new PingResponse(StringUtils.defaultString(echoMessage,"pong"), detailLevel, userService);

                // More checking of the ConfigurationService
                if (StringUtils.isNoneBlank(
                        configurationService.getHome(),
                        configurationService.getBaseUri()
                )) {
                    response.setConfigStatus(configurationService);
                } else {
                    throw new Exception("configuration-service failed");
                }

                // Check the TripleStore:
                try {
                    final RepositoryConnection con = sesameService.getConnection();
                    try {
                        con.begin();
                        response.setSesameStatus(con);
                        con.commit();
                    } finally {
                        con.close();
                    }
                } catch (final Throwable t) {
                    throw new Exception("trimple-store not available", t);
                }

                // Check available modules
                response.setModuleStatus(moduleService);

                return Response.ok(response).build();
            } catch (Throwable t) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(t.getMessage()).build();
            }
        }
    }

    @JsonAutoDetect(fieldVisibility= JsonAutoDetect.Visibility.ANY)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected static class PingResponse {

        @JsonIgnore
        private final int detailLevel;

        private String message;
        private final Map<String, String> user = new HashMap<>();
        private final Map<String, String> config = new HashMap<>();
        private final Map<String, String> tripleStore = new HashMap<>();
        private final Set<String> modules = new HashSet<>();

        public PingResponse(String message, int requestedDetailLevle, UserService userService) {
            this.message = message;
            final URI u = checkNotNull(userService.getCurrentUser(), "no user available");

            int allowedDetailLevel;
            if (userService.isAnonymous(u)) {
                allowedDetailLevel = 0;
            } else if (Objects.equals(u, userService.getAdminUser())) {
                allowedDetailLevel = 2;
            } else {
                allowedDetailLevel = 1;
            }
            this.detailLevel = Math.min(allowedDetailLevel, requestedDetailLevle);
            setUserStatus(userService);
        }

        public void setUserStatus(UserService userService) {
            final URI u = userService.getCurrentUser();

            user.clear();
            switch (detailLevel) {
                case 2:
                    user.put("name", u.getLocalName());
                case 1:
                    user.put("uri", u.stringValue());
                    break;
                default:
                    // nop;
            }
        }

        public void setConfigStatus(ConfigurationService configurationService) {
            config.clear();
            switch (detailLevel) {
                case 2:
                    config.put("homeDir", configurationService.getHome());
                    config.put("context.default", configurationService.getDefaultContext());
                    config.put("context.enhanced", configurationService.getEnhancerContex());
                    config.put("context.inferred", configurationService.getInferredContext());
                case 1:
                    config.put("baseUri", configurationService.getBaseUri());
                    config.put("serverUrl", configurationService.getServerUri());
                    break;
                case 0:
                default:
                    //nop;
            }
        }

        public void setSesameStatus(RepositoryConnection repositoryConnection) throws RepositoryException {
            final Repository repository = repositoryConnection.getRepository();
            tripleStore.clear();
            switch (detailLevel) {
                case 2:
                    tripleStore.put("statements", String.valueOf(repositoryConnection.size()));
                    tripleStore.put("namespaces", String.valueOf(sizeOf(repositoryConnection.getNamespaces())));
                    tripleStore.put("contexts", String.valueOf(sizeOf(repositoryConnection.getContextIDs())));
                case 1:
                    tripleStore.put("writeable", String.valueOf(repository.isWritable()));
                    break;
                case 0:
                default:
                    //nop;
            }
        }

        private static long sizeOf(RepositoryResult<?> repositoryResult) throws RepositoryException {
            try {
                long result = 0;
                while (repositoryResult.hasNext()) {
                    repositoryResult.next();
                    result++;
                }
                return result;
            } finally {
                repositoryResult.close();
            }
        }

        public void setModuleStatus(ModuleService moduleStatus) {
            switch (detailLevel) {
                case 2:
                case 1:
                    modules.addAll(moduleStatus.listModules());
                    break;
                case 0:
                default:
                    modules.clear();
            }
        }
    }

    @Provider
    @Produces("text/plain")
    public static class PingResponseMessageBodyWriter implements MessageBodyWriter<PingResponse> {
        /**
         * Ascertain if the MessageBodyWriter supports a particular type.
         *
         * @param type        the class of instance that is to be written.
         * @param genericType the type of instance to be written, obtained either
         *                    by reflection of a resource method return type or via inspection
         *                    of the returned instance. {@link javax.ws.rs.core.GenericEntity}
         *                    provides a way to specify this information at runtime.
         * @param annotations an array of the annotations attached to the message entity instance.
         * @param mediaType   the media type of the HTTP entity.
         * @return {@code true} if the type is supported, otherwise {@code false}.
         */
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return PingResponse.class.isAssignableFrom(type);
        }

        /**
         * Originally, the method has been called before {@code writeTo} to ascertain the length in bytes of
         * the serialized form of {@code t}. A non-negative return value has been used in a HTTP
         * {@code Content-Length} header.
         * <p>
         * As of JAX-RS 2.0, the method has been deprecated and the value returned by the method is ignored
         * by a JAX-RS runtime. All {@code MessageBodyWriter} implementations are advised to return {@code -1}
         * from the method. Responsibility to compute the actual {@code Content-Length} header value has been
         * delegated to JAX-RS runtime.
         * </p>
         *
         * @param pingResponse the instance to write
         * @param type         the class of instance that is to be written.
         * @param genericType  the type of instance to be written. {@link javax.ws.rs.core.GenericEntity}
         *                     provides a way to specify this information at runtime.
         * @param annotations  an array of the annotations attached to the message entity instance.
         * @param mediaType    the media type of the HTTP entity.
         * @return length in bytes or -1 if the length cannot be determined in advance.
         */
        @Override
        public long getSize(PingResponse pingResponse, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        /**
         * Write a type to an HTTP message. The message header map is mutable
         * but any changes must be made before writing to the output stream since
         * the headers will be flushed prior to writing the message body.
         *
         * @param pingResponse the instance to write.
         * @param type         the class of instance that is to be written.
         * @param genericType  the type of instance to be written. {@link javax.ws.rs.core.GenericEntity}
         *                     provides a way to specify this information at runtime.
         * @param annotations  an array of the annotations attached to the message entity instance.
         * @param mediaType    the media type of the HTTP entity.
         * @param httpHeaders  a mutable map of the HTTP message headers.
         * @param entityStream the {@link java.io.OutputStream} for the HTTP entity. The
         *                     implementation should not close the output stream.
         * @throws java.io.IOException                 if an IO error arises.
         * @throws javax.ws.rs.WebApplicationException if a specific HTTP error response needs to be produced.
         *                                             Only effective if thrown prior to the message being committed.
         */
        @Override
        public void writeTo(PingResponse pingResponse, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            final PrintStream out = new PrintStream(entityStream);

            out.printf("Message: %s%n", pingResponse.message);
            writeMapTo("Config", pingResponse.config, out);
            writeMapTo("TripleStore", pingResponse.tripleStore, out);
            writeMapTo("User", pingResponse.user, out);
            writeSetTo("Modules", pingResponse.modules, out);
        }

        private void writeSetTo(String title, Set<String> set, PrintStream out) {
            if (set != null) {
                out.printf("%s:%n", title);
                for (String entry: set) {
                    out.printf("\t%s%n", entry);
                }
            }
        }

        private void writeMapTo(String title, Map<String, String> map, PrintStream out) {
            if (map != null) {
                out.printf("%s:%n", title);
                for (Map.Entry<String, String> e: map.entrySet()) {
                    out.printf("\t%s: %s%n", e.getKey(), e.getValue());
                }
            }
        }

    }

}
