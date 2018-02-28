package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import kin.core.KinAccount;
import kin.core.exception.OperationFailedException;

import static java.lang.Thread.sleep;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ATNAccountOnBoardingTest {

    private final static String PUBLIC_ADDRESS = "GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV";
    @Mock
    private ATNServer mockAtnServer;
    @Mock
    private EventLogger mockEventLogger;
    @Mock
    private KinAccount mockKinAccount;

    private ATNAccountOnBoarding onBoarding;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        onBoarding = new ATNAccountOnBoarding(mockEventLogger, mockAtnServer);
        when(mockKinAccount.getPublicAddress()).thenReturn(PUBLIC_ADDRESS);
        when(mockEventLogger.startDurationLogging()).thenCallRealMethod();
    }

    @Test
    public void onBoard_FundWIthXLMFailed_OnBoardingFailure() throws Exception {
        IOException expectedException = new IOException("some error");
        doThrow(expectedException).when(mockAtnServer).fundWithXLM(PUBLIC_ADDRESS);

        assertFalse(onBoarding.onBoard(mockKinAccount));
        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendErrorEvent("fund_xlm_failed", expectedException);
    }

    @Test
    public void onBoard_ActivateAccountFailed_OnBoardingFailure() throws Exception {
        OperationFailedException expectedException = new OperationFailedException("some error");
        doThrow(expectedException).when(mockKinAccount).activateSync("");

        assertFalse(onBoarding.onBoard(mockKinAccount));
        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendErrorEvent("activate_failed", expectedException);
    }

    @Test
    public void onBoard_FundWIthATNFailed_OnBoardingFailure() throws Exception {
        IOException expectedException = new IOException("some error");
        doThrow(expectedException).when(mockAtnServer).fundWithATN(PUBLIC_ADDRESS);

        assertFalse(onBoarding.onBoard(mockKinAccount));
        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendErrorEvent("fund_xlm_failed", expectedException);
    }

    @Test
    public void onBoard_Success_ReportSuccess() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sleep(1000);
                return null;
            }
        }).when(mockAtnServer).fundWithXLM(PUBLIC_ADDRESS);

        assertTrue(onBoarding.onBoard(mockKinAccount));

        verify(mockEventLogger).sendEvent("onboard_started");
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(mockEventLogger).sendDurationEvent(eq("account_created"), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue(), greaterThan(1000L));
        assertThat(argumentCaptor.getValue(), lessThan(1200L));
    }

}