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
package org.apache.marmotta.platform.sparql.services.sparqlio.sparqlhtml;

import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.helpers.RioSettingImpl;

/**
 * Contains settings relevant to SPARQL HTML Query Results Writers.
 * 
 * @author Peter Ansell
 */
public final class SPARQLHTMLSettings
{
    /**
     * The {@link TemplatingService} used by the SPARQL Results HTML Writer.
     * <p>
     * Defaults to null
     */
    public static final RioSetting<TemplatingService> TEMPLATING_SERVICE = new RioSettingImpl<TemplatingService>(
    "org.apache.marmotta.platform.sparql.services.sparqlio.sparqlhtml.templatingservice", "Templating service for SPARQL Results HTML Writer", null);
}
