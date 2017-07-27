package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <TResult> the type parameter
 */
public abstract class ErrorPromise<TResult> extends Promise<TResult> {

    // region package private methods

    /**
     * Execute.
     *
     * @param rejection the rejection
     */
    protected abstract void execute(Rejection rejection);

    // endregion
}
