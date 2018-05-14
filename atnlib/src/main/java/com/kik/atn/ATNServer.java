package com.kik.atn;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class ATNServer {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String URL_CREATE_ACCOUNT = "accounts/%s";
    private static final String URL_FUND = "accounts/%s/fundings";
    private static final String URL_FUND_ORBS = "accounts/%s/fundings";
    private static final String URL_CLAIM_ATN = "accounts/%s/claims";
    private static final String URL_CLAIM_ORBS = "accounts/%s/claims";
    private static final String URL_SEND_EVENT = "events";
    private static final String URL_GET_CONFIGURATION = "accounts/%s/config";
    private final OkHttpClient okHttpClient;
    private final Gson gson;
    private final ATNServerURLProvider urlProvider;

    ATNServer(ATNServerURLProvider urlProvider) {
        this.urlProvider = urlProvider;
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
        gson = new GsonBuilder().create();
    }

    void fundWithXLM(String publicAddress) throws IOException {
        sendPublicAddressRequest(publicAddress, "", URL_CREATE_ACCOUNT);
    }

    void fundWithATN(String publicAddress) throws IOException {
        sendPublicAddressRequest(publicAddress, "", URL_FUND);
    }

    void fundOrbsAccount(String publicAddress) throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO
    }

    void receiveATN(String publicAddress) throws IOException {
        sendPublicAddressRequest(publicAddress, "", URL_CLAIM_ATN);
    }

    void receiveOrbs(String publicAddress) throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO
    }

    void sendEvent(Event event) throws IOException {
        String json = gson.toJson(event, Event.class);
        sendPostRequest(json, URL_SEND_EVENT);
    }

    private void sendPublicAddressRequest(String publicAddress, String requestContent, String urlFormat) throws IOException {
        String url = String.format(urlFormat, publicAddress);
        sendPostRequest(requestContent, url);
    }

    @NonNull
    private RequestBody createRequestBody(@NonNull String string) {
        return RequestBody.create(MEDIA_TYPE_JSON, string);
    }

    private void sendPostRequest(String contents, String restPath) throws IOException {
        Request request = new Request.Builder()
                .url(urlProvider.getUrl() + restPath)
                .post(createRequestBody(contents))
                .build();
        Response response = okHttpClient.newCall(request)
                .execute();
        int code = response.code();
        response.close();
        if (code != 200) {
            throw new HttpResponseException(response.code());
        }
    }

    @Nullable
    Config getConfiguration(String publicAddress) throws IOException {
        Request request = new Request.Builder()
                .url(urlProvider.getUrl() + String.format(URL_GET_CONFIGURATION, publicAddress))
                .get()
                .build();
        Response response = okHttpClient.newCall(request)
                .execute();
        int code = response.code();

        if (code == 200) {
            ResponseBody body = response.body();
            if (body != null) {
                String configString = body.string();
                return gson.fromJson(configString, Config.class);
            } else {
                return null;
            }
        } else {
            throw new HttpResponseException(response.code());
        }

    }

}
