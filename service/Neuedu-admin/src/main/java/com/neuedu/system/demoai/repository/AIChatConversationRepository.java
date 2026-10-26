package com.neuedu.system.demoai.repository;

import com.neuedu.system.demoai.entity.AIChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AIChatConversationRepository extends JpaRepository<AIChatConversation, Long> {
    Optional<AIChatConversation> findByConversationKeyAndUserId(String conversationKey, Long userId);
    long deleteByUpdateTimeBefore(LocalDateTime cutoff);
}
