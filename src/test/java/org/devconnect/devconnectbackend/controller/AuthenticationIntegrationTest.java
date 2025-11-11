package org.devconnect.devconnectbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devconnect.devconnectbackend.dto.UserDTO;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Authentication endpoints
 * Tests the complete authentication flow including registration and login
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authentication Integration Tests")
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Clean up any existing test users
        userRepository.deleteAll();
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should successfully register a new developer user")
    void testRegisterNewDeveloper() throws Exception {
        // Arrange
        UserDTO newUser = new UserDTO();
        newUser.setUsername("john_dev");
        newUser.setEmail("john@developer.com");
        newUser.setPassword("SecurePass123!");
        newUser.setRole("developer");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("john_dev"))
                .andExpect(jsonPath("$.email").value("john@developer.com"))
                .andExpect(jsonPath("$.role").value("developer"))
                .andExpect(jsonPath("$.status").value("offline"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be returned
    }

    @Test
    @DisplayName("Should successfully register a new client user")
    void testRegisterNewClient() throws Exception {
        // Arrange
        UserDTO newUser = new UserDTO();
        newUser.setUsername("jane_client");
        newUser.setEmail("jane@client.com");
        newUser.setPassword("ClientPass456!");
        newUser.setRole("client");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane_client"))
                .andExpect(jsonPath("$.email").value("jane@client.com"))
                .andExpect(jsonPath("$.role").value("client"))
                .andExpect(jsonPath("$.status").value("offline"));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        // Arrange - create existing user
        User existingUser = new User();
        existingUser.setUsername("existing_user");
        existingUser.setEmail("duplicate@test.com");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setRole(User.UserRole.DEVELOPER);
        userRepository.save(existingUser);

        // Try to register with same email
        UserDTO newUser = new UserDTO();
        newUser.setUsername("different_username");
        newUser.setEmail("duplicate@test.com");
        newUser.setPassword("Password123!");
        newUser.setRole("developer");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with duplicate username")
    void testRegisterDuplicateUsername() throws Exception {
        // Arrange - create existing user
        User existingUser = new User();
        existingUser.setUsername("duplicate_username");
        existingUser.setEmail("first@test.com");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setRole(User.UserRole.DEVELOPER);
        userRepository.save(existingUser);

        // Try to register with same username
        UserDTO newUser = new UserDTO();
        newUser.setUsername("duplicate_username");
        newUser.setEmail("second@test.com");
        newUser.setPassword("Password123!");
        newUser.setRole("developer");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with invalid role")
    void testRegisterInvalidRole() throws Exception {
        // Arrange
        UserDTO newUser = new UserDTO();
        newUser.setUsername("test_user");
        newUser.setEmail("test@test.com");
        newUser.setPassword("Password123!");
        newUser.setRole("INVALID_ROLE");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should hash password during registration")
    void testPasswordIsHashed() throws Exception {
        // Arrange
        UserDTO newUser = new UserDTO();
        newUser.setUsername("test_hash");
        newUser.setEmail("hash@test.com");
        newUser.setPassword("PlainTextPassword");
        newUser.setRole("developer");

        // Act
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // Assert - verify password is hashed in database
        User savedUser = userRepository.findByEmail("hash@test.com").orElseThrow();
        assert !savedUser.getPassword().equals("PlainTextPassword");
        assert passwordEncoder.matches("PlainTextPassword", savedUser.getPassword());
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should successfully login with correct credentials")
    void testLoginSuccess() throws Exception {
        // Arrange - create user
        User user = new User();
        user.setUsername("login_user");
        user.setEmail("login@test.com");
        user.setPassword(passwordEncoder.encode("CorrectPassword"));
        user.setRole(User.UserRole.DEVELOPER);
        userRepository.save(user);

        // Login credentials
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "login@test.com");
        credentials.put("password", "CorrectPassword");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("login_user"))
                .andExpect(jsonPath("$.email").value("login@test.com"))
                .andExpect(jsonPath("$.role").value("developer"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be returned
    }

    @Test
    @DisplayName("Should reject login with wrong password")
    void testLoginWrongPassword() throws Exception {
        // Arrange - create user
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("test@test.com");
        user.setPassword(passwordEncoder.encode("CorrectPassword"));
        user.setRole(User.UserRole.DEVELOPER);
        userRepository.save(user);

        // Wrong credentials
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@test.com");
        credentials.put("password", "WrongPassword");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login with non-existent email")
    void testLoginNonExistentUser() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "nonexistent@test.com");
        credentials.put("password", "SomePassword");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login with empty email")
    void testLoginEmptyEmail() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "");
        credentials.put("password", "SomePassword");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login with empty password")
    void testLoginEmptyPassword() throws Exception {
        // Arrange - create user
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("test@test.com");
        user.setPassword(passwordEncoder.encode("CorrectPassword"));
        user.setRole(User.UserRole.DEVELOPER);
        userRepository.save(user);

        // Empty password
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@test.com");
        credentials.put("password", "");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== COMPLETE FLOW TESTS ====================

    @Test
    @DisplayName("Should complete full registration and login flow")
    void testCompleteRegistrationAndLoginFlow() throws Exception {
        // Step 1: Register a new user
        UserDTO newUser = new UserDTO();
        newUser.setUsername("flow_test_user");
        newUser.setEmail("flowtest@example.com");
        newUser.setPassword("TestPassword123!");
        newUser.setRole("developer");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("flow_test_user"));

        // Step 2: Login with the registered credentials
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "flowtest@example.com");
        credentials.put("password", "TestPassword123!");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("flow_test_user"))
                .andExpect(jsonPath("$.email").value("flowtest@example.com"));
    }

    @Test
    @DisplayName("Should prevent double registration and allow login")
    void testDoubleRegistrationPrevention() throws Exception {
        // Step 1: Register first time
        UserDTO newUser = new UserDTO();
        newUser.setUsername("double_test");
        newUser.setEmail("double@test.com");
        newUser.setPassword("Password123!");
        newUser.setRole("client");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // Step 2: Try to register again (should fail)
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());

        // Step 3: Verify can still login with original credentials
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "double@test.com");
        credentials.put("password", "Password123!");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("double_test"));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle case-sensitive email in login")
    void testLoginCaseSensitiveEmail() throws Exception {
        // Arrange - register with lowercase email
        User user = new User();
        user.setUsername("case_test");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRole(User.UserRole.DEVELOPER);
        userRepository.save(user);

        // Try to login with uppercase email (should fail if case-sensitive)
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "TEST@EXAMPLE.COM");
        credentials.put("password", "Password123!");

        // Act & Assert - this will depend on your database collation
        // Most databases treat email as case-insensitive, but it's good to test
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should register users with different roles")
    void testRegisterMultipleRoles() throws Exception {
        // Register developer
        UserDTO developer = new UserDTO();
        developer.setUsername("dev_user");
        developer.setEmail("dev@test.com");
        developer.setPassword("Password123!");
        developer.setRole("developer");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(developer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("developer"));

        // Register client
        UserDTO client = new UserDTO();
        client.setUsername("client_user");
        client.setEmail("client@test.com");
        client.setPassword("Password123!");
        client.setRole("client");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("client"));
    }
}
