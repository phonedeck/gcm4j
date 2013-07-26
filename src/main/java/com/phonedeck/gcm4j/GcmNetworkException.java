package com.phonedeck.gcm4j;

/**
 * Exception that is thrown when the GCM server responds with an error.
 */
public class GcmNetworkException extends RuntimeException {

    private static final long serialVersionUID = -8755599343380178261L;

    private final int code;

    private final String response;
    
    public GcmNetworkException(int code, String response, Throwable cause) {
        super("HTTP " + code + ": " + response);
        this.code = code;
        this.response = response;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getResponse() {
        return response;
    }
    
}
