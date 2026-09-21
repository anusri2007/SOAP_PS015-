package com.talentacquisition.applicationservice.repository;

import com.talentacquisition.applicationservice.entity.Application;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByJobId(Long jobId);

    Optional<Application> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    List<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status);

    List<Application> findByCandidateIdAndStatus(Long candidateId, ApplicationStatus status);
}

