package com.neuedu.system.demoai.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_chat_metric_event")
public class AIChatMetricEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "retrieval_hit", nullable = false)
    private Integer retrievalHit;

    @Column(name = "tool_calls", nullable = false)
    private Integer toolCalls;

    @Column(name = "tool_failures", nullable = false)
    private Integer toolFailures;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "prompt_tokens", nullable = false)
    private Integer promptTokens;

    @Column(name = "completion_tokens", nullable = false)
    private Integer completionTokens;

    @Column(name = "estimated_cost", nullable = false, precision = 20, scale = 8)
    private BigDecimal estimatedCost;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
}
