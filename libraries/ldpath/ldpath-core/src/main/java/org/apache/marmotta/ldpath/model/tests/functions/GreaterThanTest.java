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
package org.apache.marmotta.ldpath.model.tests.functions;


public class GreaterThanTest<Node> extends BinaryNumericTest<Node> {


	@Override
	protected boolean test(Double left, Double right) {
		return left.compareTo(right) > 0;
	}

	@Override
    public String getDescription() {
		return "Check whether the first argument is greater than the second";
	}

	@Override
	public String getLocalName() {
		return "gt";
	}

}
