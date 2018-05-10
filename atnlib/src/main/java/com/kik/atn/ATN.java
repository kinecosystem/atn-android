package com.kik.atn;


import android.content.Context;
import android.support.annotation.VisibleForTesting;

import static com.kik.atn.Dispatcher.MessageType.MSG_RECEIVE;
import static com.kik.atn.Dispatcher.MessageType.MSG_RECEIVE_ORBS;
import static com.kik.atn.Dispatcher.MessageType.MSG_SENT;
import static com.kik.atn.Dispatcher.MessageType.MSG_SENT_ORBS;

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
        sendMessage(context, MSG_SENT, MSG_SENT_ORBS);
    }

    public void onMessageReceived(Context context) {
        sendMessage(context, MSG_RECEIVE, MSG_RECEIVE_ORBS);
    }

    private void sendMessage(Context context, int msg, int orbsMsg) {
        if (initializer.isInitialized(getModulesProvider(context))) {
            initializer.getDispatcher().dispatch(initializer.getHandler(), msg);
            initializer.getOrbsDispatcher().dispatch(initializer.getHandler(), orbsMsg);
        }
    }

    private synchronized ModulesProvider getModulesProvider(Context context) {
        if (modulesProvider == null) {
            modulesProvider = new ModulesProviderImpl(context);
        }
        return modulesProvider;
    }
}
