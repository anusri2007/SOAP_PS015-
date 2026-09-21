package com.soap.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soap.profile.dto.CreateProfileRequest;
import com.soap.profile.dto.UpdateProfileRequest;
import com.soap.profile.entity.Profile;
import com.soap.profile.entity.ProfileType;
import com.soap.profile.repository.ProfileRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @BeforeEach
    void cleanDatabase() {
        profileRepository.deleteAll();
    }

    private String createTestToken(Long userId, String email, String role) {
        return createTestTokenWithExpiration(userId, email, role, 3600000);
    }

    private String createTestTokenWithExpiration(Long userId, String email, String role, long expirationMs) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .claims(Map.of("userId", userId, "role", role, "name", "User " + userId))
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Test
    @DisplayName("1. Create Candidate Profile Successfully")
    void testCreateCandidateProfile() throws Exception {
        String token = createTestToken(101L, "candidate1@example.com", "CANDIDATE");

        CreateProfileRequest request = CreateProfileRequest.builder()
                .name("Alice Wonder")
                .phone("+1234567890")
                .profileType(ProfileType.CANDIDATE)
                .location("New York, NY")
                .skills(List.of("Java", "Spring Boot", "PostgreSQL", "Docker"))
                .education("B.S. Computer Science")
                .experience("3 years Software Engineer at TechCorp")
                .resumeHeadline("Full Stack Java Developer")
                .resumeUrl("https://storage.portal.com/resumes/alice.pdf")
                .build();

        mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.email").value("candidate1@example.com"))
                .andExpect(jsonPath("$.name").value("Alice Wonder"))
                .andExpect(jsonPath("$.profileType").value("CANDIDATE"))
                .andExpect(jsonPath("$.location").value("New York, NY"))
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.resumeUrl").value("https://storage.portal.com/resumes/alice.pdf"));

        Profile saved = profileRepository.findByUserId(101L).orElse(null);
        assertNotNull(saved);
        assertEquals("Alice Wonder", saved.getName());
    }

    @Test
    @DisplayName("2. Create HR Profile Successfully")
    void testCreateHrProfile() throws Exception {
        String token = createTestToken(102L, "hr@acme.com", "HR");

        CreateProfileRequest request = CreateProfileRequest.builder()
                .name("Bob Talent")
                .phone("+9876543210")
                .profileType(ProfileType.HR)
                .company("Acme Corporation")
                .designation("Senior Talent Acquisition Specialist")
                .department("Human Resources")
                .contactInfo("hr-team@acme.com / Slack: @bob")
                .build();

        mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(102))
                .andExpect(jsonPath("$.profileType").value("HR"))
                .andExpect(jsonPath("$.company").value("Acme Corporation"))
                .andExpect(jsonPath("$.designation").value("Senior Talent Acquisition Specialist"));
    }

    @Test
    @DisplayName("3. Get Own Profile via /api/profiles/me")
    void testGetOwnProfile() throws Exception {
        Profile profile = profileRepository.save(Profile.builder()
                .userId(103L)
                .email("me@example.com")
                .name("Charlie Brown")
                .profileType(ProfileType.CANDIDATE)
                .location("San Francisco")
                .build());

        String token = createTestToken(103L, "me@example.com", "CANDIDATE");

        mockMvc.perform(get("/api/profiles/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profile.getId()))
                .andExpect(jsonPath("$.userId").value(103))
                .andExpect(jsonPath("$.name").value("Charlie Brown"));
    }

    @Test
    @DisplayName("4. Get Profile by ID")
    void testGetProfileById() throws Exception {
        Profile profile = profileRepository.save(Profile.builder()
                .userId(104L)
                .email("view@example.com")
                .name("Diana Prince")
                .profileType(ProfileType.CANDIDATE)
                .build());

        // Owner can view own profile by ID
        String ownerToken = createTestToken(104L, "view@example.com", "CANDIDATE");

        mockMvc.perform(get("/api/profiles/" + profile.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profile.getId()))
                .andExpect(jsonPath("$.name").value("Diana Prince"));
    }

    @Test
    @DisplayName("5. Update Profile Successfully by Owner")
    void testUpdateProfile() throws Exception {
        Profile profile = profileRepository.save(Profile.builder()
                .userId(105L)
                .email("update@example.com")
                .name("Edward Initial")
                .profileType(ProfileType.CANDIDATE)
                .location("Boston")
                .build());

        String token = createTestToken(105L, "update@example.com", "CANDIDATE");

        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .name("Edward Updated")
                .location("Seattle")
                .skills(List.of("Java", "Kubernetes", "AWS"))
                .experience("5 years senior developer")
                .build();

        mockMvc.perform(put("/api/profiles/" + profile.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Edward Updated"))
                .andExpect(jsonPath("$.location").value("Seattle"))
                .andExpect(jsonPath("$.skills[1]").value("Kubernetes"));

        Profile updated = profileRepository.findById(profile.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("Edward Updated", updated.getName());
        assertEquals("Seattle", updated.getLocation());
    }

    @Test
    @DisplayName("6. Delete Profile Successfully by Owner")
    void testDeleteProfile() throws Exception {
        Profile profile = profileRepository.save(Profile.builder()
                .userId(106L)
                .email("delete@example.com")
                .name("Frank ToDelete")
                .profileType(ProfileType.CANDIDATE)
                .build());

        String token = createTestToken(106L, "delete@example.com", "CANDIDATE");

        mockMvc.perform(delete("/api/profiles/" + profile.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("Profile deleted successfully")));

        assertFalse(profileRepository.findById(profile.getId()).isPresent());
    }

    @Test
    @DisplayName("7. Request Without JWT Returns 401 Unauthorized")
    void testRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("8. Request With Invalid JWT Returns 401 Unauthorized")
    void testRequestWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/profiles/me")
                        .header("Authorization", "Bearer bad.malformed.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("9. Unauthorized Profile Modification Returns 403 Forbidden")
    void testUnauthorizedProfileModification() throws Exception {
        Profile profile = profileRepository.save(Profile.builder()
                .userId(107L)
                .email("user107@example.com")
                .name("User 107")
                .profileType(ProfileType.CANDIDATE)
                .build());

        // Another candidate tries to modify user 107's profile
        String attackerToken = createTestToken(108L, "user108@example.com", "CANDIDATE");

        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .name("Hacked Name")
                .build();

        mockMvc.perform(put("/api/profiles/" + profile.getId())
                        .header("Authorization", "Bearer " + attackerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("10. Authorized Profile Access - HR can view candidate profile")
    void testHrCanViewCandidateProfile() throws Exception {
        Profile candidateProfile = profileRepository.save(Profile.builder()
                .userId(109L)
                .email("cand109@example.com")
                .name("Candidate 109")
                .profileType(ProfileType.CANDIDATE)
                .location("Chicago")
                .build());

        String hrToken = createTestToken(110L, "hr110@company.com", "HR");

        mockMvc.perform(get("/api/profiles/" + candidateProfile.getId())
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(candidateProfile.getId()))
                .andExpect(jsonPath("$.name").value("Candidate 109"));
    }

    @Test
    @DisplayName("11. Validation Errors on Create Profile Returns 400 Bad Request")
    void testValidationErrors() throws Exception {
        String token = createTestToken(111L, "user111@example.com", "CANDIDATE");

        CreateProfileRequest invalid = CreateProfileRequest.builder()
                .name("") // Blank name
                .profileType(null) // Missing type
                .build();

        mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.profileType").exists());
    }

    @Test
    @DisplayName("12. Duplicate Profile Creation Rejection Returns 409 Conflict")
    void testDuplicateProfileCreationRejection() throws Exception {
        String token = createTestToken(112L, "user112@example.com", "CANDIDATE");

        CreateProfileRequest request = CreateProfileRequest.builder()
                .name("Initial Name")
                .profileType(ProfileType.CANDIDATE)
                .build();

        // First profile created
        mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second profile creation for same user returns 409 Conflict
        mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}

