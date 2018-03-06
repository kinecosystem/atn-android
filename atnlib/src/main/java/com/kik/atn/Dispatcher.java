package com.kik.atn;


import android.os.Handler;
import android.support.annotation.IntDef;
import android.text.format.DateUtils;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

class Dispatcher {

    static final int MSG_RECEIVE = 0;
    static final int MSG_SENT = 1;
    private static final long DEFAULT_DELAY = 5000;
    private final ATNThreadHandler handler;
    private final AndroidLogger logger;
    private long rateLimitInMillis;
    private long lastAllowedTime;

    @Retention(SOURCE)
    @IntDef({MSG_RECEIVE, MSG_SENT})
    @interface MessageType {

    }

    Dispatcher(ATNThreadHandler threadHandler, AndroidLogger logger) {
        this.handler = threadHandler;
        this.logger = logger;
        this.rateLimitInMillis = DEFAULT_DELAY;
    }

    synchronized void dispatch(Handler handler, @MessageType int msg) {
        if (isAllowed()) {
            handler.sendEmptyMessage(msg);
        }
    }

    void setRateLimit(long rateLimitSec) {
        if (rateLimitSec > 0) {
            this.rateLimitInMillis = rateLimitSec * DateUtils.SECOND_IN_MILLIS;
        }
    }

    private boolean isAllowed() {
        if (handler.getHandler().hasMessages(Dispatcher.MSG_RECEIVE) ||
                handler.getHandler().hasMessages(Dispatcher.MSG_SENT)
                || handler.isBusy()
                || System.currentTimeMillis() - rateLimitInMillis < lastAllowedTime) {
            logger.log("RateLimit - dropping request");
            return false;
        }
        lastAllowedTime = System.currentTimeMillis();
        return true;
    }
}
