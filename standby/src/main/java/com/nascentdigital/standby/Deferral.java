package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <TResult> the type parameter
 */
public final class Deferral<TResult> {

    // region instance variables

    private final Promise<TResult> _promise;

    // endregion


    // region constructors

    /**
     * Instantiates a new Deferral.
     *
     * @param promise the promise
     */
    Deferral(Promise<TResult> promise) {
        _promise = promise;
    }

    // endregion


    // region actions

    /**
     * Resolve.
     *
     * @param result Value to resolve the {@link Promise} with
     */
    public void resolve(TResult result) {
        _promise.onResolve(result);
    }

    /**
     * Reject.
     *
     * @param error Error to reject the {@link Promise} with
     */
    public void reject(Exception error) {
        _promise.onReject(new Rejection(error));
    }

    // endregion
}
