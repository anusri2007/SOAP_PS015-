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

    @GetMapping("/my")
    public ResponseEntity<java.util.List<ApplicationResponse>> getMyApplications(
            @RequestHeader(value = "X-Candidate-Id", required = false) Long candidateHeaderId,
            Authentication authentication) {

        Long candidateId = extractCandidateId(authentication, candidateHeaderId);
        log.info("Fetching applications for candidateId={}", candidateId);
        java.util.List<ApplicationResponse> list = applicationService.getApplicationsByCandidateId(candidateId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userHeaderId,
            @RequestHeader(value = "X-Candidate-Id", required = false) Long candidateHeaderId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            Authentication authentication) {

        Long callerId = userHeaderId != null ? userHeaderId : extractCandidateId(authentication, candidateHeaderId);
        java.util.List<String> roles = extractRoles(authentication, rolesHeader);

        log.info("Fetching application id={} for callerId={}, roles={}", id, callerId, roles);
        ApplicationResponse response = applicationService.getApplicationById(id, callerId, roles);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<java.util.List<ApplicationResponse>> getApplicationsForJob(
            @PathVariable("jobId") Long jobId,
            @RequestHeader(value = "X-User-Id", required = false) Long userHeaderId,
            @RequestHeader(value = "X-Candidate-Id", required = false) Long candidateHeaderId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            Authentication authentication) {

        Long callerId = userHeaderId != null ? userHeaderId : extractCandidateId(authentication, candidateHeaderId);
        java.util.List<String> roles = extractRoles(authentication, rolesHeader);

        log.info("Fetching applications for jobId={} by callerId={}, roles={}", jobId, callerId, roles);
        java.util.List<ApplicationResponse> list = applicationService.getApplicationsByJobId(jobId, callerId, roles);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable("id") Long id,
            @jakarta.validation.Valid @RequestBody com.talentacquisition.applicationservice.dto.ApplicationStatusUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userHeaderId,
            @RequestHeader(value = "X-Candidate-Id", required = false) Long candidateHeaderId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            Authentication authentication) {

        Long callerId = userHeaderId != null ? userHeaderId : extractCandidateId(authentication, candidateHeaderId);
        java.util.List<String> roles = extractRoles(authentication, rolesHeader);

        log.info("Updating application id={} status to {} by callerId={}, roles={}", id, request.getStatus(), callerId, roles);
        ApplicationResponse response = applicationService.updateApplicationStatus(id, request.getStatus(), callerId, roles);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/shortlist")
    public ResponseEntity<ApplicationResponse> shortlistCandidate(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userHeaderId,
            @RequestHeader(value = "X-Candidate-Id", required = false) Long candidateHeaderId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            Authentication authentication) {

        Long callerId = userHeaderId != null ? userHeaderId : extractCandidateId(authentication, candidateHeaderId);
        java.util.List<String> roles = extractRoles(authentication, rolesHeader);

        log.info("Shortlisting application id={} by callerId={}, roles={}", id, callerId, roles);
        ApplicationResponse response = applicationService.shortlistCandidate(id, callerId, roles);
        return ResponseEntity.ok(response);
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

    private java.util.List<String> extractRoles(Authentication authentication, String rolesHeader) {
        if (rolesHeader != null && !rolesHeader.isBlank()) {
            return java.util.Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toList());
        }
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .collect(java.util.stream.Collectors.toList());
        }
        return java.util.List.of();
    }
}

