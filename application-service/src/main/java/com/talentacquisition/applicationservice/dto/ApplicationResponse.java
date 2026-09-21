package com.talentacquisition.applicationservice.dto;

import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;
    private Long candidateId;
    private Long jobId;
    private LocalDateTime applicationDate;
    private ApplicationStatus status;
    private String jobTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

