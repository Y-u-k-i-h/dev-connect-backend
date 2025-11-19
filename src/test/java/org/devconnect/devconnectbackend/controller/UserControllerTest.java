package org.devconnect.devconnectbackend.controller;

import org.devconnect.devconnectbackend.dto.*;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Controller Unit Tests")
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponseDTO testUserResponse;
    private LoginResponseDTO testLoginResponse;

    @BeforeEach
    public void setUp() {
        testUserResponse = new UserResponseDTO();
        testUserResponse.setUserId(1);
        testUserResponse.setFirstName("Test");
        testUserResponse.setLastName("User");
        testUserResponse.setEmail("test@example.com");
        testUserResponse.setUserRole(User.UserRole.DEVELOPER);
        testUserResponse.setUserStatus(User.UserStatus.OFFLINE);
        
        testLoginResponse = new LoginResponseDTO();
        testLoginResponse.setAccessToken("test-access-token");
        testLoginResponse.setRefreshToken("test-refresh-token");
        testLoginResponse.setUser(testUserResponse);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterUser_Success() {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setEmail("new@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("New");
        registrationDTO.setLastName("User");
        registrationDTO.setUserRole(User.UserRole.DEVELOPER);

        UserResponseDTO newUserResponse = new UserResponseDTO();
        newUserResponse.setUserId(1);
        newUserResponse.setEmail("new@example.com");
        newUserResponse.setUserRole(User.UserRole.DEVELOPER);

        when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(newUserResponse);

        ResponseEntity<UserResponseDTO> response = userController.register(registrationDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new@example.com", response.getBody().getEmail());
        assertEquals(User.UserRole.DEVELOPER, response.getBody().getUserRole());
        
        verify(userService).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    @DisplayName("Should successfully login with correct credentials")
    void testLogin_Success() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");

        // Mock HttpServletRequest
        jakarta.servlet.http.HttpServletRequest mockRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:3000");

        when(userService.login(any(LoginDTO.class))).thenReturn(testLoginResponse);

        ResponseEntity<LoginResponseDTO> response = userController.login(mockRequest, loginDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-access-token", response.getBody().getAccessToken());
        assertNotNull(response.getBody().getUser());
        assertEquals("test@example.com", response.getBody().getUser().getEmail());
        
        verify(userService).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("Should get user by ID")
    void testGetUserById_Success() {
        when(userService.getUserById(1)).thenReturn(testUserResponse);

        ResponseEntity<UserResponseDTO> response = userController.getUserById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getUserId());
        assertEquals("test@example.com", response.getBody().getEmail());
        
        verify(userService).getUserById(1);
    }

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        UserResponseDTO user2 = new UserResponseDTO();
        user2.setUserId(2);
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setEmail("user2@example.com");
        user2.setUserRole(User.UserRole.CLIENT);

        List<UserResponseDTO> users = Arrays.asList(testUserResponse, user2);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserResponseDTO>> response = userController.getUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should update user status")
    void testUpdateUserStatus() {
        doNothing().when(userService).updateUserStatus(1, User.UserStatus.ONLINE);

        ResponseEntity<Void> response = userController.updateUserStatus(1, User.UserStatus.ONLINE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(userService).updateUserStatus(1, User.UserStatus.ONLINE);
    }

    @Test
    @DisplayName("Should delete user")
    void testDeleteUser() {
        doNothing().when(userService).deleteUser(1);

        ResponseEntity<Void> response = userController.deleteUser(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        verify(userService).deleteUser(1);
    }
}
