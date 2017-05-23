package com.nascentdigital.standby;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by tomwark on 2017-05-19.
 */

public class Always {

    @Test
    public void always_ShouldRunIfErrorOccurs() {

        Box<Boolean> called = new Box<>(false);

        Promise.reject(new RuntimeException("This should fail"))
            .then(value -> {

                fail("This shouldn't be called");
                return null;
            })
            .error(error -> {

                assertNotNull(error);
            })
            .always(() -> {

                called.value = true;
            });

        assertTrue(called.value);
    }

    @Test
    public void always_ShouldRunIfSuccessOccurs() {

        Box<Boolean> called = new Box<>(false);

        Promise.resolve("Test value")
            .then(value -> {

                assertNotNull(value);
                return null;
            })
            .error(error -> {

                fail("This should not be called");
            })
            .always(() -> {

                called.value = true;
            });

        assertTrue(called.value);
    }

    @Test
    public void always_ShouldBeCalledIfInnerPromiseFails() {

        Box<Boolean> errorCalled = new Box<>(false);
        Box<Boolean> alwaysCalled = new Box<>(false);

        Promise.resolve("Test value")
            .then(value -> {

                return Promise.reject(new RuntimeException())
                    .error(error -> {

                        assertNotNull(error);
                        errorCalled.value = true;
                    });
            })
            // this outer error handler should not be called
            .error(error -> {

                fail("Error should not bubble out");
            })
            .always(() -> {

                alwaysCalled.value = true;
            });

        assertTrue(errorCalled.value);
        assertTrue(alwaysCalled.value);
    }
}
