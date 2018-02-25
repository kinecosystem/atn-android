package com.kik.atn;


public class ATNServer {

    boolean fundWithXLM(String publicAddress) {
        return true;
    }

    boolean fundWithATN(String publicAddress) {
        return true;
    }

    boolean receiveATN(String publicAddress) {
        return true;
    }

    boolean sendEvent(String event) {
        return false;
    }

    void getConfiguration() {

    }
}
