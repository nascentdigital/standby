package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */
class Rejection {

    // region instance variables

    /**
     * The Error.
     */
    final Exception error;
    private boolean _consumed;

    // endregion


    // region constructors

    /**
     * Instantiates a new Rejection.
     *
     * @param error the error
     */
    Rejection(Exception error) {
        this.error = error;
    }

    // endregion


    // region lifecycle

    /**
     * Is consumed boolean.
     *
     * @return the boolean
     */
    boolean isConsumed() {
        return _consumed;
    }

    /**
     * Consume.
     */
    void consume() {
        _consumed = true;
    }

    /**
     * Share rejection.
     *
     * @return the rejection
     */
    Rejection share() {
        return _consumed
            ? this
            : new Rejection(error);
    }

    // endregion
}
