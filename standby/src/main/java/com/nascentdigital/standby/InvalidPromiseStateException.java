package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 * An exception used if a promise is ever in a state that it should not be in.
 */
public final class InvalidPromiseStateException extends Exception {

    // region instance variables

    private Promise _promise;

    // endregion


    // region constructors

    /**
     * Instantiates a new Invalid promise state exception.
     *
     * @param promise the promise
     */
    InvalidPromiseStateException(Promise promise) {
        _promise = promise;
    }

    // endregion
}
