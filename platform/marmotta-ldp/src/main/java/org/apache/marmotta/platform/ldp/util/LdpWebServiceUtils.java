package org.apache.marmotta.platform.ldp.util;

/**
 * Created by jakob on 2/18/14.
 */
public class LdpWebServiceUtils {

    /**
     * Urify the Slug: header value, i.e. replace all non-url chars with a single dash.
     *
     * @param slugHeaderValue
     * @return the slugHeaderValue "urified"
     */
    public static String urify(String slugHeaderValue) {
        return slugHeaderValue
                // Replace non-url chars with '-'
                .replaceAll("[^\\w]+", "-");
    }
}
