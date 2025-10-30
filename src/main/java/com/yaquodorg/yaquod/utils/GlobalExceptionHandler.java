package com.yaquodorg.yaquod.utils;

import static com.yaquodorg.yaquod.response.ApiResponse.createFailureResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.MessageResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        String errorMessage = "Validation failed: " + errors.toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleConstraintViolations(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String path = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "";
            errors.put(path, cv.getMessage());
        });
        String errorMessage = "Constraint violations: " + errors.toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        String errorMessage = "Required request header '" + ex.getHeaderName() + "' is missing";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        String errorMessage = "Required request body is missing: " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String errorMessage = "Method argument type mismatch: " + ex.getName() + " should be of type "
                + Objects.requireNonNull(ex.getRequiredType()).getSimpleName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<ApiResponse<MessageResponse>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        String errorMessage = "HTTP request method '" + ex.getMethod() + "' is not supported for this endpoint";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleMultipartException(MultipartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createFailureResponse("Multipart request error: " + ex.getMessage()));
    }
}
