package com.mpay.exceptions;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.lang.reflect.UndeclaredThrowableException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccessControlException.class)
    protected ResponseEntity<Object> handleAccessControlException(Exception ex) {
        ApiError apiError = new ApiError(UNAUTHORIZED);
        if (ex instanceof UndeclaredThrowableException) {
            UndeclaredThrowableException e = (UndeclaredThrowableException) ex;
            apiError.setMessage(e.getUndeclaredThrowable().getMessage());
        } else {
            apiError.setMessage(ex.getMessage());
        }
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getHttpStatus());
    }
}
