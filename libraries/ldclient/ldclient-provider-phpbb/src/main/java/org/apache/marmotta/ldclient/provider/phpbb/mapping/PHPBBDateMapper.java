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
package org.apache.marmotta.ldclient.provider.phpbb.mapping;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.apache.marmotta.commons.util.DateUtils;
import org.apache.marmotta.ldclient.provider.html.mapping.CssTextLiteralMapper;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class PHPBBDateMapper extends CssTextLiteralMapper {

    public PHPBBDateMapper(String xpath) {
        super(xpath);
    }



    /**
     * Take the selected value, process it according to the mapping definition, and create Sesame Values using the
     * factory passed as argument.
     *
     * @param resourceUri
     * @param el
     * @param factory
     * @return
     */
    @Override
    public List<Value> map(String resourceUri, Element el, ValueFactory factory) {
        String selectedValue = el.text();

        while(!Character.isDigit(selectedValue.charAt(0)))  {
            selectedValue = selectedValue.substring(1,selectedValue.length());
        }
        while (!Character.isDigit(selectedValue.charAt(selectedValue.length()-1))) {
            selectedValue = selectedValue.substring(0,selectedValue.length()-1);
        }

        Date date = DateUtils.parseDate(selectedValue);

        return Collections.singletonList((Value)factory.createLiteral(DateUtils.ISO8601FORMAT.format(date),factory.createURI(Namespaces.NS_XSD + "dateTime")));
    }
}
