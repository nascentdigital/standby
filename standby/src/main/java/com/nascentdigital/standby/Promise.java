package com.nascentdigital.standby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;


/**
 * Created by tomwark on 2016-12-06.
 */

/**
 * TODO: add tap
 */

public class Promise<T> {

    private enum PromiseState {
        PENDING,
        FULFILLED,
        REJECTED
    }

    private PromiseState state = PromiseState.PENDING;
    private T value;
    ErrorContext errorContext;

    private CatchHandler catchHandler;
    private RecoveryHandler recoveryHandler;

    private Done doneHandler;
    private Always alwaysHandler;

    // TODO: add some type of check that throws on multiple resolve / reject calls
    public static class Deferral {
        public <T> void resolve(T value) {}
        public void reject(Exception error) {}
    }

    //region interfaces

    @FunctionalInterface
    public interface Action {
        void execute(Deferral deferral);
    }

    @FunctionalInterface
    public interface Then<V> {
        Object execute(V value) throws Exception;
    }

    @FunctionalInterface
    public interface PromiseThen<U, V> {
        Promise<U> execute(V value) throws Exception;
    }

    @FunctionalInterface
    private interface Done<T> {
        void onComplete(T value, Exception error);
    }

    @FunctionalInterface
    public interface CatchHandler {
        void onError(Exception error) throws Exception;
    }

    @FunctionalInterface
    public interface RecoveryHandler<U> {
        void onError(Exception error, Recovery<U> recovery) throws Exception;
    }

    @FunctionalInterface
    public interface Always {
        void onComplete();
    }

    //endregion

    //region Constructors

    public Promise(Action action) {

        final Promise self = this;
        // set done handler to no-op
        doneHandler = (value, error) -> {};

        // set errorContext to new instance
        errorContext = new ErrorContext(null, false);

        try {
            action.execute(new Deferral() {
                @Override
                public <T> void resolve(T value) {
                    self.onResolved(value);
                }

                @Override
                public void reject(Exception error) {
                    self.onRejected(error);
                }
            });
        } catch (Exception e) {
            self.onRejected(new PromiseInvocationException(e));
        }
    }

    private Promise(ErrorContext errorContext, Action action) {

        this(action);
        this.errorContext = errorContext;
    }

    public static <T> Promise<T> resolve(T value) {
        return new Promise<>(deferral -> {
            deferral.resolve(value);
        });
    }

    public static <T> Promise<T> reject(Exception error) {
        return new Promise<T>(deferral -> {
            deferral.reject(error);
        });
    }

    //endregion

    public static Promise<ArrayList<?>> all(Promise<?>[] promises) {

        final int promiseCount = promises.length;
        final AtomicInteger promisesComplete = new AtomicInteger(0);
        final AtomicBoolean rejected = new AtomicBoolean(false);
        final AtomicReferenceArray values = new AtomicReferenceArray(promiseCount);

        return new Promise<>(deferral -> {

            int promiseIndex = 0;
            for (Promise promise: promises) {

                final int finalPromiseIndex = promiseIndex;
                promise.then(value -> {

                    // if one of the promises have been rejected just bail out here
                    if (rejected.get() == true) {
                        return null;
                    }

                    // otherwise add value to list of values
                    int currentPromiseCount = promisesComplete.incrementAndGet();
                    values.set(finalPromiseIndex, value);

                    // if all promises are complete then resolve wrapping promise with an array of the values
                    if (currentPromiseCount == promiseCount) {

                        @SuppressWarnings("unchecked")
                        ArrayList finalValues = new ArrayList();
                        for (int i=0; i < values.length(); i++) {
                            finalValues.add(values.get(i));
                        }
                        deferral.resolve(finalValues);
                    }
                    return null;
                })
                .error(error -> {

                    // if any promises are rejected, the wrapping promise is rejected immediately
                    deferral.reject(error);
                    rejected.set(true);
                });

                promiseIndex++;
            }
        });
    }

    public static Promise<ArrayList<?>> all(ArrayList<Promise<?>> promises) {

        Promise[] promiseArray = new Promise[promises.size()];
        for (int i=0; i<promises.size(); i++) {
            promiseArray[i] = promises.get(i);
        }
        return Promise.all(promiseArray);
    }

    public static <T> Promise<ArrayList<T>> when(ArrayList<Promise<T>> promises) {

        Promise<T>[] promiseArray = new Promise[promises.size()];
        for (int i=0; i<promises.size(); i++) {
            promiseArray[i] = promises.get(i);
        }
        return Promise.when(promiseArray);
    }

    public static <T> Promise<ArrayList<T>> when(Promise<T>[] promises) {

        final int promiseCount = promises.length;
        final AtomicInteger promisesComplete = new AtomicInteger(0);
        final AtomicBoolean rejected = new AtomicBoolean(false);
        final AtomicReferenceArray<T> values = new AtomicReferenceArray(promiseCount);

        return new Promise<>(deferral -> {

            int promiseIndex = 0;
            for (Promise<T> promise: promises) {

                final int finalPromiseIndex = promiseIndex;
                promise.then(value -> {

                    // if one of the promises have been rejected just bail out here
                    if (rejected.get() == true) {
                        return null;
                    }

                    // otherwise add value to list of values
                    int currentPromiseCount = promisesComplete.incrementAndGet();
                    values.set(finalPromiseIndex, value);

                    // if all promises are complete then resolve wrapping promise with an array of the values
                    if (currentPromiseCount == promiseCount) {

                        @SuppressWarnings("unchecked")
                        ArrayList<T> finalValues = new ArrayList();
                        for (int i=0; i < values.length(); i++) {
                            finalValues.add(values.get(i));
                        }
                        deferral.resolve(finalValues);
                    }
                    return null;
                })
                .error(error -> {

                    // if any promises are rejected, the wrapping promise is rejected immediately
                    deferral.reject(error);
                    rejected.set(true);
                });

                promiseIndex++;
            }
        });
    }

    public static <T, U> Promise<VariadicPromiseValue<T, U, Void>> when(Promise<T> promise1, Promise<U> promise2) {

        final VariadicPromiseValue<T, U, Void> values = new VariadicPromiseValue<>(2);

        ArrayList<Promise> promiseList = new ArrayList<>(2);
        promiseList.add(promise1);
        promiseList.add(promise2);

        return when(promiseList, values);
    }

    public static <T, U, V> Promise<VariadicPromiseValue<T, U, V>> when(Promise<T> promise1, Promise<U> promise2,
        Promise<V> promise3) {

        final VariadicPromiseValue<T, U, V> values = new VariadicPromiseValue<>(3);

        ArrayList<Promise> promiseList = new ArrayList<>(3);
        promiseList.add(promise1);
        promiseList.add(promise2);
        promiseList.add(promise3);

        return when(promiseList, values);
    }

    private static <T, U, V> Promise<VariadicPromiseValue<T, U, V>> when(ArrayList<Promise> promiseList,
        VariadicPromiseValue values) {

        final int promiseCount = promiseList.size();
        final AtomicInteger promisesComplete = new AtomicInteger(0);
        final AtomicBoolean rejected = new AtomicBoolean(false);

        return new Promise<>(deferral -> {

            int promiseIndex = 0;
            for (Promise promise : promiseList) {

                final int currentPromiseIndex = promiseIndex;
                promise.then(value -> {

                    // if already rejected do nothing
                    if (rejected.get() == true) {
                        return null;
                    }

                    // set value on variadicPromiseValue based on current index
                    switch (currentPromiseIndex) {
                        case 0:
                            values.setFirst(value);
                            break;
                        case 1:
                            values.setSecond(value);
                            break;
                        case 2:
                            values.setThird(value);
                    }

                    // if all promises are complete, resolve new promise
                    if (promisesComplete.incrementAndGet() == promiseCount) {
                        deferral.resolve(values);
                    }
                    return null;
                })
                .error(error -> {

                    if (rejected.get() == false) {

                        rejected.set(true);
                        deferral.reject(error);
                    }
                });

                promiseIndex++;
            }
        });
    }

    //region Instance methods

    /**
     * Method that will execute the provided handler once this promise has been resolved
     * @param thenAction A lambda function executed with one parameter representing the value that this promise
     *                is resolved with, when this promise resolves. If this handler returns a new promise,
     *                the following then in the chain will not be executed until this promise is resolved.
     * @param <U> Type of the value that the returned promise will have.
     * @return A new promise to be resolved when any promises returned within the handler are resolved
     */
    public <U> Promise<U> then(Then<T> thenAction) {

        final Promise<T> currentPromise = this;
        // then method will return a new promise which will be rejected or resolved when the current promise is complete
        final Promise<U> continuation = new Promise<>(this.errorContext, continuationDeferral -> {

            // once all thens have been executed on the current promise, try to call the onComplete on this current then
            // and reject the current promise if necessary
            // this done will not get called if a promise it is relying on has caught the exception and not bubbled it up
            currentPromise.done((value, error) -> {

                // if there was an error we want to reject the new promise
                if (error != null) {
                    continuationDeferral.reject(error);
                }
                // otherwise continue
                else if (value != null) {
                    try {
                        // check if current then is returning a promise using reflection
                        Object thenResult = thenAction.execute(value);

                        // if it is returning a promise we need to add a then to it
                        if (thenResult instanceof Promise) {

                            Promise<U> innerThenPromise = (Promise<U>)thenResult;
                            innerThenPromise.then(innerValue -> {

                                // resolve newly created "thenPromise" after inner promise has resolved
                                continuationDeferral.resolve(innerValue);
                                return null;
                            })
                            .always(() -> {
                                if (innerThenPromise.errorContext.error != null) {

                                    this.errorContext.updateWithContext(innerThenPromise.errorContext);
                                    continuationDeferral.reject(this.errorContext.error);
                                }
                            });
                        }
                        // if current thenAction is returning an object we can resolved right away
                        else if (thenResult != null) {
                            continuationDeferral.resolve(thenResult);
                        }
                        else {
                            continuationDeferral.resolve(value);
                        }
                    } catch (Exception e) {

                        continuationDeferral.reject(e);
                    }
                }
                else {
                    String message = "A null value and null error were provided to this promise";
                    String stack = Arrays.toString(Thread.currentThread().getStackTrace());
                    continuationDeferral.reject(new NullErrorAndValueException(message, stack));
                }
            });
        });

        return continuation;
    }

    /**
     * Method that will execute the provided handler when this promise is rejected.
     * @param catchHandler A lambda function executed with the exception that the promise was rejected with.
     * @return The current promise.
     */
    public <U> Promise<U> error(CatchHandler catchHandler) {

        // throw exception if no always handler is provided
        if (catchHandler == null) {
            throw new IllegalArgumentException("Handler lambda must be provided to error method.");
        }

        this.catchHandler = catchHandler;

        // if promise has already been rejected and error has not already been consumed, just invoke handler
        if (state == PromiseState.REJECTED) {

            if (errorContext.consumed == false) {
                try {
                    invokeErrorHandler(errorContext.error);
                    errorContext.consumed = true;
                } catch (Exception error) {
                    errorContext.consumed = false;
                    errorContext.error = error;
                }
            }
        }
        return (Promise<U>)this;
    }

    public <U> Promise<U> error(RecoveryHandler recoveryHandler) {

        // throw exception if no always handler is provided
        if (recoveryHandler == null) {
            throw new IllegalArgumentException("Handler lambda must be provided to error method.");
        }

        this.recoveryHandler = recoveryHandler;

        // if promise has already been rejected and error has not already been consumed, just invoke handler
        if (state == PromiseState.REJECTED) {

            if (errorContext.consumed == false) {
                try {
                    // invoke recoveryHandler
                    U value = this.invokeRecoveryHandler(errorContext.error);
                    return Promise.resolve(value);

                } catch (Exception error) {
                    errorContext.consumed = false;
                    errorContext.error = error;
                    return Promise.reject(error);
                }
            }
        }
        return (Promise<U>)this;
    }

    public void always(Always handler) {

        // throw exception if no always handler is provided
        if (handler == null) {
            throw new IllegalArgumentException("Handler lambda must be provided to always method.");
        }

        if (state != PromiseState.PENDING) {
            handler.onComplete();
        }
        else {
            alwaysHandler = handler;
        }
    }

    private void done(Done<T> handler) {

        // throw exception if no done handler is provided
        if (handler == null) {
            throw new IllegalArgumentException("Handler lambda must be provided to done method.");
        }

        // invoke or save doneHandler to be invoked later
        if (state == PromiseState.FULFILLED) {
            handler.onComplete(value, null);
        }
        else if (state == PromiseState.REJECTED) {
            handler.onComplete(null, errorContext.error);
        }
        else {
            doneHandler = handler;
        }
    }

    private void onResolved(T value) {

        state = PromiseState.FULFILLED;
        this.value = value;
        this.errorContext.error = null;

        invokeCompleteMethods(value, null);
    }

    private void onRejected(Exception error) {

        state = PromiseState.REJECTED;
        if (errorContext != null) {
            errorContext.error = error;
        }
        else {
            errorContext = new ErrorContext(error, false);
        }

        // check if there is a recoveryHandler and if error has been recovered
        T newVal = null;
        try {
            newVal = invokeRecoveryHandler(errorContext.error);
        } catch (Exception e) {
            errorContext.error = e;
        }

        // if error was recovered call onResolved
        if (newVal != null) {
            onResolved(newVal);
        }
        // if error was not recovered, invoke error handler and complete methods
        else {
            invokeErrorHandler(error);
            invokeCompleteMethods(null, error);
        }
    }

    private <U> U invokeRecoveryHandler(Exception error) throws Exception {

        if (recoveryHandler != null) {

            try {
                // invoke recoveryHandler
                Recovery<U> recovery = new Recovery<>();
                recoveryHandler.onError(errorContext.error, recovery);

                // check if value was set on recovery
                try {
                    U newValue = recovery.getValue();
                    return newValue;
                }
                // if no value was set, we mark the error as consumed because this error block has already handled it
                catch (ValueNotFoundException e) {
                    errorContext.consumed = true;
                    return null;
                }

            }
            // if recoveryHandler threw uncaught exception just re-throw it
            catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    private void invokeErrorHandler(Exception error) {

        if (catchHandler != null) {

            try {

                // return the result of the catchHandler
                catchHandler.onError(error);
                errorContext.consumed = true;

            } catch (Exception e) {

                errorContext.consumed = false;
                errorContext.error = e;
            }
        }
    }

    private void invokeCompleteMethods(T value, Exception error) {

        // Always handler needs to be executed before the done handler
        // invoke always handler if provided
        if (alwaysHandler != null) {
            alwaysHandler.onComplete();
        }

        // invoke final handler
        if (doneHandler != null) {
            doneHandler.onComplete(value, error);
        }
    }

    //endregion
}
