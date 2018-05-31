package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrbsReceiverTest {

    @Mock
    private ATNServer mockAtnServer;
    @Mock
    private EventLogger mockEventLogger;
    @Mock
    private ConfigurationProvider mockConfigProvider;
    private OrbsReceiver receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        receiver = new OrbsReceiver(mockAtnServer, mockEventLogger, "c2739f7021f841b2cf9c23a4647b0729bf8b69f32551733c2046c82b437a89bc");
        //by default mock enabled configuration
        when(mockConfigProvider.getConfig(anyString())).thenReturn(
                new Config(false, "dummyaddress", 1, new Config.Orbs(true, 1, "")));
    }

    @Test
    public void receiveOrbs() throws Exception {
        receiver.receiveOrbs();

        verify(mockEventLogger).sendOrbsEvent("claim_orbs_started");
        verify(mockEventLogger).sendOrbsEvent("claim_orbs_succeeded");
    }

    @Test
    public void receiveOrbs_Error() throws Exception {
        HttpResponseException expectedException = new HttpResponseException(404);
        doThrow(expectedException)
                .when(mockAtnServer)
                .receiveOrbs(anyString());

        receiver.receiveOrbs();

        verify(mockEventLogger).sendOrbsEvent("claim_orbs_started");
        verify(mockEventLogger).sendOrbsErrorEvent("claim_orbs_failed", expectedException);
    }

}