package com.kik.atn;


import com.orbs.client.OrbsClient;
import com.orbs.client.OrbsContract;
import com.orbs.client.SendTransactionResponse;
import com.orbs.cryptosdk.Address;
import com.orbs.cryptosdk.CryptoSDK;
import com.orbs.cryptosdk.ED25519Key;

import java.math.BigDecimal;

class OrbsWallet {

    static final String KEY_ORBS_PUBLIC_ADDRESS = "key_orbs_public_address";
    static final String KEY_ORBS_PRIVATE_KEY = "key_orbs_private_key";
    private static final String VIRTUAL_CHAIN_ID = "640ed3";
    private static final String NETWORK_ID_TESTNET = "T";
    private static final String CONTRACT_NAME = "kinatn";
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
        ED25519Key keyPair = new ED25519Key();
        publicAddress = keyPair.getPublicKey();
        privateKey = keyPair.getPrivateKeyUnsafe();
        localStore.saveString(KEY_ORBS_PUBLIC_ADDRESS, publicAddress);
        localStore.saveString(KEY_ORBS_PRIVATE_KEY, privateKey);
        initOrbsApis(keyPair);
    }

    void loadWallet() throws Exception {
        CryptoSDK.initialize();
        publicAddress = localStore.getString(KEY_ORBS_PUBLIC_ADDRESS);
        privateKey = localStore.getString(KEY_ORBS_PRIVATE_KEY);
        if (publicAddress != null && privateKey != null) {
            ED25519Key keyPair = new ED25519Key(publicAddress, privateKey);
            initOrbsApis(keyPair);
        } else {
            throw new IllegalStateException("Error loading wallet, publicAddress = " + publicAddress + ", privateKey = " + privateKey);
        }
    }

    private void initOrbsApis(ED25519Key keyPair) throws Exception {
        Address address = new Address(publicAddress, VIRTUAL_CHAIN_ID, NETWORK_ID_TESTNET);
        orbsClient = new OrbsClient(orbsEndpoint, address, keyPair);
        orbsContract = new OrbsContract(orbsClient, CONTRACT_NAME);
        isLoaded = true;
    }

    String getPublicAddress() {
        return publicAddress;
    }

    void sendOrbs(String toAddress, BigDecimal amount) throws Exception {
        SendTransactionResponse response = orbsContract.sendTransaction("transfer",
                new Object[]{toAddress, amount.toPlainString()});
        if (response == null) {
            throw new Exception("transaction response is null");
        }
        if (response.transactionId == null || response.transactionId.isEmpty()) {
            throw new Exception("transaction transactionId is null or empty");
        }
    }

    BigDecimal getBalance() throws Exception {
        String getBalance = orbsContract.call("getBalance", null);
        return BigDecimal.ZERO;
    }

    void fundAccount() throws Exception {
        String response = orbsContract.call("financeAccount", new Object[]{publicAddress});
    }
}
