package com.kik.atn;


import java.io.IOException;

class EventLogger {

    private static final String TAG = EventLogger.class.getSimpleName();
    private final ATNServer server;
    private final AndroidLogger androidLogger;
    private String publicAddress;

    EventLogger(ATNServer server, AndroidLogger androidLogger) {
        this.server = server;
        this.androidLogger = androidLogger;
    }

    void sendEvent(String name) {
        Event event = new Event(name, Event.TYPE_EVENT, publicAddress);
        sendEvent(event);
    }

    void sendDurationEvent(String name, long duration) {
        Event event = new Event(name, Event.TYPE_DURATION, publicAddress)
                .addField("duration", duration);
        sendEvent(event);
    }

    private void sendEvent(Event event) {
        try {
            androidLogger.log(event);
            server.sendEvent(event);
        } catch (IOException e) {
            androidLogger.log(TAG, "can't send event");
        }
    }

    void sendErrorEvent(String name, Throwable throwable) {
        Event event = new Event(name, Event.TYPE_ERROR, publicAddress)
                .addField("exception_type", throwable.getClass().getSimpleName())
                .addField("exception_msg", throwable.toString());

        sendEvent(event);
    }

    void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }
}
