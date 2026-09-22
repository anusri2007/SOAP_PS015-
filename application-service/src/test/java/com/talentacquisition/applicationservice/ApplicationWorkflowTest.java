package com.talentacquisition.applicationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentacquisition.applicationservice.controller.ApplicationController;
import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplicationStatusUpdateRequest;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import com.talentacquisition.applicationservice.exception.DuplicateApplicationException;
import com.talentacquisition.applicationservice.exception.ForbiddenException;
import com.talentacquisition.applicationservice.exception.GlobalExceptionHandler;
import com.talentacquisition.applicationservice.exception.InvalidStatusException;
import com.talentacquisition.applicationservice.exception.JobNotFoundException;
import com.talentacquisition.applicationservice.exception.ProfileNotFoundException;
import com.talentacquisition.applicationservice.security.JwtAccessDeniedHandler;
import com.talentacquisition.applicationservice.security.JwtAuthenticationEntryPoint;
import com.talentacquisition.applicationservice.security.JwtAuthenticationFilter;
import com.talentacquisition.applicationservice.security.JwtUtils;
import com.talentacquisition.applicationservice.security.SecurityConfig;
import com.talentacquisition.applicationservice.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplicationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtUtils.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class ApplicationWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private ApplicationService applicationService;

    private String candidateToken;
    private String hrToken;

    @BeforeEach
    void setUp() {
        candidateToken = "Bearer " + jwtUtils.generateToken(101L, "candidate@kluniversity.in", List.of("ROLE_CANDIDATE"));
        hrToken = "Bearer " + jwtUtils.generateToken(501L, "hr@kluniversity.in", List.of("ROLE_HR"));
    }

    // Scenario 1: Apply for existing job
    @Test
    @DisplayName("Scenario 1: Candidate applies for existing job -> 201 Created")
    void scenario1_applyForExistingJob() throws Exception {
        ApplyJobRequest request = ApplyJobRequest.builder()
                .jobId(201L)
                .coverLetter("I am an experienced Spring Boot engineer")
                .build();

        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDateTime.now())
                .jobTitle("Senior Java Developer")
                .build();

        when(applicationService.createApplication(eq(101L), any(ApplyJobRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.candidateId").value(101L))
                .andExpect(jsonPath("$.jobId").value(201L))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.jobTitle").value("Senior Java Developer"));
    }

    // Scenario 2: Apply for nonexistent job
    @Test
    @DisplayName("Scenario 2: Apply for nonexistent job -> 404 Not Found")
    void scenario2_applyForNonexistentJob() throws Exception {
        ApplyJobRequest request = ApplyJobRequest.builder()
                .jobId(999L)
                .coverLetter("Applying for missing job")
                .build();

        when(applicationService.createApplication(eq(101L), any(ApplyJobRequest.class)))
                .thenThrow(new JobNotFoundException("Job with ID 999 does not exist"));

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Job with ID 999 does not exist")));
    }

    // Scenario 3: Apply without candidate profile
    @Test
    @DisplayName("Scenario 3: Apply without candidate profile -> 404 Not Found")
    void scenario3_applyWithoutCandidateProfile() throws Exception {
        ApplyJobRequest request = ApplyJobRequest.builder()
                .jobId(201L)
                .coverLetter("Applying without profile")
                .build();

        when(applicationService.createApplication(eq(101L), any(ApplyJobRequest.class)))
                .thenThrow(new ProfileNotFoundException("Candidate profile not found for candidate ID: 101"));

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Candidate profile not found for candidate ID: 101")));
    }

    // Scenario 4: Duplicate application
    @Test
    @DisplayName("Scenario 4: Duplicate application -> 409 Conflict")
    void scenario4_duplicateApplication() throws Exception {
        ApplyJobRequest request = ApplyJobRequest.builder()
                .jobId(201L)
                .coverLetter("Applying again for the same job")
                .build();

        when(applicationService.createApplication(eq(101L), any(ApplyJobRequest.class)))
                .thenThrow(new DuplicateApplicationException("Candidate 101 has already applied for job 201"));

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("has already applied for job 201")));
    }

    // Scenario 5: Candidate views own applications
    @Test
    @DisplayName("Scenario 5: Candidate views own applications -> 200 OK")
    void scenario5_candidateViewsOwnApplications() throws Exception {
        ApplicationResponse app1 = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .status(ApplicationStatus.APPLIED)
                .jobTitle("Backend Engineer")
                .build();

        ApplicationResponse app2 = ApplicationResponse.builder()
                .id(2L)
                .candidateId(101L)
                .jobId(202L)
                .status(ApplicationStatus.SHORTLISTED)
                .jobTitle("Frontend Engineer")
                .build();

        when(applicationService.getApplicationsByCandidateId(101L))
                .thenReturn(List.of(app1, app2));

        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].jobTitle").value("Backend Engineer"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("SHORTLISTED"));
    }

    // Scenario 6: Candidate attempts to view another candidate's applications (403)
    @Test
    @DisplayName("Scenario 6: Candidate attempts to view another candidate's application -> 403 Forbidden")
    void scenario6_candidateAttemptsToViewAnotherCandidateApplication() throws Exception {
        when(applicationService.getApplicationById(eq(2L), eq(101L), anyList()))
                .thenThrow(new ForbiddenException("You are not authorized to view another candidate's application"));

        mockMvc.perform(get("/api/applications/2")
                        .header("Authorization", candidateToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString("not authorized to view another candidate's application")));
    }

    // Scenario 7: HR views job applications
    @Test
    @DisplayName("Scenario 7: HR views job applications -> 200 OK")
    void scenario7_hrViewsJobApplications() throws Exception {
        ApplicationResponse app1 = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .status(ApplicationStatus.APPLIED)
                .jobTitle("Backend Engineer")
                .build();

        ApplicationResponse app2 = ApplicationResponse.builder()
                .id(2L)
                .candidateId(102L)
                .jobId(201L)
                .status(ApplicationStatus.SHORTLISTED)
                .jobTitle("Backend Engineer")
                .build();

        when(applicationService.getApplicationsByJobId(eq(201L), eq(501L), anyList()))
                .thenReturn(List.of(app1, app2));

        mockMvc.perform(get("/api/applications/job/201")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].candidateId").value(101L))
                .andExpect(jsonPath("$[1].candidateId").value(102L));
    }

    // Scenario 8: HR updates application status
    @Test
    @DisplayName("Scenario 8: HR updates application status -> 200 OK")
    void scenario8_hrUpdatesApplicationStatus() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        ApplicationResponse updatedResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(applicationService.updateApplicationStatus(eq(1L), eq(ApplicationStatus.SHORTLISTED), eq(501L), anyList()))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/applications/1/status")
                        .header("Authorization", hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    // Scenario 9: HR shortlists candidate
    @Test
    @DisplayName("Scenario 9: HR shortlists candidate -> 200 OK")
    void scenario9_hrShortlistsCandidate() throws Exception {
        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(applicationService.shortlistCandidate(eq(1L), eq(501L), anyList()))
                .thenReturn(response);

        mockMvc.perform(post("/api/applications/1/shortlist")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    // Scenario 10: HR rejects candidate
    @Test
    @DisplayName("Scenario 10: HR rejects candidate -> 200 OK")
    void scenario10_hrRejectsCandidate() throws Exception {
        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .status(ApplicationStatus.REJECTED)
                .build();

        when(applicationService.rejectCandidate(eq(1L), eq(501L), anyList()))
                .thenReturn(response);

        mockMvc.perform(post("/api/applications/1/reject")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    // Scenario 11: HR selects candidate
    @Test
    @DisplayName("Scenario 11: HR selects candidate -> 200 OK")
    void scenario11_hrSelectsCandidate() throws Exception {
        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .status(ApplicationStatus.SELECTED)
                .build();

        when(applicationService.selectCandidate(eq(1L), eq(501L), anyList()))
                .thenReturn(response);

        mockMvc.perform(post("/api/applications/1/select")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SELECTED"));
    }

    // Scenario 12: Invalid application status (400)
    @Test
    @DisplayName("Scenario 12: Invalid application status transition or payload -> 400 Bad Request")
    void scenario12_invalidApplicationStatus() throws Exception {
        // Test invalid transition from business logic
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus(ApplicationStatus.SELECTED);

        when(applicationService.updateApplicationStatus(eq(1L), eq(ApplicationStatus.SELECTED), eq(501L), anyList()))
                .thenThrow(new InvalidStatusException("Cannot transition application directly to SELECTED from REJECTED"));

        mockMvc.perform(put("/api/applications/1/status")
                        .header("Authorization", hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Cannot transition application")));

        // Also test invalid status JSON string deserialization
        mockMvc.perform(put("/api/applications/1/status")
                        .header("Authorization", hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"NON_EXISTENT_STATUS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // Scenario 13: Request without JWT (401)
    @Test
    @DisplayName("Scenario 13: Request without JWT -> 401 Unauthorized")
    void scenario13_requestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/applications/my"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // Scenario 14: Unauthorized candidate attempting HR operation (403)
    @Test
    @DisplayName("Scenario 14: Unauthorized candidate attempting HR operation -> 403 Forbidden")
    void scenario14_candidateAttemptingHrOperation() throws Exception {
        // Candidate trying to shortlist
        mockMvc.perform(post("/api/applications/1/shortlist")
                        .header("Authorization", candidateToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        // Candidate trying to reject
        mockMvc.perform(post("/api/applications/1/reject")
                        .header("Authorization", candidateToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // Candidate trying to select
        mockMvc.perform(post("/api/applications/1/select")
                        .header("Authorization", candidateToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // Candidate trying to view all applications for a job
        mockMvc.perform(get("/api/applications/job/201")
                        .header("Authorization", candidateToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}

