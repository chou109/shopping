package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import com.neuedu.system.demoai.entity.Product;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.LongAdder;

/** Gogo (逛逛) assistant: DeepSeek chat, retrieval and allow-listed business tools. */
@Slf4j
@Service
public class GogoAssistantService {
    private final ObjectMapper mapper;
    private final FAQRepository faqRepository;
    private final ProductRepository productRepository;
    private final IOrderInfoService orderInfoService;
    private final GogoPromptFactory promptFactory;

    @Value("${ai.gogo.api-key:}") private String apiKey;
    @Value("${ai.gogo.base-url:https://api.deepseek.com}") private String baseUrl;
    @Value("${ai.gogo.model:deepseek-flash}") private String model;
    @Value("${ai.gogo.connect-timeout-ms:5000}") private int connectTimeout;
    @Value("${ai.gogo.read-timeout-ms:45000}") private int readTimeout;
    @Value("${ai.gogo.input-price-per-million:0}") private BigDecimal inputPricePerMillion;
    @Value("${ai.gogo.output-price-per-million:0}") private BigDecimal outputPricePerMillion;
    private final LongAdder requestCount = new LongAdder();
    private final LongAdder retrievalHitCount = new LongAdder();
    private final LongAdder toolCallCount = new LongAdder();
    private final LongAdder toolFailureCount = new LongAdder();
    private final LongAdder totalDurationMs = new LongAdder();
    private final LongAdder promptTokenCount = new LongAdder();
    private final LongAdder completionTokenCount = new LongAdder();
    private final java.util.concurrent.atomic.DoubleAdder estimatedCost = new java.util.concurrent.atomic.DoubleAdder();

    public GogoAssistantService(ObjectMapper mapper, FAQRepository faqRepository, ProductRepository productRepository,
                              IOrderInfoService orderInfoService, GogoPromptFactory promptFactory) {
        this.mapper = mapper;
        this.faqRepository = faqRepository;
        this.productRepository = productRepository;
        this.orderInfoService = orderInfoService;
        this.promptFactory = promptFactory;
    }

    public void answer(String question, Long productId, LoginUser loginUser, List<ObjectNode> history,
                       Consumer<String> onToken, Consumer<List<Map<String, Object>>> onSources,
                       Consumer<Boolean> onHandoff) {
        long started = System.currentTimeMillis();
        List<Map<String, Object>> sources = retrieve(question, productId);
        onSources.accept(sources);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            onToken.accept("逛逛的模型服务尚未配置，已为您转接人工客服。您也可以先查看商品详情页或登录后的订单页。");
            onHandoff.accept(true);
            long duration = System.currentTimeMillis() - started;
            recordMetrics(!sources.isEmpty(), 0, 0, 0, 0, duration);
            log.info("ai_chat model_configured=false retrieval_hits={} duration_ms={}", sources.size(), duration);
            return;
        }

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", promptFactory.create(sources, loginUser != null));
        for (ObjectNode prior : history) messages.add(prior.deepCopy());
        ObjectNode user = messages.addObject(); user.put("role", "user"); user.put("content", question);

        int toolCount = 0;
        int toolFailures = 0;
        int promptTokens = 0;
        int completionTokens = 0;
        boolean handoff = false;
        StringBuilder finalAnswer = new StringBuilder();
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
                toolCount += result.toolCalls.size();
                ObjectNode assistantCall = mapper.createObjectNode(); assistantCall.put("role", "assistant");
                assistantCall.set("tool_calls", result.rawToolCalls);
                messages.add(assistantCall);
                for (JsonNode call : result.toolCalls) {
                    String id = call.path("id").asText("");
                    String name = call.path("function").path("name").asText("");
                    String args = call.path("function").path("arguments").asText("{}");
                    String toolResult;
                    try { toolResult = executeTool(name, mapper.readTree(args), loginUser); }
                    catch (Exception ex) { toolFailures++; toolResult = "查询失败，请稍后重试或联系人工客服。"; }
                    ObjectNode tool = messages.addObject(); tool.put("role", "tool"); tool.put("tool_call_id", id); tool.put("content", toolResult);
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
        recordMetrics(!sources.isEmpty(), toolCount, toolFailures, promptTokens, completionTokens, duration);
        log.info("ai_chat model={} retrieval_hits={} tool_calls={} tool_failures={} prompt_tokens={} completion_tokens={} estimated_cost={} duration_ms={} handoff={}",
                model, sources.size(), toolCount, toolFailures, promptTokens, completionTokens, estimatedCost(promptTokens, completionTokens), duration, handoff);
        history.add(user.deepCopy());
        ObjectNode answer = mapper.createObjectNode(); answer.put("role", "assistant"); answer.put("content", finalAnswer.toString()); history.add(answer);
        while (history.size() > 12) history.remove(0);
    }

    private void recordMetrics(boolean retrievalHit, int tools, int toolFailures, int promptTokens, int completionTokens, long duration) {
        requestCount.increment(); if (retrievalHit) retrievalHitCount.increment();
        toolCallCount.add(tools); toolFailureCount.add(toolFailures); totalDurationMs.add(duration);
        promptTokenCount.add(promptTokens); completionTokenCount.add(completionTokens);
        estimatedCost.add(estimatedCost(promptTokens, completionTokens).doubleValue());
    }

    private BigDecimal estimatedCost(int promptTokens, int completionTokens) {
        BigDecimal input = inputPricePerMillion == null ? BigDecimal.ZERO : inputPricePerMillion;
        BigDecimal output = outputPricePerMillion == null ? BigDecimal.ZERO : outputPricePerMillion;
        return input.multiply(BigDecimal.valueOf(promptTokens)).add(output.multiply(BigDecimal.valueOf(completionTokens)))
                .divide(BigDecimal.valueOf(1000000), 8, RoundingMode.HALF_UP);
    }

    public Map<String, Object> metricsSnapshot() {
        long requests = requestCount.sum(); long calls = toolCallCount.sum(); long failures = toolFailureCount.sum();
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("requests", requests);
        metrics.put("retrievalHitRate", requests == 0 ? 0D : (double) retrievalHitCount.sum() / requests);
        metrics.put("toolCalls", calls);
        metrics.put("toolSuccessRate", calls == 0 ? 0D : (double) (calls - failures) / calls);
        metrics.put("averageResponseMs", requests == 0 ? 0D : (double) totalDurationMs.sum() / requests);
        metrics.put("promptTokens", promptTokenCount.sum());
        metrics.put("completionTokens", completionTokenCount.sum());
        metrics.put("estimatedCost", BigDecimal.valueOf(estimatedCost.sum()).setScale(8, RoundingMode.HALF_UP));
        return metrics;
    }

    private List<Map<String, Object>> retrieve(String question, Long productId) {
        List<Map<String, Object>> docs = new ArrayList<>();
        Set<String> terms = terms(question);
        try {
            List<FrequentlyAskedQuestion> faqs = faqRepository.findForAssistant(productId, PageRequest.of(0, 200));
            for (FrequentlyAskedQuestion faq : faqs) {
                String text = safe(faq.getQuestion()) + " " + safe(faq.getAnswer());
                int score = score(terms, text);
                if (relevant(terms, score)) {
                    String sourceType = safe(faq.getSourceType()).isEmpty() ? "FAQ" : faq.getSourceType().toLowerCase(Locale.ROOT);
                    docs.add(doc(sourceType.toUpperCase(Locale.ROOT) + " #" + faq.getId() + "：" + safe(faq.getQuestion()),
                            safe(faq.getAnswer()), score, sourceType, faq.getId()));
                }
            }
        } catch (Exception ex) { log.debug("FAQ retrieval unavailable: {}", ex.getClass().getSimpleName()); }
        try {
            List<Product> products = terms.isEmpty() ? new ArrayList<>() : new ArrayList<>(productRepository.searchActiveByKeyword(question, PageRequest.of(0, 5)));
            if (productId != null) {
                Product selected = productRepository.findByIdAndDeleteFlagAndStatus(productId, 0, 1);
                if (selected != null && products.stream().noneMatch(p -> Objects.equals(p.getId(), selected.getId()))) products.add(selected);
            }
            for (Product p : products) {
                String content = "商品：" + p.getName() + "；简介：" + p.getSubTitle() + "；价格：" + p.getPrice() + "；库存：" + p.getStock();
                int relevance = score(terms, content);
                boolean explicitlySelected = productId != null && Objects.equals(productId, p.getId());
                if (relevant(terms, relevance) || explicitlySelected) docs.add(doc("商品 #" + p.getId() + "：" + p.getName(), content, relevance, "product", p.getId()));
            }
        } catch (Exception ex) { log.debug("Product retrieval unavailable: {}", ex.getClass().getSimpleName()); }
        docs.sort((a,b) -> Integer.compare((Integer)b.get("score"), (Integer)a.get("score")));
        if (docs.size() > 5) docs = new ArrayList<>(docs.subList(0, 5));
        for (Map<String,Object> doc : docs) doc.remove("score");
        return docs;
    }
    private Map<String,Object> doc(String title, String content, int score, String sourceType, Long sourceId) { Map<String,Object> d = new LinkedHashMap<>(); d.put("title",title); d.put("content",content); d.put("sourceType",sourceType); d.put("sourceId",sourceId); d.put("score",score); return d; }
    private Set<String> terms(String text) { Set<String> out = new LinkedHashSet<>(); if (text == null) return out; for (String w : text.toLowerCase(Locale.ROOT).split("[^\\p{IsHan}a-z0-9]+")) if (w.length()>1) out.add(w); for (int i=0;i+1<text.length();i++) if (Character.UnicodeScript.of(text.charAt(i))==Character.UnicodeScript.HAN && Character.UnicodeScript.of(text.charAt(i+1))==Character.UnicodeScript.HAN) out.add(text.substring(i,i+2)); return out; }
    private int score(Set<String> terms, String text) { int n=0; String lower=text.toLowerCase(Locale.ROOT); for(String term:terms) if(lower.contains(term)) n++; return n; }
    private boolean relevant(Set<String> terms, int matchedTerms) {
        if (terms.isEmpty() || matchedTerms == 0) return false;
        int minimumMatches = Math.min(2, terms.size());
        return matchedTerms >= minimumMatches;
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
        byte[] payload = mapper.writeValueAsBytes(body); connection.getOutputStream().write(payload);
        int status = connection.getResponseCode();
        InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (status >= 400) { if (input != null) try(BufferedReader r=new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) { while(r.readLine()!=null){} } throw new IllegalStateException("model_http_"+status); }
        StreamResult result = new StreamResult(); Map<Integer,ObjectNode> calls = new TreeMap<>();
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
        } finally { connection.disconnect(); }
        result.rawToolCalls=mapper.createArrayNode(); for(ObjectNode call:calls.values()) { result.rawToolCalls.add(call); result.toolCalls.add(call); }
        return result;
    }

    private ArrayNode toolDefinitions(boolean authenticated) {
        ArrayNode tools=mapper.createArrayNode();
        tools.add(tool("search_products","按名称或关键词搜索在售商品", "query", "string"));
        if(authenticated) tools.add(tool("get_my_order_status","查询当前登录用户自己的订单状态；不可指定用户或订单号", "status", "integer"));
        return tools;
    }
    private ObjectNode tool(String name,String description,String argName,String type) {
        ObjectNode tool=mapper.createObjectNode(); tool.put("type","function"); ObjectNode fn=tool.putObject("function"); fn.put("name",name); fn.put("description",description); ObjectNode params=fn.putObject("parameters"); params.put("type","object"); ObjectNode props=params.putObject("properties"); props.putObject(argName).put("type",type).put("description",name.equals("search_products")?"商品搜索关键词":"可选订单状态：10待付款、20待发货、30待收货、40待评价、50完成、60取消"); params.putArray("required"); params.put("additionalProperties",false); return tool;
    }
    String executeTool(String name, JsonNode args, LoginUser loginUser) {
        if("search_products".equals(name)) {
            if (!onlyFields(args, "query") || !args.path("query").isTextual()) return "搜索参数无效。";
            String query=args.path("query").asText("").trim(); if(query.isEmpty() || query.length()>80) return "搜索词无效。";
            List<Product> found=productRepository.searchActiveByKeyword(query, PageRequest.of(0, 5)); StringBuilder b=new StringBuilder();
            for(int i=0;i<Math.min(found.size(),5);i++){ Product p=found.get(i); b.append("商品").append(p.getName()).append("，价格").append(p.getPrice()).append("，库存").append(p.getStock()).append("；"); }
            return b.length()==0?"没有找到在售商品。":b.toString();
        }
        if("get_my_order_status".equals(name)) {
            if (!onlyFields(args, "status")) return "订单查询参数无效。";
            if (args.has("status") && !args.get("status").isNull()
                    && (!args.get("status").isIntegralNumber() || !args.get("status").canConvertToInt())) return "订单状态条件无效。";
            if(loginUser==null || loginUser.getUserId()==null) return "请登录后查询订单。";
            Integer status=args.hasNonNull("status")?args.path("status").asInt():null;
            if(status!=null && !Arrays.asList(10,20,30,40,50,60).contains(status)) return "订单状态条件无效。";
            List<OrderInfo> orders=orderInfoService.selectOrderListByUserId(loginUser.getUserId(),status); StringBuilder b=new StringBuilder();
            if(orders!=null) for(int i=0;i<Math.min(orders.size(),5);i++){ OrderInfo o=orders.get(i); b.append("订单尾号").append(mask(o.getOrderNo())).append("，状态").append(safe(o.getStatusDesc())).append("；"); }
            return b.length()==0?"当前没有符合条件的订单。":b.toString();
        }
        return "未授权的工具。";
    }
    private boolean onlyFields(JsonNode args, String... allowed) {
        if (args == null || !args.isObject()) return false;
        Set<String> names = new HashSet<>(Arrays.asList(allowed));
        Iterator<String> fields = args.fieldNames();
        while (fields.hasNext()) if (!names.contains(fields.next())) return false;
        return true;
    }
    private String mask(String orderNo) { if(orderNo==null)return "未知"; return orderNo.length()<=4?"****": "****"+orderNo.substring(orderNo.length()-4); }
    private static class StreamResult { final StringBuilder text=new StringBuilder(); final List<JsonNode> toolCalls=new ArrayList<>(); ArrayNode rawToolCalls; int promptTokens; int completionTokens; }
}
