package com.talentacquisition.applicationservice.dto;

import com.talentacquisition.applicationservice.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Application status is required")
    private ApplicationStatus status;

    private String remarks;
}

