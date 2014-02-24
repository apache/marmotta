package org.apache.marmotta.platform.ldp.api;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *  LDP Service
 *
 *  @author Sergio Fernández
 */
public interface LdpService {

    boolean exists(String resource) throws RepositoryException;

    boolean exists(URI resource) throws RepositoryException;

    boolean addResource(InputStream stream, MediaType type, String container, String resource) throws RepositoryException, IOException, RDFParseException;

    boolean addResource(InputStream stream, MediaType type, URI container, URI resource) throws RepositoryException, IOException, RDFParseException;

    List<Statement> getStatements(String resource) throws RepositoryException;

    List<Statement> getStatements(URI resource) throws RepositoryException;

    void exportResource(OutputStream output, String resource, RDFFormat format) throws RepositoryException, RDFHandlerException;

    void exportResource(OutputStream output, URI resouce, RDFFormat format) throws RepositoryException, RDFHandlerException;

    EntityTag generateETag(String uri) throws RepositoryException;

    EntityTag generateETag(URI uri) throws RepositoryException;

    boolean deleteResource(URI resource) throws RepositoryException;

    boolean deleteResource(String resource) throws RepositoryException;

}
