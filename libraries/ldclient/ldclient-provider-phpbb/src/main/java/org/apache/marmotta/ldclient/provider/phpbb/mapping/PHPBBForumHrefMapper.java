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

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.marmotta.ldclient.provider.html.mapping.CssSelectorMapper;
import org.jsoup.nodes.Element;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PHPBBForumHrefMapper extends CssSelectorMapper {

    public PHPBBForumHrefMapper(String css) {
        super(css);
    }

    @Override
    public List<Value> map(String resourceUri, Element selectedValue, ValueFactory factory) {
        String baseUriSite = resourceUri.substring(0, resourceUri.lastIndexOf('/'));
        String baseUriTopic = baseUriSite + "/viewforum.php?";

        try {
            URI uri = new URI(selectedValue.absUrl("href"));
            Map<String, String> params = new HashMap<String, String>();
            for (NameValuePair p : URLEncodedUtils.parse(uri, "UTF-8")) {
                params.put(p.getName(), p.getValue());
            }

            return Collections.singletonList((Value) factory.createURI(baseUriTopic + "f=" + params.get("f")));
        } catch (URISyntaxException ex) {
            throw new RuntimeException("invalid syntax for URI", ex);
        }
    }

}
