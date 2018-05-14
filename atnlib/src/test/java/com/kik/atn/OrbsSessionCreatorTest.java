package com.kik.atn;


import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import java.io.IOException;
import java.math.BigDecimal;

import kin.core.KinAccount;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(manifest = org.robolectric.annotation.Config.NONE)
public class OrbsSessionCreatorTest {

    private static final String ORBS_SERVER_ADDRESS = "c2739f7021f841b2cf9c23a4647b0729bf8b69f32551733c2046c82b437a89bc";
    private static final String ORBS_ACCOUNT_ADDRESS = "058ce08b42254e4f00f0f85622edfd4871799df6898ba440a94bc0f579ef986d";
    @Mock
    private
    OrbsWallet mockOrbsWallet;
    @Mock
    private
    EventLogger mockEventLogger;
    @Mock
    private
    ATNServer mockAtnServer;
    @Mock
    private
    KinAccountCreator mockKinAccountCreator;
    @Mock
    private
    KinAccount mockKinAccount;
    @Mock
    private
    ConfigurationProvider mockConfigurationProvider;

    private OrbsSessionCreator sessionCreator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        sessionCreator = new OrbsSessionCreator(mockOrbsWallet, mockEventLogger,
                mockAtnServer, mockKinAccountCreator, mockConfigurationProvider);

        when(mockKinAccount.getPublicAddress()).thenReturn("GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV");
        when(mockKinAccountCreator.getAccount()).thenReturn(mockKinAccount);
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(false, "", 1,
                new Config.Orbs(true, 1, ORBS_SERVER_ADDRESS)));
        when(mockOrbsWallet.getPublicAddress()).thenReturn(ORBS_ACCOUNT_ADDRESS);
        setAndroidSDKVersion(23);
    }

    @Test
    public void create_Disabled_Failure() throws Exception {
        when(mockConfigurationProvider.getConfig(anyString())).thenReturn(new Config(false, "", 1,
                new Config.Orbs(false, 1, ORBS_SERVER_ADDRESS)));

        boolean result = sessionCreator.create();

        assertThat(result, is(false));
        verifyZeroInteractions(mockOrbsWallet, mockEventLogger, mockAtnServer);
    }

    @Test
    public void create_BelowAPI19_Failure() throws Exception {
        setAndroidSDKVersion(18);

        boolean result = sessionCreator.create();

        assertThat(result, is(false));
        verifyZeroInteractions(mockOrbsWallet, mockEventLogger, mockAtnServer);
    }

    @Test
    public void create_NotFunded_Success() throws Exception {
        mockNotFundedAccount();

        boolean result = sessionCreator.create();

        assertThat(result, is(true));
        assertThat(sessionCreator.getOrbsReceiver(), notNullValue());
        assertThat(sessionCreator.getOrbsSender(), notNullValue());
        InOrder inOrder = Mockito.inOrder(mockEventLogger, mockAtnServer, mockOrbsWallet);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_STARTED);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_LOAD_WALLET_STARTED);
        inOrder.verify(mockOrbsWallet).loadWallet();
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_LOAD_WALLET_SUCCEEDED);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_ACCOUNT_NOT_FUNDED);
        inOrder.verify(mockAtnServer).fundOrbsAccount(ORBS_ACCOUNT_ADDRESS);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ACCOUNT_FUNDING_SUCCEEDED);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_SUCCEEDED);
    }

    @Test
    public void create_AlreadyFunded_Success() throws Exception {
        mockFundedAccount();

        boolean result = sessionCreator.create();

        assertThat(result, is(true));
        InOrder inOrder = Mockito.inOrder(mockEventLogger, mockAtnServer, mockOrbsWallet);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_STARTED);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_LOAD_WALLET_STARTED);
        inOrder.verify(mockOrbsWallet).loadWallet();
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_LOAD_WALLET_SUCCEEDED);
        inOrder.verify(mockEventLogger, times(0)).sendOrbsEvent(Events.ONBOARD_ACCOUNT_NOT_FUNDED);
        inOrder.verify(mockAtnServer, times(0)).fundOrbsAccount(anyString());
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_SUCCEEDED);
    }

    @Test
    public void create_LoadingWalletFailure_Failure() throws Exception {
        Exception expectedException = new Exception("some error");
        doThrow(expectedException).when(mockOrbsWallet).loadWallet();

        boolean result = sessionCreator.create();

        assertThat(result, is(false));
        InOrder inOrder = Mockito.inOrder(mockEventLogger, mockAtnServer, mockOrbsWallet);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_STARTED);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_LOAD_WALLET_STARTED);
        inOrder.verify(mockOrbsWallet).loadWallet();
        inOrder.verify(mockEventLogger).sendOrbsErrorEvent(Events.ONBOARD_LOAD_WALLET_FAILED, expectedException);
        inOrder.verify(mockEventLogger, times(0)).sendOrbsEvent(Events.ONBOARD_ACCOUNT_NOT_FUNDED);
        inOrder.verify(mockAtnServer, times(0)).fundOrbsAccount(anyString());
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_FAILED);
    }

    @Test
    public void create_FundsOrbsFailure_Failure() throws Exception {
        mockNotFundedAccount();

        IOException expectedException = new IOException("some error");
        doThrow(expectedException).when(mockAtnServer).fundOrbsAccount(ORBS_ACCOUNT_ADDRESS);

        boolean result = sessionCreator.create();

        assertThat(result, is(false));
        InOrder inOrder = Mockito.inOrder(mockEventLogger, mockAtnServer, mockOrbsWallet);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_STARTED);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_LOAD_WALLET_STARTED);
        inOrder.verify(mockOrbsWallet).loadWallet();
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_LOAD_WALLET_SUCCEEDED);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_ACCOUNT_NOT_FUNDED);
        inOrder.verify(mockEventLogger).sendOrbsErrorEvent(Events.ACCOUNT_FUNDING_FAILED, expectedException);
        inOrder.verify(mockEventLogger).sendOrbsEvent(Events.ONBOARD_FAILED);
    }

    private void mockNotFundedAccount() {
        when(mockOrbsWallet.getBalance()).thenReturn(BigDecimal.ZERO);
    }

    private void mockFundedAccount() {
        when(mockOrbsWallet.getBalance()).thenReturn(BigDecimal.TEN);
    }

    private void setAndroidSDKVersion(int version) {
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", version);
    }

}
