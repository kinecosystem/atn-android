package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import kin.core.KinAccount;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ATNSessionCreatorTest {

    @Mock
    private ATNServer mockAtnServer;
    @Mock
    private EventLogger mockEventLogger;
    @Mock
    private KinAccountCreator mockKinAccountCreator;
    @Mock
    private ConfigurationProvider mockConfigurationProvider;
    @Mock
    private KinAccount mockKinAccount;
    @Mock
    private ATNAccountOnBoarding mockOnBoarding;
    private ATNSessionCreator sessionCreator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        sessionCreator = new ATNSessionCreator(mockEventLogger, mockAtnServer, mockKinAccountCreator,
                mockConfigurationProvider, mockOnBoarding);

        when(mockKinAccount.getPublicAddress()).thenReturn("GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV");
    }

    @Test
    public void create_AccountCreatorFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(null);

        assertFalse(sessionCreator.create());
    }

    @Test
    public void create_ConfigurationProviderInitFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(false, ""));

        assertFalse(sessionCreator.create());
    }

    @Test
    public void create_OnBoardingFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(true, ""));
        when(mockOnBoarding.onBoard((KinAccount) any())).thenReturn(false);

        assertFalse(sessionCreator.create());
    }

    @Test
    public void create_Success_Created() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(true, ""));
        when(mockOnBoarding.onBoard((KinAccount) any())).thenReturn(true);

        assertTrue(sessionCreator.create());
        assertNotNull(sessionCreator.getATNReceiver());
        assertNotNull(sessionCreator.getATNSender());
    }

}