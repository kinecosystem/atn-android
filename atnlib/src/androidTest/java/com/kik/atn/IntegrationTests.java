package com.kik.atn;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import kin.core.Balance;
import kin.core.KinAccount;
import kin.core.exception.AccountNotFoundException;
import kin.core.exception.OperationFailedException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static java.lang.Thread.sleep;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class IntegrationTests {


    private static final String PUBLIC_ADDRESS = "GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV";
    @Mock
    private KinAccount mockKinAccount;
    private MockWebServer mockWebServer;
    private ATN atn;
    private String mockServerUrl;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mockKinAccount.getPublicAddress()).thenReturn(PUBLIC_ADDRESS);

        Context appContext = InstrumentationRegistry.getTargetContext();
        mockWebServer = new MockWebServer();
        mockServerUrl = mockWebServer.url("").toString();
        TestModulesProvider modulesProvider = new TestModulesProvider(appContext, mockServerUrl, mockKinAccount);

        atn = new ATN(modulesProvider);
    }

    @Test
    public void init_WithOnboarding() throws Exception {
        //configuration
        mockEnabledConfiguration();
        //not onboarded yet
        doThrow(new AccountNotFoundException("")).when(mockKinAccount).getBalanceSync();
        //create account
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        //fund with ATN
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        //for send transaction request
        mockEnabledConfiguration();

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        //should be dropped by rate limiter
        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        sleep(1010);

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());

        verify(mockKinAccount, timeout(1000).times(1))
                .sendTransactionSync(eq("GBNU4TLYIQOQBM3PT32Z3CCYSMI6CDK7FXQR6R5DYB52GUPXES2S6XTU")
                        , anyString(), eq(new BigDecimal(1.0)));
    }

    @Test
    public void init_AlreadyOnboarded() throws Exception {
        mockAlreadyOnBoarded();
        mockEnabledConfiguration();

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        //should be dropped by rate limiter
        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        sleep(1010);

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());

        verify(mockKinAccount, timeout(1000).times(2)).getPublicAddress();
        verify(mockKinAccount, timeout(1000).times(1)).getBalanceSync();
        verify(mockKinAccount, timeout(1000).times(1)).sendTransactionSync(anyString(), anyString(), (BigDecimal) any());
        verifyNoMoreInteractions(mockKinAccount);
    }

    private void mockAlreadyOnBoarded() throws OperationFailedException {
        //configuration
        mockEnabledConfiguration();
        when(mockKinAccount.getBalanceSync()).thenReturn(new Balance() {
            @Override
            public BigDecimal value() {
                return new BigDecimal(10);
            }

            @Override
            public String value(int precision) {
                return "10.0";
            }
        });
    }

    private void mockEnabledConfiguration() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\n" +
                        "     \"enabled\" : true,\n" +
                        "     \"transaction_lapse\" : 1,\n" +
                        "     \"target_wallet_address\": \"GBNU4TLYIQOQBM3PT32Z3CCYSMI6CDK7FXQR6R5DYB52GUPXES2S6XTU\"\n" +
                        "}")
                .setResponseCode(200));
    }

    @Test
    public void init_NotEnabled() throws Exception {
        //configuration
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\n" +
                        "     \"enabled\" : false,\n" +
                        "     \"target_wallet_address\": \"GBNU4TLYIQOQBM3PT32Z3CCYSMI6CDK7FXQR6R5DYB52GUPXES2S6XTU\"\n" +
                        "}")
                .setResponseCode(200));

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        verify(mockKinAccount, timeout(1000).only()).getPublicAddress();
        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        verify(mockKinAccount, timeout(1000).only()).getPublicAddress();
        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        verify(mockKinAccount, timeout(1000).only()).getPublicAddress();
    }

    @Test
    public void onMessageReceived() throws Exception {
        ATNServer mockATNServer = mock(ATNServer.class);
        TestModulesProvider modulesProvider = new TestModulesProvider(InstrumentationRegistry.getTargetContext(), mockServerUrl, mockKinAccount,
                mockATNServer);

        atn = new ATN(modulesProvider);

        mockAlreadyOnBoarded();

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        //should be dropped by rate limiter
        atn.onMessageReceived(InstrumentationRegistry.getTargetContext());
        sleep(1010);

        atn.onMessageReceived(InstrumentationRegistry.getTargetContext());

        verify(mockATNServer, timeout(1000).times(1)).receiveATN(PUBLIC_ADDRESS);
        verifyNoMoreInteractions(mockATNServer);
    }

    @Test
    public void sentAndReceived() throws Exception {
        ATNServer mockATNServer = mock(ATNServer.class);
        TestModulesProvider modulesProvider = new TestModulesProvider(InstrumentationRegistry.getTargetContext(), mockServerUrl, mockKinAccount,
                mockATNServer);

        atn = new ATN(modulesProvider);

        mockAlreadyOnBoarded();
        mockEnabledConfiguration(); //for send transaction request

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        //should be dropped by rate limiter
        atn.onMessageReceived(InstrumentationRegistry.getTargetContext());
        sleep(1010);

        atn.onMessageReceived(InstrumentationRegistry.getTargetContext());
        sleep(1010);
        atn.onMessageSent(InstrumentationRegistry.getTargetContext());

        InOrder inOrder = inOrder(mockKinAccount, mockATNServer);
        inOrder.verify(mockATNServer, timeout(1000).times(1))
                .receiveATN(PUBLIC_ADDRESS);
        inOrder.verify(mockKinAccount, timeout(1000).times(1))
                .sendTransactionSync(anyString(), anyString(), (BigDecimal) any());
    }

    @Test
    public void receivedAndSent() throws Exception {
        ATNServer mockATNServer = mock(ATNServer.class);
        TestModulesProvider modulesProvider = new TestModulesProvider(InstrumentationRegistry.getTargetContext(), mockServerUrl, mockKinAccount,
                mockATNServer);

        atn = new ATN(modulesProvider);

        mockAlreadyOnBoarded();
        mockEnabledConfiguration(); //for send transaction request

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        sleep(1010);

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        sleep(1010);
        atn.onMessageReceived(InstrumentationRegistry.getTargetContext());

        InOrder inOrder = inOrder(mockKinAccount, mockATNServer);
        inOrder.verify(mockKinAccount, timeout(1000).times(1))
                .sendTransactionSync(anyString(), anyString(), (BigDecimal) any());
        inOrder.verify(mockATNServer, timeout(1000).times(1))
                .receiveATN(PUBLIC_ADDRESS);
    }

    @Test
    public void rateLimit() throws Exception {
        ATNServer mockATNServer = mock(ATNServer.class);
        TestModulesProvider modulesProvider = new TestModulesProvider(InstrumentationRegistry.getTargetContext(), mockServerUrl, mockKinAccount,
                mockATNServer);

        atn = new ATN(modulesProvider);
        //Mock onboarding with rate limit value
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\n" +
                        "     \"enabled\" : true,\n" +
                        "     \"target_wallet_address\": \"GBNU4TLYIQOQBM3PT32Z3CCYSMI6CDK7FXQR6R5DYB52GUPXES2S6XTU\",\n" +
                        "     \"transaction_lapse\": 1\n" +
                        "}")
                .setResponseCode(200));
        when(mockKinAccount.getBalanceSync()).thenReturn(new Balance() {
            @Override
            public BigDecimal value() {
                return new BigDecimal(10);
            }

            @Override
            public String value(int precision) {
                return "10.0";
            }
        });

        mockEnabledConfiguration(); //for send transaction request

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        sleep(1010);

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        atn.onMessageReceived(InstrumentationRegistry.getTargetContext());
        atn.onMessageReceived(InstrumentationRegistry.getTargetContext());

        verify(mockKinAccount, timeout(1000).times(1))
                .sendTransactionSync(anyString(), anyString(), (BigDecimal) any());
        verifyZeroInteractions(mockATNServer);

        sleep(1010);
        atn.onMessageReceived(InstrumentationRegistry.getTargetContext());
        verify(mockATNServer, timeout(1000).times(1))
                .receiveATN(PUBLIC_ADDRESS);
    }

}
