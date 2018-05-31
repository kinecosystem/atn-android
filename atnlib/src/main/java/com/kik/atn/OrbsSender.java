package com.kik.atn;


import java.math.BigDecimal;

import static com.kik.atn.Events.SEND_ORBS_FAILED;
import static com.kik.atn.Events.SEND_ORBS_STARTED;
import static com.kik.atn.Events.SEND_ORBS_SUCCEEDED;

class OrbsSender {


    private final OrbsWallet orbsWallet;
    private final EventLogger eventLogger;

    OrbsSender(OrbsWallet orbsWallet, EventLogger eventLogger) {
        this.orbsWallet = orbsWallet;
        this.eventLogger = eventLogger;
    }

    void sendOrbs(String targetAddress) {
        eventLogger.sendOrbsEvent(SEND_ORBS_STARTED);
        try {
            String txId = orbsWallet.sendOrbs(targetAddress, BigDecimal.ONE);
            eventLogger.sendOrbsEvent(SEND_ORBS_SUCCEEDED, txId);
        } catch (Exception ex) {
            eventLogger.sendOrbsErrorEvent(SEND_ORBS_FAILED, ex);
        }
    }
}
