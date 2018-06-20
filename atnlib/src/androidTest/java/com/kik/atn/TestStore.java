package com.kik.atn;


import android.content.Context;

class TestStore extends LocalStore {

    TestStore(Context context) {
        super(context);
    }

    void clearAll() {
        sharedPref.edit().clear().commit();
    }
}
