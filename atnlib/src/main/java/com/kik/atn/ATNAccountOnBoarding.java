package com.kik.atn;


import java.io.IOException;
import java.math.BigDecimal;

import kin.core.Balance;
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

        if (isOnBoarded(account)) {
            eventLogger.sendEvent("onboard_already_onboarded");
            return true;
        }

        if (fundWithXLM(account) && activateAccount(account) && fundWithATN(account)) {
            durationLogger.report("onboard_succeed");
            return true;
        }
        return false;
    }

    private boolean isOnBoarded(KinAccount account) {
        try {
            Balance balance = account.getBalanceSync();
            if (balance.value().compareTo(new BigDecimal("0.0")) > 0) {
                return true;
            }
        } catch (OperationFailedException e) {
            eventLogger.sendErrorEvent("onboard_is_on_boarded_failed", e);
        }
        return false;
    }


    private boolean fundWithXLM(KinAccount account) {
        try {
            atnServer.fundWithXLM(account.getPublicAddress());
            return true;
        } catch (IOException e) {
            eventLogger.sendErrorEvent("onboard_fund_xlm_failed", e);
        }
        return false;
    }

    private boolean activateAccount(KinAccount account) {
        try {
            account.activateSync("");
            return true;
        } catch (OperationFailedException e) {
            eventLogger.sendErrorEvent("onboard_activate_failed", e);
        }
        return false;
    }

    private boolean fundWithATN(KinAccount account) {
        try {
            atnServer.fundWithATN(account.getPublicAddress());
            return true;
        } catch (IOException e) {
            eventLogger.sendErrorEvent("onboard_fund_atn_failed", e);
        }
        return false;
    }


}
