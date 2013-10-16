package com.phonedeck.gcm4j.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import com.phonedeck.gcm4j.GcmRequest;

/**
 * Factory that produces example GCM request messages for testing purposes.
 */
public class TestRequestFactory
{
    private TestRequestFactory()
    {
        /* No build me please */
    }
    
    /**
     * Build an example GCM request message.
     * @param collapsekeyLength the length of collapse key.
     * @param dataLength the length of the data section.
     * @param regIdSize the length of the registration id.
     * @param count the number of requests to generate.
     * @return a list of requests generated.
     */
    public static List<GcmRequest> build(int collapsekeyLength, int dataLength, int regIdSize, int count)
    {
        List<GcmRequest> requests = new ArrayList<>();
        for (int i =0; i < count; i++)
        {
            String collapseKey = RandomStringUtils.random(collapsekeyLength);
            String data = RandomStringUtils.random(dataLength);
            String regId = TestRegistrationIdFactory.build(regIdSize);
            GcmRequest request = new GcmRequest().withCollapseKey(collapseKey).withDataItem("SOME_DATA_KEY", data)
                    .withDelayWhileIdle(true).withRegistrationId(regId).withTimeToLive(360);
            requests.add(request);
        }
        return requests;
    }
}
