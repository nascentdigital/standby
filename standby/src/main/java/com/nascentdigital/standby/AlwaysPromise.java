package com.nascentdigital.standby;

/**
 * Created by tomwark on 2017-05-18.
 */

class AlwaysPromise<TResult> extends Promise<TResult> {

    // region instance variables

    private final AlwaysBlock _alwaysBlock;

    // endregion


    // region constructors

    AlwaysPromise(AlwaysBlock alwaysBlock) {

        // call base constructor
        super();

        // initialize instance variables
        _alwaysBlock = alwaysBlock;
    }

    // endregion


    // region lifecycle

    protected void execute() {

        // run always block
        _alwaysBlock.execute();
    }

    // endregion
}
