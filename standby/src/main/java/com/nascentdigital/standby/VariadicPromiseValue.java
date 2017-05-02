package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-01-09.
 */

public class VariadicPromiseValue<T, U, V> {

    private T first;
    private U second;
    private V third;
    private int length;

    //region static methods
//
//    static <T, U, V> VariadicPromiseValue createPromiseValue(T value1, U value2, V value3) {
//
//        // check which arguments are null
//        if (value1 != null && value2 != null && value3 != null) {
//            return new VariadicPromiseValue<>(value1, value2, value3, 3);
//        }
//        else if (value1 != null && value2 != null) {
//            return new VariadicPromiseValue<>(value1, value2, null, 2);
//        }
//        else {
//            return null;
//        }
//    }

    //endregion

    //region Constructors

    VariadicPromiseValue(int length) {

        this.length = length;
    }

//    VariadicPromiseValue(T value1, U value2, V value3, int length) {
//
//        this.first = value1;
//        this.second = value2;
//        this.third = value3;
//        this.length = length;
//    }

    //endregion

    //region Getters

    // TODO: these need to be made thread safe
    public T getFirst() {
        return first;
    }

    void setFirst(T first) {
        this.first = first;
    }

    public U getSecond() {
        return second;
    }

    void setSecond(U second) {
        this.second = second;
    }

    public V getThird() {
        return third;
    }

    void setThird(V third) {
        this.third = third;
    }

    public int getLength() {
        return length;
    }

    //endregion
}
