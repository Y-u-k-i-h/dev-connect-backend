package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.MessageDTO;
import org.devconnect.devconnectbackend.model.Chat;
import org.devconnect.devconnectbackend.model.Message;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.ChatRepository;
import org.devconnect.devconnectbackend.repository.MessageRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Message Service Tests")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageService messageService;

    private User sender;
    private User receiver;
    private Message testMessage;
    private Chat testChat;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test users
        sender = new User("johndoe", "john@example.com", "password123", User.UserRole.CLIENT);
        sender.setId(1L);

        receiver = new User("janedoe", "jane@example.com", "password456", User.UserRole.DEVELOPER);
        receiver.setId(2L);

        // Create test message
        testMessage = new Message(sender, receiver, "Hello Jane!", 101L);
        testMessage.setId(1L);
        testMessage.setStatus(Message.MessageStatus.SENT);
        testMessage.setTimestamp(LocalDateTime.now());

        // Create test chat
        testChat = new Chat(sender, receiver, 101L);
        testChat.setId(1L);
        testChat.setUnreadCountUser1(0);
        testChat.setUnreadCountUser2(0);
    }

    @Test
    @DisplayName("Should send message successfully")
    void testSendMessage() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(chatRepository.findChatBetweenUsersAnyProject(1L, 2L))
                .thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

        // Act
        MessageDTO result = messageService.sendMessage(1L, 2L, "Hello Jane!", 101L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getSenderId());
        assertEquals(2L, result.getReceiverId());
        assertEquals("Hello Jane!", result.getText());
        assertEquals("sent", result.getStatus());

        verify(messageRepository, times(1)).save(any(Message.class));
        verify(chatRepository, times(1)).save(any(Chat.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("2"), eq("/queue/messages"), any(MessageDTO.class));
    }

    @Test
    @DisplayName("Should throw exception when sender not found")
    void testSendMessageSenderNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(1L, 2L, "Hello!", 101L);
        });

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when receiver not found")
    void testSendMessageReceiverNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(1L, 2L, "Hello!", 101L);
        });

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should get messages between users")
    void testGetMessagesBetweenUsers() {
        // Arrange
        Message message2 = new Message(receiver, sender, "Hi John!", 101L);
        message2.setId(2L);
        message2.setStatus(Message.MessageStatus.SENT);
        message2.setTimestamp(LocalDateTime.now().plusMinutes(1));

        List<Message> messages = Arrays.asList(testMessage, message2);
        when(messageRepository.findMessagesBetweenUsers(1L, 2L)).thenReturn(messages);

        // Act
        List<MessageDTO> result = messageService.getMessagesBetweenUsers(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(messageRepository, times(1)).findMessagesBetweenUsers(1L, 2L);
    }

    @Test
    @DisplayName("Should mark messages as read")
    void testMarkMessagesAsRead() {
        // Arrange
        testMessage.setStatus(Message.MessageStatus.SENT);
        List<Message> messages = Arrays.asList(testMessage);
        
        when(messageRepository.findMessagesBetweenUsers(1L, 2L)).thenReturn(messages);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(chatRepository.findChatBetweenUsersAnyProject(1L, 2L))
                .thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

        // Act
        messageService.markMessagesAsRead(1L, 2L);

        // Assert
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("1"), eq("/queue/read-receipts"), any(MessageDTO.class));
        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    @DisplayName("Should mark message as delivered")
    void testMarkMessageAsDelivered() {
        // Arrange
        testMessage.setStatus(Message.MessageStatus.SENT);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // Act
        messageService.markMessageAsDelivered(1L, 2L);

        // Assert
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("1"), eq("/queue/delivery-receipts"), any(MessageDTO.class));
    }

    @Test
    @DisplayName("Should not mark message as delivered if receiver mismatch")
    void testMarkMessageAsDeliveredReceiverMismatch() {
        // Arrange
        testMessage.setStatus(Message.MessageStatus.SENT);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // Act
        messageService.markMessageAsDelivered(1L, 999L); // Wrong receiver ID

        // Assert
        verify(messageRepository, never()).save(any(Message.class));
        verify(messagingTemplate, never())
                .convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should handle empty message list when marking as read")
    void testMarkMessagesAsReadEmptyList() {
        // Arrange
        when(messageRepository.findMessagesBetweenUsers(1L, 2L)).thenReturn(Arrays.asList());
        when(chatRepository.findChatBetweenUsersAnyProject(1L, 2L))
                .thenReturn(Optional.of(testChat));

        // Act
        messageService.markMessagesAsRead(1L, 2L);

        // Assert
        verify(messageRepository, never()).save(any(Message.class));
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }
}
