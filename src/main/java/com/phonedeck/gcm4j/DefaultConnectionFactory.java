package com.phonedeck.gcm4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Connection Factory that uses {@link URL#openConnection()}.
 *
 */
public class DefaultConnectionFactory implements ConnectionFactory {

    @Override
    public HttpURLConnection open(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

}
