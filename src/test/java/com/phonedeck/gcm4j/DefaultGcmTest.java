package com.phonedeck.gcm4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.util.concurrent.MoreExecutors;

public class DefaultGcmTest {
    
    private ConnectionFactory resourceConnectionFactory(final String resourceName) {
        return new ConnectionFactory() {
            @Override
            public HttpURLConnection open(URL url) throws IOException {                
                HttpURLConnection result = Mockito.mock(HttpURLConnection.class);
                Mockito.when(result.getResponseCode()).thenReturn(200);
                Mockito.when(result.getOutputStream()).thenReturn(new NullOutputStream());
                Mockito.when(result.getInputStream()).thenReturn(getClass().getResourceAsStream(resourceName));
                return result;                
            }            
        };
    }
    
    private ConnectionFactory jsonErrorConnectionFactory(final int responseCode, final String errorMessage) {
        return new ConnectionFactory() {
            @Override
            public HttpURLConnection open(URL url) throws IOException {                
                HttpURLConnection result = Mockito.mock(HttpURLConnection.class);
                Mockito.when(result.getResponseCode()).thenReturn(responseCode);
                Mockito.when(result.getOutputStream()).thenReturn(new NullOutputStream());
                Mockito.when(result.getInputStream()).thenThrow(new IOException());
                Mockito.when(result.getErrorStream()).thenReturn(errorMessage != null ? new ByteArrayInputStream(errorMessage.getBytes()) : null);                
                return result;                
            }            
        };        
    }
    
    @Test
    public void unknownErrorResponse() throws IOException, InterruptedException, ExecutionException {
        GcmConfig gcmConfig = new GcmConfig();                
        gcmConfig.setConnectionFactory(resourceConnectionFactory("unknown-error-response.json"));
        gcmConfig.setKey("mykey");
        gcmConfig.setExecutor(MoreExecutors.sameThreadExecutor());
        
        Gcm gcm = new DefaultGcm(gcmConfig);
                
        GcmRequest request = new GcmRequest()
            .withRegistrationId("hello-world");
                
        GcmResponse response = gcm.send(request).get();
        Assert.assertEquals(response.getResults().get(0).getError(), ResultError.UNSUPPORTED_ERROR_CODE);        
    }
    
    @Test
    public void good1() throws IOException, InterruptedException, ExecutionException {
        GcmConfig gcmConfig = new GcmConfig();                
        gcmConfig.setConnectionFactory(resourceConnectionFactory("good-1.json"));
        gcmConfig.setKey("mykey");
        gcmConfig.setExecutor(MoreExecutors.sameThreadExecutor());
        
        Gcm gcm = new DefaultGcm(gcmConfig);
                
        GcmRequest request = new GcmRequest()
            .withRegistrationId("hello-world");
                
        GcmResponse response = gcm.send(request).get();
        Assert.assertSame(request, response.getRequest());
        Assert.assertNotNull(response.getResults().get(0).getMessageId());
        Assert.assertNull(response.getResults().get(0).getError());
        Assert.assertNull(response.getResults().get(0).getCanonicalRegistrationId());
        Assert.assertEquals("hello-world", response.getResults().get(0).getRequestedRegistrationId());
    }
    
    @Test
    public void configuration() throws MalformedURLException {
        GcmConfig gcmConfig = new GcmConfig();
        DefaultGcm gcm = new DefaultGcm(gcmConfig);
        Assert.assertNotNull(gcm.getObjectMapper());
        Assert.assertNotNull(gcm.getFilters());
    }
    
    @Test
    public void bad1() throws IOException, InterruptedException, ExecutionException {
        GcmConfig gcmConfig = new GcmConfig();                
        gcmConfig.setConnectionFactory(jsonErrorConnectionFactory(400, "Invalid JSON"));
        gcmConfig.setKey("mykey");
        gcmConfig.setExecutor(MoreExecutors.sameThreadExecutor());
        
        Gcm gcm = new DefaultGcm(gcmConfig);
                
        GcmRequest request = new GcmRequest()
            .withRegistrationId("hello-world");
                
        try {
            gcm.send(request).get();
            Assert.fail("did not throw");
        } catch (ExecutionException ex) {
            Assert.assertTrue(ex.getCause() instanceof GcmNetworkException);
            Assert.assertEquals(400, ((GcmNetworkException) ex.getCause()).getCode());
        }
    }    
    
    @Test
    public void authError() throws IOException, InterruptedException, ExecutionException {
        GcmConfig gcmConfig = new GcmConfig();                
        gcmConfig.setConnectionFactory(jsonErrorConnectionFactory(401, null));
        gcmConfig.setKey("mykey");
        gcmConfig.setExecutor(MoreExecutors.sameThreadExecutor());
        
        Gcm gcm = new DefaultGcm(gcmConfig);
                
        GcmRequest request = new GcmRequest()
            .withRegistrationId("hello-world");
                
        try {
            gcm.send(request).get();
            Assert.fail("did not throw");
        } catch (ExecutionException ex) {
            Assert.assertTrue(ex.getCause() instanceof GcmNetworkException);
            Assert.assertEquals(401, ((GcmNetworkException) ex.getCause()).getCode());
        }
    }       
    
}
