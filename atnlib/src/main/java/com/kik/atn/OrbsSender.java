package com.kik.atn;


class OrbsSender {

    private final String publicAddress;
    private final EventLogger eventLogger;
    private final ConfigurationProvider configProvider;

    OrbsSender(String publicAddress, EventLogger eventLogger, ConfigurationProvider configProvider) {
        this.publicAddress = publicAddress;
        this.eventLogger = eventLogger;
        this.configProvider = configProvider;
    }

    void sendOrbs() {
        Config config = configProvider.getConfig(publicAddress);

        if (config.orbs().isEnabled()) {
            eventLogger.sendOrbsEvent("send_orbs_started");
            try {
                //TODO send orbs
                eventLogger.sendOrbsEvent("send_orbs_succeeded");
            } catch (Exception ex) {
                eventLogger.sendOrbsErrorEvent("send_orbs_failed", ex);
            }
        } else {
            eventLogger.log("sendOrbs - disabled by configuration");
        }
    }
}
