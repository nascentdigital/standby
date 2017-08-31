package com.nascentdigital.standby;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by tomwark on 2017-05-19.
 */

public class Recover {

    @Test
    public void error_withRecoveryShouldRecoverPromiseRejection() {

        Box<Boolean> called = new Box<>(false);

        String testValue = String.valueOf("Test");
        Promise.reject(new Exception("Throwing"))
            .error((error, recovery) -> {

                recovery.recover(testValue);
            })
            .then(value -> {

                called.value = true;
                assertEquals(value, testValue);
                return null;
            });

        assertTrue(called.value);
    }

    @Test
    public void errorBlock_shouldNotBeCalledIfErrorIsRecovered() {

        Box<Boolean> called = new Box<>(false);
        String recoveredValue = "Recovered value";

        Promise.resolve(recoveredValue)
            .then(string -> {

                return Promise.reject(new Exception("Fail"))
                    .then(value -> {

                        // won't get called
                        fail("Then block should no be called");
                        return null;
                    })
                    .error((error, recovery) -> {
                        // recover here
                        recovery.recover(string);
                    });
            })
            .error((error, recovery) -> {
                // this second error block should not be called
                fail("Then block should no be called");
            })
            .then(string -> {
                // this should be called with Recovered Value
                assertEquals(string, recoveredValue);
                called.value = true;
                return null;
            });

        assertTrue(called.value);
    }

    @Test
    public void errorBlock_shouldNotBeCalledIfChainedOnAnErrorRecoveryBlock() {

        Box<Boolean> called = new Box<>(false);

        Promise.reject(new Exception())
            .error((error, recovery) -> {

                called.value = true;
            })
            .then(value -> {
                fail("Then should not be called on rejeced promise");
                return null;
            })
            .error(error -> {

                fail("Chained error block should not be called if error is consumed");
            });

        assertTrue(called.value);
    }

    @Test
    public void async_errorBlock_shouldNotBeCalledIfChainedOnAnErrorRecoveryBlock() throws Exception {

        Box<Boolean> called = new Box<>(false);
        TriggeredPromise<Boolean> triggerPromise = new TriggeredPromise(() -> true);

        triggerPromise.promise
            .then(result -> {
                throw new Exception("Fail");
            })
            .error((error, recovery) -> {

                called.value = true;
            })
            .then(value -> {
                fail("Then should not be called on rejected promise");
                return null;
            })
            .error(error -> {

                fail("Second error block should not be called if error is consumed");
            });

        AsyncPromise.executeTriggerAndJoin(triggerPromise);

        assertTrue(called.value);
    }

    @Test
    public void recover_shouldBeAbleToReThrowException() throws Exception {

        Box<Boolean> called = new Box<>(false);

        Promise.reject(new Exception("fail"))
            .error((error, recovery) -> {
                throw error;
            })
            .then(value -> {
                fail("Promise should be rejected");
                return null;
            })
            .error(error -> {

                called.value = true;
            });

        assertTrue(called.value);
    }
}
