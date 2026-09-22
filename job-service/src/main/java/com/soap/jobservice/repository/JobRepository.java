package com.soap.jobservice.repository;

import com.soap.jobservice.entity.Job;
import com.soap.jobservice.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    List<Job> findByHrId(Long hrId);

    List<Job> findByStatus(JobStatus status);

    List<Job> findByCompanyIgnoreCase(String company);
}
