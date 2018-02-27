package com.kik.atn;


import android.content.Context;
import android.os.Handler;

class Initializer {

    private ATNThreadHandler atnThreadHandler;

    boolean isInitialized(Context context) {
        createThreadHandlerIfNeeded(context);
        return atnThreadHandler.isInitialized();
    }

    private synchronized void createThreadHandlerIfNeeded(Context context) {
        if (atnThreadHandler == null) {
            atnThreadHandler = new ATNThreadHandler(context);
            atnThreadHandler.start();
        }
    }

    Handler getHandler() {
        return atnThreadHandler.getHandler();
    }
}
