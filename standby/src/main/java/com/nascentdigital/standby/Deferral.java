package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

public final class Deferral<TResult> {

    // region instance variables

    private final Promise<TResult> _promise;

    // endregion


    // region constructors

    Deferral(Promise<TResult> promise) {
        _promise = promise;
    }

    // endregion


    // region actions

    public void resolve(TResult result) {
        _promise.onResolve(result);
    }

    public void reject(Exception error) {
        _promise.onReject(new Rejection(error));
    }

    // endregion
}
