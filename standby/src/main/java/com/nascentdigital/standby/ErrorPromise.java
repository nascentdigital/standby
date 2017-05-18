package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

class ErrorPromise<TResult> extends Promise<TResult> {

    // region instance variables

    private final ErrorBlock _errorBlock;

    // endregion


    // region constructors

    ErrorPromise(ErrorBlock errorBlock) {

        // call base constructor
        super();

        // initialize instance variables
        _errorBlock = errorBlock;
    }

    // endregion


    // region lifecycle

    protected void execute(Rejection rejection) {

        // if rejection is already consumed, reject and exit
        if (rejection.isConsumed()) {

            onReject(rejection.share());
            return;
        }

        // try to run the block
        try {

            // execute block
            _errorBlock.execute(rejection.error);

            // mark error as consumed if error block did not throw
            rejection.consume();

            // propagate rejection
            onReject(rejection.share());
        }

        // reject on any exceptions
        catch (Exception e) {

            // create a new rejection with the exception that was thrown by the error block
            Rejection errorBlockRejection = new Rejection(e);
            onReject(errorBlockRejection);
        }
    }

    // endregion
}
