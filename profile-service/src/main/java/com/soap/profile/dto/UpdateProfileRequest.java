package com.soap.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String name;
    private String phone;

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

