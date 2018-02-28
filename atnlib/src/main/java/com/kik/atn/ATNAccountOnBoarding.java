package com.kik.atn;


import java.io.IOException;

import kin.core.KinAccount;
import kin.core.exception.OperationFailedException;

class ATNAccountOnBoarding {

    private final EventLogger eventLogger;
    private final ATNServer atnServer;

    ATNAccountOnBoarding(EventLogger eventLogger, ATNServer atnServer) {
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
    }

    boolean onBoard(KinAccount account) {
        eventLogger.sendEvent("onboard_started");
        EventLogger.DurationLogger durationLogger = eventLogger.startDurationLogging();
        if (fundWithXLM(account) && activateAccount(account) && fundWithATN(account)) {
            durationLogger.report("account_created");
            return true;
        }
        return false;
    }


    private boolean fundWithXLM(KinAccount account) {
        try {
            atnServer.fundWithXLM(account.getPublicAddress());
            return true;
        } catch (IOException e) {
            eventLogger.sendErrorEvent("fund_xlm_failed", e);
        }
        return false;
    }

    private boolean activateAccount(KinAccount account) {
        try {
            account.activateSync("");
            return true;
        } catch (OperationFailedException e) {
            eventLogger.sendErrorEvent("activate_failed", e);
        }
        return false;
    }

    private boolean fundWithATN(KinAccount account) {
        try {
            atnServer.fundWithATN(account.getPublicAddress());
            return true;
        } catch (IOException e) {
            eventLogger.sendErrorEvent("fund_xlm_failed", e);
        }
        return false;
    }


}
