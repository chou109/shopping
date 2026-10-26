package com.neuedu.system.demoai.repository;

import com.neuedu.system.demoai.entity.AIChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIChatMessageRepository extends JpaRepository<AIChatMessage, Long> {
    List<AIChatMessage> findByConversationIdOrderByIdDesc(Long conversationId, Pageable pageable);
    void deleteByConversationId(Long conversationId);
}
