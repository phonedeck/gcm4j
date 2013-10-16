package com.phonedeck.gcm4j.test;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.base.Preconditions;

/**
 * Factory to produce example registration ids similar to those used by GCM.
 */
public class TestRegistrationIdFactory
{
    private static final int DEFAULT_LENGTH = 208;
    private static String VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
    
    private TestRegistrationIdFactory()
    {
        /* No constructing */
    }
    
    /**
     * Build an example registration id with a length of {@value #DEFAULT_LENGTH}.
     * @return the example registration id.
     */
    public static String build()
    {
        return build(DEFAULT_LENGTH);
    }
    
    /**
     * Build a registration id with a specific length.
     * @param len the length of registration id to generate.
     * @return the example registration id.
     */
    public static String build(int len)
    {
        Preconditions.checkArgument(len > 0 && len <= 4096);
        
        return RandomStringUtils.random(len, VALID_CHARS);
    }
}
