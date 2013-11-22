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
package org.apache.marmotta.kiwi.model.rdf;

import java.util.Date;

/**
 * Add file description here!
 * <p/>
 * User: sschaffe
 */
public class KiWiIntLiteral extends KiWiDoubleLiteral {

	private static final long serialVersionUID = -8812144398023098826L;

	/**
     * Content as integer value (if appropriate); for efficient querying
     */
    private Long intContent;



    public KiWiIntLiteral() {
        super();
    }


    public KiWiIntLiteral(Long content, KiWiUriResource type) {
        super();
        setIntContent(content);
        setType(type);
    }

    public KiWiIntLiteral(Long content, KiWiUriResource type, Date created) {
        super(created);
        setIntContent(content);
        setType(type);
    }


    public Long getIntContent() {
        return intContent;
    }


    public void setIntContent(Long intContent) {
        this.intContent = intContent;
        this.content    = intContent.toString();
        this.doubleContent = intContent.doubleValue();
    }


    /**
     * Returns the <tt>byte</tt> value of this literal.
     *
     * @return The <tt>byte value of the literal.
     * @throws NumberFormatException If the literal cannot be represented by a <tt>byte</tt>.
     */
    @Override
    public byte byteValue() {
        return getIntContent().byteValue();
    }

    /**
     * Returns the <tt>short</tt> value of this literal.
     *
     * @return The <tt>short</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by a <tt>short</tt>.
     */
    @Override
    public short shortValue() {
        return getIntContent().shortValue();
    }

    /**
     * Returns the <tt>int</tt> value of this literal.
     *
     * @return The <tt>int</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by a <tt>int</tt>.
     */
    @Override
    public int intValue() {
        return getIntContent().intValue();
    }



    /**
     * Returns the <tt>long</tt> value of this literal.
     *
     * @return The <tt>long</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by to a <tt>long</tt>.
     */
    @Override
    public long longValue() {
        return getIntContent().longValue();
    }
}
