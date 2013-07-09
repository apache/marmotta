package org.apache.marmotta.platform.core.jaxrs;

import javax.ws.rs.ext.ExceptionMapper;

/**
 * A marker-interface to allow CDI injection of ExceptionMappers implementing this interface by the ExceptionMapperService.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface CDIExceptionMapper<E extends Throwable> extends ExceptionMapper<E> {
}
