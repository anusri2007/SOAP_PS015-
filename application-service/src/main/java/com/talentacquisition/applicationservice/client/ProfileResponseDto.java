package com.talentacquisition.applicationservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDto {

    private Long id;
    private Long candidateId;
    private String fullName;
    private String email;
    private String phone;
    private String resumeUrl;
}

