package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

class Rejection {

    // region instance variables

    final Exception error;
    private boolean _consumed;

    // endregion


    // region constructors

    Rejection(Exception error) {
        this.error = error;
    }

    // endregion


    // region lifecycle

    boolean isConsumed() {
        return _consumed;
    }

    void consume() {
        _consumed = true;
    }

    Rejection share() {
        return _consumed
            ? this
            : new Rejection(error);
    }

    // endregion
}
