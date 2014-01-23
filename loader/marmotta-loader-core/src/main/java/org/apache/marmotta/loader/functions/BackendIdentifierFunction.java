package org.apache.marmotta.loader.functions;

import com.google.common.base.Function;
import org.apache.marmotta.loader.api.LoaderBackend;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class BackendIdentifierFunction implements Function<LoaderBackend, String> {
    @Override
    public String apply(LoaderBackend input) {
        return input.getIdentifier();
    }
}
