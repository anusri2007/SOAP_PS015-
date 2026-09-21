package com.soap.profile.dto;

import com.soap.profile.entity.Profile;
import com.soap.profile.entity.ProfileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private ProfileType profileType;

    // Candidate fields
    private String location;
    private List<String> skills;
    private String education;
    private String experience;
    private String resumeHeadline;
    private String resumeUrl;

    // HR fields
    private String company;
    private String designation;
    private String department;
    private String contactInfo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProfileResponse fromEntity(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .name(profile.getName())
                .phone(profile.getPhone())
                .profileType(profile.getProfileType())
                .location(profile.getLocation())
                .skills(profile.getSkills())
                .education(profile.getEducation())
                .experience(profile.getExperience())
                .resumeHeadline(profile.getResumeHeadline())
                .resumeUrl(profile.getResumeUrl())
                .company(profile.getCompany())
                .designation(profile.getDesignation())
                .department(profile.getDepartment())
                .contactInfo(profile.getContactInfo())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}

