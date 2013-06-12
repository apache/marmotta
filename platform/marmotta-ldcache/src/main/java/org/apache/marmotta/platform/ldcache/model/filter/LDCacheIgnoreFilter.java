package org.apache.marmotta.platform.ldcache.model.filter;

import org.apache.marmotta.commons.sesame.filter.resource.ResourceFilter;

/**
 * This filter can be used to define as CDI services additional filters that allow to ignore resources when
 * caching. The advantage of this approach over "blacklisting" is that filters defined in this way do not even
 * create a cache entry, they ignore the resource early on.
 *
 * The filter should return true in case the resource should be ignored.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface LDCacheIgnoreFilter extends ResourceFilter {
}
