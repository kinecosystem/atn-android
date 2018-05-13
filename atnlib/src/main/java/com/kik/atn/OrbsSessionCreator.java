package com.kik.atn;


import android.os.Build;

import java.io.IOException;
import java.math.BigDecimal;

class OrbsSessionCreator {

    private final OrbsWallet orbsWallet;
    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private final KinAccountCreator kinAccountCreator;
    private final ConfigurationProvider configurationProvider;
    private OrbsSender orbsSender;
    private OrbsReceiver orbsReceiver;

    OrbsSessionCreator(OrbsWallet orbsWallet, EventLogger eventLogger, ATNServer atnServer, KinAccountCreator kinAccountCreator,
                       ConfigurationProvider configurationProvider) {
        this.orbsWallet = orbsWallet;
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
        this.kinAccountCreator = kinAccountCreator;
        this.configurationProvider = configurationProvider;
    }

    boolean create() {
        String publicAddress = kinAccountCreator.getAccount().getPublicAddress();
        if (isEnable(publicAddress)) {
            boolean onboardSucceeded = onboard();
            if (onboardSucceeded) {
                orbsSender = new OrbsSender(orbsWallet.getPublicAddress(), eventLogger, configurationProvider);
                orbsReceiver = new OrbsReceiver(atnServer, eventLogger, configurationProvider, orbsWallet.getPublicAddress());
                return true;
            }
        }
        return false;
    }

    private boolean isEnable(String publicAddress) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                configurationProvider.getConfig(publicAddress).orbs().isEnabled();
    }

    private boolean onboard() {
        eventLogger.sendOrbsEvent("onboard_started");
        if (createAccount() && fundAccount()) {
            eventLogger.sendOrbsEvent("onboard_succeeded");
            return true;
        }
        eventLogger.sendOrbsEvent("onboard_failed");
        return false;

    }

    private boolean createAccount() {
        eventLogger.sendEvent("onboard_load_wallet_started");
        try {
            orbsWallet.loadWallet();
        } catch (Exception e) {
            eventLogger.sendOrbsErrorEvent("onboard_load_wallet_failed", e);
            return false;
        }
        eventLogger.setOrbsPublicAddress(orbsWallet.getPublicAddress());
        eventLogger.sendEvent("onboard_load_wallet_succeeded");
        return true;
    }

    private boolean fundAccount() {
        if (orbsWallet.getBalance().compareTo(new BigDecimal("0.0")) < 0) {
            eventLogger.sendOrbsEvent("onboard_account_not_funded");
            try {
                atnServer.fundOrbsAccount(orbsWallet.getPublicAddress());
                eventLogger.sendOrbsEvent("account_funding_succeeded");
                return true;
            } catch (IOException e) {
                eventLogger.sendOrbsErrorEvent("account_funding_failed", e);
                return false;
            }
        } else {
            return true;
        }
    }

    OrbsSender getOrbsSender() {
        return orbsSender;
    }

    OrbsReceiver getOrbsReceiver() {
        return orbsReceiver;
    }
}
