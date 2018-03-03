package com.kik.atn;


interface ModulesProvider {
    ATNServer atnServer();

    AndroidLogger androidLogger();

    EventLogger eventLogger();

    KinAccountCreator kinAccountCreator();

    ConfigurationProvider configurationProvider();

    ATNAccountOnBoarding accountOnboarding();
}
