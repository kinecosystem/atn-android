package com.kik.atn;


import java.io.IOException;
import java.math.BigDecimal;

import kin.core.Balance;
import kin.core.KinAccount;
import kin.core.exception.AccountNotActivatedException;
import kin.core.exception.AccountNotFoundException;
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
            durationLogger.report("onboard_succeeded");
            return true;
        }
        durationLogger.report("onboard_failed");
        return false;
    }

    private boolean isOnBoarded(KinAccount account) {
        try {
            Balance balance = account.getBalanceSync();
            if (balance.value().compareTo(new BigDecimal("0.0")) > 0) {
                return true;
            }
        } catch (AccountNotFoundException | AccountNotActivatedException e) {
            return false;
        } catch (OperationFailedException ofe) {
            eventLogger.sendErrorEvent("onboard_is_on_boarded_failed", ofe);
        }
        return false;
    }


    private boolean fundWithXLM(KinAccount account) {
        try {
            atnServer.fundWithXLM(account.getPublicAddress());
            eventLogger.sendEvent("account_creation_succeeded");
            return true;
        } catch (IOException e) {
            eventLogger.sendErrorEvent("account_creation_failed", e);
        }
        return false;
    }

    private boolean activateAccount(KinAccount account) {
        try {
            account.activateSync("");
            eventLogger.sendEvent("trustline_setup_succeeded");
            return true;
        } catch (OperationFailedException e) {
            eventLogger.sendErrorEvent("trustline_setup_failed", e);
        }
        return false;
    }

    private boolean fundWithATN(KinAccount account) {
        try {
            atnServer.fundWithATN(account.getPublicAddress());
            eventLogger.sendEvent("account_funding_succeeded");
            return true;
        } catch (IOException e) {
            eventLogger.sendErrorEvent("account_funding_failed", e);
        }
        return false;
    }


}
