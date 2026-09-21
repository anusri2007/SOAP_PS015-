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
}

