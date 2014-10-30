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

/*
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
package org.apache.marmotta.platform.ldp.util;

import org.apache.marmotta.platform.ldp.api.LdpService;
import org.openrdf.repository.RepositoryConnection;

import java.util.Random;

/**
 * Random-Based URI Generator
 */
public class RandomUriGenerator extends AbstractResourceUriGenerator {

    private static final char[] symbols;

    static {
        StringBuilder sb = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch) {
            sb.append(ch);
        }
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            sb.append(ch);
        }
        for (char ch = 'A'; ch <= 'Z'; ++ch) {
            sb.append(ch);
        }
        symbols = sb.toString().toCharArray();
    }

    private final StringBuilder builder;
    private final Random random;


    public RandomUriGenerator(LdpService ldpService, String container, RepositoryConnection connection) {
        super(ldpService, container, connection);
        builder = new StringBuilder();
        random = new Random();
    }

    @Override
    protected String generateNextLocalName() {
        return builder.append(symbols[random.nextInt(symbols.length)]).toString();
    }
}
