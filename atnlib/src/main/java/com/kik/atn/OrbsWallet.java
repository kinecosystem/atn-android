package com.kik.atn;


import com.orbs.client.OrbsClient;
import com.orbs.client.OrbsContract;
import com.orbs.cryptosdk.Address;
import com.orbs.cryptosdk.CryptoSDK;
import com.orbs.cryptosdk.ED25519Key;

import java.math.BigDecimal;

class OrbsWallet {

    private static final String KEY_ORBS_PUBLIC_ADDRESS = "key_orbs_public_address";
    private static final String KEY_ORBS_PRIVATE_KEY = "key_orbs_private_key";
    private static final String VIRTUAL_CHAIN_ID = "640ed3";
    private static final String NETWORK_ID_TESTNET = "T";
    private static final String CONTRACT_NAME = "";
    private final LocalStore localStore;
    private final String orbsEndpoint;
    private String publicAddress;
    private String privateKey;
    private OrbsClient orbsClient;
    private OrbsContract orbsContract;
    private boolean isLoaded;

    OrbsWallet(LocalStore localStore, String orbsEndpoint) {
        this.localStore = localStore;
        this.orbsEndpoint = orbsEndpoint;
    }

    void loadWallet() throws Exception {
        if (isLoaded) {
            return;
        }
        isLoaded = true;
        CryptoSDK.initialize();

        publicAddress = localStore.getString(KEY_ORBS_PUBLIC_ADDRESS);
        privateKey = localStore.getString(KEY_ORBS_PRIVATE_KEY);
        if (publicAddress == null) {
            ED25519Key key = new ED25519Key();
            publicAddress = key.getPublicKey();
            privateKey = key.getPrivateKeyUnsafe();
        }
        Address address = new Address(publicAddress, VIRTUAL_CHAIN_ID, NETWORK_ID_TESTNET);
        orbsClient = new OrbsClient(orbsEndpoint, address);
        orbsContract = new OrbsContract(orbsClient, CONTRACT_NAME);
    }

    String getPublicAddress() {
        return publicAddress;
    }

    void sendOrbs() {
        //TODO
        //orbsContract.sendTransaction();
    }

    BigDecimal getBalance() {
        //TODO
        return BigDecimal.ZERO;
    }
}
