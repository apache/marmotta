package org.apache.marmotta.platform.ldp.webservices;

import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.ldp.api.LdpService;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Pre-process interceptor for LDP
 *
 * @author Sergio Fernández
 */
@PreMatching
@ApplicationScoped
public class LdpPreProcess implements ContainerRequestFilter {

    @Inject
    private LdpService ldpService;

    @Inject
    private SesameService sesameService;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        UriInfo uriInfo = context.getUriInfo();
        UriBuilder resourceUriBuilder = ldpService.getResourceUriBuilder(uriInfo);
        //TODO avoid the expensive operations once it's initialized
        try {
            final RepositoryConnection conn = sesameService.getConnection();
            try {
                conn.begin();
                URI root = conn.getValueFactory().createURI(resourceUriBuilder.build().toString());
                ldpService.init(conn, root);
                conn.commit();
            } finally {
                conn.close();
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
