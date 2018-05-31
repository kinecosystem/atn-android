package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import kin.core.KinAccount;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ATNSessionTest {

    private static final String ATN_TARGET_ADDRESS = "GAEJDB7KBEBB25WHNB5JJ3IXEXSVVS44WL47XGXOHBZ7XEIAEEHA5JDY";
    private static final String PUBLIC_ADDRESS = "GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV";
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
    private ATNSession sessionCreator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        sessionCreator = new ATNSession(mockEventLogger, mockAtnServer, mockKinAccountCreator,
                mockConfigurationProvider, mockOnBoarding);

        when(mockKinAccount.getPublicAddress()).thenReturn(PUBLIC_ADDRESS);
    }

    @Test
    public void create_AccountCreatorFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(null);

        assertFalse(sessionCreator.create());
        assertFalse(sessionCreator.isCreated());
    }

    @Test
    public void create_ConfigurationProviderInitFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(false, ""));

        assertFalse(sessionCreator.create());
        assertFalse(sessionCreator.isCreated());
    }

    @Test
    public void create_OnBoardingFailure_NotCreated() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(true, ""));
        when(mockOnBoarding.onBoard((KinAccount) any())).thenReturn(false);

        assertFalse(sessionCreator.create());
        assertFalse(sessionCreator.isCreated());
    }

    @Test
    public void createAndSendATN_CreatedSuccessfully_AtnSent() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(true, ATN_TARGET_ADDRESS));
        when(mockOnBoarding.onBoard((KinAccount) any())).thenReturn(true);

        assertTrue(sessionCreator.create());
        assertTrue(sessionCreator.isCreated());

        sessionCreator.sendATN();
        verify(mockKinAccount).sendTransactionSync(ATN_TARGET_ADDRESS, new BigDecimal(1));
    }

    @Test
    public void createAndReceiveATN_CreatedSuccessfully_AtnReceived() throws Exception {
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(true, ATN_TARGET_ADDRESS));
        when(mockOnBoarding.onBoard((KinAccount) any())).thenReturn(true);

        assertTrue(sessionCreator.create());
        assertTrue(sessionCreator.isCreated());

        sessionCreator.receiveATN();
        verify(mockAtnServer).receiveATN(PUBLIC_ADDRESS);
    }

}