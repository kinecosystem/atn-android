package com.kik.atn;


import kin.core.KinAccount;

class ATNSession {

    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private final KinAccountCreator kinAccountCreator;
    private final ConfigurationProvider configurationProvider;
    private final ATNAccountOnBoarding accountOnBoarding;
    private ATNSender atnSender;
    private ATNReceiver atnReceiver;
    private boolean isCreated;
    private String publicAddress;

    ATNSession(EventLogger eventLogger, ATNServer atnServer, KinAccountCreator kinAccountCreator,
               ConfigurationProvider configurationProvider, ATNAccountOnBoarding accountOnBoarding) {
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
        this.kinAccountCreator = kinAccountCreator;
        this.configurationProvider = configurationProvider;
        this.accountOnBoarding = accountOnBoarding;
    }

    boolean create() {
        KinAccount account = kinAccountCreator.getAccount();
        if (account != null) {
            publicAddress = account.getPublicAddress();
            eventLogger.setPublicAddress(publicAddress);
            if (configurationProvider.getConfig(publicAddress).isEnabled() && accountOnBoarding.onBoard(account)) {
                atnSender = new ATNSender(account, eventLogger);
                atnReceiver = new ATNReceiver(atnServer, eventLogger, publicAddress);
                isCreated = true;
            }
        }
        return isCreated;
    }

    boolean isCreated() {
        return isCreated;
    }

    void receiveATN() {
        if (atnReceiver != null) {
            atnReceiver.receiveATN();
        }
    }

    void sendATN() {
        if (atnSender != null) {
            atnSender.sendATN(configurationProvider.getConfig(publicAddress).getAtnAddress());
        }
    }
}
