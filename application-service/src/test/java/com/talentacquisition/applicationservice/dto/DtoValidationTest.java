package com.talentacquisition.applicationservice.dto;

import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testApplyJobRequest_Valid() {
        ApplyJobRequest request = ApplyJobRequest.builder()
                .jobId(1001L)
                .coverLetter("Excited to apply!")
                .build();

        Set<ConstraintViolation<ApplyJobRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testApplyJobRequest_NullJobId() {
        ApplyJobRequest request = ApplyJobRequest.builder()
                .jobId(null)
                .build();

        Set<ConstraintViolation<ApplyJobRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Job ID is required");
    }

    @Test
    void testApplicationStatusUpdateRequest_Valid() {
        ApplicationStatusUpdateRequest request = ApplicationStatusUpdateRequest.builder()
                .status(ApplicationStatus.SHORTLISTED)
                .remarks("Strong fit")
                .build();

        Set<ConstraintViolation<ApplicationStatusUpdateRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testApplicationStatusUpdateRequest_NullStatus() {
        ApplicationStatusUpdateRequest request = ApplicationStatusUpdateRequest.builder()
                .status(null)
                .build();

        Set<ConstraintViolation<ApplicationStatusUpdateRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Application status is required");
    }
}

