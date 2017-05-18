package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

public class Recovery<TResult> {

    // region instance variable

    private TResult _value;
    private boolean _hasSetValue;

    // endregion


    // region public methods

    public void recover(TResult result) {
        _value = result;
        _hasSetValue = true;
    }

    // endregion


    // region package private methods

    boolean hasSetValue() {
        return _hasSetValue;
    }

    TResult getValue() {
        return _value;
    }

    // endregion
}
