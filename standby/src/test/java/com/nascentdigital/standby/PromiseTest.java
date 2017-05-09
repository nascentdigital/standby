package com.nascentdigital.standby;


import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by tomwark on 2016-12-12.
 */
public class PromiseTest {

    @Test
    public void reject_ReturnsRejectedPromise() {

        List mockList = mock(List.class);

        Promise.<String>reject(new Exception("This should be a rejected promise"))
            .then(value -> {

                mockList.add("This should not happen");
                verify(mockList, never()).add("This should not happen");
                return value;
        })
        .error(error -> {

            mockList.add("This should happen");
            verify(mockList, times(1)).add("This should happen");
            assertNotNull(error);
        });
    }

    @Test
    public void resolve_ReturnsResolvedPromise() {

        List mockList = mock(List.class);
        String testValue = "test value";

        Promise.resolve(testValue)
            .then(value -> {

                assertTrue(value == testValue);
                return value;
            })
            .error(error -> {

                mockList.add("This should not happen");
                verify(mockList, never()).add("This should not happen");
            });
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
    public void always_ShouldRunIfErrorOccurs() {

        List mockList = mock(List.class);

        Promise.reject(new RuntimeException("This should fail"))
            .then(value -> {

                mockList.add("This should not happen");
                return null;
            })
            .error(error -> {

                assertNotNull(error);
                mockList.add("This should happen twice");
            })
            .always(() -> {

                mockList.add("This should happen twice");
            });

        verify(mockList, timeout(100).times(0)).add("This should not happen");
        verify(mockList, timeout(100).times(2)).add("This should happen twice");
    }

    @Test
    public void always_ShouldRunIfSuccessOccurs() {

        List mockList = mock(List.class);

        Promise.resolve("Test value")
            .then(value -> {

                mockList.add("This should happen twice");
                return null;
            })
            .error(error -> {

                assertNotNull(error);
                mockList.add("This should not happen");
            })
            .always(() -> {

                mockList.add("This should happen twice");
            });

        verify(mockList, timeout(100).times(0)).add("This should not happen");
        verify(mockList, timeout(100).times(2)).add("This should happen twice");
    }

    @Test
    public void error_InnerErrorShouldNotBubbleOut() {

        List mockList = mock(List.class);

        Promise.resolve("Test value")
            .then(value -> {

                mockList.add("Then should be executed once");
                return Promise.reject(new RuntimeException())
                    .error(error -> {

                        assertNotNull(error);
                        mockList.add("This should happen once");
                    });
            })
            // this outer error handler should not be called
            .error(error -> {

                mockList.add("This should happen once");
            });

        verify(mockList, timeout(100).times(1)).add("Then should be executed once");
        verify(mockList, timeout(100).times(1)).add("This should happen once");
    }

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
    public void always_ShouldBeCalledIfInnerPromiseFails() {

        List mockList = mock(List.class);
        List alwaysMockList = mock(List.class);

        Promise.resolve("Test value")
            .then(value -> {

                return Promise.reject(new RuntimeException())
                    .error(error -> {

                        assertNotNull(error);
                        mockList.add("This should happen once");
                    });
            })
            // this outer error handler should not be called
            .error(error -> {

                mockList.add("This should happen once");
            })
            .always(() -> {

                alwaysMockList.add("Always should execute");
            });

        verify(mockList, timeout(100).times(1)).add("This should happen once");
        verify(alwaysMockList, timeout(100).times(1)).add("Always should execute");
    }

    @Test
    public void then_ShouldNotBeCalledOnFailedPromise() {

        List mockList = mock(List.class);

        Promise.resolve("Test value")
            .then(value -> {

                return Promise.reject(new RuntimeException());
            })
            .then(value -> {

                mockList.add("This should not happen");
                return null;
            });

        verify(mockList, timeout(100).times(0)).add("This should not happen");
    }

    @Test
    public void when_ShouldExecuteMultiplePromises() {

        List mockList = mock(List.class);

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
                mockList.add("This should happen once");
                return null;
            });

        verify(mockList, timeout(100).times(1)).add("This should happen once");
    }

    @Test
    public void when_ShouldExecuteMultiplePromisesAsync() {

        List mockList = mock(List.class);

        String firstVal = "First value";
        String secondVal = "Second value";
        Promise<String>[] promises = new Promise[]{
            createAsyncPromise(firstVal),
            createAsyncPromise(secondVal)
        };
        Promise.when(promises)
            .then(values -> {

                assertEquals(firstVal, values.get(0));
                assertEquals(secondVal, values.get(1));
                mockList.add("This should happen once");
                return null;
            });

        verify(mockList, timeout(8000).times(1)).add("This should happen once");
    }

    @Test
    public void when_ShouldFailWhenExceptionIsThrown() {

        List mockList = mock(List.class);
        List exceptionMockList = mock(List.class);

        String firstVal = "First value";
        Exception exception = new RuntimeException();
        Promise<String>[] promises = new Promise[]{
            createAsyncPromise(firstVal),
            Promise.reject(exception)
        };
        Promise.when(promises)
            .then(values -> {

                mockList.add("This should not happen");
                return null;
            })
            .error(error -> {

                assertEquals(exception, error);
                exceptionMockList.add("This should happen");
            });

        verify(exceptionMockList, timeout(5000).times(1)).add("This should happen");
        verify(mockList, timeout(5000).times(0)).add("This should not happen");
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
    public void whenShouldBeExecutedWithArrayList() {

        Integer[] initialValues = { 0, 1, 2, 3 };
        List mockList = mock(List.class);

        ArrayList<Promise<Integer>> promises = new ArrayList<>();
        for (Integer num : initialValues) {
            promises.add(createPromise(num));
        }

        Promise.when(promises)
        .then(values -> {

            assertEquals(initialValues.length, values.size());
            for (int i=0; i<initialValues.length; i++) {
                assertEquals(initialValues[i], values.get(i));
            }

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
    public void promiseReject_shouldHaveATypeAssociated() {

        List mockList = mock(List.class);

        Promise<Integer> promise = Promise.reject(new Exception("Throwing"));
        promise.then(value -> {

            // this block won't run because the promise is rejected but compiler should infer
            // that this is an integer
            value.intValue();
            return promise;
        }).error(error -> {
            mockList.add("This should happen");
            assertNotNull(error);
        });

        verify(mockList, timeout(5000).times(1)).add("This should happen");
    }

    @Test
    public void chainedThen_shouldHaveTypeInformation() {

        List mockList = mock(List.class);

        createAsyncPromise(String.valueOf("Testing"))
            .<Integer>then(value -> {

//                return Integer.valueOf(20);
                return Promise.resolve(Integer.valueOf(20));
            })
            .then(value -> {

                value.intValue();
                assertTrue(value instanceof Integer);
                mockList.add("This should happen");
                return value;
            });
        verify(mockList, timeout(5000).times(1)).add("This should happen");
    }

    @Test
    public void error_withRecoveryShouldRecoverPromiseRejection() {

        List mockList = mock(List.class);

        String testValue = String.valueOf("Test");
        Promise.reject(new Exception("Throwing"))
            .error((error, recovery) -> {

                recovery.recover(testValue);
            })
            .then(value -> {

                mockList.add("This should happen");
                assertNotNull(value);
                assertEquals(value, testValue);
                return null;
            });
        verify(mockList, timeout(5000).times(1)).add("This should happen");
    }

    @Test
    public void errorBlock_shouldNotBeCalledIfErrorIsRecovered() {

        List mockList = mock(List.class);

        createAsyncPromise("Recovered Value")
            .then(string -> {

                return createAsyncPromise(null)
                    .then(value -> {
                        // won't get called
                        mockList.add("This should not happen");

                        return null;
                    })
                    .error((error, recovery) -> {
                        // recover here
                        mockList.add("Recovering");
                        recovery.recover(string);
                    });
            })
            .error((error, recovery) -> {
                // this second error block should not be called
                mockList.add("This should not happen");
            })
            .then(string -> {
                // this should be called with Recovered Value
                assertEquals(string, "Recovered Value");
                mockList.add("This should happen");
                return null;
            });

        verify(mockList, timeout(6000).times(1)).add("Recovering");
        verify(mockList, timeout(6000).times(1)).add("This should happen");
        verify(mockList, timeout(6000).times(0)).add("This should not happen");
    }

    @Test
    public void errorBlock_shouldNotBeCalledIfChainedOnAnErrorRecoveryBlock() {

        List mockList = mock(List.class);

        Promise.reject(new Exception())
            .error((error, recovery) -> {

                mockList.add("This should be called");
            })
            .then(value -> {
                mockList.add("This should not be called");
                return null;
            })
            .error(error -> {

                mockList.add("This should not be called");
            });

        verify(mockList, timeout(500).times(1)).add("This should be called");
        verify(mockList, timeout(500).times(0)).add("This should not be called");
    }

    @Test
    public void async_errorBlock_shouldNotBeCalledIfChainedOnAnErrorRecoveryBlock() throws Exception {

        List mockList = mock(List.class);

        Thread[] box = new Thread[1];
        new Promise<>(deferral -> {
            box[0] = new Thread(() -> {
                try {
                    sleep(2000);
                    deferral.reject(new Exception("TEST"));
                }
                catch (final InterruptedException exception) {

                    deferral.reject(exception);
                }
            });

            box[0].start();
        })
        .error((error, recovery) -> {

            mockList.add("This should be called");
        })
        .then(value -> {
            mockList.add("This should not be called");
            return null;
        })
        .error(error -> {

            mockList.add("This should not be called");
        });

        System.out.println("joining on thread");
        box[0].join();

        verify(mockList, timeout(500).times(1)).add("This should be called");
        verify(mockList, timeout(500).times(0)).add("This should not be called");
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