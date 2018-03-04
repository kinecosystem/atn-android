package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ATNReceiverTest {

    @Mock
    private ATNServer mockAtnServer;
    @Mock
    private EventLogger mockEventLogger;
    @Mock
    private ConfigurationProvider mockConfigProvider;
    private ATNReceiver receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        receiver = new ATNReceiver(mockAtnServer, mockEventLogger, mockConfigProvider, "GCKG5WGBIJP74UDNRIRDFGENNIH5Y3KBI5IHREFAJKV4MQXLELT7EX6V");
        //by default mock enabled configuration
        when(mockConfigProvider.getConfig(anyString())).thenReturn(new Config(true, "dummyaddress"));
    }

    @Test
    public void receiveATN() throws Exception {
        receiver.receiveATN();

        verify(mockEventLogger).sendEvent("receive_atn_started");
        verify(mockEventLogger).sendEvent("receive_atn_succeed");
    }

    @Test
    public void receiveATN_Error() throws Exception {
        doThrow(new HttpResponseException(404))
                .when(mockAtnServer)
                .receiveATN(anyString());

        receiver.receiveATN();

        verify(mockEventLogger).sendEvent("receive_atn_started");
        verify(mockEventLogger).sendEvent("receive_atn_failed");
    }


    @Test
    public void receiveATN_Disabled_NoTransactionJustLog() throws Exception {
        when(mockConfigProvider.getConfig(anyString())).thenReturn(new Config(false, "someaddress"));

        receiver.receiveATN();

        verifyZeroInteractions(mockAtnServer);
        verify(mockEventLogger, only()).log(anyString());
    }

}