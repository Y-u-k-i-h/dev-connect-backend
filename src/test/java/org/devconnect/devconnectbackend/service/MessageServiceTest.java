package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.MessageDTO;
import org.devconnect.devconnectbackend.model.Conversation;
import org.devconnect.devconnectbackend.model.Message;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.ConversationRepository;
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
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private Conversation testConversation;
    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test users
        sender = new User();
        sender.setUserId(1);
        sender.setFirstName("Sender");
        sender.setLastName("User");
        sender.setEmail("sender@test.com");
        sender.setPasswordHash("password");
        sender.setUserRole(User.UserRole.CLIENT);
        
        receiver = new User();
        receiver.setUserId(2);
        receiver.setFirstName("Receiver");
        receiver.setLastName("User");
        receiver.setEmail("receiver@test.com");
        receiver.setPasswordHash("password");
        receiver.setUserRole(User.UserRole.DEVELOPER);

        // Create test conversation (user 1 and user 2)
        testConversation = new Conversation(1L, 2L);
        testConversation.setConversation_id(1L);
        testConversation.setUnreadCountA(0);
        testConversation.setUnreadCountB(0);

        // Create test message
        testMessage = new Message(1L, 1L, "Hello Jane!");
        testMessage.setId(1L);
        testMessage.setStatus(Message.MessageStatus.SENT);
        testMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should send message successfully")
    void testSendMessage() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(conversationService.getOrCreateConversation(1L, 2L)).thenReturn(testConversation);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        doNothing().when(conversationService).updateConversationAfterMessage(anyLong(), anyLong(), anyString());

        // Act
        MessageDTO result = messageService.sendMessage(1L, 2L, "Hello Jane!");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getSenderId());
        assertEquals(2L, result.getReceiverId());
        assertEquals("Hello Jane!", result.getText());
        assertEquals("sent", result.getStatus());

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(2);
        verify(conversationService, times(1)).getOrCreateConversation(1L, 2L);
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(conversationService, times(1)).updateConversationAfterMessage(eq(1L), eq(1L), eq("Hello Jane!"));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("2"), eq("/queue/messages"), any(MessageDTO.class));
    }

    @Test
    @DisplayName("Should get messages in conversation")
    void testGetMessagesInConversation() {
        // Arrange
        Message message2 = new Message(1L, 2L, "Hi John!");
        message2.setId(2L);
        message2.setStatus(Message.MessageStatus.SENT);
        message2.setTimestamp(LocalDateTime.now().plusMinutes(1));

        List<Message> messages = Arrays.asList(testMessage, message2);
        
        when(conversationService.getConversation(1L, 1L)).thenReturn(testConversation);
        when(messageRepository.findByConversationIdOrderByTimestampAsc(1L)).thenReturn(messages);

        // Act
        List<MessageDTO> result = messageService.getMessagesInConversation(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(conversationService, times(1)).getConversation(1L, 1L);
        verify(messageRepository, times(1)).findByConversationIdOrderByTimestampAsc(1L);
    }

    @Test
    @DisplayName("Should get messages between users")
    void testGetMessagesBetweenUsers() {
        // Arrange
        Message message2 = new Message(1L, 2L, "Hi John!");
        message2.setId(2L);
        message2.setStatus(Message.MessageStatus.SENT);
        message2.setTimestamp(LocalDateTime.now().plusMinutes(1));

        List<Message> messages = Arrays.asList(testMessage, message2);
        
        when(conversationService.getOrCreateConversation(1L, 2L)).thenReturn(testConversation);
        when(conversationService.getConversation(1L, 1L)).thenReturn(testConversation);
        when(messageRepository.findByConversationIdOrderByTimestampAsc(1L)).thenReturn(messages);

        // Act
        List<MessageDTO> result = messageService.getMessagesBetweenUsers(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(conversationService, times(1)).getOrCreateConversation(1L, 2L);
        verify(messageRepository, times(1)).findByConversationIdOrderByTimestampAsc(1L);
    }

    @Test
    @DisplayName("Should mark messages as read")
    void testMarkMessagesAsRead() {
        // Arrange
        testMessage.setStatus(Message.MessageStatus.SENT);
        List<Message> messages = Arrays.asList(testMessage);
        
        when(messageRepository.findUnreadMessagesBySender(1L, 1L)).thenReturn(messages);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        doNothing().when(conversationService).markConversationAsRead(anyLong(), anyLong());

        // Act
        messageService.markMessagesAsRead(1L, 1L, 2L);

        // Assert
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("1"), eq("/queue/read-receipts"), any(MessageDTO.class));
        verify(conversationService, times(1)).markConversationAsRead(1L, 2L);
    }

    @Test
    @DisplayName("Should mark message as delivered")
    void testMarkMessageAsDelivered() {
        // Arrange
        testMessage.setStatus(Message.MessageStatus.SENT);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(conversationService.getConversation(1L, 1L)).thenReturn(testConversation);

        // Act
        messageService.markMessageAsDelivered(1L);

        // Assert
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("1"), eq("/queue/delivery-receipts"), any(MessageDTO.class));
    }

    @Test
    @DisplayName("Should not mark message as delivered if already delivered")
    void testMarkMessageAsDeliveredAlreadyDelivered() {
        // Arrange
        testMessage.setStatus(Message.MessageStatus.DELIVERED);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // Act
        messageService.markMessageAsDelivered(1L);

        // Assert - should not save again or send notification
        verify(messageRepository, never()).save(any(Message.class));
        verify(messagingTemplate, never())
                .convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should handle empty message list when marking as read")
    void testMarkMessagesAsReadEmptyList() {
        // Arrange
        when(messageRepository.findUnreadMessagesBySender(1L, 1L)).thenReturn(Arrays.asList());
        doNothing().when(conversationService).markConversationAsRead(anyLong(), anyLong());

        // Act
        messageService.markMessagesAsRead(1L, 1L, 2L);

        // Assert
        verify(messageRepository, never()).save(any(Message.class));
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        verify(conversationService, times(1)).markConversationAsRead(1L, 2L);
    }

    @Test
    @DisplayName("Should throw exception when message not found for delivery")
    void testMarkMessageAsDeliveredNotFound() {
        // Arrange
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.markMessageAsDelivered(999L);
        });

        verify(messageRepository, never()).save(any(Message.class));
    }
}

