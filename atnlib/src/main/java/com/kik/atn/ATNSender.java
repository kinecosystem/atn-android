package com.kik.atn;


import java.math.BigDecimal;

import kin.core.KinAccount;

class ATNSender {

    private final KinAccount account;
    private final EventLogger eventLogger;
    private final ConfigurationProvider configProvider;

    ATNSender(KinAccount account, EventLogger eventLogger, ConfigurationProvider configProvider) {
        this.account = account;
        this.eventLogger = eventLogger;
        this.configProvider = configProvider;
    }

    void sendATN() {
        Config config = configProvider.getConfig(account.getPublicAddress());

        if (config.isEnabled()) {
            eventLogger.sendEvent("send_atn_started");
            try {
                EventLogger.DurationLogger durationLogger = eventLogger.startDurationLogging();
                account.sendTransactionSync(config.getAtnAddress(), new BigDecimal(1.0));
                durationLogger.report("send_atn_succeeded");
            } catch (Exception ex) {
                eventLogger.sendErrorEvent("send_atn_failed", ex);
            }
        } else {
            eventLogger.log("sendATN - disabled by configuration");
        }
    }
}
