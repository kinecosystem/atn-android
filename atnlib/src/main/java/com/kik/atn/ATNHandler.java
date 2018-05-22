package com.kik.atn;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class ATNHandler extends Handler {

    private final ATNSession atnSession;
    private final OrbsSession orbsSession;
    private final ConfigurationProvider configurationProvider;
    private final Dispatcher dispatcher;
    private final AndroidLogger logger;
    private final String publicAddress;
    private final Dispatcher orbsDispatcher;
    private volatile boolean atnIsBusy;
    private volatile boolean orbsIsBusy;
    private Config currentConfig;

    ATNHandler(ATNSession atnSession, OrbsSession orbsSession,
               Dispatcher dispatcher, Dispatcher orbsDispatcher, ConfigurationProvider configurationProvider,
               AndroidLogger logger, String publicAddress, Looper looper) {
        super(looper);
        this.atnSession = atnSession;
        this.orbsSession = orbsSession;
        this.orbsDispatcher = orbsDispatcher;
        this.configurationProvider = configurationProvider;
        this.dispatcher = dispatcher;
        this.logger = logger;
        this.publicAddress = publicAddress;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Dispatcher.MSG_RECEIVE:
            case Dispatcher.MSG_SENT:
                atnMessagesHandling(msg);
                break;
            case Dispatcher.MSG_RECEIVE_ORBS:
            case Dispatcher.MSG_SENT_ORBS:
                orbsMessagesHandling(msg);
                break;
        }
    }

    private void atnMessagesHandling(Message msg) {
        atnIsBusy = true;
        currentConfig = configurationProvider.getConfig(publicAddress);
        if (currentConfig.isEnabled()) {
            if (msg.what == Dispatcher.MSG_RECEIVE) {
                atnMessageReceived();
            } else {
                atnMessageSent();
            }
        } else {
            logger.log(msg.what == Dispatcher.MSG_RECEIVE ? "MSG_RECEIVE - disabled by configuration" :
                    "MSG_SENT - disabled by configuration");
        }
        atnIsBusy = false;
    }

    private void orbsMessagesHandling(Message msg) {
        orbsIsBusy = true;
        currentConfig = configurationProvider.getConfig(publicAddress);
        if (currentConfig.orbs().isEnabled()) {
            if (msg.what == Dispatcher.MSG_RECEIVE_ORBS) {
                orbsMessageReceived();
            } else {
                orbsMessageSent();
            }
        } else {
            logger.log(msg.what == Dispatcher.MSG_RECEIVE_ORBS ? "MSG_RECEIVE_ORBS - disabled by configuration" :
                    "MSG_SENT_ORBS - disabled by configuration");
        }
        orbsIsBusy = false;
    }

    private void atnMessageReceived() {
        if (atnSession.isCreated()) {
            atnSession.receiveATN();
            updateRateLimit();
        }
    }

    private void atnMessageSent() {
        if (atnSession.isCreated()) {
            atnSession.sendATN();
            updateRateLimit();
        } else {
            atnSession.create();
            updateRateLimit();
        }
    }

    private void orbsMessageReceived() {
        if (orbsSession.isCreated()) {
            orbsSession.receiveOrbs();
            updateRateLimit();
        }
    }

    private void orbsMessageSent() {
        if (orbsSession.isCreated()) {
            orbsSession.sendOrbs();
            updateRateLimit();
        } else {
            orbsSession.create();
            updateRateLimit();
        }
    }

    private void updateRateLimit() {
        dispatcher.setRateLimit(currentConfig.getTransactionRateLimit());
        orbsDispatcher.setRateLimit(currentConfig.orbs().getTransactionRateLimit());
    }

    boolean isAtnBusy() {
        return atnIsBusy;
    }

    boolean isOrbsBusy() {
        return orbsIsBusy;
    }

    void handleUncaughtException() {
        orbsIsBusy = false;
        atnIsBusy = false;
    }
}
