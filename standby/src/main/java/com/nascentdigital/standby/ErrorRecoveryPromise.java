package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

class ErrorRecoveryPromise<TResult> extends ErrorPromise<TResult> {

    ErrorRecoveryPromise(ErrorBlock errorBlock) {
        super(errorBlock);
    }
}
