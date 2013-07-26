package com.phonedeck.gcm4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

public class DefaultGcm implements Gcm {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGcm.class);

    private final ObjectMapper objectMapper;

    private final URL gcmUrl;

    private final String authorizationValue;

    private final ConnectionFactory connectionFactory;
    
    private final ListeningExecutorService executor;
    
    private final List<GcmFilter> filters;

    public DefaultGcm(GcmConfig gcmConfig) {
        this.objectMapper = createObjectMapper();
        this.gcmUrl = getConfigEndpoint(gcmConfig.getEndpoint());
        this.authorizationValue = "key=" + gcmConfig.getKey();
        this.connectionFactory = gcmConfig.getConnectionFactory() != null ? gcmConfig.getConnectionFactory() : new DefaultConnectionFactory();
        this.executor = gcmConfig.getExecutor() != null ? gcmConfig.getExecutor() : MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        this.filters = gcmConfig.getFilters() != null ? ImmutableList.copyOf(gcmConfig.getFilters()) : ImmutableList.<GcmFilter>of();
    }
    

    
    @Override
    public ListenableFuture<GcmResponse> send(GcmRequest request) {
        return new Chain().next(request);
    }
    
    
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    public List<GcmFilter> getFilters() {
        return filters;
    }
    
    public URL getGcmUrl() {
        return gcmUrl;
    }
    
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
    
    public ListeningExecutorService getExecutor() {
        return executor;
    }
    
    private static URL getConfigEndpoint(URL configEndpoint) {
        try {
            return configEndpoint != null ? configEndpoint : new URL("https://android.googleapis.com/gcm/send");
        } catch (MalformedURLException ex) {
            // should never happen
            throw new RuntimeException(ex);
        }
    }
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_DEFAULT);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }
    
    
    
    private GcmResponse resultSync(GcmRequest request) throws IOException {

                
        //final Map<String, Object> eventLog = new LinkedHashMap<String, Object>();

        byte[] content = objectMapper.writeValueAsBytes(request);

        HttpURLConnection conn = connectionFactory.open(gcmUrl);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Authorization", authorizationValue);
        conn.addRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setFixedLengthStreamingMode(content.length);
        
        try (OutputStream os = conn.getOutputStream()) {
            IOUtils.write(content, os);
        }

        GcmResponse response;
        byte[] rsp = null;
        try (InputStream is = conn.getInputStream()) {
            rsp = IOUtils.toByteArray(is);
            response = objectMapper.readValue(rsp, GcmResponse.class);
        } catch (IOException ex) {
            try (InputStream es = conn.getErrorStream()) {                
                String str = es != null ? IOUtils.toString(es) : "No error details provided";
                throw new GcmNetworkException(conn.getResponseCode(), str.trim(), ex);
            }
        }
        
        
        response.setRequest(request);

        //REQUESTS.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

        LOG.info("GCM: {}", response);

        //eventLog.put("multicastId", response.multicastId);

        Iterator<String> i1 = request.getRegistrationIds().iterator();
        Iterator<Result> i2 = response.getResults().iterator();
        while (i1.hasNext() && i2.hasNext()) {
            i2.next().setRequestedRegistrationId(i1.next());                
        }

        if (i1.hasNext()) {
            LOG.warn("Protocol error: Less results than requested registation IDs");
        }
        if (i2.hasNext()) {
            LOG.warn("Protocol error: More results than requested registation IDs");
        }

        return response;
    }
    
    
    
 
    private ListenableFuture<GcmResponse> resultAsync(final GcmRequest request) {
        final SettableFuture<GcmResponse> result = SettableFuture.create();                
        executor.submit(new Runnable() {
            @Override
            public void run() {                
                try {
                    result.set(resultSync(request));
                } catch (Exception ex) {
                    result.setException(ex);
                }
            }
        });
        return result;        
    }


    /**
     * Nested class to implement the {@link FilterChain}.
     */
    private final class Chain implements FilterChain {
        
        private final Iterator<GcmFilter> i = filters.iterator();
                
        @Override
        public ListenableFuture<GcmResponse> next(GcmRequest request) {
            if (i.hasNext()) {
                return i.next().filter(request, this);            
            } else {
                return resultAsync(request);
            }
        }
        
        @Override
        public Gcm getClient() {
            return DefaultGcm.this;
        }

    }
        
    
}
