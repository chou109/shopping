package com.neuedu.system.demoai.repository;

import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FrequentlyAskedQuestion, Long> {

    @Query("SELECT faq FROM FrequentlyAskedQuestion faq WHERE faq.enabled = 1 AND (faq.productId = :productId OR faq.productId IS NULL) ORDER BY faq.priority DESC")
    List<FrequentlyAskedQuestion> findForAssistant(@Param("productId") Long productId, Pageable pageable);

    // 根据产品ID查询相关问题，同时查询通用问题(productId为null)
    @Query("SELECT faq FROM FrequentlyAskedQuestion faq " +
            "WHERE faq.enabled = 1 AND (faq.productId = :productId OR faq.productId IS NULL) " +
            "ORDER BY faq.priority DESC")
    List<FrequentlyAskedQuestion> findByProductIdOrGeneral(@Param("productId") Long productId);
}
