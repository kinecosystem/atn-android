package com.kik.atn;


import android.support.annotation.IntDef;
import android.text.format.DateUtils;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

abstract class Dispatcher {

    static final int MSG_RECEIVE = 0;
    static final int MSG_SENT = 1;
    static final int MSG_RECEIVE_ORBS = 2;
    static final int MSG_SENT_ORBS = 3;
    private static final String KEY_LAST_ALLOWED_TIME = "key_last_allowed_time";
    private static final long DEFAULT_DELAY = 5000;
    final ATNThreadHandler handler;
    private final String keyLastAllowedTime;
    private final AndroidLogger logger;
    private final String dispatcherName;
    private final Store store;
    private long rateLimitInMillis;
    private long lastAllowedTime;

    @Retention(SOURCE)
    @IntDef({MSG_RECEIVE, MSG_SENT, MSG_RECEIVE_ORBS, MSG_SENT_ORBS})
    @interface MessageType {
    }

    Dispatcher(ATNThreadHandler threadHandler, AndroidLogger logger, Store store, String dispatcherName) {
        this.handler = threadHandler;
        this.logger = logger;
        this.dispatcherName = dispatcherName;
        this.rateLimitInMillis = DEFAULT_DELAY;
        this.store = store;
        keyLastAllowedTime = KEY_LAST_ALLOWED_TIME + dispatcherName;
        lastAllowedTime = store.getLong(keyLastAllowedTime);
    }

    synchronized void dispatch(@MessageType int msg) {
        if (isAllowed()) {
            handler.getHandler().sendEmptyMessage(msg);
        }
    }

    void setRateLimit(long rateLimitSec) {
        if (rateLimitSec > 0) {
            this.rateLimitInMillis = rateLimitSec * DateUtils.SECOND_IN_MILLIS;
        }
    }

    private boolean isAllowed() {
        if (isHandlerBusy()
                || System.currentTimeMillis() - rateLimitInMillis < lastAllowedTime) {
            logger.log("Dispatcher (" + dispatcherName + ") rate limit - dropping request ");
            return false;
        }
        lastAllowedTime = System.currentTimeMillis();
        store.saveLong(keyLastAllowedTime, lastAllowedTime);
        return true;
    }

    protected abstract boolean isHandlerBusy();
}
