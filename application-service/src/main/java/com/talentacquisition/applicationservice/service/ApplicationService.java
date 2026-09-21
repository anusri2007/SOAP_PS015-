package com.talentacquisition.applicationservice.service;

import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;

import java.util.List;

public interface ApplicationService {

    ApplicationResponse createApplication(Long candidateId, ApplyJobRequest request);

    ApplicationResponse getApplicationById(Long id);

    List<ApplicationResponse> getApplicationsByCandidateId(Long candidateId);

    List<ApplicationResponse> getApplicationsByJobId(Long jobId);

    ApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus);

    ApplicationResponse shortlistCandidate(Long id);

    ApplicationResponse rejectCandidate(Long id);

    ApplicationResponse selectCandidate(Long id);
}

