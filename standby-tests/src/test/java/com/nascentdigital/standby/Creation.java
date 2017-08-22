package com.nascentdigital.standby;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by tomwark on 2017-05-18.
 */

public class Creation {

    @Test
    public void constructor_shouldCreateNewPromise() {

        Promise promise = new Promise<String>(deferral -> {
           deferral.resolve("Test value");
        });

        assertNotNull(promise);
    }

    @Test
    public void resolve_shouldCreateNewPromise() {

        Promise promise = Promise.resolve("String");

        assertNotNull(promise);
    }

    @Test
    public void reject_shouldCreateNewPromise() {

        Promise promise = Promise.reject(new Exception("Rejected"));

        assertNotNull(promise);
    }

    @Test
    public void reject_ReturnsRejectedPromise() {

        Box<Boolean> called = new Box<>(false);

        Promise.<String>reject(new Exception("This should be a rejected promise"))
            .then(value -> {

                fail("This should not happen");
                return value;
            })
            .error(error -> {

                assertNotNull(error);
                called.value = true;
            });
        assertTrue(called.value);
    }

    @Test
    public void resolve_ReturnsResolvedPromise() {

        Box<Boolean> called = new Box<>(false);
        String testValue = "test value";

        Promise.resolve(testValue)
            .then(value -> {

                assertEquals(value, testValue);
                called.value = true;
                return value;
            })
            .error(error -> {

                fail("Should not be called");
            });

        assertTrue(called.value);
    }

    @Test
    public void promiseReject_shouldHaveATypeAssociated() {

        Promise<Integer> promise = Promise.reject(new Exception("Throwing"));
        promise.then(value -> {

            // this block won't run because the promise is rejected but compiler should infer
            // that this is an integer
            value.intValue();
            return promise;
        }).error(error -> {
            assertNotNull(error);
        });
    }
}
