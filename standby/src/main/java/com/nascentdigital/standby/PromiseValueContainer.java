package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-26.
 */

public final class PromiseValueContainer<T1, T2, T3> {

    // region instance variables

    private T1 _first;
    private T2 _second;
    private T3 _third;

    // endregion


    // region properties

    public T1 getFirst() {
        return _first;
    }
    void setFirst(T1 value) {
        _first = value;
    }

    public T2 getSecond() {
        return _second;
    }
    void setSecond(T2 value) {
        _second = value;
    }

    public T3 getThird() {
        return _third;
    }
    void setThird(T3 value) {
        _third = value;
    }

    // endregion


    // region instance methods

    void setValueForIndex(int index, Object value) {

        switch (index) {
            case 1:
                _first = (T1)value;
                break;
            case 2:
                _second = (T2)value;
                break;
            case 3:
                _third = (T3)value;
                break;
        }
    }

    // endregion
}
