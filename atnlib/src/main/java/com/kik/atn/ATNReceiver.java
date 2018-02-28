package com.kik.atn;


import java.io.IOException;

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
        eventLogger.sendEvent("receive_atn_started");
        try {
            atnServer.receiveATN(publicKey);
            eventLogger.sendEvent("receive_atn_succeed");
        } catch (IOException e) {
            eventLogger.sendEvent("receive_atn_failed");
        }
    }
}
