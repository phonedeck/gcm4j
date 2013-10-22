package com.phonedeck.gcm4j;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class BlockingGcm extends AbstractDefaultGcm {

    public BlockingGcm(GcmConfig gcmConfig) {
        super(gcmConfig);
    }

    @Override
    protected ListenableFuture<GcmResponse> executeRequestFuture(GcmRequest request) {
        try {
            return Futures.immediateFuture(executeRequest(request));
        } catch (Exception ex) {
            return Futures.immediateFailedFuture(ex);
        }
    }

}
