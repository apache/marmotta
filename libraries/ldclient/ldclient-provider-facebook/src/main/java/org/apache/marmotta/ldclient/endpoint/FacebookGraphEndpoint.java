package org.apache.marmotta.ldclient.endpoint;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.provider.facebook.FacebookGraphProvider;

/**
 * An endpoint that registers the FacebookGraphProvider for all facebook.com URLs.
 * <p/>
 * Author: Sebastian Schaffert (sschaffert@apache.org)
 */
public class FacebookGraphEndpoint extends Endpoint {

    public FacebookGraphEndpoint() {

        super("Facebook Graph API Provider", FacebookGraphProvider.PROVIDER_NAME, "^http://([^.]+)\\.facebook\\.com/.*", null, 86400L);
        setPriority(PRIORITY_HIGH);
        addContentType(new ContentType("application", "json"));

    }
}
