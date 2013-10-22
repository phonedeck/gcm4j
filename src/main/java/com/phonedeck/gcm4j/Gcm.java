package com.phonedeck.gcm4j;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Interface of the GCM Client. Request can be sent using the {@link #send(GcmRequest)} method.
 */
public interface Gcm {

    /**
     * Send a GCM request and get the response future.
     * @param request the request to send
     * @return future to get the response
     */
    ListenableFuture<GcmResponse> send(GcmRequest request);

}