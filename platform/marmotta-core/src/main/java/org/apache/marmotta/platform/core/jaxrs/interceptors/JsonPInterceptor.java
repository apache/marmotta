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
package org.apache.marmotta.platform.core.jaxrs.interceptors;

import org.jboss.resteasy.plugins.providers.jackson.Jackson2JsonpInterceptor;

import javax.enterprise.context.Dependent;
import javax.ws.rs.ext.Provider;

/**
 * <p>
 *  JSONP is an alternative to normal AJAX requests. Instead of using a XMLHttpRequest a script tag is added to the DOM.
 *  The browser will call the corresponding URL and download the JavaScript. The server creates a response which looks like a
 *  method call. The parameter is the body of the request. The name of the method to call is normally passed as query parameter.
 *  The method has to be present in the current JavaScript environment.
 * </p>
 * <p>
 *  Jackson JSON processor can produce such an response. This interceptor checks if the media type is a JavaScript one if there is a query
 *  parameter with the method name. The default name of this query parameter is "callback". So this interceptor is compatible with
 *  <a href="http://api.jquery.com/jQuery.ajax/">jQuery</a>.
 * </p>

 * @see org.jboss.resteasy.plugins.providers.jackson.Jackson2JsonpInterceptor
 * @see org.apache.marmotta.platform.core.jaxrs.interceptors.CDIInterceptor
 */
@Provider
@Dependent
public class JsonPInterceptor extends Jackson2JsonpInterceptor implements CDIInterceptor {

}
