package com.talentacquisition.applicationservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDto {

    private Long id;
    private String title;
    private String description;
    private String status; // OPEN, CLOSED, INACTIVE, etc.
    private Long hrId;
    private String company;
    private String location;
}

