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
package org.apache.marmotta.ldclient.test.helper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple wrapper to use in UnitTests, which handles typical
 * Exceptions when contacting remote resources.
 * 
 * All methods except {@link #retrieveResource(String)} are relayed to the
 * delegate. {@link #retrieveResource(String)} checks for common retrieval
 * errors such as "IOException: Connection refused" and deactivates any ongoing
 * Unit-Test using {@link Assume}.
 * 
 */
public class TestLDClient implements LDClientService {

	private static final Logger log = LoggerFactory.getLogger(TestLDClient.class);
	
	/**
	 * The default checks were shamelessly taken from Apache Stanbol.
	 * @see <a href="http://svn.apache.org/repos/asf/stanbol/trunk/enhancer/generic/test/src/main/java/org/apache/stanbol/enhancer/test/helper/RemoteServiceHelper.java">http://svn.apache.org/repos/asf/stanbol/trunk/enhancer/generic/test/src/main/java/org/apache/stanbol/enhancer/test/helper/RemoteServiceHelper.java</a>
	 */
	public static final List<Check> DEFAULT_CHECKS;
	static {
		LinkedList<Check> dc = new LinkedList<Check>();
		
		dc.add(new Check(UnknownHostException.class));
		dc.add(new Check(SocketTimeoutException.class));
		dc.add(new Check(IOException.class, "Connection refused"));
		dc.add(new Check(IOException.class, "Server returned HTTP response code: 50"));
		dc.add(new Check(ConnectException.class, "unreachable"));
		
		DEFAULT_CHECKS = Collections.unmodifiableList(dc);
	}
	
	private final LDClientService delegate;
	private final List<Check> extraCheck;
	private boolean defaultChecks = true;

	public TestLDClient(LDClientService delegate) {
		this(delegate, new LinkedList<Check>());
	}

	public TestLDClient(LDClientService delegate, LinkedList<Check> extraChecks) {
		this(delegate, extraChecks, true);
	}

	public TestLDClient(LDClientService delegate, LinkedList<Check> extraChecks, boolean defaultChecks) {
		this.delegate = delegate;
		this.extraCheck = extraChecks;
		this.defaultChecks = defaultChecks;
	}
	
    @Override
    public boolean ping(String resource) {
        return delegate.ping(resource);
    }

    @Override
	public ClientResponse retrieveResource(String resource)
			throws DataRetrievalException {
		try {
			return delegate.retrieveResource(resource);
		} catch (final DataRetrievalException e) {
			if (defaultChecks) {
				for (Check exCheck : DEFAULT_CHECKS) {
					exCheck.matches(e);
				}
			}
			for (Check exCheck : extraCheck) {
				exCheck.matches(e);
			}
			throw e;
		}
	}
	
    @Override
	public HttpClient getClient() {
		return delegate.getClient();
	}

    @Override
	public ClientConfiguration getClientConfiguration() {
		return delegate.getClientConfiguration();
	}

    @Override
	public Endpoint getEndpoint(String resource) {
		return delegate.getEndpoint(resource);
	}

    @Override
	public boolean hasEndpoint(String urlPattern) {
		return delegate.hasEndpoint(urlPattern);
	}

    @Override
	public void shutdown() {
		delegate.shutdown();
	}

    /**
     * Return a collection of all available data providers (i.e. registered through the service loader).
     *
     * @return
     */
    @Override
    public Set<DataProvider> getDataProviders() {
        return delegate.getDataProviders();
    }

    public void addCheck(Check check) {
		extraCheck.add(check);
	}
	
	public void addChecks(Collection<Check> checks) {
		extraCheck.addAll(checks);
	}

	public boolean isDefaultChecks() {
		return defaultChecks;
	}

	public void setDefaultChecks(boolean defaultChecks) {
		this.defaultChecks = defaultChecks;
	}

	public static class Check {
		private final Class<? extends Throwable> throwable;
		private final Pattern messagePattern;
		private boolean checkStack = true;
		private String infoMessage;

		public Check(Class<? extends Throwable> throwable) {
			this(throwable, (Pattern) null);
		}
		
		public Check(Class<? extends Throwable> throwable, String message) {
			this(throwable, Pattern.compile(Pattern.quote(message), Pattern.CASE_INSENSITIVE));
			this.infoMessage = String.format("Ignoring because of %s(\"%s\")", throwable.getSimpleName(), message);
		}
		
		public Check(Class<? extends Throwable> throwable, Pattern mPattern) {
			this.throwable = throwable;
			this.messagePattern = mPattern;
			this.infoMessage = String.format("Ignoring because of %s", throwable.getSimpleName());
		}
		
		public Check setCheckStack(boolean checkStack) {
			this.checkStack = checkStack;
			return this;
		}

		/**
		 * Check if the provided parameter matches this check. 
		 * If so, the current JUnit test is ignored ({@link Assume}) and the parameter exception is re-thrown. 
		 * @param t the {@link Throwable} to check.
		 */
		public <T extends Throwable> void matches(T t) throws T {
			matches(t, t);
		}
		
		private <T extends Throwable> void matches(T t, Throwable toCheck) throws T {
			if (toCheck == null) return;
			
			if (throwable.isAssignableFrom(toCheck.getClass())
					&& (messagePattern == null 
						|| (toCheck.getMessage() != null && messagePattern.matcher(toCheck.getMessage()).find()))) {
				log.info("Ignoring test because '{}' ({})", getMessage(), t.getMessage());
				Assume.assumeNoException(getMessage(), t);
				throw t;
			}
			
			if (checkStack)
				matches(t, toCheck.getCause());
		}
		
		public String getMessage() {
			return infoMessage;
		}
		
		public Check setMessage(String infoMessage) {
			this.infoMessage = infoMessage;
			return this;
		}
		
	}

}
