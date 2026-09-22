package com.soap.profile.controller;

import com.soap.profile.dto.CreateProfileRequest;
import com.soap.profile.dto.ProfileResponse;
import com.soap.profile.dto.UpdateProfileRequest;
import com.soap.profile.security.UserPrincipal;
import com.soap.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @Valid @RequestBody CreateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProfileResponse response = profileService.createProfile(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getOwnProfile(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProfileResponse response = profileService.getOwnProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ProfileResponse> getProfileByCandidateId(
            @PathVariable Long candidateId
    ) {
        ProfileResponse response = profileService.getProfileByCandidateId(candidateId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfileById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProfileResponse response = profileService.getProfileById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProfileResponse response = profileService.updateProfile(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        profileService.deleteProfile(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Profile deleted successfully", "profileId", String.valueOf(id)));
    }
}

