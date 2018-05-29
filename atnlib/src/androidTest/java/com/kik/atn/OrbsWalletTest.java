package com.kik.atn;

import com.orbs.cryptosdk.Address;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OrbsWalletTest {

    private final static OrbsNodeUrlProvider ORBS_URL = new OrbsNodeUrlProvider();
    private final static String PRIVATE_KEY = "5756a055b8f2aa7247cddaf9a441f8103a95ffe60e9f43d7fc29c7eecd8160c0";
    private final static String PUBLIC_KEY = "18084d8948e4fa9283cc96cb72012b5dfa91cdccf52f420e7ec185ff2b4a2723";
    private FakeStore store;
    private MockWebServer mockWebServer;
    private OrbsNodeUrlProvider mockNodeUrlProvider;

    @Test
    public void isWalletCreated_NoStoreData_False() {
        store = new FakeStore();

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);

        assertThat(wallet.isWalletCreated(), is(false));
    }

    @Test
    public void isWalletCreated_StoreHasData_True() {
        mockStoreWithLoadedWallet();

        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);

        assertThat(wallet.isWalletCreated(), is(true));
    }

    private void mockStoreWithLoadedWallet() {
        store = new FakeStore();
        store.saveString(OrbsWallet.KEY_ORBS_PRIVATE_KEY, PRIVATE_KEY);
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_KEY, PUBLIC_KEY);
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
        mockStoreWithLoadedWallet();
        OrbsWallet wallet = new OrbsWallet(store, ORBS_URL);
        wallet.loadWallet();

        assertThat(wallet.isWalletCreated(), is(true));
        Address address = new Address(PUBLIC_KEY, OrbsWallet.VIRTUAL_CHAIN_ID, OrbsWallet.NETWORK_ID);
        assertThat(wallet.getPublicAddress(), equalTo(address.toString()));
    }

    @Test(expected = Exception.class)
    public void loadWallet_StoreHasInvalidData_Failure() throws Exception {
        store = new FakeStore();

        store.saveString(OrbsWallet.KEY_ORBS_PRIVATE_KEY, PRIVATE_KEY);
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_KEY, "invalid public key format");
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
        store.saveString(OrbsWallet.KEY_ORBS_PUBLIC_KEY, PUBLIC_KEY);

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

    @Test
    public void getBalance_GotResult_Success() throws Exception {
        initMockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"result\" : 21}"));

        mockStoreWithLoadedWallet();

        OrbsWallet wallet = new OrbsWallet(store, mockNodeUrlProvider);
        wallet.loadWallet();
        BigDecimal balance = wallet.getBalance();
        assertThat(balance, equalTo(new BigDecimal("21")));
    }

    @Test(expected = Exception.class)
    public void getBalance_IOException_Failed() throws Exception {
        initMockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        mockStoreWithLoadedWallet();

        OrbsWallet wallet = new OrbsWallet(store, mockNodeUrlProvider);
        wallet.loadWallet();
        wallet.getBalance();
    }

    @Test
    public void fundAccount_GotResult_Success() throws Exception {
        initMockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"transactionId\" : \"someid\"}"));

        mockStoreWithLoadedWallet();

        OrbsWallet wallet = new OrbsWallet(store, mockNodeUrlProvider);
        wallet.loadWallet();
        wallet.fundAccount();
    }

    @Test(expected = Exception.class)
    public void fundAccount_IOException_Failed() throws Exception {
        initMockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        mockStoreWithLoadedWallet();

        OrbsWallet wallet = new OrbsWallet(store, mockNodeUrlProvider);
        wallet.loadWallet();
        wallet.fundAccount();
    }

    @Test
    public void sendOrbs_GotResult_Success() throws Exception {
        initMockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"transactionId\" : \"someid\"}"));

        mockStoreWithLoadedWallet();

        OrbsWallet wallet = new OrbsWallet(store, mockNodeUrlProvider);
        wallet.loadWallet();
        wallet.sendOrbs("someAddress", new BigDecimal(1));
    }

    @Test(expected = Exception.class)
    public void sendOrbs_IOException_Failed() throws Exception {
        initMockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        mockStoreWithLoadedWallet();

        OrbsWallet wallet = new OrbsWallet(store, mockNodeUrlProvider);
        wallet.loadWallet();
        wallet.sendOrbs("someAddress", new BigDecimal(1));
    }

    private void initMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        final HttpUrl url = mockWebServer.url("");
        mockNodeUrlProvider = new OrbsNodeUrlProvider() {
            @Override
            boolean isHttps() {
                return url.isHttps();
            }

            @Override
            String getHost() {
                return url.host();
            }

            @Override
            int getPort() {
                return url.port();
            }
        };
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