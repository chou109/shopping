package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import com.neuedu.system.demoai.entity.AIEmbeddingCacheEntry;
import com.neuedu.system.demoai.entity.AIKnowledgeDocument;
import com.neuedu.system.demoai.entity.Product;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.AIChatMetricEventRepository;
import com.neuedu.system.demoai.repository.AIEmbeddingCacheRepository;
import com.neuedu.system.demoai.repository.AIKnowledgeDocumentRepository;
import com.neuedu.system.demoai.entity.AIChatMetricEvent;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.LongAdder;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicLong;

/** Gogo (逛逛) assistant: DeepSeek chat, retrieval and allow-listed business tools. */
@Slf4j
@Service
public class GogoAssistantService {
    private static final int MAX_KNOWLEDGE_CHUNKS = 100;
    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final int MAX_HISTORY_MESSAGE_CHARS = 4000;
    private static final int MAX_TOOL_CALLS_PER_RESPONSE = 5;
    private static final int MAX_TOOL_CALLS_PER_REQUEST = 8;
    private final ObjectMapper mapper;
    private final FAQRepository faqRepository;
    private final ProductRepository productRepository;
    private final IOrderInfoService orderInfoService;
    private final GogoPromptFactory promptFactory;
    private final EmbeddingProvider embeddingProvider;
    private final AIChatMetricEventRepository metricEventRepository;
    private final AIEmbeddingCacheRepository embeddingCacheRepository;
    private final AIKnowledgeDocumentRepository knowledgeDocumentRepository;
    private final AtomicLong lastMetricCleanupAt = new AtomicLong();
    private final Map<String, float[]> embeddingCache = Collections.synchronizedMap(new LinkedHashMap<String, float[]>(128, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, float[]> eldest) { return size() > 512; }
    });

    @Value("${ai.gogo.api-key:}") private String apiKey;
    @Value("${ai.gogo.base-url:https://api.deepseek.com}") private String baseUrl;
    @Value("${ai.gogo.model:deepseek-flash}") private String model;
    @Value("${ai.gogo.connect-timeout-ms:5000}") private int connectTimeout;
    @Value("${ai.gogo.read-timeout-ms:45000}") private int readTimeout;
    @Value("${ai.gogo.input-price-per-million:0}") private BigDecimal inputPricePerMillion;
    @Value("${ai.gogo.output-price-per-million:0}") private BigDecimal outputPricePerMillion;
    @Value("${ai.gogo.embedding.minimum-similarity:0.52}") private double minimumEmbeddingSimilarity = 0.52D;
    @Value("${ai.gogo.metrics-retention-days:90}") private int metricsRetentionDays = 90;
    private final LongAdder requestCount = new LongAdder();
    private final LongAdder retrievalHitCount = new LongAdder();
    private final LongAdder handoffCount = new LongAdder();
    private final LongAdder toolCallCount = new LongAdder();
    private final LongAdder toolFailureCount = new LongAdder();
    private final LongAdder totalDurationMs = new LongAdder();
    private final LongAdder promptTokenCount = new LongAdder();
    private final LongAdder completionTokenCount = new LongAdder();
    private final java.util.concurrent.atomic.DoubleAdder estimatedCost = new java.util.concurrent.atomic.DoubleAdder();

    @Autowired
    public GogoAssistantService(ObjectMapper mapper, FAQRepository faqRepository, ProductRepository productRepository,
                              IOrderInfoService orderInfoService, GogoPromptFactory promptFactory, EmbeddingProvider embeddingProvider,
                              AIChatMetricEventRepository metricEventRepository, AIEmbeddingCacheRepository embeddingCacheRepository,
                              AIKnowledgeDocumentRepository knowledgeDocumentRepository) {
        this.mapper = mapper;
        this.faqRepository = faqRepository;
        this.productRepository = productRepository;
        this.orderInfoService = orderInfoService;
        this.promptFactory = promptFactory;
        this.embeddingProvider = embeddingProvider;
        this.metricEventRepository = metricEventRepository;
        this.embeddingCacheRepository = embeddingCacheRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    public GogoAssistantService(ObjectMapper mapper, FAQRepository faqRepository, ProductRepository productRepository,
                              IOrderInfoService orderInfoService, GogoPromptFactory promptFactory, EmbeddingProvider embeddingProvider,
                              AIChatMetricEventRepository metricEventRepository, AIEmbeddingCacheRepository embeddingCacheRepository) {
        this(mapper, faqRepository, productRepository, orderInfoService, promptFactory, embeddingProvider,
                metricEventRepository, embeddingCacheRepository, null);
    }

    public GogoAssistantService(ObjectMapper mapper, FAQRepository faqRepository, ProductRepository productRepository,
                              IOrderInfoService orderInfoService, GogoPromptFactory promptFactory, EmbeddingProvider embeddingProvider,
                              AIChatMetricEventRepository metricEventRepository) {
        this(mapper, faqRepository, productRepository, orderInfoService, promptFactory, embeddingProvider, metricEventRepository, null);
    }

    public GogoAssistantService(ObjectMapper mapper, FAQRepository faqRepository, ProductRepository productRepository,
                              IOrderInfoService orderInfoService, GogoPromptFactory promptFactory, EmbeddingProvider embeddingProvider) {
        this(mapper, faqRepository, productRepository, orderInfoService, promptFactory, embeddingProvider, null);
    }

    public GogoAssistantService(ObjectMapper mapper, FAQRepository faqRepository, ProductRepository productRepository,
                              IOrderInfoService orderInfoService, GogoPromptFactory promptFactory) {
        this(mapper, faqRepository, productRepository, orderInfoService, promptFactory, (EmbeddingProvider) null);
    }

    public void answer(String question, Long productId, LoginUser loginUser, List<ObjectNode> history,
                       Consumer<String> onToken, Consumer<List<Map<String, Object>>> onSources,
                       Consumer<Boolean> onHandoff) {
        answer(question, productId, loginUser, history, onToken, onSources, result -> { }, onHandoff);
    }

    public void answer(String question, Long productId, LoginUser loginUser, List<ObjectNode> history,
                       Consumer<String> onToken, Consumer<List<Map<String, Object>>> onSources,
                       Consumer<Map<String, Object>> onToolResult, Consumer<Boolean> onHandoff) {
        if (history == null) history = new ArrayList<>();
        long started = System.currentTimeMillis();
        List<Map<String, Object>> sources = retrieve(question, productId);
        onSources.accept(sources);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            onToken.accept("逛逛的模型服务尚未配置，已为您转接人工客服。您也可以先查看商品详情页或登录后的订单页。");
            onHandoff.accept(true);
            long duration = System.currentTimeMillis() - started;
            recordMetrics(!sources.isEmpty(), 0, 0, 0, 0, duration, true);
            log.info("ai_chat model_configured=false retrieval_hits={} duration_ms={}", sources.size(), duration);
            return;
        }

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", promptFactory.create(sources, loginUser != null));
        appendSafeHistory(messages, history);
        ObjectNode user = messages.addObject(); user.put("role", "user"); user.put("content", question);

        int toolCount = 0;
        int toolFailures = 0;
        int promptTokens = 0;
        int completionTokens = 0;
        boolean handoff = false;
        StringBuilder finalAnswer = new StringBuilder();
        Set<String> seenToolCallIds = new HashSet<>();
        Set<String> executedToolSignatures = new HashSet<>();
        Consumer<String> emit = token -> { finalAnswer.append(token); onToken.accept(token); };
        try {
            for (int turn = 0; turn < 4; turn++) {
                StreamResult result = stream(messages, loginUser, emit);
                promptTokens += result.promptTokens;
                completionTokens += result.completionTokens;
                if (result.toolCalls.isEmpty()) {
                    if (result.text.toString().trim().isEmpty()) {
                        emit.accept("暂时没有找到可靠答案，我为您转接人工客服。");
                        handoff = true;
                    }
                    break;
                }
                if (result.toolCalls.size() > MAX_TOOL_CALLS_PER_RESPONSE
                        || toolCount + result.toolCalls.size() > MAX_TOOL_CALLS_PER_REQUEST) {
                    toolCount += result.toolCalls.size();
                    toolFailures += result.toolCalls.size();
                    throw new IllegalStateException("model_tool_call_limit_exceeded");
                }
                toolCount += result.toolCalls.size();
                Set<String> responseIds = new HashSet<>();
                Set<String> responseSignatures = new HashSet<>();
                for (JsonNode call : result.toolCalls) {
                    String id = call.path("id").asText("").trim();
                    String type = call.path("type").asText("");
                    String name = call.path("function").path("name").asText("").trim();
                    String args = call.path("function").path("arguments").asText("{}");
                    JsonNode parsedArgs = null;
                    try { parsedArgs = mapper.readTree(args); } catch (Exception ignored) { }
                    String signature = parsedArgs != null && parsedArgs.isObject()
                            ? name + "\n" + canonicalJson(parsedArgs).toString() : "";
                    if (id.isEmpty() || id.length() > 128 || !"function".equals(type) || name.isEmpty() || args.length() > 2048
                            || parsedArgs == null || !parsedArgs.isObject()
                            || !responseIds.add(id) || seenToolCallIds.contains(id)
                            || !responseSignatures.add(signature) || executedToolSignatures.contains(signature)) {
                        toolFailures += result.toolCalls.size();
                        throw new IllegalStateException("model_tool_call_invalid_or_repeated");
                    }
                }
                seenToolCallIds.addAll(responseIds);
                executedToolSignatures.addAll(responseSignatures);
                ObjectNode assistantCall = mapper.createObjectNode(); assistantCall.put("role", "assistant");
                assistantCall.set("tool_calls", result.rawToolCalls);
                messages.add(assistantCall);
                for (JsonNode call : result.toolCalls) {
                    String id = call.path("id").asText("");
                    String name = call.path("function").path("name").asText("");
                    String args = call.path("function").path("arguments").asText("{}");
                    ToolResult execution;
                    try { execution = executeToolResult(name, mapper.readTree(args), loginUser); }
                    catch (Exception ex) { execution = ToolResult.failure("查询失败，请稍后重试或联系人工客服。"); }
                    if (!execution.success) toolFailures++;
                    if (execution.display != null) onToolResult.accept(execution.display);
                    ObjectNode tool = messages.addObject(); tool.put("role", "tool"); tool.put("tool_call_id", id); tool.put("content", execution.content);
                }
                if (turn == 3) { emit.accept("查询步骤过多，请缩小问题范围或联系人工客服。"); handoff = true; }
            }
        } catch (Exception ex) {
            log.warn("AI assistant request failed: type={}", ex.getClass().getSimpleName());
            if (sources.isEmpty()) emit.accept("模型暂时无法回答，且没有检索到可核验资料。请联系人工客服确认。");
            else emit.accept("模型服务暂时不可用。您可以先参考上方资料，或联系人工客服确认。");
            handoff = true;
        }
        if (finalAnswer.toString().matches("(?s).*(转接人工|联系人工客服|建议.{0,8}客服|无法确认|无法确定).*")) handoff = true;
        onHandoff.accept(handoff);
        long duration = System.currentTimeMillis() - started;
        recordMetrics(!sources.isEmpty(), toolCount, toolFailures, promptTokens, completionTokens, duration, handoff);
        log.info("ai_chat model={} retrieval_hits={} tool_calls={} tool_failures={} prompt_tokens={} completion_tokens={} estimated_cost={} duration_ms={} handoff={}",
                model, sources.size(), toolCount, toolFailures, promptTokens, completionTokens, estimatedCost(promptTokens, completionTokens), duration, handoff);
        history.add(user.deepCopy());
        ObjectNode answer = mapper.createObjectNode(); answer.put("role", "assistant"); answer.put("content", finalAnswer.toString()); history.add(answer);
        while (history.size() > 12) history.remove(0);
    }

    private JsonNode canonicalJson(JsonNode value) {
        if (value.isObject()) {
            ObjectNode sorted = mapper.createObjectNode();
            List<String> names = new ArrayList<>();
            value.fieldNames().forEachRemaining(names::add);
            Collections.sort(names);
            for (String name : names) sorted.set(name, canonicalJson(value.get(name)));
            return sorted;
        }
        if (value.isArray()) {
            ArrayNode ordered = mapper.createArrayNode();
            for (JsonNode item : value) ordered.add(canonicalJson(item));
            return ordered;
        }
        return value.deepCopy();
    }

    private void appendSafeHistory(ArrayNode messages, List<ObjectNode> history) {
        if (history == null || history.isEmpty()) return;
        List<ObjectNode> safe = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0 && safe.size() < MAX_HISTORY_MESSAGES; i--) {
            ObjectNode item = history.get(i);
            if (item == null) continue;
            String role = item.path("role").asText("");
            JsonNode contentNode = item.get("content");
            if (!("user".equals(role) || "assistant".equals(role)) || contentNode == null || !contentNode.isTextual()) continue;
            String content = contentNode.asText();
            if (content.trim().isEmpty()) continue;
            if (content.length() > MAX_HISTORY_MESSAGE_CHARS) content = content.substring(0, MAX_HISTORY_MESSAGE_CHARS) + "…（历史消息已截断）";
            ObjectNode message = mapper.createObjectNode();
            message.put("role", role);
            message.put("content", content);
            safe.add(message);
        }
        Collections.reverse(safe);
        for (ObjectNode item : safe) messages.add(item);
    }

    private void recordMetrics(boolean retrievalHit, int tools, int toolFailures, int promptTokens, int completionTokens, long duration, boolean handoff) {
        requestCount.increment(); if (retrievalHit) retrievalHitCount.increment();
        if (handoff) handoffCount.increment();
        toolCallCount.add(tools); toolFailureCount.add(toolFailures); totalDurationMs.add(duration);
        promptTokenCount.add(promptTokens); completionTokenCount.add(completionTokens);
        BigDecimal cost = estimatedCost(promptTokens, completionTokens);
        estimatedCost.add(cost.doubleValue());
        if (metricEventRepository != null) {
            try {
                AIChatMetricEvent event = new AIChatMetricEvent();
                event.setRetrievalHit(retrievalHit ? 1 : 0); event.setToolCalls(tools); event.setToolFailures(toolFailures);
                event.setHandoff(handoff ? 1 : 0);
                event.setDurationMs(duration); event.setPromptTokens(promptTokens); event.setCompletionTokens(completionTokens);
                event.setEstimatedCost(cost); event.setCreateTime(java.time.LocalDateTime.now());
                metricEventRepository.save(event);
                cleanupMetricEventsIfDue();
            } catch (Exception ex) { log.debug("持久化 AI 指标失败，错误类型={}", ex.getClass().getSimpleName()); }
        }
    }

    private void cleanupMetricEventsIfDue() {
        long now = System.currentTimeMillis();
        long last = lastMetricCleanupAt.get();
        if (now - last < 6L * 60L * 60L * 1000L || !lastMetricCleanupAt.compareAndSet(last, now)) return;
        try { metricEventRepository.deleteByCreateTimeBefore(java.time.LocalDateTime.now().minusDays(Math.max(1, metricsRetentionDays))); }
        catch (Exception ex) { lastMetricCleanupAt.set(0L); }
    }

    private BigDecimal estimatedCost(int promptTokens, int completionTokens) {
        BigDecimal input = inputPricePerMillion == null ? BigDecimal.ZERO : inputPricePerMillion;
        BigDecimal output = outputPricePerMillion == null ? BigDecimal.ZERO : outputPricePerMillion;
        return input.multiply(BigDecimal.valueOf(promptTokens)).add(output.multiply(BigDecimal.valueOf(completionTokens)))
                .divide(BigDecimal.valueOf(1000000), 8, RoundingMode.HALF_UP);
    }

    public Map<String, Object> metricsSnapshot() {
        if (metricEventRepository != null) {
            try {
                Object[] row = metricEventRepository.summarizeSince(java.time.LocalDateTime.now().minusDays(30));
                if (row != null && row.length >= 9) return metricsFromRow(row);
            } catch (Exception ex) { log.debug("读取持久化 AI 指标失败，使用进程指标：{}", ex.getClass().getSimpleName()); }
        }
        long requests = requestCount.sum(); long calls = toolCallCount.sum(); long failures = toolFailureCount.sum();
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("requests", requests);
        metrics.put("retrievalHitRate", requests == 0 ? 0D : (double) retrievalHitCount.sum() / requests);
        metrics.put("toolCalls", calls);
        metrics.put("toolSuccessRate", calls == 0 ? 0D : (double) (calls - failures) / calls);
        metrics.put("handoffRate", requests == 0 ? 0D : (double) handoffCount.sum() / requests);
        metrics.put("averageResponseMs", requests == 0 ? 0D : (double) totalDurationMs.sum() / requests);
        metrics.put("promptTokens", promptTokenCount.sum());
        metrics.put("completionTokens", completionTokenCount.sum());
        metrics.put("estimatedCost", BigDecimal.valueOf(estimatedCost.sum()).setScale(8, RoundingMode.HALF_UP));
        metrics.put("periodDays", 0);
        return metrics;
    }

    private Map<String, Object> metricsFromRow(Object[] row) {
        long requests = number(row[0]); long calls = number(row[2]); long failures = number(row[3]);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("requests", requests);
        metrics.put("retrievalHitRate", requests == 0 ? 0D : (double) number(row[1]) / requests);
        metrics.put("toolCalls", calls);
        metrics.put("toolSuccessRate", calls == 0 ? 0D : (double) (calls - failures) / calls);
        metrics.put("handoffRate", requests == 0 ? 0D : (double) number(row[8]) / requests);
        metrics.put("averageResponseMs", requests == 0 ? 0D : (double) number(row[4]) / requests);
        metrics.put("promptTokens", number(row[5]));
        metrics.put("completionTokens", number(row[6]));
        BigDecimal cost = row[7] instanceof BigDecimal ? (BigDecimal) row[7] : BigDecimal.valueOf(row[7] instanceof Number ? ((Number) row[7]).doubleValue() : 0D);
        metrics.put("estimatedCost", cost.setScale(8, RoundingMode.HALF_UP));
        metrics.put("periodDays", 30);
        return metrics;
    }

    private long number(Object value) { return value instanceof Number ? ((Number) value).longValue() : 0L; }

    private List<Map<String, Object>> retrieve(String question, Long productId) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        Set<String> queryTerms = queryTerms(question);
        try {
            List<FrequentlyAskedQuestion> faqs = faqRepository.findForAssistant(productId, PageRequest.of(0, 200));
            for (FrequentlyAskedQuestion faq : faqs) {
                String sourceType = safe(faq.getSourceType()).isEmpty() ? "FAQ" : faq.getSourceType().toLowerCase(Locale.ROOT);
                Map<String, Object> candidate = doc(sourceType.toUpperCase(Locale.ROOT) + " #" + faq.getId() + "：" + safe(faq.getQuestion()),
                        safe(faq.getAnswer()), 0D, sourceType, faq.getId());
                candidate.put("rankTitle", safe(faq.getQuestion()));
                candidate.put("embeddingText", safe(faq.getQuestion()) + "\n" + safe(faq.getAnswer()));
                candidate.put("priority", faq.getPriority() == null ? 0 : faq.getPriority());
                candidates.add(candidate);
            }
        } catch (Exception ex) { log.debug("FAQ retrieval unavailable: {}", ex.getClass().getSimpleName()); }
        if (knowledgeDocumentRepository != null) {
            try {
                List<AIKnowledgeDocument> documents = knowledgeDocumentRepository.findEnabledForProduct(productId, PageRequest.of(0, 50));
                int addedChunks = 0;
                for (AIKnowledgeDocument document : documents) {
                    List<String> chunks = KnowledgeChunker.split(document.getContent());
                    for (int i = 0; i < chunks.size(); i++) {
                        if (addedChunks >= MAX_KNOWLEDGE_CHUNKS) break;
                        String title = safe(document.getTitle()) + " · 片段 " + (i + 1);
                        String content = chunks.get(i);
                        Map<String, Object> candidate = doc(title, content, 0D, "document", document.getId());
                        candidate.put("rankTitle", safe(document.getTitle()));
                        candidate.put("embeddingText", safe(document.getTitle()) + "\n" + content);
                        candidate.put("priority", 0);
                        candidates.add(candidate);
                        addedChunks++;
                    }
                    if (addedChunks >= MAX_KNOWLEDGE_CHUNKS) break;
                }
            } catch (Exception ex) { log.debug("Knowledge document retrieval unavailable: {}", ex.getClass().getSimpleName()); }
        }
        try {
            List<Product> products = queryTerms.isEmpty() ? new ArrayList<>() : new ArrayList<>(productRepository.searchActiveByKeyword(question, PageRequest.of(0, 5)));
            if (productId != null) {
                Product selected = productRepository.findByIdAndDeleteFlagAndStatus(productId, 0, 1);
                if (selected != null && products.stream().noneMatch(p -> Objects.equals(p.getId(), selected.getId()))) products.add(selected);
            }
            for (Product p : products) {
                String title = "商品 #" + p.getId() + "：" + safe(p.getName());
                String content = "商品：" + safe(p.getName()) + "；简介：" + safe(p.getSubTitle()) + "；价格：" + p.getPrice() + "；库存：" + p.getStock();
                boolean explicitlySelected = productId != null && Objects.equals(productId, p.getId());
                Map<String, Object> candidate = doc(title, content, 0D, "product", p.getId());
                candidate.put("rankTitle", safe(p.getName()));
                // Dynamic price and stock stay live tool facts and are excluded from semantic vectors.
                candidate.put("embeddingText", "商品：" + safe(p.getName()) + "；简介：" + safe(p.getSubTitle()));
                candidate.put("selected", explicitlySelected);
                candidates.add(candidate);
            }
        } catch (Exception ex) { log.debug("Product retrieval unavailable: {}", ex.getClass().getSimpleName()); }

        if (candidates.isEmpty()) return candidates;
        Map<Map<String, Object>, List<String>> tokenized = new IdentityHashMap<>();
        Map<String, Integer> documentFrequency = new HashMap<>();
        int totalLength = 0;
        for (Map<String, Object> candidate : candidates) {
            List<String> tokens = tokens(candidate.get("rankTitle") + " " + candidate.get("content"));
            tokenized.put(candidate, tokens);
            totalLength += tokens.size();
            for (String term : new HashSet<>(tokens)) documentFrequency.merge(term, 1, Integer::sum);
        }
        double averageLength = candidates.isEmpty() ? 1D : Math.max(1D, (double) totalLength / candidates.size());
        Map<Map<String, Object>, Double> semanticScores = semanticScores(question, candidates);
        List<Map<String, Object>> ranked = new ArrayList<>();
        for (Map<String, Object> candidate : candidates) {
            boolean selected = Boolean.TRUE.equals(candidate.get("selected"));
            String title = String.valueOf(candidate.get("rankTitle"));
            String content = String.valueOf(candidate.get("content"));
            List<String> documentTokens = tokenized.get(candidate);
            Set<String> present = new HashSet<>(documentTokens);
            int matchedTerms = 0;
            Set<String> titleTerms = new HashSet<>(tokens(title));
            int matchedTitleTerms = 0;
            for (String term : queryTerms) if (present.contains(term)) matchedTerms++;
            for (String term : queryTerms) if (titleTerms.contains(term)) matchedTitleTerms++;
            double semanticSimilarity = semanticScores.getOrDefault(candidate, 0D);
            if (!selected && !relevant(queryTerms, matchedTerms, matchedTitleTerms)
                    && semanticSimilarity < minimumEmbeddingSimilarity) continue;

            List<String> titleTokens = tokens(title);
            List<String> bodyTokens = tokens(content);
            double relevance = selected ? 100D : bm25Score(queryTerms, titleTokens, bodyTokens, documentFrequency, candidates.size(), averageLength);
            relevance += Math.max(0D, semanticSimilarity) * 5D;
            int priority = candidate.get("priority") instanceof Number ? ((Number) candidate.get("priority")).intValue() : 0;
            relevance += Math.log1p(Math.max(0, Math.min(100, priority))) * 0.05D;
            String normalizedQuestion = normalize(question);
            if (normalizedQuestion.length() >= 4 && normalize(title).contains(normalizedQuestion)) relevance += 1.5D;
            relevance += queryTerms.isEmpty() ? 0D : 0.35D * matchedTerms / queryTerms.size();
            candidate.put("score", relevance);
            ranked.add(candidate);
        }
        ranked.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        if (ranked.size() > 5) ranked = new ArrayList<>(ranked.subList(0, 5));
        for (Map<String, Object> doc : ranked) {
            doc.remove("score"); doc.remove("priority"); doc.remove("selected"); doc.remove("rankTitle");
        }
        return ranked;
    }
    private Map<Map<String, Object>, Double> semanticScores(String question, List<Map<String, Object>> candidates) {
        Map<Map<String, Object>, Double> scores = new IdentityHashMap<>();
        if (embeddingProvider == null || !embeddingProvider.isConfigured() || candidates.isEmpty()) return scores;
        try {
            String queryKey = embeddingKey(question == null ? "" : question);
            Map<Map<String, Object>, String> candidateKeys = new IdentityHashMap<>();
            Map<String, String> missingTexts = new LinkedHashMap<>();
            Map<String, float[]> vectors = new LinkedHashMap<>();
            float[] queryVector = cachedEmbedding(queryKey);
            if (queryVector != null) vectors.put(queryKey, queryVector);
            else missingTexts.put(queryKey, question == null ? "" : question);
            for (Map<String, Object> candidate : candidates) {
                String text = embeddingText(candidate);
                String key = embeddingKey(text);
                candidateKeys.put(candidate, key);
                float[] vector = cachedEmbedding(key);
                if (vector != null) vectors.put(key, vector);
                else missingTexts.put(key, text);
            }

            loadPersistedEmbeddings(missingTexts.keySet(), vectors);
            missingTexts.keySet().removeAll(vectors.keySet());
            if (!missingTexts.isEmpty()) {
                List<String> batch = new ArrayList<>(missingTexts.values());
                List<float[]> generated = embeddingProvider.embed(batch);
                if (generated == null || generated.size() != batch.size()) throw new IllegalStateException("embedding_count_mismatch");
                List<AIEmbeddingCacheEntry> toPersist = new ArrayList<>();
                int index = 0;
                for (Map.Entry<String, String> missing : missingTexts.entrySet()) {
                    float[] vector = generated.get(index++);
                    if (vector == null || vector.length == 0) throw new IllegalStateException("embedding_vector_missing");
                    vectors.put(missing.getKey(), vector);
                    cacheEmbedding(missing.getKey(), vector);
                    // Persist reusable knowledge vectors only; user questions stay process-local.
                    AIEmbeddingCacheEntry entry = queryKey.equals(missing.getKey()) ? null : embeddingEntry(missing.getKey(), missing.getValue(), vector);
                    if (entry != null) toPersist.add(entry);
                }
                persistEmbeddings(toPersist);
            }
            queryVector = vectors.get(queryKey);
            for (Map.Entry<Map<String, Object>, String> entry : candidateKeys.entrySet()) {
                scores.put(entry.getKey(), cosine(queryVector, vectors.get(entry.getValue())));
            }
        } catch (Exception ex) {
            // Retrieval must remain available when the optional embedding service is down.
            log.debug("Semantic retrieval unavailable; using lexical retrieval: {}", ex.getClass().getSimpleName());
            scores.clear();
        }
        return scores;
    }

    private void loadPersistedEmbeddings(Collection<String> keys, Map<String, float[]> target) {
        if (embeddingCacheRepository == null || keys == null || keys.isEmpty()) return;
        try {
            for (AIEmbeddingCacheEntry entry : embeddingCacheRepository.findByCacheKeyIn(keys)) {
                if (entry == null || entry.getVectorJson() == null || entry.getVectorJson().length() > 100000) continue;
                float[] vector = mapper.readValue(entry.getVectorJson(), float[].class);
                if (vector.length == 0 || (entry.getDimensions() != null && vector.length != entry.getDimensions())) continue;
                boolean finite = true;
                for (float value : vector) if (!Float.isFinite(value)) { finite = false; break; }
                if (!finite) continue;
                target.put(entry.getCacheKey(), vector);
                cacheEmbedding(entry.getCacheKey(), vector);
            }
        } catch (Exception ex) { log.debug("读取持久向量缓存失败，使用运行时生成：{}", ex.getClass().getSimpleName()); }
    }

    private AIEmbeddingCacheEntry embeddingEntry(String cacheKey, String text, float[] vector) {
        if (embeddingCacheRepository == null) return null;
        try {
            AIEmbeddingCacheEntry entry = new AIEmbeddingCacheEntry();
            entry.setCacheKey(cacheKey);
            entry.setModelKey(embeddingProvider.cacheNamespace());
            entry.setContentHash(contentHash(text));
            entry.setVectorJson(mapper.writeValueAsString(vector));
            entry.setDimensions(vector.length);
            entry.setUpdatedAt(java.time.LocalDateTime.now());
            return entry;
        } catch (Exception ex) { return null; }
    }

    private void persistEmbeddings(List<AIEmbeddingCacheEntry> entries) {
        if (embeddingCacheRepository == null || entries == null || entries.isEmpty()) return;
        try { embeddingCacheRepository.saveAll(entries); }
        catch (Exception ex) { log.debug("写入持久向量缓存失败，保留本进程缓存：{}", ex.getClass().getSimpleName()); }
    }

    private String embeddingText(Map<String, Object> candidate) {
        Object configured = candidate.get("embeddingText");
        return configured == null ? String.valueOf(candidate.get("rankTitle")) + "\n" + String.valueOf(candidate.get("content")) : String.valueOf(configured);
    }

    private String contentHash(String text) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder(digest.length * 2);
        for (byte value : digest) hash.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        return hash.toString();
    }
    private float[] cachedEmbedding(String key) { synchronized (embeddingCache) { return embeddingCache.get(key); } }
    private void cacheEmbedding(String key, float[] vector) {
        if (vector == null || vector.length == 0) return;
        synchronized (embeddingCache) { embeddingCache.put(key, vector.clone()); }
    }
    private String embeddingKey(String text) throws Exception {
        String namespace = embeddingProvider == null ? "" : embeddingProvider.cacheNamespace();
        return contentHash(namespace + "\n" + (text == null ? "" : text));
    }
    private double cosine(float[] left, float[] right) {
        if (left == null || right == null || left.length != right.length || left.length == 0) return 0D;
        double dot = 0D, leftNorm = 0D, rightNorm = 0D;
        for (int i = 0; i < left.length; i++) {
            if (!Float.isFinite(left[i]) || !Float.isFinite(right[i])) return 0D;
            dot += left[i] * right[i]; leftNorm += left[i] * left[i]; rightNorm += right[i] * right[i];
        }
        return leftNorm == 0D || rightNorm == 0D ? 0D : dot / Math.sqrt(leftNorm * rightNorm);
    }
    private Map<String,Object> doc(String title, String content, double score, String sourceType, Long sourceId) { Map<String,Object> d = new LinkedHashMap<>(); d.put("title",title); d.put("content",content); d.put("sourceType",sourceType); d.put("sourceId",sourceId); d.put("score",score); return d; }
    private Set<String> queryTerms(String text) {
        String cleaned = text == null ? "" : text.replaceAll("请问|麻烦|帮我|想问一下|一下|可以吗|怎么样|怎么|如何|什么时候|现在|商品|商城|这个|这件|东西", " ");
        Set<String> terms = new LinkedHashSet<>(tokens(cleaned));
        terms.removeAll(Arrays.asList("请问", "麻烦", "商品", "商城", "东西", "现在", "如何", "怎么", "时候"));
        List<Set<String>> synonymGroups = Arrays.asList(
                new HashSet<>(Arrays.asList("退货", "退款", "退钱", "退换", "售后")),
                new HashSet<>(Arrays.asList("配送", "发货", "物流", "快递")),
                new HashSet<>(Arrays.asList("有货", "库存", "现货", "补货")));
        for (Set<String> group : synonymGroups) {
            if (!Collections.disjoint(terms, group)) terms.addAll(group);
        }
        return terms;
    }
    private List<String> tokens(String text) {
        List<String> out = new ArrayList<>();
        if (text == null) return out;
        String lower = text.toLowerCase(Locale.ROOT);
        for (String word : lower.split("[^\\p{IsHan}a-z0-9]+")) if (word.length() > 1) out.add(word);
        for (int i = 0; i + 1 < lower.length(); i++) {
            if (Character.UnicodeScript.of(lower.charAt(i)) == Character.UnicodeScript.HAN
                    && Character.UnicodeScript.of(lower.charAt(i + 1)) == Character.UnicodeScript.HAN) {
                out.add(lower.substring(i, i + 2));
            }
        }
        return out;
    }
    private double bm25Score(Set<String> terms, List<String> title, List<String> body,
                             Map<String, Integer> documentFrequency, int documentCount, double averageLength) {
        double score = 0D;
        double bodyLength = Math.max(1D, body.size());
        for (String term : terms) {
            int titleFrequency = Collections.frequency(title, term);
            int bodyFrequency = Collections.frequency(body, term);
            if (titleFrequency + bodyFrequency == 0) continue;
            int frequency = documentFrequency.getOrDefault(term, 0);
            double idf = Math.log(1D + (documentCount - frequency + 0.5D) / (frequency + 0.5D));
            score += idf * bm25TermFrequency(bodyFrequency, bodyLength, averageLength);
            score += idf * 2.2D * bm25TermFrequency(titleFrequency, Math.max(1D, title.size()), Math.max(1D, averageLength * 0.2D));
        }
        return score;
    }
    private double bm25TermFrequency(int frequency, double length, double averageLength) {
        if (frequency == 0) return 0D;
        double k1 = 1.2D;
        double normalizedLength = 1D - 0.75D + 0.75D * length / averageLength;
        return frequency * (k1 + 1D) / (frequency + k1 * normalizedLength);
    }
    private String normalize(String text) { return text == null ? "" : text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{IsHan}a-z0-9]", ""); }
    private boolean relevant(Set<String> terms, int matchedTerms, int matchedTitleTerms) {
        if (terms.isEmpty() || matchedTerms == 0) return false;
        int minimumMatches = Math.min(2, terms.size());
        if (matchedTerms < minimumMatches) return false;
        return matchedTerms > minimumMatches || matchedTitleTerms >= minimumMatches;
    }
    private String safe(String value) { return value == null ? "" : value; }

    private StreamResult stream(ArrayNode messages, LoginUser loginUser, Consumer<String> onToken) throws Exception {
        ObjectNode body = mapper.createObjectNode(); body.put("model", model); body.put("stream", true); body.set("messages", messages);
        body.set("tools", toolDefinitions(loginUser != null)); body.put("tool_choice", "auto");
        body.putObject("stream_options").put("include_usage", true);
        URL url = new URL(baseUrl.replaceAll("/$", "") + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST"); connection.setConnectTimeout(connectTimeout); connection.setReadTimeout(readTimeout);
        connection.setDoOutput(true); connection.setRequestProperty("Authorization", "Bearer " + apiKey); connection.setRequestProperty("Content-Type", "application/json");
        try {
            byte[] payload = mapper.writeValueAsBytes(body);
            try (OutputStream output = connection.getOutputStream()) { output.write(payload); }
            int status = connection.getResponseCode();
            if (status >= 400) {
                InputStream error = connection.getErrorStream();
                if (error != null) error.close();
                throw new IllegalStateException("model_http_" + status);
            }
            StreamResult result = new StreamResult(); Map<Integer,ObjectNode> calls = new TreeMap<>();
            InputStream input = connection.getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) continue;
                String data=line.substring(5).trim(); if ("[DONE]".equals(data)) break;
                JsonNode event; try { event=mapper.readTree(data); } catch(Exception ignored) { continue; }
                JsonNode usage=event.path("usage"); if(usage.has("prompt_tokens")) result.promptTokens=usage.path("prompt_tokens").asInt(); if(usage.has("completion_tokens")) result.completionTokens=usage.path("completion_tokens").asInt();
                JsonNode choice=event.path("choices").path(0); JsonNode delta=choice.path("delta");
                String text=delta.path("content").asText(""); if(!text.isEmpty()){ result.text.append(text); onToken.accept(text); }
                for(JsonNode tc:delta.path("tool_calls")) {
                    int index=tc.path("index").asInt(); ObjectNode call=calls.computeIfAbsent(index,k->mapper.createObjectNode());
                    if(tc.has("id")) call.put("id", tc.path("id").asText());
                    if(tc.has("type")) call.put("type", tc.path("type").asText());
                    JsonNode fn=tc.path("function"); if(!fn.isMissingNode()) { ObjectNode fnNode=(ObjectNode)call.with("function"); if(fn.has("name")) fnNode.put("name",fn.path("name").asText()); if(fn.has("arguments")) fnNode.put("arguments",fnNode.path("arguments").asText("")+fn.path("arguments").asText()); }
                }
            }
            }
            result.rawToolCalls=mapper.createArrayNode(); for(ObjectNode call:calls.values()) { result.rawToolCalls.add(call); result.toolCalls.add(call); }
            return result;
        } finally { connection.disconnect(); }
    }

    private ArrayNode toolDefinitions(boolean authenticated) {
        ArrayNode tools=mapper.createArrayNode();
        tools.add(tool("search_products","按名称或关键词搜索在售商品", "query", "string", true));
        if(authenticated) tools.add(tool("get_my_order_status","查询当前登录用户自己的订单状态；不可指定用户或订单号", "status", "integer", false));
        return tools;
    }
    private ObjectNode tool(String name,String description,String argName,String type,boolean required) {
        ObjectNode tool=mapper.createObjectNode(); tool.put("type","function"); ObjectNode fn=tool.putObject("function"); fn.put("name",name); fn.put("description",description); ObjectNode params=fn.putObject("parameters"); params.put("type","object"); ObjectNode props=params.putObject("properties"); props.putObject(argName).put("type",type).put("description",name.equals("search_products")?"商品搜索关键词":"可选订单状态：10待付款、20待发货、30待收货、40待评价、50完成、60取消"); ArrayNode requiredFields=params.putArray("required"); if(required) requiredFields.add(argName); params.put("additionalProperties",false); return tool;
    }
    String executeTool(String name, JsonNode args, LoginUser loginUser) {
        return executeToolResult(name, args, loginUser).content;
    }

    ToolResult executeToolResult(String name, JsonNode args, LoginUser loginUser) {
        if("search_products".equals(name)) {
            if (!onlyFields(args, "query") || !args.path("query").isTextual()) return ToolResult.failure("搜索参数无效。");
            String query=args.path("query").asText("").trim(); if(query.isEmpty() || query.length()>80) return ToolResult.failure("搜索词无效。");
            List<Product> found=productRepository.searchActiveByKeyword(query, PageRequest.of(0, 5)); StringBuilder b=new StringBuilder();
            List<Map<String, Object>> items = new ArrayList<>();
            for(int i=0;i<Math.min(found.size(),5);i++){
                Product p=found.get(i);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", p.getId()); item.put("name", safe(p.getName())); item.put("price", p.getPrice()); item.put("stock", p.getStock());
                items.add(item);
                b.append("商品ID ").append(p.getId()).append("，商品").append(safe(p.getName())).append("，价格").append(p.getPrice()).append("，库存").append(p.getStock()).append("；");
            }
            if (items.isEmpty()) return new ToolResult("没有找到在售商品。", null);
            Map<String, Object> display = new LinkedHashMap<>(); display.put("tool", "search_products"); display.put("items", items);
            return new ToolResult(b.toString(), display);
        }
        if("get_my_order_status".equals(name)) {
            if (!onlyFields(args, "status")) return ToolResult.failure("订单查询参数无效。");
            if (args.has("status") && !args.get("status").isNull()
                    && (!args.get("status").isIntegralNumber() || !args.get("status").canConvertToInt())) return ToolResult.failure("订单状态条件无效。");
            if(loginUser==null || loginUser.getUserId()==null) return ToolResult.failure("请登录后查询订单。");
            Integer status=args.hasNonNull("status")?args.path("status").asInt():null;
            if(status!=null && !Arrays.asList(10,20,30,40,50,60).contains(status)) return ToolResult.failure("订单状态条件无效。");
            List<OrderInfo> orders=orderInfoService.selectOrderListByUserId(loginUser.getUserId(),status); StringBuilder b=new StringBuilder();
            List<Map<String, Object>> items = new ArrayList<>();
            if(orders!=null) for(int i=0;i<Math.min(orders.size(),5);i++){
                OrderInfo o=orders.get(i); String masked = mask(o.getOrderNo()); String statusDesc = safe(o.getStatusDesc());
                b.append("订单尾号").append(masked).append("，状态").append(statusDesc).append("；");
                Map<String, Object> item = new LinkedHashMap<>(); item.put("orderNoMasked", masked); item.put("status", statusDesc); items.add(item);
            }
            if (items.isEmpty()) return new ToolResult("当前没有符合条件的订单。", null);
            Map<String, Object> display = new LinkedHashMap<>(); display.put("tool", "get_my_order_status"); display.put("items", items);
            return new ToolResult(b.toString(), display);
        }
        return ToolResult.failure("未授权的工具。");
    }
    private boolean onlyFields(JsonNode args, String... allowed) {
        if (args == null || !args.isObject()) return false;
        Set<String> names = new HashSet<>(Arrays.asList(allowed));
        Iterator<String> fields = args.fieldNames();
        while (fields.hasNext()) if (!names.contains(fields.next())) return false;
        return true;
    }
    private String mask(String orderNo) { if(orderNo==null)return "未知"; return orderNo.length()<=4?"****": "****"+orderNo.substring(orderNo.length()-4); }
    static class ToolResult {
        final String content;
        final Map<String, Object> display;
        final boolean success;
        ToolResult(String content, Map<String, Object> display) { this(content, display, true); }
        private ToolResult(String content, Map<String, Object> display, boolean success) {
            this.content = content; this.display = display; this.success = success;
        }
        static ToolResult failure(String content) { return new ToolResult(content, null, false); }
    }
    private static class StreamResult { final StringBuilder text=new StringBuilder(); final List<JsonNode> toolCalls=new ArrayList<>(); ArrayNode rawToolCalls; int promptTokens; int completionTokens; }
}
