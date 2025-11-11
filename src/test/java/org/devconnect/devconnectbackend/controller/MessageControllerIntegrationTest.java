package org.devconnect.devconnectbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devconnect.devconnectbackend.dto.MessageDTO;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.MessageRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Message Controller Integration Tests")
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        // Clean up database
        messageRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        sender = new User("johndoe", "john@test.com", "password123", User.UserRole.CLIENT);
        sender = userRepository.save(sender);

        receiver = new User("janedoe", "jane@test.com", "password456", User.UserRole.DEVELOPER);
        receiver = userRepository.save(receiver);
    }

    @Test
    @DisplayName("Should send message via REST endpoint")
    void testSendMessage() throws Exception {
        // Arrange
        MessageDTO messageDTO = new MessageDTO(
                null,
                sender.getId(),
                receiver.getId(),
                "Hello Jane!",
                "sent",
                null,
                101L
        );

        // Act & Assert
        mockMvc.perform(post("/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.senderId").value(sender.getId()))
                .andExpect(jsonPath("$.receiverId").value(receiver.getId()))
                .andExpect(jsonPath("$.text").value("Hello Jane!"))
                .andExpect(jsonPath("$.status").value("sent"));
    }

    @Test
    @DisplayName("Should get conversation between users")
    void testGetConversation() throws Exception {
        // Simple test - just verify the endpoint accepts valid parameters
        // The endpoint should return 200 OK even if conversation is empty
        mockMvc.perform(get("/api/messages/conversation")
                        .param("userId1", sender.getId().toString())
                        .param("userId2", receiver.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should mark messages as read")
    void testMarkMessagesAsRead() throws Exception {
        // Simple test - just verify the endpoint accepts valid parameters
        // The endpoint should return 200 OK even if there are no messages to mark
        mockMvc.perform(put("/api/messages/read")
                        .param("senderId", sender.getId().toString())
                        .param("receiverId", receiver.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should update user status")
    void testUpdateUserStatus() throws Exception {
        mockMvc.perform(put("/api/messages/status/" + sender.getId())
                        .param("status", "ONLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Status updated successfully"));
    }

    @Test
    @DisplayName("Should get user status")
    void testGetUserStatus() throws Exception {
        // Arrange - update status first
        mockMvc.perform(put("/api/messages/status/" + sender.getId())
                .param("status", "ONLINE"))
                .andExpect(status().isOk());

        // Act & Assert - UserService returns lowercase status
        mockMvc.perform(get("/api/messages/status/" + sender.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("online"));
    }

    @Test
    @DisplayName("Should return bad request for invalid user")
    void testSendMessageInvalidUser() throws Exception {
        // Arrange
        MessageDTO messageDTO = new MessageDTO(
                null,
                999L, // Non-existent user
                receiver.getId(),
                "Hello!",
                "sent",
                null,
                null
        );

        // Act & Assert
        mockMvc.perform(post("/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(status().isBadRequest());
    }
}
