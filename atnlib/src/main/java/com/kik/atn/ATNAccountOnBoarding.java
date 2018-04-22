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
    private final int STATE_NOT_CREATED = 1;
    private final int STATE_NOT_ACTIVATED = 2;
    private final int STATE_NOT_FUNDED = 3;
    private final int STATE_ONBOARDED = 4;
    private final int STATE_ERROR = 5;

    ATNAccountOnBoarding(EventLogger eventLogger, ATNServer atnServer) {
        this.eventLogger = eventLogger;
        this.atnServer = atnServer;
    }

    boolean onBoard(KinAccount account) {
        eventLogger.sendEvent("onboard_started");
        EventLogger.DurationLogger durationLogger = eventLogger.startDurationLogging();

        switch (getOnboradingState(account)) {
            case STATE_ONBOARDED:
                eventLogger.sendEvent("onboard_already_onboarded");
                return true;
            case STATE_NOT_CREATED:
                eventLogger.sendEvent("onboard_account_not_created");
                if (fundWithXLM(account) && activateAccount(account) && fundWithATN(account)) {
                    durationLogger.report("onboard_succeeded");
                    return true;
                }
                break;
            case STATE_NOT_ACTIVATED:
                eventLogger.sendEvent("onboard_trustline_not_set");
                if (activateAccount(account) && fundWithATN(account)) {
                    durationLogger.report("onboard_succeeded");
                    return true;
                }
                break;
            case STATE_NOT_FUNDED:
                eventLogger.sendEvent("onboard_account_not_funded");
                if (fundWithATN(account)) {
                    durationLogger.report("onboard_succeeded");
                    return true;
                }
                break;
            case STATE_ERROR:
                return false;
        }
        durationLogger.report("onboard_failed");
        return false;
    }

    private int getOnboradingState(KinAccount account) {
        try {
            Balance balance = account.getBalanceSync();
            if (balance.value().compareTo(new BigDecimal("0.0")) > 0) {
                return STATE_ONBOARDED;
            } else {
                return STATE_NOT_FUNDED;
            }
        } catch (AccountNotFoundException e) {
            return STATE_NOT_CREATED;
        } catch (AccountNotActivatedException e) {
            return STATE_NOT_ACTIVATED;
        } catch (OperationFailedException ofe) {
            eventLogger.sendErrorEvent("onboard_is_on_boarded_failed", ofe);
        }
        return STATE_ERROR;
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
            account.activateSync();
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
