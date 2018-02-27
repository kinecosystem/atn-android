package com.kik.atn;


import android.content.Context;

public class ATN {

    private final Initializer initializer;
    private final Dispatcher dispatcher;

    public ATN() {
        initializer = new Initializer();
        dispatcher = new Dispatcher();
    }

    void onMessageSent(Context context) {
        if (initializer.isInitialized(context.getApplicationContext())) {
            dispatcher.dispatch(initializer.getHandler(), Dispatcher.MSG_SENT);
        }
    }

    void onMessageRecieve(Context context) {
        if (initializer.isInitialized(context.getApplicationContext())) {
            dispatcher.dispatch(initializer.getHandler(), Dispatcher.MSG_RECEIVE);
        }
    }
}
