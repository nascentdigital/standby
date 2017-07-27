package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */
public interface RecoveryBlock {
    /**
     * Execute.
     *
     * @param error    the error
     * @param recovery the recovery
     */
    void execute(Exception error, Recovery recovery);
}
