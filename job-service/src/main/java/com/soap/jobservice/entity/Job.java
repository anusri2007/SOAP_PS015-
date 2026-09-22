package com.soap.jobservice.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 150)
    private String company;

    @Column(nullable = false, length = 150)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EmploymentType employmentType;

    @Column(nullable = false, length = 100)
    private String experienceRequired;

    @Column(nullable = false)
    private Double salary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String skills;

    @Column(nullable = false)
    private LocalDate postedDate;

    private LocalDate closingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(nullable = false)
    private Long hrId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Job() {
    }

    public Job(Long id, String title, String description, String company, String location,
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

    @PrePersist
    protected void onCreate() {
        if (postedDate == null) {
            postedDate = LocalDate.now();
        }
        if (status == null) {
            status = JobStatus.OPEN;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    // Builder Pattern
    public static JobBuilder builder() {
        return new JobBuilder();
    }

    public static class JobBuilder {
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

        JobBuilder() {
        }

        public JobBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public JobBuilder title(String title) {
            this.title = title;
            return this;
        }

        public JobBuilder description(String description) {
            this.description = description;
            return this;
        }

        public JobBuilder company(String company) {
            this.company = company;
            return this;
        }

        public JobBuilder location(String location) {
            this.location = location;
            return this;
        }

        public JobBuilder employmentType(EmploymentType employmentType) {
            this.employmentType = employmentType;
            return this;
        }

        public JobBuilder experienceRequired(String experienceRequired) {
            this.experienceRequired = experienceRequired;
            return this;
        }

        public JobBuilder salary(Double salary) {
            this.salary = salary;
            return this;
        }

        public JobBuilder skills(String skills) {
            this.skills = skills;
            return this;
        }

        public JobBuilder postedDate(LocalDate postedDate) {
            this.postedDate = postedDate;
            return this;
        }

        public JobBuilder closingDate(LocalDate closingDate) {
            this.closingDate = closingDate;
            return this;
        }

        public JobBuilder status(JobStatus status) {
            this.status = status;
            return this;
        }

        public JobBuilder hrId(Long hrId) {
            this.hrId = hrId;
            return this;
        }

        public JobBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public JobBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Job build() {
            return new Job(id, title, description, company, location, employmentType,
                    experienceRequired, salary, skills, postedDate, closingDate, status,
                    hrId, createdAt, updatedAt);
        }
    }
}
