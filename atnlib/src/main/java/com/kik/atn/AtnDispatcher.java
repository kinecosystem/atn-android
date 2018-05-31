package com.kik.atn;


class AtnDispatcher extends Dispatcher {

    AtnDispatcher(ATNThreadHandler threadHandler, AndroidLogger logger, String dispatcherName) {
        super(threadHandler, logger, dispatcherName);
    }

    @Override
    protected boolean isHandlerBusy() {
        return handler.isAtnBusy();
    }
}
