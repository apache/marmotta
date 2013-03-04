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
package org.apache.marmotta.ldpath.parser;


import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.marmotta.ldpath.model.backend.AbstractBackend;


public class EmptyTestingBackend extends
		AbstractBackend<String> {
	@Override
	public boolean supportsThreading() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ThreadPoolExecutor getThreadPool() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLiteral(String n) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isURI(String n) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBlank(String n) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Locale getLiteralLanguage(String n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getLiteralType(String n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createLiteral(String content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createLiteral(String content, Locale language,
			URI type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createURI(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> listObjects(String subject,
			String property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> listSubjects(String property,
			String object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringValue(String node) {
		// TODO Auto-generated method stub
		return null;
	}
}