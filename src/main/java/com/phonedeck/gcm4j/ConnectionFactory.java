package com.phonedeck.gcm4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Factory interface for opening HttpURLConnections.
 */
public interface ConnectionFactory {

    /**
     * Open a HttpURLConnection to the given URL.
     * @param url URL to connect to
     * @return HttpURLConnection the opened HttpURLConnection
     * @throws IOException when the connection cannot be opened
     */
    HttpURLConnection open(URL url) throws IOException;
    
}
