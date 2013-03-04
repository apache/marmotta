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

import org.apache.marmotta.ldclient.provider.html.mapping.CssSelectorMapper;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.util.Collections;
import java.util.List;

/**
 * Maps a PHPBB Post ID (starting with p...., e.g. p105946, to a PHPBB URI, e.g.
 * http://www.carving-ski.de/phpBB/viewtopic.php?p=105946
 * <p/>
 * Author: Sebastian Schaffert
 */
public class PHPBBPostIdMapper extends CssSelectorMapper {

    public PHPBBPostIdMapper(String css) {
        super(css);
    }

    /**
     * Take the selected value, process it according to the mapping definition, and create Sesame Values using the
     * factory passed as argument.
     *
     *
     * @param resourceUri
     * @param element
     * @param factory
     * @return
     */
    @Override
    public List<Value> map(String resourceUri, Element element, ValueFactory factory) {
        String selectedValue = element.attr("name");
        String baseUri;
        if(resourceUri.indexOf('?') >= 0) {
            baseUri = resourceUri.substring(0,resourceUri.indexOf('?'));
        } else {
            baseUri = resourceUri;
        }

        if(!selectedValue.startsWith("p")) throw new RuntimeException("invalid value for PHPBB Post ID; must start with p... but was "+selectedValue);
        else
            return Collections.singletonList(
                    (Value)factory.createURI(baseUri + "?p=" + selectedValue.substring(1))
                    );
    }
}
