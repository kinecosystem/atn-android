package com.kik.atn;


import java.io.IOException;

class EventLogger {

    private final ATNServer server;
    private final AndroidLogger androidLogger;
    private final boolean localOnly;
    private String publicAddress;

    EventLogger(ATNServer server, AndroidLogger androidLogger, boolean localOnly) {
        this.server = server;
        this.androidLogger = androidLogger;
        this.localOnly = localOnly;
    }

    void sendEvent(String name) {
        Event event = new Event(name, Event.TYPE_EVENT, publicAddress);
        sendEvent(event);
    }

    DurationLogger startDurationLogging() {
        return new DurationLogger();
    }

    void sendDurationEvent(String name, long duration) {
        Event event = new Event(name, Event.TYPE_EVENT, publicAddress)
                .addField("duration", duration);
        sendEvent(event);
    }

    private void sendEvent(Event event) {
        try {
            androidLogger.log(event);
            if (!localOnly) {
                server.sendEvent(event);
            }
        } catch (IOException e) {
            androidLogger.log("EventLogger - can't send event");
        }
    }

    void sendErrorEvent(String name, Throwable throwable) {
        Event event = new Event(name, Event.TYPE_ERROR, publicAddress)
                .addField("exception_type", throwable.getClass().getSimpleName())
                .addField("exception_msg", throwable.toString());

        sendEvent(event);
    }

    void log(String msg) {
        androidLogger.log(msg);
    }

    class DurationLogger {
        private static final int NANOS_IN_MILLI = 1000000;
        private long start;

        DurationLogger() {
            start = System.nanoTime();
        }

        void report(String name) {
            long duration = (System.nanoTime() - start) / NANOS_IN_MILLI;
            sendDurationEvent(name, duration);
        }
    }

    void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }
}
