package com.nascentdigital.standby;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

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
}
