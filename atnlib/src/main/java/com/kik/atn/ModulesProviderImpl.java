package com.kik.atn;


import android.content.Context;

class ModulesProviderImpl extends ModulesProvider {

    private static final long MIN_UPDATE_INTERVAL_MILLIS = 1000 * 60 * 10; //10 Min

    ModulesProviderImpl(Context context) {
        super(context);
    }

    @Override
    protected void inject(Context context) {
        this.atnServer = new ATNServer(new ATNServerURLProvider());
        this.androidLogger = new AndroidLogger();
        this.eventLogger = new EventLogger(atnServer, androidLogger, new LocalStore(context), false);
        this.configurationProvider = new ConfigurationProvider(atnServer, eventLogger, MIN_UPDATE_INTERVAL_MILLIS);
        this.onboarding = new ATNAccountOnBoarding(eventLogger, atnServer);
        this.kinAccountCreator = new KinAccountCreatorImpl(context, eventLogger);
    }
}
