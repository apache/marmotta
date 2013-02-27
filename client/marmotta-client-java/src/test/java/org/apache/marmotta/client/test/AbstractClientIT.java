package org.apache.marmotta.client.test;

import java.io.InputStream;

import org.junit.Assume;

public abstract class AbstractClientIT {

	protected static InputStream getTestData(String fName) {
		InputStream data = AbstractClientIT.class.getResourceAsStream(fName);
		Assume.assumeNotNull("could not load test-data '" + fName + "'", data);
		return data;
	}
	
}
