package com.kik.atn;


import android.content.Context;
import android.support.annotation.VisibleForTesting;


public class ATN {

    private ModulesProvider modulesProvider;
    private ATNThreadHandler atnThreadHandler;

    public ATN() {
    }

    @VisibleForTesting
    ATN(ModulesProvider modulesProvider) {
        this.modulesProvider = modulesProvider;
    }

    public void onMessageSent(Context context) {
        sendMessage(context, Dispatcher.MSG_SENT, Dispatcher.MSG_SENT_ORBS);
    }

    public void onMessageReceived(Context context) {
        sendMessage(context, Dispatcher.MSG_RECEIVE, Dispatcher.MSG_RECEIVE_ORBS);
    }

    private void sendMessage(Context context, int msg, int orbsMsg) {
        initIfNeeded(context);
        atnThreadHandler.getDispatcher().dispatch(msg);
        atnThreadHandler.getOrbsDispatcher().dispatch(orbsMsg);

    }

    private void initIfNeeded(Context context) {
        if (modulesProvider == null) {
            modulesProvider = new ModulesProviderImpl(context);
        }
        if (atnThreadHandler == null) {
            atnThreadHandler = new ATNThreadHandler(modulesProvider);
            atnThreadHandler.start();
        }
    }
}
