package com.nascentdigital.standby_legacy;


import org.junit.Test;

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
}