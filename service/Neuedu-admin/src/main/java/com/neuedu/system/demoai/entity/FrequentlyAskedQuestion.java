package com.neuedu.system.demoai.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "frequently_asked_question")
public class FrequentlyAskedQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String question;  // 问题
    private String answer;  // 答案
    private Long productId;  // 关联商品ID（null表示通用问题）
    private Integer priority;  // 优先级（越高越优先被AI使用）
}