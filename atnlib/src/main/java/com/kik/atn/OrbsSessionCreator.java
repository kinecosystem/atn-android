package com.kik.atn;


import android.os.Build;

import java.math.BigDecimal;

import static com.kik.atn.Events.ACCOUNT_FUNDING_FAILED;
import static com.kik.atn.Events.ACCOUNT_FUNDING_SUCCEEDED;
import static com.kik.atn.Events.ONBOARD_ACCOUNT_NOT_FUNDED;
import static com.kik.atn.Events.ONBOARD_FAILED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_FAILED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_STARTED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_SUCCEEDED;
import static com.kik.atn.Events.ONBOARD_STARTED;
import static com.kik.atn.Events.ONBOARD_SUCCEEDED;

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
                orbsSender = new OrbsSender(orbsWallet, eventLogger, configurationProvider);
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
        eventLogger.sendOrbsEvent(ONBOARD_STARTED);
        if (createAccount() && fundAccount()) {
            eventLogger.sendOrbsEvent(ONBOARD_SUCCEEDED);
            return true;
        }
        eventLogger.sendOrbsEvent(ONBOARD_FAILED);
        return false;

    }

    private boolean createAccount() {
        eventLogger.sendOrbsEvent(ONBOARD_LOAD_WALLET_STARTED);
        try {
            orbsWallet.loadWallet();
        } catch (Exception e) {
            eventLogger.sendOrbsErrorEvent(ONBOARD_LOAD_WALLET_FAILED, e);
            return false;
        }
        eventLogger.setOrbsPublicAddress(orbsWallet.getPublicAddress());
        eventLogger.sendOrbsEvent(ONBOARD_LOAD_WALLET_SUCCEEDED);
        return true;
    }

    private boolean fundAccount() {
        if (orbsWallet.getBalance().compareTo(new BigDecimal("0.0")) <= 0) {
            eventLogger.sendOrbsEvent(ONBOARD_ACCOUNT_NOT_FUNDED);
            try {
                orbsWallet.fundAccount();
                eventLogger.sendOrbsEvent(ACCOUNT_FUNDING_SUCCEEDED);
                return true;
            } catch (Exception e) {
                eventLogger.sendOrbsErrorEvent(ACCOUNT_FUNDING_FAILED, e);
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
