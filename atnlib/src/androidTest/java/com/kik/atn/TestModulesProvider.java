package com.kik.atn;


import android.content.Context;

import kin.core.KinAccount;

class TestModulesProvider extends ModulesProvider {

    private final String serverUrl;
    private final KinAccount account;

    TestModulesProvider(Context context, String serverUrl, KinAccount account) {
        super(context);
        this.serverUrl = serverUrl;
        this.account = account;
    }

    TestModulesProvider(Context context, String serverUrl, KinAccount account, ATNServer server) {
        this(context, serverUrl, account);
        this.atnServer = server;
    }

    @Override
    protected void inject(Context context) {
        this.store = new TestStore(context);
        this.androidLogger = new AndroidLogger();
        if (this.atnServer == null) {
            this.atnServer = new ATNServer(new ATNServerURLProvider() {
                @Override
                String getUrl() {
                    return serverUrl;
                }
            });
        }
        this.eventLogger = new EventLogger(atnServer, androidLogger, store, true);
        this.configurationProvider = new ConfigurationProvider(atnServer, eventLogger, 10000);
        this.onboarding = new ATNAccountOnBoarding(eventLogger, atnServer);
        this.kinAccountCreator = new KinAccountCreator() {

            @Override
            public KinAccount getAccount() {
                return account;
            }
        };
    }

    @Override
    public TestStore getStore() {
        return (TestStore) super.getStore();
    }
}
