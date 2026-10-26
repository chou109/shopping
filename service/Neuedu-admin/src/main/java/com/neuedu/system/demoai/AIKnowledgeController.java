package com.neuedu.system.demoai;

import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import com.neuedu.system.demoai.entity.AIKnowledgeDocument;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.AIKnowledgeDocumentRepository;
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
    private final AIKnowledgeDocumentRepository documentRepository;
    public AIKnowledgeController(FAQRepository repository, ProductRepository productRepository, GogoAssistantService assistantService,
                                 AIKnowledgeDocumentRepository documentRepository) {
        this.repository = repository; this.productRepository = productRepository; this.assistantService = assistantService;
        this.documentRepository = documentRepository;
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:list') or @ss.hasRole('admin')")
    @GetMapping("/metrics")
    public AjaxResult metrics() { return success(assistantService.metricsSnapshot()); }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:list') or @ss.hasRole('admin')")
    @GetMapping("/list")
    public AjaxResult list() { return success(repository.findAll()); }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:list') or @ss.hasRole('admin')")
    @GetMapping("/documents")
    public AjaxResult documents() { return success(documentRepository.findAll()); }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @PostMapping("/documents")
    public AjaxResult createDocument(@RequestBody AIKnowledgeDocument document) {
        String validationError = documentValidationError(document);
        if (validationError != null) return error(validationError);
        document.setId(null);
        document.setTitle(document.getTitle().trim());
        document.setContent(document.getContent().trim());
        document.setSourceType(normalizeDocumentSourceType(document.getSourceType()));
        document.setEnabled(document.getEnabled() == null ? 1 : document.getEnabled());
        document.setVersion(1);
        document.setUpdatedAt(LocalDateTime.now());
        return success(documentRepository.save(document));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @PutMapping("/documents")
    public AjaxResult updateDocument(@RequestBody AIKnowledgeDocument document) {
        if (document == null || document.getId() == null) return error("知识文档不存在");
        AIKnowledgeDocument current = documentRepository.findById(document.getId()).orElse(null);
        if (current == null) return error("知识文档不存在");
        String validationError = documentValidationError(document);
        if (validationError != null) return error(validationError);
        current.setTitle(document.getTitle().trim());
        current.setContent(document.getContent().trim());
        current.setProductId(document.getProductId());
        current.setSourceType(normalizeDocumentSourceType(document.getSourceType()));
        if (document.getEnabled() != null) current.setEnabled(document.getEnabled());
        current.setVersion((current.getVersion() == null ? 0 : current.getVersion()) + 1);
        current.setUpdatedAt(LocalDateTime.now());
        return success(documentRepository.save(current));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @PutMapping("/documents/{id}/status")
    public AjaxResult updateDocumentStatus(@PathVariable Long id, @RequestParam Integer enabled) {
        if (enabled == null || (enabled != 0 && enabled != 1)) return error("启用状态只能为 0 或 1");
        AIKnowledgeDocument document = documentRepository.findById(id).orElse(null);
        if (document == null) return error("知识文档不存在");
        document.setEnabled(enabled);
        document.setVersion((document.getVersion() == null ? 0 : document.getVersion()) + 1);
        document.setUpdatedAt(LocalDateTime.now());
        return success(documentRepository.save(document));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @DeleteMapping("/documents/{id}")
    public AjaxResult deleteDocument(@PathVariable Long id) {
        if (!documentRepository.existsById(id)) return error("知识文档不存在");
        documentRepository.deleteById(id);
        return success();
    }

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

    private String documentValidationError(AIKnowledgeDocument document) {
        if (document == null || document.getTitle() == null || document.getTitle().trim().isEmpty()
                || document.getTitle().trim().length() > 200 || document.getContent() == null
                || document.getContent().trim().isEmpty() || document.getContent().trim().length() > 30000) {
            return "文档标题不能为空且最多 200 字，正文不能为空且最多 30000 字";
        }
        if (document.getSourceType() != null && !Arrays.asList("FAQ", "POLICY", "PRODUCT").contains(document.getSourceType().trim().toUpperCase(Locale.ROOT))) {
            return "资料来源类型不受支持";
        }
        if (document.getEnabled() != null && document.getEnabled() != 0 && document.getEnabled() != 1) return "启用状态只能为 0 或 1";
        if (document.getProductId() != null && (document.getProductId() < 1
                || productRepository.findByIdAndDeleteFlagAndStatus(document.getProductId(), 0, 1) == null)) {
            return "关联商品不存在或当前不可用";
        }
        return null;
    }

    private String normalizeDocumentSourceType(String sourceType) {
        if (sourceType == null || sourceType.trim().isEmpty()) return "POLICY";
        String normalized = sourceType.trim().toUpperCase(Locale.ROOT);
        return Arrays.asList("POLICY", "PRODUCT", "FAQ").contains(normalized) ? normalized : "POLICY";
    }
}
