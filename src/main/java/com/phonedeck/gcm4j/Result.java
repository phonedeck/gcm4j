package com.phonedeck.gcm4j;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("registration_id")
    private String canonicalRegistrationId;

    @JsonProperty("error")
    private ResultError error;

    @JsonIgnore
    private String requestedRegistrationId;

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setCanonicalRegistrationId(String canonicalRegistrationId) {
        this.canonicalRegistrationId = canonicalRegistrationId;
    }

    public String getCanonicalRegistrationId() {
        return canonicalRegistrationId;
    }

    public void setError(ResultError error) {
        this.error = error;
    }

    public ResultError getError() {
        return error;
    }

    public void setRequestedRegistrationId(String requestedRegistrationId) {
        this.requestedRegistrationId = requestedRegistrationId;
    }

    public String getRequestedRegistrationId() {
        return requestedRegistrationId;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("messageId", messageId)
                .add("requestedRegistrationId", requestedRegistrationId)
                .add("canonicalRegistrationId", canonicalRegistrationId)
                .add("error", error)
                .toString();
    }
}