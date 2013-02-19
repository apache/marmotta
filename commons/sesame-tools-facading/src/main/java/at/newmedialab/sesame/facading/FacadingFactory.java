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
package at.newmedialab.sesame.facading;

import at.newmedialab.sesame.facading.api.Facading;
import at.newmedialab.sesame.facading.impl.FacadingImpl;
import org.openrdf.repository.RepositoryConnection;

/**
 * A factory to simplify the creation of facading services.
 * <p/>
 * Author: Sebastian Schaffert
 */
public class FacadingFactory {

    /**
     * Create a facading for an existing repository connection.
     *
     * @param connection the repository connection to use for facading
     * @return a new facading service wrapping the given connection
     */
    public static Facading createFacading(RepositoryConnection connection) {
        return new FacadingImpl(connection);
    }

}
