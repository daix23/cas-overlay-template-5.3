package com.zjasm.exception;

import javax.security.auth.login.LoginException;

public class InvalidCaptchaException extends LoginException {

    private static final long serialVersionUID = 802556922354616285L;

    /**
     * Constructs a FailedLoginException with no detail message. A detail
     * message is a String that describes this particular exception.
     */
    public InvalidCaptchaException() {
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
    public InvalidCaptchaException(String msg) {
        super(msg);
    }
}
