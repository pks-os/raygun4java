package com.mindscapehq.raygun4java.webprovider;

import com.mindscapehq.raygun4java.core.AbstractRaygunSendEventChainFactory;
import com.mindscapehq.raygun4java.core.IRaygunOnAfterSend;
import com.mindscapehq.raygun4java.core.IRaygunOnBeforeSend;
import com.mindscapehq.raygun4java.core.IRaygunClientFactory;
import com.mindscapehq.raygun4java.core.IRaygunMessageBuilderFactory;
import com.mindscapehq.raygun4java.core.IRaygunOnFailedSend;
import com.mindscapehq.raygun4java.core.IRaygunSendEventFactory;
import com.mindscapehq.raygun4java.core.RaygunClient;
import com.mindscapehq.raygun4java.core.RaygunOnAfterSendChainFactory;
import com.mindscapehq.raygun4java.core.RaygunOnBeforeSendChainFactory;
import com.mindscapehq.raygun4java.core.RaygunOnFailedSendChainFactory;
import com.mindscapehq.raygun4java.core.handlers.offlinesupport.RaygunOnFailedSendOfflineStorageHandler;
import com.mindscapehq.raygun4java.core.handlers.requestfilters.RaygunDuplicateErrorFilterFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * An out-of-the-box RaygunClient factory that can extract the application version from the .WAR file /META-INF/MANIFEST.MF
 *
 * For an out-of-the-box implementation, an instance of this should be used to initialize the static accessor:
 * RaygunClient.Initialize(new RaygunServletClientFactory(apiKey, servletContext).withBeforeSend(myBeforeSendHandler));
 */
public class RaygunServletClientFactory implements IRaygunServletClientFactory {
    private String apiKey;
    private AbstractRaygunSendEventChainFactory<IRaygunOnBeforeSend> onBeforeSendChainFactory;
    private AbstractRaygunSendEventChainFactory<IRaygunOnAfterSend> onAfterSendChainFactory;
    private AbstractRaygunSendEventChainFactory<IRaygunOnFailedSend> onFailedSendChainFactory;
    protected List<String> tags;
    protected Map data;
    private RaygunClient client;
    private String version;
    private boolean shouldProcessBreadcrumbLocations = false;
    private IRaygunMessageBuilderFactory raygunMessageBuilderFactory = new RaygunServletMessageBuilderFactory();

    public RaygunServletClientFactory(String apiKey) {
        this(apiKey, null);
    }

    public RaygunServletClientFactory(String apiKey, ServletContext context) {
        this.apiKey = apiKey;
        this.version = new RaygunServletMessageBuilder().getVersion(context);

        tags = new ArrayList<String>();
        data = new WeakHashMap();

        RaygunDuplicateErrorFilterFactory duplicateErrorRecordFilterFactory = new RaygunDuplicateErrorFilterFactory();

        onBeforeSendChainFactory = new RaygunOnBeforeSendChainFactory()
                .afterAll(duplicateErrorRecordFilterFactory);

        onAfterSendChainFactory = new RaygunOnAfterSendChainFactory()
                .withFilterFactory(duplicateErrorRecordFilterFactory);

        onFailedSendChainFactory = new RaygunOnFailedSendChainFactory();
    }

    public IRaygunClientFactory withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public IRaygunServletClientFactory withVersion(String version) {
        this.version = version;
        return this;
    }

    public IRaygunServletClientFactory withVersionFrom(ServletContext context) {
        this.version = new RaygunServletMessageBuilder().getVersion(context);
        return this;
    }

    public IRaygunServletClientFactory withVersionFrom(Class versionFromClass) {
        version = raygunMessageBuilderFactory.newMessageBuilder().setVersionFrom(versionFromClass).build().getDetails().getVersion();
        return this;
    }

    public IRaygunServletClientFactory withMessageBuilder(IRaygunMessageBuilderFactory messageBuilderFactory) {
        this.raygunMessageBuilderFactory = messageBuilderFactory;
        return this;
    }

    public IRaygunServletClientFactory withOfflineStorage() {
        return withOfflineStorage("");
    }

    public IRaygunServletClientFactory withOfflineStorage(String storageDir) {
        RaygunOnFailedSendOfflineStorageHandler sendOfflineStorageHandler = new RaygunOnFailedSendOfflineStorageHandler(storageDir, apiKey);

        onBeforeSendChainFactory.withFilterFactory(sendOfflineStorageHandler);
        onFailedSendChainFactory.withFilterFactory(sendOfflineStorageHandler);

        return this;
    }

    /**
     * Add a RaygunOnBeforeSend handler factory
     *
     * factory.withBeforeSend(myRaygunOnBeforeSendFactory)
     *
     * @param onBeforeSend
     * @return factory
     */
    public IRaygunServletClientFactory withBeforeSend(IRaygunSendEventFactory<IRaygunOnBeforeSend> onBeforeSend) {
        this.onBeforeSendChainFactory.withFilterFactory(onBeforeSend);
        return this;
    }

    /**
     * Add a RaygunOnAfterSend handler factory
     *
     * factory.withAfterSend(myRaygunOnAfterSendFactory)
     *
     * @param onAfterSend
     * @return factory
     */
    public IRaygunServletClientFactory withAfterSend(IRaygunSendEventFactory<IRaygunOnAfterSend> onAfterSend) {
        this.onAfterSendChainFactory.withFilterFactory(onAfterSend);
        return this;
    }

    /**
     * Add a RaygunOnFailedSend handler
     *
     * factory.withFailedSend(myRaygunOnFailedSendFactory)
     *
     * @param onFailedSend
     * @return factory
     */
    public IRaygunServletClientFactory withFailedSend(IRaygunSendEventFactory<IRaygunOnFailedSend> onFailedSend) {
        this.onFailedSendChainFactory.withFilterFactory(onFailedSend);
        return this;
    }

    /**
     * BEWARE: enabling could seriously degrade performance of your application
     */
    public IRaygunClientFactory withBreadcrumbLocations() {
        this.shouldProcessBreadcrumbLocations = true;
        return this;
    }

    public RaygunClient newClient() {
        throw new IllegalStateException("newClient() is not valid for RaygunServletFactory; use newClient(request)");
    }

    public IRaygunClientFactory withTag(String tag) {
        tags.add(tag);
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<?, ?> getData() {
        return data;
    }

    public void setData(Map<?, ?> data) {
        this.data = data;
    }

    public IRaygunClientFactory withData(Object key, Object value) {
        data.put(key, value);
        return this;
    }

    /**
     * @return a new RaygunClient
     */
    public RaygunServletClient newClient(HttpServletRequest request) {
        RaygunServletClient client = new RaygunServletClient(apiKey, request);
        client.setOnBeforeSend(onBeforeSendChainFactory.create());
        client.setOnAfterSend(onAfterSendChainFactory.create());
        client.setVersion(version);
        client.shouldProcessBreadcrumbLocation(shouldProcessBreadcrumbLocations);
        return client;
    }

    public AbstractRaygunSendEventChainFactory getRaygunOnBeforeSendChainFactory() {
        return onBeforeSendChainFactory;
    }

    public AbstractRaygunSendEventChainFactory getRaygunOnAfterSendChainFactory() {
        return onAfterSendChainFactory;
    }

    public AbstractRaygunSendEventChainFactory getRaygunOnFailedSendChainFactory() {
        return onFailedSendChainFactory;
    }
}
