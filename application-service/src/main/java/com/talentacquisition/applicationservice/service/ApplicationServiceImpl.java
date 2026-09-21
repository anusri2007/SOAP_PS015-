package com.talentacquisition.applicationservice.service;

import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.entity.Application;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import com.talentacquisition.applicationservice.exception.ApplicationNotFoundException;
import com.talentacquisition.applicationservice.exception.DuplicateApplicationException;
import com.talentacquisition.applicationservice.exception.ForbiddenException;
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
    private final com.talentacquisition.applicationservice.client.JobServiceClient jobServiceClient;
    private final com.talentacquisition.applicationservice.client.ProfileServiceClient profileServiceClient;

    @Override
    @Transactional
    public ApplicationResponse createApplication(Long candidateId, ApplyJobRequest request) {
        log.info("Creating application for candidateId={} and jobId={}", candidateId, request.getJobId());

        // Verify Job exists and is open via Job Service
        com.talentacquisition.applicationservice.client.JobResponseDto job = null;
        if (jobServiceClient != null) {
            try {
                job = jobServiceClient.getJobById(request.getJobId());
                if (job == null) {
                    throw new com.talentacquisition.applicationservice.exception.JobNotFoundException("Job not found with id: " + request.getJobId());
                }
                if (job.getStatus() != null &&
                    (job.getStatus().equalsIgnoreCase("CLOSED") || job.getStatus().equalsIgnoreCase("INACTIVE"))) {
                    throw new com.talentacquisition.applicationservice.exception.JobUnavailableException("Job is no longer open for applications: " + request.getJobId());
                }
            } catch (feign.FeignException.NotFound e) {
                throw new com.talentacquisition.applicationservice.exception.JobNotFoundException("Job not found with id: " + request.getJobId());
            } catch (feign.FeignException e) {
                log.error("Error communicating with Job Service", e);
                throw new com.talentacquisition.applicationservice.exception.ServiceCommunicationException("Job Service communication error: " + e.getMessage());
            }
        }

        // Verify Candidate profile exists and belongs to authenticated candidate via Profile Service
        if (profileServiceClient != null) {
            try {
                com.talentacquisition.applicationservice.client.ProfileResponseDto profile =
                        profileServiceClient.getProfileByCandidateId(candidateId);
                if (profile == null) {
                    throw new com.talentacquisition.applicationservice.exception.ProfileNotFoundException(
                            "Candidate profile not found for candidateId: " + candidateId);
                }
                if (profile.getCandidateId() != null && !profile.getCandidateId().equals(candidateId)) {
                    throw new com.talentacquisition.applicationservice.exception.ForbiddenException(
                            "Candidate profile does not belong to the authenticated candidate");
                }
            } catch (feign.FeignException.NotFound e) {
                throw new com.talentacquisition.applicationservice.exception.ProfileNotFoundException(
                        "Candidate profile not found for candidateId: " + candidateId);
            } catch (feign.FeignException e) {
                log.error("Error communicating with Profile Service", e);
                throw new com.talentacquisition.applicationservice.exception.ServiceCommunicationException(
                        "Profile Service communication error: " + e.getMessage());
            }
        }

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
        ApplicationResponse response = mapToResponse(saved);
        if (job != null && job.getTitle() != null) {
            response.setJobTitle(job.getTitle());
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + id));
        ApplicationResponse response = mapToResponse(application);
        enrichWithJobTitle(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id, Long callerId, List<String> roles) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with id: " + id));

        boolean isHrOrAdmin = roles != null && roles.stream().anyMatch(r ->
                r.equalsIgnoreCase("HR") || r.equalsIgnoreCase("ADMIN") ||
                r.equalsIgnoreCase("ROLE_HR") || r.equalsIgnoreCase("ROLE_ADMIN"));

        if (!isHrOrAdmin && callerId != null && !application.getCandidateId().equals(callerId)) {
            throw new ForbiddenException("You are not authorized to view this application");
        }

        ApplicationResponse response = mapToResponse(application);
        enrichWithJobTitle(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByCandidateId(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId).stream()
                .map(this::mapToResponse)
                .peek(this::enrichWithJobTitle)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByJobId(Long jobId) {
        return applicationRepository.findByJobId(jobId).stream()
                .map(this::mapToResponse)
                .peek(this::enrichWithJobTitle)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByJobId(Long jobId, Long callerId, List<String> roles) {
        boolean isAdmin = roles != null && roles.stream().anyMatch(r ->
                r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("ROLE_ADMIN"));
        boolean isHr = roles != null && roles.stream().anyMatch(r ->
                r.equalsIgnoreCase("HR") || r.equalsIgnoreCase("ROLE_HR"));

        if (!isAdmin && !isHr) {
            throw new ForbiddenException("Access denied: Only HR or Admin users can view job applications");
        }

        if (!isAdmin && isHr && callerId != null && jobServiceClient != null) {
            try {
                com.talentacquisition.applicationservice.client.JobResponseDto job =
                        jobServiceClient.getJobById(jobId);
                if (job == null) {
                    throw new com.talentacquisition.applicationservice.exception.JobNotFoundException("Job not found with id: " + jobId);
                }
                if (job.getHrId() != null && !job.getHrId().equals(callerId)) {
                    throw new ForbiddenException("HR user is not authorized to view applications for another HR's job");
                }
            } catch (feign.FeignException.NotFound e) {
                throw new com.talentacquisition.applicationservice.exception.JobNotFoundException("Job not found with id: " + jobId);
            } catch (feign.FeignException e) {
                log.error("Error communicating with Job Service", e);
                throw new com.talentacquisition.applicationservice.exception.ServiceCommunicationException("Job Service communication error: " + e.getMessage());
            }
        }

        return getApplicationsByJobId(jobId);
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
    public ApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus, Long callerId, List<String> roles) {
        boolean isHrOrAdmin = roles != null && roles.stream().anyMatch(r ->
                r.equalsIgnoreCase("HR") || r.equalsIgnoreCase("ADMIN") ||
                r.equalsIgnoreCase("ROLE_HR") || r.equalsIgnoreCase("ROLE_ADMIN"));

        if (!isHrOrAdmin) {
            throw new ForbiddenException("Access denied: Only HR or Admin users can update application status");
        }

        return updateApplicationStatus(id, newStatus);
    }

    @Override
    @Transactional
    public ApplicationResponse shortlistCandidate(Long id) {
        return updateApplicationStatus(id, ApplicationStatus.SHORTLISTED);
    }

    @Override
    @Transactional
    public ApplicationResponse shortlistCandidate(Long id, Long callerId, List<String> roles) {
        return updateApplicationStatus(id, ApplicationStatus.SHORTLISTED, callerId, roles);
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

    private void enrichWithJobTitle(ApplicationResponse response) {
        if (jobServiceClient != null && response != null && response.getJobId() != null) {
            try {
                com.talentacquisition.applicationservice.client.JobResponseDto job =
                        jobServiceClient.getJobById(response.getJobId());
                if (job != null && job.getTitle() != null) {
                    response.setJobTitle(job.getTitle());
                }
            } catch (Exception e) {
                log.debug("Could not fetch job title for jobId={}: {}", response.getJobId(), e.getMessage());
            }
        }
    }
}

