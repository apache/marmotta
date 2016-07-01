/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.marmotta.kiwi.model.rdf;

import java.util.Date;

/**
 * A RDF geometry literal (geo:wktLiteral)
 *
 * @author Xavier Sumba (xavier.sumba93@ucuenca.ec)
 * @author Sergio Fernández (wikier@apache.org)
 */
public class KiWiGeometryLiteral extends KiWiLiteral {

    private static final long serialVersionUID = 8761608427535540348L;

    protected String content;
    protected int SRID_URI;

    public KiWiGeometryLiteral() {
        super();
    }

    public KiWiGeometryLiteral(Date created) {
        super(created);
    }

    public KiWiGeometryLiteral(String content) {
        super(null, null);
        this.content = content;
    }

    public KiWiGeometryLiteral(String content, Date created) {
        super(null, null, created);
        this.content = content;
    }

    public KiWiGeometryLiteral(String content, KiWiUriResource type) {
        super(type);
        this.content = content;
    }

    public KiWiGeometryLiteral(String content, KiWiUriResource type, int srid) {
        super(type);
        this.content = content;
        this.SRID_URI = srid;
    }

    public KiWiGeometryLiteral(String content, KiWiUriResource type, Date created) {
        super(null, type, created);
        this.content = content;
    }

    /**
     * Return the content of the literal, using the parametrized Java type
     *
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the content of the literal to the content provided as parameter.
     *
     * @param srid
     */
    public void setSRID(int srid) {
        this.SRID_URI = srid;
    }

    /**
     * Return the SRID of the literal, using the parametrized Java type
     *
     * @return
     */
    public int getSRID() {
        return SRID_URI;
    }

    /**
     * Set the content of the literal to the content provided as parameter.
     *
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

}
