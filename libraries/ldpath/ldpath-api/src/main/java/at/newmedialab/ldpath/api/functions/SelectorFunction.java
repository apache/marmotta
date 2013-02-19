/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.ldpath.api.functions;

import at.newmedialab.ldpath.api.backend.RDFBackend;

import java.util.Collection;

/**
 * Intermediate Interface for {@link NodeFunction}s used in the
 * FunctionSelector
 * 
 * @author Jakob Frank <jakob.frank@salzburgresearch.at>
 * 
 */
public abstract class SelectorFunction<Node> implements NodeFunction<Collection<Node>, Node> {

    @Override
    public String getPathExpression(RDFBackend<Node> backend) {
        return getLocalName();
    }

    protected abstract String getLocalName();
}
