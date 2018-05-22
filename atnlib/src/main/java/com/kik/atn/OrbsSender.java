package com.kik.atn;


import java.math.BigDecimal;

class OrbsSender {

    private final OrbsWallet orbsWallet;
    private final EventLogger eventLogger;

    OrbsSender(OrbsWallet orbsWallet, EventLogger eventLogger) {
        this.orbsWallet = orbsWallet;
        this.eventLogger = eventLogger;
    }

    void sendOrbs(String targetAddress) {
        eventLogger.sendOrbsEvent("send_orbs_started");
        try {
            orbsWallet.sendOrbs(targetAddress, BigDecimal.ONE);
            eventLogger.sendOrbsEvent("send_orbs_succeeded");
        } catch (Exception ex) {
            eventLogger.sendOrbsErrorEvent("send_orbs_failed", ex);
        }
    }
}
