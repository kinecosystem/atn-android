package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import kin.core.KinAccount;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
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
    }

    @Test
    public void create_AccountCreatorFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(null);
        boolean created = sessionCreator.create();
        assertFalse(created);
    }

    @Test
    public void create_ConfigurationProviderInitFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.enabled()).thenReturn(false);

        boolean created = sessionCreator.create();
        assertFalse(created);
    }
    @Test
    public void create_OnBoardingFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.enabled()).thenReturn(true);
        when(mockOnBoarding.onBoard((KinAccount) any())).thenReturn(false);

        boolean created = sessionCreator.create();
        assertFalse(created);
    }

}