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

import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iterations;
import info.aduna.iteration.UnionIteration;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.ldp.api.LdpBinaryStoreService;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.exceptions.InvalidModificationException;
import org.apache.marmotta.platform.ldp.patch.InvalidPatchDocumentException;
import org.apache.marmotta.platform.ldp.patch.RdfPatchUtil;
import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParserImpl;
import org.apache.marmotta.platform.ldp.util.LdpUtils;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.EntityTag;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

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

    private final URI ldpContext;

    public LdpServiceImpl() {
        ldpContext = ValueFactoryImpl.getInstance().createURI(LDP.NAMESPACE);
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
        return connection.hasStatement(uri, RDF.TYPE, LDP.NonRdfResource, true, ldpContext);
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
        }finally {
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
                                && connection.hasStatement((URI) statement.getObject(), RDF.TYPE, LDP.NonRdfResource, true, ldpContext);
                    }
                };
        try {
            if (it.hasNext()) {
                return (URI) it.next().getObject();
            } else {
                return null;
            }
        }finally {
            it.close();
        }
    }

    @Override
    public void exportResource(RepositoryConnection connection, String resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException {
        exportResource(connection, buildURI(resource), output, format);
    }

    @Override
    public void exportResource(RepositoryConnection connection, URI resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException {
        // TODO: this should be a little more sophisticated...
        // TODO: non-membership triples flag / Prefer-header
        RDFWriter writer = Rio.createWriter(format, output);
        UnionIteration<Statement, RepositoryException> union = new UnionIteration<>(
                connection.getStatements(null, null, null, false, resource),
                connection.getStatements(resource, null, null, false, ldpContext)
        );
        try {
            LdpUtils.exportIteration(writer, resource, union);
        } finally {
            union.close();
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
        return addResource(connection, buildURI(container), buildURI(resource), type, stream);
    }

    @Override
    public String addResource(RepositoryConnection connection, URI container, URI resource, String type, InputStream stream) throws RepositoryException, IOException, RDFParseException {
        ValueFactory valueFactory = connection.getValueFactory();

        // Add container triples (Sec. 6.4.3)
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
        connection.add(resource, DCTERMS.created, now, ldpContext);
        connection.add(resource, DCTERMS.modified, now, ldpContext);

        // Add the bodyContent
        final RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(type);
        if (rdfFormat == null) {
            log.debug("POST creates new LDP-NR, because no suitable RDF parser found for type {}", type);
            Literal format = valueFactory.createLiteral(type);
            URI binaryResource = valueFactory.createURI(resource.stringValue() + LdpUtils.getExtension(type));

            connection.add(container, LDP.contains, binaryResource, ldpContext);

            connection.add(binaryResource, DCTERMS.created, now, ldpContext);
            connection.add(binaryResource, DCTERMS.modified, now, ldpContext);
            connection.add(binaryResource, RDF.TYPE, LDP.Resource, ldpContext);
            connection.add(binaryResource, RDF.TYPE, LDP.NonRdfResource, ldpContext);

            //extra triples
            //TODO: check conformance with 6.2.3.12
            connection.add(binaryResource, DCTERMS.format, format, ldpContext); //nie:mimeType ?
            connection.add(binaryResource, DCTERMS.isFormatOf, resource, ldpContext);
            connection.add(resource, DCTERMS.hasFormat, binaryResource, ldpContext);

            //TODO: something else?

            binaryStore.store(binaryResource, stream);//TODO: exceptions control

            return binaryResource.stringValue();
        } else {
            log.debug("POST creates new LDP-RR, data provided as {}", rdfFormat.getName());
            connection.add(container, LDP.contains, resource, ldpContext);

            // FIXME: We are (are we?) allowed to filter out server-managed properties here
            connection.add(stream, resource.stringValue(), rdfFormat, resource);

            return resource.stringValue();
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
                connection.remove(st.getSubject(), DCTERMS.modified, null);
                connection.add(st.getSubject(), DCTERMS.modified, now);
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

        // FIXME: Sec. 5.2.3.11: LDP servers that allow member creation via POST should not re-use URIs.

        return true;
    }

}
