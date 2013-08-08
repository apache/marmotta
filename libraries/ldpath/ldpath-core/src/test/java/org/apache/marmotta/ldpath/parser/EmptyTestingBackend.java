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
package org.apache.marmotta.ldpath.parser;


import java.net.URI;
import java.util.Locale;

import org.apache.marmotta.ldpath.model.backend.AbstractBackend;


public class EmptyTestingBackend extends
		AbstractBackend<String> {

	@Override
	public boolean isLiteral(String n) {
		return false;
	}

	@Override
	public boolean isURI(String n) {
		return false;
	}

	@Override
	public boolean isBlank(String n) {
		return false;
	}

	@Override
	public Locale getLiteralLanguage(String n) {
		return null;
	}

	@Override
	public URI getLiteralType(String n) {
		return null;
	}

	@Override
	public String createLiteral(String content) {
		return null;
	}

	@Override
	public String createLiteral(String content, Locale language,
			URI type) {
		return null;
	}

	@Override
	public String createURI(String uri) {
		return null;
	}

	@Override
	public String stringValue(String node) {
		return null;
	}
}