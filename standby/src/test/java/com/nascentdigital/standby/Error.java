package com.nascentdigital.standby;

import com.nascentdigital.standby_legacy.Box;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}
