package com.nascentdigital.standby;

import java.util.concurrent.CountDownLatch;

/**
 * A helper class for testing {@link Promise} instances.  This class provides a promise that won't
 * execute until a trigger is set.
 */
public class TriggeredPromise<T> {

    // region instance variables

    public final CountDownLatch _trigger;
    public final Promise<T> promise;
    private Thread _thread;

    // endregion


    // region constructors

    public TriggeredPromise(Factory<T> resultFactory) {
        this(resultFactory, 1);
    }

    public TriggeredPromise(Factory<T> resultFactory, int triggerCount) {

        // create trigger
        _trigger = new CountDownLatch(1);

        // create promise
        promise = new Promise<>(deferral -> {
            _thread = new Thread(() -> {

                // block on trigger
                try {

                    // block on trigger
                    _trigger.await();

                    // resolve with result
                    T result = resultFactory.create();
                    deferral.resolve(result);
                }

                // reject if the trigger fails
                catch (InterruptedException e) {
                    deferral.reject(e);
                }
            });
            _thread.start();
        });
    }

    // endregion


    // region public methods

    public void trigger() {
        _trigger.countDown();
    }

    public void join() throws InterruptedException {
        _thread.join();
    }

    // endregion


    // region Factory interface

    public interface Factory<T> {

        T create();
    }

    // endregion
}
