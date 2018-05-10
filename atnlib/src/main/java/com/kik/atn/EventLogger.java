package com.kik.atn;


import java.io.IOException;
import java.util.UUID;

class EventLogger {

    private final static String KEY_DEVICE_ID = "device_id";
    private final ATNServer server;
    private final AndroidLogger androidLogger;
    private final LocalStore localStore;
    private final boolean localOnly;
    private final String deviceId;
    private String publicAddress;
    private String orbsPublicAddress;

    EventLogger(ATNServer server, AndroidLogger androidLogger, LocalStore localStore, boolean localOnly) {
        this.server = server;
        this.androidLogger = androidLogger;
        this.localStore = localStore;
        this.localOnly = localOnly;

        String deviceId = localStore.getString(KEY_DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            localStore.saveString(KEY_DEVICE_ID, deviceId);
        }
        this.deviceId = deviceId;
    }

    void sendEvent(String name) {
        Event event = new Event(name, Event.TYPE_EVENT, publicAddress, deviceId, Event.BLOCKCHAIN_KIN);
        sendEvent(event);
    }

    void sendOrbsEvent(String name) {
        Event event = new Event(name, Event.TYPE_EVENT, orbsPublicAddress, deviceId, Event.BLOCKCHAIN_ORBS);
        sendEvent(event);
    }

    DurationLogger startDurationLogging() {
        return new DurationLogger();
    }

    void sendDurationEvent(String name, long duration) {
        Event event = new Event(name, Event.TYPE_EVENT, publicAddress, deviceId, Event.BLOCKCHAIN_KIN)
                .addField("duration", duration);
        sendEvent(event);
    }

    void sendErrorEvent(String name, Throwable throwable) {
        Event event = new Event(name, Event.TYPE_ERROR, publicAddress, deviceId, Event.BLOCKCHAIN_KIN)
                .addField("exception_type", throwable.getClass().getSimpleName())
                .addField("exception_msg", androidLogger.getPrintableStackTrace(throwable));

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

    void sendOrbsErrorEvent(String name, Throwable throwable) {
        Event event = new Event(name, Event.TYPE_ERROR, orbsPublicAddress, deviceId, Event.BLOCKCHAIN_ORBS)
                .addField("exception_type", throwable.getClass().getSimpleName())
                .addField("exception_msg", androidLogger.getPrintableStackTrace(throwable));

        sendEvent(event);
    }

    void log(String msg) {
        androidLogger.log(msg);
    }

    public void setOrbsPublicAddress(String orbsPublicAddress) {
        this.orbsPublicAddress = orbsPublicAddress;
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
