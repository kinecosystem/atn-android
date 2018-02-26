package com.kik.atn;


import java.math.BigDecimal;

import kin.core.KinAccount;

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
        eventLogger.sendEvent("sendATN", null);
        try {
            long start = System.nanoTime();
            account.sendTransactionSync(atnAddress, "", new BigDecimal(1.0));
            long duration = (System.nanoTime() - start) / 1000;
            eventLogger.sendDurationEvent("sendATNSucceed", duration);
        } catch (Exception ex) {
            eventLogger.sendErrorEvent("sendATNFailed", ex);
        }
    }
}
