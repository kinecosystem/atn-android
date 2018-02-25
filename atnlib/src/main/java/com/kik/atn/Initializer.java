package com.kik.atn;


import android.content.Context;
import android.os.Handler;

class Initializer {

    private ATNThreadHandler atnThreadHandler;

    private void init(Context context) {
        atnThreadHandler = new ATNThreadHandler(context);
        atnThreadHandler.start();
    }

    boolean isInitialized(Context context) {
        if (atnThreadHandler == null || !atnThreadHandler.isInitialized()) {
            init(context);
        }
        return true;
    }

    Handler getHandler() {
        return atnThreadHandler.getHandler();
    }
}
