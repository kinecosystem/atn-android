package com.kik.atn;


import android.os.Build;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

class Event {

    final static String TYPE_EVENT = "event";
    final static String TYPE_ERROR = "error";
    final static String TYPE_DURATION = "operation_duration";
    private final static SimpleDateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @SerializedName("event_name")
    private final String name;
    private final String timestamp;
    @SerializedName("event_type")
    private final String type;
    @SerializedName("public_address")
    private final String publicAddress;
    @SerializedName("sdk_level")
    private final int sdkLevel;
    @SerializedName("device_model")
    private final String model;
    @SerializedName("device_manufacturer")
    private final String manufacturer;
    private final Map<String, Object> payload = new HashMap<>();

    Event(String name, String type, String publicAddress) {
        this.name = name;
        this.timestamp = dateFormat.format(new Date());
        this.type = type;
        this.publicAddress = publicAddress;
        this.sdkLevel = Build.VERSION.SDK_INT;
        this.model = Build.MODEL;
        this.manufacturer = Build.MANUFACTURER;

    }

    Event addField(String key, Object value) {
        payload.put(key, value);
        return this;
    }

    Map<String, Object> getFields() {
        return payload;
    }

    String getName() {
        return name;
    }

    String getPublicAddress() {
        return publicAddress;
    }

    String getType() {
        return type;
    }

    String getTimestamp() {
        return timestamp;
    }

    int getSdkLevel() {
        return sdkLevel;
    }

    String getManufacturer() {
        return manufacturer;
    }

    String getModel() {
        return model;
    }

}
