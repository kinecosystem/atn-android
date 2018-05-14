package com.kik.atn;


import java.math.BigDecimal;

class OrbsSender {

    private final OrbsWallet orbsWallet;
    private final String publicAddress;
    private final EventLogger eventLogger;
    private final ConfigurationProvider configProvider;

    OrbsSender(OrbsWallet orbsWallet, EventLogger eventLogger, ConfigurationProvider configProvider) {
        this.orbsWallet = orbsWallet;
        this.publicAddress = orbsWallet.getPublicAddress();
        this.eventLogger = eventLogger;
        this.configProvider = configProvider;
    }

    void sendOrbs() {
        Config config = configProvider.getConfig(publicAddress);

        if (config.orbs().isEnabled()) {
            eventLogger.sendOrbsEvent("send_orbs_started");
            try {
                orbsWallet.sendOrbs(config.orbs().getServerAccountAddress(), BigDecimal.ONE);
                eventLogger.sendOrbsEvent("send_orbs_succeeded");
            } catch (Exception ex) {
                eventLogger.sendOrbsErrorEvent("send_orbs_failed", ex);
            }
        } else {
            eventLogger.log("sendOrbs - disabled by configuration");
        }
    }
}
