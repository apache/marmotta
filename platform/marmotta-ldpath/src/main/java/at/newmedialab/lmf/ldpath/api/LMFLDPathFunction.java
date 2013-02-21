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
package at.newmedialab.lmf.ldpath.api;

import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.openrdf.model.Value;

/**
 * Superclass of all LMF LDPath functions, used for auto-registry of functions provided by modules using
 * injection.
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class LMFLDPathFunction extends SelectorFunction<Value> {

    @Override
    public abstract String getLocalName();

}
