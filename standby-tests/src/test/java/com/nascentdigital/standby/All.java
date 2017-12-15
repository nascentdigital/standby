package com.nascentdigital.standby;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by tomwark on 2017-05-24.
 */

public class All {

    @Test
    public void all_ShouldBeAbleToAcceptArraylistOfVariableTypedPromises() throws InterruptedException {

        Box<Boolean> called = new Box<>(false);
        TriggeredPromise<String> trigger = new TriggeredPromise<>(() -> "My String");

        ArrayList<Promise<?>> list = new ArrayList<>();
        list.add(0, Promise.resolve(String.valueOf("Test")));
        list.add(1, Promise.resolve(Integer.valueOf(10)));
        list.add(2, trigger.promise);

        Promise.all(list)
            .then(valueList -> {

                assertEquals(valueList.get(0), String.valueOf("Test"));
                assertEquals(valueList.get(1), Integer.valueOf(10));
                assertEquals(valueList.get(2), String.valueOf("My String"));
                called.value = true;
                return null;
            });

        AsyncPromise.executeTriggerAndJoin(trigger);

        assertTrue(called.value);
    }

    @Test
    public void all_ShouldBeAbleToAcceptArrayOfVariableTypedPromises() throws InterruptedException {

        Box<Boolean> called = new Box<>(false);
        TriggeredPromise<String> trigger = new TriggeredPromise<>(() -> "My String");

        Promise<String> stringPromise = Promise.resolve(String.valueOf("Test"));
        Promise<String> stringPromise2 = trigger.promise;
        Promise<Integer> integerPromise = Promise.resolve(Integer.valueOf(10));

        Promise[] list = {
            stringPromise,
            stringPromise2,
            integerPromise
        };

        Promise.all(list)
        .then(valueList -> {

            assertEquals(valueList.get(0), String.valueOf("Test"));
            assertEquals(valueList.get(1), String.valueOf("My String"));
            assertEquals(valueList.get(2), Integer.valueOf(10));
            called.value = true;
            return null;
        });

        AsyncPromise.executeTriggerAndJoin(trigger);

        assertTrue(called.value);
    }

    @Test
    public void all_shouldStillCallThenBlockWhenPromisesAreRecovered() throws InterruptedException {

        Box<Boolean> called = new Box<>(false);
        TriggeredPromise<String> trigger = new TriggeredPromise<>(() -> "My String");

        Promise p1 = Promise.reject(new Exception())
            .error((error, recovery) -> {
                recovery.recover("Test1");
            });
        Promise p2 = trigger.promise;
        Promise p3 = Promise.reject(new Exception())
            .then(value -> {
                fail("This promise is rejected");
                return null;
            })
            .error((error, recovery) -> {
                recovery.recover("Test3");
            });
        Promise p4 = Promise.resolve("Test4");
        Promise p5 = Promise.resolve("Test5");

        Promise.all(new Promise[]{
            p1,
            p2,
            p3,
            p4,
            p5
        })
        .then(values -> {
            called.value = true;
            return null;
        })
        .error(error -> {
            fail("Promise rejection should be recovered");
        });

        AsyncPromise.executeTriggerAndJoin(trigger);

        assertTrue(called.value);
    }

    @Test
    public void all_shouldResolveIfCalledWithEmptyArray() throws InterruptedException {
        Box<Boolean> called = new Box<>(false);

        Promise.all(new Promise[]{})
            .then(value -> {

                assertNotNull(value);
                called.value = true;
                return null;
            })
            .error(error -> {
                fail();
            });

        assertTrue(called.value);
    }

    @Test
    public void all_shouldResolveIfCalledWithEmptyArrayList() throws InterruptedException {
        Box<Boolean> called = new Box<>(false);

        Promise.all(new ArrayList<Promise<?>>())
                .then(value -> {

                    assertNotNull(value);
                    called.value = true;
                    return null;
                })
                .error(error -> {
                    fail();
                });

        assertTrue(called.value);
    }

    @Test
    public void all_shouldAcceptAnyListSubclass() throws  InterruptedException {

        Box<Boolean> called = new Box<>(false);
        List<Promise<?>> list = new ArrayList<>();
        list.add(Promise.resolve("foo"));
        Promise.all(list)
                .then(value -> {

                    assertNotNull(value);
                    called.value = true;
                    return null;
                })
                .error(error -> {
                    fail();
                });
        assertTrue(called.value);
    }
}
