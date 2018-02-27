package com.kik.atn;


import kin.core.KinAccount;

class ATNSessionCreator {

    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private final KinAccountCreator kinAccountCreator;
    private final ConfigurationProvider configurationProvider;
    private final ATNAccountOnBoarding accountOnBoarding;
    private ATNSender atnSender;
    private ATNReceiver afnReceiver;

    ATNSessionCreator(EventLogger eventLogger, ATNServer atnServer, KinAccountCreator kinAccountCreator,
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
            String publicAddress = account.getPublicAddress();
            eventLogger.setPublicAddress(publicAddress);
            configurationProvider.init(publicAddress);
            if (configurationProvider.enabled() && accountOnBoarding.onBoard(account)) {
                atnSender = new ATNSender(account, eventLogger, configurationProvider.ATNAddress());
                afnReceiver = new ATNReceiver(atnServer, eventLogger, publicAddress);
                return true;
            }
        }
        return false;
    }

    ATNSender getATNSender() {
        return atnSender;
    }

    ATNReceiver getATNReceiver() {
        return afnReceiver;
    }
}
