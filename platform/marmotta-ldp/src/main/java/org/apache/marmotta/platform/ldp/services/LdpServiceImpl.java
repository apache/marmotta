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

import info.aduna.iteration.UnionIteration;
import org.apache.marmotta.commons.vocabulary.DCTERMS;
import org.apache.marmotta.commons.vocabulary.LDP;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldp.api.LdpService;
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
 */
@ApplicationScoped
public class LdpServiceImpl implements LdpService {

    private static final Logger log = LoggerFactory.getLogger(LdpServiceImpl.class);

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    private URI buildURI(String resource) {
        return ValueFactoryImpl.getInstance().createURI(resource);
    }

    @Override
    public boolean exists(String resource) throws RepositoryException {
        return exists(buildURI(resource));
    }

    @Override
    public boolean exists(URI resource) throws RepositoryException {
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();
            final URI ldpContext = conn.getValueFactory().createURI(LDP.NAMESPACE);
            return conn.hasStatement(resource, RDF.TYPE, null, true, ldpContext);
        } finally {
            conn.commit();
            conn.close();
        }
    }

    @Override
    public List<Statement> getStatements(String resource) throws RepositoryException {
        return getStatements(buildURI(resource));
    }

    @Override
    public List<Statement> getStatements(URI resource) throws RepositoryException {
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            URI ldp = conn.getValueFactory().createURI(LDP.NAMESPACE);
            return conn.getStatements(resource, RDF.TYPE, null, false, ldp).asList(); //FIXME
        } finally {
            conn.close();
        }
    }

    @Override
    public void exportResource(OutputStream output, String resource, RDFFormat format) throws RepositoryException, RDFHandlerException {
        exportResource(output, buildURI(resource), format);
    }

    @Override
    public void exportResource(OutputStream output, URI resource, RDFFormat format) throws RepositoryException, RDFHandlerException {
        RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();
            URI ldpContext = conn.getValueFactory().createURI(LDP.NAMESPACE);
            // TODO: this should be a little more sophisticated...
            // TODO: non-membership triples flag / Prefer-header
            RDFWriter writer = Rio.createWriter(format, output);
            UnionIteration<Statement, RepositoryException> union = new UnionIteration<>(
                    conn.getStatements(null, null, null, false, resource),
                    conn.getStatements(resource, null, null, false, ldpContext)
            );
            try {
                LdpWebServiceUtils.exportIteration(writer, resource, union);
            } finally {
                union.close();
            }
            conn.commit();
        } finally {
            conn.close();
        }
    }

    @Override
    public boolean addResource(InputStream stream, MediaType type, String container, String resource) throws RepositoryException, IOException, RDFParseException {
        return addResource(stream, type, buildURI(container), buildURI(resource));
    }

    @Override
    public boolean addResource(InputStream stream, MediaType type, URI container, URI resource) throws RepositoryException, IOException, RDFParseException {
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            final URI ldpContext = conn.getValueFactory().createURI(LDP.NAMESPACE);
            conn.begin();

            log.trace("Checking possible name clash for new resource <{}>", resource.stringValue());
            if (conn.hasStatement(resource, null, null, false, ldpContext)) {
                int i = 0;
                final String base = resource.stringValue();
                do {
                    final String candidate = base + "-" + (++i);
                    log.trace("<{}> already exists, trying <{}>", resource.stringValue(), candidate);
                    resource = conn.getValueFactory().createURI(candidate);
                } while (conn.hasStatement(resource, null, null, false, ldpContext));
                log.debug("resolved name clash, new resource will be <{}>", resource.stringValue());
            } else {
                log.debug("no name clash for <{}>", resource);
            }

            log.debug("POST to <{}> will create new LDP-R <{}>", container, resource);

            // Add container triples (Sec. 6.4.3)
            // container and meta triples!

            Literal now = conn.getValueFactory().createLiteral(new Date());

            conn.add(container, RDF.TYPE, LDP.BasicContainer, ldpContext);
            conn.add(container, LDP.contains, resource, ldpContext);
            conn.remove(container, DCTERMS.modified, null, ldpContext);
            conn.add(container, DCTERMS.modified, now, ldpContext);

            conn.add(resource, RDF.TYPE, LDP.Resource, ldpContext);
            conn.add(resource, DCTERMS.created, now, ldpContext);
            conn.add(resource, DCTERMS.modified, now, ldpContext);

            // LDP-BC for now!
            conn.commit();

            // Add the bodyContent
            log.trace("Content ({}) for new resource <{}>", type, resource);
            final RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(type.toString(), RDFFormat.TURTLE);
            if (rdfFormat == null) {
                log.debug("POST creates new LDP-BR with type {}", type);
                log.warn("LDP-BR not (yet) supported!");
                throw new UnsupportedRDFormatException("No available parser for " + type.toString());
            } else {
                log.debug("POST creates new LDP-RR, data provided as {}", rdfFormat.getName());
                conn.begin();

                // FIXME: We are (are we?) allowed to filter out server-managed properties here
                conn.add(stream, resource.stringValue(), rdfFormat, resource);

                conn.commit();
                return true;
            }
        } catch (final Throwable t) {
            if (conn.isActive()) {
                conn.rollback();
            }
            throw t;
        } finally {
            conn.close();
        }
    }

    @Override
    public EntityTag generateETag(String resource) throws RepositoryException {
        return generateETag(buildURI(resource));
    }

    @Override
    public EntityTag generateETag(URI uri) throws RepositoryException {
        final RepositoryConnection conn = sesameService.getConnection();
        final URI ldpContext = conn.getValueFactory().createURI(LDP.NAMESPACE);
        final RepositoryResult<Statement> stmts = conn.getStatements(uri, DCTERMS.modified, null, true, ldpContext);
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
    public boolean deleteResource(String resource) throws RepositoryException {
        return deleteResource(buildURI(resource));
    }

    @Override
    public boolean deleteResource(URI resource) throws RepositoryException {
        final RepositoryConnection conn = sesameService.getConnection();
        try {
            conn.begin();

            log.warn("NOT CHECKING EXISTENCE OF <{}>", resource);

            URI ldpContext = conn.getValueFactory().createURI(LDP.NAMESPACE);
            Literal now = conn.getValueFactory().createLiteral(new Date());

            // Delete corresponding containment and membership triples (Sec. 6.6.1)
            RepositoryResult<Statement> stmts = conn.getStatements(null, LDP.member, resource, false, ldpContext);
            try {
                while (stmts.hasNext()) {
                    Statement st = stmts.next();
                    conn.remove(st.getSubject(), DCTERMS.modified, null);
                    conn.add(st.getSubject(), DCTERMS.modified, now);
                    conn.remove(st);
                }
            } finally {
                stmts.close();
            }
            // Delete the resource meta
            conn.remove(resource, null, null, ldpContext);

            // Delete the resource data
            conn.clear(resource);

            conn.commit();
            return true;
        } finally {
            conn.close();
        }
    }

}
