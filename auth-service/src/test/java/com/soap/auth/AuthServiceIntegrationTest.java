package com.soap.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soap.auth.dto.LoginRequest;
import com.soap.auth.dto.RegistrationRequest;
import com.soap.auth.entity.Role;
import com.soap.auth.entity.User;
import com.soap.auth.repository.UserRepository;
import com.soap.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Register Candidate Successfully")
    void testRegisterCandidate() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .name("Alice Candidate")
                .email("alice@example.com")
                .password("Password123")
                .role(Role.CANDIDATE)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Candidate"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User user = userRepository.findByEmail("alice@example.com").orElse(null);
        assertNotNull(user);
        assertTrue(passwordEncoder.matches("Password123", user.getPassword()));
    }

    @Test
    @DisplayName("2. Register HR Successfully")
    void testRegisterHr() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .name("Bob HR")
                .email("bob@company.com")
                .password("SecurePass456")
                .role(Role.HR)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("HR"));
    }

    @Test
    @DisplayName("3. Register Admin Successfully")
    void testRegisterAdmin() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .name("Charlie Admin")
                .email("admin@portal.com")
                .password("AdminSecret789")
                .role(Role.ADMIN)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("4. Reject Duplicate Registration")
    void testDuplicateRegistration() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("Password123")
                .role(Role.CANDIDATE)
                .build();

        // First registration succeeds
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate registration fails with 409 Conflict
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(containsString("alice@example.com")));
    }

    @Test
    @DisplayName("5. Login Success and Returns JWT")
    void testLoginSuccess() throws Exception {
        // Create user
        User user = User.builder()
                .name("Dave Candidate")
                .email("dave@example.com")
                .password(passwordEncoder.encode("SecretPassword"))
                .role(Role.CANDIDATE)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("dave@example.com")
                .password("SecretPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("dave@example.com"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"));
    }

    @Test
    @DisplayName("6. Login with Invalid Credentials Returns 401")
    void testInvalidLogin() throws Exception {
        User user = User.builder()
                .name("Dave")
                .email("dave@example.com")
                .password(passwordEncoder.encode("SecretPassword"))
                .role(Role.CANDIDATE)
                .build();
        userRepository.save(user);

        // Wrong password
        LoginRequest wrongPass = LoginRequest.builder()
                .email("dave@example.com")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPass)))
                .andExpect(status().isUnauthorized());

        // Unknown email
        LoginRequest unknownEmail = LoginRequest.builder()
                .email("unknown@example.com")
                .password("SecretPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownEmail)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. JWT Generation and Claims Verification")
    void testJwtGenerationAndClaims() {
        User user = User.builder()
                .id(99L)
                .name("Test User")
                .email("test@claims.com")
                .role(Role.HR)
                .build();

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertEquals("test@claims.com", jwtService.extractUsername(token));
        assertEquals(99L, jwtService.extractUserId(token));
        assertEquals("HR", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("8. Protected Endpoint Without JWT Returns 401")
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/candidate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("9. Protected Endpoint With Valid JWT Returns 200")
    void testProtectedEndpointWithValidToken() throws Exception {
        User user = User.builder()
                .name("Alice")
                .email("alice@candidate.com")
                .password(passwordEncoder.encode("Pass123"))
                .role(Role.CANDIDATE)
                .build();
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved);

        mockMvc.perform(get("/api/auth/candidate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("Welcome Candidate")));
    }

    @Test
    @DisplayName("10. Invalid JWT Token Returns 401")
    void testInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/candidate")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("11. Expired JWT Token Returns 401")
    void testExpiredToken() throws Exception {
        User user = User.builder()
                .name("Expired User")
                .email("expired@user.com")
                .password(passwordEncoder.encode("Pass123"))
                .role(Role.CANDIDATE)
                .build();
        User saved = userRepository.save(user);

        // Generate expired token (-1000ms expiration)
        String expiredToken = jwtService.generateTokenWithCustomExpiration(
                Map.of("userId", saved.getId(), "role", saved.getRole().name()),
                saved.getEmail(),
                -1000L
        );

        mockMvc.perform(get("/api/auth/candidate")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("12. Candidate Authorization - Candidate Allowed, HR Forbidden")
    void testCandidateAuthorization() throws Exception {
        User candidate = userRepository.save(User.builder()
                .name("Candidate User")
                .email("cand@test.com")
                .password(passwordEncoder.encode("Pass123"))
                .role(Role.CANDIDATE)
                .build());

        User hr = userRepository.save(User.builder()
                .name("HR User")
                .email("hr@test.com")
                .password(passwordEncoder.encode("Pass123"))
                .role(Role.HR)
                .build());

        String candToken = jwtService.generateToken(candidate);
        String hrToken = jwtService.generateToken(hr);

        // Candidate accesses candidate endpoint -> 200
        mockMvc.perform(get("/api/auth/candidate")
                        .header("Authorization", "Bearer " + candToken))
                .andExpect(status().isOk());

        // HR accesses candidate endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/auth/candidate")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("13. HR Authorization - HR Allowed, Candidate Forbidden")
    void testHrAuthorization() throws Exception {
        User candidate = userRepository.save(User.builder()
                .name("Candidate User")
                .email("cand2@test.com")
                .password(passwordEncoder.encode("Pass123"))
                .role(Role.CANDIDATE)
                .build());

        User hr = userRepository.save(User.builder()
                .name("HR User")
                .email("hr2@test.com")
                .password(passwordEncoder.encode("Pass123"))
                .role(Role.HR)
                .build());

        String candToken = jwtService.generateToken(candidate);
        String hrToken = jwtService.generateToken(hr);

        // HR accesses hr endpoint -> 200
        mockMvc.perform(get("/api/auth/hr")
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());

        // Candidate accesses hr endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/auth/hr")
                        .header("Authorization", "Bearer " + candToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("14. Admin Authorization - Admin Allowed, Candidate & HR Forbidden")
    void testAdminAuthorization() throws Exception {
        User admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .password(passwordEncoder.encode("Pass123"))
                .role(Role.ADMIN)
                .build());

        User candidate = userRepository.save(User.builder()
                .name("Candidate")
                .email("cand3@test.com")
                .password(passwordEncoder.encode("Pass123"))
                .role(Role.CANDIDATE)
                .build());

        String adminToken = jwtService.generateToken(admin);
        String candToken = jwtService.generateToken(candidate);

        // Admin accesses admin endpoint -> 200
        mockMvc.perform(get("/api/auth/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Candidate accesses admin endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/auth/admin")
                        .header("Authorization", "Bearer " + candToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. Validation Errors - Return 400 Bad Request")
    void testValidationErrors() throws Exception {
        RegistrationRequest invalid = RegistrationRequest.builder()
                .name("") // blank
                .email("not-an-email")
                .password("123") // too short
                .role(null)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists())
                .andExpect(jsonPath("$.validationErrors.role").exists());
    }
}

