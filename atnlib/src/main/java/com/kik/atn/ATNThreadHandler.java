package com.kik.atn;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

class ATNThreadHandler extends HandlerThread {

    private final EventLogger eventLogger;
    private final ATNSessionCreator sessionCreator;
    private final ConfigurationProvider configurationProvider;
    private volatile boolean isBusy;
    private Handler handler;
    private Dispatcher dispatcher;
    private boolean sessionCreated = false;

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
                        sessionCreator.getATNSender().sendATN();
                        updateRateLimit();
                    } else {
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
