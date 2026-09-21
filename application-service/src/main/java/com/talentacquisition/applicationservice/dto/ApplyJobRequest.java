package com.talentacquisition.applicationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyJobRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    private String coverLetter;
}

