package com.kik.atn;


import android.content.Context;

import kin.core.KinAccount;
import kin.core.KinClient;
import kin.core.ServiceProvider;
import kin.core.exception.CreateAccountException;

class KinAccountCreator {

    private final Context context;
    private final KinClient kinClient;
    private final EventLogger eventLogger;
    private KinAccount account;

    KinAccountCreator(Context context, EventLogger eventLogger) {
        this.context = context;
        this.eventLogger = eventLogger;
        kinClient = createKinClient();
    }

    KinAccount getAccount() {
        if (account == null) {
            if (kinClient.hasAccount()) {
                account = kinClient.getAccount(0);
            } else {
                try {
                    account = kinClient.addAccount("");
                } catch (CreateAccountException e) {
                    eventLogger.sendErrorEvent("add_account_failed", e);
                }
            }
        }
        return account;
    }

    private KinClient createKinClient() {
        return new KinClient(context,
                new ServiceProvider("https://horizon-testnet.stellar.org",
                        ServiceProvider.NETWORK_ID_TEST));
    }
}
