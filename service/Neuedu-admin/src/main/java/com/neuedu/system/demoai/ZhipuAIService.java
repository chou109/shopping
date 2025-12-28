package com.neuedu.system.demoai;

import com.neuedu.system.demoai.entity.Product;
import com.neuedu.system.demoai.model.PriceCondition;
import com.neuedu.system.demoai.model.PriceExtractResult;
import com.neuedu.system.demoai.service.CategoryService;
import com.neuedu.system.demoai.service.ProductService;
import com.neuedu.system.demoai.service.OrderService;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.OrderDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Slf4j
@Service
public class ZhipuAIService {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 记录业务逻辑调试信息
    private void logBusinessInfo(String operation, Map<String, Object> context, String userQuestion) {
        if (context != null) {
            log.debug("{} - 业务逻辑上下文: 品牌={}, 价格条件={}, 问题={}",
                    operation,
                    context.get("brand"),
                    context.get("priceResult") != null ? context.get("priceResult") : "无",
                    userQuestion);
        } else {
            log.debug("{} - 业务逻辑无上下文, 问题={}", operation, userQuestion);
        }
    }

    // 获取电商知识库信息
    public String getEcommerceKnowledge(String userQuestion, Long productId) {
        StringBuilder knowledge = new StringBuilder();

        if (productId != null) {
            knowledge.append("用户咨询了商品ID为").append(productId).append("的商品。");
        }

        knowledge.append("\n电商平台知识：")
                .append("\n1. 商品分类：手机数码、家用办公、精品服饰、珠宝饰品、美妆护肤、生鲜绿植")
                .append("\n2. 发货时效：下单后1-3个工作日内发货，物流时效3-7天")
                .append("\n3. 售后政策：7天无理由退货，15天内质量问题可换货")
                .append("\n4. 支付方式：支持支付宝、微信支付、银行卡支付")
                .append("\n5. 运费政策：全场满99元包邮，不满99元收取8元运费");

        return knowledge.toString();
    }

    // 流式聊天 - 模拟流式输出
    public void streamChat(String userQuestion, String knowledge, Consumer<String> callback) {
        try {
            String response = processLocalChat(userQuestion, knowledge);

            int chunkSize = 10;
            for (int i = 0; i < response.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, response.length());
                String chunk = response.substring(i, end);
                callback.accept(chunk);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (Exception e) {
            log.error("流式聊天异常", e);
            callback.accept("抱歉，处理您的请求时出现错误。");
        }
    }

    // 判断是否为价格筛选请求
    public boolean isPriceFilterRequest(String question) {
        return containsPriceCondition(question);
    }

    // 检查是否包含价格条件
    public boolean containsPriceCondition(String question) {
        // 更精确的匹配
        Pattern pricePattern = Pattern.compile(
                "(\\d+)\\s*(元|块钱)?\\s*(以上|及以上|左右|以下|以内|之内)(\\s*的)?" +
                        "|(\\d+)\\s*(-|到|至|~)\\s*(\\d+)\\s*(元|块钱)?" +
                        "|(价格|价钱|价位).*(\\d+)(元|块钱)?(以上|以下|左右|以内|之内)" +
                        "|(\\d+)\\s*(元|块钱)?(以上|以下|左右|以内|之内).*"
        );

        return pricePattern.matcher(question).find();
    }

    // 本地聊天处理逻辑
    private String processLocalChat(String userQuestion, String knowledge) {
        String lowerQuestion = userQuestion.toLowerCase();

        if (lowerQuestion.matches(".*(你好|您好|hello|hi|早上好|下午好|晚上好).*")) {
            return "您好！我是智能客服，很高兴为您服务！有什么可以帮您的吗？";
        }

        if (lowerQuestion.matches(".*(发货|快递|物流|配送|多久到|什么时候到|几天到).*")) {
            return "一般情况下，商品会在下单后1-3个工作日内发货，物流时效为3-7天，具体时间以物流公司实际配送为准。";
        }

        if (lowerQuestion.matches(".*(退货|退款|售后|维修|质量|坏了|破损).*")) {
            return "我们支持7天无理由退货，15天内质量问题可换货。具体售后政策请查看商品详情页的售后说明，或联系客服处理。";
        }

        if (lowerQuestion.matches(".*(支付|付款|怎么付|怎么买|如何购买).*")) {
            return "我们支持多种支付方式：支付宝、微信支付、银行卡支付。您可以选择方便的方式进行支付。";
        }

        if (lowerQuestion.matches(".*(优惠|打折|促销|活动|便宜|降价).*")) {
            return "目前平台有满减、折扣等多种优惠活动，具体活动详情请查看商品页面或活动专区。";
        }

        // 处理价格查询请求
        if (isPriceQueryRequest(userQuestion)) {
            return handlePriceQuery(userQuestion);
        }

        // 处理商品推荐请求
        if (isProductRecommendationRequest(userQuestion)) {
            return handleProductRecommendation(userQuestion);
        }

        return "感谢您的咨询！我主要可以帮助您：\n" +
                "1. 商品推荐和查询\n" +
                "2. 价格和优惠信息\n" +
                "3. 发货和物流信息\n" +
                "4. 售后和退换货政策\n" +
                "5. 支付方式咨询\n" +
                "请告诉我您具体想了解什么，我会尽力为您解答。";
    }

    /**
     * 专门处理商品价格查询
     */
    public String handleProductPriceQuery(String userQuestion) {
        log.info("处理商品价格查询：{}", userQuestion);

        // 1. 尝试提取商品名称
        String productName = extractProductNameForQuery(userQuestion);

        if (productName != null && !productName.isEmpty()) {
            log.info("提取到商品名称：{}", productName);

            // 2. 尝试精确查询
            Product product = productService.findProductByExactName(productName);
            if (product != null) {
                return buildProductPriceResponse(product);
            }

            // 3. 如果精确查询失败，尝试模糊查询
            List<Product> products = productService.findProductsByNameContaining(productName);
            if (!products.isEmpty()) {
                if (products.size() == 1) {
                    // 只有一个匹配结果
                    return buildProductPriceResponse(products.get(0));
                } else {
                    // 多个匹配结果，返回列表
                    return buildMultipleProductsResponse(products, productName);
                }
            }

            // 4. 尝试更广泛的搜索
            products = productService.searchProducts(productName);
            if (!products.isEmpty()) {
                if (products.size() == 1) {
                    return buildProductPriceResponse(products.get(0));
                } else {
                    return buildSearchResultsResponse(products, productName);
                }
            }

            // 5. 尝试多个关键词搜索
            List<String> keywords = extractKeywordsFromQuestion(userQuestion);
            if (!keywords.isEmpty()) {
                products = productService.searchProductsByKeywords(keywords);
                if (!products.isEmpty()) {
                    return buildSearchResultsResponse(products, String.join("、", keywords));
                }
            }

            return String.format("抱歉，没有找到【%s】的相关商品信息。", productName);
        }

        // 6. 如果提取不到商品名称，使用原有的价格查询逻辑
        return handlePriceQuery(userQuestion);
    }

    /**
     * 优化商品名称提取（针对您数据库的实际情况）
     */
    private String extractProductNameForQuery(String question) {
        if (question == null || question.trim().isEmpty()) {
            return null;
        }

        // 清理问题文本
        String cleaned = question
                .replaceAll("请问", "")
                .replaceAll("我想知道", "")
                .replaceAll("查询", "")
                .replaceAll("查一下", "")
                .replaceAll("多少钱", "")
                .replaceAll("什么价", "")
                .replaceAll("价格", "")
                .replaceAll("价钱", "")
                .replaceAll("价", "")
                .replaceAll("多钱", "")
                .replaceAll("卖多少", "")
                .replaceAll("价格是多少", "")
                .replaceAll("元", "")
                .replaceAll("\\?", "")
                .replaceAll("？", "")
                .replaceAll("呢", "")
                .replaceAll("吗", "")
                .replaceAll("啊", "")
                .trim();

        // 尝试提取完整的商品名称（基于您数据库中的实际名称）

        // 直接匹配数据库中的常见商品名称模式
        String[] commonPatterns = {
                // 华为手机
                "华为.*麦芒5", "华为.*畅享6", "华为.*畅享7", "华为.*麦芒5",
                // 小米手机
                "小米.*5C", "小米.*红米.*4A", "小米.*红米.*Note.*4X", "小米.*5X", "小米.*MIX2",
                // 三星手机
                "三星.*Galaxy.*C5.*Pro", "三星.*Galaxy.*C7.*Pro", "三星.*Galaxy.*J3",
                "三星.*Galaxy.*On5", "三星.*Galaxy.*S6",
                // 苹果手机 - 增强iPhone型号匹配
                "苹果.*iPhone.*6", "苹果.*iPhone.*6.*plus", "苹果.*iPhone.*6s",
                "苹果.*iPhone.*6s.*Plus", "苹果.*iPhone.*6s.*plus",  // 新增6s Plus匹配
                "苹果.*iPhone.*7", "苹果.*iPhone.*7.*Plus",
                "苹果.*iPhone.*8", "苹果.*iPhone.*8.*Plus", "苹果.*iPhone.*X", "苹果.*iPhone.*SE",
                // 电脑
                "联想.*小新笔记本", "小米.*笔记本", "苹果.*MacBook.*Pro",
                "苹果.*MacBook.*Air", "苹果.*MacBook",
                // 电视
                "海信.*LED50MU7000U", "康佳.*S55U", "三星.*UA55KS7300JXXZ",
                "索尼.*KD-75X8566E", "索尼.*KD-6575Z9D", "先锋.*LED-55U560P",
                "小米.*L65M4-AQ", "LG.*43LG63CJ-CA", "LG.*65UJ6500-CB", "TCL.*D55A630U",
                // 空调
                "格力.*家用空调", "海尔.*家用空调", "海信.*家用空调",
                "美的.*家用空调", "TCL.*家用空调",
                // 服饰 - 提取品牌部分
                "普莱诗.*婚纱", "玖世新娘.*礼服", "蜜之姻.*婚纱", "都姿.*礼服",
                "蜜姿娅.*礼服", "情海银纱.*礼服", "雅磊.*礼服", "依贝缇.*礼服",
                "迪尔绒莎.*连衣裙", "鄂尔多斯.*连衣裙", "颈袖添香.*连衣裙",
                "拉谷谷.*连衣裙", "拉夏贝尔.*连衣裙", "朗姿.*连衣裙",
                "南极人.*连衣裙", "伲嘉.*连衣裙", "糖力.*连衣裙",
                "未风.*连衣裙", "颜域.*连衣裙", "Five.*Plus.*连衣裙",
                "ONLY.*连衣裙", "snidel.*连衣裙",
                // 鞋履
                "达芙妮.*高跟鞋", "妙龄妙品.*高跟鞋", "欧蝶莱.*高跟鞋",
                "他她.*高跟鞋", "烫.*高跟鞋", "王菲儿.*高跟鞋",
                "舞魅.*高跟鞋", "CHEERSHOW.*高跟鞋", "LUKECSION.*高跟鞋",
                "Luxury.*Rebel.*高跟鞋"
        };

        for (String pattern : commonPatterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(cleaned);
            if (matcher.find()) {
                // 提取匹配到的完整商品名称
                String matched = matcher.group();
                log.info("匹配到商品名称模式：{} -> {}", pattern, matched);
                return matched;
            }
        }

        // 如果没有匹配到模式，尝试提取品牌+型号
        String extracted = extractBrandAndModel(cleaned);
        if (extracted != null && !extracted.isEmpty()) {
            return extracted;
        }

        // 最后返回清理后的文本（前50个字符）
        return cleaned.length() > 50 ? cleaned.substring(0, 50) : cleaned;
    }

    /**
     * 提取品牌和型号
     */
    private String extractBrandAndModel(String text) {
        // 华为产品
        if (text.contains("华为")) {
            if (text.contains("麦芒5")) return "华为(HUAWEI) 麦芒5";
            if (text.contains("畅享6")) return "华为(HUAWEI) 畅享6";
            if (text.contains("畅享7")) return "华为(HUAWEI) 畅享7";
        }

        // 小米产品
        if (text.contains("小米")) {
            if (text.contains("5C")) return "小米(MI) 5C";
            if (text.contains("红米4A")) return "小米(MI) 红米 4A";
            if (text.contains("红米Note4X") || text.contains("红米 Note 4X"))
                return "小米(MI) 红米 Note 4X";
            if (text.contains("5X")) return "小米(MI) 小米 5X";
            if (text.contains("MIX2")) return "小米(MI) MIX2";
        }

        // 苹果产品 - 增强iPhone型号识别
        if (text.contains("苹果") || text.contains("iPhone")) {
            // 先检查6s Plus
            if (text.contains("iPhone6sPlus") || text.contains("iPhone 6s Plus") ||
                    text.contains("iPhone6s plus") || text.contains("6s Plus") ||
                    text.contains("6s plus")) {
                return "苹果(Apple) iPhone 6s Plus";
            }
            // 然后检查6s
            if (text.contains("iPhone6s") || text.contains("iPhone 6s") || text.contains("6s")) {
                return "苹果(Apple) iPhone 6s";
            }
            // 然后检查6 Plus
            if (text.contains("iPhone6") || text.contains("iPhone 6")) {
                if (text.contains("plus") || text.contains("Plus") || text.contains("6 plus")) {
                    return "苹果(Apple) iPhone 6 plus";
                }
                return "苹果(Apple) iPhone 6";
            }
            // 检查7系列
            if (text.contains("iPhone7") || text.contains("iPhone 7")) {
                if (text.contains("plus") || text.contains("Plus")) {
                    return "苹果(Apple) iPhone 7 Plus";
                }
                return "苹果(Apple) iPhone 7";
            }
            // 检查8系列
            if (text.contains("iPhone8") || text.contains("iPhone 8")) {
                if (text.contains("plus") || text.contains("Plus")) {
                    return "苹果(Apple) iPhone 8 Plus";
                }
                return "苹果(Apple) iPhone 8";
            }
            // 其他iPhone型号
            if (text.contains("iPhoneX") || text.contains("iPhone X")) {
                return "苹果(Apple) iPhone X";
            }
            if (text.contains("iPhoneSE") || text.contains("iPhone SE")) {
                return "苹果(Apple) iPhone SE";
            }
            // 通用苹果手机匹配
            if (text.contains("苹果手机") || text.contains("苹果 手机") || text.contains("iPhone")) {
                return "苹果(Apple) iPhone";
            }
        }

        return null;
    }

    /**
     * 提取关键词列表
     */
    private List<String> extractKeywordsFromQuestion(String question) {
        List<String> keywords = new ArrayList<>();
        String cleaned = question.toLowerCase();

        // 品牌关键词
        if (cleaned.contains("华为") || cleaned.contains("huawei")) keywords.add("华为");
        if (cleaned.contains("小米") || cleaned.contains("mi")) keywords.add("小米");
        if (cleaned.contains("苹果") || cleaned.contains("apple") || cleaned.contains("iphone")) keywords.add("苹果");
        if (cleaned.contains("三星") || cleaned.contains("samsung")) keywords.add("三星");

        // 产品类型关键词
        if (cleaned.contains("手机") || cleaned.contains("电话")) keywords.add("手机");
        if (cleaned.contains("笔记本") || cleaned.contains("电脑")) keywords.add("笔记本");
        if (cleaned.contains("电视") || cleaned.contains("电视机")) keywords.add("电视");
        if (cleaned.contains("空调")) keywords.add("空调");
        if (cleaned.contains("连衣裙") || cleaned.contains("裙子")) keywords.add("连衣裙");
        if (cleaned.contains("西装") || cleaned.contains("西服")) keywords.add("西装");
        if (cleaned.contains("大衣")) keywords.add("大衣");
        if (cleaned.contains("高跟鞋") || cleaned.contains("高跟")) keywords.add("高跟鞋");

        return keywords;
    }

    /**
     * 构建商品价格响应
     */
    private String buildProductPriceResponse(Product product) {
        StringBuilder response = new StringBuilder();
        response.append("📦 **商品信息**\n\n");
        response.append("🔹 **名称**：").append(product.getName()).append("\n");
        response.append("💰 **价格**：").append(product.getPrice()).append(" 元\n");

        if (product.getSubTitle() != null && !product.getSubTitle().isEmpty()) {
            response.append("📝 **描述**：").append(product.getSubTitle()).append("\n");
        }

        if (product.getStock() != null) {
            response.append("📊 **库存**：").append(product.getStock()).append(" 件\n");
        }

        // 如果有品牌信息
        String brandFromName = extractBrandFromProductName(product.getName());
        if (!brandFromName.isEmpty()) {
            response.append("🏷️ **品牌**：").append(brandFromName).append("\n");
        }

      /*  // 如果有分类信息
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            response.append("🗂️ **分类**：").append(product.getCategory()).append("\n");
        }
*/
        response.append("\n📞 **需要购买或有其他问题吗？**");

        return response.toString();
    }
    /**
     * 从商品名称中提取品牌
     */
    private String extractBrandFromProductName(String productName) {
        if (productName == null || productName.isEmpty()) {
            return "";
        }

        String lowerName = productName.toLowerCase();

        if (lowerName.contains("苹果") || lowerName.contains("apple") || lowerName.contains("iphone")) {
            return "苹果";
        } else if (lowerName.contains("小米") || lowerName.contains("mi") || lowerName.contains("红米")) {
            return "小米";
        } else if (lowerName.contains("华为") || lowerName.contains("huawei")) {
            return "华为";
        } else if (lowerName.contains("三星") || lowerName.contains("samsung")) {
            return "三星";
        } else if (lowerName.contains("联想") || lowerName.contains("lenovo")) {
            return "联想";
        } else if (lowerName.contains("索尼") || lowerName.contains("sony")) {
            return "索尼";
        } else if (lowerName.contains("lg")) {
            return "LG";
        } else if (lowerName.contains("tcl")) {
            return "TCL";
        } else if (lowerName.contains("美的") || lowerName.contains("midea")) {
            return "美的";
        } else if (lowerName.contains("海尔") || lowerName.contains("haier")) {
            return "海尔";
        } else if (lowerName.contains("格力") || lowerName.contains("gree")) {
            return "格力";
        } else if (lowerName.contains("海信") || lowerName.contains("hisense")) {
            return "海信";
        } else if (lowerName.contains("康佳") || lowerName.contains("konka")) {
            return "康佳";
        }

        return "";
    }

    /**
     * 构建多个商品响应
     */
    private String buildMultipleProductsResponse(List<Product> products, String keyword) {
        StringBuilder response = new StringBuilder();
        response.append("🔍 为您找到多个相关商品，请确认您要找的是哪一个：\n\n");

        int count = Math.min(products.size(), 5);
        for (int i = 0; i < count; i++) {
            Product p = products.get(i);
            response.append(i + 1).append(". **").append(p.getName()).append("**\n");
            response.append("   价格：").append(p.getPrice()).append(" 元\n");

            if (p.getSubTitle() != null && !p.getSubTitle().isEmpty()) {
                response.append("   描述：").append(p.getSubTitle()).append("\n");
            }
            response.append("\n");
        }

        if (products.size() > 5) {
            response.append("📋 还有 **").append(products.size() - 5).append("** 个商品未显示\n");
        }

        response.append("\n💡 **提示**：请告诉我更具体的商品名称，比如完整的型号或描述。");

        return response.toString();
    }

    /**
     * 构建搜索结果响应
     */
    private String buildSearchResultsResponse(List<Product> products, String keyword) {
        StringBuilder response = new StringBuilder();
        response.append("🔎 根据 **").append(keyword).append("** 搜索到以下商品：\n\n");

        int count = Math.min(products.size(), 3);
        for (int i = 0; i < count; i++) {
            Product p = products.get(i);
            response.append("🏷️ **").append(p.getName()).append("**\n");
            response.append("💰 价格：").append(p.getPrice()).append(" 元\n");

            if (p.getSubTitle() != null && !p.getSubTitle().isEmpty()) {
                response.append("📝 ").append(p.getSubTitle()).append("\n");
            }

            response.append("---\n");
        }

        if (products.size() > 3) {
            response.append("\n📊 共找到 **").append(products.size()).append("** 个相关商品。\n");
            response.append("可以告诉我更具体的信息来精确查找。");
        }

        return response.toString();
    }

    /**
     * 从用户问题中提取价格区间（如“100-200元”“300到500元”）
     * @param question 用户问题
     * @return 包含min和max的Map，无区间则返回null
     */
    // src/main/java/com/example/demoai/ZhipuAIService.java
    private Map<String, Double> extractPriceRangeFromQuestion(String question) {
        if (question == null || question.isEmpty()) {
            return null;
        }

        // 增强正则：支持"500-1000"、"500到1000"、"500至1000"、"500 ~ 1000元"等格式
        Pattern pattern = Pattern.compile(
                "(\\d+)\\s*(元|块|块钱)?\\s*(-|到|至|~)\\s*(\\d+)\\s*(元|块|块钱)?"
        );
        Matcher matcher = pattern.matcher(question);

        if (matcher.find()) {
            try {
                double min = Double.parseDouble(matcher.group(1).trim());
                double max = Double.parseDouble(matcher.group(4).trim());

                // 确保min <= max（如"1000-500"自动修正为500-1000）
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

    // 处理价格查询请求
    private String handlePriceQuery(String userQuestion) {
        // 1. 首先尝试从用户问题中提取商品名称进行精确查询
        String productName = extractProductNameFromQuestion(userQuestion);
        if (productName != null && !productName.isEmpty()) {
            List<Product> products = productService.findProductsByNameContaining(productName);
            if (!products.isEmpty()) {
                // 返回第一个最匹配的商品
                return buildExactProductResponse(products.get(0));
            }
        }

        // 2. 如果精确查询失败，尝试提取品牌和型号
        String brand = extractBrandFromQuestion(userQuestion);
        String model = extractProductModelFromQuestion(userQuestion);
        List<String> categoryKeywords = extractCategoryKeywordsFromQuestion(userQuestion);

        // 如果有具体型号，尝试按型号查询
        if (model != null && !model.isEmpty()) {
            List<Product> products = productService.findProductsByNameContaining(model);
            if (!products.isEmpty()) {
                return buildSpecificModelResponse(products, model);
            }
        }

        // 3. 如果按型号查不到，尝试按品牌和类别查询
        List<Long> categoryIds = new ArrayList<>();
        for (String keyword : categoryKeywords) {
            List<Long> ids = categoryService.getCategoryIdsByKeyword(keyword);
            if (ids != null) {
                categoryIds.addAll(ids);
            }
        }

        // ====== 在此处插入价格区间处理逻辑 ======
        // 提取价格区间
        Map<String, Double> priceRange = extractPriceRangeFromQuestion(userQuestion);
        PriceExtractResult priceResult = null;
        if (priceRange != null) {
            double min = priceRange.get("min");
            double max = priceRange.get("max");
            // 使用现有构造器创建实例（假设存在双参数构造器可接收min和max）
            priceResult = new PriceExtractResult(min, max); // 传入min和max
            priceResult.setCondition(PriceCondition.RANGE); // 设置RANGE条件
        }
        // ======================================

        List<Product> products = new ArrayList<>();
        if (brand != null && !brand.isEmpty()) {
            if (!categoryIds.isEmpty()) {
                products = productService.getProductsByBrandAndCategoryIdsAndPriceRange(brand, categoryIds, priceResult);
            } else {
                products = productService.getProductsByBrandAndPriceRange(brand, priceResult);
            }
        } else if (!categoryIds.isEmpty()) {
            products = productService.getProductsByCategoryIdsAndPriceRange(categoryIds, priceResult);
        } else {
            return "商品价格因品牌、型号不同而有所差异。请告诉我具体商品，我会为您查询。";
        }

        if (products.isEmpty()) {
            return buildNoProductResponse(brand, categoryKeywords, null);
        }

        // 返回带数量限制的响应
        return buildLimitedPriceResponse(products, brand, model, categoryKeywords);
    }

    // 从用户问题中提取商品名称
    private String extractProductNameFromQuestion(String question) {
        // 尝试匹配完整的商品名称模式
        Pattern pattern = Pattern.compile("([\\p{L}\\d\\s\\(\\)\\.\\-]+?)(多少钱|什么价|价格|价钱|价|多钱|卖多少|元|价格是多少)");
        Matcher matcher = pattern.matcher(question);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            // 过滤掉常见的疑问词
            if (!name.contains("多少钱") && !name.contains("什么价") && !name.contains("价格")) {
                return name;
            }
        }

        // 尝试匹配常见的商品名称开头
        String[] productStartWords = {
                "华为", "HUAWEI", "小米", "MI", "红米", "Redmi", "苹果", "Apple", "iPhone",
                "三星", "SAMSUNG", "联想", "Lenovo", "索尼", "SONY", "LG", "TCL",
                "美的", "Midea", "海尔", "Haier", "格力", "GREE", "海信", "Hisense",
                "康佳", "KONKA", "先锋", "Pioneer", "爱国者", "aiguo", "罗马仕", "ROMOSS",
                "半岛铁盒", "鄂尔多斯", "Erdos", "拉夏贝尔", "La Chapelle", "朗姿", "LANC",
                "南极人", "NanJiren", "vivo", "VIVO", "oppo", "OPPO", "魅族", "MEIZU",
                "努比亚", "nubia", "一加", "OnePlus", "realme", "Realme", "荣耀", "HONOR"
        };

        for (String startWord : productStartWords) {
            if (question.contains(startWord)) {
                // 提取从该词开始到句尾或价格查询词为止的内容
                int start = question.indexOf(startWord);
                String remaining = question.substring(start);

                // 找到价格查询词的位置
                String[] priceWords = {"多少钱", "什么价", "价格", "价钱", "价", "多钱", "卖多少", "价格是多少", "元"};
                int end = remaining.length();
                for (String priceWord : priceWords) {
                    int idx = remaining.indexOf(priceWord);
                    if (idx > 0) {
                        end = Math.min(end, idx);
                    }
                }

                if (end < remaining.length()) {
                    return remaining.substring(0, end).trim();
                } else {
                    return remaining.trim();
                }
            }
        }

        return null;
    }

    // 构建精确商品响应
    private String buildExactProductResponse(Product product) {
        StringBuilder response = new StringBuilder();
        response.append("为您查询到商品：").append(product.getName()).append("\n");
        response.append("价格：").append(product.getPrice()).append("元\n");
        response.append("详情：").append(product.getSubTitle());
        return response.toString();
    }

    // 构建价格响应
    // ZhipuAIService中补充构建响应的方法
    private String buildPriceResponse(List<Product> products, String brand, String model, List<String> categoryKeywords) {
        StringBuilder response = new StringBuilder();

        // 构建响应前缀
        if (brand != null && !brand.isEmpty() && !categoryKeywords.isEmpty()) {
            response.append("为您找到以下").append(brand);
            for (String keyword : categoryKeywords) {
                response.append(keyword);
            }
            response.append("的商品：\n");
        } else if (brand != null && !brand.isEmpty()) {
            response.append("为您找到以下").append(brand).append("的商品：\n");
        } else if (!categoryKeywords.isEmpty()) {
            response.append("为您找到以下");
            for (String keyword : categoryKeywords) {
                response.append(keyword);
            }
            response.append("的商品：\n");
        } else {
            response.append("为您找到以下符合条件的商品：\n");
        }

        // 只取前10个商品
        int displayCount = Math.min(products.size(), 10);
        for (int i = 0; i < displayCount; i++) {
            Product product = products.get(i);
            response.append(i+1).append(". ")
                    .append(product.getName()).append(" - 价格：").append(product.getPrice()).append("元 ")
                    .append("详情：").append(product.getDescription()).append("\n");
        }

        // 提示总数量
        if (products.size() > 10) {
            response.append("\n提示：查询到").append(products.size()).append("个商品，已显示前10个。");
        }

        return response.toString();
    }

    private String buildLimitedPriceResponse(List<Product> products, String brand, String model, List<String> categoryKeywords) {
        StringBuilder response = new StringBuilder();

        // 构建响应前缀
        if (brand != null && !brand.isEmpty() && !categoryKeywords.isEmpty()) {
            response.append("为您找到以下").append(brand);
            for (String keyword : categoryKeywords) {
                response.append(keyword);
            }
            response.append("的商品：\n");
        } else if (brand != null && !brand.isEmpty()) {
            response.append("为您找到以下").append(brand).append("的商品：\n");
        } else if (!categoryKeywords.isEmpty()) {
            response.append("为您找到以下");
            for (String keyword : categoryKeywords) {
                response.append(keyword);
            }
            response.append("的商品：\n");
        } else {
            response.append("为您找到以下符合条件的商品：\n");
        }

        // 只取前10个商品
        int displayCount = Math.min(products.size(), 10);
        for (int i = 0; i < displayCount; i++) {
            Product product = products.get(i);
            response.append(i+1).append(". ")
                    .append(product.getName()).append(" - 价格：").append(product.getPrice()).append("元")
                    .append(" 详情：").append(product.getSubTitle()).append("\n");
        }

        // 提示总数量
        if (products.size() > 10) {
            response.append("\n提示：查询到").append(products.size()).append("个商品，已显示前10个。");
        }

        return response.toString();
    }

    // 处理商品推荐请求
    // 修改 handleProductRecommendation 方法中的价格条件处理部分
    public String handleProductRecommendation(String userQuestion) {
        // 检查用户是否明确要求显示前10个
        boolean showOnlyTen = userQuestion.contains("前10个") ||
                userQuestion.contains("显示10个") ||
                userQuestion.contains("只要10个");
        String brand = extractBrandFromQuestion(userQuestion);
        List<String> categoryKeywords = extractCategoryKeywordsFromQuestion(userQuestion);
        // 处理通用的衣服推荐请求
        if (userQuestion.contains("推荐的衣服") || userQuestion.contains("有没有衣服") ||
                userQuestion.contains("想买衣服") || userQuestion.contains("衣服推荐")) {
            // 如果用户只是简单询问衣服推荐，没有指定品牌或价格
            if (brand == null || brand.isEmpty()) {
                categoryKeywords.clear(); // 清空原有的分类关键词
                categoryKeywords.add("精品服饰"); // 直接使用精品服饰分类

                // 查询精品服饰分类下的商品
                List<Long> categoryIds = new ArrayList<>();
                List<Long> ids = categoryService.getCategoryIdsByKeyword("精品服饰");
                if (ids != null) {
                    categoryIds.addAll(ids);
                }

                if (!categoryIds.isEmpty()) {
                    List<Product> products = productService.getProductsByCategoryIdsAndPriceRange(categoryIds, null);

                    if (!products.isEmpty()) {
                        StringBuilder response = new StringBuilder();
                        response.append("为您推荐以下精品服饰：\n");

                        // 限制显示数量
                        int displayCount = Math.min(products.size(), 10);
                        for (int i = 0; i < displayCount; i++) {
                            Product p = products.get(i);
                            response.append(i + 1).append(". ").append(p.getName())
                                    .append(" - 价格：").append(p.getPrice())
                                    .append("元\n   详情：").append(p.getSubTitle()).append("\n");
                        }

                        if (products.size() > displayCount) {
                            response.append("\n📊 共找到 **").append(products.size()).append("** 个相关商品，已显示前").append(displayCount).append("个。");
                        }

                        return response.toString();
                    }
                }
            }
        }
        // 新增：优先识别"衣服"相关类别，添加"精品服饰"到类别关键词
        boolean hasClothesKeyword = userQuestion.contains("衣服") || userQuestion.contains("服饰");
        // 检查是否有具体的服饰分类关键词
        boolean hasSpecificClothingKeyword = false;
        for (String keyword : categoryKeywords) {
            if ("特色女装".equals(keyword) || "女装".equals(keyword) || "女士服装".equals(keyword) ||
                "精品男装".equals(keyword) || "男装".equals(keyword) || "男士服装".equals(keyword) ||
                "潮流女鞋".equals(keyword) || "女鞋".equals(keyword) || "高跟鞋".equals(keyword) ||
                "品质男鞋".equals(keyword) || "男鞋".equals(keyword) || "皮鞋".equals(keyword)) {
                hasSpecificClothingKeyword = true;
                break;
            }
        }
        // 只有当没有具体服饰分类关键词时，才添加通用的"精品服饰"关键词
        if ((categoryKeywords.isEmpty() || hasClothesKeyword) && !hasSpecificClothingKeyword) {
            // 避免重复添加
            if (!categoryKeywords.contains("精品服饰")) {
                categoryKeywords.add("精品服饰"); // 匹配电商知识库中的分类
            }
        }

        // 关键修改：增强价格信息提取逻辑
        PriceExtractResult priceResult = extractPriceInfo(userQuestion);

        // 添加调试日志
        log.info("用户查询：{}", userQuestion);
        log.info("提取的品牌：{}", brand);
        log.info("提取的类别关键词：{}", categoryKeywords);
        log.info("提取的价格结果：{}", priceResult != null ?
                "价格=" + priceResult.getPrice() + ", 条件=" + priceResult.getCondition() : "null");

        List<Long> categoryIds = new ArrayList<>();
        for (String keyword : categoryKeywords) {
            List<Long> ids = categoryService.getCategoryIdsByKeyword(keyword);
            if (ids != null) {
                categoryIds.addAll(ids);
            }
        }

        // =========== 新增：根据用户意图精确筛选分类 ===========
        // 手机配件关键词（优先检测，因为“手机膜”包含“手机”）
        boolean hasAccessoryKeyword = userQuestion.contains("钢化膜") ||
                userQuestion.contains("手机膜") ||
                userQuestion.contains("玻璃膜") ||
                userQuestion.contains("保护膜") ||
                userQuestion.contains("贴膜") ||
                userQuestion.contains("手机壳") ||
                userQuestion.contains("保护壳") ||
                userQuestion.contains("手机套") ||
                userQuestion.contains("配件") ||
                userQuestion.contains("充电宝") ||
                userQuestion.contains("耳机") ||
                userQuestion.contains("数据线") ||
                userQuestion.contains("充电器") ||
                userQuestion.contains("支架") ||
                userQuestion.contains("手机支架");
                
        // 如果用户明确提到“手机”，但不是配件
        boolean hasMobileKeyword = (userQuestion.contains("手机") ||
                userQuestion.contains("iPhone") ||
                userQuestion.contains("智能手机")) && !hasAccessoryKeyword;

        // 服饰类商品精确筛选
        boolean hasMenClothingKeyword = userQuestion.contains("精品男装") ||
                userQuestion.contains("男装") ||
                userQuestion.contains("男士服装");
        boolean hasWomenClothingKeyword = userQuestion.contains("特色女装") ||
                userQuestion.contains("女装") ||
                userQuestion.contains("女士服装");
        boolean hasMenShoesKeyword = userQuestion.contains("品质男鞋") ||
                userQuestion.contains("男鞋") ||
                userQuestion.contains("皮鞋");
        boolean hasWomenShoesKeyword = userQuestion.contains("潮流女鞋") ||
                userQuestion.contains("女鞋") ||
                userQuestion.contains("高跟鞋");

        // 根据用户意图调整分类ID
        if (hasAccessoryKeyword) {
            // 用户要配件（手机膜、充电宝等）：只保留category_id=8
            categoryIds.clear();
            categoryIds.add(8L);
            log.info("识别为手机配件查询，使用配件分类ID=8");
        } else if (hasMobileKeyword) {
            // 用户要手机：只保留category_id=7
            categoryIds.clear();
            categoryIds.add(7L);
            log.info("识别为手机查询，使用手机分类ID=7");
        } else if (hasMenClothingKeyword) {
            // 用户要男装：只保留男装分类ID（24L）
            categoryIds.removeIf(id -> id != 24L);
            // 如果没有24，则添加24
            if (!categoryIds.contains(24L)) {
                categoryIds.add(24L);
            }
        } else if (hasWomenClothingKeyword) {
            // 用户要女装：只保留女装分类ID（23L）
            categoryIds.removeIf(id -> id != 23L);
            // 如果没有23，则添加23
            if (!categoryIds.contains(23L)) {
                categoryIds.add(23L);
            }
        } else if (hasMenShoesKeyword) {
            // 用户要男鞋：只保留男鞋相关分类
            List<Long> menShoeIds = categoryService.getCategoryIdsByKeyword("品质男鞋");
            if (menShoeIds != null && !menShoeIds.isEmpty()) {
                categoryIds.clear();
                categoryIds.addAll(menShoeIds);
            }
        } else if (hasWomenShoesKeyword) {
            // 用户要女鞋：只保留女鞋相关分类
            List<Long> womenShoeIds = categoryService.getCategoryIdsByKeyword("潮流女鞋");
            if (womenShoeIds != null && !womenShoeIds.isEmpty()) {
                categoryIds.clear();
                categoryIds.addAll(womenShoeIds);
            }
        }
        // =========================================================

        List<Product> products = new ArrayList<>();

        // 关键修改：处理价格条件查询的逻辑
        boolean hasPriceCondition = priceResult != null && priceResult.getPrice() != null;

        log.info("是否有价格条件：{}", hasPriceCondition);
        log.info("分类ID列表：{}", categoryIds);
        log.info("品牌：{}", brand);

        // 第一次查询：包含所有条件
        if (!categoryIds.isEmpty()) {
            if (brand != null && !brand.isEmpty()) {
                products = productService.getProductsByBrandAndCategoryIdsAndPriceRange(
                        brand, categoryIds, priceResult);
            } else {
                products = productService.getProductsByCategoryIdsAndPriceRange(
                        categoryIds, priceResult);
            }
        } else {
            if (brand != null && !brand.isEmpty()) {
                products = productService.getProductsByBrandAndPriceRange(
                        brand, priceResult);
            } else if (hasPriceCondition) {
                // 关键修改：当有价格条件时，执行价格筛选查询
                products = productService.getProductsByPriceRange(priceResult);

                // 如果价格筛选查询没有结果，尝试放宽条件：查询所有商品然后手动过滤
                if (products.isEmpty()) {
                    List<Product> allProducts = productService.getAllProducts();
                    log.info("价格筛选查询无结果，所有商品数量：{}", allProducts.size());

                    // 手动过滤价格低于500元的商品 - 修复类型比较
                    Double targetPrice = priceResult.getPrice();
                    if (targetPrice != null) {
                        for (Product product : allProducts) {
                            // 正确比较BigDecimal和Double
                            if (product.getPrice().doubleValue() < targetPrice) {
                                products.add(product);
                            }
                        }
                    }
                    log.info("手动过滤后商品数量：{}", products.size());
                }
            } else {
                products = productService.getAllProducts();

                if (products.size() > 20) {
                    products = products.subList(0, 20);
                }
            }
        }

        // =========== 新增：品牌过滤逻辑 ===========
        // 如果提取到了品牌，确保结果只包含该品牌的商品
        if (brand != null && !brand.isEmpty() && !products.isEmpty()) {
            List<Product> filteredProducts = new ArrayList<>();
            for (Product product : products) {
                if (isProductBrandMatch(product, brand)) {
                    filteredProducts.add(product);
                } else {
                    log.debug("过滤掉非品牌商品: {}", product.getName());
                }
            }
            products = filteredProducts;

            // 如果没有匹配品牌的商品，提供明确的反馈
            if (products.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("抱歉，没有找到").append(brand).append("品牌");

                if (!categoryKeywords.isEmpty()) {
                    for (String keyword : categoryKeywords) {
                        errorMsg.append(keyword);
                    }
                } else {
                    errorMsg.append("的商品");
                }

                if (priceResult != null) {
                    errorMsg.append(getPriceConditionText(priceResult));
                }

                errorMsg.append("。\n\n您可以尝试：\n");
                errorMsg.append("1. 调整价格范围\n");
                errorMsg.append("2. 查看其他品牌\n");
                errorMsg.append("3. 查看所有符合条件的商品");

                return errorMsg.toString();
            }
        }
        // ========================================

        log.info("查询到的商品数量：{}", products.size());

        // 如果第一次查询没有结果，尝试放宽条件
        if (products.isEmpty() && hasPriceCondition) {
            StringBuilder fallbackMessage = new StringBuilder();

            // 尝试放宽价格条件：从"左右"改为"以下"或"以上"
            if (priceResult.getCondition() == PriceCondition.AROUND) {
                // 先尝试查询价格以下的商品
                // 修正：先创建对象，再设置条件
                PriceExtractResult belowResult = new PriceExtractResult(priceResult.getPrice() * 1.2); // 放宽到120%
                belowResult.setCondition(PriceCondition.BELOW); // 单独设置条件
                products = retryWithPriceCondition(brand, categoryIds, belowResult);
                if (!products.isEmpty()) {
                    fallbackMessage.append("没有找到").append(priceResult.getPrice()).append("元左右的商品，")
                            .append("为您推荐").append(belowResult.getPrice()).append("元以下的商品：\n");
                } else {
                    // 再尝试查询所有价格的商品
                    products = retryWithoutPriceCondition(brand, categoryIds);
                    if (!products.isEmpty()) {
                        fallbackMessage.append("没有找到").append(priceResult.getPrice()).append("元左右的商品，")
                                .append("为您推荐所有").append(brand).append("商品：\n");
                    }
                }
            }

            if (!products.isEmpty()) {
                return buildResponseWithFallback(products, fallbackMessage.toString(), brand, categoryKeywords, priceResult);
            }
        }

        if (products.isEmpty()) {
            log.warn("没有找到符合条件的商品，用户查询：{}", userQuestion);

            // 提供更详细的错误信息
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("暂时没有找到");

            if (brand != null && !brand.isEmpty()) {
                errorMsg.append(brand);
            }

            if (!categoryKeywords.isEmpty()) {
                for (String keyword : categoryKeywords) {
                    errorMsg.append(keyword);
                }
            }

            if (priceResult != null && priceResult.getPrice() != null) {
                errorMsg.append(getPriceConditionText(priceResult));
            }

            errorMsg.append("的商品。\n\n");
            errorMsg.append("建议您可以尝试：\n");
            errorMsg.append("1. 调整价格范围（如：1000元以下的商品）\n");
            errorMsg.append("2. 查看其他分类的商品\n");
            errorMsg.append("3. 直接搜索具体商品名称\n");

            return errorMsg.toString();
        }

        // 修改：如果用户要求只显示前10个，则限制显示数量
        if (showOnlyTen && products.size() > 10) {
            return buildLimitedResponse(products.subList(0, Math.min(10, products.size())),
                    brand, categoryKeywords, priceResult, true);
        }

        return buildNormalResponse(products, brand, categoryKeywords, priceResult);
    }

    // 使用新的价格条件重试查询
    private List<Product> retryWithPriceCondition(String brand, List<Long> categoryIds, PriceExtractResult newPriceResult) {
        if (!categoryIds.isEmpty()) {
            if (brand != null && !brand.isEmpty()) {
                return productService.getProductsByBrandAndCategoryIdsAndPriceRange(
                        brand, categoryIds, newPriceResult);
            } else {
                return productService.getProductsByCategoryIdsAndPriceRange(
                        categoryIds, newPriceResult);
            }
        } else {
            if (brand != null && !brand.isEmpty()) {
                return productService.getProductsByBrandAndPriceRange(
                        brand, newPriceResult);
            } else {
                return productService.getProductsByPriceRange(newPriceResult);
            }
        }
    }


    /**
     * 判断商品是否匹配指定品牌
     */
    private boolean isProductBrandMatch(Product product, String brand) {
        if (product == null || brand == null || brand.isEmpty()) {
            return false;
        }

        String productName = product.getName().toLowerCase();
        String brandLower = brand.toLowerCase();

        // 品牌匹配规则
        if (brand.equals("苹果")) {
            return productName.contains("苹果") ||
                    productName.contains("apple") ||
                    productName.contains("iphone");
        } else if (brand.equals("小米")) {
            return productName.contains("小米") ||
                    productName.contains("mi") ||
                    productName.contains("红米") ||
                    productName.contains("redmi");
        } else if (brand.equals("华为")) {
            return productName.contains("华为") ||
                    productName.contains("huawei");
        } else if (brand.equals("三星")) {
            return productName.contains("三星") ||
                    productName.contains("samsung");
        } else if (brand.equals("联想")) {
            return productName.contains("联想") ||
                    productName.contains("lenovo");
        } else if (brand.equals("索尼")) {
            return productName.contains("索尼") ||
                    productName.contains("sony");
        } else {
            // 通用匹配规则
            return productName.contains(brandLower);
        }
    }

    // 在 ZhipuAIService 类中添加测试方法
    public List<Product> testPriceFilter() {
        // 测试价格筛选功能
        PriceExtractResult priceResult = new PriceExtractResult(500.0);
        priceResult.setCondition(PriceCondition.BELOW);

        List<Product> products = productService.getProductsByPriceRange(priceResult);
        log.info("测试价格筛选：500元以下的商品数量：{}", products.size());

        // 如果没有结果，尝试获取所有商品并手动过滤
        if (products.isEmpty()) {
            List<Product> allProducts = productService.getAllProducts();
            log.info("所有商品数量：{}", allProducts.size());

            // 统计价格低于500元的商品
            int count = 0;
            for (Product product : allProducts) {
                // 修复类型比较错误
                if (product.getPrice() != null && product.getPrice().doubleValue() < 500) {
                    count++;
                    log.info("低价商品：{} - {}元", product.getName(), product.getPrice());
                }
            }
            log.info("价格低于500元的商品数量：{}", count);
        }

        return products;
    }
    // 在类开头添加这个方法
    private String repeatString(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }


    // 添加一个新方法来构建限制数量的响应
    private String buildLimitedResponse(List<Product> products, String brand,
                                        List<String> categoryKeywords,
                                        PriceExtractResult priceResult, boolean limited) {
        StringBuilder response = new StringBuilder();

        response.append("✨ **AI 智能推荐**\n");
        response.append(repeatString("═", 40)).append("\n");

        if (brand != null && !brand.isEmpty()) {
            response.append("🏷️ **品牌**：").append(brand).append("\n");
        }

        if (!categoryKeywords.isEmpty()) {
            response.append("📂 **分类**：");
            for (int i = 0; i < categoryKeywords.size(); i++) {
                if (i > 0) response.append("、");
                response.append(categoryKeywords.get(i));
            }
            response.append("\n");
        }

        if (priceResult != null && priceResult.getPrice() != null) {
            response.append("💰 **价格条件**：").append(getPriceConditionText(priceResult)).append("\n");
        }

        if (limited) {
            response.append("📏 **显示限制**：仅显示前10个商品\n");
        }

        response.append(repeatString("═", 40)).append("\n");

        // 使用简洁的列表格式
        int displayCount = Math.min(products.size(), 10);
        response.append("\n📋 **商品列表**：\n");

        for (int i = 0; i < displayCount; i++) {
            Product p = products.get(i);
            String name = truncateString(p.getName(), 35);

            // 使用更整齐的格式化
            response.append(String.format("%2d. %s\n", i + 1, name));
            response.append(String.format("    💰 价格：%.2f 元\n", p.getPrice()));

            if (p.getSubTitle() != null && !p.getSubTitle().isEmpty()) {
                String subTitle = truncateString(p.getSubTitle(), 45);
                response.append(String.format("    📝 描述：%s\n", subTitle));
            }

            // 改为简单的空行分隔
            if (i < displayCount - 1) {
                response.append("\n");
            }
        }

        if (products.size() > displayCount) {
            response.append(String.format("\n📊 **已显示 %d/%d 个商品**\n",
                    displayCount, products.size()));
        }

        return response.toString();
    }
    // 新增辅助方法：根据用户意图调整分类ID
    private List<Long> adjustCategoryIdsByUserIntent(List<Long> categoryIds, String userQuestion) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return categoryIds;
        }

        boolean hasMobileKeyword = userQuestion.contains("手机") ||
                userQuestion.contains("iPhone") ||
                userQuestion.contains("智能手机");

        boolean hasAccessoryKeyword = userQuestion.contains("钢化膜") ||
                userQuestion.contains("配件") ||
                userQuestion.contains("充电宝") ||
                userQuestion.contains("耳机");

        List<Long> adjustedIds = new ArrayList<>(categoryIds);

        // 根据用户意图调整分类ID
        if (hasMobileKeyword && !hasAccessoryKeyword) {
            // 用户要手机，不是配件：只保留category_id=7
            adjustedIds.removeIf(id -> id != 7);
            if (!adjustedIds.contains(7L)) {
                adjustedIds.add(7L);
            }
        } else if (hasAccessoryKeyword && !hasMobileKeyword) {
            // 用户要配件，不是手机：只保留category_id=8
            adjustedIds.removeIf(id -> id != 8);
            if (!adjustedIds.contains(8L)) {
                adjustedIds.add(8L);
            }
        }

        return adjustedIds;
    }

    // 在 ZhipuAIService 类中添加辅助方法
    private boolean isPriceBelow(Product product, Double targetPrice) {
        if (targetPrice == null || product.getPrice() == null) {
            return false;
        }
        return product.getPrice().doubleValue() < targetPrice;
    }

    private boolean isPriceAbove(Product product, Double targetPrice) {
        if (targetPrice == null || product.getPrice() == null) {
            return false;
        }
        return product.getPrice().doubleValue() > targetPrice;
    }

    private boolean isPriceAround(Product product, Double targetPrice, double marginPercent) {
        if (targetPrice == null || product.getPrice() == null) {
            return false;
        }
        double price = product.getPrice().doubleValue();
        double margin = targetPrice * marginPercent / 100.0;
        return price >= (targetPrice - margin) && price <= (targetPrice + margin);
    }


    /**
     * 手动根据价格条件过滤商品列表
     */
    private List<Product> manuallyFilterProductsByPrice(List<Product> allProducts, PriceExtractResult priceResult) {
        List<Product> filteredProducts = new ArrayList<>();

        if (allProducts == null || allProducts.isEmpty() || priceResult == null) {
            return filteredProducts;
        }

        for (Product product : allProducts) {
            if (product.getPrice() == null) {
                continue;
            }

            double productPrice = product.getPrice().doubleValue();

            switch (priceResult.getCondition()) {
                case BELOW:
                    if (priceResult.getPrice() != null && productPrice < priceResult.getPrice()) {
                        filteredProducts.add(product);
                    }
                    break;
                case ABOVE:
                    if (priceResult.getPrice() != null && productPrice > priceResult.getPrice()) {
                        filteredProducts.add(product);
                    }
                    break;
                case AROUND:
                    if (priceResult.getPrice() != null) {
                        double margin = priceResult.getPrice() * 0.2; // 20%的浮动范围
                        if (productPrice >= priceResult.getPrice() - margin &&
                                productPrice <= priceResult.getPrice() + margin) {
                            filteredProducts.add(product);
                        }
                    }
                    break;
                case RANGE:
                    if (priceResult.getMinPrice() != null && priceResult.getMaxPrice() != null) {
                        if (productPrice >= priceResult.getMinPrice() &&
                                productPrice <= priceResult.getMaxPrice()) {
                            filteredProducts.add(product);
                        }
                    }
                    break;
                case EXACT:
                    if (priceResult.getPrice() != null && productPrice == priceResult.getPrice()) {
                        filteredProducts.add(product);
                    }
                    break;
            }
        }

        return filteredProducts;
    }

    // 带上下文的商品推荐
    public String handleProductRecommendationWithContext(Map<String, Object> context,
                                                         String priceQuery) {
        logBusinessInfo("处理带上下文的商品推荐", context, priceQuery);
        String brand = (String) context.get("brand");
        List<Long> categoryIds = (List<Long>) context.get("categoryIds");
        PriceExtractResult priceResult = extractPriceInfo(priceQuery);

        // 关键修复：如果上下文中的品牌与价格查询中的品牌不一致，优先使用价格查询中的品牌
        String priceQueryBrand = extractBrandFromQuestion(priceQuery);
        if (priceQueryBrand != null && !priceQueryBrand.isEmpty() &&
                brand != null && !brand.isEmpty() &&
                !priceQueryBrand.equals(brand)) {
            log.info("品牌冲突：上下文品牌={}，查询品牌={}，使用查询品牌", brand, priceQueryBrand);
            brand = priceQueryBrand;
        }

        List<Product> products = new ArrayList<>();

        if (categoryIds != null && !categoryIds.isEmpty()) {
            if (brand != null && !brand.isEmpty()) {
                products = productService.getProductsByBrandAndCategoryIdsAndPriceRange(
                        brand, categoryIds, priceResult);
            } else {
                products = productService.getProductsByCategoryIdsAndPriceRange(
                        categoryIds, priceResult);
            }
        } else {
            if (brand != null && !brand.isEmpty()) {
                products = productService.getProductsByBrandAndPriceRange(
                        brand, priceResult);
            } else if (priceResult != null) {
                products = productService.getProductsByPriceRange(priceResult);
                log.info("handleProductRecommendationWithContext - 通过getProductsByPriceRange查询到的商品数量：{}", products.size());

                if (products.isEmpty()) {
                    List<Product> allProducts = productService.getAllProducts();
                    products = manuallyFilterProductsByPrice(allProducts, priceResult);
                }
            } else {
                return "请输入更具体的筛选条件，如价格范围或品牌。";
            }
        }
        // 构建响应时，使用正确的品牌信息
        StringBuilder response = new StringBuilder();

        // 根据实际查询结果构建响应
        if (brand != null && !brand.isEmpty()) {
            response.append("为您找到以下").append(brand);
            if (priceResult != null) {
                response.append(getPriceConditionText(priceResult));
            }
            response.append("的商品：\n");
        } else if (priceResult != null) {
            response.append("为您找到以下").append(getPriceConditionText(priceResult)).append("的商品：\n");
        } else {
            response.append("为您推荐以下商品：\n");
        }

        // 修复：如果手动过滤后仍然没有结果，不应该立即返回空
        if (products.isEmpty()) {
            StringBuilder noProductMsg = new StringBuilder();
            noProductMsg.append("未找到");

            if (brand != null && !brand.isEmpty()) {
                noProductMsg.append(brand).append("品牌");
            }

            if (priceResult != null) {
                noProductMsg.append(getPriceConditionText(priceResult));
            }

            noProductMsg.append("的相关商品。\n\n建议您可以：\n");
            noProductMsg.append("1. 调整价格范围\n");
            noProductMsg.append("2. 查看其他分类的商品\n");
            noProductMsg.append("3. 尝试不同的搜索关键词");

            return noProductMsg.toString();
        }

        StringBuilder aresponse = new StringBuilder("为您找到以下" +
                getPriceConditionText(priceResult) + "的商品：\n");
        int displayCount = Math.min(products.size(), 10);
        for (int i = 0; i < displayCount; i++) {
            Product p = products.get(i);
            aresponse.append(i + 1).append(". ").append(p.getName())
                    .append(" - 价格：").append(p.getPrice())
                    .append("元\n   详情：").append(p.getSubTitle()).append("\n");
        }

        if (products.size() > 10) {
            aresponse.append("\n提示：查询到").append(products.size()).append("个商品，已显示前").append(Math.min(10, products.size())).append("个。");
        }

        return aresponse.toString();
    }
    // 不带价格条件重试查询
    private List<Product> retryWithoutPriceCondition(String brand, List<Long> categoryIds) {
        if (!categoryIds.isEmpty()) {
            if (brand != null && !brand.isEmpty()) {
                return productService.getProductsByBrandAndCategoryIdsAndPriceRange(
                        brand, categoryIds, null);
            } else {
                return productService.getProductsByCategoryIdsAndPriceRange(
                        categoryIds, null);
            }
        } else {
            if (brand != null && !brand.isEmpty()) {
                return productService.getProductsByBrandAndPriceRange(
                        brand, null);
            } else {
                return productService.getAllProducts();
            }
        }
    }

    private void appendProductListSimple(StringBuilder response, List<Product> products) {
        // 只取前10个商品
        int displayCount = Math.min(products.size(), 10);
        response.append("\n📦 **商品列表**\n");
        for (int i = 0; i < displayCount; i++) {
            Product p = products.get(i);

            // 商品名称（限制长度）
            String productName = p.getName();
            if (productName.length() > 35) {
                productName = productName.substring(0, 32) + "...";
            }

            // 商品描述（限制长度）
            String subTitle = p.getSubTitle();
            if (subTitle != null && !subTitle.isEmpty()) {
                if (subTitle.length() > 45) {
                    subTitle = subTitle.substring(0, 42) + "...";
                }
            } else {
                subTitle = "暂无描述";
            }

            // 格式化输出 - 使用更整齐的格式化
            response.append(String.format("%2d. %s\n", i + 1, productName));
            response.append(String.format("    价格: %.2f元\n", p.getPrice()));
            response.append(String.format("    描述: %s\n", subTitle));

            // 改为简单的空行分隔
            if (i < displayCount - 1) {
                response.append("\n");
            }
        }
        // 提示总数量
        if (products.size() > displayCount) {
            response.append("\n📊 提示: 查询到 ").append(products.size())
                    .append(" 个相关商品，已显示前 ").append(displayCount).append(" 个。\n");
        }
    }
    // 构建正常响应
    private String buildNormalResponse(List<Product> products, String brand,
                                       List<String> categoryKeywords, PriceExtractResult priceResult) {
        StringBuilder response = new StringBuilder();

        // 构建标题
        response.append("✨ AI 智能推荐\n");
        response.append("========================================\n");

        // 添加筛选条件
        boolean hasConditions = false;

        if (brand != null && !brand.isEmpty()) {
            response.append("品牌: ").append(brand).append("\n");
            hasConditions = true;
        }

        if (!categoryKeywords.isEmpty()) {
            response.append("分类: ");
            for (int i = 0; i < categoryKeywords.size(); i++) {
                if (i > 0) response.append("、");
                response.append(categoryKeywords.get(i));
            }
            response.append("\n");
            hasConditions = true;
        }

        if (priceResult != null && priceResult.getPrice() != null) {
            response.append("价格条件: ").append(getPriceConditionText(priceResult)).append("\n");
            hasConditions = true;
        }

        if (hasConditions) {
            response.append("========================================\n\n");
        }

        // 使用新的排版格式（已移除横线）
        if (products.size() <= 10) {
            // 商品数量较少，使用详细排版
            appendProductListSimple(response, products);
        } else {
            // 商品数量较多，使用简洁排版
            response.append("📋 为您找到以下商品:\n\n");

            int displayCount = Math.min(products.size(), 10);
            for (int i = 0; i < displayCount; i++) {
                Product p = products.get(i);
                String productName = truncateString(p.getName(), 30);

                // 使用更整齐的格式化
                response.append(String.format("%2d. %s - %.2f元\n",
                        i + 1,
                        productName,
                        p.getPrice()));

                // 添加描述（如果存在）
                if (p.getSubTitle() != null && !p.getSubTitle().isEmpty()) {
                    String desc = truncateString(p.getSubTitle(), 40);
                    response.append("     ").append(desc).append("\n");
                }

                // 简单的空行分隔
                if (i < displayCount - 1) {
                    response.append("\n");
                }
            }

            response.append("\n");

            if (products.size() > displayCount) {
                response.append("📊 提示: 查询到 ").append(products.size())
                        .append(" 个相关商品，已显示前 ").append(displayCount).append(" 个。\n");
            }
        }

        // 添加底部提示
        response.append("\n💡 提示: 如需查看更多商品或调整筛选条件，请告诉我。");

        return response.toString();
    }
    // 辅助方法：截断字符串
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    // 构建带回退信息的响应
    private String buildResponseWithFallback(List<Product> products, String fallbackMessage,
                                             String brand, List<String> categoryKeywords,
                                             PriceExtractResult priceResult) {
        StringBuilder response = new StringBuilder();
        response.append(fallbackMessage);
        appendProductList(response, products);

        // 添加建议
        response.append("\n\n建议：\n");
        if (priceResult != null && priceResult.getPrice() != null) {
            double targetPrice = priceResult.getPrice();
            response.append("1. 尝试搜索").append((int)(targetPrice * 0.8)).append("-")
                    .append((int)(targetPrice * 1.2)).append("元价位的商品\n");
        }
        if (brand != null && !brand.isEmpty()) {
            response.append("2. 查看其他品牌的").append(categoryKeywords.isEmpty() ? "商品" : categoryKeywords.get(0)).append("\n");
        }
        response.append("3. 调整价格范围（如：").append(priceResult != null ? priceResult.getPrice() : "1000")
                .append("元以下的商品）");

        return response.toString();
    }

    // 构建无商品响应
    private String buildNoProductResponse(String brand, List<String> categoryKeywords, PriceExtractResult priceResult) {
        StringBuilder response = new StringBuilder();
        response.append("暂时没有找到");

        if (brand != null && !brand.isEmpty()) {
            response.append(brand);
        }

        if (!categoryKeywords.isEmpty()) {
            for (String keyword : categoryKeywords) {
                response.append(keyword);
            }
        }

        if (priceResult != null && priceResult.getPrice() != null) {
            response.append(getPriceConditionText(priceResult));
        }

        response.append("的商品。\n\n建议：\n");

        if (priceResult != null && priceResult.getPrice() != null) {
            double targetPrice = priceResult.getPrice();
            response.append("1. 尝试搜索").append((int)(targetPrice * 0.8)).append("-")
                    .append((int)(targetPrice * 1.2)).append("元价位的商品\n");
        }

        if (brand != null && !brand.isEmpty()) {
            response.append("2. 查看其他品牌的").append(categoryKeywords.isEmpty() ? "商品" : categoryKeywords.get(0)).append("\n");
        } else {
            response.append("2. 尝试指定品牌（如：苹果、小米、华为等）\n");
        }

        if (!categoryKeywords.isEmpty()) {
            response.append("3. 查看其他类型的商品\n");
        } else {
            response.append("3. 尝试指定商品类型（如：手机、衣服、电脑等）\n");
        }

        response.append("4. 调整价格范围（如：").append(priceResult != null && priceResult.getPrice() != null ?
                priceResult.getPrice() + "元以下" : "1000-2000元").append("）");

        return response.toString();
    }

    // 添加商品列表
    private void appendProductList(StringBuilder response, List<Product> products) {
        // 只取前10个商品
        int displayCount = Math.min(products.size(), 10);

        // 使用表格格式
        response.append("\n┌─────────────────────────────────────────────────────────────────┐\n");
        response.append("│ 🛍️  商品列表                                                      │\n");
        response.append("├─────────┬────────────────────────────────┬─────────┬────────────┤\n");
        response.append("│   编号  │          商品名称              │  价格   │    详情    │\n");
        response.append("├─────────┼────────────────────────────────┼─────────┼────────────┤\n");

        for (int i = 0; i < displayCount; i++) {
            Product p = products.get(i);

            // 截取商品名称，避免过长
            String productName = p.getName();
            if (productName.length() > 30) {
                productName = productName.substring(0, 27) + "...";
            }

            // 格式化价格
            String priceStr = String.format("%.2f", p.getPrice()) + "元";

            // 截取详情，避免过长
            String subTitle = p.getSubTitle();
            if (subTitle == null) subTitle = "";
            if (subTitle.length() > 12) {
                subTitle = subTitle.substring(0, 9) + "...";
            }

            response.append(String.format("│   %2d    │ %-30s │ %7s │ %-10s │\n",
                    i + 1, productName, priceStr, subTitle));
        }

        response.append("└─────────┴────────────────────────────────┴─────────┴────────────┘\n");

        // 提示总数量
        if (products.size() > displayCount) {
            response.append("\n📊 **提示**：查询到 ").append(products.size())
                    .append(" 个商品，已显示前 ").append(displayCount).append(" 个。\n");
        }
    }

    // 构建具体型号的响应
    private String buildSpecificModelResponse(List<Product> products, String model) {
        if (products.isEmpty()) {
            return "抱歉，没有找到" + model + "的相关商品信息。";
        }

        StringBuilder response = new StringBuilder();
        response.append("为您查询到").append(model).append("的价格信息：\n");

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            response.append(i + 1).append(". ").append(p.getName())
                    .append(" - 价格：").append(p.getPrice())
                    .append("元\n   详情：").append(p.getSubTitle()).append("\n");
        }

        return response.toString();
    }

    // 提取价格信息
    // 提取价格信息 - 修改以支持价格区间
    // 修改 extractPriceInfo 方法，确保正确提取"以下"条件
    public PriceExtractResult extractPriceInfo(String question) {
        if (question == null || question.trim().isEmpty()) {
            return null;
        }

        // 1. 首先尝试提取价格区间
        Map<String, Double> range = extractPriceRangeFromQuestion(question);
        if (range != null) {
            try {
                PriceExtractResult result = new PriceExtractResult();
                result.setMinPrice(range.get("min"));
                result.setMaxPrice(range.get("max"));
                result.setCondition(PriceCondition.RANGE);
                return result;
            } catch (Exception e) {
                log.error("创建价格区间对象失败", e);
            }
        }

        // 2. 如果没有区间，尝试提取单个价格条件
        // 增强正则表达式，确保能匹配"500元以下"这样的格式
        Pattern pattern = Pattern.compile("(\\d+)\\s*(元|块钱)?\\s*(以上|及以上|左右|以下|以内|之内)?");
        Matcher matcher = pattern.matcher(question);

        if (matcher.find()) {
            try {
                Double price = Double.parseDouble(matcher.group(1));
                String condition = matcher.group(3);

                PriceExtractResult result = new PriceExtractResult();
                result.setPrice(price);

                if (condition != null) {
                    if (condition.contains("以上") || condition.contains("及以上")) {
                        result.setCondition(PriceCondition.ABOVE);
                    } else if (condition.contains("以下") || condition.contains("以内") || condition.contains("之内")) {
                        result.setCondition(PriceCondition.BELOW);
                    } else if (condition.contains("左右")) {
                        result.setCondition(PriceCondition.AROUND);
                    } else {
                        result.setCondition(PriceCondition.EXACT);
                    }
                } else {
                    // 如果没有明确条件，根据上下文推断
                    if (question.contains("以上") || question.contains("及以上")) {
                        result.setCondition(PriceCondition.ABOVE);
                    } else if (question.contains("以下") || question.contains("以内") || question.contains("之内")) {
                        result.setCondition(PriceCondition.BELOW);
                    } else if (question.contains("左右")) {
                        result.setCondition(PriceCondition.AROUND);
                    } else {
                        result.setCondition(PriceCondition.EXACT);
                    }
                }

                log.info("提取价格信息成功：价格={}, 条件={}", price, result.getCondition());
                return result;
            } catch (NumberFormatException e) {
                log.error("解析价格失败", e);
                return null;
            }
        }

        log.info("未提取到价格信息");
        return null;
    }

    // 获取价格条件文本
    private String getPriceConditionText(PriceExtractResult priceResult) {
        if (priceResult == null || priceResult.getPrice() == null) {
            return "";
        }

        switch (priceResult.getCondition()) {
            case ABOVE:
                return priceResult.getPrice() + "元以上";
            case BELOW:
                return priceResult.getPrice() + "元以下";
            case AROUND:
                return priceResult.getPrice() + "元左右";
            case EXACT:
                return priceResult.getPrice() + "元";
            default:
                return priceResult.getPrice() + "元";
        }
    }

    // 判断是否为价格查询请求
    private boolean isPriceQueryRequest(String question) {
        return question.matches(".*(多少钱|什么价|价格|价钱|价|多钱|卖多少).*") &&
                (extractBrandFromQuestion(question) != null ||
                        extractProductModelFromQuestion(question) != null ||
                        !extractCategoryKeywordsFromQuestion(question).isEmpty());
    }

    // 判断是否为商品推荐请求
    public boolean isProductRecommendationRequest(String question) {

        return isPriceQueryRequest(question) ||
                question.matches(".*(推荐|有没有|买|想买|想要|咨询|查询|看看|看下|看|找|寻找|搜索|介绍一下|介绍|了解|想了解).*(苹果|小米|华为|三星|联想|索尼)?(手机|衣服|衣物|电视|笔记本|电脑|空调|配件|家用|电器|商品|产品).*") ||
                question.matches(".*(\\d+)(元|块钱)?(以上|及以上|左右|以下|以内).*(苹果|小米|华为|三星|联想|索尼)?(手机|衣服|衣物|电视|笔记本|电脑|空调|配件|家用|电器).*") ||
                // 检查是否包含已知的商品分类关键词
                !extractCategoryKeywordsFromQuestion(question).isEmpty();
    }

    // 从用户问题中提取品牌
    private String extractBrandFromQuestion(String question) {
        // 转换为小写，不区分大小写
        String lowerQuestion = question.toLowerCase();
        // 苹果品牌匹配增强
        if (lowerQuestion.contains("苹果") ||
                lowerQuestion.contains("apple") ||
                lowerQuestion.contains("iphone") ||
                question.contains("苹果") ||  // 保留原始大小写匹配
                question.contains("Apple") ||
                question.contains("iPhone")) {
            return "苹果";
        }
        if (question.contains("苹果") || question.contains("Apple")) {
            return "苹果";
        } else if (question.contains("小米") || question.contains("MI") || question.contains("mi") || question.contains("红米")) {
            return "小米";
        } else if (question.contains("华为") || question.contains("HUAWEI")) {
            return "华为";
        } else if (question.contains("三星") || question.contains("SAMSUNG")) {
            return "三星";
        } else if (question.contains("联想") || question.contains("Lenovo")) {
            return "联想";
        } else if (question.contains("索尼") || question.contains("SONY")) {
            return "索尼";
        } else if (question.contains("LG")) {
            return "LG";
        } else if (question.contains("TCL")) {
            return "TCL";
        } else if (question.contains("美的") || question.contains("Midea")) {
            return "美的";
        } else if (question.contains("海尔") || question.contains("Haier")) {
            return "海尔";
        } else if (question.contains("格力") || question.contains("GREE")) {
            return "格力";
        } else if (question.contains("海信") || question.contains("Hisense")) {
            return "海信";
        } else if (question.contains("康佳") || question.contains("KONKA")) {
            return "康佳";
        } else if (question.contains("先锋") || question.contains("Pioneer")) {
            return "先锋";
        } else if (question.contains("爱国者") || question.contains("aiguo")) {
            return "爱国者";
        } else if (question.contains("罗马仕") || question.contains("ROMOSS")) {
            return "罗马仕";
        } else if (question.contains("半岛铁盒")) {
            return "半岛铁盒";
        } else if (question.contains("鄂尔多斯") || question.contains("Erdos")) {
            return "鄂尔多斯";
        } else if (question.contains("拉夏贝尔") || question.contains("La Chapelle")) {
            return "拉夏贝尔";
        } else if (question.contains("朗姿") || question.contains("LANC")) {
            return "朗姿";
        } else if (question.contains("南极人") || question.contains("NanJiren")) {
            return "南极人";
        } else if (question.contains("vivo") || question.contains("VIVO")) {
            return "vivo";
        } else if (question.contains("oppo") || question.contains("OPPO")) {
            return "OPPO";
        } else if (question.contains("魅族") || question.contains("MEIZU")) {
            return "魅族";
        } else if (question.contains("努比亚") || question.contains("nubia")) {
            return "努比亚";
        } else if (question.contains("一加") || question.contains("OnePlus")) {
            return "一加";
        } else if (question.contains("realme") || question.contains("Realme")) {
            return "realme";
        } else if (question.contains("荣耀") || question.contains("HONOR")) {
            return "荣耀";
        } else if (question.contains("金一")) {
            return "金一";
        }
        return "";
    }

    // 从用户问题中提取商品型号
    private String extractProductModelFromQuestion(String question) {
        // 处理iPhone型号，增强6s Plus的识别
        Pattern iphonePattern = Pattern.compile(
                "(iPhone|iphone|苹果iPhone|苹果 iPhone)?\\s*" +  // 可选品牌前缀
                        "(\\d+[s]?)\\s*" +  // 型号（如6、6s、7等）
                        "(Plus|plus|Pro|pro|Max|max)?"  // 可选后缀
                , Pattern.CASE_INSENSITIVE);

        Matcher iphoneMatcher = iphonePattern.matcher(question);
        if (iphoneMatcher.find()) {
            StringBuilder model = new StringBuilder();
            if (iphoneMatcher.group(1) != null && !iphoneMatcher.group(1).isEmpty()) {
                model.append(iphoneMatcher.group(1)).append(" ");
            }

            String number = iphoneMatcher.group(2);
            String suffix = iphoneMatcher.group(3);

            if (number != null) {
                model.append(number);
                if (suffix != null && !suffix.isEmpty()) {
                    model.append(" ").append(suffix);
                }
            }

            return model.toString().trim();
        }

        // 常见手机型号模式
        Pattern phoneModelPattern = Pattern.compile(
                "(小米|MI|红米|Redmi|华为|HUAWEI|荣耀|HONOR|苹果|iPhone|三星|SAMSUNG)" +
                        "\\s*[\\-\\s]?\\s*" +
                        "([A-Za-z0-9\\s\\-\\+]+?)(?=\\s|$|多少钱|什么价|价格|价钱|价|多钱|卖多少|元)"
        );

        Matcher matcher = phoneModelPattern.matcher(question);
        if (matcher.find()) {
            String brandPart = matcher.group(1);
            String modelPart = matcher.group(2);
            if (modelPart != null && !modelPart.trim().isEmpty()) {
                return (brandPart + " " + modelPart).trim();
            }
        }

        // 如果没有匹配到完整模式，尝试匹配常见型号关键词
        if (question.contains("红米4A") || question.contains("Redmi 4A") || question.contains("Redmi4A")) {
            return "红米 4A";
        } else if (question.contains("小米8") || question.contains("MI 8") || question.contains("MI8")) {
            return "小米 8";
        } else if (question.contains("小米9") || question.contains("MI 9") || question.contains("MI9")) {
            return "小米 9";
        } else if (question.contains("小米10") || question.contains("MI 10") || question.contains("MI10")) {
            return "小米 10";
        } else if (question.contains("iPhone 11") || question.contains("iPhone11")) {
            return "iPhone 11";
        } else if (question.contains("iPhone 12") || question.contains("iPhone12")) {
            return "iPhone 12";
        } else if (question.contains("iPhone 13") || question.contains("iPhone13")) {
            return "iPhone 13";
        } else if (question.contains("华为P40") || question.contains("P40")) {
            return "华为 P40";
        } else if (question.contains("华为Mate40") || question.contains("Mate40")) {
            return "华为 Mate 40";
        } else if (question.contains("黄金项链")) {
            return "黄金项链";
        }
        // 增强iPhone 6s Plus识别
        else if (question.contains("iPhone 6s Plus") || question.contains("iPhone6sPlus") ||
                question.contains("6s Plus") || question.contains("6s plus")) {
            return "iPhone 6s Plus";
        }
        // 增强iPhone 6s识别
        else if (question.contains("iPhone 6s") || question.contains("iPhone6s") ||
                question.contains("6s")) {
            return "iPhone 6s";
        }

        return null;
    }

    // 从用户问题中提取类别关键词
    private List<String> extractCategoryKeywordsFromQuestion(String question) {
        List<String> keywords = new ArrayList<>();

        if (question.contains("手机") || question.contains("iPhone") || question.contains("电话") ||
                question.contains("智能手机") || question.contains("移动电话")) {
            keywords.add("手机");
            // 同时添加电商知识库中的分类名称
            keywords.add("手机数码");
        }
        // 检查是否有具体的服饰分类关键词
        boolean hasSpecificClothingKeyword = false;
        if (question.contains("特色女装") || question.contains("女装") || question.contains("女士服装") ||
                question.contains("精品男装") || question.contains("男装") || question.contains("男士服装") ||
                question.contains("潮流女鞋") || question.contains("女鞋") || question.contains("高跟鞋") ||
                question.contains("品质男鞋") || question.contains("男鞋") || question.contains("皮鞋")) {
            hasSpecificClothingKeyword = true;
        }
        // 只有当没有具体服饰分类关键词时，才添加通用的"服饰"关键词
        if (!hasSpecificClothingKeyword && (question.contains("衣服") || question.contains("衣物") ||
                question.contains("礼服") || question.contains("连衣裙") ||
                question.contains("婚纱") || question.contains("大衣") ||
                question.contains("西装") || question.contains("外套") ||
                question.contains("服饰") || question.contains("上衣") ||
                question.contains("裤子") || question.contains("裙子") ||
                question.contains("T恤") || question.contains("衬衫"))) {
            keywords.add("服饰");
        }
        if (question.contains("电视") || question.contains("电视机") || question.contains("液晶电视")) {
            keywords.add("电视");
        }
        if (question.contains("笔记本") || question.contains("电脑") ||
                question.contains("MacBook") || question.contains("台式机") ||
                question.contains("计算机") || question.contains("笔记本电脑")) {
            keywords.add("电脑");
        }
        if (question.contains("空调")) {
            keywords.add("空调");
        }
        if (question.contains("配件") || question.contains("钢化膜") ||
                question.contains("移动电源") || question.contains("充电宝") ||
                question.contains("耳机") || question.contains("数据线") ||
                question.contains("充电器") || question.contains("保护壳")) {
            keywords.add("配件");
        }
        if (question.contains("家用") || question.contains("电器") ||
                question.contains("家居") || question.contains("冰箱") ||
                question.contains("洗衣机") || question.contains("微波炉") ||
                question.contains("电饭煲") || question.contains("吸尘器")) {
            keywords.add("家居");
        }
        if (question.contains("手表") || question.contains("手环") || question.contains("智能手表")) {
            keywords.add("手表");
        }
        if (question.contains("平板") || question.contains("iPad") || question.contains("平板电脑")) {
            keywords.add("平板");
        }
        if (question.contains("相机") || question.contains("摄像机") || question.contains("数码相机")) {
            keywords.add("相机");
        }
        if (question.contains("黄金") || question.contains("项链") || question.contains("珠宝")) {
            keywords.add("珠宝饰品");
        }
        // 新增：特定商品类别的关键词识别
        if (question.contains("特色女装") || question.contains("女装") || question.contains("女士服装")) {
            keywords.add("特色女装");
        }
        if (question.contains("精品男装") || question.contains("男装") || question.contains("男士服装")) {
            keywords.add("精品男装");
        }
        if (question.contains("潮流女鞋") || question.contains("女鞋") || question.contains("高跟鞋")) {
            keywords.add("潮流女鞋");
        }
        if (question.contains("品质男鞋") || question.contains("男鞋") || question.contains("皮鞋")) {
            keywords.add("品质男鞋");
        }
        if (question.contains("功能箱包") || question.contains("箱包") || question.contains("钱包")) {
            keywords.add("功能箱包");
        }
        if (question.contains("黄金首饰") || question.contains("黄金") || question.contains("首饰")) {
            keywords.add("黄金首饰");
        }
        if (question.contains("钻石彩宝") || question.contains("钻石") || question.contains("彩宝")) {
            keywords.add("钻石彩宝");
        }
        if (question.contains("面部护理") || question.contains("面部") || question.contains("护肤")) {
            keywords.add("面部护理");
        }

        return keywords;
    }

    // 从问题中提取品牌和类别ID（用于上下文存储）
    public Map<String, Object> extractContextFromQuestion(String question) {
        Map<String, Object> context = new HashMap<>();

        String brand = extractBrandFromQuestion(question);
        List<String> categoryKeywords = extractCategoryKeywordsFromQuestion(question);

        List<Long> categoryIds = new ArrayList<>();
        for (String keyword : categoryKeywords) {
            List<Long> ids = categoryService.getCategoryIdsByKeyword(keyword);
            if (ids != null) {
                categoryIds.addAll(ids);
            }
        }

        context.put("brand", brand);
        context.put("categoryIds", categoryIds);
        context.put("previousQuery", question);

        PriceExtractResult priceResult = extractPriceInfo(question);
        if (priceResult != null && priceResult.getPrice() != null) {
            context.put("priceResult", priceResult);
        }

        return context;
    }

    // 非流式聊天
    public String simpleChat(String userQuestion, String knowledge) {
        return processLocalChat(userQuestion, knowledge);
    }

    // ==================== 订单查询相关功能 ====================

    /**
     * 判断是否为订单查询请求
     */
    public boolean isOrderQueryRequest(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 1. 首先检查是否包含订单号格式（O + 20位数字）
        if (question.matches(".*O\\d{20}.*") || question.matches("^\\d{14,20}$")) {
            return true;
        }
        
        // 2. 订单查询关键词
        return lowerQuestion.matches(".*(订单|我的订单|查订单|查看订单|订单状态|订单进度|物流|配送|发货).*") ||
               lowerQuestion.matches(".*(订单号|单号|编号).*(o\\d{20}|\\d{14,20}).*") ||
               lowerQuestion.matches(".*(到哪了|发了没|什么时候到|几号到|收到没).*");
    }

    /**
     * 处理订单查询请求
     */
    public String handleOrderQuery(String userQuestion, Long userId) {
        log.info("处理订单查询请求：{}, userId={}", userQuestion, userId);

        // 1. 尝试从问题中提取订单号
        String orderNo = extractOrderNoFromQuestion(userQuestion);
        
        if (orderNo != null && !orderNo.isEmpty()) {
            // 查询特定订单
            return querySpecificOrder(orderNo);
        }

        // 2. 如果没有订单号，查询用户所有订单
        if (userId != null) {
            // 判断是否查询特定状态的订单
            Integer status = extractOrderStatusFromQuestion(userQuestion);
            return queryUserOrders(userId, status);
        }

        return "请提供您的订单号,或者告诉我您想查询哪种状态的订单(如:待付款、已发货等)。";
    }

    /**
     * 从问题中提取订单号
     */
    private String extractOrderNoFromQuestion(String question) {
        // 匹配格式: O + 14位数字 + 6位随机数
        Pattern pattern = Pattern.compile("(O\\d{20})");
        Matcher matcher = pattern.matcher(question);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 尝试匹配纯数字订单号
        pattern = Pattern.compile("(\\d{14,20})");
        matcher = pattern.matcher(question);
        
        if (matcher.find()) {
            String number = matcher.group(1);
            // 如果是纯数字，尝试添加O前缀
            if (number.length() == 20) {
                return "O" + number;
            }
        }
        
        return null;
    }

    /**
     * 从问题中提取订单状态
     */
    private Integer extractOrderStatusFromQuestion(String question) {
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.matches(".*(待付款|未付款|等待付款|待支付).*")) {
            return 10; // 待付款
        } else if (lowerQuestion.matches(".*(已付款|已支付|付过款).*")) {
            return 20; // 已付款
        } else if (lowerQuestion.matches(".*(待发货|未发货|等待发货).*")) {
            return 30; // 待发货
        } else if (lowerQuestion.matches(".*(已发货|发货了|在途|配送中).*")) {
            return 40; // 已发货
        } else if (lowerQuestion.matches(".*(待收货|未收货|等待收货).*")) {
            return 50; // 待收货
        } else if (lowerQuestion.matches(".*(已完成|已收货|完成).*")) {
            return 60; // 已完成
        } else if (lowerQuestion.matches(".*(已取消|取消了).*")) {
            return 70; // 已取消
        } else if (lowerQuestion.matches(".*(退款中|申请退款).*")) {
            return 80; // 退款中
        } else if (lowerQuestion.matches(".*(已退款|退款成功).*")) {
            return 90; // 已退款
        }
        
        return null; // 查询所有订单
    }

    /**
     * 查询特定订单
     */
    private String querySpecificOrder(String orderNo) {
        OrderInfo orderInfo = orderService.getOrderByOrderNo(orderNo);
        
        if (orderInfo == null) {
            return "未找到订单号为【" + orderNo + "】的订单信息，请检查订单号是否正确。";
        }
        
        return buildOrderDetailResponse(orderInfo);
    }

    /**
     * 查询用户订单列表
     */
    private String queryUserOrders(Long userId, Integer status) {
        List<OrderInfo> orders = orderService.getOrdersByUserId(userId, status);
        
        if (orders == null || orders.isEmpty()) {
            if (status != null) {
                return "您暂时没有状态为【" + getOrderStatusDesc(status) + "】的订单。";
            }
            return "您暂时还没有订单哦，快去看看喜欢的商品吧！";
        }
        
        return buildOrderListResponse(orders, status);
    }

    /**
     * 构建订单详情响应
     */
    private String buildOrderDetailResponse(OrderInfo orderInfo) {
        StringBuilder response = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        response.append("📦 **订单详情**\n\n");
        response.append("🔖 **订单编号**：").append(orderInfo.getOrderNo()).append("\n");
        response.append("📌 **订单状态**：").append(orderInfo.getStatusDesc()).append("\n");
        response.append("💰 **订单金额**：").append(orderInfo.getPayment()).append(" 元\n");
        
        // 根据状态显示不同信息
        if (orderInfo.getStatus() == 10) {
            // 待付款
            response.append("⏰ **下单时间**：").append(dateFormat.format(orderInfo.getCreateTime())).append("\n");
            if (orderInfo.getExpireTime() != null) {
                response.append("⚠️ **超时时间**：").append(dateFormat.format(orderInfo.getExpireTime())).append("\n");
                response.append("\n💡 **温馨提示**：订单将在超时后自动取消，请及时支付。");
            }
        } else if (orderInfo.getStatus() == 20) {
            // 已付款
            if (orderInfo.getPaymentTime() != null) {
                response.append("💳 **支付时间**：").append(dateFormat.format(orderInfo.getPaymentTime())).append("\n");
            }
            if (orderInfo.getPaymentTypeDesc() != null) {
                response.append("💳 **支付方式**：").append(orderInfo.getPaymentTypeDesc()).append("\n");
            }
            response.append("\n💡 **温馨提示**：您的订单正在处理中，请耐心等待。");
        } else if (orderInfo.getStatus() == 30) {
            // 待发货
            if (orderInfo.getPaymentTime() != null) {
                response.append("💳 **支付时间**：").append(dateFormat.format(orderInfo.getPaymentTime())).append("\n");
            }
            if (orderInfo.getPaymentTypeDesc() != null) {
                response.append("💳 **支付方式**：").append(orderInfo.getPaymentTypeDesc()).append("\n");
            }
            response.append("\n💡 **温馨提示**：您的订单正在准备中，预计1-3个工作日内发货。");
        } else if (orderInfo.getStatus() == 40) {
            // 已发货
            if (orderInfo.getShipTime() != null) {
                response.append("🚚 **发货时间**：").append(dateFormat.format(orderInfo.getShipTime())).append("\n");
            }
            response.append("📍 **收货地址**：").append(buildAddressString(orderInfo)).append("\n");
            response.append("📞 **收货人**：").append(orderInfo.getReceiveName())
                    .append(" ").append(orderInfo.getReceivePhone()).append("\n");
            response.append("\n💡 **温馨提示**：商品已发出，预计3-7天送达，请保持电话畅通。");
        } else if (orderInfo.getStatus() == 50) {
            // 待收货
            if (orderInfo.getPaymentTime() != null) {
                response.append("💳 **支付时间**：").append(dateFormat.format(orderInfo.getPaymentTime())).append("\n");
            }
            if (orderInfo.getPaymentTypeDesc() != null) {
                response.append("💳 **支付方式**：").append(orderInfo.getPaymentTypeDesc()).append("\n");
            }
            if (orderInfo.getShipTime() != null) {
                response.append("🚚 **发货时间**：").append(dateFormat.format(orderInfo.getShipTime())).append("\n");
            }
            response.append("📍 **收货地址**：").append(buildAddressString(orderInfo)).append("\n");
            response.append("📞 **收货人**：").append(orderInfo.getReceiveName())
                    .append(" ").append(orderInfo.getReceivePhone()).append("\n");
            response.append("\n💡 **温馨提示**：商品正在配送中，请注意查收，收到后请确认收货。");
        } else if (orderInfo.getStatus() == 60) {
            // 已完成
            response.append("✅ **收货时间**：").append(dateFormat.format(orderInfo.getReceiveTime())).append("\n");
            response.append("\n💡 **温馨提示**：感谢您的购买，期待您的再次光临！");
        } else if (orderInfo.getStatus() == 70) {
            // 已取消
            response.append("❌ **取消时间**：").append(dateFormat.format(orderInfo.getCancelTime())).append("\n");
            if (orderInfo.getCancelReason() != null) {
                response.append("📝 **取消原因**：").append(orderInfo.getCancelReason()).append("\n");
            }
        } else if (orderInfo.getStatus() == 90) {
            // 已退款
            response.append("💸 **退款金额**：").append(orderInfo.getRefundAmount()).append(" 元\n");
            response.append("⏰ **退款时间**：").append(dateFormat.format(orderInfo.getRefundTime())).append("\n");
            if (orderInfo.getRefundReason() != null) {
                response.append("📝 **退款原因**：").append(orderInfo.getRefundReason()).append("\n");
            }
        }
        
        // 显示订单商品信息
        if (orderInfo.getOrderDetails() != null && !orderInfo.getOrderDetails().isEmpty()) {
            response.append("\n📋 **订单商品**：\n");
            for (int i = 0; i < orderInfo.getOrderDetails().size(); i++) {
                OrderDetail detail = orderInfo.getOrderDetails().get(i);
                response.append((i + 1)).append(". ").append(detail.getProductName())
                        .append(" × ").append(detail.getQuantity())
                        .append(" (¥").append(detail.getCurrentUnitPrice()).append(")\n");
            }
        }
        
        return response.toString();
    }

    /**
     * 构建订单列表响应
     */
    private String buildOrderListResponse(List<OrderInfo> orders, Integer status) {
        StringBuilder response = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        if (status != null) {
            response.append("📋 **").append(getOrderStatusDesc(status)).append("订单列表**\n\n");
        } else {
            response.append("📋 **我的订单列表**\n\n");
        }
        
        int displayCount = Math.min(orders.size(), 5);
        for (int i = 0; i < displayCount; i++) {
            OrderInfo order = orders.get(i);
            response.append((i + 1)).append(". 订单号：").append(order.getOrderNo()).append("\n");
            response.append("   状态：").append(order.getStatusDesc()).append("\n");
            response.append("   金额：¥").append(order.getPayment()).append("\n");
            response.append("   时间：").append(dateFormat.format(order.getCreateTime())).append("\n");
            
            if (i < displayCount - 1) {
                response.append("\n");
            }
        }
        
        if (orders.size() > displayCount) {
            response.append("\n📊 共 ").append(orders.size())
                    .append(" 个订单，已显示最新 ").append(displayCount).append(" 个。\n");
        }
        
        response.append("\n💡 **提示**：发送订单号可查看详细信息。");
        
        return response.toString();
    }

    /**
     * 构建地址字符串
     */
    private String buildAddressString(OrderInfo orderInfo) {
        StringBuilder address = new StringBuilder();
        
        if (orderInfo.getProvince() != null) {
            address.append(orderInfo.getProvince());
        }
        if (orderInfo.getCity() != null) {
            address.append(orderInfo.getCity());
        }
        if (orderInfo.getArea() != null) {
            address.append(orderInfo.getArea());
        }
        if (orderInfo.getStreet() != null) {
            address.append(orderInfo.getStreet());
        }
        
        return address.toString();
    }

    /**
     * 获取订单状态描述
     */
    private String getOrderStatusDesc(Integer status) {
        if (status == null) return "未知";
        
        switch (status) {
            case 10: return "待付款";
            case 20: return "已付款";
            case 30: return "待发货";
            case 40: return "已发货";
            case 50: return "待收货";
            case 60: return "已完成";
            case 70: return "已取消";
            case 80: return "退款中";
            case 90: return "已退款";
            default: return "未知";
        }
    }
}
