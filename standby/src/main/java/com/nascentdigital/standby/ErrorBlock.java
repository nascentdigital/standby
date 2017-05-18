package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

public interface ErrorBlock {
    void execute(Exception error) throws Exception;
}
