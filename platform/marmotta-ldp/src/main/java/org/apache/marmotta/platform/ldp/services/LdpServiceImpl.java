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
package org.apache.marmotta.platform.ldp.services;

import info.aduna.iteration.*;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.ldp.api.LdpBinaryStoreService;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.api.Preference;
import org.apache.marmotta.platform.ldp.exceptions.IncompatibleResourceTypeException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidInteractionModelException;
import org.apache.marmotta.platform.ldp.exceptions.InvalidModificationException;
import org.apache.marmotta.platform.ldp.patch.InvalidPatchDocumentException;
import org.apache.marmotta.platform.ldp.patch.RdfPatchUtil;
import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParserImpl;
import org.apache.marmotta.platform.ldp.util.LdpUtils;
import org.apache.marmotta.platform.ldp.util.ServerManagedPropertiesInterceptor;
import org.apache.marmotta.platform.ldp.webservices.LdpWebService;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.event.base.InterceptingRepositoryConnectionWrapper;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * LDP Service default implementation
 *
 * @author Sergio Fernández
 * @author Jakob Frank
 */
@ApplicationScoped
public class LdpServiceImpl implements LdpService {

    private static final Logger log = LoggerFactory.getLogger(LdpServiceImpl.class);

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private LdpBinaryStoreService binaryStore;

    private final URI ldpContext, ldpInteractionModelProperty, ldpUsed;

    public LdpServiceImpl() {
        ldpContext = ValueFactoryImpl.getInstance().createURI(LDP.NAMESPACE);
        ldpInteractionModelProperty = ValueFactoryImpl.getInstance().createURI(LDP.NAMESPACE, "interactionModel");
        ldpUsed = ValueFactoryImpl.getInstance().createURI(LDP.NAMESPACE, "used");
    }

    @Override
    public void init(RepositoryConnection connection, URI root) throws RepositoryException {
        final ValueFactory valueFactory = connection.getValueFactory();
        final Literal now = valueFactory.createLiteral(new Date());
        if (!exists(connection, root)) {
            connection.add(root, RDFS.LABEL, valueFactory.createLiteral("Marmotta's LDP Root Container"), ldpContext);
            connection.add(root, RDF.TYPE, LDP.Resource, ldpContext);
            connection.add(root, RDF.TYPE, LDP.RDFSource, ldpContext);
            connection.add(root, RDF.TYPE, LDP.Container, ldpContext);
            connection.add(root, RDF.TYPE, LDP.BasicContainer, ldpContext);
            connection.add(root, ldpInteractionModelProperty, InteractionModel.LDPC.getUri(), ldpContext);
            connection.add(root, DCTERMS.created, now, ldpContext);
            connection.add(root, DCTERMS.modified, now, ldpContext);
        }
    }

    @Override
    public String getResourceUri(UriInfo uriInfo) {
        final UriBuilder uriBuilder = getResourceUriBuilder(uriInfo);
        uriBuilder.path(uriInfo.getPathParameters().getFirst("local"));
        // uriBuilder.path(uriInfo.getPath().replaceFirst("/$", ""));
        String uri = uriBuilder.build().toString();
        log.debug("=== Request URI: {}", uri);
        return uri;
    }

    @Override
    public UriBuilder getResourceUriBuilder(UriInfo uriInfo) {
        final UriBuilder uriBuilder;
        if (configurationService.getBooleanConfiguration("ldp.force_baseuri", false)) {
            log.trace("UriBuilder is forced to configured baseuri <{}>", configurationService.getBaseUri());
            uriBuilder = UriBuilder.fromUri(java.net.URI.create(configurationService.getBaseUri()));
        } else {
            uriBuilder = uriInfo.getBaseUriBuilder();
        }
        uriBuilder.path(LdpWebService.PATH);
        return uriBuilder;
    }

    private URI buildURI(String resource) {
        return ValueFactoryImpl.getInstance().createURI(resource);
    }

    @Override
    public boolean exists(RepositoryConnection connection, String resource) throws RepositoryException {
        return exists(connection, buildURI(resource));
    }

    @Override
    public boolean exists(RepositoryConnection connection, URI resource) throws RepositoryException {
        return connection.hasStatement(resource, null, null, true, ldpContext);
    }

    @Override
    public boolean isReusedURI(RepositoryConnection connection, String resource) throws RepositoryException {
        return isReusedURI(connection, buildURI(resource));
    }

    @Override
    public boolean isReusedURI(RepositoryConnection connection, URI resource) throws RepositoryException {
        return connection.hasStatement(ldpContext, ldpUsed, resource, true, ldpContext);
    }

    @Override
    public boolean hasType(RepositoryConnection connection, URI resource, URI type) throws RepositoryException {
        return connection.hasStatement(resource, RDF.TYPE, type, true, ldpContext);
    }

    @Override
    public List<Statement> getLdpTypes(RepositoryConnection connection, String resource) throws RepositoryException {
        return getLdpTypes(connection, buildURI(resource));
    }

    @Override
    public List<Statement> getLdpTypes(RepositoryConnection connection, URI resource) throws RepositoryException {
        return Iterations.asList(new FilterIteration<Statement, RepositoryException>(connection.getStatements(resource, RDF.TYPE, null, false, ldpContext)) {
            @Override
            protected boolean accept(Statement statement) {
                final Value object = statement.getObject();
                return object instanceof URI && object.stringValue().startsWith(LDP.NAMESPACE);
            }
        }); //FIXME
    }

    @Override
    public boolean isRdfSourceResource(RepositoryConnection connection, String resource) throws RepositoryException {
        return isRdfSourceResource(connection, buildURI(resource));
    }

    @Override
    public boolean isRdfSourceResource(RepositoryConnection connection, URI uri) throws RepositoryException {
        return connection.hasStatement(uri, RDF.TYPE, LDP.RDFSource, true, ldpContext);
    }

    @Override
    public boolean isNonRdfSourceResource(RepositoryConnection connection, String resource) throws RepositoryException {
        return isNonRdfSourceResource(connection, buildURI(resource));
    }

    @Override
    public boolean isNonRdfSourceResource(RepositoryConnection connection, URI uri) throws RepositoryException {
        return connection.hasStatement(uri, RDF.TYPE, LDP.NonRDFSource, true, ldpContext);
    }


    @Override
    public URI getRdfSourceForNonRdfSource(final RepositoryConnection connection, URI uri) throws RepositoryException {
        final FilterIteration<Statement, RepositoryException> it =
                new FilterIteration<Statement, RepositoryException>(connection.getStatements(uri, DCTERMS.isFormatOf, null, true, ldpContext)) {
                    @Override
                    protected boolean accept(Statement statement) throws RepositoryException {
                        return statement.getObject() instanceof URI
                                && connection.hasStatement((URI) statement.getObject(), RDF.TYPE, LDP.RDFSource, true, ldpContext);
                    }
                };
        try {
            if (it.hasNext()) {
                return (URI) it.next().getObject();
            } else {
                return null;
            }
        } finally {
            it.close();
        }
    }

    @Override
    public URI getRdfSourceForNonRdfSource(RepositoryConnection connection, String resource) throws RepositoryException {
        return getRdfSourceForNonRdfSource(connection, buildURI(resource));
    }

    @Override
    public URI getNonRdfSourceForRdfSource(RepositoryConnection connection, String resource) throws RepositoryException {
        return getNonRdfSourceForRdfSource(connection, buildURI(resource));
    }

    @Override
    public URI getNonRdfSourceForRdfSource(final RepositoryConnection connection, URI uri) throws RepositoryException {
        final FilterIteration<Statement, RepositoryException> it =
                new FilterIteration<Statement, RepositoryException>(connection.getStatements(uri, DCTERMS.hasFormat, null, true, ldpContext)) {
                    @Override
                    protected boolean accept(Statement statement) throws RepositoryException {
                        return statement.getObject() instanceof URI
                                && connection.hasStatement((URI) statement.getObject(), RDF.TYPE, LDP.NonRDFSource, true, ldpContext);
                    }
                };
        try {
            if (it.hasNext()) {
                return (URI) it.next().getObject();
            } else {
                return null;
            }
        } finally {
            it.close();
        }
    }

    @Override
    public void exportResource(RepositoryConnection connection, String resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException {
        exportResource(connection, buildURI(resource), output, format);
    }

    @Override
    public void exportResource(RepositoryConnection connection, URI resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException {
        exportResource(connection, resource, output, format, null);
    }

    @Override
    public void exportResource(RepositoryConnection connection, String resource, OutputStream output, RDFFormat format, Preference preference) throws RDFHandlerException, RepositoryException {
        exportResource(connection, buildURI(resource), output, format, preference);
    }

    @Override
    public void exportResource(RepositoryConnection connection, final URI resource, OutputStream output, RDFFormat format, final Preference preference) throws RepositoryException, RDFHandlerException {
        // TODO: this should be a little more sophisticated...
        // TODO: non-membership triples flag / Prefer-header
        final RDFWriter writer = Rio.createWriter(format, output);
        final CloseableIteration<Statement, RepositoryException> contentStatements;
        if (preference == null || preference.includeContent()) {
            contentStatements = connection.getStatements(null, null, null, false, resource);
        } else {
            contentStatements = new EmptyIteration<>();
        }
        try {
            CloseableIteration<Statement, RepositoryException> ldpStatements = connection.getStatements(resource, null, null, false, ldpContext);
            if (preference != null) {
                // FIXME: Get the membership predicate from the container. See http://www.w3.org/TR/ldp/#h5_ldpdc-containtriples
                final URI membershipPred = null;
                ldpStatements = new FilterIteration<Statement, RepositoryException>(ldpStatements) {
                    @Override
                    protected boolean accept(Statement stmt) throws RepositoryException {
                        final URI p = stmt.getPredicate();
                        final Resource s = stmt.getSubject();
                        final Value o = stmt.getObject();


                        if (p.equals(LDP.contains)) return preference.includeContainment();
                        if (p.equals(membershipPred)) return preference.includeMembership();

                        return preference.includeMinimalContainer();
                    }
                };
            }
            @SuppressWarnings("unchecked")
            final CloseableIteration<Statement, RepositoryException> statements = new UnionIteration<>(
                    ldpStatements, contentStatements
            );
            LdpUtils.exportIteration(writer, resource, statements);
        } finally {
            contentStatements.close();
        }
    }

    @Override
    public void exportBinaryResource(RepositoryConnection connection, String resource, OutputStream out) throws RepositoryException, IOException {
        //TODO: check (resource, dct:format, type)
        try (InputStream in = binaryStore.read(resource)) {
            if (in != null) {
                IOUtils.copy(in, out);
            } else {
                throw new IOException("Cannot read resource " + resource);
            }
        }

    }

    @Override
    public void exportBinaryResource(RepositoryConnection connection, URI resource, OutputStream out) throws RepositoryException, IOException {
        exportBinaryResource(connection, resource.stringValue(), out);
    }

    @Override
    public String getMimeType(RepositoryConnection connection, String resource) throws RepositoryException {
        return getMimeType(connection, buildURI(resource));
    }

    @Override
    public String getMimeType(RepositoryConnection connection, URI uri) throws RepositoryException {
        final RepositoryResult<Statement> formats = connection.getStatements(uri, DCTERMS.format, null, false, ldpContext);
        try {
            if (formats.hasNext()) return formats.next().getObject().stringValue();
        } finally {
            formats.close();
        }
        return null;
    }
    @Override
    public String addResource(RepositoryConnection connection, String container, String resource, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException {
        return addResource(connection, buildURI(container), buildURI(resource), InteractionModel.LDPC, type, stream);
    }

    @Override
    public String addResource(RepositoryConnection connection, URI container, URI resource, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException {
        return addResource(connection, container, resource, InteractionModel.LDPC, type, stream);
    }

    @Override
    public String addResource(RepositoryConnection connection, String container, String resource, InteractionModel interactionModel, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException {
        return addResource(connection, buildURI(container), buildURI(resource), interactionModel, type, stream);
    }

    @Override
    public String addResource(RepositoryConnection connection, URI container, URI resource, InteractionModel interactionModel, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException {
        ValueFactory valueFactory = connection.getValueFactory();

        // Add container triples (Sec. 5.2.3.2)
        // container and meta triples!

        final Literal now = valueFactory.createLiteral(new Date());

        // FIXME: This is redundant if the container already existed as a Resource!
        connection.add(container, RDF.TYPE, LDP.Resource, ldpContext);
        connection.add(container, RDF.TYPE, LDP.RDFSource, ldpContext);
        // end
        connection.add(container, RDF.TYPE, LDP.Container, ldpContext);
        // TODO: For the future we might need to check for other container types here first.
        connection.add(container, RDF.TYPE, LDP.BasicContainer, ldpContext);

        connection.remove(container, DCTERMS.modified, null, ldpContext);
        connection.add(container, DCTERMS.modified, now, ldpContext);

        connection.add(resource, RDF.TYPE, LDP.Resource, ldpContext);
        connection.add(resource, RDF.TYPE, LDP.RDFSource, ldpContext);
        connection.add(resource, ldpInteractionModelProperty, interactionModel.getUri(), ldpContext);
        if (interactionModel == InteractionModel.LDPC) {
            connection.add(resource, RDF.TYPE, LDP.Container, ldpContext);
            // TODO: For the future we might need to check for other container types here first.
            connection.add(resource, RDF.TYPE, LDP.BasicContainer, ldpContext);
        }
        connection.add(resource, DCTERMS.created, now, ldpContext);
        connection.add(resource, DCTERMS.modified, now, ldpContext);

        // Add the bodyContent
        final RDFFormat rdfFormat = LdpUtils.matchDefaultMIMEType(type, LdpUtils.filterAvailableParsers(SERVER_PREFERED_RDF_FORMATS), null);
        if (rdfFormat == null) {
            log.debug("Creating new LDP-NR, because no suitable RDF parser found for type {}", type);
            final Literal format = valueFactory.createLiteral(type);
            final URI binaryResource = valueFactory.createURI(resource.stringValue() + LdpUtils.getExtension(type));
            log.debug("LDP-NR is <{}>", binaryResource);
            log.debug("Corresponding LDP-RS is <{}>", resource);

            connection.add(container, LDP.contains, binaryResource, ldpContext);

            connection.add(binaryResource, DCTERMS.created, now, ldpContext);
            connection.add(binaryResource, DCTERMS.modified, now, ldpContext);
            connection.add(binaryResource, RDF.TYPE, LDP.Resource, ldpContext);
            connection.add(binaryResource, RDF.TYPE, LDP.NonRDFSource, ldpContext);

            //extra triples
            //TODO: check conformance with 6.2.3.12
            connection.add(binaryResource, DCTERMS.format, format, ldpContext); //nie:mimeType ?
            connection.add(binaryResource, DCTERMS.isFormatOf, resource, ldpContext);
            connection.add(resource, DCTERMS.hasFormat, binaryResource, ldpContext);

            //TODO: something else?

            binaryStore.store(binaryResource, stream);//TODO: exceptions control

            return binaryResource.stringValue();
        } else {
            log.debug("Creating new LDP-RS, data provided as {}", rdfFormat.getName());
            connection.add(container, LDP.contains, resource, ldpContext);

            final InterceptingRepositoryConnectionWrapper filtered = new InterceptingRepositoryConnectionWrapper(connection.getRepository(), connection);
            final ServerManagedPropertiesInterceptor managedPropertiesInterceptor = new ServerManagedPropertiesInterceptor(ldpContext, resource);
            filtered.addRepositoryConnectionInterceptor(managedPropertiesInterceptor);

            filtered.add(stream, resource.stringValue(), rdfFormat, resource);

            return resource.stringValue();
        }
    }

    @Override
    public String updateResource(RepositoryConnection connection, final String resource, InputStream stream, final String type) throws RepositoryException, IncompatibleResourceTypeException, RDFParseException, IOException, InvalidModificationException {
        return updateResource(connection, buildURI(resource), stream, type);
    }

    @Override
    public String updateResource(final RepositoryConnection connection, final URI resource, InputStream stream, final String type) throws RepositoryException, IncompatibleResourceTypeException, IOException, RDFParseException, InvalidModificationException {
        return updateResource(connection, resource, stream, type, false);
    }

    @Override
    public String updateResource(RepositoryConnection connection, final String resource, InputStream stream, final String type, final boolean overwrite) throws RepositoryException, IncompatibleResourceTypeException, RDFParseException, IOException, InvalidModificationException {
        return updateResource(connection, buildURI(resource), stream, type, false);
    }

    @Override
    public String updateResource(final RepositoryConnection connection, final URI resource, InputStream stream, final String type, final boolean overwrite) throws RepositoryException, IncompatibleResourceTypeException, RDFParseException, IOException, InvalidModificationException {
        final ValueFactory valueFactory = connection.getValueFactory();
        final Literal now = valueFactory.createLiteral(new Date());


        // TODO: find a better way to ingest n-triples (text/plain) while still supporting regular text files
        final RDFFormat rdfFormat = ("text/plain".equals(type) ? null : Rio.getParserFormatForMIMEType(type));
        // Check submitted format vs. real resource type (RDF-S vs. Non-RDF)
        if (rdfFormat == null && isNonRdfSourceResource(connection, resource)) {
            log.debug("Updating <{}> as LDP-NR (binary) - {}", resource, type);

            final Literal format = valueFactory.createLiteral(type);

            connection.remove(resource, DCTERMS.format, null, ldpContext);
            connection.add(resource, DCTERMS.format, format, ldpContext); //nie:mimeType ?
            connection.remove(resource, DCTERMS.modified, null, ldpContext);
            connection.add(resource, DCTERMS.modified, now, ldpContext);

            final URI ldp_rs = getRdfSourceForNonRdfSource(connection, resource);
            if (ldp_rs != null) {
                connection.remove(ldp_rs, DCTERMS.modified, null, ldpContext);
                connection.add(ldp_rs, DCTERMS.modified, now, ldpContext);
                log.trace("Updated Meta-Data of LDP-RS <{}> for LDP-NR <{}>; Modified: {}", ldp_rs, resource, now);
            } else {
                log.debug("LDP-RS for LDP-NR <{}> not found", resource);
            }
            log.trace("Meta-Data for <{}> updated; Format: {}, Modified: {}", resource, format, now);

            binaryStore.store(resource, stream);//TODO: exceptions control

            log.trace("LDP-NR <{}> updated", resource);
            return resource.stringValue();
        } else if (rdfFormat != null && isRdfSourceResource(connection, resource)) {
            log.debug("Updating <{}> as LDP-RS - {}", resource, rdfFormat.getDefaultMIMEType());

            connection.clear(resource);
            final InterceptingRepositoryConnectionWrapper filtered = new InterceptingRepositoryConnectionWrapper(connection.getRepository(), connection);
            final ServerManagedPropertiesInterceptor managedPropertiesInterceptor = new ServerManagedPropertiesInterceptor(ldpContext, resource);
            filtered.addRepositoryConnectionInterceptor(managedPropertiesInterceptor);

            filtered.add(stream, resource.stringValue(), rdfFormat, resource);

            final Set<URI> deniedProperties = managedPropertiesInterceptor.getDeniedProperties();
            if (!deniedProperties.isEmpty()) {
                final URI prop = deniedProperties.iterator().next();
                log.debug("Invalid property modification in update: <{}> is a server controlled property", prop);
                throw new InvalidModificationException(String.format("Must not update <%s> using PUT", prop));
            } else {
                // This has to happen *AFTER* the post-body was added:
                connection.remove(resource, DCTERMS.modified, null, ldpContext);
                connection.add(resource, DCTERMS.modified, now, ldpContext);

                log.trace("LDP-RS <{}> updated", resource);
                return resource.stringValue();
            }
        } else if (rdfFormat == null) {
            final String mimeType = getMimeType(connection, resource);
            log.debug("Incompatible replacement: Can't replace {} with {}", mimeType, type);
            throw new IncompatibleResourceTypeException(mimeType, type);
        } else {
            log.debug("Incompatible replacement: Can't replace a LDP-RS with {}", type);
            throw new IncompatibleResourceTypeException("RDF", type);
        }
    }

    @Override
    public EntityTag generateETag(RepositoryConnection connection, String resource) throws RepositoryException {
        return generateETag(connection, buildURI(resource));
    }

    @Override
    public EntityTag generateETag(RepositoryConnection connection, URI uri) throws RepositoryException {
        if (isNonRdfSourceResource(connection, uri)) {
            final String hash = binaryStore.getHash(uri.stringValue());
            if (hash != null) {
                return new EntityTag(hash, false);
            } else {
                return null;
            }
        } else {
            final RepositoryResult<Statement> stmts = connection.getStatements(uri, DCTERMS.modified, null, true, ldpContext);
            try {
                // TODO: ETag is the last-modified date (explicitly managed) thus only weak.
                Date latest = null;
                while (stmts.hasNext()) {
                    Value o = stmts.next().getObject();
                    if (o instanceof Literal) {
                        Date d = ((Literal)o).calendarValue().toGregorianCalendar().getTime();
                        if (latest == null || d.after(latest)) {
                            latest = d;
                        }
                    }
                }
                if (latest != null) {
                    return new EntityTag(String.valueOf(latest.getTime()), true);
                } else {
                    return null;
                }
            } finally {
                stmts.close();
            }
        }
    }

    @Override
    public Date getLastModified(RepositoryConnection connection, String resource) throws RepositoryException {
        return getLastModified(connection, buildURI(resource));
    }

    @Override
    public Date getLastModified(RepositoryConnection connection, URI uri) throws RepositoryException {
        final RepositoryResult<Statement> stmts = connection.getStatements(uri, DCTERMS.modified, null, true, ldpContext);
        try {
            Date latest = null;
            while (stmts.hasNext()) {
                Value o = stmts.next().getObject();
                if (o instanceof Literal) {
                    Date d = ((Literal)o).calendarValue().toGregorianCalendar().getTime();
                    if (latest == null || d.after(latest)) {
                        latest = d;
                    }
                }
            }
            return latest;
        } finally {
            stmts.close();
        }
    }

    @Override
    public void patchResource(RepositoryConnection connection, String resource, InputStream patchData, boolean strict) throws RepositoryException, ParseException, InvalidModificationException, InvalidPatchDocumentException {
        patchResource(connection, buildURI(resource), patchData, strict);
    }

    @Override
    public void patchResource(RepositoryConnection connection, URI uri, InputStream patchData, boolean strict) throws RepositoryException, ParseException, InvalidModificationException, InvalidPatchDocumentException {
        final Literal now = connection.getValueFactory().createLiteral(new Date());

        log.trace("parsing patch");
        List<PatchLine> patch = new RdfPatchParserImpl(patchData).parsePatch();

        // we are allowed to restrict the patch contents (Sec. ???)
        log.trace("checking for invalid patch statements");
        for (PatchLine patchLine : patch) {
            if (LDP.contains.equals(patchLine.getStatement().getPredicate())) {
                throw new InvalidModificationException("must not change <" + LDP.contains.stringValue() + "> via PATCH");
            }
        }

        log.debug("patching <{}> ({} changes)", uri.stringValue(), patch.size());

        RdfPatchUtil.applyPatch(connection, patch, uri);

        log.trace("update resource meta");
        connection.remove(uri, DCTERMS.modified, null, ldpContext);
        connection.add(uri, DCTERMS.modified, now, ldpContext);

    }

    @Override
    public boolean deleteResource(RepositoryConnection connection, String resource) throws RepositoryException {
        return deleteResource(connection, buildURI(resource));
    }

    @Override
    public boolean deleteResource(RepositoryConnection connection, URI resource) throws RepositoryException {
        final Literal now = connection.getValueFactory().createLiteral(new Date());

        // Delete corresponding containment and membership triples (Sec. 5.2.5.1)
        RepositoryResult<Statement> stmts = connection.getStatements(null, LDP.contains, resource, false, ldpContext);
        try {
            while (stmts.hasNext()) {
                Statement st = stmts.next();
                connection.remove(st.getSubject(), DCTERMS.modified, null, ldpContext);
                connection.add(st.getSubject(), DCTERMS.modified, now, ldpContext);
                connection.remove(st);
            }
        } finally {
            stmts.close();
        }
        // Sec. 5.2.5.2: When an LDPR identified by the object of a containment triple is deleted, and the LDPC server created an associated LDP-RS, the LDPC server must also remove the associated LDP-RS it created.
        RepositoryResult<Statement> associated = connection.getStatements(resource, DCTERMS.isFormatOf, null, false, ldpContext);
        try {
            while (associated.hasNext()) {
                Statement st = associated.next();
                if (st.getObject() instanceof Resource) {
                    connection.remove((Resource) st.getObject(), null, null);
                }
                connection.remove(st);
            }
        } finally {
            associated.close();
        }

        // Delete LDP-NR (binary)
        binaryStore.delete(resource);

        // Delete the resource meta
        connection.remove(resource, null, null, ldpContext);

        // Delete the resource data
        connection.clear(resource);

        // Sec. 5.2.3.11: LDP servers that allow member creation via POST should not re-use URIs.
        connection.add(ldpContext, ldpUsed, resource, ldpContext);

        return true;
    }

    @Override
    public InteractionModel getInteractionModel(List<Link> linkHeaders) throws InvalidInteractionModelException {
        if (log.isTraceEnabled()) {
            log.trace("Checking Link-Headers for LDP Interaction Models");
            for (Link link: linkHeaders) {
                log.trace(" - {}", link);
            }
        }
        for (Link link: linkHeaders) {
            if ("type".equalsIgnoreCase(link.getRel())) {
                final String href = link.getUri().toASCIIString();
                if (LDP.Resource.stringValue().equals(href)) {
                    log.debug("LDPR Interaction Model detected");
                    return InteractionModel.LDPR;
                } else if (LDP.Container.stringValue().equals(href) || LDP.BasicContainer.stringValue().equals(href)) {
                    log.debug("LDPC Interaction Model detected");
                    return InteractionModel.LDPC;
                } else if (LDP.DirectContainer.stringValue().equals(href) || LDP.IndirectContainer.stringValue().equals(href)) {
                    log.warn("only Basic Container interaction is supported");
                    log.debug("LDPC Interaction Model detected");
                    return InteractionModel.LDPC;
                } else {
                    log.debug("Invalid/Unknown LDP Interaction Model: {}", href);
                    throw new InvalidInteractionModelException(href);
                }
            }
        }
        log.debug("No LDP Interaction Model specified, defaulting to {}", InteractionModel.LDPC);
        // Default Interaction Model is LDPC
        return InteractionModel.LDPC;
    }

    @Override
    public InteractionModel getInteractionModel(RepositoryConnection connection, String resource) throws RepositoryException {
        return getInteractionModel(connection, buildURI(resource));
    }

    @Override
    public InteractionModel getInteractionModel(RepositoryConnection connection, URI uri) throws RepositoryException {
        if (connection.hasStatement(uri, ldpInteractionModelProperty, InteractionModel.LDPC.getUri(), true, ldpContext)) {
            return InteractionModel.LDPC;
        } else if (connection.hasStatement(uri, ldpInteractionModelProperty, InteractionModel.LDPR.getUri(), true, ldpContext)) {
            return InteractionModel.LDPR;
        }

        log.info("No LDP Interaction Model specified for <{}>, defaulting to {}", uri.stringValue(), InteractionModel.LDPC);
        // Default Interaction Model is LDPC
        return InteractionModel.LDPC;
    }
}
