package com.kik.atn;


import kin.core.KinAccount;

class ATNReceiver {

    private final String publicKey;
    private final EventLogger eventLogger;

    public ATNReceiver(String publicKey, EventLogger eventLogger) {
        this.publicKey = publicKey;
        this.eventLogger = eventLogger;
    }

    void receiveATN() {

    }
}
