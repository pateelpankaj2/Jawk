package com.mpay.exceptions;

public class AccessControlException extends Exception {

	private static final long serialVersionUID = 1L;

	public AccessControlException() {
        this("Access Denied");
    }

    public AccessControlException(String message) {
        super(message);
    }

    public AccessControlException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
