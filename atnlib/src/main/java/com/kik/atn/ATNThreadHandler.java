package com.kik.atn;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

class ATNThreadHandler extends HandlerThread {

    private final EventLogger eventLogger;
    private final ATNSessionCreator sessionCreator;
    private final OrbsSessionCreator orbsSessionCreator;
    private final ConfigurationProvider configurationProvider;
    private volatile boolean isBusy;
    private Handler handler;
    private Dispatcher dispatcher, orbsDispatcher;
    private boolean sessionCreated = false;
    private boolean orbsSessionCreated = false;

    ATNThreadHandler(ModulesProvider modulesProvider) {
        super("ATNThreadHandler");
        this.eventLogger = modulesProvider.eventLogger();
        sessionCreator = new ATNSessionCreator(modulesProvider.eventLogger(),
                modulesProvider.atnServer(),
                modulesProvider.kinAccountCreator(),
                modulesProvider.configurationProvider(),
                modulesProvider.accountOnboarding());
        orbsSessionCreator = new OrbsSessionCreator(modulesProvider.getOrbsWallet(),
                modulesProvider.eventLogger(),
                modulesProvider.atnServer(),
                modulesProvider.kinAccountCreator(),
                modulesProvider.configurationProvider());

        setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                handleUncaughtException(throwable);
            }
        });
        dispatcher = new Dispatcher(this, modulesProvider.androidLogger,
                new int[]{Dispatcher.MessageType.MSG_SENT, Dispatcher.MessageType.MSG_SENT});
        orbsDispatcher = new Dispatcher(this, modulesProvider.androidLogger,
                new int[]{Dispatcher.MessageType.MSG_RECEIVE_ORBS, Dispatcher.MessageType.MSG_SENT_ORBS});
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
                case Dispatcher.MessageType.MSG_RECEIVE:
                    if (sessionCreated) {
                        sessionCreator.getATNReceiver().receiveATN();
                        updateRateLimit();
                    }
                    break;
                case Dispatcher.MessageType.MSG_SENT:
                    if (sessionCreated) {
                        sessionCreator.getATNSender().sendATN();
                        updateRateLimit();
                    } else {
                        sessionCreated = sessionCreator.create();
                        updateRateLimit();
                    }
                    break;
                case Dispatcher.MessageType.MSG_RECEIVE_ORBS:
                    if (orbsSessionCreated) {
                        orbsSessionCreator.getOrbsReceiver().receiveOrbs();
                        updateRateLimit();
                    }
                    break;
                case Dispatcher.MessageType.MSG_SENT_ORBS:
                    if (orbsSessionCreated) {
                        orbsSessionCreator.getOrbsSender().sendOrbs();
                        updateRateLimit();
                    } else {
                        orbsSessionCreated = orbsSessionCreator.create();
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
        orbsDispatcher.setRateLimit(lastConfig.orbs().getTransactionRateLimit());
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

    Dispatcher getOrbsDispatcher() {
        return orbsDispatcher;
    }
}
