package com.kik.atn;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

class ATNThreadHandler extends HandlerThread {

    private static final int MSG_INIT = 42;
    private final EventLogger eventLogger;
    private final ATNSessionCreator sessionCreator;
    private final ConfigurationProvider configurationProvider;
    private volatile boolean isInitialized = false;
    private volatile boolean isBusy;
    private Handler handler;
    private Dispatcher dispatcher;

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
        isInitialized = false;
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
                case MSG_INIT:
                    isInitialized = sessionCreator.create();
                    updateRateLimit();
                    break;
                case Dispatcher.MSG_RECEIVE:
                    if (isInitialized) {
                        sessionCreator.getATNReceiver().receiveATN();
                        updateRateLimit();
                    }
                    break;
                case Dispatcher.MSG_SENT:
                    if (isInitialized) {
                        sessionCreator.getATNSender().sendATN();
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
        handler.sendEmptyMessage(MSG_INIT);
    }

    boolean isInitialized() {
        return isInitialized;
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
