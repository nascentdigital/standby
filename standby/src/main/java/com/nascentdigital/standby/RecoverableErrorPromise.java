package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <TResult> the type parameter
 */
class RecoverableErrorPromise<TResult> extends ErrorPromise<TResult> {

    // region instance variables

    private final RecoveryBlock _recoveryBlock;

    // endregion


    // region constructors

    /**
     * Instantiates a new Recoverable error promise.
     *
     * @param recoveryBlock the recovery block
     */
    RecoverableErrorPromise(RecoveryBlock recoveryBlock) {

        // call base constructor
        super();

        // initialize instance variables
        _recoveryBlock = recoveryBlock;
    }

    // endregion


    // region overrides

    @Override
    protected void execute(Rejection rejection) {

        // if rejection is already consumed, reject and exit
        if (rejection.isConsumed()) {

            onReject(rejection.share());
            return;
        }

        // try to run the block
        try {

            // instantiate new recovery
            Recovery<TResult> recovery = new Recovery<>();

            // execute recovery block with rejection error and recovery object
            _recoveryBlock.execute(rejection.error, recovery);

            // check if value was set on recovery
            if (recovery.hasSetValue()) {

                // resolve with new value and exit
                onResolve(recovery.getValue());
                return;
            }

            // mark error as consumed if recovery block did not throw and did not set a value
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
