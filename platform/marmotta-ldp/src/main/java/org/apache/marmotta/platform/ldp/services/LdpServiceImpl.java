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

import info.aduna.iteration.Iterations;
import info.aduna.iteration.UnionIteration;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.apache.marmotta.platform.ldp.exceptions.InvalidModificationException;
import org.apache.marmotta.platform.ldp.patch.InvalidPatchDocumentException;
import org.apache.marmotta.platform.ldp.patch.RdfPatchUtil;
import org.apache.marmotta.platform.ldp.patch.model.PatchLine;
import org.apache.marmotta.platform.ldp.patch.parser.ParseException;
import org.apache.marmotta.platform.ldp.patch.parser.RdfPatchParserImpl;
import org.apache.marmotta.platform.ldp.util.LdpWebServiceUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
import javax.ws.rs.core.MediaType;
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

    private URI buildURI(String resource) {
        return ValueFactoryImpl.getInstance().createURI(resource);
    }

    @Override
    public boolean exists(RepositoryConnection connection, String resource) throws RepositoryException {
        return exists(connection, buildURI(resource));
    }

    @Override
    public boolean exists(RepositoryConnection connection, URI resource) throws RepositoryException {
        final URI ldpContext = connection.getValueFactory().createURI(LDP.NAMESPACE);
        return connection.hasStatement(resource, RDF.TYPE, null, true, ldpContext);
    }

    @Override
    public List<Statement> getStatements(RepositoryConnection connection, String resource) throws RepositoryException {
        return getStatements(connection, buildURI(resource));
    }

    @Override
    public List<Statement> getStatements(RepositoryConnection connection, URI resource) throws RepositoryException {
            final URI ldp = connection.getValueFactory().createURI(LDP.NAMESPACE);
            return Iterations.asList(connection.getStatements(resource, RDF.TYPE, null, false, ldp)); //FIXME
    }

    @Override
    public void exportResource(RepositoryConnection connection, String resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException {
        exportResource(connection, buildURI(resource), output, format);
    }

    @Override
    public void exportResource(RepositoryConnection connection, URI resource, OutputStream output, RDFFormat format) throws RepositoryException, RDFHandlerException {
        URI ldpContext = connection.getValueFactory().createURI(LDP.NAMESPACE);
        // TODO: this should be a little more sophisticated...
        // TODO: non-membership triples flag / Prefer-header
        RDFWriter writer = Rio.createWriter(format, output);
        UnionIteration<Statement, RepositoryException> union = new UnionIteration<>(
                connection.getStatements(null, null, null, false, resource),
                connection.getStatements(resource, null, null, false, ldpContext)
        );
        try {
            LdpWebServiceUtils.exportIteration(writer, resource, union);
        } finally {
            union.close();
        }
    }

    @Override
    public boolean addResource(RepositoryConnection connection, String container, String resource, MediaType type, InputStream stream) throws RepositoryException, IOException, RDFParseException {
        return addResource(connection, buildURI(container), buildURI(resource), type, stream);
    }

    @Override
    public boolean addResource(RepositoryConnection connection, URI container, URI resource, MediaType type, InputStream stream) throws RepositoryException, IOException, RDFParseException {
        final URI ldpContext = connection.getValueFactory().createURI(LDP.NAMESPACE);

        // FIXME: this should be done in LdpWebService
        log.trace("Checking possible name clash for new resource <{}>", resource.stringValue());
        if (connection.hasStatement(resource, null, null, false, ldpContext)) {
            int i = 0;
            final String base = resource.stringValue();
            do {
                final String candidate = base + "-" + (++i);
                log.trace("<{}> already exists, trying <{}>", resource.stringValue(), candidate);
                resource = connection.getValueFactory().createURI(candidate);
            } while (connection.hasStatement(resource, null, null, false, ldpContext));
            log.debug("resolved name clash, new resource will be <{}>", resource.stringValue());
        } else {
            log.debug("no name clash for <{}>", resource);
        }

        log.debug("POST to <{}> will create new LDP-R <{}>", container, resource);

        // Add container triples (Sec. 6.4.3)
        // container and meta triples!

        Literal now = connection.getValueFactory().createLiteral(new Date());

        connection.add(container, RDF.TYPE, LDP.BasicContainer, ldpContext);
        connection.add(container, LDP.contains, resource, ldpContext);
        connection.remove(container, DCTERMS.modified, null, ldpContext);
        connection.add(container, DCTERMS.modified, now, ldpContext);

        connection.add(resource, RDF.TYPE, LDP.Resource, ldpContext);
        connection.add(resource, DCTERMS.created, now, ldpContext);
        connection.add(resource, DCTERMS.modified, now, ldpContext);

        // TODO: No LDP-BC for now!

        // Add the bodyContent
        log.trace("Content ({}) for new resource <{}>", type, resource);
        final RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(type.toString(), RDFFormat.TURTLE);
        if (rdfFormat == null) {
            log.debug("POST creates new LDP-BR with type {}", type);
            log.warn("LDP-BR not (yet) supported!");
            throw new UnsupportedRDFormatException("No available parser for " + type.toString());
        } else {
            log.debug("POST creates new LDP-RR, data provided as {}", rdfFormat.getName());

            // FIXME: We are (are we?) allowed to filter out server-managed properties here
            connection.add(stream, resource.stringValue(), rdfFormat, resource);

            return true;
        }
    }

    @Override
    public EntityTag generateETag(RepositoryConnection connection, String resource) throws RepositoryException {
        return generateETag(connection, buildURI(resource));
    }

    @Override
    public EntityTag generateETag(RepositoryConnection connection, URI uri) throws RepositoryException {
        final URI ldpContext = connection.getValueFactory().createURI(LDP.NAMESPACE);
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

    @Override
    public Date getLastModified(RepositoryConnection connection, String resource) throws RepositoryException {
        return getLastModified(connection, buildURI(resource));
    }

    @Override
    public Date getLastModified(RepositoryConnection connection, URI uri) throws RepositoryException {
        final URI ldpContext = connection.getValueFactory().createURI(LDP.NAMESPACE);
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
        final URI ldpContext = connection.getValueFactory().createURI(LDP.NAMESPACE);

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
        final URI ldpContext = connection.getValueFactory().createURI(LDP.NAMESPACE);
        final Literal now = connection.getValueFactory().createLiteral(new Date());

        // Delete corresponding containment and membership triples (Sec. 6.6.1)
        RepositoryResult<Statement> stmts = connection.getStatements(null, LDP.member, resource, false, ldpContext);
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
        // Delete the resource meta
        connection.remove(resource, null, null, ldpContext);

        // Delete the resource data
        connection.clear(resource);

        return true;
    }

}
