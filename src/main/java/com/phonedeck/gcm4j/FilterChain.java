package com.phonedeck.gcm4j;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Filter chain for passing the request to the next filter.
 */
public interface FilterChain {
    
    /**
     * Pass the request to the next filter.
     * @param request possible modified request
     * @return response from the next filter
     */
    ListenableFuture<GcmResponse> next(GcmRequest request);
    
    /**
     * GcmClient that sends the request.
     * @return GcmClient that sends the request
     */
    Gcm getClient();
    
}
