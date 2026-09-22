package com.soap.auth.dto;

import com.soap.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private String message;
}

