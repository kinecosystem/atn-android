package com.kik.atn;

import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(manifest = org.robolectric.annotation.Config.NONE)
public class ATNHandlerTest {

    private static final String PUBLIC_ADDRESS = "GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV";
    private static final String ATN_TARGET_ADDRESS = "GAEJDB7KBEBB25WHNB5JJ3IXEXSVVS44WL47XGXOHBZ7XEIAEEHA5JDY";
    private static final String ORBS_TARGET_ADDRESS = "18084d8948e4fa9283cc96cb72012b5dfa91cdccf52f420e7ec185ff2b4a2723";
    @Mock
    private
    ATNSession atnSession;
    @Mock
    private OrbsSession orbsSession;
    @Mock
    private Dispatcher dispatcher;
    @Mock
    private Dispatcher orbsDispatcher;
    @Mock
    private ConfigurationProvider configurationProvider;
    @Mock
    private AndroidLogger androidLogger;
    private ATNHandler atnHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Looper looper = Looper.getMainLooper();
        atnHandler = new ATNHandler(atnSession, orbsSession, dispatcher,
                orbsDispatcher, configurationProvider, androidLogger, PUBLIC_ADDRESS, looper);
    }

    @Test
    public void sentX2_Disabled_NoOpJustLog() throws Exception {
        testDisabledByConfiguration(Dispatcher.MSG_SENT, Dispatcher.MSG_SENT);
    }

    @Test
    public void sentAndReceive_Disabled_NoOpJustLog() throws Exception {
        testDisabledByConfiguration(Dispatcher.MSG_SENT, Dispatcher.MSG_RECEIVE);
    }

    @Test
    public void sentOrbsX2_Disabled_NoOpJustLog() throws Exception {
        testDisabledByConfiguration(Dispatcher.MSG_SENT_ORBS, Dispatcher.MSG_SENT_ORBS);
    }

    private void testDisabledByConfiguration(int firstMessage, int secondMessage) {
        Config config = new Config(false, ATN_TARGET_ADDRESS, 3,
                new Config.Orbs(false, 3, ""));
        mockReturnedConfig(config);

        atnHandler.handleMessage(createMessage(firstMessage));
        verifyBusyIsFalse();
        atnHandler.handleMessage(createMessage(secondMessage));
        verifyBusyIsFalse();

        verify(configurationProvider, times(2)).getConfig(PUBLIC_ADDRESS);
        verify(androidLogger, times(2)).log(anyString());
        verifyZeroInteractions(atnSession, orbsSession, dispatcher, orbsDispatcher);
    }

    @Test
    public void sentX2_SessionNotCreated_RetryNoSendReceive() throws Exception {
        Config config = new Config(true, ATN_TARGET_ADDRESS, 3,
                new Config.Orbs(false, 3, ORBS_TARGET_ADDRESS));
        mockReturnedConfig(config);
        mockSessionCreated(false);

        atnHandler.handleMessage(createMessage(Dispatcher.MSG_SENT));
        verifyBusyIsFalse();
        atnHandler.handleMessage(createMessage(Dispatcher.MSG_SENT));
        verifyBusyIsFalse();

        verify(atnSession, times(2)).create();
        verifyZeroInteractions(orbsSession);
        verifyRateLimitUpdate();

    }

    @Test
    public void sentX2_SessionCreated_SendAtn() throws Exception {
        Config config = new Config(true, ATN_TARGET_ADDRESS, 3,
                new Config.Orbs(false, 3, ORBS_TARGET_ADDRESS));
        mockReturnedConfig(config);
        mockSessionCreated(true);

        atnHandler.handleMessage(createMessage(Dispatcher.MSG_SENT));
        verifyBusyIsFalse();
        atnHandler.handleMessage(createMessage(Dispatcher.MSG_SENT));

        verifyBusyIsFalse();
        verify(atnSession).create();
        verify(atnSession).sendATN();
        verifyZeroInteractions(orbsSession);
        verifyRateLimitUpdate();
    }

    @Test
    public void sentAndReceive_SessionCreated_ReceiveAtn() throws Exception {
        Config config = new Config(true, ATN_TARGET_ADDRESS, 3,
                new Config.Orbs(false, 3, ORBS_TARGET_ADDRESS));
        mockReturnedConfig(config);
        mockSessionCreated(true);

        atnHandler.handleMessage(createMessage(Dispatcher.MSG_SENT));
        verifyBusyIsFalse();
        atnHandler.handleMessage(createMessage(Dispatcher.MSG_RECEIVE));

        verifyBusyIsFalse();
        verify(atnSession).create();
        verify(atnSession).receiveATN();
        verifyZeroInteractions(orbsSession);
        verifyRateLimitUpdate();
    }

    @Test
    public void receiveX2_NoOp() throws Exception {
        Config config = new Config(true, ATN_TARGET_ADDRESS, 3,
                new Config.Orbs(false, 3, ORBS_TARGET_ADDRESS));
        mockReturnedConfig(config);
        mockSessionCreated(false);

        atnHandler.handleMessage(createMessage(Dispatcher.MSG_RECEIVE));
        verifyBusyIsFalse();
        atnHandler.handleMessage(createMessage(Dispatcher.MSG_RECEIVE));

        verifyBusyIsFalse();
        verifyZeroInteractions(orbsSession, dispatcher, orbsDispatcher);
    }

    @Test
    public void receiveX2Orbs_NoOp() throws Exception {
        Config config = new Config(true, ATN_TARGET_ADDRESS, 3,
                new Config.Orbs(true, 3, ORBS_TARGET_ADDRESS));
        mockReturnedConfig(config);
        mockOrbsSessionCreated(false);

        atnHandler.handleMessage(createMessage(Dispatcher.MSG_RECEIVE_ORBS));
        verifyBusyIsFalse();
        atnHandler.handleMessage(createMessage(Dispatcher.MSG_RECEIVE_ORBS));

        verifyBusyIsFalse();
        verifyZeroInteractions(atnSession, dispatcher, orbsDispatcher);
    }

    private void verifyRateLimitUpdate() {
        verify(dispatcher, times(2)).setRateLimit(3);
        verify(orbsDispatcher, times(2)).setRateLimit(3);
    }

    private void verifyBusyIsFalse() {
        assertThat(atnHandler.isAtnBusy(), equalTo(false));
        assertThat(atnHandler.isOrbsBusy(), equalTo(false));
    }

    private void mockReturnedConfig(Config config) {
        when(configurationProvider.getConfig(anyString())).thenReturn(config);
        when(configurationProvider.getLastConfig()).thenReturn(config);
    }

    private Message createMessage(int what) {
        Message msg = new Message();
        msg.what = what;
        return msg;
    }

    private void mockSessionCreated(boolean created) {
        if (created) {
            when(atnSession.isCreated()).thenReturn(false).thenReturn(true);
            when(atnSession.create()).thenReturn(true);
        } else {
            when(atnSession.isCreated()).thenReturn(false);
            when(atnSession.create()).thenReturn(false);
        }
    }

    private void mockOrbsSessionCreated(boolean created) {
        if (created) {
            when(orbsSession.isCreated()).thenReturn(false).thenReturn(true);
            when(orbsSession.create()).thenReturn(true);
        } else {
            when(orbsSession.isCreated()).thenReturn(false);
            when(orbsSession.create()).thenReturn(false);
        }
    }

}