package com.kik.atn;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.orbs.cryptosdk.Address;
import com.orbs.cryptosdk.CryptoSDK;
import com.orbs.cryptosdk.ED25519Key;

class ATNThreadHandler extends HandlerThread {

    private final EventLogger eventLogger;
    private final ATNSessionCreator sessionCreator;
    private final ConfigurationProvider configurationProvider;
    private volatile boolean isBusy;
    private Handler handler;
    private Dispatcher dispatcher;
    private boolean sessionCreated = false;
    public static final String VIRTUAL_CHAIN_ID = "640ed3";
    public static final String TESTNET_NETWORK_ID = "T";

    ATNThreadHandler(ModulesProvider modulesProvider) {
        super("ATNThreadHandler");
        this.eventLogger = modulesProvider.eventLogger();
        sessionCreator = new ATNSessionCreator(modulesProvider.eventLogger(),
                modulesProvider.atnServer(),
                modulesProvider.kinAccountCreator(),
                modulesProvider.configurationProvider(),
                modulesProvider.accountOnboarding());

        setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                handleUncaughtException(throwable);
            }
        });
        dispatcher = new Dispatcher(this, modulesProvider.androidLogger);
        configurationProvider = modulesProvider.configurationProvider();
    }

    private void handleUncaughtException(Throwable throwable) {
        isBusy = false;
        eventLogger.sendErrorEvent("uncaught_exception", throwable);
    }

    private class ATNHandler extends Handler {

        ATNHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            isBusy = true;
            switch (msg.what) {
                case Dispatcher.MSG_RECEIVE:
                    if (sessionCreated) {
                        sessionCreator.getATNReceiver().receiveATN();
                        updateRateLimit();
                    }
                    break;
                case Dispatcher.MSG_SENT:
                    if (sessionCreated) {
                        long start = System.nanoTime();
                        ED25519Key key = new ED25519Key();
                        Address address = new Address(key.getPublicKey(), VIRTUAL_CHAIN_ID, TESTNET_NETWORK_ID);
                        long totalInMillis = (System.nanoTime() - start) / 1000000;
                        eventLogger.log("orbs address = " + address.getPublicKey() + " took " + totalInMillis + " ms");
                        sessionCreator.getATNSender().sendATN();
                        updateRateLimit();
                    } else {
                        CryptoSDK.initialize();
                        sessionCreated = sessionCreator.create();
                        updateRateLimit();
                    }
                    break;
            }
            isBusy = false;
        }

    }

    private void updateRateLimit() {
        Config lastConfig = configurationProvider.getLastConfig();
        dispatcher.setRateLimit(lastConfig.getTransactionRateLimit());
    }

    @Override
    public synchronized void start() {
        super.start();

        handler = new ATNHandler(getLooper());
    }

    boolean isBusy() {
        return isBusy;
    }

    Handler getHandler() {
        return handler;
    }

    Dispatcher getDispatcher() {
        return dispatcher;
    }

}
