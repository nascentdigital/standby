package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-23.
 */

public class AsyncPromise {

    static <T> void executeTriggerAndJoin(TriggeredPromise<T> triggerPromise) throws InterruptedException {

        // trigger
        triggerPromise.trigger();

        // wait
        triggerPromise.join(10000);
    }
}
