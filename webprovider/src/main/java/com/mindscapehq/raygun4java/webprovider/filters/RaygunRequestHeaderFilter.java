package com.mindscapehq.raygun4java.webprovider.filters;

import com.mindscapehq.raygun4java.webprovider.RaygunRequestMessage;

import java.util.Map;

/**
 * Given a list of header names, this will replace the header values with an optional replacement
 */
public class RaygunRequestHeaderFilter extends AbstractRaygunRequestMapFilter {

    public RaygunRequestHeaderFilter(String... keysToFilter) {
        super(keysToFilter);
    }

    public Map<String, String> getMapToFilter(RaygunRequestMessage requestMessage) {
        return requestMessage.getHeaders();
    }

    public RaygunRequestHeaderFilter replaceWith(String replacement) {
        setReplacement(replacement);
        return this;
    }
}
