package com.kik.atn;


class AtnDispatcher extends Dispatcher {

    AtnDispatcher(ATNThreadHandler threadHandler, AndroidLogger logger, Store store, String dispatcherName) {
        super(threadHandler, logger, store, dispatcherName);
    }

    @Override
    protected boolean isHandlerBusy() {
        return handler.isAtnBusy();
    }
}
