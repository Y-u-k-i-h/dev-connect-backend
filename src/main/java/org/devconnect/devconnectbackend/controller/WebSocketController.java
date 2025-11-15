//package org.devconnect.devconnectbackend.controller;
//
//import org.devconnect.devconnectbackend.dto.MessageDTO;
//import org.devconnect.devconnectbackend.dto.TypingIndicatorDTO;
//import org.devconnect.devconnectbackend.model.Conversation;
//import org.devconnect.devconnectbackend.service.ConversationService;
//import org.devconnect.devconnectbackend.service.MessageService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//@Controller
//public class WebSocketController {
//
//    @Autowired
//    private MessageService messageService;
//
//    @Autowired
//    private ConversationService conversationService;
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    /**
//     * Handle incoming messages from clients
//     * Endpoint: /app/chat
//     */
//    @MessageMapping("/chat")
//    public void handleMessage(@Payload MessageDTO messageDTO) {
//        try {
//            // Process and send message (without projectId)
//            MessageDTO savedMessage = messageService.sendMessage(
//                    messageDTO.getSenderId(),
//                    messageDTO.getReceiverId(),
//                    messageDTO.getText()
//            );
//
//            // Send message to receiver
//            messagingTemplate.convertAndSendToUser(
//                    messageDTO.getReceiverId().toString(),
//                    "/queue/messages",
//                    savedMessage
//            );
//
//            // Send confirmation to sender
//            messagingTemplate.convertAndSendToUser(
//                    messageDTO.getSenderId().toString(),
//                    "/queue/messages",
//                    savedMessage
//            );
//        } catch (Exception e) {
//            System.err.println("Error handling message: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Handle typing indicators
//     * Endpoint: /app/typing
//     */
//    @MessageMapping("/typing")
//    public void handleTypingIndicator(@Payload TypingIndicatorDTO typingIndicator) {
//        // Send typing indicator to the receiver
//        messagingTemplate.convertAndSendToUser(
//                typingIndicator.getReceiverId().toString(),
//                "/queue/typing",
//                typingIndicator
//        );
//    }
//
//    /**
//     * Handle message delivery confirmation
//     * Endpoint: /app/message-delivered
//     */
//    @MessageMapping("/message-delivered")
//    public void handleMessageDelivered(@Payload MessageDTO messageDTO) {
//        messageService.markMessageAsDelivered(messageDTO.getConversation_id());
//    }
//
//    /**
//     * Handle message read confirmation
//     * Endpoint: /app/messages-read
//     * Expects: { "senderId": ..., "receiverId": ... }
//     */
//    @MessageMapping("/messages-read")
//    public void handleMessagesRead(@Payload MessageDTO messageDTO) {
//        try {
//            // Get or create conversation to get conversation ID
//            Conversation conversation = conversationService.getOrCreateConversation(
//                messageDTO.getSenderId(),
//                messageDTO.getReceiverId()
//            );
//
//            messageService.markMessagesAsRead(
//                conversation.getConversation_id(),
//                messageDTO.getSenderId(),
//                messageDTO.getReceiverId()
//            );
//        } catch (Exception e) {
//            System.err.println("Error marking messages as read: " + e.getMessage());
//        }
//    }
//}
