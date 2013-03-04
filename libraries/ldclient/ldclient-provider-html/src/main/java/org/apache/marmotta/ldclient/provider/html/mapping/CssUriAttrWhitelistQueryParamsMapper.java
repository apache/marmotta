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

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class CssUriAttrWhitelistQueryParamsMapper extends CssUriAttrMapper {

    protected List<String> queryParams;

    public CssUriAttrWhitelistQueryParamsMapper(String cssSelector, String attr, String... queryParamWhitelist) {
        super(cssSelector, attr);
        queryParams = Arrays.asList(queryParamWhitelist);
    }

    public CssUriAttrWhitelistQueryParamsMapper(Selector selector, String attr, String... queryParamWhitelist) {
        super(selector, attr);
        queryParams = Arrays.asList(queryParamWhitelist);
    }

    @Override
    protected String rewriteUrl(String url) {
        try {
            URI u = new URI(url);
            URIBuilder builder = new URIBuilder(u).removeQuery();
            for (NameValuePair p : URLEncodedUtils.parse(u, "UTF-8")) {
                if (queryParams.contains(p.getName())) {
                    builder.setParameter(p.getName(), p.getValue());
                }
            }
            final String string = builder.build().toString();
            return super.rewriteUrl(string);
        } catch (URISyntaxException e) {
            return super.rewriteUrl(url);
        }
    }

}
