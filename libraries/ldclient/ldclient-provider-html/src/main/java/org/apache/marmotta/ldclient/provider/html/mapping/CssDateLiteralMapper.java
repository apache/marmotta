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
package org.apache.marmotta.ldclient.provider.html.mapping;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.util.DateUtils;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CssDateLiteralMapper extends CssTextLiteralMapper {

    protected DateFormat format = null;

    public CssDateLiteralMapper(String cssSelector) {
        this(cssSelector, "dateTime");
    }

    public CssDateLiteralMapper(String cssSelector, String datatype) {
        super(cssSelector, datatype);
    }

    public CssDateLiteralMapper(Selector selector) {
        this(selector, "dateTime");
    }

    public CssDateLiteralMapper(Selector selector, String datatype) {
        super(selector, datatype);
    }

    public CssDateLiteralMapper setParseFormat(String format) {
        this.format = new SimpleDateFormat(format);
        return this;
    }

    public CssDateLiteralMapper setParseFormat(DateFormat format) {
        this.format = format;
        return this;
    }

    @Override
    public List<Value> map(String resourceUri, Element elem, ValueFactory factory) {
        final String value = cleanValue(elem.text());
        Date date = parseDate(value);

        final String dateString;
        if (date != null) {
            if ("dateTime".equals(datatype)) {
                dateString = DateUtils.ISO8601FORMAT.format(date);
            } else if ("date".equals(datatype)) {
                dateString = DateUtils.ISO8601FORMAT_DATE.format(date);
            } else if ("time".equals(datatype)) {
                dateString = DateUtils.ISO8601FORMAT_TIME.format(date);
            } else {
                dateString = value;
            }
            return Collections.singletonList((Value) factory.createLiteral(dateString, factory.createURI(Namespaces.NS_XSD + datatype)));
        } else
            return Collections.emptyList();
    }

    protected Date parseDate(final String value) {
        Date date;
        if (format != null) {
            try {
                date = format.parse(value);
            } catch (ParseException e) {
                date = null;
            }
        } else {
            date = DateUtils.parseDate(value);
        }
        return date;
    }

}
