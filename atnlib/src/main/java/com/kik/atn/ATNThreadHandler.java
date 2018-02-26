package com.kik.atn;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import kin.core.KinAccount;

class ATNThreadHandler extends HandlerThread {

    private final Context context;
    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private volatile boolean isInitialized = false;
    private Handler handler;
    private ATNSender atnSender;
    private ATNReceiver afnReceiver;

    ATNThreadHandler(Context context) {
        super("ATNThreadHandler");

        this.context = context;
        atnServer = new ATNServer();
        this.eventLogger = new EventLogger(atnServer, new AndroidLogger());
        setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                handleUncaughtException(throwable);
            }
        });
    }

    private class ATNHandler extends Handler {
        ATNHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Dispatcher.MSG_RECIEVE:
                    afnReceiver.receiveATN();
                    break;
                case Dispatcher.MSG_SENT:
                    atnSender.sendATN();
                    break;
            }
        }
    }

    private void handleUncaughtException(Throwable throwable) {
        eventLogger.sendErrorEvent("uncaught_exception", throwable);
    }

    @Override
    public void run() {
        ConfigurationProvider configurationProvider = new ConfigurationProvider();

        configurationProvider.init();
        if (configurationProvider.enabled()) {
            ATNAccountCreator accountCreator = new ATNAccountCreator(context, eventLogger, atnServer);

            KinAccount account = accountCreator.create();
            atnSender = new ATNSender(account, configurationProvider.ATNAddress(), eventLogger);
            afnReceiver = new ATNReceiver(atnServer, eventLogger, account.getPublicAddress());
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
