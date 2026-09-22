package com.soap.profile.dto;

import com.soap.profile.entity.ProfileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String phone;

    @NotNull(message = "Profile type is required (CANDIDATE or HR)")
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
}

