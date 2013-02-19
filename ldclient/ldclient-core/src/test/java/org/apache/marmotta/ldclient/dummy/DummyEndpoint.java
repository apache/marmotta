package org.apache.marmotta.ldclient.dummy;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;

public class DummyEndpoint extends Endpoint {
	
	public DummyEndpoint() {
		super("Dummy", "Dummy", "^http://127.1.2.3", null, 86400l);
		setPriority(PRIORITY_HIGH);
	}

}
