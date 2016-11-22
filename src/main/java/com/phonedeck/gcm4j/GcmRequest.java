package com.phonedeck.gcm4j;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class GcmRequest {

    @JsonProperty("registration_ids")
    private List<String> registrationIds = new ArrayList<String>();

    @JsonProperty("notification_key")
    private String notificationKey;

    @JsonProperty("notification_key_name")
    private String notificationKeyName;

    @JsonProperty("collapse_key")
    private String collapseKey;

    @JsonProperty("data")
    private Map<String, String> data = new LinkedHashMap<String, String>();

    @JsonProperty("notification")
    private Map<String, String> notification = new LinkedHashMap<String, String>();

    @JsonProperty("delay_while_idle")
    private boolean delayWhileIdle;

    @JsonProperty("time_to_live")
    private long timeToLive;

    @JsonProperty("restricted_package_name")
    private String restrictedPackageName;

    @JsonProperty("priority")
    private String priority;

    @JsonIgnore
    private String key;

    @JsonIgnore
    private HashMap<String, Object> attributes;

    /**
     * Allows developers to test their request without actually sending a message.
     */
    @JsonProperty("dry_run")
    private boolean dryRun;

    @JsonProperty("content_available")
    private boolean contentAvailable;

    /*
     * Chaining setters
     */
    public GcmRequest withKey(String key) {
        setKey(key);
        return this;
    }

    public GcmRequest withRegistrationId(String registrationId) {
        getRegistrationIds().add(registrationId);
        return this;
    }

    public GcmRequest withRegistrationIds(List<String> registrationIds) {
        setRegistrationIds(registrationIds);
        return this;
    }
    
    public GcmRequest withRegistrationIds(Collection<String> registrationIds) {
    	getRegistrationIds().addAll(registrationIds);
    	return this;
    }

    public GcmRequest withNotificationKey(String notificationKey) {
        setNotificationKey(notificationKey);
        return this;
    }

    public GcmRequest withNotificationKeyName(String notificationKeyName) {
        setNotificationKeyName(notificationKeyName);
        return this;
    }

    public GcmRequest withCollapseKey(String collapseKey) {
        setCollapseKey(collapseKey);
        return this;
    }

    public GcmRequest withDataItem(String key, String value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
        return this;
    }

    public GcmRequest withData(Map<String, String> data) {
        setData(data);
        return this;
    }

    public GcmRequest withNotification(Map<String, String> notification) {
        setNotification(notification);
        return this;
    }

    public GcmRequest withDelayWhileIdle(boolean delayWhileIdle) {
        setDelayWhileIdle(delayWhileIdle);
        return this;
    }

    public GcmRequest withTimeToLive(long timeToLive) {
        setTimeToLive(timeToLive);
        return this;
    }

    public GcmRequest withPriority(String priority) {
        setPriority(priority);
        return this;
    }

    public GcmRequest withRestrictedPackageName(String restrictedPackageName) {
        setRestrictedPackageName(restrictedPackageName);
        return this;
    }

    public GcmRequest withDryRun(boolean dryRun) {
        setDryRun(dryRun);
        return this;
    }

    public GcmRequest withContentAvailable(boolean contentAvailable) {
        setContentAvailable(contentAvailable);
        return this;
    }

    @JsonIgnore
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            if (attributes != null) {
                attributes.remove(name);
            }
        } else {
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put(name, value);
        }
    }

    @JsonIgnore
    public Object getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(name);
    }

    @JsonIgnore
    public Map<String, Object> getAttributes() {
        return attributes != null ? Collections.unmodifiableMap(attributes) : Collections.<String, Object>emptyMap();
    }


    /*
     * Getters and Setters
     */

    public List<String> getRegistrationIds() {
        return registrationIds;
    }

    public void setRegistrationIds(List<String> registrationIds) {
        this.registrationIds = registrationIds;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public String getNotificationKeyName() {
        return notificationKeyName;
    }

    public void setNotificationKeyName(String notificationKeyName) {
        this.notificationKeyName = notificationKeyName;
    }

    public String getCollapseKey() {
        return collapseKey;
    }

    public void setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public void setNotification(Map<String, String> notification) {
        this.notification = notification;
    }

    public boolean isDelayWhileIdle() {
        return delayWhileIdle;
    }

    public void setDelayWhileIdle(boolean delayWhileIdle) {
        this.delayWhileIdle = delayWhileIdle;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRestrictedPackageName() {
        return restrictedPackageName;
    }

    public void setRestrictedPackageName(String restrictedPackageName) {
        this.restrictedPackageName = restrictedPackageName;
    }


    /**
     * Allows developers to test their request without actually sending a message.
     *
     * @return true if the request is a dry run
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * Allows developers to test their request without actually sending a message.
     *
     * @param dryRun true if the request is a dry run
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isContentAvailable() {
        return contentAvailable;
    }

    public void setContentAvailable(boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
    }
}
