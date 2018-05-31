package com.kik.atn;


import java.math.BigDecimal;

import kin.core.KinAccount;

class ATNSender {

    private final KinAccount account;
    private final EventLogger eventLogger;

    ATNSender(KinAccount account, EventLogger eventLogger) {
        this.account = account;
        this.eventLogger = eventLogger;
    }

    void sendATN(String targetAddress) {
        eventLogger.sendEvent("send_atn_started");
        try {
            EventLogger.DurationLogger durationLogger = eventLogger.startDurationLogging();
            account.sendTransactionSync(targetAddress, new BigDecimal(1.0));
            durationLogger.report("send_atn_succeeded");
        } catch (Exception ex) {
            eventLogger.sendErrorEvent("send_atn_failed", ex);
        }
    }
}
