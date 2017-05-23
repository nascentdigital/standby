package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-23.
 */

public class AsyncPromise {

    private TriggeredPromise _triggerPromise;

    AsyncPromise(TriggeredPromise triggerPromise) {
        _triggerPromise = triggerPromise;
    }

    void executeTriggerAndJoin() throws InterruptedException {

        // trigger
        _triggerPromise.trigger();

        // wait
        _triggerPromise.join(10000);
    }
}
