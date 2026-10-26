package com.neuedu.system.demoai.repository;

import com.neuedu.system.demoai.entity.AIChatMetricEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AIChatMetricEventRepository extends JpaRepository<AIChatMetricEvent, Long> {
    @Query("SELECT COUNT(event), COALESCE(SUM(event.retrievalHit), 0), COALESCE(SUM(event.toolCalls), 0), " +
            "COALESCE(SUM(event.toolFailures), 0), COALESCE(SUM(event.durationMs), 0), " +
            "COALESCE(SUM(event.promptTokens), 0), COALESCE(SUM(event.completionTokens), 0), " +
            "COALESCE(SUM(event.estimatedCost), 0) FROM AIChatMetricEvent event WHERE event.createTime >= :since")
    Object[] summarizeSince(@Param("since") LocalDateTime since);
}
