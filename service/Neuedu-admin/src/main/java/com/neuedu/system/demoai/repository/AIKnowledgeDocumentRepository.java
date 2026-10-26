package com.neuedu.system.demoai.repository;

import com.neuedu.system.demoai.entity.AIKnowledgeDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AIKnowledgeDocumentRepository extends JpaRepository<AIKnowledgeDocument, Long> {
    @Query("SELECT d FROM AIKnowledgeDocument d WHERE d.enabled = 1 AND (d.productId IS NULL OR d.productId = :productId) ORDER BY d.updatedAt DESC")
    List<AIKnowledgeDocument> findEnabledForProduct(@Param("productId") Long productId, Pageable pageable);
}
