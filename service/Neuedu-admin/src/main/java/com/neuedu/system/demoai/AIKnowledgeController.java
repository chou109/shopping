package com.neuedu.system.demoai;

import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.assistant.GogoAssistantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/ai/knowledge")
public class AIKnowledgeController extends BaseController {
    private final FAQRepository repository;
    private final GogoAssistantService assistantService;
    public AIKnowledgeController(FAQRepository repository, GogoAssistantService assistantService) {
        this.repository = repository; this.assistantService = assistantService;
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
        if (!valid(faq)) return error("问题和答案均不能为空，且长度不能超限");
        faq.setId(null);
        if (faq.getPriority() == null) faq.setPriority(0);
        return success(repository.save(faq));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @PutMapping
    public AjaxResult update(@RequestBody FrequentlyAskedQuestion faq) {
        if (faq.getId() == null || !repository.existsById(faq.getId())) return error("知识条目不存在");
        if (!valid(faq)) return error("问题和答案均不能为空，且长度不能超限");
        return success(repository.save(faq));
    }

    @PreAuthorize("@ss.hasPermi('system:ai:knowledge:edit') or @ss.hasRole('admin')")
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return error("知识条目不存在");
        repository.deleteById(id); return success();
    }

    private boolean valid(FrequentlyAskedQuestion faq) {
        return faq != null && faq.getQuestion() != null && !faq.getQuestion().trim().isEmpty()
                && faq.getQuestion().length() <= 255 && faq.getAnswer() != null
                && !faq.getAnswer().trim().isEmpty() && faq.getAnswer().length() <= 10000;
    }
}
