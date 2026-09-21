package com.soap.jobservice.service;

import com.soap.jobservice.dto.JobRequestDto;
import com.soap.jobservice.dto.JobResponseDto;
import com.soap.jobservice.entity.EmploymentType;
import com.soap.jobservice.entity.Job;
import com.soap.jobservice.entity.JobStatus;
import com.soap.jobservice.exception.ResourceNotFoundException;
import com.soap.jobservice.repository.JobRepository;
import com.soap.jobservice.service.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    private Job sampleJob;
    private JobRequestDto sampleRequest;

    @BeforeEach
    void setUp() {
        sampleJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .description("Build microservices with Spring Boot and PostgreSQL")
                .company("Acme Corp")
                .location("Hyderabad")
                .employmentType(EmploymentType.FULL_TIME)
                .experienceRequired("3-5 years")
                .salary(1200000.0)
                .skills("Java, Spring Boot, PostgreSQL, Docker")
                .postedDate(LocalDate.now())
                .closingDate(LocalDate.now().plusDays(30))
                .status(JobStatus.OPEN)
                .hrId(101L)
                .build();

        sampleRequest = JobRequestDto.builder()
                .title("Senior Java Developer")
                .description("Build microservices with Spring Boot and PostgreSQL")
                .company("Acme Corp")
                .location("Hyderabad")
                .employmentType(EmploymentType.FULL_TIME)
                .experienceRequired("3-5 years")
                .salary(1200000.0)
                .skills("Java, Spring Boot, PostgreSQL, Docker")
                .closingDate(LocalDate.now().plusDays(30))
                .status(JobStatus.OPEN)
                .hrId(101L)
                .build();
    }

    @Test
    @DisplayName("createJob should persist and return JobResponseDto")
    void testCreateJob_Success() {
        when(jobRepository.save(any(Job.class))).thenReturn(sampleJob);

        JobResponseDto response = jobService.createJob(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Senior Java Developer");
        assertThat(response.getCompany()).isEqualTo("Acme Corp");
        assertThat(response.getStatus()).isEqualTo(JobStatus.OPEN);

        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    @DisplayName("getJobById should return job when found")
    void testGetJobById_Found() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));

        JobResponseDto response = jobService.getJobById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Senior Java Developer");
        verify(jobRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getJobById should throw ResourceNotFoundException when not found")
    void testGetJobById_NotFound() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found with id: 999");

        verify(jobRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getAllJobs should return list of jobs")
    void testGetAllJobs() {
        when(jobRepository.findAll(any(Sort.class))).thenReturn(List.of(sampleJob));

        List<JobResponseDto> result = jobService.getAllJobs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Senior Java Developer");
        verify(jobRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("updateJob should modify fields and save")
    void testUpdateJob_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(jobRepository.save(any(Job.class))).thenReturn(sampleJob);

        sampleRequest.setTitle("Lead Java Architect");
        JobResponseDto response = jobService.updateJob(1L, sampleRequest);

        assertThat(response).isNotNull();
        verify(jobRepository, times(1)).findById(1L);
        verify(jobRepository, times(1)).save(sampleJob);
    }

    @Test
    @DisplayName("deleteJob should delete entity when found")
    void testDeleteJob_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        doNothing().when(jobRepository).delete(sampleJob);

        jobService.deleteJob(1L);

        verify(jobRepository, times(1)).findById(1L);
        verify(jobRepository, times(1)).delete(sampleJob);
    }

    @Test
    @DisplayName("updateJobStatus should change status and save")
    void testUpdateJobStatus_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(jobRepository.save(any(Job.class))).thenReturn(sampleJob);

        JobResponseDto response = jobService.updateJobStatus(1L, JobStatus.CLOSED);

        assertThat(response).isNotNull();
        assertThat(sampleJob.getStatus()).isEqualTo(JobStatus.CLOSED);
        verify(jobRepository, times(1)).save(sampleJob);
    }

    @Test
    @DisplayName("searchJobs should query repository with Specification")
    void testSearchJobs() {
        when(jobRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(sampleJob));

        List<JobResponseDto> results = jobService.searchJobs("Java");

        assertThat(results).hasSize(1);
        verify(jobRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("filterJobs should query repository with dynamic filters")
    void testFilterJobs() {
        when(jobRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(sampleJob));

        List<JobResponseDto> results = jobService.filterJobs(
                "Hyderabad", EmploymentType.FULL_TIME, JobStatus.OPEN, 500000.0, 1500000.0, "Acme");

        assertThat(results).hasSize(1);
        verify(jobRepository, times(1)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("getJobsByHrId should return jobs for given HR")
    void testGetJobsByHrId() {
        when(jobRepository.findByHrId(101L)).thenReturn(List.of(sampleJob));

        List<JobResponseDto> results = jobService.getJobsByHrId(101L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getHrId()).isEqualTo(101L);
        verify(jobRepository, times(1)).findByHrId(101L);
    }
}
