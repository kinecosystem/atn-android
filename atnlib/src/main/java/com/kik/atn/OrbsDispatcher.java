package com.kik.atn;


class OrbsDispatcher extends Dispatcher {
    OrbsDispatcher(ATNThreadHandler threadHandler, AndroidLogger logger, Store store, String dispatcherName) {
        super(threadHandler, logger, store, dispatcherName);
    }

    @Override
    protected boolean isHandlerBusy() {
        return handler.isOrbsBusy();
    }
}
