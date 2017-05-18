package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

public final class InvalidPromiseStateException extends Exception {

    // region instance variables

    private Promise _promise;

    // endregion


    // region constructors

    InvalidPromiseStateException(Promise promise) {
        _promise = promise;
    }

    // endregion
}
