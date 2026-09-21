package com.talentacquisition.applicationservice.exception;

import com.talentacquisition.applicationservice.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/applications/test");
    }

    @Test
    void testHandleJobNotFound() {
        JobNotFoundException ex = new JobNotFoundException("Job 404 not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleJobNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Job 404 not found");
    }

    @Test
    void testHandleProfileNotFound() {
        ProfileNotFoundException ex = new ProfileNotFoundException("Profile 404 not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleProfileNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void testHandleApplicationNotFound() {
        ApplicationNotFoundException ex = new ApplicationNotFoundException("Application 404 not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleApplicationNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void testHandleDuplicateApplication() {
        DuplicateApplicationException ex = new DuplicateApplicationException("Duplicate application");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateApplication(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    void testHandleUnauthorized() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorized(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    void testHandleForbidden() {
        ForbiddenException ex = new ForbiddenException("Forbidden");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleForbidden(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    void testHandleInvalidStatus() {
        InvalidStatusException ex = new InvalidStatusException("Invalid status");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidStatus(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void testHandleJobUnavailable() {
        JobUnavailableException ex = new JobUnavailableException("Job is closed");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleJobUnavailable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void testHandleServiceCommunication() {
        ServiceCommunicationException ex = new ServiceCommunicationException("Connection failed");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleServiceCommunication(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
    }
}

