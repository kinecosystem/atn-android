package com.kik.atn;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

class ATNThreadHandler extends HandlerThread {

    private static final int MSG_INIT = 42;
    private final EventLogger eventLogger;
    private final ATNSessionCreator sessionCreator;
    private volatile boolean isInitialized = false;
    private Handler handler;

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
                isInitialized = false;
                handleUncaughtException(throwable);
            }
        });
    }

    private void handleUncaughtException(Throwable throwable) {
        eventLogger.sendErrorEvent("uncaught_exception", throwable);
    }

    private class ATNHandler extends Handler {

        ATNHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    isInitialized = sessionCreator.create();
                    break;
                case Dispatcher.MSG_RECEIVE:
                    if (isInitialized) {
                        sessionCreator.getATNReceiver().receiveATN();
                    }
                    break;
                case Dispatcher.MSG_SENT:
                    if (isInitialized) {
                        sessionCreator.getATNSender().sendATN();
                    }
                    break;
            }
        }

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

    Handler getHandler() {
        return handler;
    }
}
