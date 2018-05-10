package com.kik.atn;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

class LocalStore {
    private static final String SHARED_PREF_NAME = "atn_data";
    private final SharedPreferences sharedPref;

    LocalStore(Context context) {
        sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    void saveString(String key, String data) {
        sharedPref.edit().putString(key, data).apply();
    }

    @Nullable
    String getString(String key) {
        return sharedPref.getString(key, null);
    }
}
