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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public abstract class CssSelectorMapper implements JSoupMapper {

    protected final Selector selector;

    public CssSelectorMapper(final String cssSelector) {
        this(new Selector() {
            @Override
            public Elements select(Element node) {
                return node.select(cssSelector);
            }
        });
    }

    public CssSelectorMapper(Selector selector) {
        this.selector = selector;
    }

    @Override
    public Elements select(Element htmlDoc) {
        return selector.select(htmlDoc);
    }

    public interface Selector {
        public Elements select(Element node);
    }

}
