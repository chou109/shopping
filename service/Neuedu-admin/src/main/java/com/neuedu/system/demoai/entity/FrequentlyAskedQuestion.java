package com.neuedu.system.demoai.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "frequently_asked_question")
public class FrequentlyAskedQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String question;  // 问题
    private String answer;  // 答案
    @Column(name = "product_id")
    private Long productId;  // 关联商品ID（null表示通用问题）
    private Integer priority;  // 优先级（越高越优先被AI使用）
    @Column(nullable = false)
    private Integer enabled = 1;
    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType = "FAQ";
    @Column(nullable = false)
    private Integer version = 1;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
