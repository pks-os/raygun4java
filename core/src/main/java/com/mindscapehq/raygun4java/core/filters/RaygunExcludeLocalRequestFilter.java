package com.mindscapehq.raygun4java.core.filters;

import com.mindscapehq.raygun4java.core.messages.RaygunRequestMessage;

/**
 * Excludes requests that come from host names starting with "localhost"
 */
public class RaygunExcludeLocalRequestFilter extends RaygunExcludeRequestFilter {
    private static final String LOCALHOST = "localhost";
    public RaygunExcludeLocalRequestFilter() {
        super(new Filter() {
            public boolean shouldFilterOut(RaygunRequestMessage requestMessage) {
                return requestMessage.getHostName().toLowerCase().startsWith(LOCALHOST);
            }
        });
    }
}