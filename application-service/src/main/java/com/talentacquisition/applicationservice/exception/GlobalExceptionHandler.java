package com.talentacquisition.applicationservice.exception;

import com.talentacquisition.applicationservice.dto.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJobNotFound(JobNotFoundException ex, HttpServletRequest request) {
        log.warn("Job not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Job Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProfileNotFound(ProfileNotFoundException ex, HttpServletRequest request) {
        log.warn("Profile not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Profile Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApplicationNotFound(ApplicationNotFoundException ex, HttpServletRequest request) {
        log.warn("Application not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Application Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateApplication(DuplicateApplicationException ex, HttpServletRequest request) {
        log.warn("Duplicate application: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Duplicate Application", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex, HttpServletRequest request) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(Exception ex, HttpServletRequest request) {
        log.warn("Forbidden access: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidStatusException ex, HttpServletRequest request) {
        log.warn("Invalid status: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Status", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(JobUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleJobUnavailable(JobUnavailableException ex, HttpServletRequest request) {
        log.warn("Job unavailable: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Job Unavailable", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleServiceCommunication(ServiceCommunicationException ex, HttpServletRequest request) {
        log.error("Service communication error: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, HttpServletRequest request) {
        log.error("Feign client communication error: status={}, message={}", ex.status(), ex.getMessage());
        HttpStatus status = ex.status() == 404 ? HttpStatus.NOT_FOUND : HttpStatus.SERVICE_UNAVAILABLE;
        return buildResponse(status, "Service Communication Error", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error on request to {}", request.getRequestURI());
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", "Request validation failed", request.getRequestURI(), errors);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request to {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed JSON request or invalid field value", request.getRequestURI(), null);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch on request to {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid argument type: " + ex.getName(), request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception processing request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, String path, Map<String, String> validationErrors) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
        return new ResponseEntity<>(response, status);
    }
}

