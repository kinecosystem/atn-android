package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class ATNReceiverTest {

    @Mock
    private ATNServer mockAtnServer;
    @Mock
    private EventLogger mockEventLogger;
    private ATNReceiver receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        receiver = new ATNReceiver(mockAtnServer, mockEventLogger, "GCKG5WGBIJP74UDNRIRDFGENNIH5Y3KBI5IHREFAJKV4MQXLELT7EX6V");
    }

    @Test
    public void receiveATN() throws Exception {
        receiver.receiveATN();

        verify(mockEventLogger).sendEvent("claim_atn_started");
        verify(mockEventLogger).sendEvent("claim_atn_succeeded");
    }

    @Test
    public void receiveATN_Error() throws Exception {
        HttpResponseException expectedException = new HttpResponseException(404);
        doThrow(expectedException)
                .when(mockAtnServer)
                .receiveATN(anyString());

        receiver.receiveATN();

        verify(mockEventLogger).sendEvent("claim_atn_started");
        verify(mockEventLogger).sendErrorEvent("claim_atn_failed", expectedException);
    }
}