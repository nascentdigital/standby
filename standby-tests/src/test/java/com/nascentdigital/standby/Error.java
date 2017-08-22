package com.nascentdigital.standby;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by tomwark on 2017-05-18.
 */

public class Error {

    @Test
    public void error_ShouldBeExecutedOnRejectedPromise() {

        Box<Boolean> called = new Box<>(false);

        Exception testException = new Exception("Test Exception");
        Promise.reject(testException)
            .error(error -> {

                assertEquals(error, testException);
                called.value = true;
            });

        assertTrue(called.value);
    }

    @Test
    public void error_ShouldNotBeExecutedOnResolvedPromise() {

        Box<Boolean> called = new Box<>(false);

        Promise.resolve(new Exception())
            .error(error -> {
                called.value = true;
            });

        assertFalse(called.value);
    }

    @Test
    public void error_InnerErrorShouldNotBubbleOutIfConsumed() {

        Box<Boolean> thenCalled = new Box<>(false);
        Box<Boolean> errorCalled = new Box<>(false);

        Promise.resolve("Test value")
            .then(value -> {

                thenCalled.value = true;
                return Promise.reject(new RuntimeException())
                    .error(error -> {

                        assertNotNull(error);
                        errorCalled.value = true;
                    });
            })
            // this outer error handler should not be called
            .error(error -> {

                fail("Error should not bubble out if consumed");
            });

        assertTrue(thenCalled.value);
        assertTrue(errorCalled.value);
    }
}
