package com.nascentdigital.standby_legacy;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Created by tomwark on 2016-12-12.
 */
public class PromiseTest {

    @Test
    public void newPromise_ShouldRejectIfErrorThrownInBody() {

        List mockList = mock(List.class);

        new Promise<>(deferral -> {

            String someString = null;
            someString.charAt(4);

            deferral.resolve(someString);
        })
        .then(value -> {

            mockList.add("This should not happen");
            return null;
        })
        .error(error -> {

            mockList.add("This should happen");
        });

        verify(mockList, timeout(100).times(0)).add("This should not happen");
        verify(mockList, timeout(100).times(1)).add("This should happen");
    }

    @Test
    public void then_ShouldBubblePromiseInvocationExceptionForUncaughtException() {

        List mockList = mock(List.class);

        String testValue = String.valueOf("Test");
        new Promise(d1 -> {
            createAsyncPromise(testValue)
                .then(result -> {
                    return new Promise<String>(d2 -> {
                        String someString = null;
                        someString.charAt(4);
                        d2.resolve(someString);
                    })
                    .then(value -> {

                        mockList.add("This should not happen");
                        return value;
                    });
            })
            .error(e -> {

                assertNotNull(e);
                assertTrue(e instanceof PromiseInvocationException);
                mockList.add("This should happen");
            });
        });

        verify(mockList, timeout(3000).times(1)).add("This should happen");
        verify(mockList, timeout(3000).times(0)).add("This should not happen");
    }

    @Test
    public void twoParameterWhen_ShouldResolveWithStrongTypedValues() {

        String firstVal = "First value";
        Integer secondVal = new Integer(50);
        List mockList = mock(List.class);


        Promise<VariadicPromiseValue<String, Integer, Void>> promise = Promise.when(
            createAsyncPromise(firstVal),
            createAsyncPromise(secondVal)
        );
        promise
            .then(values -> {

                assertEquals(firstVal, values.getFirst());
                assertEquals(secondVal, values.getSecond());

                assertTrue(values.getFirst() instanceof String);
                assertTrue(values.getSecond() instanceof Integer);

                mockList.add("This should happen");
                return null;
            })
            .error(error -> {
                assertNull(error);
            });

        verify(mockList, timeout(5000).times(1)).add("This should happen");
    }

    @Test
    public void all_ShouldBeAbleToAcceptArraylistOfVariableTypedPromises() {

        List mockList = mock(List.class);

        ArrayList<Promise<?>> list = new ArrayList<>();
        Promise<String> stringPromise = createAsyncPromise("My String");
        list.add(0, Promise.resolve(String.valueOf("Test")));
        list.add(1, Promise.resolve(Integer.valueOf(10)));
        list.add(2, stringPromise);

        Promise.all(list)
            .then(valueList -> {

                assertEquals(valueList.get(0), String.valueOf("Test"));
                assertEquals(valueList.get(1), Integer.valueOf(10));
                assertEquals(valueList.get(2), String.valueOf("My String"));
                mockList.add("This should happen");
                return null;
            });
        verify(mockList, timeout(3000).times(1)).add("This should happen");
    }

    @Test
    public void all_ShouldBeAbleToAcceptArrayOfVariableTypedPromises() {

        List mockList = mock(List.class);

        Promise<String> stringPromise = Promise.resolve(String.valueOf("Test"));
        Promise<String> stringPromise2 = createAsyncPromise("My String");
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
            mockList.add("This should happen");
            return null;
        });
        verify(mockList, timeout(3000).times(1)).add("This should happen");
    }

    @Test
    public void shouldBeAbleToReturnWhenInsideThenBlock() {

        List mockList = mock(List.class);

        Promise<String> promise1 = createAsyncPromise(String.valueOf("Testing"));
        Promise<Integer> promise2 = createAsyncPromise(Integer.valueOf(2));
        Promise<String> promise3 = createAsyncPromise(String.valueOf("More Testing"));

        promise1.<VariadicPromiseValue<Integer, String, String>>then(value -> {

            return Promise.when(promise2, promise3, Promise.resolve(value));
        })
        .then(values -> {

            assertEquals(values.getFirst(), Integer.valueOf(2));
            assertEquals(values.getSecond(), String.valueOf("More Testing"));
            assertEquals(values.getThird(), String.valueOf("Testing"));
            mockList.add("This should happen");

            return null;
        });
        verify(mockList, timeout(5000).times(1)).add("This should happen");
    }
    @Test
    public void all_shouldStillCallThenBlockWhenPromisesAreRecovered() {

        List mockList = mock(List.class);

        Promise p1 = Promise.reject(new Exception())
            .error((error, recovery) -> {
                recovery.recover("Test1");
            });
        Promise p2 = createAsyncPromise("Test2");
        Promise p3 = Promise.reject(new Exception())
            .then(value -> {
                mockList.add("This should not happen");
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
            mockList.add("This should be called");
            return null;
        })
        .error(error -> {
            mockList.add("This should not be called");
        });

        verify(mockList, timeout(5000).times(1)).add("This should be called");
        verify(mockList, timeout(5000).times(0)).add("This should not be called");
    }

    private <T> Promise<T> createAsyncPromise(T testValue) {

        return new Promise<>(deferral -> {

            new Thread(() -> {
                try {
                    sleep(2000);
                    deferral.resolve(testValue);
                }
                catch (final InterruptedException exception) {

                    deferral.reject(exception);
                }
            }).start();

        });
    }

    private Promise<Integer> createPromise(Integer num) {

        String someString = "HEYY";
        return createAsyncPromise(someString)
            .then(string -> {
                Integer someInt = Integer.valueOf(num);
                return someInt;
            })
            .error(error -> {
                throw error;
            });
    }
}