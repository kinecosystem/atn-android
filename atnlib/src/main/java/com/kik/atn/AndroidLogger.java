package com.kik.atn;


import android.util.Log;

import java.util.Map;

class AndroidLogger {

    private static final String TAG = "atnlib";
    private static final String FIELD_END = ", ";
    private static boolean SHOULD_LOG = BuildConfig.DEBUG || Log.isLoggable(TAG, Log.DEBUG);

    void log(Event event) {
        if (SHOULD_LOG) {
            StringBuilder log = new StringBuilder();
            log.append("name = ");
            log.append(event.getName());
            log.append(FIELD_END);
            log.append("type = ");
            log.append(event.getType());
            log.append(FIELD_END);
            log.append("public address = ");
            log.append(event.getPublicAddress());
            log.append(FIELD_END);
            log.append("timestamp = ");
            log.append(event.getTimestamp());
            log.append(FIELD_END);
            log.append("sdk_level = ");
            log.append(event.getSdkLevel());
            log.append(FIELD_END);
            log.append("model = ");
            log.append(event.getModel());
            log.append(FIELD_END);
            log.append("manufacturer = ");
            log.append(event.getManufacturer());
            log.append(FIELD_END);
            for (Map.Entry<String, Object> entry : event.getFields().entrySet()) {
                log.append(entry.getKey())
                        .append(" = ")
                        .append(entry.getValue())
                        .append(FIELD_END);
            }

            Log.w(TAG, log.toString());
        }
    }

    void log(String msg) {
        if (SHOULD_LOG) {
            Log.w(TAG, msg);
        }
    }

    String getPrintableStackTrace(Throwable t) {
        return Log.getStackTraceString(t);
    }
}
