package com.soap.jobservice.service;

import com.soap.jobservice.dto.JobRequestDto;
import com.soap.jobservice.dto.JobResponseDto;
import com.soap.jobservice.entity.EmploymentType;
import com.soap.jobservice.entity.JobStatus;

import java.util.List;

public interface JobService {

    JobResponseDto createJob(JobRequestDto request);

    List<JobResponseDto> getAllJobs();

    JobResponseDto getJobById(Long id);

    JobResponseDto updateJob(Long id, JobRequestDto request);

    void deleteJob(Long id);

    JobResponseDto updateJobStatus(Long id, JobStatus status);

    List<JobResponseDto> searchJobs(String keyword);

    List<JobResponseDto> filterJobs(String location,
                                     EmploymentType employmentType,
                                     JobStatus status,
                                     Double minSalary,
                                     Double maxSalary,
                                     String company);

    List<JobResponseDto> getJobsByHrId(Long hrId);
}
