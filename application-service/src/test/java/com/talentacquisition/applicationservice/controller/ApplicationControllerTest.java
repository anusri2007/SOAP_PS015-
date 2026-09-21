package com.talentacquisition.applicationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import com.talentacquisition.applicationservice.security.SecurityConfig;
import com.talentacquisition.applicationservice.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@Import(SecurityConfig.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @Test
    void testApplyForJob_Success() throws Exception {
        ApplyJobRequest request = ApplyJobRequest.builder()
                .jobId(501L)
                .coverLetter("Applying for software engineer")
                .build();

        ApplicationResponse mockResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(501L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.APPLIED)
                .build();

        when(applicationService.createApplication(eq(101L), any(ApplyJobRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/applications")
                        .header("X-Candidate-Id", 101L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.candidateId").value(101L))
                .andExpect(jsonPath("$.jobId").value(501L))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void testApplyForJob_ValidationError_MissingJobId() throws Exception {
        ApplyJobRequest request = ApplyJobRequest.builder()
                .jobId(null)
                .build();

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMyApplications() throws Exception {
        ApplicationResponse mockResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(501L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.APPLIED)
                .jobTitle("Backend Engineer")
                .build();

        when(applicationService.getApplicationsByCandidateId(101L))
                .thenReturn(java.util.List.of(mockResponse));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/applications/my")
                        .header("X-Candidate-Id", 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].candidateId").value(101L))
                .andExpect(jsonPath("$[0].jobId").value(501L))
                .andExpect(jsonPath("$[0].jobTitle").value("Backend Engineer"))
                .andExpect(jsonPath("$[0].status").value("APPLIED"));
    }

    @Test
    void testGetApplicationById() throws Exception {
        ApplicationResponse mockResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(501L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.APPLIED)
                .jobTitle("Backend Engineer")
                .build();

        when(applicationService.getApplicationById(eq(1L), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/applications/1")
                        .header("X-Candidate-Id", 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.candidateId").value(101L))
                .andExpect(jsonPath("$.jobId").value(501L))
                .andExpect(jsonPath("$.jobTitle").value("Backend Engineer"));
    }

    @Test
    void testGetApplicationsForJob() throws Exception {
        ApplicationResponse mockResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(501L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.APPLIED)
                .jobTitle("Backend Engineer")
                .build();

        when(applicationService.getApplicationsByJobId(eq(501L), any(), any()))
                .thenReturn(java.util.List.of(mockResponse));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/applications/job/501")
                        .header("X-User-Id", 501L)
                        .header("X-User-Roles", "ROLE_HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].jobId").value(501L))
                .andExpect(jsonPath("$[0].status").value("APPLIED"));
    }

    @Test
    void testUpdateApplicationStatus_Success() throws Exception {
        com.talentacquisition.applicationservice.dto.ApplicationStatusUpdateRequest request =
                com.talentacquisition.applicationservice.dto.ApplicationStatusUpdateRequest.builder()
                        .status(ApplicationStatus.SHORTLISTED)
                        .remarks("Looks promising")
                        .build();

        ApplicationResponse mockResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(501L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(applicationService.updateApplicationStatus(eq(1L), eq(ApplicationStatus.SHORTLISTED), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/applications/1/status")
                        .header("X-User-Id", 501L)
                        .header("X-User-Roles", "ROLE_HR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void testUpdateApplicationStatus_ValidationError_NullStatus() throws Exception {
        com.talentacquisition.applicationservice.dto.ApplicationStatusUpdateRequest request =
                com.talentacquisition.applicationservice.dto.ApplicationStatusUpdateRequest.builder()
                        .status(null)
                        .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/applications/1/status")
                        .header("X-User-Id", 501L)
                        .header("X-User-Roles", "ROLE_HR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testShortlistCandidate() throws Exception {
        ApplicationResponse mockResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(501L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(applicationService.shortlistCandidate(eq(1L), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/applications/1/shortlist")
                        .header("X-User-Id", 501L)
                        .header("X-User-Roles", "ROLE_HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void testRejectCandidate() throws Exception {
        ApplicationResponse mockResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(501L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.REJECTED)
                .build();

        when(applicationService.rejectCandidate(eq(1L), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/applications/1/reject")
                        .header("X-User-Id", 501L)
                        .header("X-User-Roles", "ROLE_HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void testSelectCandidate() throws Exception {
        ApplicationResponse mockResponse = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(501L)
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.SELECTED)
                .build();

        when(applicationService.selectCandidate(eq(1L), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/applications/1/select")
                        .header("X-User-Id", 501L)
                        .header("X-User-Roles", "ROLE_HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SELECTED"));
    }
}

