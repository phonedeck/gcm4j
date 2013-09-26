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
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

public class DefaultGcm implements Gcm {

    private static final String AUTH_KEY = "key=";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGcm.class);

    protected final ObjectMapper objectMapper;

    private final URL gcmUrl;

    private final String authorizationValue;

    private final ConnectionFactory connectionFactory;

    private final ListeningExecutorService executor;

    private final List<GcmFilter> filters;

    public DefaultGcm(GcmConfig gcmConfig) {
        this.objectMapper = createObjectMapper();
        this.gcmUrl = getConfigEndpoint(gcmConfig.getEndpoint());
        this.authorizationValue = buildAuthString(gcmConfig.getKey());
        this.connectionFactory = gcmConfig.getConnectionFactory() != null ? gcmConfig.getConnectionFactory() : new DefaultConnectionFactory();
        this.executor = gcmConfig.getExecutor() != null ? gcmConfig.getExecutor() : MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        this.filters = gcmConfig.getFilters() != null ? ImmutableList.copyOf(gcmConfig.getFilters()) : ImmutableList.<GcmFilter>of();
    }

    private static String buildAuthString(String authToken)
    {
        return AUTH_KEY + authToken;
    }

    @Override
    public ListenableFuture<GcmResponse> send(GcmRequest request) {
        return new Chain().next(request);
    }

    @Override
    public GcmResponse sendBlocking(GcmRequest request) {
        try
        {
            return executeRequest(request);
        }
        catch (Exception ex)
        {
            throw new GcmException("An error occurred submitting the message", ex);
        }
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

    private String getAuthorization(GcmRequest request) {
        String authToken = request.getAuthorizationToken();
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
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        String found = null;
        for (String key : headerFields.keySet())
        {
            if (HttpHeaders.RETRY_AFTER.equalsIgnoreCase(key))
            {
                found = key;
                break;
            }
        }
        if (found == null) return null;
        String value = headerFields.get(found).get(0);
        Long delta = Longs.tryParse(value);

        if (delta != null)
        {
            return delta * 1000;
        }
        else
        {
            Date date = DateUtils.parseDate(value);
            long delay = date.getTime() - DateTimeUtils.currentTimeMillis();
            return delay < 0 ? 0 : delay;
        }
    }


    private ListenableFuture<GcmResponse> executeRequestAsync(final GcmRequest request) {
        final SettableFuture<GcmResponse> result = SettableFuture.create();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    result.set(executeRequest(request));
                } catch (GcmException ex) {
                    result.setException(ex);
                }
                  catch (Exception ex) {
                    result.setException(new GcmException("An error occurred when submitting the messsage", ex));
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
                return executeRequestAsync(request);
            }
        }

        @Override
        public Gcm getGcm() {
            return DefaultGcm.this;
        }

    }


}
