package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrbsSenderTest {

    private static final String ORBS_ADDRESS = "c2739f7021f841b2cf9c23a4647b0729bf8b69f32551733c2046c82b437a89bc";

    @Mock
    private OrbsWallet mockOrbsWallet;
    @Mock
    private EventLogger mockEventLogger;
    private OrbsSender sender;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mockOrbsWallet.getPublicAddress()).thenReturn("058ce08b42254e4f00f0f85622edfd4871799df6898ba440a94bc0f579ef986d");
        sender = new OrbsSender(mockOrbsWallet, mockEventLogger);
    }

    @Test
    public void sendorbs_Success() throws Exception {

        sender.sendOrbs(ORBS_ADDRESS);

        verify(mockOrbsWallet).sendOrbs(ORBS_ADDRESS, new BigDecimal("1"));
        verify(mockEventLogger).sendOrbsEvent("send_orbs_started");
    }

    @Test
    public void sendorbs_SendTransactionFailure_ReportFailure() throws Exception {
        Exception expectedException = new Exception("some error");
        doThrow(expectedException).when(mockOrbsWallet).sendOrbs(anyString(), (BigDecimal) any());

        sender.sendOrbs(ORBS_ADDRESS);

        verify(mockOrbsWallet).sendOrbs(ORBS_ADDRESS, new BigDecimal("1"));
        verify(mockEventLogger).sendOrbsErrorEvent("send_orbs_failed", expectedException);
    }

}