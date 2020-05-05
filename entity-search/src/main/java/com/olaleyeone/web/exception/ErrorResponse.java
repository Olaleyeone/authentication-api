package com.olaleyeone.web.exception;

import com.olaleyeone.web.ApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = false)
public class ErrorResponse extends RuntimeException {

    private final HttpStatus httpStatus;
    private ApiResponse<?> response;

    public ErrorResponse(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ErrorResponse(HttpStatus httpStatus, ApiResponse<?> response) {
        this.httpStatus = httpStatus;
        this.response = response;
    }
}
