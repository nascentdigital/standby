package com.nascentdigital.standby;

/**
 * Created by sdedios on 2017-05-09.
 */

public class Box<T> {

    public T value;


    public Box() {
        value = null;
    }

    public Box(T initial) {
        value = initial;
    }
}
