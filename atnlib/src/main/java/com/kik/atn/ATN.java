package com.kik.atn;


import android.content.Context;
import android.support.annotation.VisibleForTesting;

public class ATN {

    private final Initializer initializer;
    private ModulesProvider modulesProvider;

    public ATN() {
        initializer = new Initializer();
    }

    @VisibleForTesting
    ATN(ModulesProvider modulesProvider) {
        this();
        this.modulesProvider = modulesProvider;
    }

    public void onMessageSent(Context context) {
        sendMessage(context, Dispatcher.MSG_SENT);
    }

    public void onMessageReceived(Context context) {
        sendMessage(context, Dispatcher.MSG_RECEIVE);
    }

    private void sendMessage(Context context, int msg) {
        if (initializer.isInitialized(getModulesProvider(context))) {
            initializer.getDispatcher().dispatch(initializer.getHandler(), msg);
        }
    }

    private synchronized ModulesProvider getModulesProvider(Context context) {
        if (modulesProvider == null) {
            modulesProvider = new ModulesProviderImpl(context);
        }
        return modulesProvider;
    }
}
