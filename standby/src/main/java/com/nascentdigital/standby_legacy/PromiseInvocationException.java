package com.nascentdigital.standby_legacy;

/**
 * Created by tomwark on 2017-04-12.
 */

public final class PromiseInvocationException extends Exception {

    public Exception originalException;

    public PromiseInvocationException(Exception e) {
        this.originalException = e;
    }
}
