/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.rometools.feed.module.opensearch;

import com.sun.syndication.feed.module.Module;

/** Provides access to A9 Open Search information.
 * @author Michael W. Nassif (enrouteinc@gmail.com)
 */
public interface OpenSearchModule extends Module, OpenSearchResponse{

	String URI = "http://a9.com/-/spec/opensearch/1.1/";
	
}
