package com.kik.atn;


import java.math.BigDecimal;
import java.util.List;

import kin.core.KinAccount;
import kin.core.exception.TransactionFailedException;

class ATNSender {

    private final KinAccount account;
    private final String atnAddress;
    private final EventLogger eventLogger;

    ATNSender(KinAccount account, EventLogger eventLogger, String atnAddress) {
        this.account = account;
        this.atnAddress = atnAddress;
        this.eventLogger = eventLogger;
    }

    void sendATN() {
        eventLogger.sendEvent("send_atn_started");
        try {
            EventLogger.DurationLogger durationLogger = eventLogger.startDurationLogging();
            account.sendTransactionSync(atnAddress, "", new BigDecimal(1.0));
            durationLogger.report("send_atn_succeed");
        } catch (Exception ex) {
            if (ex instanceof TransactionFailedException) {
                reportUnderfundedError(ex);
            } else {
                eventLogger.sendErrorEvent("send_atn_failed", ex);
            }
        }
    }

    private void reportUnderfundedError(Exception ex) {
        TransactionFailedException tfe = (TransactionFailedException) ex;
        List<String> resultCodes = tfe.getOperationsResultCodes();
        if (resultCodes != null && resultCodes.size() > 0 && "underfunded".equals(resultCodes.get(0))) {
            eventLogger.sendEvent("underfunded");
        }
    }
}
