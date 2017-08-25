package com.nascentdigital.standby;

import com.nascentdigital.standby.annotations.Group;
import com.nascentdigital.standby.annotations.GroupType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <TResult> the type parameter
 */
public class Promise<TResult> {

    // region instance variables

    /**
     * The Then promises.
     */
    protected final List<ThenPromise<TResult, ?>> _thenPromises;
    /**
     * The Error promises.
     */
    protected final List<ErrorPromise<TResult>> _errorPromises;
    /**
     * The Always promises.
     */
    protected final List<AlwaysPromise<TResult>> _alwaysPromises;

    /**
     * The State.
     */
    protected PromiseState _state;
    /**
     * The Result.
     */
    protected TResult _result;
    /**
     * The Rejection.
     */
    protected Rejection _rejection;

    // endregion


    // region constructors

    /**
     * Instantiates a new Promise.
     */
    protected Promise() {

        // initialize instance variables
        _thenPromises = new ArrayList<>();
        _errorPromises = new ArrayList<>();
        _alwaysPromises = new ArrayList<>();
        _state = PromiseState.PENDING;
    }

    /**
     * Instantiates a new Promise.
     *
     * @param deferralBlock A block that is executed immediately with a deferral object that is used to either                      resolve or reject the newly instantiated promise.
     */
    public Promise(DeferralBlock deferralBlock) {

        // call core constructor
        this();

        // execute block
        // TODO: add in logic to throw an exception if resolve or reject are called more than once
        // TODO: wrap this in a try-catch to check for thrown exceptions in execute block
        deferralBlock.execute(new Deferral<>(this));
    }

    // endregion


    // region properties

    /**
     * Gets state.
     *
     * @return The current {@link PromiseState} of the promise.
     */
    @Group (type = GroupType.Properties)
    public PromiseState getState() {
        return _state;
    }

    // endregion


    // region creation


    /**
     * Creates a new resolved {@link Promise}.
     *
     * @param <U>   The type of the value that the promise represents.
     * @param value The value to resolve with.
     * @return A resolved promise.
     */
    @Group (type = GroupType.Creation)
    public static <U> Promise<U> resolve(U value) {

       // create new resolved promise
        Promise<U> promise = new Promise<>();
        promise.onResolve(value);

        // return promise
        return promise;
    }

    /**
     * Creates a rejected {@link Promise}.
     *
     * @param <U>   The type of the value that the promise represents.
     * @param error The error that the promise is rejected with.
     * @return A rejected promise.
     */
    @Group (type = GroupType.Creation)
    public static <U> Promise<U> reject(Exception error) {

        // create and return new Promise that is rejected immediately
        Promise<U> promise = new Promise<>();
        promise.onReject(new Rejection(error));

        // return promise
        return promise;
    }

    /**
     * Creates a new {@link Promise} that is resolved when all promises in the provided array are resolved.
     * Promise will be rejected as soon as one of the promises is rejected.
     *
     * @param <U>      The type of the value that the promise represents.
     * @param promises The list of promises to resolve on.
     * @return A new promise.
     */
    @Group (type = GroupType.Creation)
    public static <U> Promise<ArrayList<U>> when(Promise<U>[] promises) {

        // create new CollectionWhenPromise with list of promises
        CollectionWhenPromise<ArrayList<U>, U> collectionWhenPromise = new CollectionWhenPromise<>(promises);

        // execute promise list
        collectionWhenPromise.executePromiseList();

        // return new when promise
        return collectionWhenPromise;
    }

    /**
     * Creates a new {@link Promise} that is resolved when all promises in the provided array are resolved.
     * Promise will be rejected as soon as one of the promises is rejected.
     *
     * @param <U>      The type of the value that the promise represents.
     * @param promises The arraylist of promises to resolve on.
     * @return A new promise.
     */
    @Group (type = GroupType.Creation)
    public static <U> Promise<ArrayList<U>> when(ArrayList<Promise<U>> promises) {

        // create empty array
        Promise<U>[] promiseArray = new Promise[promises.size()];

        // called base method with new array from arraylist
        return Promise.when(promises.toArray(promiseArray));
    }

    /**
     * Creates a new {@link Promise} that is resolved when all promises provided are resolved.
     * Promise will be rejected as soon as one of the promises is rejected.
     *
     * @param <T1>     The first type parameter.
     * @param <T2>     The second type parameter.
     * @param <T3>     The third parameter.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param promise3 The third promise.
     * @return A new promise.
     */
    @Group (type = GroupType.Creation)
    public static <T1, T2, T3> Promise<PromiseValueContainer<T1, T2, T3>> when(Promise<T1> promise1,
        Promise<T2> promise2, Promise<T3> promise3) {

        // create new when promise with 3 promises provided
        WhenPromise whenPromise = new WhenPromise(promise1, promise2, promise3);

        // execute 3 promises
        whenPromise.executePromises();

        // return new when promise
        return whenPromise;
    }

    /**
     * Creates a new {@link Promise} that is resolved when all promises provided are resolved.
     * Promise will be rejected as soon as one of the promises is rejected.
     *
     * @param <T1>     The first type parameter.
     * @param <T2>     The second type parameter.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @return A new promise.
     */
    @Group (type = GroupType.Creation)
    public static <T1, T2> Promise<PromiseValueContainer<T1, T2, Void>> when(Promise<T1> promise1,
        Promise<T2> promise2) {

        // call base implementation
        return Promise.when(promise1, promise2, null);
    }

    /**
     * Creates a new {@link Promise} that is resolved when all promises in the provided arraylist are resolved.
     * This method is meant to be used when the types of the promises are varied.
     * Promise will be rejected as soon as one of the promises is rejected.
     *
     * @param promises An arraylist of promises to be executed.
     * @return A new promise.
     */
    @Group (type = GroupType.Creation)
    public static Promise<ArrayList<?>> all(ArrayList<Promise<?>> promises) {

        // create empty array
        Promise[] promiseArray = new Promise[promises.size()];

        // called base method with new array from arraylist
        return Promise.all(promises.toArray(promiseArray));
    }

    /**
     * Creates a new {@link Promise} that is resolved when all promises in the provided array are resolved.
     * This method is meant to be used when the types of the promises are varied.
     * Promise will be rejected as soon as one of the promises is rejected.
     *
     * @param promises An array of promises to be executed.
     * @return A new promise.
     */
    @Group (type = GroupType.Creation)
    public static Promise<ArrayList<?>> all(Promise<?>[] promises) {

        // create new AllPromise with list of promises
        AllPromise<ArrayList<?>> allPromise = new AllPromise<>(promises);

        // execute promise list
        allPromise.executePromiseList();

        // return new when promise
        return allPromise;
    }

    // endregion


    // region chaining

    /**
     * Executes provided block when promise is resolved with the value that the promise represents.
     * Block will not be executed if promise is rejected.
     * If promise is already resolved, the block will be executed immediately,
     * otherwise it will be executed asynchronously when the promise is resolved.
     *
     * @param <T>   The type of the value the inner promise holds.
     * @param block A block of code to be executed when promise is resolved.
     * @return A new promise that represents the value returned by the block. If that value is a promise, this new promise will depend on the inner promise.
     */
    @Group (type = GroupType.Chaining)
    public <T> Promise<T> then(ThenBlock<TResult> block) {

        // create new ThenPromise
        ThenPromise<TResult, T> thenPromise = new ThenPromise<>(block);

        // if state is resolved, execute thenPromise
        if (_state == PromiseState.RESOLVED) {
            thenPromise.execute(_result);
        }

        // if state is rejected, reject thenPromise
        else if (_state == PromiseState.REJECTED) {
            thenPromise.onReject(_rejection.share());
        }

        // if state is pending, add to list of thenPromises
        else {
            _thenPromises.add(thenPromise);
        }

        // return newly created thenPromise
        return thenPromise;
    }

    /**
     * Executes provided block when promise is rejected with the exception that the promise was rejected with.
     * Block will not be executed if the promise is resolved.
     * If promise is already rejected, the block will be executed immediately,
     * otherwise it will be executed asynchronously when the promise is rejected.
     *
     * @param block A block of code to be executed with the promise is rejected.              If this block throws an error the returned promise is rejected.
     * @return A new promise that represents the value and state of the current promise.
     */
    @Group (type = GroupType.Chaining)
    public Promise<TResult> error(ErrorBlock block) {

        // create new UnrecoverableErrorPromise
        UnrecoverableErrorPromise<TResult> errorPromise = new UnrecoverableErrorPromise<>(block);

        // if state is resolved, resolve errorPromise
        if (_state == PromiseState.RESOLVED) {
            errorPromise.onResolve(_result);
        }

        // if state is rejected execute errorPromise
        else if (_state == PromiseState.REJECTED) {
            errorPromise.execute(_rejection.share());
        }

        // if state is pending, add to list of errorPromises
        else {
            _errorPromises.add(errorPromise);
        }

        // return newly created errorPromise
        return errorPromise;
    }

    /**
     * Executes provided block when promise is rejected with the exception that the promise was rejected with,
     * as well as a recovery object. Calling the recover method on the recovery object will "recover" the current promise
     * and return a new promise resolved with the value passed to the recover method.
     * Block will not be executed if the promise is resolved.
     * If promise is already rejected, the block will be executed immediately,
     * otherwise it will be executed asynchronously when the promise is rejected.
     *
     * @param block A block of code to be executed with the promise is rejected.              If this block throws an error the returned promise is rejected.              If this block calls the recover method on the provided recovery object the returned promise              will be resolved with the value provided.
     * @return A new promise that is rejected or resolved based on the action taken in the recovery block.
     */
    @Group (type = GroupType.Chaining)
    public Promise<TResult> error(RecoveryBlock block) {

        // create new UnrecoverableErrorPromise
        RecoverableErrorPromise<TResult> errorPromise = new RecoverableErrorPromise<>(block);

        // if state is resolved, resolve errorPromise
        if (_state == PromiseState.RESOLVED) {
            errorPromise.onResolve(_result);
        }

        // if state is rejected execute errorPromise
        else if (_state == PromiseState.REJECTED) {
            errorPromise.execute(_rejection.share());
        }

        // if state is pending, add to list of errorPromises
        else {
            _errorPromises.add(errorPromise);
        }

        // return newly created errorPromise
        return errorPromise;
    }

    /**
     * Executes provided block when a promise is either resolved or rejected. This block will be invoked after any
     * error or then blocks chained on the promise.
     *
     * @param block A block of code to be executed with the promise is resolved or rejected.
     * @return A new promise that represents the value and state of the current promise.
     */
    @Group (type = GroupType.Chaining)
    public Promise<TResult> always(AlwaysBlock block) {

        // create new AlwaysPromise
        AlwaysPromise<TResult> alwaysPromise = new AlwaysPromise<>(block);

        // if state is not pending, execute alwaysPromise immediately
        if (_state != PromiseState.PENDING) {
            alwaysPromise.execute();
        }

        // if state is pending, add to list of alwaysPromises
        else {
            _alwaysPromises.add(alwaysPromise);
        }

        // return newly created errorPromise
        return alwaysPromise;
    }

    // endregion


    // region lifecycle

    /**
     * On resolve.
     *
     * @param result the result
     */
    @Group (type = GroupType.Lifecycle)
    protected void onResolve(TResult result) {

        // set result
        _result = result;

        // update state
        _state = PromiseState.RESOLVED;

        // propagate down the chain
        for (ThenPromise<TResult, ?> promise : _thenPromises) {
            promise.execute(result);
        }

        // resolve all error promises
        for (ErrorPromise<TResult> promise : _errorPromises) {
            promise.onResolve(result);
        }

        // execute and resolve all always promises
        for (AlwaysPromise<TResult> promise : _alwaysPromises) {
            promise.execute();
            promise.onResolve(result);
        }
    }

    /**
     * On reject.
     *
     * @param rejection the rejection
     */
    @Group (type = GroupType.Lifecycle)
    protected void onReject(Rejection rejection) {

        // set result
        _rejection = rejection;

        // update state
        _state = PromiseState.REJECTED;

        // propagate down the chain
        for (ErrorPromise<TResult> promise : _errorPromises) {
            promise.execute(rejection.share());
        }

        // reject all then promises
        for (ThenPromise<TResult, ?> promise : _thenPromises) {
            promise.onReject(rejection.share());
        }

        // execute reject all always promises
        for (AlwaysPromise<TResult> promise : _alwaysPromises) {
            promise.execute();
            promise.onReject(rejection.share());
        }
    }

    // endregion
}
