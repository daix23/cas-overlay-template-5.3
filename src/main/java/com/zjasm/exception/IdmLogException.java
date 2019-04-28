package com.zjasm.exception;

import javax.security.auth.login.AccountException;

public class IdmLogException  extends AccountException {
    /**
     * Constructs a FailedLoginException with no detail message. A detail
     * message is a String that describes this particular exception.
     */
    public IdmLogException() {
        super();
    }

    /**
     * Constructs a FailedLoginException with the specified detail
     * message.  A detail message is a String that describes this particular
     * exception.
     *
     * <p>
     *
     * @param msg the detail message.
     */
    public IdmLogException(String msg) {
        super(msg);
    }
}
