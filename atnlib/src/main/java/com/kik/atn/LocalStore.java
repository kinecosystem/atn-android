package com.kik.atn;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

class LocalStore implements Store {
    private static final String SHARED_PREF_NAME = "atn_data";
    final SharedPreferences sharedPref;

    LocalStore(Context context) {
        sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void saveString(String key, String data) {
        sharedPref.edit().putString(key, data).apply();
    }

    @Override
    @Nullable
    public String getString(String key) {
        return sharedPref.getString(key, null);
    }

    @Override
    public void saveLong(String key, long data) {
        sharedPref.edit().putLong(key, data).apply();
    }

    @Override
    public long getLong(String key) {
        return sharedPref.getLong(key, 0L);
    }
}
