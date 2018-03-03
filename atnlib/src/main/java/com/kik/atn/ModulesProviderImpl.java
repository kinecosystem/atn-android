package com.kik.atn;


import android.content.Context;

class ModulesProviderImpl implements ModulesProvider {

    private final ATNServer atnServer;
    private final AndroidLogger androidLogger;
    private final EventLogger eventLogger;
    private final Context context;
    private final ConfigurationProvider configurationProvider;
    private final ATNAccountOnBoarding onboarding;
    private KinAccountCreator kinAccountCreator;

    ModulesProviderImpl(Context context) {
        this.context = context;
        this.atnServer = new ATNServer();
        this.androidLogger = new AndroidLogger();
        this.eventLogger = new EventLogger(atnServer, androidLogger);
        this.configurationProvider = new ConfigurationProvider(atnServer, eventLogger);
        this.onboarding = new ATNAccountOnBoarding(eventLogger, atnServer);
    }

    @Override
    public ATNServer atnServer() {
        return atnServer;
    }

    @Override
    public AndroidLogger androidLogger() {
        return androidLogger;
    }

    @Override
    public EventLogger eventLogger() {
        return eventLogger;
    }

    @Override
    public KinAccountCreator kinAccountCreator() {
        if (kinAccountCreator == null) {
            kinAccountCreator = new KinAccountCreator(context, eventLogger);
        }
        return kinAccountCreator;
    }

    @Override
    public ConfigurationProvider configurationProvider() {
        return configurationProvider;
    }

    @Override
    public ATNAccountOnBoarding accountOnboarding() {
        return onboarding;
    }
}
