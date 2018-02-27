package com.kik.atn;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

class ATNThreadHandler extends HandlerThread {

    private final EventLogger eventLogger;
    private final ATNSessionCreator sessionCreator;
    private volatile boolean isInitialized = false;
    private Handler handler;

    ATNThreadHandler(Context context) {
        super("ATNThreadHandler");
        ATNServer atnServer = new ATNServer();
        this.eventLogger = new EventLogger(atnServer, new AndroidLogger());
        sessionCreator = new ATNSessionCreator(eventLogger, atnServer, new KinAccountCreator(context, eventLogger));
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
