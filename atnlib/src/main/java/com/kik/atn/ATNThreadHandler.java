package com.kik.atn;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

class ATNThreadHandler extends HandlerThread {

    private final EventLogger eventLogger;
    private final ATNSessionCreator sessionCreator;
    private volatile boolean isInitialized = false;
    private Handler handler;

    ATNThreadHandler(ModulesProvider modulesProvider) {
        super("ATNThreadHandler");
        this.eventLogger = new EventLogger(modulesProvider.atnServer(), modulesProvider.androidLogger());
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
                case Dispatcher.MSG_RECEIVE:
                    sessionCreator.getATNReceiver().receiveATN();
                    break;
                case Dispatcher.MSG_SENT:
                    sessionCreator.getATNSender().sendATN();
                    break;
            }
        }

    }

    @Override
    public void run() {
        if (sessionCreator.create()) {
            handler = new ATNHandler(getLooper());
            isInitialized = true;
        }
    }

    boolean isInitialized() {
        return isInitialized;
    }

    Handler getHandler() {
        return handler;
    }
}
