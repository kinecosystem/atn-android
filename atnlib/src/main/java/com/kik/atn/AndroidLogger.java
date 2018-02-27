package com.kik.atn;


import android.util.Log;

import java.util.Map;

class AndroidLogger {

    private static final String TAG = "atnlib";
    private static boolean SHOULD_LOG = BuildConfig.DEBUG || Log.isLoggable(TAG, Log.DEBUG);

    void log(Event event) {
        if (SHOULD_LOG) {
            StringBuilder log = new StringBuilder();
            for (Map.Entry<String, Object> entry : event.getFields().entrySet()) {
                log.append(entry.getKey())
                        .append(" = ")
                        .append(entry.getValue())
                        .append(" ");
            }

            Log.d(TAG, log.toString());
        }
    }

    void log(String tag, String msg) {
        if (SHOULD_LOG) {
            Log.d(tag, msg);
        }
    }
}
