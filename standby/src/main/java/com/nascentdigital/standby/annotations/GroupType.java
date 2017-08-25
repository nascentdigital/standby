package com.nascentdigital.standby.annotations;

/**
 * Created by kitty on 2017-08-23.
 */

public enum GroupType {

    Undefined("Undefined"),
    Properties("Properties"),
    Creation("Creation"),
    Chaining("Chaining"),
    Lifecycle("Lifecycle");

    public final String name;

    private GroupType(String name) {this.name = name;}
}
