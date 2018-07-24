package com.mindscapehq.raygun4java.webprovider;

import com.mindscapehq.raygun4java.core.RaygunClient;
import com.mindscapehq.raygun4java.core.RaygunOnBeforeSend;
import com.mindscapehq.raygun4java.core.messages.RaygunMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * This client is the main sending object for servlet/JSP environments
 */
public class RaygunServletClient extends RaygunClient {
    private HttpServletRequest request;
    private HttpServletResponse response;

    public RaygunServletClient(String apiKey, HttpServletRequest request) {
        super(apiKey);
        this.request = request;
    }

    public int send(Throwable throwable) {
        return send(throwable, null, null);
    }

    public int send(Throwable throwable, List<?> tags) {
        return send(throwable, tags, null);
    }

    public int send(Throwable throwable, List<?> tags, Map<?, ?> userCustomData) {
        if (throwable != null) {
            return post(buildServletMessage(throwable, tags, userCustomData));
        }
        return -1;
    }

    public void sendAsync(Throwable throwable) {
        sendAsync(throwable, null, null);
    }

    public void sendAsync(Throwable throwable, List<?> tags) {
        sendAsync(throwable, tags, null);
    }

    public void sendAsync(Throwable throwable, List<?> tags, Map<?, ?> userCustomData) {
        if (throwable != null) {
            postAsync(buildServletMessage(throwable, tags, userCustomData));
        }
    }

    private void postAsync(final RaygunMessage message) {
        Runnable r = new Runnable() {
            public void run() {
                post(message);
            }
        };

        Executors.newSingleThreadExecutor().submit(r);
    }

    private RaygunMessage buildServletMessage(Throwable throwable, List<?> tags, Map<?, ?> userCustomData) {
        try {
            return RaygunServletMessageBuilder.New()
                    .setRequestDetails(request, response)
                    .setEnvironmentDetails()
                    .setMachineName(getMachineName())
                    .setExceptionDetails(throwable)
                    .setClientDetails()
                    .setVersion(string)
                    .setUser(user)
                    .setTags(tags)
                    .setUserCustomData(userCustomData)
                    .build();
        } catch (Exception e) {
            Logger.getLogger("Raygun4Java").warning("Failed to build RaygunMessage: " + e.getMessage());
        }
        return null;
    }

    String getVersion() {
        return string;
    }

    String getApiKey() {
        return apiKey;
    }

    public RaygunOnBeforeSend getOnBeforeSend() {
        return onBeforeSend;
    }

    /**
     * Adds the response information to the error.
     * Only set the response once the response has been committed.
     * @param response
     */
    public void setResponse(HttpServletResponse response) {
        if (response.isCommitted()) {
            this.response = response;
        }
    }
}
