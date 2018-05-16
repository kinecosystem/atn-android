package com.kik.atn;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OrbsWalletTest {

    private final static String ORBS_URL = "www.orbs.com";
    private final static String PRIVATE_KEY = "5756a055b8f2aa7247cddaf9a441f8103a95ffe60e9f43d7fc29c7eecd8160c0";
    private final static String PUBLIC_ADDRESS = "18084d8948e4fa9283cc96cb72012b5dfa91cdccf52f420e7ec185ff2b4a2723";
    private FakeStore store;

    @Test
    public void isWalletCreated_NoStoreData_False() {
        store = new FakeStore();

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);

        assertThat(wallet.isWalletCreated(), is(false));
    }

    @Test
    public void isWalletCreated_StoreHasData_True() {
        store = new FakeStore();
        store.saveString(OrbsWallet.KEY_ORBS_PRIVATE_KEY, PRIVATE_KEY);
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_ADDRESS, PUBLIC_ADDRESS);

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);

        assertThat(wallet.isWalletCreated(), is(true));
    }

    @Test
    public void createWallet_Success() throws Exception {
        store = new FakeStore();

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);
        wallet.createWallet();

        assertThat(wallet.isWalletCreated(), is(true));
        assertThat(wallet.getPublicAddress(), notNullValue());
    }

    @Test
    public void loadWallet_StoreHasData_Success() throws Exception {
        store = new FakeStore();

        store.saveString(OrbsWallet.KEY_ORBS_PRIVATE_KEY, PRIVATE_KEY);
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_ADDRESS, PUBLIC_ADDRESS);
        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);
        wallet.loadWallet();

        assertThat(wallet.isWalletCreated(), is(true));
        assertThat(wallet.getPublicAddress(), equalTo(PUBLIC_ADDRESS));
    }

    @Test(expected = Exception.class)
    public void loadWallet_StoreHasInvalidData_Failure() throws Exception {
        store = new FakeStore();

        store.saveString(OrbsWallet.KEY_ORBS_PRIVATE_KEY, PRIVATE_KEY);
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_ADDRESS, "invalid public key format");
        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);
        wallet.loadWallet();

        assertThat(wallet.isWalletCreated(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void loadWallet_NoStoreData_Failure() throws Exception {
        store = new FakeStore();

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);
        wallet.loadWallet();

        assertThat(wallet.isWalletCreated(), is(false));
        assertThat(wallet.getPublicAddress(), nullValue());
    }

    @Test(expected = IllegalStateException.class)
    public void loadWallet_OnlyPublicAddress_Failure() throws Exception {
        store = new FakeStore();
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_ADDRESS, PUBLIC_ADDRESS);

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);
        wallet.loadWallet();

        assertThat(wallet.isWalletCreated(), is(false));
        assertThat(wallet.getPublicAddress(), nullValue());
    }

    @Test(expected = IllegalStateException.class)
    public void loadWallet_OnlyPrivateKey_Failure() throws Exception {
        store = new FakeStore();
        store.saveString(OrbsWallet.KEY_ORBS_PRIVATE_KEY, PRIVATE_KEY);

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);
        wallet.loadWallet();

        assertThat(wallet.isWalletCreated(), is(false));
        assertThat(wallet.getPublicAddress(), nullValue());
    }

    public static class FakeStore implements Store {

        private HashMap<String, String> map = new HashMap<>();

        @Override
        public void saveString(String key, String data) {
            map.put(key, data);
        }

        @Override
        public String getString(String key) {
            return map.get(key);
        }
    }
}