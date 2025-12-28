package com.neuedu.system.demoai;

import com.neuedu.system.demoai.model.PriceCondition;
import com.neuedu.system.demoai.model.PriceExtractResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@ServerEndpoint("/ai/chat")
public class AIChatWebSocket {
    private static final Map<Session, Map<String, Object>> USER_CONTEXT = new ConcurrentHashMap<>();
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static com.neuedu.system.demoai.ZhipuAIService zhipuAIService;

    @Autowired
    public void setZhipuAIService(ZhipuAIService zhipuAIService) {
        AIChatWebSocket.zhipuAIService = zhipuAIService;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到用户{}消息：{}", session.getId(), message);

        try {
            ChatRequest request = objectMapper.readValue(message, ChatRequest.class);
            String userQuestion = request.getQuestion().trim();
            Long productId = request.getProductId();
            Long userId = request.getUserId(); // 新增：获取用户ID

            if (userQuestion.isEmpty()) {
                sendMessage(session, buildResponse("stream", "请问您想咨询什么问题呢？"));
                sendMessage(session, buildResponse("end", ""));
                return;
            }

            // 新增：处理订单查询请求
            if (zhipuAIService.isOrderQueryRequest(userQuestion)) {
                String response = zhipuAIService.handleOrderQuery(userQuestion, userId);
                sendMessage(session, buildResponse("stream", response));
                sendMessage(session, buildResponse("end", ""));
                return;
            }

            // 新增：专门处理商品价格查询
            if (isProductPriceQuery(userQuestion)) {
                String response = zhipuAIService.handleProductPriceQuery(userQuestion);
                sendMessage(session, buildResponse("stream", response));
                sendMessage(session, buildResponse("end", ""));
                return;
            }

            // 获取当前用户的上下文
            Map<String, Object> context = USER_CONTEXT.get(session);

            // 检查是否需要清除上下文：当用户明确指定新品牌或新类型时
            boolean shouldClearContext = shouldClearExistingContext(userQuestion, context);
            if (shouldClearContext && context != null) {
                log.info("清除旧上下文，用户查询：{}", userQuestion);
                context.clear();
                USER_CONTEXT.put(session, context);
            }

            // 处理价格筛选请求（如果有商品查询上下文）
            // 修改：使用ZhipuAIService中的方法来检查是否包含价格条件
            if (context != null && "product".equals(context.get("type")) &&
                    (zhipuAIService.containsPriceCondition(userQuestion) || containsPriceRange(userQuestion))) {

                // 关键修复：即使有上下文，也要重新提取品牌和类别
                Map<String, Object> currentContext = zhipuAIService.extractContextFromQuestion(userQuestion);
                String currentBrand = (String) currentContext.get("brand");
                List<Long> currentCategoryIds = (List<Long>) currentContext.get("categoryIds");

                // 如果当前问题中提取到了品牌，使用当前品牌
                if (currentBrand != null && !currentBrand.isEmpty()) {
                    context.put("brand", currentBrand);
                }

                // 如果当前问题中提取到了类别，使用当前类别
                if (currentCategoryIds != null && !currentCategoryIds.isEmpty()) {
                    context.put("categoryIds", currentCategoryIds);
                }

                // 使用ZhipuAIService统一提取价格信息
                PriceExtractResult priceResult = zhipuAIService.extractPriceInfo(userQuestion);
                // 如果提取不到单个价格，尝试提取价格区间
                if (priceResult == null && containsPriceRange(userQuestion)) {
                    priceResult = extractPriceRangeForContext(userQuestion);
                }

                if (priceResult != null) {
                    context.put("priceResult", priceResult);
                }

                String response = zhipuAIService.handleProductRecommendationWithContext(context, userQuestion);
                sendMessage(session, buildResponse("stream", response));
                sendMessage(session, buildResponse("end", ""));
                return;
            }

            // 处理商品推荐请求 - 修改：检查是否包含价格区间
            if (zhipuAIService.isProductRecommendationRequest(userQuestion) || containsPriceRange(userQuestion)) {
                String response = zhipuAIService.handleProductRecommendation(userQuestion);
                sendMessage(session, buildResponse("stream", response));
                sendMessage(session, buildResponse("end", ""));

                // 存储查询上下文 - 总是重新提取，而不是追加
                Map<String, Object> newContext = zhipuAIService.extractContextFromQuestion(userQuestion);
                newContext.put("type", "product");

                // 提取价格信息并存入上下文
                PriceExtractResult priceResult = zhipuAIService.extractPriceInfo(userQuestion);
                if (priceResult == null && containsPriceRange(userQuestion)) {
                    priceResult = extractPriceRangeForContext(userQuestion);
                }
                if (priceResult != null) {
                    newContext.put("priceResult", priceResult);
                }

                USER_CONTEXT.put(session, newContext);
                return;
            }
            // 处理其他类型的请求
            if (userQuestion.matches(".*(你好|您好|hello|hi).*")) {
                sendMessage(session, buildResponse("stream", "您好！我是智能客服，很高兴为您服务！"));
                sendMessage(session, buildResponse("end", ""));
                return;
            }

            if (userQuestion.matches(".*(发货|快递|物流|配送|多久到|什么时候到).*")) {
                sendMessage(session, buildResponse("stream", "一般情况下，商品会在下单后1-3个工作日内发货，物流时效为3-7天，具体时间以物流公司实际配送为准。"));
                sendMessage(session, buildResponse("end", ""));
                return;
            }

            if (userQuestion.matches(".*(退货|退款|售后|维修|质量).*")) {
                sendMessage(session, buildResponse("stream", "我们支持7天无理由退货，15天内质量问题可换货。具体售后政策请查看商品详情页的售后说明，或联系客服处理。"));
                sendMessage(session, buildResponse("end", ""));
                return;
            }

            // 原有AI调用逻辑
            String knowledge = zhipuAIService.getEcommerceKnowledge(userQuestion, productId);
            zhipuAIService.streamChat(userQuestion, knowledge, content -> {
                sendMessage(session, buildResponse("stream", content));
            });
            sendMessage(session, buildResponse("end", ""));

        } catch (Exception e) {
            log.error("处理用户消息失败", e);
            sendMessage(session, buildResponse("stream", "非常抱歉，系统暂时出错，请稍后再试～"));
            sendMessage(session, buildResponse("end", ""));
        }
    }
    // 判断是否为商品价格查询
    private boolean isProductPriceQuery(String question) {
        String lowerQuestion = question.toLowerCase();

        // 排除通用的推荐询问
        if (lowerQuestion.matches(".*(推荐|有没有|想买|想要|咨询|介绍|了解|想了解|看看|看下|看|找|寻找).*(衣服|服饰|连衣裙|西装|大衣|婚纱|礼服|上衣|裤子|裙子|T恤|衬衫).*") &&
                !lowerQuestion.contains("多少钱") &&
                !lowerQuestion.contains("什么价") &&
                !lowerQuestion.contains("价格") &&
                !lowerQuestion.contains("价钱") &&
                !lowerQuestion.contains("价") &&
                !lowerQuestion.contains("多钱") &&
                !lowerQuestion.contains("卖多少")) {
            return false;
        }

        // 包含价格查询关键词
        String[] priceWords = {"多少钱", "什么价", "价格", "价钱", "价", "多钱", "卖多少", "价格是多少"};

        for (String priceWord : priceWords) {
            if (lowerQuestion.contains(priceWord)) {
                return true;
            }
        }

        // 或者包含商品相关词汇且包含特定查询词
        String[] productWords = {
                "华为", "小米", "苹果", "三星", "联想", "索尼",
                "笔记本", "电脑", "电视", "空调", "衣服", "连衣裙",
                "西装", "大衣", "手机", "iPhone", "iPad", "MacBook"
        };

        // 只有当同时包含商品词汇和"什么"、"多少"、"什么价"等查询词时才认为是价格查询
        boolean hasProductWord = false;
        for (String productWord : productWords) {
            if (lowerQuestion.contains(productWord)) {
                hasProductWord = true;
                break;
            }
        }

        // 同时包含商品词汇和查询词才认为是价格查询
        String[] queryWords = {"什么", "多少", "价", "价格", "价钱", "多钱"};
        boolean hasQueryWord = false;
        for (String queryWord : queryWords) {
            if (lowerQuestion.contains(queryWord)) {
                hasQueryWord = true;
                break;
            }
        }

        return hasProductWord && hasQueryWord;
    }

    // 日志记录方法 - 加在 AIChatWebSocket 类中
    private void logContextInfo(String operation, Session session, Map<String, Object> context, String userQuestion) {
        if (context != null) {
            log.info("{} - 用户{}上下文: 品牌={}, 类型={}, 价格条件={}, 问题={}",
                    operation,
                    session.getId(),
                    context.get("brand"),
                    context.get("type"),
                    context.get("priceResult") != null ? context.get("priceResult") : "无",
                    userQuestion);
        } else {
            log.info("{} - 用户{}无上下文, 问题={}",
                    operation,
                    session.getId(),
                    userQuestion);
        }
    }


    // 新增：判断是否需要清除现有上下文
    private boolean shouldClearExistingContext(String userQuestion, Map<String, Object> context) {
        if (context == null) {
            return false;
        }

        // 提取当前问题中的品牌
        String currentBrand = extractBrandFromQuestion(userQuestion);

        // 如果上下文中已有品牌，但当前问题指定了不同的品牌，需要清除上下文
        String contextBrand = (String) context.get("brand");
        if (contextBrand != null && !contextBrand.isEmpty() &&
                currentBrand != null && !currentBrand.isEmpty() &&
                !contextBrand.equals(currentBrand)) {
            return true;
        }

        // 如果用户明确要求新的查询类型（如从手机切换到衣服）
        if (isNewQueryType(userQuestion, context)) {
            return true;
        }

        return false;
    }

    // 新增：从问题中提取品牌（简化版本）
    private String extractBrandFromQuestion(String question) {
        // 转换为小写进行匹配
        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("苹果") || lowerQuestion.contains("apple") || lowerQuestion.contains("iphone")) {
            return "苹果";
        } else if (lowerQuestion.contains("小米") || lowerQuestion.contains("mi") || lowerQuestion.contains("红米")) {
            return "小米";
        } else if (lowerQuestion.contains("华为") || lowerQuestion.contains("huawei")) {
            return "华为";
        } else if (lowerQuestion.contains("三星") || lowerQuestion.contains("samsung")) {
            return "三星";
        }
        // 可以添加更多品牌...

        return "";
    }

    // 新增：判断是否是新查询类型
    private boolean isNewQueryType(String userQuestion, Map<String, Object> context) {
        String lowerQuestion = userQuestion.toLowerCase();

        // 如果上下文是商品查询，但用户问的是发货、售后等通用问题
        if ("product".equals(context.get("type"))) {
            if (lowerQuestion.matches(".*(发货|快递|物流|配送|多久到|什么时候到).*") ||
                    lowerQuestion.matches(".*(退货|退款|售后|维修|质量).*") ||
                    lowerQuestion.matches(".*(你好|您好|hello|hi).*")) {
                return true;
            }
        }

        return false;
    }

    // 新增：检查是否包含价格区间
    private boolean containsPriceRange(String question) {
        // 匹配"500到1000"、"500-1000"、"500~1000"等模式
        Pattern rangePattern = Pattern.compile("(\\d+)\\s*(元|块|块钱)?\\s*(-|到|至|~)\\s*(\\d+)\\s*(元|块|块钱)?");
        return rangePattern.matcher(question).find();
    }

    // 新增：为上下文提取价格区间
    private PriceExtractResult extractPriceRangeForContext(String question) {
        Map<String, Double> range = extractPriceRangeFromQuestion(question);
        if (range != null) {
            try {
                // 创建PriceExtractResult对象
                PriceExtractResult result = new PriceExtractResult();
                result.setMinPrice(range.get("min"));
                result.setMaxPrice(range.get("max"));
                result.setCondition(PriceCondition.RANGE);
                return result;
            } catch (Exception e) {
                log.error("创建价格区间对象失败", e);
            }
        }
        return null;
    }

    // 修改现有的extractPriceRangeFromQuestion方法，使其更健壮
    private Map<String, Double> extractPriceRangeFromQuestion(String question) {
        // 增强正则表达式，支持多种格式
        Pattern rangePattern = Pattern.compile(
                "(\\d+)\\s*(元|块|块钱)?\\s*(-|到|至|~)\\s*(\\d+)\\s*(元|块|块钱)?"
        );
        Matcher matcher = rangePattern.matcher(question);

        if (matcher.find()) {
            try {
                double min = Double.parseDouble(matcher.group(1).trim());
                double max = Double.parseDouble(matcher.group(4).trim());

                // 确保最小值不大于最大值
                if (min > max) {
                    double temp = min;
                    min = max;
                    max = temp;
                }

                Map<String, Double> range = new HashMap<>();
                range.put("min", min);
                range.put("max", max);
                return range;
            } catch (NumberFormatException e) {
                log.error("解析价格区间失败", e);
                return null;
            }
        }
        return null;
    }



    // 发送消息
    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    // 构建响应消息
    private String buildResponse(String type, String content) {
        try {
            Map<String, String> response = new java.util.HashMap<>();
            response.put("type", type);
            response.put("content", content);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("构建响应失败", e);
            return "{\"type\":\"error\",\"content\":\"系统错误\"}";
        }
    }

    @OnClose
    public void onClose(Session session) {
        log.info("用户断开连接：{}", session.getId());
        USER_CONTEXT.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误", error);
    }

    // 内部类：聊天请求
    public static class ChatRequest {
        private String question;
        private Long productId;
        private Long userId; // 新增：用户ID字段

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/ai/**")
//                        .allowedOrigins("*")
//                        .allowedMethods("*")
//                        .allowedHeaders("*")
//                        .allowCredentials(false)
//                        .maxAge(3600);
//            }
//        };
//    }



}
