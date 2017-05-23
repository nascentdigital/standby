package com.nascentdigital.standby;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}
