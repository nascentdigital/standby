package com.nascentdigital.standby_legacy;

/**
 * Created by tomwark on 2017-04-21.
 */

public final class NullErrorAndValueException extends Exception {
    String message;
    String stackTrace;

    public NullErrorAndValueException(String message, String stackTrace) {
        this.message = message;
        this.stackTrace = stackTrace;
    }
}
