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
package org.apache.marmotta.platform.core.services.prefix;

import java.net.URISyntaxException;

import org.apache.marmotta.platform.core.api.prefix.PrefixProvider;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

/**
 * Prefix manager supporting prefix.cc as eager external provider
 * 
 * @author Sergio Fernández
 * 
 */
@Alternative
@ApplicationScoped
public class PrefixServiceCC extends PrefixServiceImpl {

    @Inject
    private PrefixProvider prefixcc;

    public PrefixServiceCC() {
        super();
    }

    @Override
    public String getNamespace(String prefix) {
        String namespace = super.getNamespace(prefix);
        if (StringUtils.isNotBlank(namespace))
            return namespace;
        else
            try {
                return namespaceLookup(prefix);
            } catch (Exception e) {
                return null;
            }
    }

    @Override
    public String getPrefix(String namespace) {
        String prefix = super.getPrefix(namespace);
        if (StringUtils.isNotBlank(prefix))
            return prefix;
        else
            try {
                return prefixLookup(namespace);
            } catch (Exception e) {
                return null;
            }
    }

    @Override
    public boolean containsPrefix(String prefix) {
        if (!super.containsPrefix(prefix)) {
            try {
                namespaceLookup(prefix);
            } catch (Exception e) {
                return false;
            }
        }
        return super.containsPrefix(prefix);
    }

    @Override
    public boolean containsNamespace(String namespace) {
        if (!super.containsNamespace(namespace)) {
            try {
                prefixLookup(namespace);
            } catch (Exception e) {
                return false;
            }
        }
        return super.containsNamespace(namespace);
    }

    private String prefixLookup(String namespace) throws IllegalArgumentException, URISyntaxException {
        String prefix = prefixcc.getPrefix(namespace);
        if (StringUtils.isNotBlank(prefix)) {
            add(prefix, namespace);
        }
        return prefix;
    }

    private String namespaceLookup(String prefix) throws IllegalArgumentException, URISyntaxException {
        String namespace = prefixcc.getNamespace(prefix);
        if (StringUtils.isNotBlank(namespace)) {
            add(prefix, namespace);
        }
        return namespace;
    }

}
