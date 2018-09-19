package com.zjasm.exception;

import javax.security.auth.login.LoginException;

public class NoAuthException  extends LoginException {

    private static final long serialVersionUID = 802556922354616287L;

    /**
     * Constructs a FailedLoginException with no detail message. A detail
     * message is a String that describes this particular exception.
     */
    public NoAuthException() {
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
    public NoAuthException(String msg) {
        super(msg);
    }
}
