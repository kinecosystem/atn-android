package com.kik.atn;


import java.util.HashMap;

class EventLogger {

    void sendEvent(String name, HashMap<String, Object> params) {

    }

    void sendEvent(String name) {
        sendEvent(name, null);
    }

    void sendDurationEvent(String name, long duration) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("duration", duration);
        sendEvent(name, params);
    }

    void sendErrorEvent(String name, Throwable throwable) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("exception", throwable.toString());
        sendEvent(name, params);
    }
}
