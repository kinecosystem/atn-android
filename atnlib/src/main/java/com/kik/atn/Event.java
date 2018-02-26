package com.kik.atn;


import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class Event {

    private final String name;
    private final String timestamp;
    private final String type;
    @SerializedName("public_address")
    private final String publicAddress;
    private final Payload payload = new Payload();


    Event(String name, String type, String publicAddress) {
        this.name = name;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        this.timestamp = simpleDateFormat.format(new Date());
        this.type = type;
        this.publicAddress = publicAddress;
    }

    Event addField(String key, Object value) {
        payload.addField(key, value);
        return this;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getFields() {
        return payload.getPayload();
    }

    private class Payload {
        private final Map<String, Object> payload = new HashMap<>();

        void addField(String key, Object value) {
            payload.put(key, value);
        }

        public Map<String, Object> getPayload() {
            return payload;
        }
    }
}
