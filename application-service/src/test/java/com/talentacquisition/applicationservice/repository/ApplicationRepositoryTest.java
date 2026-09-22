package com.talentacquisition.applicationservice.repository;

import com.talentacquisition.applicationservice.entity.Application;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        Application app = Application.builder()
                .candidateId(101L)
                .jobId(201L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.APPLIED)
                .build();

        Application saved = applicationRepository.save(app);

        assertThat(saved.getId()).isNotNull();
        Optional<Application> found = applicationRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCandidateId()).isEqualTo(101L);
        assertThat(found.get().getJobId()).isEqualTo(201L);
        assertThat(found.get().getStatus()).isEqualTo(ApplicationStatus.APPLIED);
    }

    @Test
    void testFindByCandidateId() {
        Application app1 = Application.builder().candidateId(101L).jobId(201L).status(ApplicationStatus.APPLIED).build();
        Application app2 = Application.builder().candidateId(101L).jobId(202L).status(ApplicationStatus.SHORTLISTED).build();
        Application app3 = Application.builder().candidateId(102L).jobId(201L).status(ApplicationStatus.APPLIED).build();

        applicationRepository.saveAll(List.of(app1, app2, app3));

        List<Application> candidateApps = applicationRepository.findByCandidateId(101L);
        assertThat(candidateApps).hasSize(2);
    }

    @Test
    void testFindByJobId() {
        Application app1 = Application.builder().candidateId(101L).jobId(201L).status(ApplicationStatus.APPLIED).build();
        Application app2 = Application.builder().candidateId(102L).jobId(201L).status(ApplicationStatus.SHORTLISTED).build();

        applicationRepository.saveAll(List.of(app1, app2));

        List<Application> jobApps = applicationRepository.findByJobId(201L);
        assertThat(jobApps).hasSize(2);
    }

    @Test
    void testExistsByCandidateIdAndJobId() {
        Application app = Application.builder().candidateId(101L).jobId(201L).status(ApplicationStatus.APPLIED).build();
        applicationRepository.save(app);

        assertThat(applicationRepository.existsByCandidateIdAndJobId(101L, 201L)).isTrue();
        assertThat(applicationRepository.existsByCandidateIdAndJobId(101L, 999L)).isFalse();
    }

    @Test
    void testDuplicateApplicationConstraint() {
        Application app1 = Application.builder().candidateId(101L).jobId(201L).status(ApplicationStatus.APPLIED).build();
        applicationRepository.saveAndFlush(app1);

        Application app2 = Application.builder().candidateId(101L).jobId(201L).status(ApplicationStatus.APPLIED).build();
        assertThrows(DataIntegrityViolationException.class, () -> {
            applicationRepository.saveAndFlush(app2);
        });
    }
}

