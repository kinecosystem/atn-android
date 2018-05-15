package com.kik.atn;


import android.os.Build;

import java.math.BigDecimal;

import static com.kik.atn.Events.ACCOUNT_FUNDING_FAILED;
import static com.kik.atn.Events.ACCOUNT_FUNDING_SUCCEEDED;
import static com.kik.atn.Events.ONBOARD_ACCOUNT_NOT_FUNDED;
import static com.kik.atn.Events.ONBOARD_CREATE_WALLET_FAILED;
import static com.kik.atn.Events.ONBOARD_CREATE_WALLET_STARTED;
import static com.kik.atn.Events.ONBOARD_CREATE_WALLET_SUCCEEDED;
import static com.kik.atn.Events.ONBOARD_FAILED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_FAILED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_STARTED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_SUCCEEDED;
import static com.kik.atn.Events.ONBOARD_STARTED;
import static com.kik.atn.Events.ONBOARD_SUCCEEDED;
import static com.kik.atn.Events.SESSION_CREATION_FAILED;
import static com.kik.atn.Events.SESSION_CREATION_STARTED;
import static com.kik.atn.Events.SESSION_CREATION_SUCCEEDED;

class OrbsSessionCreator {

    private final OrbsWallet orbsWallet;
    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private final KinAccountCreator kinAccountCreator;
    private final ConfigurationProvider configurationProvider;
    private OrbsSender orbsSender;
    private OrbsReceiver orbsReceiver;
    private boolean isWalletCreated;
    private boolean isFunded;

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
            isWalletCreated = orbsWallet.isWalletCreated();
            isFunded = isFunded();
            if (isWalletCreated && isFunded) {
                if (createSession()) {
                    createOrbsTxHandlers();
                    return true;
                }
            } else {
                if (onboard()) {
                    createOrbsTxHandlers();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean createSession() {
        eventLogger.sendOrbsEvent(SESSION_CREATION_STARTED);
        try {
            orbsWallet.loadWallet();
        } catch (Exception e) {
            eventLogger.sendOrbsErrorEvent(SESSION_CREATION_FAILED, e);
            return false;
        }
        eventLogger.setOrbsPublicAddress(orbsWallet.getPublicAddress());
        eventLogger.sendOrbsEvent(SESSION_CREATION_SUCCEEDED);
        return true;
    }

    private void createOrbsTxHandlers() {
        orbsSender = new OrbsSender(orbsWallet, eventLogger, configurationProvider);
        orbsReceiver = new OrbsReceiver(atnServer, eventLogger, configurationProvider, orbsWallet.getPublicAddress());
    }

    private boolean isEnable(String publicAddress) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                configurationProvider.getConfig(publicAddress).orbs().isEnabled();
    }

    private boolean onboard() {
        eventLogger.sendOrbsEvent(ONBOARD_STARTED);
        if (createAccountIfNeeded() && fundAccountIdNeeded()) {
            eventLogger.sendOrbsEvent(ONBOARD_SUCCEEDED);
            return true;
        }
        eventLogger.sendOrbsEvent(ONBOARD_FAILED);
        return false;

    }

    private boolean createAccountIfNeeded() {
        if (!isWalletCreated) {
            eventLogger.sendOrbsEvent(ONBOARD_CREATE_WALLET_STARTED);
            try {
                orbsWallet.createWallet();
            } catch (Exception e) {
                eventLogger.sendOrbsErrorEvent(ONBOARD_CREATE_WALLET_FAILED, e);
                return false;
            }
            eventLogger.setOrbsPublicAddress(orbsWallet.getPublicAddress());
            eventLogger.sendOrbsEvent(ONBOARD_CREATE_WALLET_SUCCEEDED);
            return true;
        } else {
            try {
                eventLogger.sendOrbsEvent(ONBOARD_LOAD_WALLET_STARTED);
                orbsWallet.loadWallet();
            } catch (Exception e) {
                eventLogger.sendOrbsErrorEvent(ONBOARD_LOAD_WALLET_FAILED, e);
                return false;
            }
            eventLogger.setOrbsPublicAddress(orbsWallet.getPublicAddress());
            eventLogger.sendOrbsEvent(ONBOARD_LOAD_WALLET_SUCCEEDED);
            return true;
        }
    }

    private boolean fundAccountIdNeeded() {
        if (!isFunded) {
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

    private boolean isFunded() {
        return orbsWallet.getBalance().compareTo(new BigDecimal("0.0")) > 0;
    }

    OrbsSender getOrbsSender() {
        return orbsSender;
    }

    OrbsReceiver getOrbsReceiver() {
        return orbsReceiver;
    }
}
