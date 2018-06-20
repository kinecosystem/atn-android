package com.kik.atn;


import android.content.Context;

abstract class ModulesProvider {

    ATNServer atnServer;
    AndroidLogger androidLogger;
    EventLogger eventLogger;
    ConfigurationProvider configurationProvider;
    ATNAccountOnBoarding onboarding;
    KinAccountCreator kinAccountCreator;
    OrbsWallet orbsWallet;
    Store store;

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

    public Store getStore() {
        return store;
    }

    public OrbsWallet getOrbsWallet() {
        return orbsWallet;
    }
}
