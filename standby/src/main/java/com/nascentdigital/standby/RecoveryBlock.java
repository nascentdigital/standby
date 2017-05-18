package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

public interface RecoveryBlock {
    void execute(Exception error, Recovery recovery);
}
