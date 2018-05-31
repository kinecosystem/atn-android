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
        this(enabled, atnAddress, 0, null);
    }

    Config(boolean enabled, String atnAddress, int transactionRateLimit) {
        this(enabled, atnAddress, transactionRateLimit, null);
    }

    Config(boolean enabled, String atnAddress, int transactionRateLimit, Orbs orbs) {
        this.enabled = enabled;
        this.atnAddress = atnAddress;
        this.transactionRateLimit = transactionRateLimit;
        if (orbs == null) {
            this.orbs = new Orbs(false, 0, "");
        } else {
            this.orbs = orbs;
        }
    }

    boolean isEnabled() {
        return enabled;
    }

    String getAtnAddress() {
        return atnAddress;
    }

    int getTransactionRateLimit() {
        return transactionRateLimit;
    }

    public Orbs orbs() {
        return orbs;
    }

    static class Orbs {
        @SerializedName("enabled")
        private final boolean enabled;
        @SerializedName("transaction_lapse")
        private final int transactionRateLimit;
        @SerializedName("target_wallet_address")
        private final String serverAccountAddress;

        Orbs(boolean enabled) {
            this(enabled, 0, "");
        }

        Orbs(boolean enabled, int transactionRateLimit, String serverAccountAddress) {
            this.enabled = enabled;
            this.transactionRateLimit = transactionRateLimit;
            this.serverAccountAddress = serverAccountAddress;
        }

        boolean isEnabled() {
            return enabled;
        }

        int getTransactionRateLimit() {
            return transactionRateLimit;
        }

        String getServerAccountAddress() {
            return serverAccountAddress;
        }
    }
}
