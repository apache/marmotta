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
package org.apache.marmotta.kiwi.model.caching;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.marmotta.commons.collections.EquivalenceHashSet;
import org.apache.marmotta.commons.sesame.model.StatementCommons;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.io.Serializable;
import java.util.*;

/**
 * A triple table that allows efficient in-memory operations over large collections of triples. This can be used as
 * a simplified version of a Sesame in-memory repository (MemStore) using typical collection methods.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class TripleTable<Triple extends Statement> implements Set<Triple>, Serializable {

	private static final long serialVersionUID = 1L;

	private Set<Triple> data;

    /**
     * A set that orders triples orderd in a way that the subject is the most significant, while the context is the
     * least significant property in the order. Can be used for efficient queries that involve either just a subject,
     * a subject and a property, a subject, property and object or a subject, property, object and context
     */
    private NavigableMap<IntArray,Triple> indexSPOC;

    /**
     * A set that orders triples orderd in a way that the context is the most significant, while the object is the
     * least significant property in the order. Can be used for efficient queries that involve either just a context,
     * a context and a subject, a context, subject, and property
     */
    private NavigableMap<IntArray,Triple> indexCSPO;

    public TripleTable() {
        data = new EquivalenceHashSet<Triple>(StatementCommons.quadrupleEquivalence());
        indexSPOC = new TreeMap<IntArray, Triple>();
        indexCSPO = new TreeMap<IntArray, Triple>();
    }


    public TripleTable(Collection<Triple> triples) {
        data = new EquivalenceHashSet<Triple>(StatementCommons.quadrupleEquivalence());
        indexSPOC = new TreeMap<IntArray, Triple>();
        indexCSPO = new TreeMap<IntArray, Triple>();
        addAll(triples);
    }


    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this set (its cardinality)
     */
    @Override
    public synchronized int size() {
        return data.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    @Override
    public synchronized boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this set (optional)
     * @throws NullPointerException if the specified element is null and this
     *         set does not permit null elements (optional)
     */
    @Override
    public synchronized boolean contains(Object o) {
        return data.contains(o);
    }

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set
     */
    @Override
    public Iterator<Triple> iterator() {
        return data.iterator();
    }

    /**
     * Returns an array containing all of the elements in this set.
     * If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the
     * elements in the same order.
     *
     * <p>The returned array will be "safe" in that no references to it
     * are maintained by this set.  (In other words, this method must
     * allocate a new array even if this set is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all the elements in this set
     */
    @Override
    public synchronized Object[] toArray() {
        return data.toArray();
    }

    /**
     * Returns an array containing all of the elements in this set; the
     * runtime type of the returned array is that of the specified array.
     * If the set fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this set.
     *
     * <p>If this set fits in the specified array with room to spare
     * (i.e., the array has more elements than this set), the element in
     * the array immediately following the end of the set is set to
     * <tt>null</tt>.  (This is useful in determining the length of this
     * set <i>only</i> if the caller knows that this set does not contain
     * any null elements.)
     *
     * <p>If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements
     * in the same order.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a set known to contain only strings.
     * The following code can be used to dump the set into a newly allocated
     * array of <tt>String</tt>:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this set are to be
     *        stored, if it is big enough; otherwise, a new array of the same
     *        runtime type is allocated for this purpose.
     * @return an array containing all the elements in this set
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in this
     *         set
     * @throws NullPointerException if the specified array is null
     */
    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return data.toArray(a);
    }

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation).  More formally, adds the specified element
     * <tt>e</tt> to this set if the set contains no element <tt>e2</tt>
     * such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.  In combination with the
     * restriction on constructors, this ensures that sets never contain
     * duplicate elements.
     *
     * <p>The stipulation above does not imply that sets must accept all
     * elements; sets may refuse to add any particular element, including
     * <tt>null</tt>, and throw an exception, as described in the
     * specification for {@link java.util.Collection#add Collection.add}.
     * Individual set implementations should clearly document any
     * restrictions on the elements that they may contain.
     *
     * @param triple element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     *         element
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this set
     * @throws NullPointerException if the specified element is null and this
     *         set does not permit null elements
     * @throws IllegalArgumentException if some property of the specified element
     *         prevents it from being added to this set
     */
    @Override
    public synchronized boolean add(Triple triple) {
        indexSPOC.put(IntArray.createSPOCKey(triple.getSubject(), triple.getPredicate(), triple.getObject(), triple.getContext()),triple);
        indexCSPO.put(IntArray.createCSPOKey(triple.getSubject(), triple.getPredicate(), triple.getObject(), triple.getContext()),triple);
        return data.add(triple);
    }

    /**
     * Removes the specified element from this set if it is present
     * (optional operation).  More formally, removes an element <tt>e</tt>
     * such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if
     * this set contains such an element.  Returns <tt>true</tt> if this set
     * contained the element (or equivalently, if this set changed as a
     * result of the call).  (This set will not contain the element once the
     * call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if this set contained the specified element
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this set (optional)
     * @throws NullPointerException if the specified element is null and this
     *         set does not permit null elements (optional)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *         is not supported by this set
     */
    @Override
    public synchronized boolean remove(Object o) {
        if(o instanceof Statement) {
            Statement triple = (Statement)o;
            indexSPOC.remove(IntArray.createSPOCKey(triple.getSubject(), triple.getPredicate(), triple.getObject(), triple.getContext()));
            indexCSPO.remove(IntArray.createCSPOKey(triple.getSubject(), triple.getPredicate(), triple.getObject(), triple.getContext()));
        }
        return data.remove(o);
    }

    /**
     * Returns <tt>true</tt> if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
     *
     * @param  c collection to be checked for containment in this set
     * @return <tt>true</tt> if this set contains all of the elements of the
     * 	       specified collection
     * @throws ClassCastException if the types of one or more elements
     *         in the specified collection are incompatible with this
     *         set (optional)
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this set does not permit null
     *         elements (optional), or if the specified collection is null
     * @see    #contains(Object)
     */
    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation).  If the specified
     * collection is also a set, the <tt>addAll</tt> operation effectively
     * modifies this set so that its value is the <i>union</i> of the two
     * sets.  The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.
     *
     * @param  c collection containing elements to be added to this set
     * @return <tt>true</tt> if this set changed as a result of the call
     *
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of an element of the
     *         specified collection prevents it from being added to this set
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this set does not permit null
     *         elements, or if the specified collection is null
     * @throws IllegalArgumentException if some property of an element of the
     *         specified collection prevents it from being added to this set
     * @see #add(Object)
     */
    @Override
    public synchronized boolean addAll(Collection<? extends Triple> c) {
        boolean modified = false;
        for(Triple t : c) {
            modified = add(t) || modified;
        }
        return modified;
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this set all of its elements that are not contained in the
     * specified collection.  If the specified collection is also a set, this
     * operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     *
     * @param  c collection containing elements to be retained in this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of an element of this set
     *         is incompatible with the specified collection (optional)
     * @throws NullPointerException if this set contains a null element and the
     *         specified collection does not permit null elements (optional),
     *         or if the specified collection is null
     * @see #remove(Object)
     */
    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        Iterator<Map.Entry<IntArray,Triple>> it = indexSPOC.entrySet().iterator();
        while(it.hasNext()) {
            if(!c.contains(it.next().getValue())) {
                it.remove();
            }
        }
        Iterator<Map.Entry<IntArray,Triple>> it2 = indexCSPO.entrySet().iterator();
        while(it2.hasNext()) {
            if(!c.contains(it2.next().getValue())) {
                it2.remove();
            }
        }
        return data.retainAll(c);
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation).  If the specified
     * collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>asymmetric set difference</i> of
     * the two sets.
     *
     * @param  c collection containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of an element of this set
     *         is incompatible with the specified collection (optional)
     * @throws NullPointerException if this set contains a null element and the
     *         specified collection does not permit null elements (optional),
     *         or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for(Object o : c) {
            modified = remove(o) || modified;
        }
        return modified;
    }

    /**
     * Removes all of the elements from this set (optional operation).
     * The set will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method
     *         is not supported by this set
     */
    @Override
    public synchronized void clear() {
        data.clear();
        indexSPOC.clear();
        indexCSPO.clear();
    }

    /**
     * Return a subset of the triples matching the filter criteria. Arguments with null value are treated as wildcards.
     *
     *
     * @param subject
     * @param property
     * @param object
     * @param context
     * @param wildcardContext
     * @return
     */
    public synchronized Collection<Triple> listTriples(final Resource subject, final URI property, final Value object, final Resource context, boolean wildcardContext) {
        // in special cases we can make use of the index
        if(subject != null && property != null && object != null && context != null) {
            IntArray key = IntArray.createSPOCKey(subject, property, object, context);
            Triple result = indexSPOC.get(key);
            if(result != null) {
                return Collections.singleton(result);
            } else {
                return Collections.emptyList();
            }
        } else if(wildcardContext &&
                (  (subject != null && property != null && object != null)
                 || (subject != null && property != null)
                 || subject != null)) {
            IntArray fromKey = IntArray.createSPOCKey(subject, property, object, context);
            IntArray toKey   = IntArray.createSPOCMaxKey(subject, property, object, context);

            return indexSPOC.subMap(fromKey,true,toKey,true).values();
        } else if(  (context != null && subject != null && property != null)
                 || (context != null && subject != null)
                 || context != null) {
            IntArray fromKey = IntArray.createCSPOKey(subject, property, object, context);
            IntArray toKey   = IntArray.createCSPOMaxKey(subject, property, object, context);

            return indexCSPO.subMap(fromKey,true,toKey,true).values();
        } else {
            // in all other cases we need to iterate and filter :-(
            Predicate<Statement> p = new Predicate<Statement>() {
                @Override
                public boolean apply(Statement triple) {
                    if(subject != null && !triple.getSubject().equals(subject)) {
                        return false;
                    }
                    if(property != null && !triple.getPredicate().equals(property)) {
                        return false;
                    }
                    if(object != null && !triple.getObject().equals(object)) {
                        return false;
                    }
                    if(context != null && !triple.getContext().equals(context)) {
                        return false;
                    }

                    return true;
                }
            };

            return Collections2.filter(data, p);
        }
    }

    public synchronized Collection<Resource> listContextIDs() {
        Set<Resource> result = new HashSet<>();
        for(Triple t : data) {
            result.add(t.getContext());
        }
        return result;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

		@SuppressWarnings("rawtypes")
		TripleTable that = (TripleTable) o;

        if (!data.equals(that.data)) return false;

        return true;
    }

    @Override
    public synchronized int hashCode() {
        return data.hashCode();
    }


}
