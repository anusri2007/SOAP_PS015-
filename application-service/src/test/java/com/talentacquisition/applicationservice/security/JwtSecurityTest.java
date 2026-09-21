package com.talentacquisition.applicationservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentacquisition.applicationservice.controller.ApplicationController;
import com.talentacquisition.applicationservice.dto.ApplicationResponse;
import com.talentacquisition.applicationservice.dto.ApplyJobRequest;
import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import com.talentacquisition.applicationservice.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtUtils.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class JwtSecurityTest {

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
        candidateToken = "Bearer " + jwtUtils.generateToken(101L, "candidate@example.com", List.of("ROLE_CANDIDATE"));
        hrToken = "Bearer " + jwtUtils.generateToken(501L, "hr@example.com", List.of("ROLE_HR"));
    }

    @Test
    void testRequestWithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/applications/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRequestWithInvalidToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", "Bearer invalid-jwt-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCandidateToken_CanApplyJob() throws Exception {
        ApplyJobRequest request = ApplyJobRequest.builder().jobId(201L).build();
        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .candidateId(101L)
                .jobId(201L)
                .status(ApplicationStatus.APPLIED)
                .applicationDate(LocalDateTime.now())
                .build();

        when(applicationService.createApplication(eq(101L), any(ApplyJobRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCandidateToken_CanViewOwnApplications() throws Exception {
        when(applicationService.getApplicationsByCandidateId(101L)).thenReturn(List.of());

        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", candidateToken))
                .andExpect(status().isOk());
    }

    @Test
    void testCandidateToken_CannotPerformHROperation_Forbidden() throws Exception {
        mockMvc.perform(post("/api/applications/1/shortlist")
                        .header("Authorization", candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testHRToken_CanViewJobApplications() throws Exception {
        when(applicationService.getApplicationsByJobId(eq(201L), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/applications/job/201")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk());
    }

    @Test
    void testHRToken_CanShortlistCandidate() throws Exception {
        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(applicationService.shortlistCandidate(eq(1L), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/applications/1/shortlist")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk());
    }
}

