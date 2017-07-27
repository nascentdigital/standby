package com.nascentdigital.standby;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <T1> the type parameter
 * @param <T2> the type parameter
 * @param <T3> the type parameter
 */
class WhenPromise<T1, T2, T3> extends Promise<PromiseValueContainer<T1, T2, T3>> {

    // region instance variables

    private PromiseValueContainer<T1, T2, T3> _valueContainer;
    private int _promiseCount = 0;
    private int _promisesComplete = 0;
    private Promise<T1> _firstPromise;
    private Promise<T2> _secondPromise;
    private Promise<T3> _thirdPromise;
    private boolean _hasRejections = false;

    // endregion


    // region constructors

    /**
     * Instantiates a new When promise.
     *
     * @param p1 the p 1
     * @param p2 the p 2
     * @param p3 the p 3
     */
    WhenPromise(Promise<T1> p1, Promise<T2> p2, Promise<T3> p3) {

        // call base constructor
        super();

        // throw if either of the first 2 args are null
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Must provide at least two promises to constructor");
        }

        // initialize instance variables
        _firstPromise = p1;
        _secondPromise = p2;
        _thirdPromise = p3;
        _promiseCount = _thirdPromise != null ? 3 : 2;
        _valueContainer = new PromiseValueContainer<T1, T2, T3>();
    }

    /**
     * Instantiates a new When promise.
     *
     * @param p1 the p 1
     * @param p2 the p 2
     */
    WhenPromise(Promise<T1> p1, Promise<T2> p2) {

        // call base constructor
        this(p1, p2, null);
    }

    // endregion


    // region lifecycle

    /**
     * Execute promises.
     */
    void executePromises() {

        _firstPromise.always(() -> onPromiseComplete(_firstPromise, 1));
        _secondPromise.always(() -> onPromiseComplete(_secondPromise, 2));

        if (_thirdPromise != null) {
            _thirdPromise.always(() -> onPromiseComplete(_thirdPromise, 3));
        }
    }

    // endregion


    // region private methods

    // TODO: make this thread safe
    private void onPromiseComplete(Promise promise, int index) {

        // increment promises complete
        _promisesComplete++;

        // exit if a rejection has already occurred
        if (_hasRejections) {
            return;
        }

        // check state of promise
        if (promise._state == PromiseState.REJECTED) {

            // if rejected mark has rejections to true and reject
            _hasRejections = true;
            onReject(promise._rejection.share());
        }
        // add value to array if resolved
        else if (promise._state == PromiseState.RESOLVED) {

            // set value at current index in promise list
            _valueContainer.setValueForIndex(index, promise._result);

            // if all promises are complete call resolve promise
            if (_promisesComplete == _promiseCount) {

                // resolve using value container
                onResolve(_valueContainer);
            }
        }
        // if always block is called and promise is neither resolved nor rejected
        // reject with invalid state exception
        else {
            onReject(new Rejection(new InvalidPromiseStateException(promise)));
        }

    }

    // endregion
}
