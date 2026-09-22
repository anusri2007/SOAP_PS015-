package com.soap.jobservice.dto;

import com.soap.jobservice.entity.EmploymentType;
import com.soap.jobservice.entity.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class JobRequestDto {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 200, message = "Job title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    private String company;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @NotNull(message = "Employment type is required (FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, REMOTE)")
    private EmploymentType employmentType;

    @NotBlank(message = "Experience required is required")
    private String experienceRequired;

    @NotNull(message = "Salary is required")
    @PositiveOrZero(message = "Salary cannot be negative")
    private Double salary;

    @NotBlank(message = "Skills are required")
    private String skills;

    private LocalDate closingDate;

    @NotNull(message = "HR ID is required")
    private Long hrId;

    private JobStatus status;

    public JobRequestDto() {
    }

    public JobRequestDto(String title, String description, String company, String location,
                         EmploymentType employmentType, String experienceRequired, Double salary,
                         String skills, LocalDate closingDate, Long hrId, JobStatus status) {
        this.title = title;
        this.description = description;
        this.company = company;
        this.location = location;
        this.employmentType = employmentType;
        this.experienceRequired = experienceRequired;
        this.salary = salary;
        this.skills = skills;
        this.closingDate = closingDate;
        this.hrId = hrId;
        this.status = status;
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

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public Long getHrId() {
        return hrId;
    }

    public void setHrId(Long hrId) {
        this.hrId = hrId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public static JobRequestDtoBuilder builder() {
        return new JobRequestDtoBuilder();
    }

    public static class JobRequestDtoBuilder {
        private String title;
        private String description;
        private String company;
        private String location;
        private EmploymentType employmentType;
        private String experienceRequired;
        private Double salary;
        private String skills;
        private LocalDate closingDate;
        private Long hrId;
        private JobStatus status;

        public JobRequestDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public JobRequestDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public JobRequestDtoBuilder company(String company) {
            this.company = company;
            return this;
        }

        public JobRequestDtoBuilder location(String location) {
            this.location = location;
            return this;
        }

        public JobRequestDtoBuilder employmentType(EmploymentType employmentType) {
            this.employmentType = employmentType;
            return this;
        }

        public JobRequestDtoBuilder experienceRequired(String experienceRequired) {
            this.experienceRequired = experienceRequired;
            return this;
        }

        public JobRequestDtoBuilder salary(Double salary) {
            this.salary = salary;
            return this;
        }

        public JobRequestDtoBuilder skills(String skills) {
            this.skills = skills;
            return this;
        }

        public JobRequestDtoBuilder closingDate(LocalDate closingDate) {
            this.closingDate = closingDate;
            return this;
        }

        public JobRequestDtoBuilder hrId(Long hrId) {
            this.hrId = hrId;
            return this;
        }

        public JobRequestDtoBuilder status(JobStatus status) {
            this.status = status;
            return this;
        }

        public JobRequestDto build() {
            return new JobRequestDto(title, description, company, location, employmentType,
                    experienceRequired, salary, skills, closingDate, hrId, status);
        }
    }
}
