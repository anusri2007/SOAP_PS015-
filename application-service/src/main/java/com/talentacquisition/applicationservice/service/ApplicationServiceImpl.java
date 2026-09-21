package com.talentacquisition.applicationservice.service;

import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.entity.Application;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import com.talentacquisition.applicationservice.exception.ApplicationNotFoundException;
import com.talentacquisition.applicationservice.exception.DuplicateApplicationException;
import com.talentacquisition.applicationservice.exception.InvalidStatusException;
import com.talentacquisition.applicationservice.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public ApplicationResponse createApplication(Long candidateId, ApplyJobRequest request) {
        log.info("Creating application for candidateId={} and jobId={}", candidateId, request.getJobId());

        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, request.getJobId())) {
            throw new DuplicateApplicationException("Candidate " + candidateId + " has already applied for job " + request.getJobId());
        }

        Application application = Application.builder()
                .candidateId(candidateId)
                .jobId(request.getJobId())
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.APPLIED)
                .build();

        Application saved = applicationRepository.save(application);
        log.info("Application created successfully with id={}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + id));
        return mapToResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByCandidateId(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByJobId(Long jobId) {
        return applicationRepository.findByJobId(jobId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidStatusException("Application status cannot be null");
        }

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + id));

        application.setStatus(newStatus);
        Application updated = applicationRepository.save(application);
        log.info("Application id={} status updated to {}", id, newStatus);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ApplicationResponse shortlistCandidate(Long id) {
        return updateApplicationStatus(id, ApplicationStatus.SHORTLISTED);
    }

    @Override
    @Transactional
    public ApplicationResponse rejectCandidate(Long id) {
        return updateApplicationStatus(id, ApplicationStatus.REJECTED);
    }

    @Override
    @Transactional
    public ApplicationResponse selectCandidate(Long id) {
        return updateApplicationStatus(id, ApplicationStatus.SELECTED);
    }

    private ApplicationResponse mapToResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .candidateId(application.getCandidateId())
                .jobId(application.getJobId())
                .applicationDate(application.getApplicationDate())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}

