package com.kik.atn;


import com.google.gson.annotations.SerializedName;

class Config {
    @SerializedName("enabled")
    private final boolean enabled;
    @SerializedName("target_wallet_address")
    private final String atnAddress;
    @SerializedName("transaction_lapse")
    private final int transactionRateLimit;
    @SerializedName("orbs")
    private final Orbs orbs;

    Config(boolean enabled, String atnAddress) {
        this(enabled, atnAddress, 0);
    }

    Config(boolean enabled, String atnAddress, int transactionRateLimit) {
        this.enabled = enabled;
        this.atnAddress = atnAddress;
        this.transactionRateLimit = transactionRateLimit;
        orbs = new Orbs(false, 0);
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

    public Orbs orbs() {
        return orbs;
    }

    public class Orbs {
        @SerializedName("enabled")
        private final boolean enabled;
        @SerializedName("transaction_lapse")
        private final int transactionRateLimit;

        Orbs(boolean enabled) {
            this(enabled, 0);
        }

        Orbs(boolean enabled, int transactionRateLimit) {
            this.enabled = enabled;
            this.transactionRateLimit = transactionRateLimit;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getTransactionRateLimit() {
            return transactionRateLimit;
        }
    }
}
