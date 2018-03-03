package com.kik.atn;


import android.content.Context;

class ModulesProviderImpl extends ModulesProvider {

    ModulesProviderImpl(Context context) {
        super(context);
    }

    @Override
    protected void inject(Context context) {
        this.atnServer = new ATNServer(new ATNServerURLProvider());
        this.androidLogger = new AndroidLogger();
        this.eventLogger = new EventLogger(atnServer, androidLogger, false);
        this.configurationProvider = new ConfigurationProvider(atnServer, eventLogger);
        this.onboarding = new ATNAccountOnBoarding(eventLogger, atnServer);
        this.kinAccountCreator = new KinAccountCreatorImpl(context, eventLogger);
    }
}
