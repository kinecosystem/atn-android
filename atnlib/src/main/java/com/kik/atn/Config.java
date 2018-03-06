package com.kik.atn;


import com.google.gson.annotations.SerializedName;

class Config {
    private final boolean enabled;
    @SerializedName("target_wallet_address")
    private final String atnAddress;
    @SerializedName("transaction_lapse")
    private final int transactionRateLimit;

    Config(boolean enabled, String atnAddress) {
        this(enabled, atnAddress, 0);
    }

    Config(boolean enabled, String atnAddress, int transactionRateLimit) {
        this.enabled = enabled;
        this.atnAddress = atnAddress;
        this.transactionRateLimit = transactionRateLimit;
    }

    boolean isEnabled() {
        return enabled;
    }

    String getAtnAddress() {
        return atnAddress;
    }

    public int getTransactionRateLimit() {
        return transactionRateLimit;
    }
}
