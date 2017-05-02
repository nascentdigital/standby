package com.nascentdigital.standby;

/**
 * Created by tomwark on 2016-12-15.
 */

class ErrorContext {
    public boolean consumed = false;
    public Exception error;

    public ErrorContext(Exception error, boolean consumed) {
        this.error = error;
        this.consumed = consumed;
    }

    void updateWithContext(ErrorContext context) {
        this.error = context.error;
        this.consumed = context.consumed;
    }
}
