package com.phonedeck.gcm4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.ListenableFuture;

public abstract class AbstractDefaultGcm implements Gcm {

    private static final String AUTH_KEY = "key=";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDefaultGcm.class);

    private final ObjectMapper objectMapper;

    private final URL gcmUrl;

    private final String authorizationValue;

    private final ConnectionFactory connectionFactory;

    private final List<GcmFilter> filters;

    public AbstractDefaultGcm(GcmConfig gcmConfig) {
        this.objectMapper = createObjectMapper();
        this.gcmUrl = getConfigEndpoint(gcmConfig.getEndpoint());
        this.authorizationValue = buildAuthString(gcmConfig.getKey());
        this.connectionFactory = gcmConfig.getConnectionFactory() != null ? gcmConfig.getConnectionFactory() : new DefaultConnectionFactory();
        this.filters = gcmConfig.getFilters() != null ? ImmutableList.copyOf(gcmConfig.getFilters()) : ImmutableList.<GcmFilter>of();
    }

    private static String buildAuthString(String key) {
        return AUTH_KEY + key;
    }

    @Override
    public ListenableFuture<GcmResponse> send(GcmRequest request) {
        return new Chain().next(request);
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
        return objectMapper;
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

    private String getAuthorization(GcmRequest request) {
        String authToken = request.getKey();
        if (authToken == null) {
            if (authorizationValue == null) {
                throw new RuntimeException("Gcm client authorisation token can only be null when requests provide their own"
                        + " token");
            }
            return authorizationValue;
        }
        else {
            return buildAuthString(authToken);
        }
    }

    protected GcmResponse executeRequest(GcmRequest request) throws IOException {
        byte[] content = objectMapper.writeValueAsBytes(request);

        HttpURLConnection conn = connectionFactory.open(gcmUrl);
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Authorization", getAuthorization(request));
        conn.addRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setFixedLengthStreamingMode(content.length);

        try (OutputStream os = conn.getOutputStream()) {
            IOUtils.write(content, os);
        }
        catch (Exception ex)
        {
            throw new GcmNetworkException("Error sending HTTP request to GCM", ex);
        }

        GcmResponse response;
        byte[] rsp = null;
        try (InputStream is = conn.getInputStream()) {
            rsp = IOUtils.toByteArray(is);
            response = objectMapper.readValue(rsp, GcmResponse.class);

        } catch (IOException ex) {
            try (InputStream es = conn.getErrorStream()) {
                String str = es != null ? IOUtils.toString(es) : "No error details provided";

                int responseCode = conn.getResponseCode();
                if (responseCode < 500)
                {
                    throw new GcmNetworkException(conn.getResponseCode(), str.trim(), ex);
                }
                else
                {
                    Long retryAfter = checkForRetryInResponse(conn);
                    throw new GcmNetworkException(conn.getResponseCode(), str.trim(), retryAfter, ex);
                }
            }
        }

        response.setRequest(request);
        Long retryAfter = checkForRetryInResponse(conn);
        response.setRetryAfter(retryAfter);

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

    private Long checkForRetryInResponse(HttpURLConnection conn) {
        String retryAfterStr = conn.getHeaderField("Retry-After");
        if (retryAfterStr == null) {
            return null;
        }

        Long delta = Longs.tryParse(retryAfterStr);
        if (delta != null) {
            return delta * 1000;
        } else {
            Date date = DateUtils.parseDate(retryAfterStr);
            long delay = date.getTime() - System.currentTimeMillis();
            return delay < 0 ? 0 : delay;
        }
    }

    protected abstract ListenableFuture<GcmResponse> executeRequestFuture(final GcmRequest request);


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
                return executeRequestFuture(request);
            }
        }

        @Override
        public Gcm getGcm() {
            return AbstractDefaultGcm.this;
        }

    }


}
