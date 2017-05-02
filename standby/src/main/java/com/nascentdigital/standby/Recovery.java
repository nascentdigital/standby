package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-04-11.
 */

class ValueNotFoundException extends Exception {}

public final class Recovery<T> {

    private boolean hasSetValue;
    private T _value;

    Recovery() {
        hasSetValue = false;
    }

    T getValue() throws ValueNotFoundException {
        if (hasSetValue == false) {
            throw new ValueNotFoundException();
        }
        return _value;
    }

    void setValue(T value) {
        _value = value;
        hasSetValue = true;
    }

    public void recover(T value) {
        this.setValue(value);
    }
}
