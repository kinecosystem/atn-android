package com.kik.atn;


import java.io.IOException;

class ATNReceiver {

    private final ATNServer atnServer;
    private final String publicKey;
    private final EventLogger eventLogger;
    private final ConfigurationProvider configProvider;

    ATNReceiver(ATNServer atnServer, EventLogger eventLogger, ConfigurationProvider configProvider, String publicKey) {
        this.atnServer = atnServer;
        this.configProvider = configProvider;
        this.publicKey = publicKey;
        this.eventLogger = eventLogger;
    }

    void receiveATN() {
        Config config = configProvider.getConfig(publicKey);

        if (config.isEnabled()) {
            eventLogger.sendEvent("claim_atn_started");
            try {
                atnServer.receiveATN(publicKey);
                eventLogger.sendEvent("claim_atn_succeeded");
            } catch (IOException e) {
                eventLogger.sendErrorEvent("claim_atn_failed", e);
            }
        } else {
            eventLogger.log("receiveATN - disabled by configuration");
        }
    }
}
