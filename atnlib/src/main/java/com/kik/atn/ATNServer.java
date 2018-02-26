package com.kik.atn;


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
    private static final String URL_CREATE_ACCOUNT = "http://188.166.34.7:8000/create_account";
    private static final String URL_FUND = "http://188.166.34.7:8000/fund";
    private static final String URL_SEND_EVENT = "http://188.166.34.7:8000/fund";
    private static final String URL_GET_CONFIGURATION = "http://188.166.34.7:8000/fund";
    private static final String JSON_KEY_PUBLIC_ADDRESS = "public_address";
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
        sendPublicAddressRequest(publicAddress, URL_CREATE_ACCOUNT);
    }

    void fundWithATN(String publicAddress) throws IOException {
        sendPublicAddressRequest(publicAddress, URL_FUND);
    }

    void receiveATN(String publicAddress) throws IOException {
        sendPublicAddressRequest(publicAddress, URL_FUND);
    }

    void sendEvent(Event event) throws IOException {
        String json = gson.toJson(event, event.getClass());
        sendPostRequest(json, URL_SEND_EVENT);
    }

    private void sendPublicAddressRequest(String publicAddress, String url) throws IOException {
        PublicKeyQuery publicKeyQuery = new PublicKeyQuery(publicAddress);
        String json = gson.toJson(publicKeyQuery, publicKeyQuery.getClass());
        sendPostRequest(json, url);
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
            throw new HttpResponseException(response.code(), "Error Response code " + response.code());
        }
    }

    @Nullable
    Event getConfiguration() throws IOException {
        Request request = new Request.Builder()
                .url(URL_GET_CONFIGURATION)
                .get()
                .build();
        Response response = okHttpClient.newCall(request)
                .execute();
        int code = response.code();

        if (code == 200) {
            ResponseBody body = response.body();
            if (body != null) {
                String configString = body.string();
                return gson.fromJson(configString, Event.class);
            } else {
                return null;
            }
        } else {
            throw new HttpResponseException(response.code(), "Error Response code " + response.code());
        }

    }

    private static class PublicKeyQuery {

        @SerializedName("public_address")
        private final String publicAddress;

        PublicKeyQuery(String publicAddress) {
            this.publicAddress = publicAddress;
        }

        public String getPublicAddress() {
            return publicAddress;
        }
    }

    class HttpResponseException extends IOException {
        private final int statusCode;

        public HttpResponseException(int statusCode, String msg) {
            super(msg);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return this.statusCode;
        }
    }
}
