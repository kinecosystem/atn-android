package com.kik.atn;


import java.util.HashMap;

class EventLogger {

    void sendEvent(String name, HashMap<String, Object> params) {

    }

    void sendDurationEvent(String name, long duration) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("duration", duration);
        sendEvent(name, params);
    }

    void sendErrorEvent(String name, Throwable throwable) {

    }
}
