package com.kik.atn;


import java.io.IOException;

class OrbsReceiver {

    private final ATNServer atnServer;
    private final String publicKey;
    private final EventLogger eventLogger;
    private final ConfigurationProvider configProvider;

    OrbsReceiver(ATNServer atnServer, EventLogger eventLogger, ConfigurationProvider configProvider, String publicKey) {
        this.atnServer = atnServer;
        this.configProvider = configProvider;
        this.publicKey = publicKey;
        this.eventLogger = eventLogger;
    }

    void receiveOrbs() {
        Config config = configProvider.getConfig(publicKey);

        if (config.orbs().isEnabled()) {
            eventLogger.sendOrbsEvent("claim_orbs_started");
            try {
                atnServer.receiveORBS(publicKey);
                eventLogger.sendOrbsEvent("claim_orbs_succeeded");
            } catch (IOException e) {
                eventLogger.sendOrbsErrorEvent("claim_orbs_failed", e);
            }
        } else {
            eventLogger.log("receiveOrbs - disabled by configuration");
        }
    }
}
