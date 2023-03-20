package com.mpay.exceptions;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiError {

    private int code;
    private HttpStatus httpStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;

    private ApiError() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(HttpStatus status) {
        this();
        this.httpStatus = status;
        this.code = status.value();
    }

    public ApiError(HttpStatus status, Throwable ex) {
        this();
        this.httpStatus = status;
        this.code = status.value();
        this.message = "Unexpected error";
        this.debugMessage = ex.getLocalizedMessage();
    }

    public ApiError(HttpStatus status, String message, Throwable ex) {
        this();
        this.httpStatus = status;
        this.code = status.value();
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }
}
