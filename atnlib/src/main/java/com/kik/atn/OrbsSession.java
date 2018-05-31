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
import static com.kik.atn.Events.ONBOARD_IS_FUNDED_FAILED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_FAILED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_STARTED;
import static com.kik.atn.Events.ONBOARD_LOAD_WALLET_SUCCEEDED;
import static com.kik.atn.Events.ONBOARD_STARTED;
import static com.kik.atn.Events.ONBOARD_SUCCEEDED;

class OrbsSession {

    private final OrbsWallet orbsWallet;
    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private final KinAccountCreator kinAccountCreator;
    private final ConfigurationProvider configurationProvider;
    private OrbsSender orbsSender;
    private OrbsReceiver orbsReceiver;
    private boolean isCreated;
    private String publicAddress;

    OrbsSession(OrbsWallet orbsWallet, EventLogger eventLogger, ATNServer atnServer, KinAccountCreator kinAccountCreator,
                ConfigurationProvider configurationProvider) {
        this.orbsWallet = orbsWallet;
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
        this.kinAccountCreator = kinAccountCreator;
        this.configurationProvider = configurationProvider;
    }

    boolean create() {
        publicAddress = kinAccountCreator.getAccount().getPublicAddress();
        if (isEnable(publicAddress)) {
            if (onboard()) {
                orbsSender = new OrbsSender(orbsWallet, eventLogger);
                orbsReceiver = new OrbsReceiver(atnServer, eventLogger, orbsWallet.getPublicAddress());
                isCreated = true;
            }
        }
        return isCreated;
    }

    private boolean isEnable(String publicAddress) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                configurationProvider.getConfig(publicAddress).orbs().isEnabled();
    }

    private boolean onboard() {
        eventLogger.sendOrbsEvent(ONBOARD_STARTED);
        if (loadOrCreateAccount() && fundAccount()) {
            eventLogger.sendOrbsEvent(ONBOARD_SUCCEEDED);
            return true;
        }
        eventLogger.sendOrbsEvent(ONBOARD_FAILED);
        return false;

    }

    private boolean loadOrCreateAccount() {
        if (orbsWallet.isWalletCreated()) {
            return loadWallet();
        } else {
            return createWallet();
        }
    }

    private boolean createWallet() {
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
    }

    private boolean loadWallet() {
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

    private boolean fundAccount() {
        try {
            if (!isFunded()) {
                eventLogger.sendOrbsEvent(ONBOARD_ACCOUNT_NOT_FUNDED);
                try {
                    String txId = orbsWallet.fundAccount();
                    eventLogger.sendOrbsEvent(ACCOUNT_FUNDING_SUCCEEDED, txId);
                    return true;
                } catch (Exception e) {
                    eventLogger.sendOrbsErrorEvent(ACCOUNT_FUNDING_FAILED, e);
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            eventLogger.sendOrbsErrorEvent(ONBOARD_IS_FUNDED_FAILED, e);
            return false;
        }
    }

    private boolean isFunded() throws Exception {
        return orbsWallet.getBalance().compareTo(new BigDecimal("0.0")) > 0;
    }

    boolean isCreated() {
        return isCreated;
    }

    void receiveOrbs() {
        if (orbsReceiver != null) {
            orbsReceiver.receiveOrbs();
        }
    }

    void sendOrbs() {
        if (orbsSender != null) {
            orbsSender.sendOrbs(configurationProvider.getConfig(publicAddress).orbs().getServerAccountAddress());
        }
    }
}
