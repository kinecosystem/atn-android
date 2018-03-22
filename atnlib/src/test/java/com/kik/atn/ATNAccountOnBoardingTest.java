package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.math.BigDecimal;

import kin.core.Balance;
import kin.core.KinAccount;
import kin.core.exception.AccountNotActivatedException;
import kin.core.exception.AccountNotFoundException;
import kin.core.exception.OperationFailedException;

import static java.lang.Thread.sleep;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.ArgumentMatchers.anyLong;
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
        doThrow(new AccountNotFoundException("")).when(mockKinAccount).getBalanceSync();

        assertFalse(onBoarding.onBoard(mockKinAccount));
        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendEvent("onboard_account_not_created");
        verify(mockEventLogger).sendErrorEvent("account_creation_failed", expectedException);
    }

    @Test
    public void onBoard_ActivateAccountFailed_OnBoardingFailure() throws Exception {
        OperationFailedException expectedException = new OperationFailedException("some error");
        doThrow(expectedException).when(mockKinAccount).activateSync("");
        doThrow(new AccountNotFoundException("")).when(mockKinAccount).getBalanceSync();

        assertFalse(onBoarding.onBoard(mockKinAccount));
        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendEvent("onboard_account_not_created");
        verify(mockEventLogger).sendErrorEvent("trustline_setup_failed", expectedException);
    }

    @Test
    public void onBoard_AccountNotActivated_OnBoardingSuccess() throws Exception {
        OperationFailedException expectedException = new OperationFailedException("some error");
        doThrow(expectedException).when(mockKinAccount).activateSync("");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(mockKinAccount).activateSync("");
        doThrow(new AccountNotActivatedException("")).when(mockKinAccount).getBalanceSync();

        assertTrue(onBoarding.onBoard(mockKinAccount));
        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendEvent("onboard_trustline_not_set");
        verify(mockEventLogger).sendDurationEvent(eq("onboard_succeeded"), anyLong());
    }

    @Test
    public void onBoard_FundWIthATNFailed_OnBoardingFailure() throws Exception {
        IOException expectedException = new IOException("some error");
        doThrow(expectedException).when(mockAtnServer).fundWithATN(PUBLIC_ADDRESS);
        doThrow(new AccountNotFoundException("")).when(mockKinAccount).getBalanceSync();

        assertFalse(onBoarding.onBoard(mockKinAccount));
        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendEvent("onboard_account_not_created");
        verify(mockEventLogger).sendErrorEvent("account_funding_failed", expectedException);
    }

    @Test
    public void onBoard_NotFunded_OnBoardingSuccess() throws Exception {
        IOException expectedException = new IOException("some error");
        doThrow(expectedException).when(mockAtnServer).fundWithATN(PUBLIC_ADDRESS);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(mockAtnServer).fundWithATN(PUBLIC_ADDRESS);
        doThrow(new AccountNotFoundException("")).when(mockKinAccount).getBalanceSync();


        assertTrue(onBoarding.onBoard(mockKinAccount));
        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendEvent("onboard_account_not_created");
        verify(mockEventLogger).sendDurationEvent(eq("onboard_succeeded"), anyLong());
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
        doThrow(new AccountNotFoundException("")).when(mockKinAccount).getBalanceSync();

        assertTrue(onBoarding.onBoard(mockKinAccount));

        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendEvent("onboard_account_not_created");
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(mockEventLogger).sendDurationEvent(eq("onboard_succeeded"), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue(), greaterThan(1000L));
        assertThat(argumentCaptor.getValue(), lessThan(1200L));
    }

    @Test
    public void onBoard_AlreadyOnboarded() throws Exception {
        when(mockKinAccount.getBalanceSync()).thenReturn(new Balance() {
            @Override
            public BigDecimal value() {
                return new BigDecimal(10);
            }

            @Override
            public String value(int precision) {
                return "10";
            }
        });

        assertTrue(onBoarding.onBoard(mockKinAccount));

        verify(mockEventLogger).sendEvent("onboard_started");
        verify(mockEventLogger).sendEvent(eq("onboard_already_onboarded"));
    }

}