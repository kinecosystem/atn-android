package com.kik.atn;


import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

class OrbsWallet {

    static final String KEY_ORBS_PUBLIC_KEY = "key_orbs_public_address";
    static final String KEY_ORBS_PRIVATE_KEY = "key_orbs_private_key";
    static final String VIRTUAL_CHAIN_ID = "6b696e";
    private static final String NETWORK_ID_PROD = "M";
    static final String NETWORK_ID = NETWORK_ID_PROD;
    private final Store localStore;
    private final OrbsNodeUrlProvider orbsEndpoint;
    private String publicAddress;
    private boolean isLoaded;

    OrbsWallet(Store localStore, OrbsNodeUrlProvider orbsEndpoint) {
        this.localStore = localStore;
        this.orbsEndpoint = orbsEndpoint;
    }

    boolean isWalletCreated() {
        return isLoaded ||
                (localStore.getString(KEY_ORBS_PUBLIC_KEY) != null && localStore.getString(KEY_ORBS_PRIVATE_KEY) != null);
    }

    void createWallet() throws Exception {
        throw new Exception("Orbs disabled.");
    }

    void loadWallet() throws Exception {
        throw new Exception("Orbs disabled.");
    }

    String getPublicAddress() {
        return publicAddress;
    }

    String sendOrbs(String toAddress, BigDecimal amount) throws Exception {
        throw new Exception("Orbs disabled.");
    }

    BigDecimal getBalance() throws Exception {
        throw new Exception("Orbs disabled.");
    }

    private class BalanceResult {
        @SerializedName("result")
        float result;
    }

    String fundAccount() throws Exception {
        throw new Exception("Orbs disabled.");
    }
}
