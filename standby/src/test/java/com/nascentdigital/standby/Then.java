package com.nascentdigital.standby;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    @Test
    public void then_shouldBeExecutedOnResolvedAsyncPromise() throws InterruptedException {

        Box<Boolean> called = new Box<>(false);
        String testValue = "Test Value";
        TriggeredPromise<String> triggerPromise = new TriggeredPromise<>(() -> testValue);

        triggerPromise.promise
            .then(result -> {

                assertEquals(result, testValue);
                called.value = true;
                return result;
            });

        triggerPromise.trigger();

        triggerPromise.join(10000);

        assertTrue(called.value);
    }

    @Test
    public void then_ShouldPassValueThroughToNextThen() {

        String testValue = "test value";
        Integer testInt = new Integer(50);

        Promise.resolve(testValue)
            .then(value -> {

                assertTrue(value == testValue);
                return testInt;
            })
            .then(newValue -> {

                assertTrue(newValue == testInt);
                return null;
            });
    }

    @Test
    public void then_ShouldNotBeCalledOnFailedPromise() {

        Promise.resolve("Test value")
            .then(value -> Promise.reject(new RuntimeException()))
            .then(value -> {

                fail("Then should not be called if inner promise rejects");
                return null;
            });

    }

    @Test
    public void chainedThen_shouldHaveTypeInformation() {

        Box<Boolean> called = new Box<>(false);

        Promise.resolve(String.valueOf("Testing"))
            .<Integer>then(value -> Promise.resolve(Integer.valueOf(20)))
            .then(value -> {

                value.intValue();
                assertTrue(value instanceof Integer);
                called.value = true;
                return value;
            });

        assertTrue(called.value);
    }
}
