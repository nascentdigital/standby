package com.nascentdigital.standby;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <TResult> the type parameter
 */
class AllPromise<TResult> extends Promise<TResult> {

    // region instance variables

    private final Promise[] _promiseList;
    private AtomicReferenceArray _values;
    private int _promisesComplete = 0;
    private boolean _hasRejections = false;

    // endregion


    // region constructors

    /**
     * Instantiates a new All promise.
     *
     * @param promiseList the promise list
     */
    AllPromise(Promise[] promiseList) {

        // call base constructor
        super();

        // initialize instance variables
        _values = new AtomicReferenceArray<>(promiseList.length);
        _promiseList = promiseList;
    }

    // endregion


    // region lifecycle

    /**
     * Execute promise list.
     */
    // TODO: make this thread safe
    void executePromiseList() {

        // create counter to hold current index
        int i = 0;

        // iterate over promises in list
        for (Promise<?> promise : _promiseList) {

            // break out of loop if any promises have failed
            if (_hasRejections) {
                break;
            }

            // capture current index in array
            final int index = i;

            // add always block to handle resolution or rejection of promise
            promise.always(() -> {

                // increment promises complete count
                _promisesComplete++;

                // exit if any rejections have already occurred (fail fast)
                if (_hasRejections) {
                    return;
                }

                // reject if rejected and exit loop (fail fast)
                if (promise._state == PromiseState.REJECTED) {
                    onReject(promise._rejection.share());
                }
                // add value to array if resolved
                else if (promise._state == PromiseState.RESOLVED) {

                    // set value at current index in array
                    _values.set(index, promise._result);

                    // if all promises are complete call resolve promise
                    if (_promisesComplete == _promiseList.length) {

                        // cast resulting array to TResult
                        TResult finalValues = (TResult)arrayFrom(_values);
                        onResolve(finalValues);
                    }
                }
                // if always block is called and promise is neither resolved nor rejected
                // reject with invalid state exception
                else {
                    onReject(new Rejection(new InvalidPromiseStateException(promise)));
                }
            });

            // increment index counter
            i++;
        }
    }

    // endregion


    // region private methods

    private ArrayList arrayFrom(AtomicReferenceArray atomicArray) {

        // cache atomic array length in variable
        int len = atomicArray.length();

        // create new arraylist
        ArrayList newList = new ArrayList<>(atomicArray.length());

        // iterate over atomic array and copy to arraylist
        for (int i = 0; i < len; i++) {
            newList.add(atomicArray.get(i));
        }

        // return new arraylist
        return newList;
    }

    // endregion
}
