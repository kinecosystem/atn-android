package com.kik.atn;


import android.support.annotation.Nullable;

interface Store {
    void saveString(String key, String data);

    @Nullable
    String getString(String key);

    void saveLong(String key, long data);

    long getLong(String key);
}
