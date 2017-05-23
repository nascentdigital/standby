package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-23.
 */

public final class PromiseListException extends Exception {

    // region instance variables

    public Promise promise;

    // endregion


    // region constructors

    PromiseListException(Promise promise) {

        // set instance variables
        this.promise = promise;
    }

    // endregion
}
