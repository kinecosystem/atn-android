package com.kik.atn;


import java.io.IOException;

import kin.core.KinAccount;
import kin.core.exception.OperationFailedException;

class ATNAccountOnborading {

    private final EventLogger eventLogger;
    private final ATNServer atnServer;

    ATNAccountOnborading(EventLogger eventLogger, ATNServer atnServer) {
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
    }

    boolean onboard(KinAccount account) {
        long start = System.nanoTime();
        if (fundWithXLM(account) && activateAccount(account) && fundWithATN(account)) {
            long duration = (System.nanoTime() - start) / 1000;
            eventLogger.sendDurationEvent("account_created", duration);
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
