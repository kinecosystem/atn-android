package com.kik.atn;


import com.google.gson.annotations.SerializedName;

class Config {
    private final boolean enabled;
    @SerializedName("target_wallet_address")
    private final String atnAddress;

    Config(boolean enabled, String atnAddress) {
        this.enabled = enabled;
        this.atnAddress = atnAddress;
    }

    boolean isEnabled() {
        return enabled;
    }

    String getAtnAddress() {
        return atnAddress;
    }
}
