package com.soap.jobservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soap.jobservice.dto.JobRequestDto;
import com.soap.jobservice.dto.JobResponseDto;
import com.soap.jobservice.entity.EmploymentType;
import com.soap.jobservice.entity.JobStatus;
import com.soap.jobservice.exception.GlobalExceptionHandler;
import com.soap.jobservice.exception.ResourceNotFoundException;
import com.soap.jobservice.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@Import(GlobalExceptionHandler.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    private JobResponseDto sampleResponse;
    private JobRequestDto validRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = JobResponseDto.builder()
                .id(1L)
                .title("Software Engineer")
                .description("Design and build high-performance APIs")
                .company("Google")
                .location("Bengaluru")
                .employmentType(EmploymentType.FULL_TIME)
                .experienceRequired("2-4 years")
                .salary(1500000.0)
                .skills("Java, Spring Boot, Microservices")
                .postedDate(LocalDate.now())
                .closingDate(LocalDate.now().plusDays(45))
                .status(JobStatus.OPEN)
                .hrId(200L)
                .build();

        validRequest = JobRequestDto.builder()
                .title("Software Engineer")
                .description("Design and build high-performance APIs")
                .company("Google")
                .location("Bengaluru")
                .employmentType(EmploymentType.FULL_TIME)
                .experienceRequired("2-4 years")
                .salary(1500000.0)
                .skills("Java, Spring Boot, Microservices")
                .closingDate(LocalDate.now().plusDays(45))
                .status(JobStatus.OPEN)
                .hrId(200L)
                .build();
    }

    @Test
    @DisplayName("POST /api/jobs with valid payload should return 201 Created")
    void testCreateJob_Valid() throws Exception {
        when(jobService.createJob(any(JobRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.title", is("Software Engineer")))
                .andExpect(jsonPath("$.message", containsString("created successfully")));
    }

    @Test
    @DisplayName("POST /api/jobs with missing title should return 400 Bad Request")
    void testCreateJob_Invalid_MissingTitle() throws Exception {
        validRequest.setTitle(""); // blank title

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.validationErrors.title", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/jobs with negative salary should return 400 Bad Request")
    void testCreateJob_Invalid_NegativeSalary() throws Exception {
        validRequest.setSalary(-500.0);

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.salary", containsString("cannot be negative")));
    }

    @Test
    @DisplayName("GET /api/jobs should return 200 OK with job list")
    void testGetAllJobs() throws Exception {
        when(jobService.getAllJobs()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title", is("Software Engineer")));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} when found should return 200 OK")
    void testGetJobById_Found() throws Exception {
        when(jobService.getJobById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} when not found should return 404 Not Found")
    void testGetJobById_NotFound() throws Exception {
        when(jobService.getJobById(999L)).thenThrow(new ResourceNotFoundException("Job not found with id: 999"));

        mockMvc.perform(get("/api/jobs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Job not found with id: 999")));
    }

    @Test
    @DisplayName("PUT /api/jobs/{id} should update and return 200 OK")
    void testUpdateJob() throws Exception {
        when(jobService.updateJob(eq(1L), any(JobRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("updated successfully")));
    }

    @Test
    @DisplayName("DELETE /api/jobs/{id} should return 200 OK")
    void testDeleteJob() throws Exception {
        doNothing().when(jobService).deleteJob(1L);

        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("deleted successfully")));
    }

    @Test
    @DisplayName("GET /api/jobs/search?keyword=Java should return 200 OK")
    void testSearchJobs() throws Exception {
        when(jobService.searchJobs("Java")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/jobs/search").param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/jobs/filter?location=Bengaluru should return 200 OK")
    void testFilterJobs() throws Exception {
        when(jobService.filterJobs(eq("Bengaluru"), any(), any(), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/jobs/filter").param("location", "Bengaluru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
