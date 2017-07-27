package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */
public interface DeferralBlock {
    /**
     * Execute.
     *
     * @param deferral the deferral
     */
    void execute(Deferral deferral);
}
