package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */
public interface ErrorBlock {
    /**
     * Execute.
     *
     * @param error the error
     * @throws Exception the exception
     */
    void execute(Exception error) throws Exception;
}
