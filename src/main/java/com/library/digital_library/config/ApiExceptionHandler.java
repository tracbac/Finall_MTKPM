package com.library.digital_library.config;

import com.library.digital_library.service.BookCoverStorageException;
import com.library.digital_library.service.EBookStorageException;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, String>> handleDataConflict() {
        return error(HttpStatus.CONFLICT, "The request conflicts with existing data");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParameter() {
        return error(HttpStatus.BAD_REQUEST, "A request parameter has an invalid value");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> handleValidation(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Request validation failed");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(BookCoverStorageException.class)
    public ResponseEntity<Map<String, String>> handleStorageFailure(BookCoverStorageException exception) {
        return error(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler(EBookStorageException.class)
    public ResponseEntity<Map<String, String>> handleEBookStorageFailure(EBookStorageException exception) {
        return error(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.name(),
                "message", message == null ? status.getReasonPhrase() : message));
    }
}
