/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.ostrich.model;

import org.apache.marmotta.ostrich.model.proto.Model;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;

/**
 * An implementation of a Sesame Literal backed by a StringLiteral protocol buffer.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ProtoStringLiteral extends ProtoLiteralBase implements Literal {

    private Model.StringLiteral message;

    public ProtoStringLiteral(Model.StringLiteral message) {
        this.message = message;
    }

    public ProtoStringLiteral(String content) {
        this.message = Model.StringLiteral.newBuilder()
                .setContent(content)
                .build();
    }

    public ProtoStringLiteral(String content, String language) {
        this.message = Model.StringLiteral.newBuilder()
                .setContent(content)
                .setLanguage(language)
                .build();
    }

    public Model.StringLiteral getMessage() {
        return message;
    }

    /**
     * Gets the language tag for this literal, normalized to lower case.
     *
     * @return The language tag for this literal, or <tt>null</tt> if it
     * doesn't have one.
     */
    @Override
    public String getLanguage() {
        if ("".equals(message.getLanguage()) || message.getLanguage() == null) {
            return null;
        }
        return message.getLanguage();
    }

    /**
     * Gets the datatype for this literal.
     *
     * @return The datatype for this literal, or <tt>null</tt> if it doesn't
     * have one.
     */
    @Override
    public URI getDatatype() {
        return null;
    }


    /**
     * Gets the label of this literal.
     *
     * @return The literal's label.
     */
    @Override
    public String getLabel() {
        return message.getContent();
    }


    /**
     * Returns the String-value of a <tt>Value</tt> object. This returns either
     * a {@link Literal}'s label, a {@link URI}'s URI or a {@link BNode}'s ID.
     */
    @Override
    public String stringValue() {
        return message.getContent();
    }

}
