package com.nascentdigital.standby;

import com.nascentdigital.standby_legacy.Box;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by tomwark on 2017-05-18.
 */

public class Then {

    @Test
    public void then_shouldBeExecutedOnResolvedPromise() {

        Box<Boolean> called = new Box<>(false);

        String testValue = "Test Value";
        Promise.resolve(testValue)
            .then(result -> {

                assertEquals(testValue, result);
                called.value = true;
                return result;
            });

        assertTrue(called.value);
    }

    @Test
    public void then_shouldNotBeExecutedOnRejectedPromise() {

        Box<Boolean> called = new Box<>(false);

        Promise.reject(new Exception())
            .then(result -> {

                called.value = true;
                return result;
            });

        assertFalse(called.value);
    }
}
