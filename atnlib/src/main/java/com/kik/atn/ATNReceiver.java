package com.kik.atn;


class ATNReceiver {

    private final ATNServer atnServer;
    private final String publicKey;
    private final EventLogger eventLogger;

    ATNReceiver(ATNServer atnServer, EventLogger eventLogger, String publicKey) {
        this.atnServer = atnServer;
        this.publicKey = publicKey;
        this.eventLogger = eventLogger;
    }

    void receiveATN() {
        eventLogger.sendEvent("receive_atn");
        boolean success = atnServer.receiveATN(publicKey);
        if (success) {
            eventLogger.sendEvent("receive_atn_succeed");
        } else {
            eventLogger.sendEvent("receive_atn_failed");
        }
    }
}
