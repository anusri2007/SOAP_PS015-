package com.soap.jobservice.service.impl;

import com.soap.jobservice.dto.JobRequestDto;
import com.soap.jobservice.dto.JobResponseDto;
import com.soap.jobservice.entity.EmploymentType;
import com.soap.jobservice.entity.Job;
import com.soap.jobservice.entity.JobStatus;
import com.soap.jobservice.exception.ResourceNotFoundException;
import com.soap.jobservice.repository.JobRepository;
import com.soap.jobservice.repository.JobSpecification;
import com.soap.jobservice.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    private final JobRepository jobRepository;

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public JobResponseDto createJob(JobRequestDto request) {
        log.info("Creating new job listing: title='{}', company='{}'", request.getTitle(), request.getCompany());
        Job job = Job.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .company(request.getCompany().trim())
                .location(request.getLocation().trim())
                .employmentType(request.getEmploymentType())
                .experienceRequired(request.getExperienceRequired().trim())
                .salary(request.getSalary())
                .skills(request.getSkills().trim())
                .closingDate(request.getClosingDate())
                .status(request.getStatus() != null ? request.getStatus() : JobStatus.OPEN)
                .hrId(request.getHrId())
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job successfully created with id={}", savedJob.getId());
        return JobResponseDto.fromEntity(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> getAllJobs() {
        log.info("Fetching all jobs ordered by postedDate DESC");
        return jobRepository.findAll(Sort.by(Sort.Direction.DESC, "postedDate", "id"))
                .stream()
                .map(JobResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponseDto getJobById(Long id) {
        log.info("Fetching job with id={}", id);
        Job job = findJobOrThrow(id);
        return JobResponseDto.fromEntity(job);
    }

    @Override
    public JobResponseDto updateJob(Long id, JobRequestDto request) {
        log.info("Updating job with id={}", id);
        Job job = findJobOrThrow(id);

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setCompany(request.getCompany().trim());
        job.setLocation(request.getLocation().trim());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceRequired(request.getExperienceRequired().trim());
        job.setSalary(request.getSalary());
        job.setSkills(request.getSkills().trim());
        job.setClosingDate(request.getClosingDate());
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }
        job.setHrId(request.getHrId());

        Job updatedJob = jobRepository.save(job);
        log.info("Job successfully updated with id={}", updatedJob.getId());
        return JobResponseDto.fromEntity(updatedJob);
    }

    @Override
    public void deleteJob(Long id) {
        log.info("Deleting job with id={}", id);
        Job job = findJobOrThrow(id);
        jobRepository.delete(job);
        log.info("Job successfully deleted with id={}", id);
    }

    @Override
    public JobResponseDto updateJobStatus(Long id, JobStatus status) {
        log.info("Updating status of job id={} to status={}", id, status);
        Job job = findJobOrThrow(id);
        job.setStatus(status);
        Job saved = jobRepository.save(job);
        return JobResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> searchJobs(String keyword) {
        log.info("Searching jobs with keyword='{}'", keyword);
        Specification<Job> spec = JobSpecification.hasKeyword(keyword);
        return jobRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "postedDate", "id"))
                .stream()
                .map(JobResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> filterJobs(
            String location,
            EmploymentType employmentType,
            JobStatus status,
            Double minSalary,
            Double maxSalary,
            String company) {
        log.info("Filtering jobs: location='{}', type='{}', status='{}', minSalary='{}', maxSalary='{}', company='{}'",
                location, employmentType, status, minSalary, maxSalary, company);
        Specification<Job> spec = JobSpecification.filter(
                location, employmentType, status, minSalary, maxSalary, company);
        return jobRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "postedDate", "id"))
                .stream()
                .map(JobResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> getJobsByHrId(Long hrId) {
        log.info("Fetching jobs posted by hrId={}", hrId);
        return jobRepository.findByHrId(hrId)
                .stream()
                .map(JobResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    private Job findJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }
}
