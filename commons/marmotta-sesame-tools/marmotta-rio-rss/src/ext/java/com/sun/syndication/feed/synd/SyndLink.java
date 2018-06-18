/*
 * Copyright 2004 Sun Microsystems, Inc.
 * Copyright 2011 ROME Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.sun.syndication.feed.synd;

/**
 * Represents a link or enclosure associated with entry.
 * @author Dave Johnson
 */
public interface SyndLink {
    /**
     * Creates a deep 'bean' clone of the object.
     * <p>
     * @return a clone of the object.
     * @throws CloneNotSupportedException thrown if an element of the object cannot be cloned.
     *
     */
    Object clone() throws CloneNotSupportedException;

    /**
     * Indicates whether some other object is "equal to" this one as defined by the Object equals() method.
     * <p>
     * @param other he reference object with which to compare.
     * @return <b>true</b> if 'this' object is equal to the 'other' object.
     *
     */
    @Override
    boolean equals(Object other);

    /**
     * Returns a hashcode value for the object.
     * <p>
     * It follows the contract defined by the Object hashCode() method.
     * <p>
     * @return the hashcode of the bean object.
     *
     */
    @Override
    int hashCode();

    /**
     * Returns the String representation for the object.
     * <p>
     * @return String representation for the object.
     *
     */
    @Override
    String toString();

    /**
     * Returns the link rel.
     * <p>
     * @return the link rel, <b>null</b> if none.
     *
     */
    String getRel();

    /**
     * Sets the link rel.
     * <p>
     * @param rel the link rel,, <b>null</b> if none.
     *
     */
    void setRel(String rel);

    /**
     * Returns the link type.
     * <p>
     * @return the link type, <b>null</b> if none.
     *
     */
    String getType();

    /**
     * Sets the link type.
     * <p>
     * @param type the link type, <b>null</b> if none.
     *
     */
    void setType(String type);

    /**
     * Returns the link href.
     * <p>
     * @return the link href, <b>null</b> if none.
     *
     */
    String getHref();

    /**
     * Sets the link href.
     * <p>
     * @param href the link href, <b>null</b> if none.
     *
     */
    void setHref(String href);

    /**
     * Returns the link title.
     * <p>
     * @return the link title, <b>null</b> if none.
     *
     */
    String getTitle();

    /**
     * Sets the link title.
     * <p>
     * @param title the link title, <b>null</b> if none.
     *
     */
    void setTitle(String title);

    /**
     * Returns the hreflang
     * <p>
     * @return Returns the hreflang.
     */
    String getHreflang();

    /**
     * Set the hreflang
     * <p>
     * @param hreflang The hreflang to set.
     */
    void setHreflang(String hreflang);

    /**
     * Returns the length
     * <p>
     * @return Returns the length.
     */
    long getLength();

    /**
     * Set the length
     * <p>
     * @param length The length to set.
     */
    void setLength(long length);
}
