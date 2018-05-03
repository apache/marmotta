package com.sun.syndication.feed.synd;

import com.sun.syndication.feed.CopyFrom;

/**
 * @author Alejandro Abdelnur
 */
public interface SyndEnclosure extends Cloneable, CopyFrom<SyndEnclosure> {
    /**
     * Returns the enclosure URL.
     * <p>
     * @return the enclosure URL, <b>null</b> if none.
     *
     */
    String getUrl();

    /**
     * Sets the enclosure URL.
     * <p>
     * @param url the enclosure URL to set, <b>null</b> if none.
     *
     */
    void setUrl(String url);

    /**
     * Returns the enclosure length.
     * <p>
     * @return the enclosure length, <b>0</b> if none.
     *
     */
    long getLength();

    /**
     * Sets the enclosure length.
     * <p>
     * @param length the enclosure length to set, <b>0</b> if none.
     *
     */
    void setLength(long length);

    /**
     * Returns the enclosure type.
     * <p>
     * @return the enclosure type, <b>null</b> if none.
     *
     */
    String getType();

    /**
     * Sets the enclosure type.
     * <p>
     * @param type the enclosure type to set, <b>null</b> if none.
     *
     */
    void setType(String type);

}
