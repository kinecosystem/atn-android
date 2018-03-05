package com.kik.atn;


import android.os.Build;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class Event {

    final static String TYPE_EVENT = "event";
    final static String TYPE_ERROR = "error";
    final static String TYPE_DURATION = "operation_duration";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    private final static DeviceInfo deviceInfo = new DeviceInfo();

    private final String name;
    private final String timestamp;
    private final String type;
    @SerializedName("public_address")
    private final String publicAddress;
    private final DeviceInfo device;
    private final Payload payload = new Payload();


    Event(String name, String type, String publicAddress) {
        this.name = name;
        this.timestamp = dateFormat.format(new Date());
        this.type = type;
        this.publicAddress = publicAddress;
        device = deviceInfo;
    }

    Event addField(String key, Object value) {
        payload.addField(key, value);
        return this;
    }

    Map<String, Object> getFields() {
        return payload.getPayload();
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

    DeviceInfo getDevice() {
        return device;
    }

    private class Payload {
        private final Map<String, Object> payload = new HashMap<>();

        void addField(String key, Object value) {
            payload.put(key, value);
        }

        Map<String, Object> getPayload() {
            return payload;
        }
    }

    @SuppressWarnings("unused")
    static class DeviceInfo {
        @SerializedName("sdk_level")
        private final int sdkLevel;
        private final String model;
        private final String manufacturer;


        private DeviceInfo() {
            this.sdkLevel = Build.VERSION.SDK_INT;
            this.model = Build.MODEL;
            this.manufacturer = Build.MANUFACTURER;
        }

        public int getSdkLevel() {
            return sdkLevel;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public String getModel() {
            return model;
        }
    }
}
