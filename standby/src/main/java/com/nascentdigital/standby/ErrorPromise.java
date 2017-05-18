package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

public abstract class ErrorPromise<TResult> extends Promise<TResult> {

    // region package private methods

    protected abstract void execute(Rejection rejection);

    // endregion
}
