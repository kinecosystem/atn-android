package com.kik.atn;


import com.orbs.client.OrbsClient;
import com.orbs.client.OrbsContract;
import com.orbs.cryptosdk.Address;
import com.orbs.cryptosdk.CryptoSDK;
import com.orbs.cryptosdk.ED25519Key;

import java.math.BigDecimal;

class OrbsWallet {

    static final String KEY_ORBS_PUBLIC_ADDRESS = "key_orbs_public_address";
    static final String KEY_ORBS_PRIVATE_KEY = "key_orbs_private_key";
    private static final String VIRTUAL_CHAIN_ID = "640ed3";
    private static final String NETWORK_ID_TESTNET = "T";
    private static final String CONTRACT_NAME = "";
    private final Store localStore;
    private final String orbsEndpoint;
    private String publicAddress;
    private String privateKey;
    private OrbsClient orbsClient;
    private OrbsContract orbsContract;
    private boolean isLoaded;

    OrbsWallet(Store localStore, String orbsEndpoint) {
        this.localStore = localStore;
        this.orbsEndpoint = orbsEndpoint;
    }

    boolean isWalletCreated() {
        return isLoaded ||
                (localStore.getString(KEY_ORBS_PUBLIC_ADDRESS) != null && localStore.getString(KEY_ORBS_PRIVATE_KEY) != null);
    }

    void createWallet() throws Exception {
        CryptoSDK.initialize();
        ED25519Key key = new ED25519Key();
        publicAddress = key.getPublicKey();
        privateKey = key.getPrivateKeyUnsafe();
        localStore.saveString(KEY_ORBS_PUBLIC_ADDRESS, publicAddress);
        localStore.saveString(KEY_ORBS_PRIVATE_KEY, privateKey);
        initOrbsApis();
    }

    void loadWallet() throws Exception {
        publicAddress = localStore.getString(KEY_ORBS_PUBLIC_ADDRESS);
        privateKey = localStore.getString(KEY_ORBS_PRIVATE_KEY);
        initOrbsApis();
    }

    private void initOrbsApis() throws Exception {
        Address address = new Address(publicAddress, VIRTUAL_CHAIN_ID, NETWORK_ID_TESTNET);
        orbsClient = new OrbsClient(orbsEndpoint, address);
        orbsContract = new OrbsContract(orbsClient, CONTRACT_NAME);
        isLoaded = true;
    }

    String getPublicAddress() {
        return publicAddress;
    }

    void sendOrbs(String address, BigDecimal amount) throws Exception {
        //TODO
    }

    BigDecimal getBalance() {
        //TODO
        return BigDecimal.ZERO;
    }

    void fundAccount() throws Exception {
        //TODO call init function on contract
    }
}
