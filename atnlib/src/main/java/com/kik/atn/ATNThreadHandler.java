package com.kik.atn;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import kin.core.KinAccount;

class ATNThreadHandler extends HandlerThread {

    private final ConfigurationProvider configurationProvider;
    private Handler handler;
    private final ATNAccountCreator accountCreator;
    private boolean isInitialized;
    private ATNSender atnSender;
    private ATNReceiver afnReceiver;

    public ATNThreadHandler(Context context) {
        super("ATNThreadHandler");

        setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                handleUncaughtException(throwable);
            }
        });
        configurationProvider = new ConfigurationProvider();
        accountCreator = new ATNAccountCreator(context);
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

    }

    @Override
    public void run() {
        configurationProvider.init();
        if (configurationProvider.enabled()) {
            KinAccount account = accountCreator.create();
            handler = new ATNHandler(getLooper());
            atnSender = new ATNSender(account, configurationProvider.ATNAddres());
            afnReceiver = new ATNReceiver(account);
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
