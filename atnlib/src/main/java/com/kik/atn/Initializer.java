package com.kik.atn;


import android.os.Handler;

class Initializer {

    private ATNThreadHandler atnThreadHandler;

    Initializer() {
    }

    boolean isInitialized(ModulesProvider modulesProvider) {
        createThreadHandlerIfNeeded(modulesProvider);
        return atnThreadHandler.isInitialized();
    }

    private synchronized void createThreadHandlerIfNeeded(ModulesProvider modulesProvider) {
        if (atnThreadHandler == null) {
            atnThreadHandler = new ATNThreadHandler(modulesProvider);
            atnThreadHandler.start();
        }
    }

    Handler getHandler() {
        return atnThreadHandler.getHandler();
    }
}
