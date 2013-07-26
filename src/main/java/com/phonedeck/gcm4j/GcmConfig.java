package com.phonedeck.gcm4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Configuration of the DefaultGcmClient.
 */
public class GcmConfig {

    private URL endpoint;
    
    private String key;
    
    private ConnectionFactory connectionFactory;
    
    private ListeningExecutorService executor;
    
    private List<GcmFilter> filters;
    
    
    
    public GcmConfig withEndpoint(URL endpoint) {
        setEndpoint(endpoint);
        return this;
    }
    
    public GcmConfig withKey(String key) {
        setKey(key);
        return this;
    }
    
    public GcmConfig withConnectionFactory(ConnectionFactory connectionFactory) {
        setConnectionFactory(connectionFactory);
        return this;
    }
    
    public GcmConfig withExecutor(ListeningExecutorService executor) {
        setExecutor(executor);
        return this;
    }
    
    public GcmConfig withFilters(List<GcmFilter> filters) {
        setFilters(filters);
        return this;
    }
    
    public GcmConfig withFilter(GcmFilter filter) {
        if (getFilters() == null) {
            setFilters(new ArrayList<GcmFilter>());
        }
        getFilters().add(filter);
        return this;
    }
    
    
    
    
    public URL getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(URL endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
    
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
        
    public List<GcmFilter> getFilters() {
        return filters;
    }
    
    public void setFilters(List<GcmFilter> filters) {
        this.filters = filters;
    }
        
    public void setExecutor(ListeningExecutorService executor) {
        this.executor = executor;
    }
    
    public ListeningExecutorService getExecutor() {
        return executor;
    }
}
