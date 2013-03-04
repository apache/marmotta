/**
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
package org.apache.marmotta.kiwi.caching;

import com.google.common.collect.ImmutableList;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.marmotta.kiwi.model.caching.IntArray;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class KiWiQueryCache  {

    private static Logger log = LoggerFactory.getLogger(KiWiQueryCache.class);

    private Ehcache queryCache;


    public KiWiQueryCache(Ehcache queryCache) {
        this.queryCache = queryCache;
        log.debug("Building up new {}", KiWiQueryCache.class.getSimpleName());
    }


    /**
     * Look up a triple query in the query cache. Returns the result set if the query is found in the cache, returns
     * null if the query is not found.
     *
     * @param subject  the subject of the triples to list or null for wildcard
     * @param property the property of the triples to list or null for wildcard
     * @param object   the object of the triples to list or null for wildcard
     * @param context  the context/knowledge space of the triples to list or null for all spaces
     * @param inferred if true, inferred triples are included in the result; if false not
     * @return the result set if the query is found in the cache, returns null if the query is not found
     */
    @SuppressWarnings("unchecked")
	public List<KiWiTriple> listTriples(KiWiResource subject, KiWiUriResource property, KiWiNode object, KiWiUriResource context, boolean inferred) {
        IntArray key = createCacheKey(subject,property,object,context,inferred);
        if(queryCache.get(key) != null) return (List<KiWiTriple>)queryCache.get(key).getObjectValue();
        else
            return null;
    }


    /**
     * Cache the result of a triple query in the query cache.
     *
     * @param subject  the subject of the triples to list or null for wildcard
     * @param property the property of the triples to list or null for wildcard
     * @param object   the object of the triples to list or null for wildcard
     * @param context  the context/knowledge space of the triples to list or null for all spaces
     * @param inferred if true, inferred triples are included in the result; if false not
     * @param result   the result of the triple query to cache
     */
    public void cacheTriples(KiWiResource subject, KiWiUriResource property, KiWiNode object, KiWiResource context, boolean inferred, List<KiWiTriple> result) {

        // cache the query result
        IntArray key = createCacheKey(subject,property,object,context,inferred);
        queryCache.put(new Element(key,result));

        // cache the nodes of the triples and the triples themselves
        Set<KiWiNode> nodes = new HashSet<KiWiNode>();
        for(KiWiTriple triple : result) {
            Collections.addAll(nodes, new KiWiNode[]{triple.getSubject(), triple.getObject(), triple.getPredicate(), triple.getContext()});
            queryCache.put(new Element(createCacheKey(triple.getSubject(),triple.getPredicate(),triple.getObject(),triple.getContext(),triple.isInferred()), ImmutableList.of(triple)));
        }

        // special optimisation: when only the subject (and optionally context) is given, we also fill the caches for
        // all property values
        if(subject != null && property == null && object == null) {
            HashMap<KiWiUriResource,List<KiWiTriple>> properties = new HashMap<KiWiUriResource, List<KiWiTriple>>();
            for(KiWiTriple triple : result) {
                List<KiWiTriple> values = properties.get(triple.getPredicate());
                if(values == null) {
                    values = new LinkedList<KiWiTriple>();
                    properties.put(triple.getPredicate(),values);
                }
                values.add(triple);
            }
            for(Map.Entry<KiWiUriResource,List<KiWiTriple>> entry : properties.entrySet()) {
                IntArray key2 = createCacheKey(subject,entry.getKey(),null,context,inferred);
                queryCache.put(new Element(key2,entry.getValue()));
            }
        }


    }


    /**
     * Add the triple given as argument to the triple cache. This method ensures that all other cache entries that
     * are affected by the addition of this triple will be expired.
     *
     * @param triple
     */
    public void cacheTriple(KiWiTriple triple) {
        tripleUpdated(triple);
        cacheTriples(triple.getSubject(), triple.getPredicate(), triple.getObject(), triple.getContext(), triple.isInferred(), Collections.singletonList(triple));
    }


    /**
     * Remove the triple given as argument from the triple cache. This method ensures that all other cache entries
     * that are affected by the removal of this triple will be expired.
     * @param triple
     */
    public void removeTriple(KiWiTriple triple) {
        tripleUpdated(triple);
    }


    /**
     * Clear all contents of the query cache.
     */
    public void clearAll() {
        queryCache.removeAll();
    }


    /**
     * Notify the cache that the triple passed as argument has been updated and that all cache entries affected by
     * the triple update need to be cleared.
     *
     * @param triple
     */
    public void tripleUpdated(KiWiTriple triple) {
        queryCache.remove(createCacheKey(null,null,null,null,false));
        queryCache.remove(createCacheKey(null,null,null,null,true));

        // remove all possible combinations of this triple as they may appear in the cache
        queryCache.remove(createCacheKey(triple.getSubject(),null,null,null,false));
        queryCache.remove(createCacheKey(triple.getSubject(),null,null,null,true));
        queryCache.remove(createCacheKey(null,triple.getPredicate(),null,null,false));
        queryCache.remove(createCacheKey(null,triple.getPredicate(),null,null,true));
        queryCache.remove(createCacheKey(null,null,triple.getObject(),null,false));
        queryCache.remove(createCacheKey(null,null,triple.getObject(),null,true));
        queryCache.remove(createCacheKey(null,null,null,triple.getContext(),false));
        queryCache.remove(createCacheKey(null,null,null,triple.getContext(),true));

        queryCache.remove(createCacheKey(triple.getSubject(),triple.getPredicate(),null,null,false));
        queryCache.remove(createCacheKey(triple.getSubject(),triple.getPredicate(),null,null,true));
        queryCache.remove(createCacheKey(triple.getSubject(),null,triple.getObject(),null,false));
        queryCache.remove(createCacheKey(triple.getSubject(),null,triple.getObject(),null,true));
        queryCache.remove(createCacheKey(triple.getSubject(),null,null,triple.getContext(),false));
        queryCache.remove(createCacheKey(triple.getSubject(),null,null,triple.getContext(),true));
        queryCache.remove(createCacheKey(null,triple.getPredicate(),triple.getObject(),null,false));
        queryCache.remove(createCacheKey(null,triple.getPredicate(),triple.getObject(),null,true));
        queryCache.remove(createCacheKey(null,triple.getPredicate(),null,triple.getContext(),false));
        queryCache.remove(createCacheKey(null,triple.getPredicate(),null,triple.getContext(),true));
        queryCache.remove(createCacheKey(null,null,triple.getObject(),triple.getContext(),false));
        queryCache.remove(createCacheKey(null,null,triple.getObject(),triple.getContext(),true));


        queryCache.remove(createCacheKey(triple.getSubject(),triple.getPredicate(),triple.getObject(),null,false));
        queryCache.remove(createCacheKey(triple.getSubject(),triple.getPredicate(),triple.getObject(),null,true));
        queryCache.remove(createCacheKey(triple.getSubject(),triple.getPredicate(),null,triple.getContext(),false));
        queryCache.remove(createCacheKey(triple.getSubject(),triple.getPredicate(),null,triple.getContext(),true));
        queryCache.remove(createCacheKey(triple.getSubject(),null,triple.getObject(),triple.getContext(),false));
        queryCache.remove(createCacheKey(triple.getSubject(),null,triple.getObject(),triple.getContext(),true));
        queryCache.remove(createCacheKey(null,triple.getPredicate(),triple.getObject(),triple.getContext(),false));
        queryCache.remove(createCacheKey(null,triple.getPredicate(),triple.getObject(),triple.getContext(),true));

        queryCache.remove(createCacheKey(triple.getSubject(),triple.getPredicate(),triple.getObject(),triple.getContext(),false));
        queryCache.remove(createCacheKey(triple.getSubject(),triple.getPredicate(),triple.getObject(),triple.getContext(),true));
    }


    private static IntArray createCacheKey(KiWiResource subject, KiWiUriResource property, KiWiNode object, KiWiResource context, boolean inferred){

        // the cache key is generated by appending the bytes of the hashcodes of subject, property, object, context and inferred and
        // storing them as a BigInteger; generating the cache key should thus be very efficient

        int s = subject != null ? subject.hashCode() : Integer.MIN_VALUE;
        int p = property != null ? property.hashCode() : Integer.MIN_VALUE;
        int o = object != null ? object.hashCode() : Integer.MIN_VALUE;
        int c = context != null ? context.hashCode() : Integer.MIN_VALUE;

        IntBuffer bb = IntBuffer.allocate(5);
        bb.put(s);
        bb.put(p);
        bb.put(o);
        bb.put(c);
        bb.put( (byte) (inferred ? 1 : 0) );

        return new IntArray(bb.array());

    }
}
