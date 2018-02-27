package com.kik.atn;


import java.math.BigDecimal;
import java.util.List;

import kin.core.KinAccount;
import kin.core.exception.TransactionFailedException;

class ATNSender {

    private final KinAccount account;
    private final String atnAddress;
    private final EventLogger eventLogger;

    ATNSender(KinAccount account, String atnAddress, EventLogger eventLogger) {
        this.account = account;
        this.atnAddress = atnAddress;
        this.eventLogger = eventLogger;
    }

    void sendATN() {
        eventLogger.sendEvent("send_atn_started");
        try {
            long start = System.nanoTime();
            account.sendTransactionSync(atnAddress, "", new BigDecimal(1.0));
            long duration = (System.nanoTime() - start) / 1000;
            eventLogger.sendDurationEvent("send_atn_succeed", duration);
        } catch (Exception ex) {
            handleUnderfundedError(ex);
            eventLogger.sendErrorEvent("send_atn_failed", ex);
        }
    }

    private void handleUnderfundedError(Exception ex) {
        if (ex instanceof TransactionFailedException) {
            TransactionFailedException tfe = (TransactionFailedException) ex;
            List<String> resultCodes = tfe.getOperationsResultCodes();
            if (resultCodes != null && resultCodes.size() > 0 && "underfunded".equals(resultCodes.get(0))) {
                eventLogger.sendEvent("underfunded");
            }
        }
    }
}
