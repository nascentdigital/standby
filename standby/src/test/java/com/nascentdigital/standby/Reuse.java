package com.nascentdigital.standby;


import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests cases where the promise is being reused.
 */
public class Reuse {


    @Test
    public void then_shouldBeCalledTwice_whenParentIsReused() throws InterruptedException {

        // create parent promise
        TriggeredPromise<Boolean> triggerPromise = new TriggeredPromise<>(() -> true);

        // add error handler
        triggerPromise.promise.error(e -> {
            fail("Unexpected error: " + e.getMessage());
        });

        // add child 1
        Box<Integer> result1 = new Box<>();
        Promise<Integer> promise1 = triggerPromise.promise
                .then(result -> {

                    // assert
                    assertEquals(result, Boolean.TRUE);

                    // capture value
                    result1.value = 1;

                    // return
                    return result1.value;
                });


        // add child 2
        Box<Integer> result2 = new Box<>();
        Promise<Integer> promise2 = triggerPromise.promise
                .then(result -> {

                    // assert
                    assertEquals(result, Boolean.TRUE);

                    // capture value
                    result2.value = 2;

                    // return
                    return result2.value;
                });

        // trigger
        triggerPromise.trigger();

        // wait
        triggerPromise.join();

        // assert
        assertEquals(result2.value, Integer.valueOf(2));
        assertEquals(result1.value, Integer.valueOf(1));
    }
}
