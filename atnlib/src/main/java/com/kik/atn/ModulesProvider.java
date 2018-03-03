package com.kik.atn;


import android.content.Context;

abstract class ModulesProvider {

    protected ATNServer atnServer;
    protected AndroidLogger androidLogger;
    protected EventLogger eventLogger;
    protected ConfigurationProvider configurationProvider;
    protected ATNAccountOnBoarding onboarding;
    protected KinAccountCreator kinAccountCreator;

    ModulesProvider(Context context) {
        inject(context);
    }

    protected abstract void inject(Context context);

    ATNServer atnServer() {
        return atnServer;
    }

    AndroidLogger androidLogger() {
        return androidLogger;
    }

    EventLogger eventLogger() {
        return eventLogger;
    }

    KinAccountCreator kinAccountCreator() {
        return kinAccountCreator;
    }

    ConfigurationProvider configurationProvider() {
        return configurationProvider;
    }

    ATNAccountOnBoarding accountOnboarding() {
        return onboarding;
    }
}
