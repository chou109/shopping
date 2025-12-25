package com.neuedu.system.demoai.service;

import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import com.neuedu.system.demoai.repository.FAQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FAQService {

    @Autowired
    private FAQRepository faqRepository;

    // 获取某商品相关问题+通用问题
    public List<FrequentlyAskedQuestion> getRelevantQuestions(Long productId) {
        return faqRepository.findByProductIdOrGeneral(productId);
    }

    // 将问题列表转换为AI可理解的文本（例如："常见问题: 如何退货？，答案: ...；"）
    public String convertToKnowledge(List<FrequentlyAskedQuestion> questions) {
        StringBuilder knowledge = new StringBuilder();
        for (FrequentlyAskedQuestion question : questions) {
            knowledge.append("常见问题: ").append(question.getQuestion())
                    .append("，答案: ").append(question.getAnswer()).append("；");
        }
        return knowledge.toString();
    }
}