package com.kik.atn;


import android.content.Context;

import kin.core.KinAccount;
import kin.core.KinClient;
import kin.core.ServiceProvider;
import kin.core.exception.CreateAccountException;
import kin.core.exception.OperationFailedException;

class ATNAccountCreator {

    private final Context context;
    private final EventLogger eventLogger;
    private final ATNServer atnServer;
    private KinClient kinClient;

    ATNAccountCreator(Context context, EventLogger eventLogger, ATNServer atnServer) {
        this.context = context;
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
    }

    KinAccount create() {
        createKinClient();

        long start = System.nanoTime();
        KinAccount account = addAccount();
        if (account != null)
            if (atnServer.fundWithXLM(account.getPublicAddress())) {
                try {
                    account.activateSync("");
                    if (atnServer.fundWithATN(account.getPublicAddress())) {
                        long duration = (System.nanoTime() - start) / 1000;
                        eventLogger.sendDurationEvent("account_created", duration);
                        return account;
                    } else {
                        eventLogger.sendEvent("fund_atn_failed", null);
                    }
                } catch (OperationFailedException e) {
                    eventLogger.sendErrorEvent("activate_failed", e);
                }
            } else {
                eventLogger.sendEvent("fund_xlm_failed", null);
            }
        return null;
    }

    private void createKinClient() {
        kinClient = new KinClient(context,
                new ServiceProvider("https://horizon-testnet.stellar.org",
                        ServiceProvider.NETWORK_ID_TEST));
    }

    private KinAccount addAccount() {
        KinAccount account = null;
        try {
            account = kinClient.addAccount("");
        } catch (CreateAccountException e) {
            eventLogger.sendErrorEvent("add_account_failed", e);
        }
        return account;
    }
}
