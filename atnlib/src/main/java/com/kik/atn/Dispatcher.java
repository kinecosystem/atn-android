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
    private static final long DEFAULT_DELAY = 5000;
    protected final ATNThreadHandler handler;
    private final AndroidLogger logger;
    private final String dispatcherName;
    private long rateLimitInMillis;
    private long lastAllowedTime;

    @Retention(SOURCE)
    @IntDef({MSG_RECEIVE, MSG_SENT, MSG_RECEIVE_ORBS, MSG_SENT_ORBS})
    @interface MessageType {
    }

    Dispatcher(ATNThreadHandler threadHandler, AndroidLogger logger, String dispatcherName) {
        this.handler = threadHandler;
        this.logger = logger;
        this.dispatcherName = dispatcherName;
        this.rateLimitInMillis = DEFAULT_DELAY;
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
        return true;
    }

    protected abstract boolean isHandlerBusy();
}
