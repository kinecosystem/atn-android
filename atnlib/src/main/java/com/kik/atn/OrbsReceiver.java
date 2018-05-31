package com.kik.atn;


import java.io.IOException;

class OrbsReceiver {

    private final ATNServer atnServer;
    private final String publicKey;
    private final EventLogger eventLogger;

    OrbsReceiver(ATNServer atnServer, EventLogger eventLogger, String publicKey) {
        this.atnServer = atnServer;
        this.publicKey = publicKey;
        this.eventLogger = eventLogger;
    }

    void receiveOrbs() {
        eventLogger.sendOrbsEvent("claim_orbs_started");
        try {
            atnServer.receiveOrbs(publicKey);
            eventLogger.sendOrbsEvent("claim_orbs_succeeded");
        } catch (IOException e) {
            eventLogger.sendOrbsErrorEvent("claim_orbs_failed", e);
        }
    }
}
