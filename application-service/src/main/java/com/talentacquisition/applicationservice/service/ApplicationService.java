package com.talentacquisition.applicationservice.service;

import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;

import java.util.List;

public interface ApplicationService {

    ApplicationResponse createApplication(Long candidateId, ApplyJobRequest request);

    ApplicationResponse getApplicationById(Long id);

    ApplicationResponse getApplicationById(Long id, Long callerId, List<String> roles);

    List<ApplicationResponse> getApplicationsByCandidateId(Long candidateId);

    List<ApplicationResponse> getApplicationsByJobId(Long jobId);

    List<ApplicationResponse> getApplicationsByJobId(Long jobId, Long callerId, List<String> roles);

    ApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus);

    ApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus, Long callerId, List<String> roles);

    ApplicationResponse shortlistCandidate(Long id);

    ApplicationResponse shortlistCandidate(Long id, Long callerId, List<String> roles);

    ApplicationResponse rejectCandidate(Long id);

    ApplicationResponse rejectCandidate(Long id, Long callerId, List<String> roles);

    ApplicationResponse selectCandidate(Long id);
}

