package com.kik.atn;


import kin.core.KinAccount;

class ATNSessionCreator {

    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private final KinAccountCreator kinAccountCreator;
    private ATNSender atnSender;
    private ATNReceiver afnReceiver;

    ATNSessionCreator(EventLogger eventLogger, ATNServer atnServer, KinAccountCreator kinAccountCreator) {
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
        this.kinAccountCreator = kinAccountCreator;
    }

    boolean create() {
        KinAccount account = kinAccountCreator.getAccount();
        if (account != null) {
            String publicAddress = account.getPublicAddress();
            eventLogger.setPublicAddress(publicAddress);
            ConfigurationProvider configurationProvider = new ConfigurationProvider(atnServer, eventLogger, publicAddress);
            configurationProvider.init();
            if (configurationProvider.enabled()) {
                ATNAccountOnborading accountCreator = new ATNAccountOnborading(eventLogger, atnServer);
                if (accountCreator.onboard(account)) {
                    atnSender = new ATNSender(account, configurationProvider.ATNAddress(), eventLogger);
                    afnReceiver = new ATNReceiver(atnServer, eventLogger, publicAddress);
                    return true;
                }
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
