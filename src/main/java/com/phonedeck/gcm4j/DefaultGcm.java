package com.phonedeck.gcm4j;

import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

public class DefaultGcm extends AbstractDefaultGcm {

    private final ListeningExecutorService executor;

    public DefaultGcm(GcmConfig gcmConfig) {
        super(gcmConfig);
        this.executor = gcmConfig.getExecutor() != null ? gcmConfig.getExecutor() : MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    @Override
    protected ListenableFuture<GcmResponse> executeRequestFuture(final GcmRequest request) {
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

}
