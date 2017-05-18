package com.nascentdigital.standby_legacy;

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
