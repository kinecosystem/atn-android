package com.kik.atn;


import android.content.Context;

import java.io.IOException;

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
        if (account != null) {
            if (fundWithXLM(account) && activateAccount(account) && fundWithATN(account)) {
                long duration = (System.nanoTime() - start) / 1000;
                eventLogger.sendDurationEvent("account_created", duration);
            }
        }
        return account;
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

    private boolean fundWithXLM(KinAccount account) {
        try {
            atnServer.fundWithXLM(account.getPublicAddress());
            return true;
        } catch (IOException e) {
            eventLogger.sendErrorEvent("fund_xlm_failed", e);
        }
        return false;
    }

    private boolean activateAccount(KinAccount account) {
        try {
            account.activateSync("");
            return true;
        } catch (OperationFailedException e) {
            eventLogger.sendErrorEvent("activate_failed", e);
        }
        return false;
    }

    private boolean fundWithATN(KinAccount account) {
        try {
            atnServer.fundWithATN(account.getPublicAddress());
            return true;
        } catch (IOException e) {
            eventLogger.sendErrorEvent("fund_xlm_failed", e);
        }
        return false;
    }


}
