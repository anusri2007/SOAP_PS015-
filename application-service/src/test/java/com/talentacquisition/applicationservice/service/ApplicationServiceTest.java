package com.talentacquisition.applicationservice.service;

import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.entity.Application;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import com.talentacquisition.applicationservice.exception.ApplicationNotFoundException;
import com.talentacquisition.applicationservice.exception.DuplicateApplicationException;
import com.talentacquisition.applicationservice.exception.InvalidStatusException;
import com.talentacquisition.applicationservice.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private com.talentacquisition.applicationservice.client.JobServiceClient jobServiceClient;

    @Mock
    private com.talentacquisition.applicationservice.client.ProfileServiceClient profileServiceClient;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Application sampleApp;
    private ApplyJobRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleApp = Application.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.APPLIED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = ApplyJobRequest.builder()
                .jobId(201L)
                .coverLetter("Here is my application")
                .build();
    }

    @Test
    void testCreateApplication_Success() {
        com.talentacquisition.applicationservice.client.JobResponseDto job =
                com.talentacquisition.applicationservice.client.JobResponseDto.builder()
                        .id(201L)
                        .title("Software Engineer")
                        .status("OPEN")
                        .build();
        com.talentacquisition.applicationservice.client.ProfileResponseDto profile =
                com.talentacquisition.applicationservice.client.ProfileResponseDto.builder()
                        .id(50L)
                        .candidateId(101L)
                        .fullName("John Candidate")
                        .build();

        when(jobServiceClient.getJobById(201L)).thenReturn(job);
        when(profileServiceClient.getProfileByCandidateId(101L)).thenReturn(profile);
        when(applicationRepository.existsByCandidateIdAndJobId(101L, 201L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(sampleApp);

        ApplicationResponse response = applicationService.createApplication(101L, sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCandidateId()).isEqualTo(101L);
        assertThat(response.getJobId()).isEqualTo(201L);
        assertThat(response.getJobTitle()).isEqualTo("Software Engineer");
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void testCreateApplication_JobNotFound() {
        when(jobServiceClient.getJobById(201L)).thenReturn(null);

        assertThrows(com.talentacquisition.applicationservice.exception.JobNotFoundException.class, () -> {
            applicationService.createApplication(101L, sampleRequest);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void testCreateApplication_JobClosed() {
        com.talentacquisition.applicationservice.client.JobResponseDto job =
                com.talentacquisition.applicationservice.client.JobResponseDto.builder()
                        .id(201L)
                        .title("Software Engineer")
                        .status("CLOSED")
                        .build();

        when(jobServiceClient.getJobById(201L)).thenReturn(job);

        assertThrows(com.talentacquisition.applicationservice.exception.JobUnavailableException.class, () -> {
            applicationService.createApplication(101L, sampleRequest);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void testCreateApplication_ProfileNotFound() {
        com.talentacquisition.applicationservice.client.JobResponseDto job =
                com.talentacquisition.applicationservice.client.JobResponseDto.builder()
                        .id(201L)
                        .title("Software Engineer")
                        .status("OPEN")
                        .build();
        when(jobServiceClient.getJobById(201L)).thenReturn(job);
        when(profileServiceClient.getProfileByCandidateId(101L)).thenReturn(null);

        assertThrows(com.talentacquisition.applicationservice.exception.ProfileNotFoundException.class, () -> {
            applicationService.createApplication(101L, sampleRequest);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void testCreateApplication_ProfileDoesNotBelongToCandidate() {
        com.talentacquisition.applicationservice.client.JobResponseDto job =
                com.talentacquisition.applicationservice.client.JobResponseDto.builder()
                        .id(201L)
                        .title("Software Engineer")
                        .status("OPEN")
                        .build();
        com.talentacquisition.applicationservice.client.ProfileResponseDto profile =
                com.talentacquisition.applicationservice.client.ProfileResponseDto.builder()
                        .id(50L)
                        .candidateId(999L)
                        .build();

        when(jobServiceClient.getJobById(201L)).thenReturn(job);
        when(profileServiceClient.getProfileByCandidateId(101L)).thenReturn(profile);

        assertThrows(com.talentacquisition.applicationservice.exception.ForbiddenException.class, () -> {
            applicationService.createApplication(101L, sampleRequest);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void testCreateApplication_DuplicateThrowsException() {
        com.talentacquisition.applicationservice.client.JobResponseDto job =
                com.talentacquisition.applicationservice.client.JobResponseDto.builder()
                        .id(201L)
                        .title("Software Engineer")
                        .status("OPEN")
                        .build();
        com.talentacquisition.applicationservice.client.ProfileResponseDto profile =
                com.talentacquisition.applicationservice.client.ProfileResponseDto.builder()
                        .id(50L)
                        .candidateId(101L)
                        .build();
        when(jobServiceClient.getJobById(201L)).thenReturn(job);
        when(profileServiceClient.getProfileByCandidateId(101L)).thenReturn(profile);
        when(applicationRepository.existsByCandidateIdAndJobId(101L, 201L)).thenReturn(true);

        assertThrows(DuplicateApplicationException.class, () -> {
            applicationService.createApplication(101L, sampleRequest);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void testGetApplicationById_Success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        ApplicationResponse response = applicationService.getApplicationById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void testGetApplicationById_NotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.getApplicationById(99L);
        });
    }

    @Test
    void testGetApplicationById_AuthorizedCandidate() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        ApplicationResponse response = applicationService.getApplicationById(1L, 101L, List.of("ROLE_CANDIDATE"));

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void testGetApplicationById_UnauthorizedCandidate() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        assertThrows(com.talentacquisition.applicationservice.exception.ForbiddenException.class, () -> {
            applicationService.getApplicationById(1L, 999L, List.of("ROLE_CANDIDATE"));
        });
    }

    @Test
    void testGetApplicationById_AuthorizedHR() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        ApplicationResponse response = applicationService.getApplicationById(1L, 999L, List.of("ROLE_HR"));

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void testGetApplicationsByCandidateId() {
        when(applicationRepository.findByCandidateId(101L)).thenReturn(List.of(sampleApp));

        List<ApplicationResponse> list = applicationService.getApplicationsByCandidateId(101L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCandidateId()).isEqualTo(101L);
    }

    @Test
    void testGetApplicationsByJobId() {
        when(applicationRepository.findByJobId(201L)).thenReturn(List.of(sampleApp));

        List<ApplicationResponse> list = applicationService.getApplicationsByJobId(201L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getJobId()).isEqualTo(201L);
    }

    @Test
    void testUpdateApplicationStatus_Success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));
        when(applicationRepository.save(any(Application.class))).thenReturn(sampleApp);

        ApplicationResponse response = applicationService.updateApplicationStatus(1L, ApplicationStatus.SHORTLISTED);

        assertThat(response).isNotNull();
        verify(applicationRepository).save(sampleApp);
        assertThat(sampleApp.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
    }

    @Test
    void testUpdateApplicationStatus_NullThrowsException() {
        assertThrows(InvalidStatusException.class, () -> {
            applicationService.updateApplicationStatus(1L, null);
        });
    }

    @Test
    void testShortlistCandidate() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));
        when(applicationRepository.save(any(Application.class))).thenReturn(sampleApp);

        ApplicationResponse response = applicationService.shortlistCandidate(1L);

        assertThat(response).isNotNull();
        assertThat(sampleApp.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
    }

    @Test
    void testRejectCandidate() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));
        when(applicationRepository.save(any(Application.class))).thenReturn(sampleApp);

        ApplicationResponse response = applicationService.rejectCandidate(1L);

        assertThat(response).isNotNull();
        assertThat(sampleApp.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void testSelectCandidate() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));
        when(applicationRepository.save(any(Application.class))).thenReturn(sampleApp);

        ApplicationResponse response = applicationService.selectCandidate(1L);

        assertThat(response).isNotNull();
        assertThat(sampleApp.getStatus()).isEqualTo(ApplicationStatus.SELECTED);
    }
}

