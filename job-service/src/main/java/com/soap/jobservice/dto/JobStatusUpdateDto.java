package com.soap.jobservice.dto;

import com.soap.jobservice.entity.JobStatus;
import jakarta.validation.constraints.NotNull;

public class JobStatusUpdateDto {

    @NotNull(message = "Job status is required (OPEN, CLOSED)")
    private JobStatus status;

    public JobStatusUpdateDto() {
    }

    public JobStatusUpdateDto(JobStatus status) {
        this.status = status;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
