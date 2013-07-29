package com.phonedeck.gcm4j;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ResultError {
    /**
     * Check that the request contains a registration ID (either in the registration_id parameter in 
     * a plain text message, or in the registration_ids field in JSON). 
     */
    MISSING_REGISTRATION("MissingRegistration"),
    
    /**
     * Check the formatting of the registration ID that you pass to the server. Make sure it matches
     * the registration ID the phone receives in the com.google.android.c2dm.intent.REGISTRATION intent
     * and that you're not truncating it or adding additional characters. 
     */
    INVALID_REGISTRATION("InvalidRegistration"),
    
    /**
     * A registration ID is tied to a certain group of senders. When an application registers for GCM
     * usage, it must specify which senders are allowed to send messages. Make sure you're using one 
     * of those when trying to send messages to the device. If you switch to a different sender, the 
     * existing registration IDs won't work. 
     */
    MISMATCH_SENDER_ID("MismatchSenderId"),
    
    /**
     * An existing registration ID may cease to be valid in a number of scenarios, including:
     * 
     * - If the application manually unregisters by issuing a com.google.android.c2dm.intent.UNREGISTER intent.
     * - If the application is automatically unregistered, which can happen (but is not guaranteed) 
     *   if the user uninstalls the application.
     * - If the registration ID expires. Google might decide to refresh registration IDs.
     * 
     * For all these cases, you should remove this registration ID from the 3rd-party server and stop
     * using it to send messages. 
     */
    NOT_REGISTERED("NotRegistered"),
    
    /**
     * The total size of the payload data that is included in a message can't exceed 4096 bytes. Note
     * that this includes both the size of the keys as well as the values. 
     */
    MESSAGE_TOO_BIG("MessageTooBig"),
    
    /**
     * The payload data contains a key (such as from or any value prefixed by google.) that is used
     * internally by GCM in the com.google.android.c2dm.intent.RECEIVE Intent and cannot be used.
     * Note that some words (such as collapse_key) are also used by GCM but are allowed in the
     * payload, in which case the payload value will be overridden by the GCM value. 
     */
    INVALID_DATA_KEY("InvalidDataKey"),
    
    /**
     * The value for the Time to Live field must be an integer representing a duration in seconds between 0
     * and 2,419,200 (4 weeks).
     */
    INVALID_TTL("InvalidTtl"),
    
    /**
     * The server encountered an error while trying to process the request. You could retry the same request 
     * (obeying the requirements listed in the Timeout section), but if the error persists, please report the
     * problem in the android-gcm group.
     * 
     * Senders that cause problems risk being blacklisted. 
     */
    INTERNAL_SERVER_ERROR("InternalServerError"),
    
    /**
     * GCM servers were busy and could not process the message for this particular recipient.
     */
    UNAVAILABLE("Unavailable"),
    
    /**
     * A message was addressed to a registration ID whose package name did not match the value
     * passed in the request.
     */
    INVALID_PACKAGE_NAME("InvalidPackageName"),
    
    /**
     * The returned error code was unknown when this version of the client was released. Please check if
     * there is a new version, or file a ticket about it.
     */
    UNSUPPORTED_ERROR_CODE(null);
    
    /**
     * Table for looking up JSON values quickly.
     */
    private static final Map<String, ResultError> REVERSE_TABLE = new HashMap<>(ResultError.values().length);
    
    /**
     * Value of the enum as used in the JSON protocol.
     */
    private final String jsonValue;
    
    static {
        for (ResultError resultError : values()) {
            if (resultError.jsonValue() != null) {
                REVERSE_TABLE.put(resultError.jsonValue(), resultError);
            }
        }
    }
    
    private ResultError(String jsonValue) {
        this.jsonValue = jsonValue;
    }
    
    /**
     * Value of the enum as used in the JSON protocol.
     * @return the value of the enum as used in the JSON protocol
     */
    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }
    
    /**
     * Gets an enum value of the JSON equivalent, or UNSUPPORTED_ERROR_CODE if cannot be matched.
     * @param jsonValue value of the constant in the JSON protocol
     * @return equivalent ResultError value or UNSUPPORTED_ERROR_CODE if cannot be matched
     */
    @JsonCreator
    public static ResultError jsonValueOf(String jsonValue) {
        ResultError result = REVERSE_TABLE.get(jsonValue);
        if (result == null) {
            LoggerFactory.getLogger(ResultError.class).warn("Unsupported error code: {}", jsonValue);
            result = ResultError.UNSUPPORTED_ERROR_CODE;            
        }
        return result;
    }
    
}