package com.talentacquisition.applicationservice.controller;

import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> applyForJob(
            @Valid @RequestBody ApplyJobRequest request,
            @RequestHeader(value = "X-Candidate-Id", required = false) Long candidateHeaderId,
            Authentication authentication) {

        Long candidateId = extractCandidateId(authentication, candidateHeaderId);
        log.info("Received job application from candidateId={} for jobId={}", candidateId, request.getJobId());

        ApplicationResponse response = applicationService.createApplication(candidateId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    private Long extractCandidateId(Authentication authentication, Long candidateHeaderId) {
        if (candidateHeaderId != null) {
            return candidateHeaderId;
        }
        if (authentication != null && authentication.getName() != null) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException ignored) {
                // If username is email or non-numeric
            }
        }
        return 101L; // default fallback
    }
}

