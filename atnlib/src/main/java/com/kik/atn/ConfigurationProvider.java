package com.kik.atn;


import java.io.IOException;

class ConfigurationProvider {

    private final ATNServer server;
    private final EventLogger eventLogger;
    private final long minUpdateIntervalMillis;
    private Config latestConfig = null;
    private long latestUpdate = 0;

    ConfigurationProvider(ATNServer server, EventLogger eventLogger, long minUpdateIntervalMillis) {
        this.server = server;
        this.eventLogger = eventLogger;
        this.minUpdateIntervalMillis = minUpdateIntervalMillis;
    }

    Config getConfig(String publicAddress) {
        long current = System.currentTimeMillis();
        if (current - latestUpdate > minUpdateIntervalMillis) {
            latestUpdate = current;
            latestConfig = fetchConfig(publicAddress);
        }
        return latestConfig;
    }

    private Config fetchConfig(String publicAddress) {
        try {
            return server.getConfiguration(publicAddress);
        } catch (IOException e) {
            eventLogger.sendErrorEvent("get_config_failed", e);
            return new Config(false, null);
        }
    }

}
