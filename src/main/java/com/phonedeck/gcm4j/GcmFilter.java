package com.phonedeck.gcm4j;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Users can implement this interface to modify request and responses.
 */
public interface GcmFilter {
    
    /**
     * Allows users to modify the request and the response. Filters are chained, every filter must
     * invoke chain.next() to pass the request to the next filter.
     * @param request the request that may be modified
     * @param chain filter chain to pass the request and get the response to and from the next filter
     * @return the response. Must not be null
     */
    ListenableFuture<GcmResponse> filter(GcmRequest request, FilterChain chain);
    
}
