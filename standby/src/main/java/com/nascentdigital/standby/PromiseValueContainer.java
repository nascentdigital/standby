package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-26.
 * A class to hold up to three strongly typed values.
 *
 * @param <T1> the type parameter
 * @param <T2> the type parameter
 * @param <T3> the type parameter
 */
public final class PromiseValueContainer<T1, T2, T3> {

    // region instance variables

    private T1 _first;
    private T2 _second;
    private T3 _third;

    // endregion


    // region properties

    /**
     * Gets first.
     *
     * @return the first
     */
    public T1 getFirst() {
        return _first;
    }

    /**
     * Sets first.
     *
     * @param value the value
     */
    void setFirst(T1 value) {
        _first = value;
    }

    /**
     * Gets second.
     *
     * @return the second
     */
    public T2 getSecond() {
        return _second;
    }

    /**
     * Sets second.
     *
     * @param value the value
     */
    void setSecond(T2 value) {
        _second = value;
    }

    /**
     * Gets third.
     *
     * @return the third
     */
    public T3 getThird() {
        return _third;
    }

    /**
     * Sets third.
     *
     * @param value the value
     */
    void setThird(T3 value) {
        _third = value;
    }

    // endregion


    // region instance methods

    /**
     * Sets value for index.
     *
     * @param index the index
     * @param value the value
     */
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
