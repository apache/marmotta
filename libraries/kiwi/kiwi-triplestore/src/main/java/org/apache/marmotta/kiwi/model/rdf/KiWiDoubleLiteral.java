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
public class KiWiDoubleLiteral extends KiWiStringLiteral {

	private static final long serialVersionUID = 4928628421436572560L;
	
	/**
     * Content as double value (if appropriate); for efficient querying
     */
    protected Double doubleContent;


    public KiWiDoubleLiteral() {
        super();
    }

    protected KiWiDoubleLiteral(Date created) {
        super(created);
    }



    public KiWiDoubleLiteral(Double content, KiWiUriResource type) {
        super();
        setType(type);
        setDoubleContent(content);
     }

    public KiWiDoubleLiteral(Double content, KiWiUriResource type, Date created) {
        super(created);
        setType(type);
        setDoubleContent(content);
    }


    public Double getDoubleContent() {
        return doubleContent;
    }

    public void setDoubleContent(Double doubleContent) {
        this.doubleContent = doubleContent;
        this.content = fmt(doubleContent);
    }


    /**
     * Returns the <tt>float</tt> value of this literal.
     *
     * @return The <tt>float</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by a <tt>float</tt>.
     */
    @Override
    public float floatValue() {
        return getDoubleContent().floatValue();
    }

    /**
     * Returns the <tt>double</tt> value of this literal.
     *
     * @return The <tt>double</tt> value of the literal.
     * @throws NumberFormatException If the literal's label cannot be represented by a <tt>double</tt>.
     */
    @Override
    public double doubleValue() {
        return getDoubleContent().doubleValue();
    }


    private static String fmt(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }
}
