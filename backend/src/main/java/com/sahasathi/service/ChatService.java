package com.sahasathi.service;

import com.sahasathi.dto.ConversationResponse;
import com.sahasathi.dto.MessageResponse;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Conversation;
import com.sahasathi.model.Message;
import com.sahasathi.model.User;
import com.sahasathi.repository.ConversationRepository;
import com.sahasathi.repository.MessageRepository;
import com.sahasathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Conversation getOrCreateConversation(Long user1Id, Long user2Id) {
        return conversationRepository.findByParticipants(user1Id, user2Id)
                .orElseGet(() -> {
                    Conversation conv = Conversation.builder()
                            .participant1Id(user1Id)
                            .participant2Id(user2Id)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(conv);
                });
    }

    @Transactional
    public MessageResponse sendMessage(Long conversationId, Long senderId, String text) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));

        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .text(text)
                .createdAt(LocalDateTime.now())
                .build();
        Message saved = messageRepository.save(message);

        conv.setLastMessage(text);
        conv.setLastMessageAt(saved.getCreatedAt());
        conversationRepository.save(conv);

        return toMessageResponse(saved, sender);
    }

    public Page<MessageResponse> getMessages(Long conversationId, Long userId, int page, int size) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        if (!conv.getParticipant1Id().equals(userId) && !conv.getParticipant2Id().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(m -> {
                    User sender = userRepository.findById(m.getSenderId()).orElse(null);
                    return toMessageResponse(m, sender);
                });
    }

    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        messageRepository.markAsRead(conversationId, userId);
    }

    public List<ConversationResponse> getConversations(Long userId) {
        List<Conversation> convs = conversationRepository.findByParticipantId(userId);

        return convs.stream().map(c -> {
            Long otherId = c.getParticipant1Id().equals(userId) ? c.getParticipant2Id() : c.getParticipant1Id();
            User otherUser = userRepository.findById(otherId).orElse(null);
            long unread = messageRepository.countByConversationIdAndSenderIdNotAndIsReadFalse(c.getId(), userId);

            return ConversationResponse.builder()
                    .id(c.getId())
                    .otherUserId(otherId)
                    .otherUserName(otherUser != null ? otherUser.getName() : "Unknown")
                    .otherUserPicture(otherUser != null ? otherUser.getProfilePictureUrl() : null)
                    .lastMessage(c.getLastMessage())
                    .lastMessageAt(c.getLastMessageAt())
                    .unreadCount(unread)
                    .build();
        }).collect(Collectors.toList());
    }

    private MessageResponse toMessageResponse(Message m, User sender) {
        return MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .senderId(m.getSenderId())
                .senderName(sender != null ? sender.getName() : "Unknown")
                .senderPicture(sender != null ? sender.getProfilePictureUrl() : null)
                .text(m.getText())
                .isRead(m.isRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
