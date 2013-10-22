package com.phonedeck.gcm4j;

public class GcmException extends RuntimeException {

    private static final long serialVersionUID = 982618239551145618L;

    public GcmException(String message, Throwable cause) {
        super(message, cause);
    }

    public GcmException(String message) {
        super(message);
    }

}
