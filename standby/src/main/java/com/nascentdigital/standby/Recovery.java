package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 *
 * @param <TResult> the type parameter
 */
public class Recovery<TResult> {

    // region instance variable

    private TResult _value;
    private boolean _hasSetValue;

    // endregion


    // region public methods

    /**
     * Recover.
     *
     * @param result A value to recover the returned promise with. The type of the value must match that of
     *               the original promise
     */
    public void recover(TResult result) {
        _value = result;
        _hasSetValue = true;
    }

    // endregion


    // region package private methods

    /**
     * Has set value boolean.
     *
     * @return the boolean
     */
    boolean hasSetValue() {
        return _hasSetValue;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    TResult getValue() {
        return _value;
    }

    // endregion
}
