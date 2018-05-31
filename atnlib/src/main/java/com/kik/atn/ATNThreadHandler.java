package com.kik.atn;


import android.os.Handler;
import android.os.HandlerThread;

import kin.core.KinAccount;

class ATNThreadHandler extends HandlerThread {

    private final EventLogger eventLogger;
    private final ATNSession sessionCreator;
    private final OrbsSession orbsSession;
    private final ModulesProvider modulesProvider;
    private ATNHandler handler;
    private Dispatcher dispatcher, orbsDispatcher;

    ATNThreadHandler(ModulesProvider modulesProvider) {
        super("ATNThreadHandler");
        this.eventLogger = modulesProvider.eventLogger();
        sessionCreator = new ATNSession(modulesProvider.eventLogger(),
                modulesProvider.atnServer(),
                modulesProvider.kinAccountCreator(),
                modulesProvider.configurationProvider(),
                modulesProvider.accountOnboarding());
        orbsSession = new OrbsSession(modulesProvider.getOrbsWallet(),
                modulesProvider.eventLogger(),
                modulesProvider.atnServer(),
                modulesProvider.kinAccountCreator(),
                modulesProvider.configurationProvider());

        setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                handleUncaughtException(throwable);
            }
        });
        dispatcher = new AtnDispatcher(this, modulesProvider.androidLogger, "kin");
        orbsDispatcher = new OrbsDispatcher(this, modulesProvider.androidLogger, "orbs");
        this.modulesProvider = modulesProvider;
    }

    private void handleUncaughtException(Throwable throwable) {
        eventLogger.sendErrorEvent("uncaught_exception", throwable);
        if (handler != null) {
            handler.handleUncaughtException();
        }
    }

    @Override
    public synchronized void start() {
        super.start();

        handler = new ATNHandler(sessionCreator, orbsSession, dispatcher, orbsDispatcher,
                modulesProvider.configurationProvider(), modulesProvider.androidLogger(),
                extractPublicAddress(), getLooper());
    }

    private String extractPublicAddress() {
        KinAccount kinAccount = modulesProvider.kinAccountCreator().getAccount();
        if (kinAccount != null) {
            return kinAccount.getPublicAddress();
        } else {
            return "";
        }
    }

    boolean isAtnBusy() {
        return handler.isAtnBusy();
    }

    boolean isOrbsBusy() {
        return handler.isOrbsBusy();
    }

    Handler getHandler() {
        return handler;
    }

    Dispatcher getDispatcher() {
        return dispatcher;
    }

    Dispatcher getOrbsDispatcher() {
        return orbsDispatcher;
    }
}
