package com.soap.auth.controller;

import com.soap.auth.dto.AuthResponse;
import com.soap.auth.dto.LoginRequest;
import com.soap.auth.dto.RegistrationRequest;
import com.soap.auth.dto.RegistrationResponse;
import com.soap.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Map<String, Object>> candidateEndpoint(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome Candidate! Authorized access granted.",
                "user", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities()
        ));
    }

    @GetMapping("/hr")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Map<String, Object>> hrEndpoint(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome HR! Authorized access granted.",
                "user", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities()
        ));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminEndpoint(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome Admin! Authorized access granted.",
                "user", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities()
        ));
    }
}

