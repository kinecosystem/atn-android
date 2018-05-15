package com.kik.atn;

import android.support.annotation.Nullable;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OrbsWalletTest {

    private final static String ORBS_URL = "www.orbs.com";
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
        store.saveString(OrbsWallet.KEY_ORBS_PRIVATE_KEY, "private key");
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_ADDRESS, "public address");

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
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_ADDRESS, "public address");

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);
        wallet.loadWallet();

        assertThat(wallet.isWalletCreated(), is(false));
        assertThat(wallet.getPublicAddress(), nullValue());
    }

    @Test(expected = IllegalStateException.class)
    public void loadWallet_OnlyPRivateKey_Failure() throws Exception {
        store = new FakeStore();
        store.saveString(OrbsWallet.KEY_ORBS_PRIVATE_KEY, "private key");

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

        @Nullable
        @Override
        public String getString(String key) {
            return map.get(key);
        }
    }
}