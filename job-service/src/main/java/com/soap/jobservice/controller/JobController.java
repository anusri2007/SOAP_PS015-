package com.soap.jobservice.controller;

import com.soap.jobservice.dto.ApiResponse;
import com.soap.jobservice.dto.JobRequestDto;
import com.soap.jobservice.dto.JobResponseDto;
import com.soap.jobservice.dto.JobStatusUpdateDto;
import com.soap.jobservice.entity.EmploymentType;
import com.soap.jobservice.entity.JobStatus;
import com.soap.jobservice.service.JobService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponseDto>> createJob(@Valid @RequestBody JobRequestDto request) {
        log.info("REST request to create Job: title='{}', company='{}'", request.getTitle(), request.getCompany());
        JobResponseDto createdJob = jobService.createJob(request);
        return new ResponseEntity<>(
                ApiResponse.success(createdJob, "Job listing created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponseDto>>> getAllJobs() {
        log.info("REST request to get all Jobs");
        List<JobResponseDto> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success(jobs, "Fetched all jobs successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponseDto>> getJobById(@PathVariable Long id) {
        log.info("REST request to get Job by ID: {}", id);
        JobResponseDto job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(job, "Job fetched successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponseDto>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequestDto request) {
        log.info("REST request to update Job with ID: {}", id);
        JobResponseDto updatedJob = jobService.updateJob(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedJob, "Job updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        log.info("REST request to delete Job with ID: {}", id);
        jobService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<JobResponseDto>> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody JobStatusUpdateDto request) {
        log.info("REST request to update Job status: id={}, status={}", id, request.getStatus());
        JobResponseDto updatedJob = jobService.updateJobStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(updatedJob, "Job status updated successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<JobResponseDto>>> searchJobs(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        log.info("REST request to search Jobs by keyword: '{}'", keyword);
        List<JobResponseDto> jobs = jobService.searchJobs(keyword);
        return ResponseEntity.ok(ApiResponse.success(jobs, "Job search results retrieved"));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<JobResponseDto>>> filterJobs(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String company) {
        log.info("REST request to filter Jobs: location='{}', type='{}', status='{}', minSalary='{}', maxSalary='{}', company='{}'",
                location, employmentType, status, minSalary, maxSalary, company);
        List<JobResponseDto> jobs = jobService.filterJobs(location, employmentType, status, minSalary, maxSalary, company);
        return ResponseEntity.ok(ApiResponse.success(jobs, "Filtered jobs retrieved successfully"));
    }

    @GetMapping("/hr/{hrId}")
    public ResponseEntity<ApiResponse<List<JobResponseDto>>> getJobsByHrId(@PathVariable Long hrId) {
        log.info("REST request to get Jobs posted by HR: {}", hrId);
        List<JobResponseDto> jobs = jobService.getJobsByHrId(hrId);
        return ResponseEntity.ok(ApiResponse.success(jobs, "HR jobs retrieved successfully"));
    }
}
