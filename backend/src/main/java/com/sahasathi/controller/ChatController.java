package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.dto.ConversationResponse;
import com.sahasathi.dto.MessageResponse;
import com.sahasathi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations(
            @RequestParam Long userId) {
        log.info("Fetching conversations for user: {}", userId);
        List<ConversationResponse> convs = chatService.getConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(convs, "Conversations fetched successfully"));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreateConversation(
            @RequestParam Long userId,
            @RequestParam Long otherUserId) {
        log.info("Getting or creating conversation between {} and {}", userId, otherUserId);
        var conv = chatService.getOrCreateConversation(userId, otherUserId);
        List<ConversationResponse> convs = chatService.getConversations(userId);
        ConversationResponse response = convs.stream()
                .filter(c -> c.getId().equals(conv.getId()))
                .findFirst().orElse(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Conversation created"));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Fetching messages for conversation: {}", conversationId);
        Page<MessageResponse> messages = chatService.getMessages(conversationId, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages, "Messages fetched successfully"));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long conversationId,
            @RequestParam Long userId,
            @RequestBody Map<String, String> body) {
        log.info("Sending message in conversation: {} by user: {}", conversationId, userId);
        MessageResponse msg = chatService.sendMessage(conversationId, userId, body.get("text"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(msg, "Message sent"));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long conversationId,
            @RequestParam Long userId) {
        chatService.markAsRead(conversationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
    }
}
