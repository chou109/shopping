package com.neuedu.system.demoai;

import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.demoai.assistant.GogoAssistantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;

@RestController
@RequestMapping("/system/ai/knowledge")
public class AIKnowledgeController extends BaseController {
    private final FAQRepository repository;
    private final ProductRepository productRepository;
    private final GogoAssistantService assistantService;
    public AIKnowledgeController(FAQRepository repository, ProductRepository productRepository, GogoAssistantService assistantService) {
        this.repository = repository; this.productRepository = productRepository; this.assistantService = assistantService;
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:list') or @ss.hasRole('admin')")
    @GetMapping("/metrics")
    public AjaxResult metrics() { return success(assistantService.metricsSnapshot()); }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public AjaxResult list() { return success(repository.findAll()); }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @PostMapping
    public AjaxResult create(@RequestBody FrequentlyAskedQuestion faq) {
        String validationError = validationError(faq);
        if (validationError != null) return error(validationError);
        faq.setId(null);
        if (faq.getPriority() == null) faq.setPriority(0);
        faq.setEnabled(faq.getEnabled() == null ? 1 : faq.getEnabled());
        faq.setSourceType(normalizeSourceType(faq.getSourceType()));
        faq.setVersion(1);
        faq.setUpdatedAt(LocalDateTime.now());
        return success(repository.save(faq));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @PutMapping
    public AjaxResult update(@RequestBody FrequentlyAskedQuestion faq) {
        if (faq == null || faq.getId() == null) return error("知识条目不存在");
        FrequentlyAskedQuestion current = repository.findById(faq.getId()).orElse(null);
        if (current == null) return error("知识条目不存在");
        String validationError = validationError(faq);
        if (validationError != null) return error(validationError);
        current.setQuestion(faq.getQuestion().trim());
        current.setAnswer(faq.getAnswer().trim());
        current.setProductId(faq.getProductId());
        current.setPriority(faq.getPriority() == null ? 0 : faq.getPriority());
        if (faq.getEnabled() != null) current.setEnabled(faq.getEnabled());
        if (faq.getSourceType() != null) current.setSourceType(normalizeSourceType(faq.getSourceType()));
        current.setVersion((current.getVersion() == null ? 0 : current.getVersion()) + 1);
        current.setUpdatedAt(LocalDateTime.now());
        return success(repository.save(current));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @PutMapping("/{id}/status")
    public AjaxResult updateStatus(@PathVariable Long id, @RequestParam Integer enabled) {
        if (enabled == null || (enabled != 0 && enabled != 1)) return error("启用状态只能为 0 或 1");
        FrequentlyAskedQuestion faq = repository.findById(id).orElse(null);
        if (faq == null) return error("知识条目不存在");
        faq.setEnabled(enabled);
        faq.setVersion((faq.getVersion() == null ? 0 : faq.getVersion()) + 1);
        faq.setUpdatedAt(LocalDateTime.now());
        return success(repository.save(faq));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return error("知识条目不存在");
        repository.deleteById(id); return success();
    }

    private String validationError(FrequentlyAskedQuestion faq) {
        if (faq == null || faq.getQuestion() == null || faq.getQuestion().trim().isEmpty()
                || faq.getQuestion().length() > 255 || faq.getAnswer() == null
                || faq.getAnswer().trim().isEmpty() || faq.getAnswer().length() > 10000) {
            return "问题和答案均不能为空，问题最多 255 字，答案最多 10000 字";
        }
        if (faq.getPriority() != null && (faq.getPriority() < 0 || faq.getPriority() > 10000)) {
            return "优先级必须在 0 到 10000 之间";
        }
        if (faq.getEnabled() != null && faq.getEnabled() != 0 && faq.getEnabled() != 1) return "启用状态只能为 0 或 1";
        if (faq.getSourceType() != null && !Arrays.asList("FAQ", "POLICY", "PRODUCT").contains(faq.getSourceType().trim().toUpperCase(Locale.ROOT))) {
            return "资料来源类型不受支持";
        }
        if (faq.getProductId() != null && (faq.getProductId() < 1
                || productRepository.findByIdAndDeleteFlagAndStatus(faq.getProductId(), 0, 1) == null)) {
            return "关联商品不存在或当前不可用";
        }
        return null;
    }

    private String normalizeSourceType(String sourceType) {
        if (sourceType == null || sourceType.trim().isEmpty()) return "FAQ";
        return sourceType.trim().toUpperCase(Locale.ROOT);
    }
}
