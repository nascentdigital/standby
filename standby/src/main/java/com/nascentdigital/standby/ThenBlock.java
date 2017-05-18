package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

public interface ThenBlock<T> {
    Object execute(T value) throws Exception;
}
