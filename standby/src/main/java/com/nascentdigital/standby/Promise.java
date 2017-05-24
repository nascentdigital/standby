package com.nascentdigital.standby;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomwark on 2017-05-18.
 */

public class Promise<TResult> {

    // region instance variables

    protected final List<ThenPromise<TResult, ?>> _thenPromises;
    protected final List<ErrorPromise<TResult>> _errorPromises;
    protected final List<AlwaysPromise<TResult>> _alwaysPromises;

    protected PromiseState _state;
    protected TResult _result;
    protected Rejection _rejection;

    // endregion


    // region constructors

    protected Promise() {

        // initialize instance variables
        _thenPromises = new ArrayList<>();
        _errorPromises = new ArrayList<>();
        _alwaysPromises = new ArrayList<>();
        _state = PromiseState.PENDING;
    }

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

    public PromiseState getState() {
        return _state;
    }

    // endregion


    // region creation

    public static <U> Promise<U> resolve(U value) {

       // create new resolved promise
        Promise<U> promise = new Promise<>();
        promise.onResolve(value);

        // return promise
        return promise;
    }

    public static <U> Promise<U> reject(Exception error) {

        // create and return new Promise that is rejected immediately
        Promise<U> promise = new Promise<>();
        promise.onReject(new Rejection(error));

        // return promise
        return promise;
    }

    public static <U> Promise<ArrayList<U>> when(Promise<U>[] promises) {

        // create new WhenPromise with list of promises
        WhenPromise<ArrayList<U>, U> whenPromise = new WhenPromise<>(promises);

        // execute promise list
        whenPromise.executePromiseList();

        // return new when promise
        return whenPromise;
    }

    public static <U> Promise<ArrayList<U>> when(ArrayList<Promise<U>> promises) {

        // create empty array
        Promise<U>[] promiseArray = new Promise[promises.size()];

        // called base method with new array from arraylist
        return Promise.when(promises.toArray(promiseArray));
    }

    public static Promise<ArrayList<?>> all(ArrayList<Promise<?>> promises) {

        // create empty array
        Promise[] promiseArray = new Promise[promises.size()];

        // called base method with new array from arraylist
        return Promise.all(promises.toArray(promiseArray));
    }

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
