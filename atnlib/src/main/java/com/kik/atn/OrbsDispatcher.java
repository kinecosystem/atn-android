package com.kik.atn;


class OrbsDispatcher extends Dispatcher {
    OrbsDispatcher(ATNThreadHandler threadHandler, AndroidLogger logger, String dispatcherName) {
        super(threadHandler, logger, dispatcherName);
    }

    @Override
    protected boolean isHandlerBusy() {
        return handler.isOrbsBusy();
    }
}
