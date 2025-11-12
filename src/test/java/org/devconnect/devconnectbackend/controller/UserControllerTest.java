package org.devconnect.devconnectbackend.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.devconnect.devconnectbackend.dto.UserDTO;
import org.devconnect.devconnectbackend.dto.UserRegistrationDTO;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Unit tests for UserController
 * Tests authentication logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Unit Tests")
public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private BCryptPasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        // Create a test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(User.UserRole.DEVELOPER);
        testUser.setStatus(User.UserStatus.OFFLINE);
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterUser_Success() {
        // Arrange
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("new@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setRole("developer");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        ResponseEntity<UserDTO> response = userController.registerUser(registrationDTO);

        // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    UserDTO body = Objects.requireNonNull(response.getBody());
    assertEquals("newuser", body.getUsername());
    assertEquals("new@example.com", body.getEmail());
    assertEquals("developer", body.getRole());
    assertNull(body.getPassword()); // Password should not be returned
        
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void testRegisterUser_EmailExists() {
        // Arrange
    UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
    registrationDTO.setUsername("newuser");
    registrationDTO.setEmail("existing@example.com");
    registrationDTO.setPassword("password123");
    registrationDTO.setRole("developer");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act
    ResponseEntity<UserDTO> response = userController.registerUser(registrationDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when username already exists")
    void testRegisterUser_UsernameExists() {
        // Arrange
    UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
    registrationDTO.setUsername("existinguser");
    registrationDTO.setEmail("new@example.com");
    registrationDTO.setPassword("password123");
    registrationDTO.setRole("developer");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act
    ResponseEntity<UserDTO> response = userController.registerUser(registrationDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle invalid role during registration")
    void testRegisterUser_InvalidRole() {
        // Arrange
    UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
    registrationDTO.setUsername("newuser");
    registrationDTO.setEmail("new@example.com");
    registrationDTO.setPassword("password123");
    registrationDTO.setRole("INVALID_ROLE");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // Act
    ResponseEntity<UserDTO> response = userController.registerUser(registrationDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should successfully login with correct credentials")
    void testLogin_Success() {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@example.com");
        credentials.put("password", "password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<UserDTO> response = userController.login(credentials);

        // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    UserDTO body = Objects.requireNonNull(response.getBody());
    assertEquals("testuser", body.getUsername());
    assertEquals("test@example.com", body.getEmail());
    assertNull(body.getPassword()); // Password should not be returned
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void testLogin_WrongPassword() {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "test@example.com");
        credentials.put("password", "wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<UserDTO> response = userController.login(credentials);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should fail login with non-existent email")
    void testLogin_UserNotFound() {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "nonexistent@example.com");
        credentials.put("password", "password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDTO> response = userController.login(credentials);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    // ==================== GET USER TESTS ====================

    @Test
    @DisplayName("Should get user by ID")
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    UserDTO body = Objects.requireNonNull(response.getBody());
    assertEquals(1L, body.getId());
    assertEquals("testuser", body.getUsername());
        
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDTO> response = userController.getUserById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setRole(User.UserRole.CLIENT);
        user2.setStatus(User.UserStatus.ONLINE);

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    List<UserDTO> body = Objects.requireNonNull(response.getBody());
    assertEquals(2, body.size());
        
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should get users by role")
    void testGetUsersByRole() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));

        // Act
        ResponseEntity<List<UserDTO>> response = userController.getUsersByRole("developer");

        // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    List<UserDTO> body = Objects.requireNonNull(response.getBody());
    assertEquals(1, body.size());
    assertEquals("developer", body.get(0).getRole());
        
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should handle invalid role in getUsersByRole")
    void testGetUsersByRole_InvalidRole() {
        // Act
        ResponseEntity<List<UserDTO>> response = userController.getUsersByRole("invalid_role");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
