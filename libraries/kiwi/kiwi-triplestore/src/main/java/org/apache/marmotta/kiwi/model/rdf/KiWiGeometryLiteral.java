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
import java.util.Locale;

/**
 * A RDF geometry literal (of type geo:wktLiteral), possibly with language information.
 * <p/>
 * User: Xavier 
 */
public class KiWiGeometryLiteral extends KiWiLiteral {

	private static final long serialVersionUID = 8761608427535540348L;
	
	protected String content;


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


    public KiWiGeometryLiteral(String content, Locale language, KiWiUriResource type) {
        super(language, type);
        this.content = content;
    }

    public KiWiGeometryLiteral(String content, Locale language, KiWiUriResource type, Date created) {
        super(language, type, created);
        this.content = content;
    }


    /**
     * Return the content of the literal, using the parametrized Java type
     * @return
     */
    public String getContent() {
        return content;
    }


    /**
     * Set the content of the literal to the content provided as parameter.
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

}
