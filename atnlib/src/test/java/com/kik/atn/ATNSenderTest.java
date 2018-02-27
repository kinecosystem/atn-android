package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import kin.core.KinAccount;
import kin.core.TransactionId;
import kin.core.exception.OperationFailedException;
import kin.core.exception.TransactionFailedException;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ATNSenderTest {

    private static final String ATN_ADDRESS = "GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV";
    private static final TransactionId dummyTransactionId = new TransactionId() {
        @Override
        public String id() {
            return "1dd0051db7beaf02507ca0d95381bc44ce0f387c2ade3f8aa9a20f3101f13f76";
        }
    };
    @Mock
    private KinAccount mockKinAccount;
    @Mock
    private EventLogger mockEventLogger;
    private ATNSender sender;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        sender = new ATNSender(mockKinAccount, mockEventLogger, ATN_ADDRESS);
        when(mockEventLogger.startDurationLogging()).thenCallRealMethod();
    }

    @Test
    public void sendATN_Success() throws Exception {
        when(mockKinAccount.sendTransactionSync(anyString(), anyString(), (BigDecimal) any()))
                .thenReturn(dummyTransactionId);

        sender.sendATN();

        verify(mockKinAccount).sendTransactionSync(ATN_ADDRESS, "", new BigDecimal("1"));
        verify(mockEventLogger).sendEvent("send_atn_started");
        verify(mockEventLogger).sendDurationEvent(eq("send_atn_succeed"), anyLong());
    }

    @Test
    public void sendAFN_SuccessfulTransaction_ReportDuration() throws Exception {

        when(mockKinAccount.sendTransactionSync(anyString(), anyString(), (BigDecimal) any()))
                .thenAnswer(new Answer<TransactionId>() {
                    @Override
                    public TransactionId answer(InvocationOnMock invocation) throws Throwable {
                        sleep(1000);
                        return dummyTransactionId;
                    }
                });

        sender.sendATN();

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(mockEventLogger).sendDurationEvent(eq("send_atn_succeed"), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue(), greaterThan(1000L));
        assertThat(argumentCaptor.getValue(), lessThan(1200L));
    }

    @Test
    public void sendATN_SendTransactionFailure_ReportFailure() throws Exception {
        OperationFailedException expectedException = new OperationFailedException("some error");
        when(mockKinAccount.sendTransactionSync(anyString(), anyString(), (BigDecimal) any()))
                .thenThrow(expectedException);

        sender.sendATN();

        verify(mockKinAccount).sendTransactionSync(ATN_ADDRESS, "", new BigDecimal("1"));
        verify(mockEventLogger).sendErrorEvent("send_atn_failed", expectedException);
    }

    @Test
    public void sendATN_SendTransactionNotEnoughFunds_Report() throws Exception {
        OperationFailedException expectedException = new TransactionFailedException(
                "op_failed", Collections.singletonList("underfunded")
        );
        when(mockKinAccount.sendTransactionSync(anyString(), anyString(), (BigDecimal) any()))
                .thenThrow(expectedException);

        sender.sendATN();

        verify(mockKinAccount).sendTransactionSync(ATN_ADDRESS, "", new BigDecimal("1"));
        verify(mockEventLogger).sendEvent("underfunded");
    }
}
