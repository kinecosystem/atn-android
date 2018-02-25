package com.kik.atn;


import android.os.Handler;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

class Dispatcher {

    static final int MSG_RECIEVE = 0;
    static final int MSG_SENT = 1;

    public Dispatcher() {
    }

    @Retention(SOURCE)
    @IntDef({MSG_RECIEVE, MSG_SENT})
    @interface MessageType {
    }

    void dispatch(Handler handler, @MessageType int msg) {
        handler.sendEmptyMessage(msg);
    }
}
