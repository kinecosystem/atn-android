package com.kik.atn;


import android.content.Context;
import android.support.annotation.VisibleForTesting;

public class ATN {

    private final Initializer initializer;
    private final Dispatcher dispatcher;
    private ModulesProvider modulesProvider;

    public ATN() {
        initializer = new Initializer();
        dispatcher = new Dispatcher();
    }

    @VisibleForTesting
    ATN(ModulesProvider modulesProvider) {
        this();
        this.modulesProvider = modulesProvider;
    }

    void onMessageSent(Context context) {
        sendMessage(context, Dispatcher.MSG_SENT);
    }

    void onMessageReceived(Context context) {
        sendMessage(context, Dispatcher.MSG_RECEIVE);
    }

    private void sendMessage(Context context, int msg) {
        if (initializer.isInitialized(getModulesProvider(context))) {
            dispatcher.dispatch(initializer.getHandler(), msg);
        }
    }

    private synchronized ModulesProvider getModulesProvider(Context context) {
        if (modulesProvider == null) {
            modulesProvider = new ModulesProviderImpl(context);
        }
        return modulesProvider;
    }
}
