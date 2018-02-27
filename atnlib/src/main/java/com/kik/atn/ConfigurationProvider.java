package com.kik.atn;


import java.io.IOException;

class ConfigurationProvider {

    private final ATNServer server;
    private final EventLogger eventLogger;
    private Config config;

    ConfigurationProvider(ATNServer server, EventLogger eventLogger) {
        this.server = server;
        this.eventLogger = eventLogger;
    }

    boolean enabled() {
        return config.isEnabled();
    }

    String ATNAddress() {
        return config.getAtnAddress();
    }

    void init(String publicAddress) {
        try {
            config = server.getConfiguration(publicAddress);
        } catch (IOException e) {
            config = new Config(false, null);
            eventLogger.sendErrorEvent("get_config_failed", e);
        }
    }

}
