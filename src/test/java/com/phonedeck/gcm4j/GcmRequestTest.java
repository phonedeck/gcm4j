package com.phonedeck.gcm4j;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class GcmRequestTest {

    /**
     * Test given marshalling that the two fields on the {@link GcmRequest} are not included in the marshalling.
     */
    @Test
    public void testGivenGcmRequestWithApiTokenAndClientIdSetAssertTokenIsNotMarshalledIntoJson() throws Exception {
        GcmRequest request = new GcmRequest();

        // set the two fields that should be ignored
        request.setKey("token");
        request.setAttribute("attr", "asdf");
        request.setTo("54321");
        request.setDryRun(true);
        request.setCollapseKey("Collapse");
        request.setDelayWhileIdle(true);
        request.setRestrictedPackageName("restricted.package.name");
        request.setTimeToLive(360);
        request.setRegistrationIds(Arrays.asList("12345"));
        request.setPriority("normal");
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        request.setData(data);

        Map<String, String> notification = new HashMap<>();
        notification.put("sound", "default");
        request.setNotification(notification);

        String expected = loadStringFromFile("test-non-marshalled-fields.json");

        ObjectMapper mapper = buildObjectMapper();
        assertEquals(expected, mapper.writeValueAsString(request));
    }

    private static ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_DEFAULT);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    private String loadStringFromFile(String file) {
        InputStream in = getClass().getResourceAsStream(file);
        if (in == null) {
            throw new RuntimeException(String.format("file: [%s] could not be loaded", file));
        }
        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\A");
        String result = scanner.next();
        scanner.close();
        return result;
    }

}
