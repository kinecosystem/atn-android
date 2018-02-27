package com.kik.atn;


import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

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
    private static final String URL_BASE = "http://188.166.34.7:8000/";
    private static final String URL_CREATE_ACCOUNT = URL_BASE + "accounts/%s";
    private static final String URL_FUND = URL_BASE + "accounts/%s/fundings";
    private static final String URL_CLAIM_ATN = URL_BASE + "accounts/%s/claims";
    private static final String URL_SEND_EVENT = URL_BASE + "events";
    private static final String URL_GET_CONFIGURATION = URL_BASE + "config/%s";
    private final OkHttpClient okHttpClient;
    private final Gson gson;

    ATNServer() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
        gson = new GsonBuilder().create();

    }

    void fundWithXLM(String publicAddress) throws IOException {
        FundWithXlmRequest fundWithXlmRequest = new FundWithXlmRequest();
        String requestContent = gson.toJson(fundWithXlmRequest, FundWithXlmRequest.class);
        sendPublicAddressRequest(publicAddress, requestContent, URL_CREATE_ACCOUNT);
    }

    void fundWithATN(String publicAddress) throws IOException {
        sendPublicAddressRequest(publicAddress, "", URL_FUND);
    }

    void receiveATN(String publicAddress) throws IOException {
        sendPublicAddressRequest(publicAddress, "", URL_CLAIM_ATN);
    }

    void sendEvent(Event event) throws IOException {
        String json = gson.toJson(event, event.getClass());
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

    private void sendPostRequest(String contents, String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
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
                .url(String.format(URL_GET_CONFIGURATION, publicAddress))
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

    @SuppressWarnings("unused")
    private static class FundWithXlmRequest {
        @SerializedName("sdk_level")
        private final int sdkLevel;
        private final String model;
        private final String manufacturer;


        private FundWithXlmRequest() {
            this.sdkLevel = Build.VERSION.SDK_INT;
            this.model = Build.MODEL;
            this.manufacturer = Build.MANUFACTURER;
        }
    }
}
