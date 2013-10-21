package com.phonedeck.gcm4j;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GcmResponse {

    @JsonProperty("multicast_id")
    private long multicastId;

    @JsonProperty("results")
    private List<Result> results = new ArrayList<>();

    private Long retryAfter;

    private GcmRequest request;

    public long getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(long multicastId) {
        this.multicastId = multicastId;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public GcmRequest getRequest() {
        return request;
    }

    public void setRequest(GcmRequest request) {
        this.request = request;
    }

    public Long getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(Long retryAfter) {
        this.retryAfter = retryAfter;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("multicastId", multicastId)
                .add("results", results)
                .toString();
    }
}