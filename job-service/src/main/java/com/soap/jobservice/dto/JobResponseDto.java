package com.soap.jobservice.dto;

import com.soap.jobservice.entity.EmploymentType;
import com.soap.jobservice.entity.Job;
import com.soap.jobservice.entity.JobStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JobResponseDto {

    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private EmploymentType employmentType;
    private String experienceRequired;
    private Double salary;
    private String skills;
    private LocalDate postedDate;
    private LocalDate closingDate;
    private JobStatus status;
    private Long hrId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobResponseDto() {
    }

    public JobResponseDto(Long id, String title, String description, String company, String location,
                          EmploymentType employmentType, String experienceRequired, Double salary,
                          String skills, LocalDate postedDate, LocalDate closingDate, JobStatus status,
                          Long hrId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.company = company;
        this.location = location;
        this.employmentType = employmentType;
        this.experienceRequired = experienceRequired;
        this.salary = salary;
        this.skills = skills;
        this.postedDate = postedDate;
        this.closingDate = closingDate;
        this.status = status;
        this.hrId = hrId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static JobResponseDto fromEntity(Job job) {
        if (job == null) {
            return null;
        }
        return JobResponseDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .experienceRequired(job.getExperienceRequired())
                .salary(job.getSalary())
                .skills(job.getSkills())
                .postedDate(job.getPostedDate())
                .closingDate(job.getClosingDate())
                .status(job.getStatus())
                .hrId(job.getHrId())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public String getExperienceRequired() {
        return experienceRequired;
    }

    public void setExperienceRequired(String experienceRequired) {
        this.experienceRequired = experienceRequired;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public LocalDate getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(LocalDate postedDate) {
        this.postedDate = postedDate;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Long getHrId() {
        return hrId;
    }

    public void setHrId(Long hrId) {
        this.hrId = hrId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static JobResponseDtoBuilder builder() {
        return new JobResponseDtoBuilder();
    }

    public static class JobResponseDtoBuilder {
        private Long id;
        private String title;
        private String description;
        private String company;
        private String location;
        private EmploymentType employmentType;
        private String experienceRequired;
        private Double salary;
        private String skills;
        private LocalDate postedDate;
        private LocalDate closingDate;
        private JobStatus status;
        private Long hrId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public JobResponseDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public JobResponseDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public JobResponseDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public JobResponseDtoBuilder company(String company) {
            this.company = company;
            return this;
        }

        public JobResponseDtoBuilder location(String location) {
            this.location = location;
            return this;
        }

        public JobResponseDtoBuilder employmentType(EmploymentType employmentType) {
            this.employmentType = employmentType;
            return this;
        }

        public JobResponseDtoBuilder experienceRequired(String experienceRequired) {
            this.experienceRequired = experienceRequired;
            return this;
        }

        public JobResponseDtoBuilder salary(Double salary) {
            this.salary = salary;
            return this;
        }

        public JobResponseDtoBuilder skills(String skills) {
            this.skills = skills;
            return this;
        }

        public JobResponseDtoBuilder postedDate(LocalDate postedDate) {
            this.postedDate = postedDate;
            return this;
        }

        public JobResponseDtoBuilder closingDate(LocalDate closingDate) {
            this.closingDate = closingDate;
            return this;
        }

        public JobResponseDtoBuilder status(JobStatus status) {
            this.status = status;
            return this;
        }

        public JobResponseDtoBuilder hrId(Long hrId) {
            this.hrId = hrId;
            return this;
        }

        public JobResponseDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public JobResponseDtoBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public JobResponseDto build() {
            return new JobResponseDto(id, title, description, company, location,
                    employmentType, experienceRequired, salary, skills,
                    postedDate, closingDate, status, hrId, createdAt, updatedAt);
        }
    }
}
