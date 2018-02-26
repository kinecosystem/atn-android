package com.kik.atn;


class ConfigurationProvider {

    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_RATE_LIMIT = "rate_limit";
    private static final String KEY_ATN_ADDRESS = "atn_address";
    private static final String KEY_SEND_AMOUNT = "send_amount";

    private Event event;

    ConfigurationProvider() {

    }

    boolean enabled() {

        return false;
    }

    int rateLimit() {
        return 0;
    }

    String ATNAddress() {

        return null;
    }

    int ATNSendAmount() {

        return 0;
    }

    void init() {

    }

}
