package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.MessageDTO;
import org.devconnect.devconnectbackend.model.Conversation;
import org.devconnect.devconnectbackend.model.Message;
import org.devconnect.devconnectbackend.repository.MessageRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send a message from one user to another
     */
    @Transactional
    public MessageDTO sendMessage(Long senderId, Long receiverId, String content) {
        // Validate users exist
        userRepository.findById(senderId.intValue())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        userRepository.findById(receiverId.intValue())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Get or create conversation
        Conversation conversation = conversationService.getOrCreateConversation(senderId, receiverId);

        // Create and save message
        Message message = new Message(conversation.getConversation_id(), senderId, content);
        message.setStatus(Message.MessageStatus.SENT);
        message = messageRepository.save(message);

        // Update conversation metadata
        conversationService.updateConversationAfterMessage(conversation.getConversation_id(), senderId, content);

        // Convert to DTO and send via WebSocket
        MessageDTO messageDTO = convertToDTO(message, receiverId);
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                messageDTO
        );

        return messageDTO;
    }

    /**
     * Get all messages in a conversation
     */
    public List<MessageDTO> getMessagesInConversation(Long conversationId, Long requestingUserId) {
        // Verify user is a participant
        Conversation conversation = conversationService.getConversation(conversationId, requestingUserId);

        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        List<MessageDTO> messageDTOs = new ArrayList<>();

        Long otherUserId = conversation.getOtherParticipantId(requestingUserId);

        for (Message message : messages) {
            // Determine receiverId for DTO (opposite of sender)
            Long receiverId = message.getSenderId().equals(requestingUserId) ? otherUserId : requestingUserId;
            messageDTOs.add(convertToDTO(message, receiverId));
        }

        return messageDTOs;
    }

    /**
     * Get messages between two users (creates conversation if needed)
     */
    public List<MessageDTO> getMessagesBetweenUsers(Long userId1, Long userId2) {
        Conversation conversation = conversationService.getOrCreateConversation(userId1, userId2);
        return getMessagesInConversation(conversation.getConversation_id(), userId1);
    }

    /**
     * Mark messages as read in a conversation
     */
    @Transactional
    public void markMessagesAsRead(Long conversationId, Long senderId, Long receiverId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessagesBySender(conversationId, senderId);

        for (Message message : unreadMessages) {
            message.setStatus(Message.MessageStatus.READ);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);

            // Notify sender about read receipt
            MessageDTO messageDTO = convertToDTO(message, receiverId);
            messagingTemplate.convertAndSendToUser(
                    senderId.toString(),
                    "/queue/read-receipts",
                    messageDTO
            );
        }

        // Update conversation unread count
        conversationService.markConversationAsRead(conversationId, receiverId);
    }

    /**
     * Mark message as delivered
     */
    @Transactional
    public void markMessageAsDelivered(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getStatus() == Message.MessageStatus.SENT) {
            message.setStatus(Message.MessageStatus.DELIVERED);
            messageRepository.save(message);

            // Notify sender about delivery
            Conversation conversation = conversationService.getConversation(
                message.getConversationId(),
                message.getSenderId()
            );
            Long receiverId = conversation.getOtherParticipantId(message.getSenderId());

            MessageDTO messageDTO = convertToDTO(message, receiverId);
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId().toString(),
                    "/queue/delivery-receipts",
                    messageDTO
            );
        }
    }

    /**
     * Convert Message entity to DTO
     */
    private MessageDTO convertToDTO(Message message, Long receiverId) {
        return new MessageDTO(
                message.getId(),
                message.getSenderId(),
                receiverId,
                message.getContent(),
                message.getStatus().name().toLowerCase(),
                message.getTimestamp(),
                null // No projectId in new model
        );
    }
}
