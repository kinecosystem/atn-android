package com.kik.atn;


import android.content.Context;

import kin.core.KinAccount;
import kin.core.KinClient;
import kin.core.ServiceProvider;
import kin.core.exception.CreateAccountException;

class KinAccountCreatorImpl implements KinAccountCreator {

    private static final String ASSET_CODE_ATN = "ATN";
    private static final String ASSET_ISSUER = "GCAUZH5OGE4HU4NZPBXX67A66D6DVR2IIZMT2BU635UN5PJXWUPUO3A7";
    private static final String HORIZON_ENDPOINT = "http://horizon-testnet.kininfrastructure.com";
    private final Context context;
    private final KinClient kinClient;
    private final EventLogger eventLogger;
    private KinAccount account;

    KinAccountCreatorImpl(Context context, EventLogger eventLogger) {
        this.context = context;
        this.eventLogger = eventLogger;
        kinClient = createKinClient();
    }

    @Override
    public KinAccount getAccount() {
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
                new ServiceProvider(HORIZON_ENDPOINT,
                        ServiceProvider.NETWORK_ID_TEST) {
                    @Override
                    protected String getAssetCode() {
                        return ASSET_CODE_ATN;
                    }

                    @Override
                    protected String getIssuerAccountId() {
                        return ASSET_ISSUER;
                    }
                });
    }
}
