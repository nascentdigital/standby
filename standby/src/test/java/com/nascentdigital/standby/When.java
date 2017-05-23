package com.nascentdigital.standby;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by tomwark on 2017-05-23.
 */

public class When {

    @Test
    public void when_ShouldExecuteMultiplePromises() {

        Box<Boolean> called = new Box<>(false);

        String firstVal = "First value";
        String secondVal = "Second value";
        Promise<String>[] promises = new Promise[]{
            Promise.resolve(firstVal),
            Promise.resolve(secondVal)
        };
        Promise.when(promises)
            .then(values -> {

                assertEquals(firstVal, values.get(0));
                assertEquals(secondVal, values.get(1));
                called.value = true;
                return null;
            });

        assertTrue(called.value);
    }

    @Test
    public void when_ShouldFailWhenExceptionIsThrown() {

        String firstVal = "First value";
        Exception exception = new RuntimeException();
        Box<Boolean> called = new Box<>(false);

        Promise<String>[] promises = new Promise[]{
            Promise.resolve(firstVal),
            Promise.reject(exception)
        };
        Promise.when(promises)
            .then(values -> {

                fail("Then block should not be called on failed promise");
                return null;
            })
            .error(error -> {

                assertEquals(exception, error);
                called.value = true;
            });

        assertTrue(called.value);
    }

    @Test
    public void when_ShouldBeExecutedWithArrayList() {

        Integer[] initialValues = { 0, 1, 2, 3 };
        Box<Boolean> called = new Box<>(false);

        ArrayList<Promise<Integer>> promises = new ArrayList<>();
        for (Integer num : initialValues) {
            promises.add(Promise.resolve(num));
        }

        Promise.when(promises)
            .then(values -> {

                assertEquals(initialValues.length, values.size());
                for (int i=0; i<initialValues.length; i++) {
                    assertEquals(initialValues[i], values.get(i));
                }

                called.value = true;
                return null;
            })
            .error(error -> {
                fail("Error block should not be called");
            });

        assertTrue(called.value);
    }

    @Test
    public void when_ShouldExecuteMultiplePromisesAsync() throws InterruptedException {

        Box<Boolean> called = new Box<>(false);
        String firstVal = "First value";
        String secondVal = "Second value";

        TriggeredPromise<String> trigger = new TriggeredPromise<>(() -> firstVal);

        Promise<String>[] promises = new Promise[]{
            trigger.promise,
            Promise.resolve(secondVal)
        };

        // FIXME: this isn't actually testing async
        AsyncPromise.executeTriggerAndJoin(trigger);

        Promise.when(promises)
            .then(values -> {

                assertEquals(firstVal, values.get(0));
                assertEquals(secondVal, values.get(1));
                called.value = true;
                return null;
            });


        assertTrue(called.value);
    }
}
