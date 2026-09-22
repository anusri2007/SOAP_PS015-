package com.soap.profile.service;

import com.soap.profile.dto.CreateProfileRequest;
import com.soap.profile.dto.ProfileResponse;
import com.soap.profile.dto.UpdateProfileRequest;
import com.soap.profile.entity.Profile;
import com.soap.profile.exception.DuplicateProfileException;
import com.soap.profile.exception.ResourceNotFoundException;
import com.soap.profile.exception.UnauthorizedAccessException;
import com.soap.profile.repository.ProfileRepository;
import com.soap.profile.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request, UserPrincipal currentUser) {
        Long currentUserId = currentUser.getUserId();

        if (profileRepository.existsByUserId(currentUserId)) {
            log.warn("Create profile failed: Profile already exists for user ID: {}", currentUserId);
            throw new DuplicateProfileException("Profile already exists for user ID: " + currentUserId);
        }

        Profile profile = Profile.builder()
                .userId(currentUserId)
                .email(currentUser.getEmail())
                .name(request.getName().trim())
                .phone(request.getPhone())
                .profileType(request.getProfileType())
                .location(request.getLocation())
                .skills(request.getSkills() != null ? new ArrayList<>(request.getSkills()) : new ArrayList<>())
                .education(request.getEducation())
                .experience(request.getExperience())
                .resumeHeadline(request.getResumeHeadline())
                .resumeUrl(request.getResumeUrl())
                .company(request.getCompany())
                .designation(request.getDesignation())
                .department(request.getDepartment())
                .contactInfo(request.getContactInfo())
                .build();

        Profile savedProfile = profileRepository.save(profile);
        log.info("Profile created successfully for user ID: {}, profile ID: {}", currentUserId, savedProfile.getId());
        return ProfileResponse.fromEntity(savedProfile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getOwnProfile(UserPrincipal currentUser) {
        Profile profile = profileRepository.findByUserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for current user (ID: " + currentUser.getUserId() + ")"));
        return ProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByCandidateId(Long candidateId) {
        Profile profile = profileRepository.findByUserId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for candidate ID: " + candidateId));
        return ProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileById(Long id, UserPrincipal currentUser) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + id));

        boolean isOwner = profile.getUserId().equals(currentUser.getUserId());
        boolean isHrOrAdmin = "HR".equalsIgnoreCase(currentUser.getRole()) || "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (!isOwner && !isHrOrAdmin) {
            log.warn("Access denied: User {} (Role {}) attempted to view profile of user {}",
                    currentUser.getUserId(), currentUser.getRole(), profile.getUserId());
            throw new UnauthorizedAccessException("Access denied: You can only view your own profile");
        }

        return ProfileResponse.fromEntity(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(Long id, UpdateProfileRequest request, UserPrincipal currentUser) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + id));

        boolean isOwner = profile.getUserId().equals(currentUser.getUserId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            log.warn("Access denied: User {} attempted to modify profile owned by {}",
                    currentUser.getUserId(), profile.getUserId());
            throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this profile");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            profile.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getSkills() != null) {
            profile.setSkills(new ArrayList<>(request.getSkills()));
        }
        if (request.getEducation() != null) {
            profile.setEducation(request.getEducation());
        }
        if (request.getExperience() != null) {
            profile.setExperience(request.getExperience());
        }
        if (request.getResumeHeadline() != null) {
            profile.setResumeHeadline(request.getResumeHeadline());
        }
        if (request.getResumeUrl() != null) {
            profile.setResumeUrl(request.getResumeUrl());
        }
        if (request.getCompany() != null) {
            profile.setCompany(request.getCompany());
        }
        if (request.getDesignation() != null) {
            profile.setDesignation(request.getDesignation());
        }
        if (request.getDepartment() != null) {
            profile.setDepartment(request.getDepartment());
        }
        if (request.getContactInfo() != null) {
            profile.setContactInfo(request.getContactInfo());
        }

        Profile updated = profileRepository.save(profile);
        log.info("Profile ID {} updated successfully by user {}", updated.getId(), currentUser.getUserId());
        return ProfileResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteProfile(Long id, UserPrincipal currentUser) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + id));

        boolean isOwner = profile.getUserId().equals(currentUser.getUserId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            log.warn("Access denied: User {} attempted to delete profile owned by {}",
                    currentUser.getUserId(), profile.getUserId());
            throw new UnauthorizedAccessException("Access denied: You do not have permission to delete this profile");
        }

        profileRepository.delete(profile);
        log.info("Profile ID {} deleted successfully by user {}", id, currentUser.getUserId());
    }
}

