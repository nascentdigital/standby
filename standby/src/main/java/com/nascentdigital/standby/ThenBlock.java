package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <T> the type parameter
 */
public interface ThenBlock<T> {
    /**
     * Execute object.
     *
     * @param value the value
     * @return the object
     * @throws Exception the exception
     */
    Object execute(T value) throws Exception;
}
