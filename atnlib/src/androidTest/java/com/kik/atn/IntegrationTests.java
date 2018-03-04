package com.kik.atn;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import kin.core.KinAccount;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static java.lang.Thread.sleep;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class IntegrationTests {


    @Mock
    private KinAccount mockKinAccount;
    private MockWebServer mockWebServer;
    private ATN atn;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mockKinAccount.getPublicAddress()).thenReturn("GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV");

        Context appContext = InstrumentationRegistry.getTargetContext();
        mockWebServer = new MockWebServer();
        String url = mockWebServer.url("").toString();
        TestModulesProvider modulesProvider = new TestModulesProvider(appContext, url, mockKinAccount);

        atn = new ATN(modulesProvider);
    }

    @Test
    public void init_WithOnboarding() throws Exception {
        //configuration
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\n" +
                        "     \"enabled\" : true,\n" +
                        "     \"target_wallet_address\": \"GBNU4TLYIQOQBM3PT32Z3CCYSMI6CDK7FXQR6R5DYB52GUPXES2S6XTU\"\n" +
                        "}")
                .setResponseCode(200));
        //create account
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        //fund with ATN
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        atn.onMessageSent(InstrumentationRegistry.getTargetContext());
        sleep(3000);

        atn.onMessageSent(InstrumentationRegistry.getTargetContext());

        verify(mockKinAccount, timeout(1000).times(1)).sendTransactionSync(anyString(), anyString(), (BigDecimal) any());
    }


}
