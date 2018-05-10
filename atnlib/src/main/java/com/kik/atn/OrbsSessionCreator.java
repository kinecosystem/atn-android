package com.kik.atn;


import java.io.IOException;

class OrbsSessionCreator {

    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private final KinAccountCreator kinAccountCreator;
    private final ConfigurationProvider configurationProvider;
    private OrbsSender orbsSender;
    private OrbsReceiver orbsReceiver;
    private String orbsPublicAddress;

    OrbsSessionCreator(EventLogger eventLogger, ATNServer atnServer, KinAccountCreator kinAccountCreator,
                       ConfigurationProvider configurationProvider) {
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
        this.kinAccountCreator = kinAccountCreator;
        this.configurationProvider = configurationProvider;
    }

    boolean create() {
        String publicAddress = kinAccountCreator.getAccount().getPublicAddress();
        if (configurationProvider.getConfig(publicAddress).orbs().isEnabled() &&
                onboard()) {
            orbsSender = new OrbsSender(orbsPublicAddress, eventLogger, configurationProvider);
            orbsReceiver = new OrbsReceiver(atnServer, eventLogger, configurationProvider, orbsPublicAddress);
            return true;
        }
        return false;
    }

    private boolean onboard() {
        eventLogger.sendOrbsEvent("onboard_started");
        if (createAccount()) {
            if (fundAccount()) {
                eventLogger.sendOrbsEvent("onboard_succeeded");
                return true;
            }
        }
        eventLogger.sendOrbsEvent("onboard_failed");
        return false;

    }

    private boolean createAccount() {
        eventLogger.sendEvent("onboard_account_not_created");
        orbsPublicAddress = "";
        eventLogger.setOrbsPublicAddress(orbsPublicAddress);
        eventLogger.sendEvent("account_creation_succeeded");
        return true;
    }

    private boolean fundAccount() {
        eventLogger.sendOrbsEvent("onboard_account_not_funded");
        try {
            atnServer.fundOrbsAccount(orbsPublicAddress);
            eventLogger.sendOrbsEvent("account_funding_succeeded");
            return true;
        } catch (IOException e) {
            eventLogger.sendOrbsErrorEvent("account_funding_failed", e);
            return false;
        }
    }

    OrbsSender getOrbsSender() {
        return orbsSender;
    }

    OrbsReceiver getOrbsReceiver() {
        return orbsReceiver;
    }
}
