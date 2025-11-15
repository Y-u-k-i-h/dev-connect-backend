package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.ChatDTO;
import org.devconnect.devconnectbackend.model.Conversation;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.ConversationRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get or create a conversation between two users
     */
    @Transactional
    public Conversation getOrCreateConversation(Long userId1, Long userId2) {
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);

        return conversationRepository.findByParticipantPair(smallerId, largerId)
                .orElseGet(() -> {
                    Conversation conversation = new Conversation(userId1, userId2);
                    return conversationRepository.save(conversation);
                });
    }

    /**
     * Get all conversations for a user as ChatDTOs
     */
    public List<ChatDTO> getConversationsForUser(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        List<ChatDTO> chatDTOs = new ArrayList<>();

        for (Conversation conversation : conversations) {
            Long otherUserId = conversation.getOtherParticipantId(userId);
            User otherUser = userRepository.findById(otherUserId.intValue())
                    .orElse(null);

            if (otherUser != null) {
                String userName = otherUser.getFirstName() + " " + otherUser.getLastName();
                ChatDTO chatDTO = new ChatDTO(
                        conversation.getConversation_id(),
                        otherUser.getUserId().longValue(),
                        userName,
                        null, // User model doesn't have avatar field
                        otherUser.getUserRole().name().toLowerCase(),
                        otherUser.getUserStatus().name().toLowerCase(),
                        conversation.getLastMessagePreview(),
                        conversation.getLastMessageAt(),
                        conversation.getUnreadCountForUser(userId),
                        null // No projectId in new model
                );
                chatDTOs.add(chatDTO);
            }
        }

        return chatDTOs;
    }

    /**
     * Get a conversation by ID (with permission check)
     */
    public Conversation getConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Check that the user is a participant
        if (!conversation.getParticipantAId().equals(userId) &&
            !conversation.getParticipantBId().equals(userId)) {
            throw new RuntimeException("Access denied: User is not a participant in this conversation");
        }

        return conversation;
    }

    /**
     * Update conversation metadata after a new message
     */
    @Transactional
    public void updateConversationAfterMessage(Long conversationId, Long senderId, String messagePreview) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setLastMessagePreview(messagePreview);
        conversation.setLastMessageAt(java.time.LocalDateTime.now());

        // Increment unread count for the receiver
        Long receiverId = conversation.getOtherParticipantId(senderId);
        conversation.incrementUnreadForUser(receiverId);

        conversationRepository.save(conversation);
    }

    /**
     * Mark conversation as read for a user
     */
    @Transactional
    public void markConversationAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.resetUnreadForUser(userId);
        conversationRepository.save(conversation);
    }
}

