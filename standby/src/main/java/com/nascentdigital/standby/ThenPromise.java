package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <TInput>  the type parameter
 * @param <TResult> the type parameter
 */
class ThenPromise<TInput, TResult> extends Promise<TResult> {

    // region instance variables

    private final ThenBlock<TInput> _thenBlock;

    // endregion


    // region constructors

    /**
     * Instantiates a new Then promise.
     *
     * @param thenBlock the then block
     */
    ThenPromise(ThenBlock<TInput> thenBlock) {

        // call base constructor
        super();

        // initialize instance variables
        _thenBlock = thenBlock;
    }

    // endregion


    // region lifecycle

    /**
     * Execute.
     *
     * @param input the input
     */
    protected void execute(TInput input) {

        // try to run the block
        try {

            // execute block to get result
            Object resultOrPromise = _thenBlock.execute(input);

            // process promise
            if (resultOrPromise instanceof Promise) {

                // cast returned promise (may throw)
                final Promise<TResult> promise = (Promise<TResult>)resultOrPromise;

                // resolve current promise when inner promise is completed
                promise.always(new AlwaysBlock() {
                    @Override
                    public void execute() {

                        // resolve if resolved at end
                        if (promise._state == PromiseState.RESOLVED) {
                            onResolve(promise._result);
                        }
                        // reject if rejected at end
                        else if (promise._state == PromiseState.REJECTED) {
                            onReject(promise._rejection.share());
                        }
                        // if always block is called and promise is neither resolved nor rejected
                        // reject with invalid state exception
                        else {
                            onReject(new Rejection(new InvalidPromiseStateException(promise)));
                        }
                    }
                });
            }

            // or process raw result
            else {

                // cast result (may throw)
                TResult result = (TResult)resultOrPromise;

                // resolve using result
                onResolve(result);
            }
        }

        // reject on any exceptions
        catch (Exception e) {

            // set new error on rejection
            Rejection rejection = new Rejection(e);

            // reject promise with rejection
            onReject(rejection);
        }
    }

    // endregion
}
