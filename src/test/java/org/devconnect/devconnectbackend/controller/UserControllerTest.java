package org.devconnect.devconnectbackend.controller;

import org.devconnect.devconnectbackend.dto.UserDTO;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void setUp() {
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
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setEmail("new@example.com");
        userDTO.setPassword("password123");
        userDTO.setRole("developer");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        ResponseEntity<UserDTO> response = userController.registerUser(userDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        assertEquals("new@example.com", response.getBody().getEmail());
        assertEquals("developer", response.getBody().getRole());
        assertNull(response.getBody().getPassword()); // Password should not be returned
        
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void testRegisterUser_EmailExists() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setEmail("existing@example.com");
        userDTO.setPassword("password123");
        userDTO.setRole("developer");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act
        ResponseEntity<UserDTO> response = userController.registerUser(userDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when username already exists")
    void testRegisterUser_UsernameExists() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("existinguser");
        userDTO.setEmail("new@example.com");
        userDTO.setPassword("password123");
        userDTO.setRole("developer");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act
        ResponseEntity<UserDTO> response = userController.registerUser(userDTO);

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
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setEmail("new@example.com");
        userDTO.setPassword("password123");
        userDTO.setRole("INVALID_ROLE");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // Act
        ResponseEntity<UserDTO> response = userController.registerUser(userDTO);

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
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertNull(response.getBody().getPassword()); // Password should not be returned
        
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
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("testuser", response.getBody().getUsername());
        
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
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
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
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("developer", response.getBody().get(0).getRole());
        
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
