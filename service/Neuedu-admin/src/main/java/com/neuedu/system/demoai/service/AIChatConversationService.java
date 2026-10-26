package com.neuedu.system.demoai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neuedu.system.demoai.entity.AIChatConversation;
import com.neuedu.system.demoai.entity.AIChatMessage;
import com.neuedu.system.demoai.repository.AIChatConversationRepository;
import com.neuedu.system.demoai.repository.AIChatMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AIChatConversationService {
    private static final int HISTORY_LIMIT = 12;
    private final AIChatConversationRepository conversations;
    private final AIChatMessageRepository messages;
    private final ObjectMapper mapper;
    private final AtomicLong lastCleanupAt = new AtomicLong();
    private static final long CLEANUP_INTERVAL_MS = 6L * 60L * 60L * 1000L;

    @Value("${ai.gogo.chat-history-retention-days:30}") private int historyRetentionDays = 30;

    public AIChatConversationService(AIChatConversationRepository conversations, AIChatMessageRepository messages,
                                    ObjectMapper mapper) {
        this.conversations = conversations;
        this.messages = messages;
        this.mapper = mapper;
    }

    public Opened open(String requestedKey, Long userId) {
        if (userId == null) return new Opened(null, null, Collections.emptyList());
        cleanupExpiredIfDue();
        if (isUuid(requestedKey)) {
            AIChatConversation existing = conversations.findByConversationKeyAndUserId(requestedKey, userId).orElse(null);
            if (existing != null) return new Opened(existing.getConversationKey(), existing.getId(), load(existing.getId()));
        }
        AIChatConversation conversation = new AIChatConversation();
        conversation.setConversationKey(UUID.randomUUID().toString());
        conversation.setUserId(userId);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(conversation.getCreateTime());
        conversation = conversations.saveAndFlush(conversation);
        return new Opened(conversation.getConversationKey(), conversation.getId(), new ArrayList<>());
    }

    private void cleanupExpiredIfDue() {
        long now = System.currentTimeMillis();
        long last = lastCleanupAt.get();
        if (now - last < CLEANUP_INTERVAL_MS || !lastCleanupAt.compareAndSet(last, now)) return;
        try {
            conversations.deleteByUpdateTimeBefore(LocalDateTime.now().minusDays(Math.max(1, historyRetentionDays)));
        } catch (Exception ex) {
            // An uninitialized optional history schema must not prevent chat from working.
            lastCleanupAt.set(0L);
        }
    }

    @Transactional(readOnly = true)
    public List<ObjectNode> load(Long conversationId) {
        if (conversationId == null) return new ArrayList<>();
        List<AIChatMessage> recent = messages.findByConversationIdOrderByIdDesc(conversationId, PageRequest.of(0, HISTORY_LIMIT));
        Collections.reverse(recent);
        List<ObjectNode> history = new ArrayList<>();
        for (AIChatMessage message : recent) {
            ObjectNode item = mapper.createObjectNode();
            item.put("role", message.getRole());
            item.put("content", message.getContent());
            history.add(item);
        }
        return history;
    }

    @Transactional
    public boolean saveExchange(String conversationKey, Long userId, String question, String answer) {
        if (userId == null || !isUuid(conversationKey) || question == null || answer == null || answer.trim().isEmpty()) return false;
        AIChatConversation conversation = conversations.findByConversationKeyAndUserId(conversationKey, userId).orElse(null);
        if (conversation == null) return false;
        LocalDateTime now = LocalDateTime.now();
        AIChatMessage userMessage = message(conversation.getId(), "user", question, now);
        AIChatMessage assistantMessage = message(conversation.getId(), "assistant", answer, now);
        messages.save(userMessage);
        messages.save(assistantMessage);
        conversation.setUpdateTime(now);
        conversations.save(conversation);
        return true;
    }

    @Transactional
    public boolean clear(String conversationKey, Long userId) {
        if (userId == null || !isUuid(conversationKey)) return false;
        AIChatConversation conversation = conversations.findByConversationKeyAndUserId(conversationKey, userId).orElse(null);
        if (conversation == null) return false;
        messages.deleteByConversationId(conversation.getId());
        conversations.delete(conversation);
        return true;
    }

    private AIChatMessage message(Long conversationId, String role, String content, LocalDateTime now) {
        AIChatMessage message = new AIChatMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(now);
        return message;
    }

    private boolean isUuid(String value) {
        if (value == null) return false;
        try { return UUID.fromString(value).toString().equalsIgnoreCase(value); }
        catch (IllegalArgumentException ex) { return false; }
    }

    public static class Opened {
        private final String key;
        private final Long id;
        private final List<ObjectNode> history;
        Opened(String key, Long id, List<ObjectNode> history) { this.key = key; this.id = id; this.history = history; }
        public String getKey() { return key; }
        public Long getId() { return id; }
        public List<ObjectNode> getHistory() { return history; }
    }
}
